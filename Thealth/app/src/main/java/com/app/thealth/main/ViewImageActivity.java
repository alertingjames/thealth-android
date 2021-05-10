package com.app.thealth.main;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.app.thealth.R;
import com.app.thealth.base.BaseActivity;
import com.app.thealth.commons.Commons;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class ViewImageActivity extends AppCompatActivity {

    private PhotoView image;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        String imageStr = getIntent().getStringExtra("image");

        image = (PhotoView) findViewById(R.id.image);
        if(imageStr.length() > 0)
            Picasso.with(getApplicationContext()).load(imageStr).into(image);
        else if(Commons.file != null)
            Picasso.with(getApplicationContext()).load(Commons.file).into(image);

    }

    public void back(View view){
        onBackPressed();
    }
}
