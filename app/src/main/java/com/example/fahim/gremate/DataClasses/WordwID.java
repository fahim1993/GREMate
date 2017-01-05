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

    public WordwID(String copyOf, String listId, String value, boolean practicable, int validity, int lastOpen, int level, int added, String id) {
        super(copyOf, listId, value, practicable, validity, lastOpen, level, added);
        this.id = id;
    }

    public WordwID(Word word, String id){
        super(word.getCopyOf(), word.getListId(), word.getValue(), word.isPracticable(), word.getValidity(),
                word.getLastOpen(), word.getLevel(), word.getAdded());

        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Word toWord(){
        return new Word(getCopyOf(), getListId(), getValue(), isPracticable(), getValidity(), getLastOpen(), getLevel(), getAdded());
    }
}
