package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/23/16.
 */

public class WordDefP {
    private String word, value;

    public WordDefP() {
        this.word = "";
        this.value = "";
    }

    public WordDefP(String word, String value) {
        this.word = word;
        this.value = value;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
