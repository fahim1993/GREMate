package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/16/16.
 */

public class WordDef {
    String wordInListID, wListID, definition, pos, sentence;

    public WordDef() {
        this.wordInListID = "";
        this.wListID = "";
        this.definition = "";
        this.pos = "";
        this.sentence = "";
    }


    public WordDef(String wordInListID, String wListID, String definition, String pos, String sentence) {
        this.wordInListID = wordInListID;
        this.wListID = wListID;
        this.definition = definition;
        this.pos = pos;
        this.sentence = sentence;
    }

    public String getWordInListID() {
        return wordInListID;
    }

    public void setWordInListID(String wordInListID) {
        this.wordInListID = wordInListID;
    }

    public String getwListID() {
        return wListID;
    }

    public void setwListID(String wListID) {
        this.wListID = wListID;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }
}
