package com.app.thealth.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.app.thealth.R;
import com.app.thealth.adapters.CapturedPictureListAdapter;
import com.app.thealth.base.BaseActivity;
import com.app.thealth.commons.Commons;
import com.app.thealth.commons.Constants;
import com.app.thealth.commons.ReqConst;
import com.app.thealth.models.Picture;
import com.google.android.gms.maps.model.LatLng;
import com.iceteck.silicompressorr.SiliCompressor;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.viewpagerindicator.LinePageIndicator;
import com.wang.avi.AVLoadingIndicatorView;
import com.yqritc.scalablevideoview.ScalableVideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;

import static android.os.Environment.getExternalStorageDirectory;

public class EditFeedActivity extends BaseActivity {

    public String privacy = "public";
    public TextView privacyBox, locationBox;
    public LatLng latLng = null;
    EditText descriptionBox;
    TextView shareButton;
    TextView mediaNoteBox;
    AVLoadingIndicatorView progressBar;
    public FrameLayout pictureFrame, videoFrame;
    public ScalableVideoView videoView;
    ViewPager pager;
    ArrayList<Picture> pictures = new ArrayList<>();
    CapturedPictureListAdapter adapter = new CapturedPictureListAdapter(this);
    File thumbFile = null, videoFile = null;
    LinearLayout delFrame;
    long videoDuration = 0;
    ImageView imageButton, videoButton;

