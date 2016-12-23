package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/16/16.
 */

public class Stats {
    String id;
    int wordCount, learned;

    public Stats() {
        this.id = "";
        this.wordCount = 0;
        this.learned = 0;
    }


    public Stats(String id, int wordCount, int learned) {
        this.id = id;
        this.wordCount = wordCount;
        this.learned = learned;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
