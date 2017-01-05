package com.example.fahim.gremate.DataClasses;

import android.util.Log;

import com.google.android.gms.common.data.DataBufferObserverSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Fahim on 24-Dec-16.
 */

public class DB {

    private static FirebaseAuth auth;
    private static FirebaseDatabase db;
    private static DatabaseReference ref;

    public static String USER_DATA = "UserData";
    public static String USER_WORD = "UserWord";
    public static String WORDSET = "WordSet";
    public static String WORDLIST = "WordList";
    public static String WORD = "Word";
    public static String WORDDATA = "WordData";
    public static String WORDDEF = "WordDef";
    public static String WORDDEFP = "WordDefP";
    public static String SENTENCE = "Sentence";
    public static String WORDCLONE = "WordClone";


    private static String username = "-1";
    private static String userid = "-1";

    private static String wordSet;
    private static String wordSetId;
    private static String wordList;
    private static String wordListId;
    private static String wordVal;

    private static ValueEventListener listener1;
    private static ValueEventListener listener2;
    private static ValueEventListener listener3;
    private static ValueEventListener listener4;
    private static ValueEventListener listener5;


    private static void initDB(){
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        userid = auth.getCurrentUser().getUid();
    }
    private static void getUserName(final int fncNo){
        if(username.equals("-1")) {
            initDB();
            DatabaseReference mref = db.getReference().child(USER_DATA).child(userid);
            mref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserData user = dataSnapshot.getValue(UserData.class);
                    username = user.getUserName();
                    switch (fncNo){
                        case 1:
                            newWordSet(wordSet);
                            break;
                        case 2:
                            newList(wordList, wordSetId);
                            break;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public static void deleteWordSet(String wsId){
        if(userid.equals("-1")){
            initDB();
        }
        ref = db.getReference().child(USER_WORD).child(userid).child(WORDSET).child(wsId);
        ref.removeValue();
    }

    public static void newWordSet(String wsname){
        wordSet = wsname;
        if(username.equals("-1")) getUserName(1);
        else {
            String wskey = db.getReference().child(USER_WORD).child(userid).child(WORDSET).push().getKey();

            ref = db.getReference().child(USER_WORD).child(userid).child(WORDLIST);
            String allListKey = ref.push().getKey();
            ref.child(allListKey).setValue(WordList.getAllList(wskey));

            db.getReference().child(USER_WORD).child(userid).child(WORDSET).child(wskey).
                    setValue(WordSet.newWordSet(wsname, username, allListKey));
        }
    }

    public static void newList(String listname, String wsId){
        wordList = listname;
        wordSetId = wsId;

        if(username.equals("-1")) getUserName(2);
        else {
            ref = db.getReference().child(USER_WORD).child(userid).child(WORDLIST);
            ref.push().setValue(WordList.getNewList(wsId, listname));
        }
    }

    public static void newWord(String word, String listId, String wsId){
        wordVal = word;
        wordListId = listId;

        if(userid.equals("-1")) initDB();

        DatabaseReference ref1 = db.getReference().child(USER_WORD).child(userid).child(WORD);
        String wordId = ref1.push().getKey();
        ref1.push().setValue(Word.newWord(listId, word));

        final DatabaseReference mRef1 = db.getReference().child(USER_WORD).child(userid).child(WORDSET).child(wsId).child("wordCount");
        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = (long)dataSnapshot.getValue();
                mRef1.setValue(count + 1);
                mRef1.removeEventListener(listener1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRef1.addValueEventListener(listener1);
    }

    public static void addWordToAnotherList(WordwID wordwID, String listId){

        if(userid.equals("-1")) initDB();

        wordwID.setListId(listId);

        if(wordwID.getCopyOf().length()<=1)
            wordwID.setCopyOf(wordwID.getId());

        DatabaseReference ref1 = db.getReference().child(USER_WORD).child(userid).child(WORD);
        String wordId = ref1.push().getKey();
        ref1.child(wordId).setValue(wordwID.toWord());

        db.getReference().child(USER_WORD).child(userid).child(WORDCLONE).child(wordwID.getCopyOf()).
                push().setValue(new WordClones(wordId));
    }

    public static void setWordSetLastOpen(String wsid){
        if(userid.equals("-1")){
            initDB();
        }
        int time = getCurrentMin();

        db.getReference().child(USER_WORD).child(userid).child(WORDSET).child(wsid).child("lastOpen").
                setValue(time);

    }




    public static void setWordData(WordAllData wordAllData, String wordId){
        if(userid.equals("-1")){
            initDB();
        }

        setWordLastOpen(wordId);
        setWordPracticable(wordId, wordAllData.getWord().isPracticable());
        setWordValidity(wordId, wordAllData.getWord().getValidity());

        db.getReference().child(USER_WORD).child(userid).child(WORDDATA).push().setValue(wordAllData.getWordData());
        for(WordDef def: wordAllData.getWordDefs()){
            db.getReference().child(USER_WORD).child(userid).child(WORDDEF).push().setValue(def);
        }
        for(WordDefP defp: wordAllData.getWordDefPs()){
            db.getReference().child(USER_WORD).child(userid).child(WORDDEFP).push().setValue(defp);
        }
        for(Sentence s: wordAllData.getSentences()){
            db.getReference().child(USER_WORD).child(userid).child(SENTENCE).push().setValue(s);
        }

    }

    public static void setWordLastOpen(String wordId){
        if(userid.equals("-1")){
            initDB();
        }

        final int time = getCurrentMin();

        db.getReference().child(USER_WORD).child(userid).child(WORD).child(wordId).child("lastOpen").
                setValue(time);

        final DatabaseReference mRef = db.getReference().child(USER_WORD).child(userid).child(WORDCLONE).child(wordId);

        listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    WordClones wcl = ds.getValue(WordClones.class);
                    db.getReference().child(USER_WORD).child(userid).child(WORD).child(wcl.getWordId()).child("lastOpen").
                            setValue(time);
                }
                mRef.removeEventListener(listener2);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRef.addValueEventListener(listener2);
    }

    public static void setWordLevel(String wordId, final int level){
        if(userid.equals("-1")){
            initDB();
        }
        db.getReference().child(USER_WORD).child(userid).child(WORD).child(wordId).child("level").setValue(level);

        final DatabaseReference mRef = db.getReference().child(USER_WORD).child(userid).child(WORDCLONE).child(wordId);
        listener3 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    WordClones wcl = ds.getValue(WordClones.class);
                    db.getReference().child(USER_WORD).child(userid).child(WORD).child(wcl.getWordId()).child("level").
                            setValue(level);
                }
                mRef.removeEventListener(listener3);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRef.addValueEventListener(listener3);
    }

    public static void setWordPracticable(String wordId, final boolean val){
        if(userid.equals("-1")){
            initDB();
        }
        db.getReference().child(USER_WORD).child(userid).child(WORD).child(wordId).child("practicable").setValue(val);

        final DatabaseReference mRef = db.getReference().child(USER_WORD).child(userid).child(WORDCLONE).child(wordId);
        listener4 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    WordClones wcl = ds.getValue(WordClones.class);
                    db.getReference().child(USER_WORD).child(userid).child(WORD).child(wcl.getWordId()).child("practicable").
                            setValue(val);
                }
                mRef.removeEventListener(listener4);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRef.addValueEventListener(listener4);
    }

    public static void setWordValidity(String wordId, final int val){
        if(userid.equals("-1")){
            initDB();
        }
        db.getReference().child(USER_WORD).child(userid).child(WORD).child(wordId).child("validity").setValue(val);

        final DatabaseReference mRef = db.getReference().child(USER_WORD).child(userid).child(WORDCLONE).child(wordId);
        listener5 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    WordClones wcl = ds.getValue(WordClones.class);
                    db.getReference().child(USER_WORD).child(userid).child(WORD).child(wcl.getWordId()).child("validity").
                            setValue(val);
                }
                mRef.removeEventListener(listener5);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRef.addValueEventListener(listener5);
    }

    public static int getCurrentMin(){
        return (int)(System.currentTimeMillis()/60000);
    }
}
