package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/15/16.
 */

public class WordList {
    private String wordSet, name;
    private int wordCount;


    public WordList() {
        this.wordSet = "";
        this.name = "";
        this.wordCount = 0;
    }

    public WordList(String wordSet, String name, int wordCount) {
        this.wordSet = wordSet;
        this.name = name;
        this.wordCount = wordCount;
    }

    public static WordList getNewList(String wordSetKey, String listName){
        return new WordList(wordSetKey, listName, 0);
    }

    public static WordList getAllList(String wordSetKey){
        return new WordList(wordSetKey, "All", 0);
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

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

}
