package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/23/16.
 */

public class WordDef {
    private String word, title, syn, ant, def;

    public WordDef() {
        this.word = "";
        this.title = "";
        this.syn = "";
        this.ant = "";
        this.def = "";
    }

    public WordDef(String word, String title, String syn, String ant, String def) {
        this.word = word;
        this.title = title;
        this.syn = syn;
        this.ant = ant;
        this.def = def;

    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
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
