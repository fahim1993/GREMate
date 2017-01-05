package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/15/16.
 */

public class WordSetwID extends WordSet {
    private String id;

    public WordSetwID(String name, String owner, String allList, int wordCount, int lastOpen, String id) {
        super(name, owner, allList, wordCount, lastOpen);
        this.id = id;
    }
    public WordSetwID(WordSet ws, String id) {
        super(ws.getName(), ws.getOwner(), ws.getAllList(), ws.getWordCount(), ws.getLastOpen());
        this.id = id;
    }
    public WordSetwID() {
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
