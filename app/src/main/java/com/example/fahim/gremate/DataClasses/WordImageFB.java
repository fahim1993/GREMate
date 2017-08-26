package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 24-Aug-17.
 */

public class WordImageFB {
    private String word, url;

    public WordImageFB() {
        word = "";
        url = "";
    }

    public WordImageFB(String word, String url) {
        this.word = word;
        this.url = url;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String value) {
        this.url = value;
    }
}
