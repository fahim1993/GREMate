package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/15/16.
 */

public class WordSetGet {
    private String name, owner, id;
    private int wordCount, learned;

    public WordSetGet() {
        this.name = "";
        this.owner = "";
        this.id="";
        this.wordCount = 0;
        this.learned = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WordSetGet(WordSet ws, String id) {
        this.name = ws.getName();
        this.owner = ws.getOwner();
        this.id = id;

        this.wordCount = ws.getWordCount();
        this.learned = ws.getLearned();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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
