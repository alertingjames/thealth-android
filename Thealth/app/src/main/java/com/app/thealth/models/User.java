package com.app.thealth.models;

import com.google.android.gms.maps.model.LatLng;

public class User {
    int _idx = 0;
    String _name = "";
    String _email = "";
    String _password = "";
    String _phone_number = "";
    String _auth_status = "";
    String _photoUrl = "";
    String _address = "";
    int followers = 0;
    int followings = 0;
    int posts = 0;
    LatLng latLng = null;
    String _status = "";
    String _registered_time = "";

    public User(){}

    public String get_address() {
        return _address;
    }

    public void set_address(String _address) {
        this._address = _address;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowings() {
        return followings;
    }

    public void setFollowings(int followings) {
        this.followings = followings;
    }

    public int getPosts() {
        return posts;
    }

    public void setPosts(int posts) {
        this.posts = posts;
    }

    public int get_idx() {
        return _idx;
    }

    public void set_idx(int _idx) {
        this._idx = _idx;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_email() {
        return _email;
    }

    public void set_email(String _email) {
        this._email = _email;
    }

    public String get_password() {
        return _password;
    }

    public void set_password(String _password) {
        this._password = _password;
    }

    public String get_phone_number() {
        return _phone_number;
    }

    public void set_phone_number(String _phone_number) {
        this._phone_number = _phone_number;
    }

    public String get_auth_status() {
        return _auth_status;
    }

    public void set_auth_status(String _auth_status) {
        this._auth_status = _auth_status;
    }

    public String get_photoUrl() {
        return _photoUrl;
    }

    public void set_photoUrl(String _photoUrl) {
        this._photoUrl = _photoUrl;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String get_status() {
        return _status;
    }

    public void set_status(String _status) {
        this._status = _status;
    }

    public String get_registered_time() {
        return _registered_time;
    }

    public void set_registered_time(String _registered_time) {
        this._registered_time = _registered_time;
    }
}
