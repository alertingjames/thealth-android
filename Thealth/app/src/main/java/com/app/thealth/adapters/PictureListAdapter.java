package com.app.thealth.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.app.thealth.main.ViewImageActivity;
import com.app.thealth.models.Picture;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PictureListAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<Picture> _datas = new ArrayList<>();
    private ArrayList<Picture> _alldatas = new ArrayList<>();

    public PictureListAdapter(Context context) {
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
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.item_image_list, collection, false);
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