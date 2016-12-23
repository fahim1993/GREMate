package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/16/16.
 */

public class Sentence {
    String wordInListID, sntence, source;

    public Sentence(String wordInListID, String sntence, String source) {
        this.wordInListID = wordInListID;
        this.sntence = sntence;
        this.source = source;
    }

    public Sentence() {
        this.wordInListID = "";
        this.sntence = "";
        this.source = "";
    }

    public String getSntence() {
        return sntence;
    }

    public void setSntence(String sntence) {
        this.sntence = sntence;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getWordInListID() {
        return wordInListID;
    }

    public void setWordInListID(String wordInListID) {
        this.wordInListID = wordInListID;
    }
}
