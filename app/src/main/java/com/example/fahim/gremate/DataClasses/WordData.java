package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/23/16.
 */

public class WordData {
    private String des, mn, pronunciation;

    public WordData() {
        this.des = "";
        this.mn = "";
        pronunciation = "";
    }

    public WordData(String des, String mn, String pronunciation) {
        this.des = des;
        this.mn = mn;
        this.pronunciation = pronunciation;
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

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }
}
