package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/15/16.
 */

public class WordSet {
    private String name, creator;

    public WordSet( ) {
        this.name = "";
        this.creator = "";
    }

    public WordSet(String name, String creator) {
        this.name = name;
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
