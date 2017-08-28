package com.example.fahim.gremate.DataClasses;

import java.util.Comparator;

/**
 * Created by Fahim on 27-Dec-16.
 */

public class WordwID extends Word {
    private String id;

    public WordwID() {
        super();
        this.id = "";
    }

    public WordwID(String copyOf, String listId, String value, String listName, boolean practicable,
                   int validity, int lastOpen, int level, int added, String id) {
        super(copyOf, listId, value, listName, practicable, validity, lastOpen, level, added);
        this.id = id;
    }

    public WordwID(Word word, String id){
        super(word.getCopyOf(), word.getListId(), word.getValue(), word.getSourceListName(),
                word.isPracticable(), word.getValidity(),
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
        return new Word(getCopyOf(), getListId(), getValue(), getSourceListName(), isPracticable(),
                getValidity(), getLastOpen(), getLevel(), getAdded());
    }

    public static Comparator<WordwID> recAdded_Asc = new Comparator<WordwID>() {
        public int compare(WordwID w1, WordwID w2) {
            if(w1.getAdded() == w2.getAdded()){
                return w1.getValue().toUpperCase().compareTo(w2.getValue().toUpperCase());
            }
            else{
                return  Long.valueOf(w2.getAdded()).compareTo(w1.getAdded());
            }
        }
    };

    public static Comparator<WordwID> recAdded_Dsc = new Comparator<WordwID>() {
        public int compare(WordwID w1, WordwID w2) {
            if(w1.getAdded() == w2.getAdded()){
                return w1.getValue().toUpperCase().compareTo(w2.getValue().toUpperCase());
            }
            else{
                return  Long.valueOf(w1.getAdded()).compareTo(w2.getAdded());
            }
        }
    };

    public static Comparator<WordwID> recViewed_Asc = new Comparator<WordwID>() {
        public int compare(WordwID w1, WordwID w2) {
            if(w1.getLastOpen() == w2.getLastOpen()){
                return w1.getValue().toUpperCase().compareTo(w2.getValue().toUpperCase());
            }
            else{
                return Long.valueOf(w2.getLastOpen()).compareTo( w1.getLastOpen() );
            }
        }
    };

    public static Comparator<WordwID> recViewed_Dsc = new Comparator<WordwID>() {
        public int compare(WordwID w1, WordwID w2) {
            if(w1.getLastOpen() == w2.getLastOpen()){
                return w1.getValue().toUpperCase().compareTo(w2.getValue().toUpperCase());
            }
            else{
                return Long.valueOf(w1.getLastOpen()).compareTo( w2.getLastOpen() );
            }
        }
    };

    public static Comparator<WordwID> alphabetical_Asc = new Comparator<WordwID>() {
        public int compare(WordwID w1, WordwID w2) {
            return w1.getValue().toUpperCase().compareTo(w2.getValue().toUpperCase());
        }
    };

    public static Comparator<WordwID> alphabetical_Dsc = new Comparator<WordwID>() {
        public int compare(WordwID w1, WordwID w2) {
            return w2.getValue().toUpperCase().compareTo(w1.getValue().toUpperCase());
        }
    };

    public static Comparator<WordwID> difficulty_Asc = new Comparator<WordwID>() {
        public int compare(WordwID w1, WordwID w2) {
            if(w1.getLastOpen() == w2.getLastOpen()){
                return w1.getValue().toUpperCase().compareTo(w2.getValue().toUpperCase());
            }
            else{
                return w1.getLevel() - w2.getLevel();
            }
        }
    };

    public static Comparator<WordwID> difficulty_Dsc = new Comparator<WordwID>() {
        public int compare(WordwID w1, WordwID w2) {
            if(w1.getLevel() == w2.getLevel()){
                return w1.getValue().toUpperCase().compareTo(w2.getValue().toUpperCase());
            }
            else{
                return w2.getLevel() - w1.getLevel();
            }
        }
    };


}
