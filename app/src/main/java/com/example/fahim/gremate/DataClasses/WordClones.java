package com.example.fahim.gremate.DataClasses;

/**
 * Created by Fahim on 05-Jan-17.
 */

public class WordClones {
    private String wordId;

    public WordClones(String otherWordId) {
        this.wordId = otherWordId;
    }

    public WordClones() {
        this.wordId = "";
    }

    public String getWordId() {
        return wordId;
    }

    public void setWordId(String otherWordId) {
        this.wordId = otherWordId;
    }
}
