package com.app.thealth.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.app.thealth.R;
import com.app.thealth.commons.Commons;
import com.app.thealth.main.FeedVideoPlayActivity;
import com.app.thealth.main.ViewImageActivity;
import com.app.thealth.models.Picture;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.yqritc.scalablevideoview.ScalableType;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CapturedPictureListAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<Picture> _datas = new ArrayList<>();
    private ArrayList<Picture> _alldatas = new ArrayList<>();

    public CapturedPictureListAdapter(Context context) {
        super();
        this.context = context;
    }

    public void setDatas(ArrayList<Picture> datas) {

        _alldatas = datas;
        _datas.clear();
        _datas.addAll(_alldatas);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.item_captured_picture_list, collection, false);
        ImageView pagerImage = (ImageView) layout.findViewById(R.id.picrure);
        ImageButton videoMark = (ImageButton) layout.findViewById(R.id.videomark);

        final Picture entity = (Picture) _datas.get(position);
        if(entity.getBitmap() != null){
            pagerImage.setImageBitmap(entity.getBitmap());
            ((ProgressBar) layout.findViewById(R.id.progressBar)).setVisibility(View.INVISIBLE);
        }else if(entity.getUrl().length() > 0){
            Log.d("PICTURE URL!!!", entity.getUrl());
            Picasso.with(context)
                    .load(entity.getUrl())
                    .into(pagerImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            ((ProgressBar) layout.findViewById(R.id.progressBar)).setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError() {
                            ((ProgressBar) layout.findViewById(R.id.progressBar)).setVisibility(View.INVISIBLE);
                        }
                    });
        }

        if(entity.getType().equals("video"))videoMark.setVisibility(View.VISIBLE);
        else videoMark.setVisibility(View.GONE);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        String date = dateFormat.format(new Date());

        videoMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Commons.createPostActivity != null){
                    Commons.createPostActivity.videoFrame.setVisibility(View.VISIBLE);
                    if(entity.getUri() != null){
                        Commons.createPostActivity.videoView.setBackground(null);
                        Commons.createPostActivity.videoView.setMediaController(new MediaController(context));
                        Commons.createPostActivity.videoView.setVideoURI(entity.getUri());
                        Commons.createPostActivity.videoView.requestFocus();
                        Commons.createPostActivity.videoView.start();
                    }
                }else if(Commons.editFeedActivity != null){
                    Commons.editFeedActivity.videoFrame.setVisibility(View.VISIBLE);
                    if(entity.getUri() != null){
                        try {
                            Commons.editFeedActivity.videoView.setDataSource(Commons.editFeedActivity, entity.getUri());
                            Commons.editFeedActivity.videoView.setScalableType(ScalableType.FIT_CENTER);
                            Commons.editFeedActivity.videoView.prepareAsync(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
//                    videoView.setLooping(true);
                                    Commons.editFeedActivity.videoView.start();
                                }
                            });
                            Commons.editFeedActivity.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {

                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Commons.editFeedActivity.videoView.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                                    if(Commons.editFeedActivity.videoView.isPlaying()){
                                        Commons.editFeedActivity.videoView.pause();
                                    }
                                    else {
                                        Commons.editFeedActivity.videoView.start();
                                    }
                                }
                                return true;
                            }
                        });

                    }else if(entity.getVideoUrl().length() > 0){
                        Commons.editFeedActivity.progressBar2.setVisibility(View.VISIBLE);
                        Commons.editFeedActivity.thumbView.setVisibility(View.VISIBLE);
                        Picasso.with(Commons.editFeedActivity).load(Commons.feed.getPictureUrl()).into(Commons.editFeedActivity.thumbView);
                        try {
                            Uri uri = Uri.parse(entity.getVideoUrl());
                            Commons.editFeedActivity.videoView.setDataSource(Commons.editFeedActivity, uri);
                            Commons.editFeedActivity.videoView.setScalableType(ScalableType.FIT_CENTER);
                            Commons.editFeedActivity.videoView.prepareAsync(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
//                    videoView.setLooping(true);
                                    Commons.editFeedActivity.videoView.start();
                                    Commons.editFeedActivity.thumbView.setVisibility(View.GONE);
                                    Commons.editFeedActivity.progressBar2.setVisibility(View.GONE);
                                }
                            });
                            Commons.editFeedActivity.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    Commons.editFeedActivity.videoMark.setVisibility(View.VISIBLE);
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Commons.editFeedActivity.videoView.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                                    if(Commons.editFeedActivity.videoView.isPlaying()){
                                        Commons.editFeedActivity.videoView.pause();
                                        Commons.editFeedActivity.videoMark.setVisibility(View.VISIBLE);
                                    }
                                    else {
                                        Commons.editFeedActivity.videoView.start();
                                        Commons.editFeedActivity.videoMark.setVisibility(View.GONE);
                                    }
                                }
                                return true;
                            }
                        });
                    }
                }
            }
        });

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(entity.getType().equals("image")){
                    if(entity.getUrl().length() == 0){
                        Intent intent = new Intent(context, ViewImageActivity.class);
                        intent.putExtra("image", "");
                        Commons.file = entity.getFile();
                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation((Activity) context, v, context.getString(R.string.transition));
                        context.startActivity(intent, options.toBundle());
                    }else{
                        Intent intent = new Intent(context, ViewImageActivity.class);
                        intent.putExtra("image", entity.getUrl());
                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation((Activity) context, v, context.getString(R.string.transition));
                        context.startActivity(intent, options.toBundle());
                    }
                }
            }
        });

        collection.addView(layout);

        return layout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object view) {
        container.removeView((View) view);
    }

    @Override
    public int getCount() {
        return this._datas.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}