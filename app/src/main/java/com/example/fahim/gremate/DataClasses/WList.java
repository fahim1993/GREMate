package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/15/16.
 */

public class WList {
    private String wordSet;
    private String name;
    private boolean deletable;

    public WList(String wordSet, String name, boolean deletable) {
        this.wordSet = wordSet;
        this.name = name;
        this.deletable = deletable;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public WList() {
        this.wordSet = "";
        this.name = "";
        deletable = false;
        
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

}
