package com.example.ece1778;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.multidex.MultiDexApplication;

public class CurrentPost extends Application {

    private Bitmap postBitmap;
    private String currentPhotoPath = "";
    private Uri postURI;

    public Bitmap getPostBitmap() {
        return postBitmap;
    }

    public void setPostBitmap(Bitmap bitmap) {
        this.postBitmap = bitmap;
    }

    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }

    public void setCurrentPhotoPath(String currentPhotoPath) {
        this.currentPhotoPath = currentPhotoPath;
    }


    public Uri getPostUri() {
        return postURI;
    }

    public void setPostUri(Uri photoUri) {
        this.postURI = photoUri;
    }

}
