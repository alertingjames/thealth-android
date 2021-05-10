package com.app.thealth.main;

import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abdulhakeem.seemoretextview.SeeMoreTextView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.thealth.R;
import com.app.thealth.adapters.FeedCommentListAdapter;
import com.app.thealth.adapters.PictureListAdapter;
import com.app.thealth.base.BaseActivity;
import com.app.thealth.classes.DateMain;
import com.app.thealth.commons.Commons;
import com.app.thealth.commons.ReqConst;
import com.app.thealth.models.Feed;
import com.app.thealth.models.FeedComment;
import com.app.thealth.models.Picture;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.rd.PageIndicatorView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedDetailActivity extends BaseActivity {

    ViewPager pager;
    PageIndicatorView pageIndicatorView;
    AVLoadingIndicatorView progressBar;
    ImageView downloader;
    LinearLayout commentsFrame;
    TextView commentsbox;
    ImageView commentButton;
    ArrayList<Picture> pictures = new ArrayList<>();
    PictureListAdapter adapter = new PictureListAdapter(this);
    ArrayList<FeedComment> comments = new ArrayList<>();
    FeedCommentListAdapter feedCommentListAdapter = new FeedCommentListAdapter(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_detail);

        CircleImageView userPictureBox = (CircleImageView) findViewById(R.id.userPictureBox);
        ProgressBar progressBar_user = (ProgressBar) findViewById(R.id.progressBar_user);
        TextView userNameBox = (TextView) findViewById(R.id.userNameBox);
        TextView addressBox = (TextView) findViewById(R.id.addressBox);

        TextView postedDateBox = (TextView) findViewById(R.id.postedDateBox);
        commentButton = (ImageView) findViewById(R.id.btn_comment);
        ImageView shareButton = (ImageView) findViewById(R.id.btn_share);
        ImageView saveButton = (ImageView) findViewById(R.id.btn_save);
        TextView noteBox = (TextView) findViewById(R.id.descriptionBox);
        TextView likesBox = (TextView) findViewById(R.id.likesBox);
        commentsbox = (TextView) findViewById(R.id.commentsBox);
        ImageView menuButton = (ImageView) findViewById(R.id.btn_menu);
        LikeButton likeButton = (LikeButton) findViewById(R.id.likeButton);

        LinearLayout mediaFrame = (LinearLayout) findViewById(R.id.mediaFrame);
        LinearLayout textFrame1 = (LinearLayout) findViewById(R.id.textFrame1);
        LinearLayout textFrame2 = (LinearLayout) findViewById(R.id.textFrame2);
        TextView noteBox1 = (TextView) findViewById(R.id.descriptionBox1);
        TextView postedDateBox1 = (TextView) findViewById(R.id.postedDateBox1);

        commentsFrame = (LinearLayout)findViewById(R.id.commentsFrame);

        pager = (ViewPager) findViewById(R.id.viewPager);
        pageIndicatorView = (PageIndicatorView) findViewById(R.id.pageIndicatorView);
        TextView titleBox = (TextView)findViewById(R.id.titleBox);
        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        titleBox.setText(Commons.feed.getUserName() + "'s " + getString(R.string.post));
        downloader=(ImageView)findViewById(R.id.download);
        downloader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                (drawableToBitmap(LoadImageFromWebOperations(pictures.get(pager.getCurrentItem()).getUrl()))).compress(Bitmap.CompressFormat.JPEG, 90, bytes);

                String path = Environment.getExternalStorageDirectory()+"/Pictures";
                File storageDir = new File(path);
                if (!storageDir.exists() && !storageDir.mkdir()) {
                    return;
                }

                File file = new File(path,
                        System.currentTimeMillis() + ".jpg");

                FileOutputStream fo;
                try {
                    file.createNewFile();
                    fo = new FileOutputStream(file);
                    fo.write(bytes.toByteArray());
                    fo.close();

                    showToast(getString(R.string.downloaded_at) + " " + file.getPath());

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        userNameBox.setText(Commons.feed.getUserName());
        addressBox.setText(Commons.feed.getUserLocation());
        postedDateBox.setText(DateMain.getDateStr(FeedDetailActivity.this, Long.parseLong(Commons.feed.getPostedTime())));
        likesBox.setText(String.valueOf(Commons.feed.getLikes()));
        commentsbox.setText(String.valueOf(Commons.feed.getComments()));

        if(Commons.feed.getDescription().length() == 0)noteBox.setVisibility(View.GONE);
        else {
            noteBox.setVisibility(View.VISIBLE);
            noteBox.setText(StringEscapeUtils.unescapeJava(Commons.feed.getDescription()));
        }

        if(Commons.feed.getUserPhoto().length() > 0){
            Picasso.with(getApplicationContext())
                    .load(Commons.feed.getUserPhoto())
                    .error(R.drawable.user)
                    .placeholder(R.drawable.user)
                    .into(userPictureBox, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar_user.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void onError() {
                            progressBar_user.setVisibility(View.INVISIBLE);
                        }
                    });
        }

        if(Commons.feed.getPictureUrl().length() > 0){
            mediaFrame.setVisibility(View.VISIBLE);
            textFrame1.setVisibility(View.GONE);
            textFrame2.setVisibility(View.VISIBLE);
            if(Commons.feed.getDescription().length() == 0){
                noteBox.setVisibility(View.GONE);
            }
            else {
                noteBox.setVisibility(View.VISIBLE);
                noteBox.setText(StringEscapeUtils.unescapeJava(Commons.feed.getDescription()));
            }
        }else {
            mediaFrame.setVisibility(View.GONE);
            textFrame1.setVisibility(View.VISIBLE);
            noteBox1.setText(StringEscapeUtils.unescapeJava(Commons.feed.getDescription()));
            postedDateBox1.setText(DateMain.getDateStr(getApplicationContext(), Long.parseLong(Commons.feed.getPostedTime())));
            textFrame2.setVisibility(View.GONE);
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
                    Commons.commentButton = commentButton;
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

        getFeedPictures(String.valueOf(Commons.feed.getIdx()));
        getFeedComments(String.valueOf(Commons.feed.getIdx()));
    }

    public void toComment(View view){
        if(Commons.feed.getUserId() != Commons.thisUser.get_idx()) {
            Commons.commentsBox = commentsbox;
            Commons.commentButton = commentButton;
            Intent intent = new Intent(getApplicationContext(), FeedCommentsActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
        }
    }

    private Drawable LoadImageFromWebOperations(String url)
    {
        try
        {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        }catch (Exception e) {
            System.out.println("Exc="+e);
            return null;
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void back(View view){
        finish();
        overridePendingTransition(0,0);
    }

    private void getFeedPictures(String feedId) {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "getFeedPictures")
                .addBodyParameter("feed_id", feedId)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("Feed Pictures Resp!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                pictures.clear();
                                JSONArray dataArr = response.getJSONArray("data");
                                for(int i=0; i<dataArr.length(); i++) {
                                    JSONObject object = (JSONObject) dataArr.get(i);
                                    Picture picture = new Picture();
                                    picture.setUrl(object.getString("picture_url"));
                                    picture.setType("image");
                                    pictures.add(picture);
                                    adapter.setDatas(pictures);
                                    adapter.notifyDataSetChanged();
                                    pager.setAdapter(adapter);
                                }
                                if(pictures.isEmpty())pageIndicatorView.setVisibility(View.GONE);
                            }else {
                                showToast("Server issue.");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        progressBar.setVisibility(View.GONE);
                    }
                });
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

    private void getFeedComments(String feedId){
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "getFeedComments")
                .addBodyParameter("feed_id", feedId)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("Feed Pictures Resp!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                comments.clear();
                                commentsFrame.removeAllViews();
                                JSONArray commentsArr = response.getJSONArray("data");
                                for(int i=0; i<commentsArr.length(); i++){
                                    JSONObject object = (JSONObject) commentsArr.get(i);
                                    JSONObject commentObj = (JSONObject) object.getJSONObject("comment");
                                    JSONObject userObj = (JSONObject) object.getJSONObject("member");
                                    FeedComment comment = new FeedComment();
                                    comment.setIdx(commentObj.getInt("id"));
                                    comment.setFeedId(commentObj.getInt("feed_id"));
                                    comment.setUserId(commentObj.getInt("member_id"));
                                    comment.setUserName(userObj.getString("name"));
                                    comment.setUserPhoto(userObj.getString("picture_url"));
                                    comment.setComment(commentObj.getString("comment"));
                                    comment.setCommentedTime(commentObj.getString("commented_time"));
                                    comments.add(comment);

                                    LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                    View view = layoutInflater.inflate(R.layout.item_comment, null);
                                    CircleImageView picture = (CircleImageView) view.findViewById(R.id.userPicture);
                                    TextView nameBox = (TextView) view.findViewById(R.id.name);
                                    TextView dateBox = (TextView) view.findViewById(R.id.date);
                                    TextView commentBox = (TextView) view.findViewById(R.id.comment);
                                    ProgressBar prog = (ProgressBar) view.findViewById(R.id.progressBar);

                                    nameBox.setText(comment.getUserName());
                                    dateBox.setText(DateMain.getDateStr(getApplicationContext(), Long.parseLong(comment.getCommentedTime())));
                                    commentBox.setText(StringEscapeUtils.unescapeJava(comment.getComment()));
                                    Picasso.with(getApplicationContext())
                                            .load(comment.getUserPhoto())
                                            .error(R.drawable.user)
                                            .placeholder(R.drawable.user)
                                            .into(picture, new Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    prog.setVisibility(View.INVISIBLE);
                                                }
                                                @Override
                                                public void onError() {
                                                    prog.setVisibility(View.INVISIBLE);
                                                }
                                            });
                                    commentsFrame.addView(view);

                                }

                                if(comments.isEmpty())((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.VISIBLE);
                                else ((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.GONE);

                            }else {
                                showToast(getString(R.string.server_issue));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

}
























































