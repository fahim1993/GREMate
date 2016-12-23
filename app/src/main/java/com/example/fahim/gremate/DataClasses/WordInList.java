package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/15/16.
 */

public class WordInList {
    String wordSetID, wListID, name, wordDataID;
    Boolean shadow, learned;

    public WordInList() {
        this.wordSetID = "";
        this.wListID = "";
        this.name = "";
        this.wordDataID = "";
        this.shadow = false;
        this.learned = false;
    }

    public WordInList(String wordSetID, String wListID, String name, String wordDataID, Boolean shadow, Boolean learned) {
        this.wordSetID = wordSetID;
        this.wListID = wListID;
        this.name = name;
        this.wordDataID = wordDataID;
        this.shadow = shadow;
        this.learned = learned;
    }

    public String getWordSetID() {
        return wordSetID;
    }

    public void setWordSetID(String wordSetID) {
        this.wordSetID = wordSetID;
    }

    public String getwListID() {
        return wListID;
    }

    public void setwListID(String wListID) {
        this.wListID = wListID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWordDataID() {
        return wordDataID;
    }

    public void setWordDataID(String wordDataID) {
        this.wordDataID = wordDataID;
    }

    public Boolean getShadow() {
        return shadow;
    }

    public void setShadow(Boolean shadow) {
        this.shadow = shadow;
    }

    public Boolean getLearned() {
        return learned;
    }

    public void setLearned(Boolean learned) {
        this.learned = learned;
    }
}
