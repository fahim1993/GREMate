package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/15/16.
 */

public class WordList {
    private String wordSet, name;
    private boolean deletable;
    private int wordCount, learned;

    public WordList() {
        this.wordSet = "";
        this.name = "";
        this.deletable = true;
        this.wordCount = 0;
        this.learned = 0;
    }

    public WordList(String wordSet, String name, boolean deletable, int wordCount, int learned) {
        this.wordSet = wordSet;
        this.name = name;
        this.deletable = deletable;
        this.wordCount = wordCount;
        this.learned = learned;
    }

    public String getWordSet() {
        return wordSet;
    }

    public void setWordSet(String wordSet) {
        this.wordSet = wordSet;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getLearned() {
        return learned;
    }

    public void setLearned(int learned) {
        this.learned = learned;
    }
}
