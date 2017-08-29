package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/15/16.
 */

public class List {
    private String name;
    private int wordCount;


    public List() {
        this.name = "";
        this.wordCount = 0;
    }

    public List(String name, int wordCount) {
        this.name = name;
        this.wordCount = wordCount;
    }

    public static List getNewList(String listName){
        return new List(listName, 0);
    }

    public static List getAllList(){
        return new List("All Words", 0);
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
