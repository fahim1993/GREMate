package com.example.fahim.gremate.DataClasses;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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

    private static String username = "-1";
    private static String userid = "-1";

    private static String wordSet;
    private static String wordSetId;
    private static String wordList;
    private static String wordListId;
    private static String wordVal;


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
    public static void newWordSet(String wsname){
        wordSet = wsname;
        if(username.equals("-1")) getUserName(1);
        else {
            ref = db.getReference().child(USER_WORD).child(userid).child(WORDSET);
            String wskey = ref.push().getKey();
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
        wordSetId = wsId;

        if(userid.equals("-1")) initDB();

        ref = db.getReference().child(USER_WORD).child(userid).child(WORD);
        ref.push().setValue(Word.newWord(word, listId, wsId));

    }

    public static void updateWord(Word word, String wordId){
        if(userid.equals("-1")) initDB();

        ref = db.getReference().child(USER_WORD).child(userid).child(WORD);
        ref.child(wordId).setValue(word);

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
}
