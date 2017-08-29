package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/23/16.
 */

public class WordData {
    private String des, mn;

    public WordData() {
        this.des = "";
        this.mn = "";
    }

    public WordData(String des, String mn) {
        this.des = des;
        this.mn = mn;
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
