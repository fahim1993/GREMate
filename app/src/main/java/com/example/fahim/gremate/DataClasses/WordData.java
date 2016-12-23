package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/15/16.
 */

public class WordData {
    String name, wordInListId, mnemonic, description;

    public WordData() {
        this.name = "";
        this.wordInListId = "";
        this.mnemonic = "";
        this.description = "";
    }


    public WordData(String name, String wordInListId, String mnemonic, String description) {
        this.name = name;
        this.wordInListId = wordInListId;
        this.mnemonic = mnemonic;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWordInListId() {
        return wordInListId;
    }

    public void setWordInListId(String wordInListId) {
        this.wordInListId = wordInListId;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
