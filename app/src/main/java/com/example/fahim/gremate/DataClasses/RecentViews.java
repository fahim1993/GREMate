package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/23/16.
 */

public class RecentViews {
    private String word, time;

    public RecentViews() {
        this.word = "";
        this.time = "";
    }

    public RecentViews(String word, String time) {
        this.word = word;
        this.time = time;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
