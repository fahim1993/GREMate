package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/15/16.
 */

public class WordSetWithId extends WordSet {
    private String id;

//    public WordSet(String name, String mainList, int wordCount)

    public WordSetWithId(String name, String mainList, int wordCount, String id) {
        super(name, mainList, wordCount);
        this.id = id;
    }
    public WordSetWithId(WordSet ws, String id) {
        super(ws.getName(), ws.getMainList(), ws.getWordCount());
        this.id = id;
    }
    public WordSetWithId() {
        super();
        this.id = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
