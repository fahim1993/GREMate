package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/23/16.
 */

public class WordData {
    private String word, des, mn;

    public WordData() {
        this.word = "";
        this.des = "";
        this.mn = "";
    }

    public WordData(String word, String des, String mn) {
        this.word = word;
        this.des = des;
        this.mn = mn;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getMn() {
        return mn;
    }

    public void setMn(String mn) {
        this.mn = mn;
    }
}
