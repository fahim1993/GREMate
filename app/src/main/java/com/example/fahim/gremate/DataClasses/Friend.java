package com.example.fahim.gremate.DataClasses;

/**
 * Created by Fahim on 10-Jan-17.
 */

public class Friend {
    private String name, id;

    public Friend() {
        this.name = "";
        this.id = "";
    }

    public Friend(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
