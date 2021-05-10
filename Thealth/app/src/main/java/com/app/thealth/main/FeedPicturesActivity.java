package com.app.thealth.main;

import androidx.appcompat.app.AppCompatActivity;
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
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class FeedPicturesActivity extends AppCompatActivity {

    ViewPager pager;
    PageIndicatorView pageIndicatorView;
    AVLoadingIndicatorView progressBar;
    ImageView downloader;
    TextView commentsbox;
    ImageView commentButton;
    ArrayList<Picture> pictures = new ArrayList<>();
    PictureListAdapter adapter = new PictureListAdapter(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_pictures);

        commentButton = (ImageView) findViewById(R.id.btn_comment);
        ImageView saveButton = (ImageView) findViewById(R.id.btn_save);
        TextView likesBox = (TextView) findViewById(R.id.likesBox);
        commentsbox = (TextView) findViewById(R.id.commentsBox);
        LikeButton likeButton = (LikeButton) findViewById(R.id.likeButton);

        pager = (ViewPager) findViewById(R.id.viewPager);
        pageIndicatorView = (PageIndicatorView) findViewById(R.id.pageIndicatorView);
        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);
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

        likesBox.setText(String.valueOf(Commons.feed.getLikes()));
        commentsbox.setText(String.valueOf(Commons.feed.getComments()));

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
    }

    public void showToast(String content){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = FeedPicturesActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.toast_view, null);
                TextView textView=(TextView)dialogView.findViewById(R.id.text);
                textView.setText(content);
                Toast toast=new Toast(FeedPicturesActivity.this);
                toast.setView(dialogView);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        });
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
                                    pictures.add(0, picture);
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

}
























































