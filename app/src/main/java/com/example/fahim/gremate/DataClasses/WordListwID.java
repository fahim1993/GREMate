package com.example.fahim.gremate.DataClasses;

/**
 * Created by Fahim on 25-Dec-16.
 */

public class WordListwID extends WordList {
    String id;

    public WordListwID(String wordSet, String name, boolean deletable, int wordCount, int learned, String id) {
        super(wordSet, name, deletable, wordCount, learned);
        this.id = id;
    }

    public WordListwID(WordList wordList, String id) {
        super(wordList.getWordSet(), wordList.getName(), wordList.isDeletable(), wordList.getWordCount(), wordList.getLearned());
        this.id = id;
    }

    public  WordListwID(){
        super();
        id  = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
