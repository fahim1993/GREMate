package com.example.fahim.gremate.DataClasses;

import com.google.firebase.database.Exclude;

import java.util.Random;

/**
 * Created by Fahim on 24-Jan-18.
 */

public class WordPractice {
    private String word, synonyms, definitions;

    public WordPractice() {
        this.word = "";
        this.synonyms = "";
        this.definitions = "";
    }

    public WordPractice(String word, String synonyms, String definitions) {
        this.word = word;
        this.synonyms = synonyms;
        this.definitions = definitions;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String synonyms) {
        this.synonyms = synonyms;
    }

    public String getDefinitions() {
        return definitions;
    }

    public void setDefinitions(String definitions) {
        this.definitions = definitions;
    }

    public boolean hasSynonyms() {
        return (synonyms != null && synonyms.length() > 0);
    }
    public boolean hasDefinitions(){
        return ( definitions != null && definitions.length()>0);
    }

    @Exclude
    public String getRandomSynonym(){
        String [] syns = synonyms.split(DB.DELIM);
        return syns[new Random().nextInt(syns.length)];
    }

    @Exclude
    public String getRandomDefinition(){
        String [] defs = definitions.split(DB.DELIM);
        String ret = defs[new Random().nextInt(defs.length)];
        int length = ret.length();
        if(ret.charAt(length-1)=='.') return ret.substring(0, length-1);
        return ret;
    }

}
