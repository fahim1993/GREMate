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

    public WordwID(String wordSet, String wordList, String value, boolean practicable, boolean shadow, boolean learned, int appeared, int correct, String id) {
        super(wordSet, wordList, value, practicable, shadow, learned, appeared, correct);
        this.id = id;
    }

    public WordwID(Word word, String id){
        super(word.getWordSet(), word.getWordList(), word.getValue(), word.isPracticable(), word.isShadow(),word.isLearned(), word.getAppeared(), word.getCorrect());
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
