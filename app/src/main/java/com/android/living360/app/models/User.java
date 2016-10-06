package com.android.living360.app.models;

import android.graphics.Bitmap;

/**
 * Created by Jose on 9/21/2016.
 */

public class User {

    private String fname;
    private String lname;
    private Bitmap profileImage;

    public User() {}

    public User(String fname, String lname) {
        this.fname = fname;
        this.lname = lname;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public Bitmap getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Bitmap profileImage) {
        this.profileImage = profileImage;
    }
}
