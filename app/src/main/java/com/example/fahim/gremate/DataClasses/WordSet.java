package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/15/16.
 */

public class WordSet {
    private String name, mainList;
    private int wordCount;

    public WordSet() { }

    public WordSet(String name, String mainList, int wordCount) {
        this.name = name;
        this.mainList = mainList;
        this.wordCount = wordCount;
    }

    public static WordSet newWordSet (String name, String mainList){
        return new WordSet(name, mainList, 0);
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

    public String getMainList() {
        return mainList;
    }

    public void setMainList(String mainList) {
        this.mainList = mainList;
    }
}
