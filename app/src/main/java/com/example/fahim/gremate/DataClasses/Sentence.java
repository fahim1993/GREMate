package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/16/16.
 */

public class Sentence {
    private String word, value;

    public Sentence() {
        word = "";
        value = "";
    }

    public Sentence(String word, String value) {
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
