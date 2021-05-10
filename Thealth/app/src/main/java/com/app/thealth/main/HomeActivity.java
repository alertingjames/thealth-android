package com.app.thealth.main;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityOptionsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.abdulhakeem.seemoretextview.SeeMoreTextView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.thealth.R;
import com.app.thealth.base.BaseActivity;
import com.app.thealth.classes.CustomSwipeRefreshLayout;
import com.app.thealth.classes.DateMain;
import com.app.thealth.commons.Commons;
import com.app.thealth.commons.ReqConst;
import com.app.thealth.models.Feed;
import com.app.thealth.models.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;
import com.yqritc.scalablevideoview.ScalableType;
import com.yqritc.scalablevideoview.ScalableVideoView;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends BaseActivity implements SwipyRefreshLayout.OnRefreshListener  {

    CustomSwipeRefreshLayout ui_RefreshLayout;
    LinearLayout userList, feedList;
    CircleImageView profilePictureBox;
    AVLoadingIndicatorView progressBar;
    ScrollView scrollView;

    ArrayList<User> users = new ArrayList<>();
    public ArrayList<Feed> feeds = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        checkPermissions(CAM_PER);

        Commons.homeActivity = this;

        scrollView = (ScrollView)findViewById(R.id.scrollView);
        ui_RefreshLayout = (CustomSwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        ui_RefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFeedData("");
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        // Stop animation (This will be after 3 seconds)
                        ui_RefreshLayout.setRefreshing(false);
                    }
                }, 5000); // D
            }
        });

        ui_RefreshLayout.setView(scrollView);

        ui_RefreshLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);
        profilePictureBox = (CircleImageView)findViewById(R.id.profilePictureBox);
        Glide.with(this).load(Commons.thisUser.get_photoUrl()).into(profilePictureBox);
        userList = (LinearLayout)findViewById(R.id.userFrame);
        feedList = (LinearLayout)findViewById(R.id.feedFrame);

        setupUI(findViewById(R.id.activity), this);

        getFeedData("");
    }

    public void toAct(View view){

    }

    public void toCalc(View view){

    }

    public void toProf(View view){

    }

    public void toChat(View view){

    }

    public void search(View view){
        showAlertDialogForSearch(getString(R.string.search_feed), HomeActivity.this);
    }

    public void addFeed(View view){
        Intent intent = new Intent(getApplicationContext(), CreatePostActivity.class);
        startActivity(intent);
    }

    public void addStory(View view){
        Intent intent = new Intent(getApplicationContext(), CreatePostActivity.class);
        startActivity(intent);
    }

    public void openLive(View view){

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Commons.homeActivity = null;
    }

    public void getFeedData(String keyword){

        userList.removeAllViews();
        feedList.removeAllViews();

        progressBar.setVisibility(View.VISIBLE);

        AndroidNetworking.post(ReqConst.SERVER_URL + "homefeeds")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("Home Feeds RESP!!!", response.toString());
                        progressBar.setVisibility(View.INVISIBLE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                feeds.clear();
                                users.clear();
                                JSONArray dataArr = response.getJSONArray("data");
                                for(int i=0; i<dataArr.length(); i++) {
                                    JSONObject object = (JSONObject) dataArr.get(i);
                                    JSONObject feedObj = (JSONObject) object.getJSONObject("feed");
                                    JSONObject userObj = (JSONObject) object.getJSONObject("member");
                                    String picCount = (String) object.getString("pic_count");
                                    Feed feed = new Feed();
                                    feed.setIdx(feedObj.getInt("id"));
                                    feed.setUserId(userObj.getInt("id"));
                                    feed.setUserName(userObj.getString("name"));
                                    feed.setUserPhoto(userObj.getString("picture_url"));
                                    feed.setUserLocation(userObj.getString("address"));
                                    feed.setDescription(feedObj.getString("description"));
                                    feed.setPrivacy(feedObj.getString("privacy"));
                                    feed.setPictureUrl(feedObj.getString("picture_url"));
                                    feed.setVideoUrl(feedObj.getString("video_url"));
                                    feed.setLocation(feedObj.getString("location"));
                                    double lat = 0.0d, lng = 0.0d;
                                    if(feedObj.getString("latitude").length() > 0){
                                        lat = Double.parseDouble(feedObj.getString("latitude"));
                                        lng = Double.parseDouble(feedObj.getString("longitude"));
                                    }
                                    feed.setLatLng(new LatLng(lat, lng));
                                    feed.setPostedTime(feedObj.getString("posted_time"));
                                    feed.setLikes(Integer.parseInt(feedObj.getString("likes")));
                                    feed.setComments(Integer.parseInt(feedObj.getString("comments")));
                                    feed.setLiked(feedObj.getString("is_liked").equals("yes"));
                                    feed.setSaved(feedObj.getString("is_saved").equals("yes"));
                                    feed.setStatus(feedObj.getString("status"));

                                    LayoutInflater inflater = getLayoutInflater();
                                    View myLayout = inflater.inflate(R.layout.feeds_list_item, null);
                                    ImageView feedPicture = (ImageView) myLayout.findViewById(R.id.feedPictureBox);
                                    CircleImageView userPictureBox = (CircleImageView) myLayout.findViewById(R.id.userPictureBox);
                                    TextView userNameBox = (TextView) myLayout.findViewById(R.id.userNameBox);
                                    TextView addressBox = (TextView) myLayout.findViewById(R.id.addressBox);
                                    TextView postedDateBox = (TextView) myLayout.findViewById(R.id.postedDateBox);
                                    ImageView commentButton = (ImageView) myLayout.findViewById(R.id.btn_comment);
                                    ImageView shareButton = (ImageView) myLayout.findViewById(R.id.btn_share);
                                    ImageView saveButton = (ImageView) myLayout.findViewById(R.id.btn_save);
                                    ImageButton videoMark = (ImageButton) myLayout.findViewById(R.id.videomark);
                                    ProgressBar progressBar = (ProgressBar) myLayout.findViewById(R.id.progressBar);
                                    ProgressBar progressBar_user = (ProgressBar) myLayout.findViewById(R.id.progressBar_user);
                                    SeeMoreTextView noteBox = (SeeMoreTextView) myLayout.findViewById(R.id.descriptionBox);
                                    TextView likesBox = (TextView) myLayout.findViewById(R.id.likesBox);
                                    TextView commentsbox = (TextView) myLayout.findViewById(R.id.commentsBox);
                                    ImageView menuButton = (ImageView) myLayout.findViewById(R.id.btn_menu);
                                    LikeButton likeButton = (LikeButton) myLayout.findViewById(R.id.likeButton);
                                    ScalableVideoView mVideoView = (ScalableVideoView) myLayout.findViewById(R.id.videoView);
                                    TextView moreBox = (TextView) myLayout.findViewById(R.id.moreBox);
                                    ImageView fullscreenButton = (ImageView) myLayout.findViewById(R.id.fullscreenButton);

                                    FrameLayout mediaFrame = (FrameLayout) myLayout.findViewById(R.id.mediaFrame);
                                    LinearLayout textFrame1 = (LinearLayout) myLayout.findViewById(R.id.textFrame1);
                                    LinearLayout textFrame2 = (LinearLayout) myLayout.findViewById(R.id.textFrame2);
                                    SeeMoreTextView noteBox1 = (SeeMoreTextView) myLayout.findViewById(R.id.descriptionBox1);
                                    TextView postedDateBox1 = (TextView) myLayout.findViewById(R.id.postedDateBox1);

                                    userNameBox.setText(feed.getUserName());
                                    addressBox.setText(feed.getUserLocation());
                                    postedDateBox.setText(DateMain.getDateStr(HomeActivity.this, Long.parseLong(feed.getPostedTime())));
                                    likesBox.setText(String.valueOf(feed.getLikes()));
                                    commentsbox.setText(String.valueOf(feed.getComments()));

                                    int pic_count = Integer.parseInt(picCount);
                                    if(pic_count < 2)moreBox.setVisibility(View.GONE);
                                    else {
                                        moreBox.setVisibility(View.VISIBLE);
                                        moreBox.setText("+" + String.valueOf(pic_count - 1));
                                    }

                                    if(feed.getDescription().length() == 0)noteBox.setVisibility(View.GONE);
                                    else {
                                        noteBox.setVisibility(View.VISIBLE);
                                        noteBox.setContent(String.valueOf(StringEscapeUtils.unescapeJava(feed.getDescription())));
                                    }

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

                                    menuButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            AlertDialog alertDialog = null;
                                            if(feed.getUserId() == Commons.thisUser.get_idx()){
                                                String[] items = {
                                                        getString(R.string.share),
                                                        getString(R.string.view_likes),
                                                        getString(R.string.edit),
                                                        getString(R.string.delete),
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                                                builder.setItems(items, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // the user clicked on colors[which]
                                                        switch (which){
                                                            case 0:
                                                                String mediaUrl = "";
                                                                if(feed.getPictureUrl().length() > 0)
                                                                    mediaUrl = feed.getPictureUrl();
                                                                else if(feed.getVideoUrl().length() > 0)
                                                                    mediaUrl = feed.getVideoUrl();
                                                                Intent sendIntent = new Intent();
                                                                sendIntent.setAction(Intent.ACTION_SEND);
                                                                sendIntent.putExtra(Intent.EXTRA_TEXT,
                                                                        mediaUrl);
                                                                sendIntent.setType("text/plain");
                                                                startActivity(sendIntent);
                                                                break;
                                                            case 1:
                                                                Commons.feed = feed;
                                                                Intent intent = new Intent(getApplicationContext(), FeedLikesActivity.class);
                                                                startActivity(intent);
                                                                overridePendingTransition(0,0);
                                                                break;
                                                            case 2:
                                                                Commons.feed = feed;
                                                                intent = new Intent(getApplicationContext(), EditFeedActivity.class);
                                                                startActivity(intent);
                                                                overridePendingTransition(0,0);
                                                                break;
                                                            case 3:
                                                                showAlertDelete(feed, myLayout);
                                                                break;
                                                        }
                                                    }
                                                });
                                                alertDialog = builder.create();
                                                alertDialog.show();
                                                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                                                lp.copyFrom(alertDialog.getWindow().getAttributes());
                                                lp.width = 900;
                                                alertDialog.getWindow().setAttributes(lp);
                                            }else {
                                                String[] items = {
                                                        getString(R.string.share),
                                                        getString(R.string.view_likes),
                                                        getString(R.string.report_abuse),
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                                                builder.setItems(items, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // the user clicked on colors[which]
                                                        switch (which){
                                                            case 0:
                                                                String mediaUrl = "";
                                                                if(feed.getPictureUrl().length() > 0)
                                                                    mediaUrl = feed.getPictureUrl();
                                                                else if(feed.getVideoUrl().length() > 0)
                                                                    mediaUrl = feed.getVideoUrl();
                                                                Intent sendIntent = new Intent();
                                                                sendIntent.setAction(Intent.ACTION_SEND);
                                                                sendIntent.putExtra(Intent.EXTRA_TEXT,
                                                                        mediaUrl);
                                                                sendIntent.setType("text/plain");
                                                                startActivity(sendIntent);
                                                                break;
                                                            case 1:
                                                                Commons.feed = feed;
                                                                Intent intent = new Intent(getApplicationContext(), FeedLikesActivity.class);
                                                                startActivity(intent);
                                                                overridePendingTransition(0,0);
                                                                break;
                                                            case 2:
                                                                break;
                                                        }
                                                    }
                                                });
                                                alertDialog = builder.create();
                                                alertDialog.show();
                                                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                                                lp.copyFrom(alertDialog.getWindow().getAttributes());
                                                lp.width = 900;
                                                alertDialog.getWindow().setAttributes(lp);
                                            }
                                        }
                                    });

                                    if(feed.getUserPhoto().length() > 0){
                                        Picasso.with(getApplicationContext())
                                                .load(feed.getUserPhoto())
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

                                    if(feed.getPictureUrl().length() > 0){
                                        Picasso.with(getApplicationContext())
                                                .load(feed.getPictureUrl())
                                                .error(R.drawable.logo_no_title)
                                                .placeholder(R.drawable.logo_no_title)
                                                .into(feedPicture, new Callback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        if(feed.getVideoUrl().length() == 0) progressBar.setVisibility(View.INVISIBLE);
                                                    }
                                                    @Override
                                                    public void onError() {
                                                        if(feed.getVideoUrl().length() == 0) progressBar.setVisibility(View.INVISIBLE);
                                                    }
                                                });

                                        mediaFrame.setVisibility(View.VISIBLE);
                                        textFrame1.setVisibility(View.GONE);
                                        textFrame2.setVisibility(View.VISIBLE);
                                        if(feed.getDescription().length() == 0){
                                            noteBox.setVisibility(View.GONE);
                                        }
                                        else {
                                            noteBox.setVisibility(View.VISIBLE);
                                            noteBox.setContent(String.valueOf(StringEscapeUtils.unescapeJava(feed.getDescription())));
                                        }

                                        if(feed.getVideoUrl().length() > 0){

                                            Uri uri = Uri.parse(feed.getVideoUrl());
                                            mVideoView.setDataSource(HomeActivity.this, uri);
                                            mVideoView.setScalableType(ScalableType.CENTER_CROP);
                                            mVideoView.prepareAsync(new MediaPlayer.OnPreparedListener() {
                                                @Override
                                                public void onPrepared(MediaPlayer mp) {
                                                    progressBar.setVisibility(View.GONE);
                                                    videoMark.setVisibility(View.VISIBLE);
                                                    mVideoView.setVisibility(View.VISIBLE);
                                                    fullscreenButton.setVisibility(View.VISIBLE);
//                                                    mVideoView.setLooping(true);
//                                                    mVideoView.start();
                                                }
                                            });

                                            mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                @Override
                                                public void onCompletion(MediaPlayer mediaPlayer) {
                                                    videoMark.setVisibility(View.VISIBLE);
                                                }
                                            });

                                        }

                                    }else {
                                        mediaFrame.setVisibility(View.GONE);
                                        textFrame1.setVisibility(View.VISIBLE);
                                        noteBox1.setContent(String.valueOf(StringEscapeUtils.unescapeJava(feed.getDescription())));
                                        postedDateBox1.setText(DateMain.getDateStr(getApplicationContext(), Long.parseLong(feed.getPostedTime())));
                                        textFrame2.setVisibility(View.GONE);
                                    }

                                    if(feed.getDescription().length() > 100){
                                        noteBox.setTextMaxLength(100);
                                        noteBox.toggle();
                                        noteBox.expandText(false);
                                        noteBox.setSeeMoreTextColor(R.color.green);
                                        noteBox.setSeeMoreText(getString(R.string.read_more),getString(R.string.less));

                                        noteBox1.setTextMaxLength(100);
                                        noteBox1.toggle();
                                        noteBox1.expandText(false);
                                        noteBox1.setSeeMoreTextColor(R.color.green);
                                        noteBox1.setSeeMoreText(getString(R.string.read_more),getString(R.string.less));
                                    }


                                    feedPicture.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if(feed.getVideoUrl().length() == 0){
                                                Commons.feed = feed;
                                                Commons.view = myLayout;
                                                Intent intent = new Intent(getApplicationContext(), FeedPicturesActivity.class);
                                                startActivity(intent);
                                                overridePendingTransition(0,0);
                                            }
                                        }
                                    });

                                    fullscreenButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Commons.feed = feed;
                                            Commons.view = myLayout;
                                            if(mVideoView.isPlaying()){
                                                mVideoView.pause();
                                                videoMark.setVisibility(View.VISIBLE);
                                            }
                                            Intent intent = new Intent(getApplicationContext(), FeedVideoPlayActivity.class);
                                            ActivityOptionsCompat options = ActivityOptionsCompat.
                                                    makeSceneTransitionAnimation((Activity) HomeActivity.this, mVideoView, getString(R.string.transition));
                                            startActivity(intent, options.toBundle());
                                        }
                                    });

                                    mVideoView.setOnTouchListener( new View.OnTouchListener() {
                                        @Override
                                        public boolean onTouch(View v, MotionEvent event) {
                                            if(event.getAction() == MotionEvent.ACTION_UP){
                                                if(mVideoView.isPlaying()) {
                                                    mVideoView.pause();
                                                    videoMark.setVisibility(View.VISIBLE);
                                                }else {
                                                    mVideoView.setVisibility(View.VISIBLE);
                                                    mVideoView.start();
                                                    videoMark.setVisibility(View.GONE);
                                                }
                                            }
                                            return true;
                                        }
                                    });

                                    if(feed.isLiked())likeButton.setLiked(true);
                                    else likeButton.setLiked(false);

                                    likeButton.setOnLikeListener(new OnLikeListener() {
                                        @Override
                                        public void liked(LikeButton likeButton) {
                                            likeFeed(feed, likesBox);
                                        }

                                        @Override
                                        public void unLiked(LikeButton likeButton) {
                                            unLikeFeed(feed, likesBox);
                                        }
                                    });

                                    if(feed.getComments() > 0)commentButton.setImageResource(R.drawable.ic_commented);
                                    else commentButton.setImageResource(R.drawable.ic_comment);
                                    commentButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if(feed.getUserId() != Commons.thisUser.get_idx()) {
                                                Commons.feed = feed;
                                                Commons.commentsBox = commentsbox;
                                                Commons.view = myLayout;
                                                Intent intent = new Intent(getApplicationContext(), FeedCommentsActivity.class);
                                                startActivity(intent);
                                            }else {

                                            }
                                        }
                                    });

                                    videoMark.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if(mVideoView.isPlaying()) {
                                                mVideoView.pause();
                                                videoMark.setVisibility(View.VISIBLE);
                                            }else {
                                                mVideoView.setVisibility(View.VISIBLE);
                                                mVideoView.start();
                                                videoMark.setVisibility(View.GONE);
                                            }
                                        }
                                    });

                                    shareButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String mediaUrl = "";
                                            if(feed.getPictureUrl().length() > 0)
                                                mediaUrl = feed.getPictureUrl();
                                            else if(feed.getVideoUrl().length() > 0)
                                                mediaUrl = feed.getVideoUrl();
                                            Intent sendIntent = new Intent();
                                            sendIntent.setAction(Intent.ACTION_SEND);
                                            sendIntent.putExtra(Intent.EXTRA_TEXT,
                                                    mediaUrl);
                                            sendIntent.setType("text/plain");
                                            startActivity(sendIntent);
                                        }
                                    });

                                    if(feed.isSaved())saveButton.setImageResource(R.drawable.ic_saved);
                                    else saveButton.setImageResource(R.drawable.ic_save);

                                    saveButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if(!feed.isSaved()) {
                                                saveFeed(feed, saveButton);
                                            }else {
                                                unSaveFeed(feed, saveButton);
                                            }
                                        }
                                    });

                                    if(keyword.length() > 0){
                                        if(feed.getUserName().toLowerCase().contains(keyword.toLowerCase())
                                                || feed.getDescription().toLowerCase().contains(keyword.toLowerCase())
                                                || feed.getLocation().toLowerCase().contains(keyword.toLowerCase())
                                                || feed.getPrivacy().toLowerCase().contains(keyword.toLowerCase())
                                                || DateMain.getDateStr(HomeActivity.this, Long.parseLong(feed.getPostedTime()))
                                                .toLowerCase().contains(keyword.toLowerCase())
                                        ){
                                            feeds.add(feed);
                                            feedList.addView(myLayout);
                                        }
                                    }else {
                                        feeds.add(feed);
                                        feedList.addView(myLayout);
                                    }

                                }

                                JSONArray usersArr = response.getJSONArray("feed_users");
                                for(int i=0; i<usersArr.length(); i++) {
                                    JSONObject object = (JSONObject) usersArr.get(i);
                                    User user = new User();
                                    user.set_idx(object.getInt("id"));
                                    user.set_name(object.getString("name"));
                                    user.set_email(object.getString("email"));
                                    user.set_password(object.getString("password"));
                                    user.set_phone_number(object.getString("phone_number"));
                                    user.set_photoUrl(object.getString("picture_url"));
                                    user.set_registered_time(object.getString("registered_time"));
                                    double lat = 0.0d, lng = 0.0d;
                                    if(object.getString("latitude").length() > 0){
                                        lat = Double.parseDouble(object.getString("latitude"));
                                        lng = Double.parseDouble(object.getString("longitude"));
                                    }
                                    user.setLatLng(new LatLng(lat, lng));
                                    user.set_address(object.getString("address"));
                                    user.setFollowers(Integer.parseInt(object.getString("followers")));
                                    user.setFollowings(Integer.parseInt(object.getString("followings")));
                                    user.setPosts(Integer.parseInt(object.getString("posts")));
                                    user.set_status(object.getString("status"));

                                    LayoutInflater inflater = getLayoutInflater();
                                    View myLayout = inflater.inflate(R.layout.feed_user_item, null);
                                    CircleImageView profilePicture = (CircleImageView) myLayout.findViewById(R.id.profilePicture);
                                    TextView nameBox = (TextView) myLayout.findViewById(R.id.nameBox);
                                    String[] splited = user.get_name().split("\\s+");
                                    nameBox.setText(splited[0] + " " + splited[1].substring(0,1) + ".");
                                    ProgressBar progressBar = (ProgressBar) myLayout.findViewById(R.id.progressBar);

                                    Picasso.with(getApplicationContext())
                                            .load(user.get_photoUrl())
                                            .error(R.drawable.logo_no_title)
                                            .placeholder(R.drawable.logo_no_title)
                                            .into(profilePicture, new Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                }

                                                @Override
                                                public void onError() {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                }
                                            });
                                    myLayout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Commons.user = user;

                                        }
                                    });

                                    userList.addView(myLayout);
                                    users.add(user);
                                }

                                if(users.isEmpty()){
                                    profilePictureBox.setBorderColor(getColor(R.color.green));
                                    profilePictureBox.setBorderWidth(5);
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        progressBar.setVisibility(View.INVISIBLE);
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

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void showAlertDelete(Feed feed, View layout){
        showAlertDialogForQuestion(getString(R.string.warning), getString(R.string.sure_delete), HomeActivity.this, null, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                progressBar.setVisibility(View.VISIBLE);
                AndroidNetworking.post(ReqConst.SERVER_URL + "delFeed")
                        .addBodyParameter("feed_id", String.valueOf(feed.getIdx()))
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // do anything with response
                                Log.d("RESPONSE!!!", response.toString());
                                progressBar.setVisibility(View.GONE);
                                try {
                                    String result = response.getString("result_code");
                                    if(result.equals("0")){
                                        showToast(getString(R.string.deleted));
                                        feedList.removeViewAt(feedList.indexOfChild(layout));
                                        feeds.remove(feed);
                                    }else if(result.equals("1")){
                                        showToast(getString(R.string.no_exist));
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
                                showToast(error.getErrorDetail());
                            }
                        });
                return null;
            }
        });
    }

    public void showAlertDialogForSearch(String title, Activity activity){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_alert_search, null);
        builder.setView(view);
        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.SearchDialogAnimation;
        dialog.show();
        TextView titleBox = (TextView)view.findViewById(R.id.title);
        titleBox.setText(title);
        EditText searchBox = (EditText) view.findViewById(R.id.edt_search);
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    getFeedData(searchBox.getText().toString().trim());
                    dialog.dismiss();
                    return true;
                }
                return false;
            }
        });
        TextView allButton = (TextView)view.findViewById(R.id.allButton);
        allButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFeedData("");
                dialog.dismiss();
            }
        });

        ImageView cancelButton = (ImageView)view.findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Get screen width and height in pixels
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // The absolute width of the available display size in pixels.
        int displayWidth = displayMetrics.widthPixels;
        // The absolute height of the available display size in pixels.
        int displayHeight = displayMetrics.heightPixels;

        // Initialize a new window manager layout parameters
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        // Copy the alert dialog window attributes to new layout parameter instance
        layoutParams.copyFrom(dialog.getWindow().getAttributes());

        // Set alert dialog width equal to screen width 80%
        int dialogWindowWidth = (int) (displayWidth * 0.8f);
        // Set alert dialog height equal to screen height 80%
        //    int dialogWindowHeight = (int) (displayHeight * 0.8f);

        // Set the width and height for the layout parameters
        // This will bet the width and height of alert dialog
        layoutParams.width = dialogWindowWidth;
        //      layoutParams.height = dialogWindowHeight;

        // Apply the newly created layout parameters to the alert dialog window
        dialog.getWindow().setAttributes(layoutParams);
        dialog.setCancelable(false);
    }


    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {
//        boolean isTop = (direction == SwipyRefreshLayoutDirection.TOP ? true : false);
//        if (isTop) {
//            getFeedData("");
//            new Handler().postDelayed(new Runnable() {
//                @Override public void run() {
//                    // Stop animation (This will be after 3 seconds)
//                    ui_RefreshLayout.setRefreshing(false);
//                }
//            }, 4000); // D
//        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }



}
































