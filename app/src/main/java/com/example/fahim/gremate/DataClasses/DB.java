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
    public static String LISTWORDS = "ListWords";
    public static String WORDINLIST = "WordInList";


    private static String username = "-1";
    private static String userid = "-1";

    private static String wordSet;
    private static String wordSetId;
    private static String wordList;
    private static String wordListId;
    private static String wordVal;

    private static ValueEventListener listener1;
    private static DatabaseReference mRef1;

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
            ref = db.getReference().child(USER_WORD).child(userid).child(WORDSET);
            String wskey = ref.push().getKey();                                    // Important
            ref.child(wskey).setValue(WordSet.newWordSet(wsname, username));

            ref = db.getReference().child(USER_WORD).child(userid).child(WORDLIST);
            ref.push().setValue(WordList.getAllList(wskey));
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

        mRef1 = db.getReference().child(USER_WORD).child(userid).child(WORDSET).child(wsId).child("wordCount");
        listener1 = mRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = (long)dataSnapshot.getValue();
                mRef1.setValue(count + 1);
                removeListener1();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void removeListener1(){
        mRef1.removeEventListener(listener1);
    }

    public static void updateWord(Word word, String wordId){
        if(userid.equals("-1")) initDB();

        ref = db.getReference().child(USER_WORD).child(userid).child(WORD);
        ref.child(wordId).setValue(word);

    }

    public static void setWordSetLastOpen(String wsid){
        if(userid.equals("-1")){
            initDB();
        }
        db.getReference().child(USER_WORD).child(userid).child(WORDSET).child(wsid).child("lastOpen").
                setValue(System.currentTimeMillis()/60000);
    }

    public static void setWordLastOpen(String wordId){
        if(userid.equals("-1")){
            initDB();
        }
        db.getReference().child(USER_WORD).child(userid).child(WORD).child(wordId).child("lastOpen").
                setValue(getCurrentMin());
    }

    public static void  setWordLevel(String wordId, int level){
        if(userid.equals("-1")){
            initDB();
        }
        db.getReference().child(USER_WORD).child(userid).child(WORD).child(wordId).child("level").setValue(level);
    }

    public static void setWordData(WordAllData wordAllData, String wordId){
        if(userid.equals("-1")){
            initDB();
        }
        db.getReference().child(USER_WORD).child(userid).child(WORD).child(wordId).setValue(wordAllData.getWord());
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

    public static int getCurrentMin(){
        return (int)(System.currentTimeMillis()/60000);
    }
}
