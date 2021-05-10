package com.app.thealth.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.app.thealth.R;
import com.app.thealth.classes.DateMain;
import com.app.thealth.commons.Commons;
import com.app.thealth.main.FeedCommentsActivity;
import com.app.thealth.main.FeedDetailActivity;
import com.app.thealth.main.HomeActivity;
import com.app.thealth.models.Feed;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.yqritc.scalablevideoview.ScalableType;
import com.yqritc.scalablevideoview.ScalableVideoView;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedListAdapter extends BaseAdapter {

    private Context _context;
    private ArrayList<Feed> _datas = new ArrayList<>();
    private ArrayList<Feed> _alldatas = new ArrayList<>();

    public FeedListAdapter(Context context){

        super();
        this._context = context;
    }

    public void setDatas(ArrayList<Feed> datas) {

        _alldatas = datas;
        _datas.clear();
        _datas.addAll(_alldatas);
    }

    @Override
    public int getCount(){
        return _datas.size();
    }

    @Override
    public Object getItem(int position){
        return _datas.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        CustomHolder holder;

        if (convertView == null) {
            holder = new CustomHolder();

            LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            convertView = inflater.inflate(R.layout.feeds_list_item, parent, false);

            holder.feedPicture = (ImageView) convertView.findViewById(R.id.feedPictureBox);
            holder.userPictureBox = (CircleImageView) convertView.findViewById(R.id.userPictureBox);
            holder.userNameBox = (TextView) convertView.findViewById(R.id.userNameBox);
            holder.addressBox = (TextView) convertView.findViewById(R.id.addressBox);
            holder.postedDateBox = (TextView) convertView.findViewById(R.id.postedDateBox);
            holder.commentButton = (ImageView) convertView.findViewById(R.id.btn_comment);
            holder.shareButton = (ImageView) convertView.findViewById(R.id.btn_share);
            holder.saveButton = (ImageView) convertView.findViewById(R.id.btn_save);
            holder.videoMark = (ImageButton) convertView.findViewById(R.id.videomark);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
            holder.progressBar_user = (ProgressBar) convertView.findViewById(R.id.progressBar_user);
            holder.noteBox = (TextView) convertView.findViewById(R.id.descriptionBox);
            holder.likesBox = (TextView) convertView.findViewById(R.id.likesBox);
            holder.commentsbox = (TextView) convertView.findViewById(R.id.commentsBox);
            holder.menuButton = (ImageView) convertView.findViewById(R.id.btn_menu);
            holder.likeButton = (LikeButton) convertView.findViewById(R.id.likeButton);
            holder.mVideoView = (ScalableVideoView) convertView.findViewById(R.id.videoView);

            holder.mediaFrame = (FrameLayout) convertView.findViewById(R.id.mediaFrame);
            holder.textFrame1 = (LinearLayout) convertView.findViewById(R.id.textFrame1);
            holder.textFrame2 = (LinearLayout) convertView.findViewById(R.id.textFrame2);
            holder.noteBox1 = (TextView) convertView.findViewById(R.id.descriptionBox1);
            holder.postedDateBox1 = (TextView) convertView.findViewById(R.id.postedDateBox1);

            convertView.setTag(holder);

        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final Feed feed = (Feed) _datas.get(position);

        holder.userNameBox.setText(feed.getUserName());
        holder.addressBox.setText(feed.getUserLocation());
        holder.postedDateBox.setText(DateMain.getDateStr(_context, Long.parseLong(feed.getPostedTime())));
        holder.likesBox.setText(String.valueOf(feed.getLikes()));
        holder.commentsbox.setText(String.valueOf(feed.getComments()));

        holder.likesBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        holder.commentsbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        holder.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = null;
                if(feed.getUserId() == Commons.thisUser.get_idx()){
                    String[] items = {_context.getString(R.string.edit), _context.getString(R.string.delete)};
                    AlertDialog.Builder builder = new AlertDialog.Builder(_context);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // the user clicked on colors[which]
                        }
                    });
                    alertDialog = builder.create();
                    alertDialog.show();
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(alertDialog.getWindow().getAttributes());
                    lp.width = 600;
                    alertDialog.getWindow().setAttributes(lp);
                }else {
                    String[] items = {_context.getString(R.string.report_abuse)};
                    AlertDialog.Builder builder = new AlertDialog.Builder(_context);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // the user clicked on colors[which]
                        }
                    });
                    alertDialog = builder.create();
                    alertDialog.show();
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(alertDialog.getWindow().getAttributes());
                    lp.width = 600;
                    alertDialog.getWindow().setAttributes(lp);
                }
            }
        });

        if(feed.getUserPhoto().length() > 0){
            Picasso.with(_context)
                    .load(feed.getUserPhoto())
                    .error(R.drawable.user)
                    .placeholder(R.drawable.user)
                    .into(holder.userPictureBox, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.progressBar_user.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void onError() {
                            holder.progressBar_user.setVisibility(View.INVISIBLE);
                        }
                    });
        }

        if(feed.getPictureUrl().length() > 0){
            Picasso.with(_context)
                    .load(feed.getPictureUrl())
                    .error(R.drawable.logo_no_title)
                    .placeholder(R.drawable.logo_no_title)
                    .into(holder.feedPicture, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.progressBar.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void onError() {
                            holder.progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
            holder.mediaFrame.setVisibility(View.VISIBLE);
            holder.textFrame1.setVisibility(View.GONE);
            holder.textFrame2.setVisibility(View.VISIBLE);
            if(feed.getDescription().length() == 0){
                holder.noteBox.setVisibility(View.GONE);
            }
            else {
                holder.noteBox.setVisibility(View.VISIBLE);
                holder.noteBox.setText(StringEscapeUtils.unescapeJava(feed.getDescription()));
            }
        }else {
            holder.mediaFrame.setVisibility(View.GONE);
            holder.textFrame1.setVisibility(View.VISIBLE);
            holder.noteBox1.setText(StringEscapeUtils.unescapeJava(feed.getDescription()));
            holder.postedDateBox1.setText(DateMain.getDateStr(_context, Long.parseLong(feed.getPostedTime())));
            holder.textFrame2.setVisibility(View.GONE);
        }

        if(feed.getVideoUrl().length() > 0){
            holder.mVideoView.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.INVISIBLE);
            try {
                Uri uri = Uri.parse(feed.getVideoUrl());
                holder.mVideoView.setDataSource(_context, uri);
                holder.mVideoView.setScalableType(ScalableType.CENTER_CROP);
                holder.mVideoView.prepareAsync(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        holder.mVideoView.setLooping(true);
                        holder.mVideoView.start();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        holder.feedPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(feed.getVideoUrl().length() == 0){
                    Commons.feed = feed;
                    Intent intent = new Intent(_context, FeedDetailActivity.class);
                    _context.startActivity(intent);
                }else {
//                                                startActivity(PlayerActivity.getVideoPlayerIntent(getApplicationContext(),
//                                                        feed.getVideoUrl(),
//                                                        feed.getUserName()));
                }
            }
        });

        if(feed.isLiked())holder.likeButton.setLiked(true);
        else holder.likeButton.setLiked(false);

        holder.likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                if(Commons.homeActivity != null){
                    Commons.homeActivity.likeFeed(feed, holder.likesBox);
                }
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                if(Commons.homeActivity != null){
                    Commons.homeActivity.unLikeFeed(feed, holder.likesBox);
                }
            }
        });

        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(feed.getUserId() != Commons.thisUser.get_idx()) {
                    Commons.feed = feed;
                    Commons.commentsBox = holder.commentsbox;
                    Intent intent = new Intent(_context, FeedCommentsActivity.class);
                    _context.startActivity(intent);
                }else {

                }
            }
        });

        holder.videoMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                                            startActivity(PlayerActivity.getVideoPlayerIntent(getApplicationContext(),
//                                                    feed.getVideoUrl(),
//                                                    feed.getUserName()));
            }
        });

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
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
                _context.startActivity(sendIntent);
            }
        });

        if(feed.isSaved())holder.saveButton.setImageResource(R.drawable.ic_bookmark_black_fill);
        else holder.saveButton.setImageResource(R.drawable.ic_bookmark_black);

        holder.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!feed.isSaved()) {
                    if(Commons.homeActivity != null){
                        Commons.homeActivity.saveFeed(feed, holder.saveButton);
                    }
                }else {
                    if(Commons.homeActivity != null){
                        Commons.homeActivity.unSaveFeed(feed, holder.saveButton);
                    }
                }
            }
        });


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return convertView;
    }

    public void filter(String keyword){
        keyword = keyword.toLowerCase();
        _datas.clear();

        if(keyword.length() == 0){
            _datas.addAll(_alldatas);
        }else {
            for (Feed feed : _alldatas){
                if(feed.getUserName().toLowerCase().contains(keyword.toLowerCase())
                        || feed.getDescription().toLowerCase().contains(keyword.toLowerCase())
                        || feed.getLocation().toLowerCase().contains(keyword.toLowerCase())
                        || feed.getPrivacy().toLowerCase().contains(keyword.toLowerCase())
                        || DateMain.getDateStr(_context, Long.parseLong(feed.getPostedTime()))
                        .toLowerCase().contains(keyword.toLowerCase())
                ){
                    _datas.add(feed);
                }
            }
        }
        notifyDataSetChanged();
    }

    class CustomHolder {
        FrameLayout mediaFrame;
        LinearLayout textFrame1, textFrame2;
        TextView noteBox1, postedDateBox1;
        ImageView feedPicture;
        CircleImageView userPictureBox;
        TextView userNameBox,addressBox, postedDateBox, noteBox, likesBox, commentsbox;
        ImageView commentButton, shareButton, saveButton, menuButton;
        ImageButton videoMark;
        ProgressBar progressBar, progressBar_user;
        LikeButton likeButton;
        ScalableVideoView mVideoView;
    }
}


