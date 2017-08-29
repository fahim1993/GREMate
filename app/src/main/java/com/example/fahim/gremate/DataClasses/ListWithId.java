package com.example.fahim.gremate.DataClasses;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Fahim on 25-Dec-16.
 */

public class ListWithId extends List implements Parcelable {
    String id;

    public ListWithId(String name, int wordCount, String id) {
        super(name, wordCount);
        this.id = id;
    }

    public ListWithId(List wordList, String id) {
        super(wordList.getName(), wordList.getWordCount());
        this.id = id;
    }

    public ListWithId(){
        super();
        id  = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getName());
        parcel.writeInt(getWordCount());
        parcel.writeString(getId());
    }

    private ListWithId(Parcel in) {
        setName(in.readString());
        setWordCount(in.readInt());
        setId(in.readString());
    }

    public static final Parcelable.Creator<ListWithId> CREATOR
            = new Parcelable.Creator<ListWithId>() {

        @Override
        public ListWithId createFromParcel(Parcel in) {
            return new ListWithId(in);
        }

        @Override
        public ListWithId[] newArray(int size) {
            return new ListWithId[size];
        }
    };
}
