package com.example.fahim.gremate.DataClasses;

import java.util.ArrayList;

/**
 * Created by Fahim on 26-Dec-16.
 */

public class WordAllData {
    private Word word;
    private ArrayList<WordDef> wordDefs;
    private WordData wordData;
    private ArrayList<Sentence> sentences;

    public WordAllData() {
        word = new Word();
        wordDefs = new ArrayList<>();
        wordData = new WordData();
        sentences = new ArrayList<>();
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public ArrayList<WordDef> getWordDefs() {
        return wordDefs;
    }

    public void setWordDefs(ArrayList<WordDef> wordDefs) {
        this.wordDefs = wordDefs;
    }

    public WordData getWordData() {
        return wordData;
    }

    public void setWordData(WordData wordData) {
        this.wordData = wordData;
    }

    public ArrayList<Sentence> getSentences() {
        return sentences;
    }

    public void setSentences(ArrayList<Sentence> sentences) {
        this.sentences = sentences;
    }
}
