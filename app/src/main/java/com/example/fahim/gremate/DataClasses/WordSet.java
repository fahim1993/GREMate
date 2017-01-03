package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/15/16.
 */

public class WordSet {
    private String name, owner;
    private int wordCount, lastOpen;

    public WordSet() {
        this.name = "";
        this.owner = "";
        this.wordCount = 0;
        this.lastOpen = 0;
    }

    public WordSet(String name, String owner, int wordCount, int learned) {
        this.name = name;
        this.owner = owner;
        this.wordCount = wordCount;
        this.lastOpen = learned;
    }

    public static WordSet newWordSet (String name, String owner){
        return new WordSet(name, owner, 0, 0);
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

    public int getLastOpen() {
        return lastOpen;
    }

    public void setLastOpen(int learned) {
        this.lastOpen = learned;
    }
}
