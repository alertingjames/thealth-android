package com.app.thealth.main;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.app.thealth.R;
import com.app.thealth.adapters.FeedCommentListAdapter;
import com.app.thealth.base.BaseActivity;
import com.app.thealth.commons.Commons;
import com.app.thealth.commons.ReqConst;
import com.app.thealth.models.FeedComment;
import com.app.thealth.models.Picture;
import com.app.thealth.models.User;
import com.google.android.gms.maps.model.LatLng;
import com.iamhabib.easy_preference.EasyPreference;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedCommentsActivity extends BaseActivity {

    ListView list;
    CircleImageView myPicture;
    EditText commentBox;
    ArrayList<FeedComment> comments = new ArrayList<>();
    AVLoadingIndicatorView progressBar;
    FeedCommentListAdapter adapter = new FeedCommentListAdapter(this);

    ImageView searchButton, cancelButton;
    public LinearLayout searchBar;
    EditText ui_edtsearch;
    TextView titleBox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_comments);

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        list = (ListView)findViewById(R.id.list);
        myPicture = (CircleImageView)findViewById(R.id.myPictureBox);
        commentBox = (EditText)findViewById(R.id.commentBox);
        commentBox.setSelection(commentBox.getText().length());

        titleBox = (TextView)findViewById(R.id.titleBox);

        searchBar = (LinearLayout)findViewById(R.id.search_bar);
        searchButton = (ImageView)findViewById(R.id.searchButton);
        cancelButton = (ImageView)findViewById(R.id.cancelButton);

        ui_edtsearch = (EditText)findViewById(R.id.edt_search);
        ui_edtsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = ui_edtsearch.getText().toString().trim().toLowerCase(Locale.getDefault());
                adapter.filter(text);
            }
        });

        Picasso.with(getApplicationContext())
                .load(Commons.thisUser.get_photoUrl())
                .error(R.drawable.noresult)
                .placeholder(R.drawable.noresult)
                .into(myPicture);
        commentBox.setFocusable(true);
        commentBox.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        getFeedComments(String.valueOf(Commons.feed.getIdx()));

        setupUI(findViewById(R.id.activity), this);

    }

    public void search(View view){
        cancelButton.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.GONE);
        searchBar.setVisibility(View.VISIBLE);
        titleBox.setVisibility(View.GONE);
        ui_edtsearch.setFocusable(true);
        ui_edtsearch.requestFocus();
        commentBox.clearFocus();
    }

    public void cancelSearch(View view){
        cancelButton.setVisibility(View.GONE);
        searchButton.setVisibility(View.VISIBLE);
        searchBar.setVisibility(View.GONE);
        titleBox.setVisibility(View.VISIBLE);
        ui_edtsearch.setText("");
        ui_edtsearch.clearFocus();
        commentBox.setFocusable(true);
        commentBox.requestFocus();
    }

    public void back(View view){
        onBackPressed();
    }

    public static void showKeyboard(Activity activity){
        InputMethodManager imm = (InputMethodManager)   activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
                        Log.d("Feed Comments Resp!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                comments.clear();
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
                                }

                                if(comments.isEmpty())((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.VISIBLE);
                                else ((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.GONE);

                                adapter.setDatas(comments);
                                list.setAdapter(adapter);
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

    public void sendComment(View view){
        if(commentBox.getText().length() == 0)return;
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "sendFeedComment")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .addBodyParameter("feed_id", String.valueOf(Commons.feed.getIdx()))
                .addBodyParameter("comment",  StringEscapeUtils.escapeJava(commentBox.getText().toString().trim()))
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                        Log.d("UPLOADED!!!", String.valueOf(bytesUploaded) + "/" + String.valueOf(totalBytes));
                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                Commons.commentsBox.setText(String.valueOf(Commons.feed.getComments() + 1));
                                Commons.feed.setComments(Commons.feed.getComments() + 1);
                                if(Commons.feed.getComments() > 0)Commons.commentButton.setImageResource(R.drawable.ic_commented);
                                else Commons.commentButton.setImageResource(R.drawable.ic_comment);
                                getFeedComments(String.valueOf(Commons.feed.getIdx()));
                                hideKeyboard(FeedCommentsActivity.this);
                                commentBox.setText("");
                                ((TextView)Commons.view.findViewById(R.id.commentBox)).setText(String.valueOf(Commons.feed.getComments() + 1));
                                ((ImageView)Commons.view.findViewById(R.id.commentButton)).setImageResource(R.drawable.ic_commented);

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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideKeyboard(FeedCommentsActivity.this);
    }
}



































