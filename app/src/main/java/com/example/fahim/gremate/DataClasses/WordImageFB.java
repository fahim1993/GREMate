package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 24-Aug-17.
 */

public class WordImageFB {
    private String url;

    public WordImageFB() {
        url = "";
    }

    public WordImageFB(String word, String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String value) {
        this.url = value;
    }
}
