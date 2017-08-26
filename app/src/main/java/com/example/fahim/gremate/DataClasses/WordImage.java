package com.example.fahim.gremate.DataClasses;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by fahim on 24-Aug-17.
 */

public class WordImage {
    private Bitmap image;

    public WordImage(Bitmap image) {
        this.image = image;
    }

    public WordImage() {
    }

    public Bitmap getImage() {
        if(image==null)return null;
        return image;
    }

    public byte[] getImageByteArray() {
        if(image==null)return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    public void setImage(Bitmap bitmap) {
        image = bitmap;
    }

    public void setImage(byte [] b) {
        image = BitmapFactory.decodeStream(new ByteArrayInputStream(b));
    }

    public boolean isValid(){
        if(image!=null)return true;
        return  false;
    }
}
