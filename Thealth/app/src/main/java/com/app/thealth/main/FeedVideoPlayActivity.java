package com.app.thealth.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.thealth.R;
import com.app.thealth.base.BaseActivity;
import com.app.thealth.classes.DateMain;
import com.app.thealth.commons.Commons;
import com.app.thealth.commons.ReqConst;
import com.app.thealth.models.Feed;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;
import com.yqritc.scalablevideoview.ScalableType;
import com.yqritc.scalablevideoview.ScalableVideoView;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class FeedVideoPlayActivity extends AppCompatActivity {

    private ScalableVideoView videoView;
    private ImageView thumbView;
    private ImageButton videoMark;
    private LinearLayout descFrame;
    private ImageView descButton;
    private ProgressBar progressBar;
    TextView commentsbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_video_play);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        thumbView = (ImageView)findViewById(R.id.thumbView);
        videoMark = (ImageButton)findViewById(R.id.videomark);
        Picasso.with(this).load(Commons.feed.getPictureUrl()).into(thumbView);
        thumbView.setVisibility(View.VISIBLE);
        videoView = (ScalableVideoView) findViewById(R.id.videoView);

        TextView durationBox = (TextView) findViewById(R.id.videoDurationBox);

        try {
            Uri uri = Uri.parse(Commons.feed.getVideoUrl());
            videoView.setDataSource(FeedVideoPlayActivity.this, uri);
            videoView.setScalableType(ScalableType.FIT_CENTER);
            videoView.prepareAsync(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
//                    videoView.setLooping(true);
                    videoView.start();
                    progressBar.setVisibility(View.GONE);
                    thumbView.setVisibility(View.GONE);
                }
            });
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    videoMark.setVisibility(View.VISIBLE);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    if(videoView.isPlaying()){
                        videoView.pause();
                        videoMark.setVisibility(View.VISIBLE);
                    }
                    else {
                        videoView.start();
                        videoMark.setVisibility(View.GONE);
                    }
                }
                return true;
            }
        });

        videoMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(videoView.isPlaying()){
                    videoView.pause();
                    videoMark.setVisibility(View.VISIBLE);
                }
                else {
                    videoView.start();
                    videoMark.setVisibility(View.GONE);
                }
            }
        });

        TextView postedDateBox = (TextView) findViewById(R.id.postedDateBox);
        ImageView commentButton = (ImageView) findViewById(R.id.btn_comment);
        ImageView shareButton = (ImageView) findViewById(R.id.btn_share);
        ImageView saveButton = (ImageView) findViewById(R.id.btn_save);
        TextView noteBox = (TextView) findViewById(R.id.descriptionBox);
        TextView likesBox = (TextView) findViewById(R.id.likesBox);
        commentsbox = (TextView) findViewById(R.id.commentsBox);
        LikeButton likeButton = (LikeButton) findViewById(R.id.likeButton);
        descFrame = (LinearLayout)findViewById(R.id.descFrame);
        descButton = (ImageView) findViewById(R.id.descButton);

        postedDateBox.setText(DateMain.getDateStr(FeedVideoPlayActivity.this, Long.parseLong(Commons.feed.getPostedTime())));
        likesBox.setText(String.valueOf(Commons.feed.getLikes()));
        commentsbox.setText(String.valueOf(Commons.feed.getComments()));

        if(Commons.feed.getDescription().length() == 0){
//            descButton.setVisibility(View.GONE);
        }
        else {
            noteBox.setText(StringEscapeUtils.unescapeJava(Commons.feed.getDescription()));
        }

        if(Commons.feed.isLiked())likeButton.setLiked(true);
        else likeButton.setLiked(false);

        likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                likeFeed(Commons.feed, likesBox);
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                unLikeFeed(Commons.feed, likesBox);
            }
        });

        if(Commons.feed.getComments() > 0)commentButton.setImageResource(R.drawable.ic_commented);
        else commentButton.setImageResource(R.drawable.ic_comment);
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Commons.feed.getUserId() != Commons.thisUser.get_idx()) {
                    Commons.commentsBox = commentsbox;
                    Intent intent = new Intent(getApplicationContext(), FeedCommentsActivity.class);
                    startActivity(intent);
                }else {

                }
            }
        });

        likesBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        commentsbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mediaUrl = "";
                if(Commons.feed.getPictureUrl().length() > 0)
                    mediaUrl = Commons.feed.getPictureUrl();
                else if(Commons.feed.getVideoUrl().length() > 0)
                    mediaUrl = Commons.feed.getVideoUrl();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        mediaUrl);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        if(Commons.feed.isSaved())saveButton.setImageResource(R.drawable.ic_saved);
        else saveButton.setImageResource(R.drawable.ic_save);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Commons.feed.isSaved()) {
                    saveFeed(Commons.feed, saveButton);
                }else {
                    unSaveFeed(Commons.feed, saveButton);
                }
            }
        });

