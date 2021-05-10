package com.app.thealth.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.app.thealth.R;
import com.app.thealth.base.BaseActivity;
import com.app.thealth.commons.Commons;

public class SelectPostPrivacyActivity extends BaseActivity {

    ImageView publicmark, privatemark;
    String privacy = "public";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_post_privacy);

        publicmark = (ImageView)findViewById(R.id.publicmark);
        privatemark = (ImageView)findViewById(R.id.privatemark);

    }

    private void clearMarks(){
        privacy = "";
        publicmark.setVisibility(View.INVISIBLE);
        privatemark.setVisibility(View.INVISIBLE);
    }

    public void selPublic(View view){
        clearMarks();
        privacy = "public";
        publicmark.setVisibility(View.VISIBLE);
    }

    public void selPrivate(View view){
        clearMarks();
        privacy = "private";
        privatemark.setVisibility(View.VISIBLE);
    }

    public void toFriends(View view){
        Intent intent = new Intent(getApplicationContext(), FriendListActivity.class);
        startActivity(intent);
    }

    public void toGroups(View view){
        Intent intent = new Intent(getApplicationContext(), GroupListActivity.class);
        startActivity(intent);
    }

    public void done(View view){
        if(Commons.createPostActivity != null){
            Commons.createPostActivity.privacyBox.setText(privacy.substring(0,1).toUpperCase() + privacy.substring(1, privacy.length()));
            Commons.createPostActivity.privacy = privacy;
        }else if(Commons.editFeedActivity != null){
            Commons.editFeedActivity.privacyBox.setText(privacy.substring(0,1).toUpperCase() + privacy.substring(1, privacy.length()));
            Commons.editFeedActivity.privacy = privacy;
        }
        finish();
    }

    public void back(View view){
        onBackPressed();
    }
}






























