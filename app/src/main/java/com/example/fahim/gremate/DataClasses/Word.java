package com.example.fahim.gremate.DataClasses;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by fahim on 12/23/16.
 */

public class Word implements Parcelable{


    private String wordSet, wordList, value;
    private boolean practicable, learned;
    private int appeared, correct, validity;

    public static Word newWord(String val, String listId, String wsId){
        return new Word(wsId, listId, val, false, false, 0, 0, 0);
    }

    public Word() {
        this.wordSet = "";
        this.wordList = "";
        this.value = "";
        this.practicable = false;
        this.learned = false;
        this.appeared = 0;
        this.correct = 0;
        this.validity = 0;
    }
    public Word(String wordSet, String wordList, String value, boolean practicable, boolean learned, int appeared, int correct, int validity) {
        this.wordSet = wordSet;
        this.wordList = wordList;
        this.value = value;
        this.practicable = practicable;
        this.learned = learned;
        this.appeared = appeared;
        this.correct = correct;
        this.validity = validity;
    }

    public String getWordSet() {
        return wordSet;
    }

    public void setWordSet(String wordSet) {
        this.wordSet = wordSet;
    }

    public String getWordList() {
        return wordList;
    }

    public void setWordList(String wordList) {
        this.wordList = wordList;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isPracticable() {
        return practicable;
    }

    public void setPracticable(boolean practicable) {
        this.practicable = practicable;
    }

    public boolean isLearned() {
        return learned;
    }

    public void setLearned(boolean learned) {
        this.learned = learned;
    }

    public int getAppeared() {
        return appeared;
    }

    public void setAppeared(int appeared) {
        this.appeared = appeared;
    }

    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }

    public int getValidity() {
        return validity;
    }

    public void setValidity(int validity) {
        this.validity = validity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(wordSet);
        parcel.writeString(wordList);
        parcel.writeString(value);
        parcel.writeValue(practicable);
        parcel.writeValue(learned);
        parcel.writeInt(appeared);
        parcel.writeInt(correct);
        parcel.writeInt(validity);
    }

    private Word(Parcel in) {
        wordSet = in.readString();
        wordList = in.readString();
        value = in.readString();
        practicable = (Boolean) in.readValue( null );
        learned = (Boolean) in.readValue( null );
        appeared = in.readInt();
        correct = in.readInt();
        validity = in.readInt();
    }

    public static final Parcelable.Creator<Word> CREATOR
            = new Parcelable.Creator<Word>() {

        @Override
        public Word createFromParcel(Parcel in) {
            return new Word(in);
        }

        @Override
        public Word[] newArray(int size) {
            return new Word[size];
        }
    };
}
