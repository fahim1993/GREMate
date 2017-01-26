package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/15/16.
 */

public class WordSet {
    private String name, owner, allList;
    private int wordCount;
    private long lastOpen;

    public WordSet() {
        this.name = "";
        this.owner = "";
        this.allList = "";
        this.wordCount = 0;
        this.lastOpen = 0;
    }

    public WordSet(String name, String owner, String allList, int wordCount, long lastOpen) {
        this.name = name;
        this.owner = owner;
        this.allList = allList;
        this.wordCount = wordCount;
        this.lastOpen = lastOpen;
    }

    public static WordSet newWordSet (String name, String owner, String allList){
        return new WordSet(name, owner, allList, 0, 0);
    }

    public String getAllList() {
        return allList;
    }

    public void setAllList(String allList) {
        this.allList = allList;
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

    public long getLastOpen() {
        return lastOpen;
    }

    public void setLastOpen(long lastOpen) {
        this.lastOpen = lastOpen;
    }
}
