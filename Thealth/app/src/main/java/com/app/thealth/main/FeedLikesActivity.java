package com.app.thealth.main;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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
import com.app.thealth.R;
import com.app.thealth.adapters.LikeListAdapter;
import com.app.thealth.base.BaseActivity;
import com.app.thealth.commons.Commons;
import com.app.thealth.commons.ReqConst;
import com.app.thealth.models.FeedComment;
import com.app.thealth.models.User;
import com.google.android.gms.maps.model.LatLng;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedLikesActivity extends BaseActivity {

    ListView list;
    ArrayList<User> users = new ArrayList<>();
    AVLoadingIndicatorView progressBar;
    LikeListAdapter adapter = new LikeListAdapter(this);

    ImageView searchButton, cancelButton;
    public LinearLayout searchBar;
    EditText ui_edtsearch;
    TextView titleBox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_likes);

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        list = (ListView)findViewById(R.id.list);

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

        getFeedLikes(String.valueOf(Commons.feed.getIdx()), String.valueOf(Commons.thisUser.get_idx()));
    }

    public void search(View view){
        cancelButton.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.GONE);
        searchBar.setVisibility(View.VISIBLE);
        titleBox.setVisibility(View.GONE);
        ui_edtsearch.setFocusable(true);
        ui_edtsearch.requestFocus();
    }

    public void cancelSearch(View view){
        cancelButton.setVisibility(View.GONE);
        searchButton.setVisibility(View.VISIBLE);
        searchBar.setVisibility(View.GONE);
        titleBox.setVisibility(View.VISIBLE);
        ui_edtsearch.setText("");
        ui_edtsearch.clearFocus();
    }

    public void back(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0,0);
    }

    private void getFeedLikes(String feedId, String memberId){
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "feedLikes")
                .addBodyParameter("feed_id", feedId)
                .addBodyParameter("member_id", memberId)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("Feed Likes Resp!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                users.clear();
                                JSONArray arr = response.getJSONArray("data");
                                for(int i=0; i<arr.length(); i++){
                                    JSONObject object = (JSONObject) arr.get(i);
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

                                    if(user.get_idx() != Commons.thisUser.get_idx())users.add(user);
                                }

                                if(users.isEmpty())((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.VISIBLE);
                                else ((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.GONE);

                                adapter.setDatas(users);
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

}



































