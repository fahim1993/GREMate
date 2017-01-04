package com.example.fahim.gremate.DataClasses;

/**
 * Created by Fahim on 27-Dec-16.
 */

public class WordwID extends Word {
    private String id;

    public WordwID() {
        super();
        this.id = "";
    }

    public WordwID(String listId, String value, boolean practicable, int validity, int lastOpen, int level, String id) {
        super(listId, value, practicable, validity, lastOpen, level);
        this.id = id;
    }

    public WordwID(Word word, String id){
        super(word.getListId(), word.getValue(), word.isPracticable(), word.getValidity(),
                word.getLastOpen(), word.getLevel());

        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
