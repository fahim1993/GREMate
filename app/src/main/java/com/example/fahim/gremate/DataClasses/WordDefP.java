package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/23/16.
 */

public class WordDefP {
    private String wordId, word, value;

    public WordDefP() {
        this.wordId = "";
        this.word = "";
        this.value = "";
    }

    public WordDefP(String wordId, String word, String value) {
        this.wordId = wordId;
        this.word = word;
        this.value = value;
    }

    public String getWordId() {
        return wordId;
    }

    public void setWordId(String wordId) {
        this.wordId = wordId;
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
