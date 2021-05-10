package com.app.thealth.commons;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.thealth.main.CreatePostActivity;
import com.app.thealth.main.EditFeedActivity;
import com.app.thealth.main.HomeActivity;
import com.app.thealth.main.RegisterProfileActivity;
import com.app.thealth.models.Feed;
import com.app.thealth.models.Picture;
import com.app.thealth.models.User;
import com.squareup.picasso.Picasso;

import java.io.File;

public class Commons {
    public static User thisUser = null;
    public static User user = null;
    public static int curMapTypeIndex = 1;

    public static RegisterProfileActivity registerProfileActivity = null;

    public static Feed feed = null;
    public static CreatePostActivity createPostActivity = null;
    public static File file = null;
    public static HomeActivity homeActivity = null;
    public static TextView commentsBox = null;
    public static ImageView commentButton = null;
    public static View view = null;
    public static Picture picture = null;
    public static EditFeedActivity editFeedActivity = null;
}
