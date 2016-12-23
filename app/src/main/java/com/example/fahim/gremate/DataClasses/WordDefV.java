package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/23/16.
 */

public class WordDefV {
    private String word, title, syn, ant;

    public WordDefV() {
        this.word = "";
        this.title = "";
        this.syn = "";
        this.ant = "";
    }

    public WordDefV(String word, String title, String syn, String ant) {
        this.word = word;
        this.title = title;
        this.syn = syn;
        this.ant = ant;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSyn() {
        return syn;
    }

    public void setSyn(String syn) {
        this.syn = syn;
    }

    public String getAnt() {
        return ant;
    }

    public void setAnt(String ant) {
        this.ant = ant;
    }
}
