package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/23/16.
 */

public class Word {
    private String wordSet, wordList, value;
    private boolean practicable, shadow, learned;
    private int appeared, correct;

    public Word() {
        this.wordSet = "";
        this.wordList = "";
        this.value = "";
        this.practicable = false;
        this.shadow = true;
        this.learned = false;
        this.appeared = 0;
        this.correct = 0;
    }

    public Word(String wordSet, String wordList, String value, boolean practicable, boolean shadow, boolean learned, int appeared, int correct) {
        this.wordSet = wordSet;
        this.wordList = wordList;
        this.value = value;
        this.practicable = practicable;
        this.shadow = shadow;
        this.learned = learned;
        this.appeared = appeared;
        this.correct = correct;
    }

    public String getWordSet() {
        return wordSet;
    }

    public void setWordSet(String wordSet) {
        this.wordSet = wordSet;
    }

    public String getWordList() {
        return wordList;
    }

    public void setWordList(String wordList) {
        this.wordList = wordList;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isPracticable() {
        return practicable;
    }

    public void setPracticable(boolean practicable) {
        this.practicable = practicable;
    }

    public boolean isShadow() {
        return shadow;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    public boolean isLearned() {
        return learned;
    }

    public void setLearned(boolean learned) {
        this.learned = learned;
    }

    public int getAppeared() {
        return appeared;
    }

    public void setAppeared(int appeared) {
        this.appeared = appeared;
    }

    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }
}
