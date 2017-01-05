package com.example.fahim.gremate.DataClasses;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by fahim on 12/23/16.
 */

public class Word implements Parcelable{


    private String copyOf, listId, value;
    private boolean practicable;
    private int validity, lastOpen, level, added;

    public static Word newWord(String listId, String val) {
        return new Word(listId, val, false, 0, 0, 1, DB.getCurrentMin());
    }

    public Word() {
        this.copyOf = "";
        this.listId = "";
        this.value = "";
        this.practicable = false;
        this.lastOpen = 0;
        this.validity = 0;
        this.level = 0;
        this.added = 0;
    }
    public Word(String listId, String value, boolean practicable, int validity, int lastOpen, int level, int added) {
        this.copyOf = "";
        this.listId = listId;
        this.value = value;
        this.practicable = practicable;
        this.validity = validity;
        this.lastOpen = lastOpen;
        this.level = level;
        this.added = added;
    }

    public int getAdded() {
        return added;
    }

    public void setAdded(int added) {
        this.added = added;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
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

    public int getLastOpen() {
        return lastOpen;
    }

    public void setLastOpen(int lastOpen) {
        this.lastOpen = lastOpen;
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

    public String getCopyOf() {
        return copyOf;
    }

    public void setCopyOf(String copyOf) {
        this.copyOf = copyOf;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(copyOf);
        parcel.writeString(listId);
        parcel.writeString(value);
        parcel.writeValue(practicable);
        parcel.writeInt(validity);
        parcel.writeValue(lastOpen);
        parcel.writeValue(level);
        parcel.writeValue(added);
    }

    private Word(Parcel in) {
        copyOf = in.readString();
        listId = in.readString();
        value = in.readString();
        practicable = (Boolean) in.readValue( null );
        validity = in.readInt();
        lastOpen = in.readInt();
        level = in.readInt();
        added = in.readInt();
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
