package com.example.fahim.gremate.DataClasses;

/**
 * Created by Fahim on 25-Dec-16.
 */

public class ListWithId extends List {
    String id;

    public ListWithId(String name, int wordCount, String id) {
        super(name, wordCount);
        this.id = id;
    }

    public ListWithId(List wordList, String id) {
        super(wordList.getName(), wordList.getWordCount());
        this.id = id;
    }

    public ListWithId(){
        super();
        id  = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
