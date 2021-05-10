package com.app.thealth.models;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;

public class Picture {
    int idx = 0;
    File file = null;
    File thumb = null;
    Bitmap bitmap = null;
    Uri uri = null;
    String url = "";
    String type = "";
    int userId = 0;
    int feedId = 0;
    String videoUrl = "";

    public Picture(){}

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getFeedId() {
        return feedId;
    }

    public void setFeedId(int feedId) {
        this.feedId = feedId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public File getThumb() {
        return thumb;
    }

    public void setThumb(File thumb) {
        this.thumb = thumb;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getIdx() {
        return idx;
    }

    public File getFile() {
        return file;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getUrl() {
        return url;
    }
}