//        try{
//            durationBox.setVisibility(View.VISIBLE);
//            durationBox.setText(getVideoDuration(Commons.feed.getVideoUrl()));
//        }catch (IOException e){
//            e.printStackTrace();
//        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int newOrientation = newConfig.orientation;

        if (newOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Do certain things when the user has switched to landscape.
            progressBar.setVisibility(View.VISIBLE);
            thumbView.setVisibility(View.VISIBLE);
        }else if (newOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // Do certain things when the user has switched to landscape.
            progressBar.setVisibility(View.VISIBLE);
            thumbView.setVisibility(View.VISIBLE);
        }
    }


    public void closeDescBox(View view){
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.bottom_out);
        descFrame.startAnimation(animation);
        descFrame.setVisibility(View.GONE);
    }
//
    public void back(View view){
        finish();
        overridePendingTransition(0,0);
    }
//
    public void openDescBox(View view){
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.bottom_in);
        descFrame.startAnimation(animation);
        descFrame.setVisibility(View.VISIBLE);
    }

    public String getVideoDuration(String uriOfFile) throws IOException {
        MediaPlayer mp = MediaPlayer.create(this, Uri.parse(uriOfFile));
        int duration = mp.getDuration();
        mp.release();
        /*convert millis to appropriate time*/
        return String.format("%s:%s",
                TimeUnit.MILLISECONDS.toMinutes(duration) < 10? "0" + String.valueOf(TimeUnit.MILLISECONDS.toMinutes(duration)):String.valueOf(TimeUnit.MILLISECONDS.toMinutes(duration)),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)) < 10 ? "0" + String.valueOf(TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))) : String.valueOf(TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)))
        );
    }

    public void likeFeed(Feed feed, TextView likesBox) {
        AndroidNetworking.post(ReqConst.SERVER_URL + "likeFeed")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .addBodyParameter("feed_id", String.valueOf(feed.getIdx()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("LOCRESPONSE!!!", response.toString());
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                feed.setLiked(true);
                                feed.setLikes(feed.getLikes() + 1);
                                likesBox.setText(String.valueOf(feed.getLikes()));
                                Commons.feed = feed;
                                ((LikeButton)Commons.view.findViewById(R.id.likeButton)).setLiked(true);
                                ((TextView)Commons.view.findViewById(R.id.likesBox)).setText(String.valueOf(feed.getLikes()));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {

                    }
                });
    }

    public void unLikeFeed(Feed feed, TextView likesBox) {
        AndroidNetworking.post(ReqConst.SERVER_URL + "unLikeFeed")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .addBodyParameter("feed_id", String.valueOf(feed.getIdx()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("LOCRESPONSE!!!", response.toString());
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                feed.setLiked(false);
                                feed.setLikes(feed.getLikes() - 1);
                                likesBox.setText(String.valueOf(feed.getLikes()));
                                Commons.feed = feed;
                                ((LikeButton)Commons.view.findViewById(R.id.likeButton)).setLiked(false);
                                ((TextView)Commons.view.findViewById(R.id.likesBox)).setText(String.valueOf(feed.getLikes()));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {

                    }
                });
    }

    public void saveFeed(Feed feed, ImageView saveButton) {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "saveFeed")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .addBodyParameter("feed_id", String.valueOf(feed.getIdx()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        progressBar.setVisibility(View.GONE);
                        Log.d("LOCRESPONSE!!!", response.toString());
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                saveButton.setImageResource(R.drawable.ic_saved);
                                feed.setSaved(true);
                                showToast(getString(R.string.saved));
                                Commons.feed = feed;
                                ((ImageView)Commons.view.findViewById(R.id.btn_save)).setImageResource(R.drawable.ic_saved);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    public void unSaveFeed(Feed feed, ImageView saveButton) {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "unSaveFeed")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .addBodyParameter("feed_id", String.valueOf(feed.getIdx()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        progressBar.setVisibility(View.GONE);
                        Log.d("LOCRESPONSE!!!", response.toString());
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                saveButton.setImageResource(R.drawable.ic_save);
                                feed.setSaved(false);
                                showToast(getString(R.string.unsaved));
                                Commons.feed = feed;
                                ((ImageView)Commons.view.findViewById(R.id.btn_save)).setImageResource(R.drawable.ic_save);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    public void showToast(String content){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = FeedVideoPlayActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.toast_view, null);
                TextView textView=(TextView)dialogView.findViewById(R.id.text);
                textView.setText(content);
                Toast toast=new Toast(FeedVideoPlayActivity.this);
                toast.setView(dialogView);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

}




