    public ImageView thumbView;
    public ImageButton videoMark;
    public ProgressBar progressBar2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_feed);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Commons.editFeedActivity = this;
        checkPermissions(LOC_PER);

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        thumbView = (ImageView)findViewById(R.id.thumbView);
        videoMark = (ImageButton)findViewById(R.id.videomark);

        privacyBox = (TextView)findViewById(R.id.privacyBox);
        locationBox = (TextView)findViewById(R.id.locationBox);
        shareButton = (TextView)findViewById(R.id.shareButton);
        descriptionBox = (EditText)findViewById(R.id.descriptionBox);

        mediaNoteBox = (TextView)findViewById(R.id.mediaNoteBox);
        delFrame = (LinearLayout)findViewById(R.id.delFrame);

        videoFrame = (FrameLayout)findViewById(R.id.videoFrame);
        pictureFrame = (FrameLayout)findViewById(R.id.pictureFrame);
        videoView = (ScalableVideoView) findViewById(R.id.videoView);

        pager = findViewById(R.id.viewPager);

        mediaNoteBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pictures.size() > 0)pictureFrame.setVisibility(View.VISIBLE);
            }
        });

        imageButton = (ImageView)findViewById(R.id.imageButton);
        videoButton = (ImageView)findViewById(R.id.videoButton);

        if(Commons.feed.getVideoUrl().length() == 0){
            videoButton.setVisibility(View.GONE);
            getFeedPictures(String.valueOf(Commons.feed.getIdx()));
        }else {
            imageButton.setVisibility(View.GONE);
            Picture picture = new Picture();
            picture.setBitmap(null);
            picture.setUri(null);
            picture.setUrl(Commons.feed.getPictureUrl());
            picture.setVideoUrl(Commons.feed.getVideoUrl());
            picture.setType("video");
            pictures.add(picture);
            adapter.setDatas(pictures);
            adapter.notifyDataSetChanged();
            pager.setAdapter(adapter);
            mediaNoteBox.setText(getString(R.string.video_loaded));
        }

        privacyBox.setText(Commons.feed.getPrivacy());
        locationBox.setText(Commons.feed.getLocation());
        descriptionBox.setText(Commons.feed.getDescription());
        latLng = Commons.feed.getLatLng();

    }

    public void toPrivacy(View view){
        Intent intent = new Intent(getApplicationContext(), SelectPostPrivacyActivity.class);
        startActivity(intent);
    }

    public void addLocation(View view){
        Intent intent = new Intent(getApplicationContext(), PickLocationActivity.class);
        startActivity(intent);
    }

    public void editPicture(View view){
        if(pictures.size() > 0){
            Commons.picture = pictures.get(pager.getCurrentItem());

            if(Commons.picture.getBitmap() == null){
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                Bitmap bitmap = drawableToBitmap(LoadImageFromWebOperations(pictures.get(pager.getCurrentItem()).getUrl()));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

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

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Commons.picture.setBitmap(bitmap);
            }

            Intent intent = new Intent(getApplicationContext(), EditImageActivity.class);
            intent.putExtra("item", String.valueOf(pager.getCurrentItem()));
            startActivity(intent);
            overridePendingTransition(0,0);
        }
    }

    public void showVideoOptions(View view){
        showAlertDialogForVideo();
    }

    public void pickPicture(View view){
        checkPermissions(CAM_PER);
        showAlertDialogForPicture();
    }

    public void submitPost(View view){
        if(latLng == null){
            latLng = Commons.thisUser.getLatLng();
            locationBox.setText(Commons.thisUser.get_address());
        }

        if(pictures.size() == 0 && descriptionBox.getText().length() == 0){
            showToast(getString(R.string.enter_post_data));
            return;
        }

        if(pictures.size() > 0){
            if(pictures.get(pager.getCurrentItem()).getType().equals("image")){
                ArrayList<File> files = new ArrayList<>();
                for(Picture picture: pictures){
                    if(picture.getUri() != null)
                        files.add(picture.getFile());
                }
                if(files.size() > 10){
                    showToast(getString(R.string.picture_cant_exceed10));
                    return;
                }
                submitImage(files);
            }else if(pictures.get(pager.getCurrentItem()).getType().equals("video")){
                if(pictures.get(pager.getCurrentItem()).getUri() != null){
                    try {
                        saveVideoThumb();
                    } catch (IOException e) {
                        e.fillInStackTrace();
                    }
                }else {
                    uploadFeedWithoutMedia();
                }
            }
        }else {
            uploadFeedWithoutMedia();
        }
    }

    public void back(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Commons.homeActivity.getFeedData("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Commons.editFeedActivity = null;
    }

    public void showAlertDialogForVideo(){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditFeedActivity.this);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_alert_video, null);

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

        wmlp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        wmlp.x = 50;   //x position
        wmlp.y = 100;   //y position

        dialog.show();

        TextView cameraButton = (TextView)view.findViewById(R.id.cameraButton);
        TextView galleryButton = (TextView)view.findViewById(R.id.galleryButton);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions(CAM_PER);
                dispatchTakeVideoIntent();

                dialog.dismiss();
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions(CAM_PER);
                selectVideoFromGallery();

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
        int dialogWindowWidth = (int) (displayWidth * 0.4f);
        // Set alert dialog height equal to screen height 80%
        //    int dialogWindowHeight = (int) (displayHeight * 0.8f);

        // Set the width and height for the layout parameters
        // This will bet the width and height of alert dialog
        layoutParams.width = dialogWindowWidth;
        //      layoutParams.height = dialogWindowHeight;

        // Apply the newly created layout parameters to the alert dialog window
        dialog.getWindow().setAttributes(layoutParams);
    }


    public void showAlertDialogForPicture(){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditFeedActivity.this);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_alert_image, null);

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

        wmlp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        wmlp.x = 50;   //x position
        wmlp.y = 100;   //y position

        dialog.show();

        TextView cameraButton = (TextView)view.findViewById(R.id.cameraButton);
        TextView galleryButton = (TextView)view.findViewById(R.id.galleryButton);
        TextView cropButton = (TextView)view.findViewById(R.id.cropButton);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions(CAM_PER);
                takePhoto();

                dialog.dismiss();
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions(CAM_PER);
                pickPhoto();

                dialog.dismiss();
            }
        });

        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions(CAM_PER);
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(EditFeedActivity.this);

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
        int dialogWindowWidth = (int) (displayWidth * 0.4f);
        // Set alert dialog height equal to screen height 80%
        //    int dialogWindowHeight = (int) (displayHeight * 0.8f);

        // Set the width and height for the layout parameters
        // This will bet the width and height of alert dialog
        layoutParams.width = dialogWindowWidth;
        //      layoutParams.height = dialogWindowHeight;

        // Apply the newly created layout parameters to the alert dialog window
        dialog.getWindow().setAttributes(layoutParams);
    }


    public void takePhoto(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, Constants.TAKE_FROM_CAMERA);
    }

    public void pickPhoto(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), Constants.PICK_FROM_GALLERY);
    }

    static final int REQUEST_VIDEO_CAPTURE = 1;
    static final int SELECT_VIDEO_REQUEST=2;

    String selectedVideoPath = "",takenVideoPath = "";
    private Uri fileUri;
    public Uri videoUri = null;
    public Bitmap thumb = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                //From here you can load the image however you need to, I recommend using the Glide library
                File imageFile = new File(resultUri.getPath());
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    Picture picture = new Picture();
                    picture.setFile(imageFile);
                    picture.setBitmap(bitmap);
                    picture.setType("image");
                    if(pictures.size() > 0 && pictures.get(0).getType().equals("video")) pictures.clear();
                    pictures.add(picture);
                    adapter.setDatas(pictures);
                    adapter.notifyDataSetChanged();
                    pager.setAdapter(adapter);

                    LinePageIndicator linePageIndicator = findViewById(R.id.indicator);
                    linePageIndicator.setViewPager(pager);

                    final float density = getResources().getDisplayMetrics().density;
                    linePageIndicator.setSelectedColor(0x88FF0000);
                    linePageIndicator.setUnselectedColor(0xFF888888);
                    linePageIndicator.setStrokeWidth(4 * density);
                    linePageIndicator.setLineWidth(30 * density);
                    pager.setCurrentItem(pictures.size());
                    pictureFrame.setVisibility(View.VISIBLE);
                    mediaNoteBox.setText(getString(R.string.attached_pictures) + " : " + String.valueOf(pictures.size()));
                    delFrame.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }

        }
        else if(requestCode == Constants.PICK_FROM_GALLERY){
            if (resultCode == RESULT_OK) {
                onSelectFromGalleryResult(data);
            }
        }

        else if(requestCode == Constants.TAKE_FROM_CAMERA){
            if (resultCode == RESULT_OK) {
                onCaptureImageResult(data);
            }
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {

            takenVideoPath = getPath(data.getData());
            videoUri = data.getData();
//            if(isVideoDurationMoreThan3Mins(videoUri)){
//                showToast(getString(R.string.video_exceeds_3mins));
//                return;
//            }
            thumb = ThumbnailUtils.createVideoThumbnail(takenVideoPath, MediaStore.Images.Thumbnails.MINI_KIND);
            pictures.clear();
            Picture picture = new Picture();
            picture.setBitmap(thumb);
            picture.setUri(videoUri);
            picture.setType("video");
            pictures.add(picture);
            adapter.setDatas(pictures);
            adapter.notifyDataSetChanged();
            pager.setAdapter(adapter);

            pager.setCurrentItem(pictures.size());
            pictureFrame.setVisibility(View.VISIBLE);
            mediaNoteBox.setText(getString(R.string.attached_videos) + " : " + String.valueOf(pictures.size()));
//            delFrame.setVisibility(View.VISIBLE);
        }

        else if(requestCode == SELECT_VIDEO_REQUEST && resultCode == RESULT_OK)
        {
            if(data.getData()!=null)
            {
                selectedVideoPath = getPath(data.getData());
                try{
                    videoUri = data.getData();
                    if(isVideoDurationMoreThan3Mins(videoUri)){
                        showToast(getString(R.string.video_exceeds_3mins));
                        return;
                    }
                    thumb = ThumbnailUtils.createVideoThumbnail(selectedVideoPath, MediaStore.Images.Thumbnails.MINI_KIND);
                    Log.d("THUMBNAIL!!!", String.valueOf(thumb == null));
                    pictures.clear();
                    Picture picture = new Picture();
                    picture.setBitmap(thumb);
                    picture.setUri(videoUri);
                    picture.setType("video");
                    pictures.add(picture);
                    adapter.setDatas(pictures);
                    adapter.notifyDataSetChanged();
                    pager.setAdapter(adapter);

                    pager.setCurrentItem(pictures.size());
                    pictureFrame.setVisibility(View.VISIBLE);
                    mediaNoteBox.setText(getString(R.string.attached_videos) + " : " + String.valueOf(pictures.size()));
//                    delFrame.setVisibility(View.VISIBLE);

                }catch (NullPointerException e){

                }

            }
            else {
                Toast.makeText(getApplicationContext(), "Failed to select video" , Toast.LENGTH_LONG).show();
            }
        }

    }

    private void onSelectFromGalleryResult(Intent data) {

        if (data != null) {
            Uri resultUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                File file = new File(Environment.getExternalStorageDirectory()+"/Pictures",
                        System.currentTimeMillis() + ".jpg");

                FileOutputStream fo;
                try {
                    file.createNewFile();
                    fo = new FileOutputStream(file);
                    fo.write(byteArrayOutputStream.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Picture picture = new Picture();
                picture.setFile(file);
                picture.setBitmap(bitmap);
                picture.setType("image");
                if(pictures.size() > 0 && pictures.get(0).getType().equals("video")) pictures.clear();
                pictures.add(picture);
                adapter.setDatas(pictures);
                adapter.notifyDataSetChanged();
                pager.setAdapter(adapter);

                LinePageIndicator linePageIndicator = findViewById(R.id.indicator);
                linePageIndicator.setViewPager(pager);

                final float density = getResources().getDisplayMetrics().density;
                linePageIndicator.setSelectedColor(0x88FF0000);
                linePageIndicator.setUnselectedColor(0xFF888888);
                linePageIndicator.setStrokeWidth(4 * density);
                linePageIndicator.setLineWidth(30 * density);
                pager.setCurrentItem(pictures.size());
                pictureFrame.setVisibility(View.VISIBLE);
                mediaNoteBox.setText(getString(R.string.attached_pictures) + " : " + String.valueOf(pictures.size()));
                delFrame.setVisibility(View.VISIBLE);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void onCaptureImageResult(Intent data) {

        File dir = new File(getExternalStorageDirectory().getAbsolutePath(), "Pictures");
        if (!dir.exists())
            dir.mkdirs();
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File file = new File(dir,
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            file.createNewFile();
            fo = new FileOutputStream(file);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Picture picture = new Picture();
        picture.setFile(file);
        picture.setBitmap(thumbnail);
        picture.setType("image");
        if(pictures.size() > 0 && pictures.get(0).getType().equals("video")) pictures.clear();
        pictures.add(picture);
        adapter.setDatas(pictures);
        adapter.notifyDataSetChanged();
        pager.setAdapter(adapter);

        LinePageIndicator linePageIndicator = findViewById(R.id.indicator);
        linePageIndicator.setViewPager(pager);

        final float density = getResources().getDisplayMetrics().density;
        linePageIndicator.setSelectedColor(0x88FF0000);
        linePageIndicator.setUnselectedColor(0xFF888888);
        linePageIndicator.setStrokeWidth(4 * density);
        linePageIndicator.setLineWidth(30 * density);
        pager.setCurrentItem(pictures.size());
        pictureFrame.setVisibility(View.VISIBLE);
        mediaNoteBox.setText(getString(R.string.attached_pictures) + " : " + String.valueOf(pictures.size()));
        delFrame.setVisibility(View.VISIBLE);

    }

    public void deleteCurrentItem(View view){
        if(pictures.size() > 0) {
            if(pictures.get(pager.getCurrentItem()).getUrl().length() > 0){
                showAlertDialogForQuestion(getString(R.string.warning), getString(R.string.sure_delete), EditFeedActivity.this, null,  new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        progressBar.setVisibility(View.VISIBLE);
                        AndroidNetworking.post(ReqConst.SERVER_URL + "deleteFeedPicture")
                                .addBodyParameter("fpic_id", String.valueOf(pictures.get(pager.getCurrentItem()).getIdx()))
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
                                                Picture picture = pictures.get(pager.getCurrentItem());
                                                String type = picture.getType();
                                                pictures.remove(picture);
                                                adapter.setDatas(pictures);
                                                adapter.notifyDataSetChanged();
                                                pager.setAdapter(adapter);
                                                if(type.equals("image"))mediaNoteBox.setText(getString(R.string.attached_pictures) + " : " + String.valueOf(pictures.size()));
                                                else if(type.equals("video"))mediaNoteBox.setText(getString(R.string.attached_videos) + " : " + String.valueOf(pictures.size()));
                                                if(pictures.size() == 0){
                                                    mediaNoteBox.setText(getString(R.string.add_to_post));
                                                    pictureFrame.setVisibility(View.GONE);
                                                    delFrame.setVisibility(View.INVISIBLE);
                                                    shareButton.setVisibility(View.INVISIBLE);
                                                    LinePageIndicator linePageIndicator = findViewById(R.id.indicator);
                                                    linePageIndicator.setViewPager(pager);

                                                    final float density = getResources().getDisplayMetrics().density;
                                                    linePageIndicator.setSelectedColor(0x88FF0000);
                                                    linePageIndicator.setUnselectedColor(0xFF888888);
                                                    linePageIndicator.setStrokeWidth(4 * density);
                                                    linePageIndicator.setLineWidth(30 * density);
                                                }
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
            }else {
                Picture picture = pictures.get(pager.getCurrentItem());
                String type = picture.getType();
                pictures.remove(picture);
                adapter.setDatas(pictures);
                adapter.notifyDataSetChanged();
                pager.setAdapter(adapter);
                if(type.equals("image"))mediaNoteBox.setText(getString(R.string.attached_pictures) + " : " + String.valueOf(pictures.size()));
                else if(type.equals("video"))mediaNoteBox.setText(getString(R.string.attached_videos) + " : " + String.valueOf(pictures.size()));
                if(pictures.size() == 0){
                    mediaNoteBox.setText(getString(R.string.add_to_post));
                    pictureFrame.setVisibility(View.GONE);
                    delFrame.setVisibility(View.INVISIBLE);
                    shareButton.setVisibility(View.INVISIBLE);
                    LinePageIndicator linePageIndicator = findViewById(R.id.indicator);
                    linePageIndicator.setViewPager(pager);

                    final float density = getResources().getDisplayMetrics().density;
                    linePageIndicator.setSelectedColor(0x88FF0000);
                    linePageIndicator.setUnselectedColor(0xFF888888);
                    linePageIndicator.setStrokeWidth(4 * density);
                    linePageIndicator.setLineWidth(30 * density);
                }
            }

        }
    }

    public void closePictureFrame(View view){
        pictureFrame.setVisibility(View.GONE);
    }

    public void closeVideoFrame(View view){
        if(videoView.isPlaying())videoView.pause();
        videoFrame.setVisibility(View.GONE);
    }

    private void saveVideoThumb() throws IOException {

//        if(isVideoDurationMoreThan3Mins(pictures.get(pager.getCurrentItem()).getUri())){
//            showToast(getString(R.string.video_cant_exceed_3mins));
//            return;
//        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        pictures.get(pager.getCurrentItem()).getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        // You are allowed to write external storage:
        String path = getExternalStorageDirectory().getAbsolutePath() + "/videoThumbnail";
        File storageDir = new File(path);
        if (!storageDir.exists() && !storageDir.mkdir()) {
            Log.d("ISSUE!!!", "Thumbnail not saved");
            return;
        }

        thumbFile = new File(path,
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            thumbFile.createNewFile();
            fo = new FileOutputStream(thumbFile);
            fo.write(byteArrayOutputStream.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        videoFile = new File(getPath(pictures.get(pager.getCurrentItem()).getUri()));

        //create destination directory
        File f2 = new File(getExternalStorageDirectory().getAbsolutePath() + "/" + getPackageName() + "/media/videos");
        if (f2.mkdirs() || f2.isDirectory())
            //compress and output new video specs
            new EditFeedActivity.VideoCompressAsyncTask(this).execute(pictures.get(pager.getCurrentItem()).getUri().toString(), f2.getPath());

    }

    private void dispatchTakeVideoIntent() {
        showToast(getString(R.string.video_cant_exceed_3mins));
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 180);  // 3 mins limitation
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if(cursor!=null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else return null;
    }

    public void selectVideoFromGallery(){
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, SELECT_VIDEO_REQUEST);
    }

    public void uploadVideoToSever(File videoFile, File thumbFile) {
        ((LinearLayout)findViewById(R.id.progressLayout)).setVisibility(View.VISIBLE);
        AndroidNetworking.upload(ReqConst.SERVER_URL + "updatefeed")
                .addMultipartFile("video",videoFile)
                .addMultipartFile("thumb",thumbFile)
                .addMultipartParameter("feed_id", String.valueOf(Commons.feed.getIdx()))
                .addMultipartParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .addMultipartParameter("description", descriptionBox.getText().toString().trim())  //  .addMultipartParameter("name", imeiID)
                .addMultipartParameter("location", locationBox.getText().toString())
                .addMultipartParameter("privacy", privacy)
                .addMultipartParameter("latitude", String.valueOf(latLng.latitude))
                .addMultipartParameter("longitude", String.valueOf(latLng.longitude))
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                        Log.d("UPLOADED!!!", String.valueOf(bytesUploaded) + "/" + String.valueOf(totalBytes));
                        ((TextView)findViewById(R.id.progressText)).setText(String.valueOf((int)bytesUploaded*100/totalBytes));
                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        ((LinearLayout)findViewById(R.id.progressLayout)).setVisibility(View.GONE);
                        showToast(getString(R.string.post_update_success));
                        finish();
                        Commons.homeActivity.getFeedData("");
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        ((LinearLayout)findViewById(R.id.progressLayout)).setVisibility(View.GONE);
                        showToast(error.getMessage());
                    }
                });
    }


    public void uploadFeedWithoutMedia() {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "updatefeed")
                .addBodyParameter("feed_id", String.valueOf(Commons.feed.getIdx()))
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .addBodyParameter("description", descriptionBox.getText().toString().trim())  //  .addMultipartParameter("name", imeiID)
                .addBodyParameter("location", locationBox.getText().toString())
                .addBodyParameter("privacy", privacy)
                .addBodyParameter("latitude", String.valueOf(latLng.latitude))
                .addBodyParameter("longitude", String.valueOf(latLng.longitude))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        showToast(getString(R.string.post_update_success));
                        finish();
                        Commons.homeActivity.getFeedData("");
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        progressBar.setVisibility(View.GONE);
                        showToast(error.getMessage());
                    }
                });
    }


    public void submitImage(ArrayList<File> files){
        ((LinearLayout)findViewById(R.id.progressLayout)).setVisibility(View.VISIBLE);
        AndroidNetworking.upload(ReqConst.SERVER_URL + "updatefeed")
                .addMultipartFileList("pictures", files)
                .addMultipartParameter("feed_id", String.valueOf(Commons.feed.getIdx()))
                .addMultipartParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .addMultipartParameter("description", descriptionBox.getText().toString().trim())  //  .addMultipartParameter("name", imeiID)
                .addMultipartParameter("location", locationBox.getText().toString())
                .addMultipartParameter("privacy", privacy)
                .addMultipartParameter("latitude", String.valueOf(latLng.latitude))
                .addMultipartParameter("longitude", String.valueOf(latLng.longitude))
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                        Log.d("UPLOADED!!!", String.valueOf(bytesUploaded) + "/" + String.valueOf(totalBytes));
                        ((TextView)findViewById(R.id.progressText)).setText(String.valueOf((int)bytesUploaded*100/totalBytes));
                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        ((LinearLayout)findViewById(R.id.progressLayout)).setVisibility(View.GONE);
                        showToast(getString(R.string.post_update_success));
                        finish();
                        Commons.homeActivity.getFeedData("");
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        ((LinearLayout)findViewById(R.id.progressLayout)).setVisibility(View.GONE);
                        showToast(error.getMessage());
                    }
                });
    }

    private boolean isVideoDurationMoreThan3Mins(Uri uriOfFile){
        MediaPlayer mp = MediaPlayer.create(this, uriOfFile);
        int duration = mp.getDuration();
        Log.d("DURATION!!!", String.valueOf(duration));
        mp.release();
        if(duration > 3*60*1000 + 50)return true;
        return false;
    }

    class VideoCompressAsyncTask extends AsyncTask<String, String, String> {

        Context mContext;

        public VideoCompressAsyncTask(Context context){
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((LinearLayout) findViewById(R.id.compressionMsg)).setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... paths) {
            String filePath = null;
            try {

                filePath = SiliCompressor.with(mContext).compressVideo(Uri.parse(paths[0]), paths[1]);

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return  filePath;
        }
        @Override
        protected void onPostExecute(String compressedFilePath) {
            super.onPostExecute(compressedFilePath);
            File imageFile = new File(compressedFilePath);
            float length = imageFile.length() / 1024f; // Size in KB
            String value;
            if(length >= 1024)
                value = length/1024f+" MB";
            else
                value = length+" KB";
            String text = String.format(Locale.US, "%s\nName: %s\nSize: %s", "Video compression complete", imageFile.getName(), value);
            ((LinearLayout) findViewById(R.id.compressionMsg)).setVisibility(View.GONE);
            Log.i("Silicompressor", "Path: "+compressedFilePath);
            Log.d("COMPRESSED RESULT!!!", text);
            uploadVideoToSever(new File((compressedFilePath)), thumbFile);
        }
    }

    public void refreshPictures(int currentItem){
        adapter.setDatas(pictures);
        adapter.notifyDataSetChanged();
        pager.setAdapter(adapter);

        LinePageIndicator linePageIndicator = findViewById(R.id.indicator);
        linePageIndicator.setViewPager(pager);

        final float density = getResources().getDisplayMetrics().density;
        linePageIndicator.setSelectedColor(0x88FF0000);
        linePageIndicator.setUnselectedColor(0xFF888888);
        linePageIndicator.setStrokeWidth(4 * density);
        linePageIndicator.setLineWidth(30 * density);
        pager.setCurrentItem(currentItem);
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
                                    picture.setIdx(object.getInt("id"));
                                    picture.setUserId(Integer.parseInt(object.getString("member_id")));
                                    picture.setFeedId(Integer.parseInt(object.getString("feed_id")));
                                    picture.setUrl(object.getString("picture_url"));
                                    picture.setType("image");
                                    pictures.add(0, picture);
                                    adapter.setDatas(pictures);
                                    adapter.notifyDataSetChanged();
                                    pager.setAdapter(adapter);
                                }
                                LinePageIndicator linePageIndicator = findViewById(R.id.indicator);
                                linePageIndicator.setViewPager(pager);

                                final float density = getResources().getDisplayMetrics().density;
                                linePageIndicator.setSelectedColor(0x88FF0000);
                                linePageIndicator.setUnselectedColor(0xFF888888);
                                linePageIndicator.setStrokeWidth(4 * density);
                                linePageIndicator.setLineWidth(30 * density);

                                mediaNoteBox.setText(getString(R.string.loaded_pictures) + ": " + String.valueOf(pictures.size()));
                                if(pictures.size() > 0)delFrame.setVisibility(View.VISIBLE);

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

}





























