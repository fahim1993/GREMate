package com.example.fahim.gremate.DataClasses;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import com.example.fahim.gremate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Fahim on 24-Dec-16.
 */

public class DB {

    public static DatabaseReference USER_DATA;
    public static DatabaseReference USER_WORD;
    public static DatabaseReference WORD_SET;
    public static DatabaseReference WORD_LIST;
    public static DatabaseReference WORD;
    public static DatabaseReference WORD_DATA;
    public static DatabaseReference WORD_DEF;
    public static DatabaseReference SENTENCE;
    public static DatabaseReference IMAGE;
    public static DatabaseReference WORD_CLONES;
    public static DatabaseReference LAST_LIST;
    public static DatabaseReference LAST_SET;

    public static String USER_ID = "-1";


    private static SparseArray<ValueEventListener> listenerMap = new SparseArray<>();
    private static int listenerCounter = 0;

    private static ArrayList<String> listIds1;

    public static void initDB() {

        if(!USER_ID.equals("-1")) return;

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        USER_ID = auth.getCurrentUser().getUid();

        USER_DATA = db.getReference().child("UserData");
        USER_WORD = db.getReference().child("UserWords");
        WORD_SET = USER_WORD.child(USER_ID).child("WordSet");
        WORD_LIST = USER_WORD.child(USER_ID).child("List");
        WORD = USER_WORD.child(USER_ID).child("Word");
        WORD_DATA = USER_WORD.child(USER_ID).child("WordData");
        WORD_DEF = USER_WORD.child(USER_ID).child("WordDef");
        SENTENCE = USER_WORD.child(USER_ID).child("Sentence");
        IMAGE = USER_WORD.child(USER_ID).child("Image");
        WORD_CLONES = USER_WORD.child(USER_ID).child("WordClones");
        LAST_LIST = USER_WORD.child(USER_ID).child("LastListId");
        LAST_SET = USER_WORD.child(USER_ID).child("LastWordSetId");
    }

    public static Pair<String, String> newWordSet(final String wsName) {
        initDB();
        String wsId = WORD_SET.push().getKey();
        String mainListId = WORD_LIST.child(wsId).push().getKey();
        WORD_LIST.child(wsId).child(mainListId).setValue(List.getAllList());
        WORD_SET.child(wsId).setValue(WordSet.newWordSet(wsName, mainListId));
        return new Pair<>(wsId, mainListId);
    }

    public static String newList(final String wsId, final String listName) {
        initDB();
        String listId = WORD_LIST.child(wsId).push().getKey();
        WORD_LIST.child(wsId).child(listId).setValue(List.getNewList(listName));
        return listId;
    }

    public static String newWord(final String wsId, final String listId, final String listName,
                               final String mainListId, final String wordValue) {

        initDB();
        String wordId = WORD.child(listId).push().getKey();
        Word w = Word.newWord(listName, wordId, wordValue);
        WORD.child(listId).child(wordId).setValue(w);
        WORD_CLONES.child(wordId).push().setValue(new WordClones(wordId, listId));

        if (!mainListId.equals(listId)) {
            addWordToAnotherList(wsId, mainListId, w);
        }

        final int listenerKey1 = ++listenerCounter;
        final DatabaseReference mRef1 = WORD_LIST.child(wsId).child(listId).child("wordCount");
        ValueEventListener listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    long count = (long) dataSnapshot.getValue();
                    mRef1.setValue(count + 1);
                }
                if(listenerMap.get(listenerKey1)!=null) {
                    mRef1.removeEventListener(listenerMap.get(listenerKey1));
                    listenerMap.remove(listenerKey1);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError){}
        };
        mRef1.addValueEventListener(listener1);
        listenerMap.put(listenerKey1, listener1);

        final int listenerKey2 = ++listenerCounter;
        final DatabaseReference mRef2 = WORD_SET.child(wsId).child("wordCount");
        ValueEventListener listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    long count = (long) dataSnapshot.getValue();
                    mRef2.setValue(count + 1);
                }
                if(listenerMap.get(listenerKey2)!=null) {
                    mRef2.removeEventListener(listenerMap.get(listenerKey2));
                    listenerMap.remove(listenerKey2);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mRef2.addValueEventListener(listener2);
        listenerMap.put(listenerKey2, listener2);

        return wordId;
    }

    public static void addWordToAnotherList(String wsId, String toListId, Word _word) {
        initDB();
        String wordId = WORD.child(toListId).push().getKey();
        WORD.child(toListId).child(wordId).setValue(_word);
        WORD_CLONES.child(_word.getCloneOf()).push().setValue(new WordClones(wordId, toListId));

        final int listenerKey = ++listenerCounter;
        final DatabaseReference mRef = WORD_LIST.child(wsId).child(toListId).child("wordCount");
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    long count = (long) dataSnapshot.getValue();
                    mRef.setValue(count + 1);
                }
                if(listenerMap.get(listenerKey)!=null) {
                    mRef.removeEventListener(listenerMap.get(listenerKey));
                    listenerMap.remove(listenerKey);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mRef.addValueEventListener(listener);
        listenerMap.put(listenerKey, listener);
    }

    public static void setWordData(WordAllData wordAllData, String wordId) {

        initDB();

        setWordPracticable(wordId, wordAllData.getWord().isPracticable());
        setWordValidity(wordId, wordAllData.getWord().getValidity());
        setWordLevel(wordId, wordAllData.getWord().getLevel());

        if (wordAllData.getWordData() != null) {
            WORD_DATA.child(wordId).push().setValue(wordAllData.getWordData());
        }

        if (wordAllData.getWordDefs() != null) {
            for (WordDef def : wordAllData.getWordDefs()) {
                WORD_DEF.child(wordId).push().setValue(def);
            }
        }
        if (wordAllData.getWordSentences() != null) {
            for (WordSentence s : wordAllData.getWordSentences()) {
                SENTENCE.child(wordId).push().setValue(s);
            }
        }
        if (wordAllData.getImages() != null) {
            for (WordImageFB im : wordAllData.getImages()) {
                IMAGE.child(wordId).push().setValue(im);
            }
        }
    }

    public static void setWordLevel(final String wordId, final int level) {
        initDB();

        final int listenerKey = ++listenerCounter;
        final DatabaseReference mRef = WORD_CLONES.child(wordId);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordClones wcl = ds.getValue(WordClones.class);
                    WORD.child(wcl.getListId()).child(wcl.getCloneId()).child("level").setValue(level);
                }
                if(listenerMap.get(listenerKey)!=null) {
                    mRef.removeEventListener(listenerMap.get(listenerKey));
                    listenerMap.remove(listenerKey);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mRef.addValueEventListener(listener);
        listenerMap.put(listenerKey, listener);
    }

    public static void setWordPracticable(String wordId, final boolean val) {
        initDB();

        final int listenerKey = ++listenerCounter;
        final DatabaseReference mRef = WORD_CLONES.child(wordId);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordClones wcl = ds.getValue(WordClones.class);
                    WORD.child(wcl.getListId()).child(wcl.getCloneId()).child("practicable").setValue(val);
                }
                if(listenerMap.get(listenerKey)!=null) {
                    mRef.removeEventListener(listenerMap.get(listenerKey));
                    listenerMap.remove(listenerKey);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mRef.addValueEventListener(listener);
        listenerMap.put(listenerKey, listener);
    }

    public static void setWordValidity(String wordId, final int val) {
        initDB();
        final int listenerKey = ++listenerCounter;
        final DatabaseReference mRef = WORD_CLONES.child(wordId);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordClones wcl = ds.getValue(WordClones.class);
                    WORD.child(wcl.getListId()).child(wcl.getCloneId()).child("validity").setValue(val);
                }
                if(listenerMap.get(listenerKey)!=null){
                    mRef.removeEventListener(listenerMap.get(listenerKey));
                    listenerMap.remove(listenerKey);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mRef.addValueEventListener(listener);
        listenerMap.put(listenerKey, listener);
    }

    public static void deleteWord(final String wsId, final String wordId,
                                  final boolean reduceWordCount, boolean isEdit) {


        initDB();

        IMAGE.child(wordId).setValue(null);
        SENTENCE.child(wordId).setValue(null);
        WORD_DEF.child(wordId).setValue(null);
        WORD_DATA.child(wordId).setValue(null);

        if (isEdit) return;

        listIds1 = new ArrayList<>();

        final int listenerKey1 = ++listenerCounter;
        final DatabaseReference mRef1 = WORD_CLONES.child(wordId);
        ValueEventListener listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordClones wcl = ds.getValue(WordClones.class);
                    WORD.child(wcl.getListId()).child(wcl.getCloneId()).setValue(null);
                    listIds1.add(wcl.getListId());
                }
                if(reduceWordCount)reduceListWordCount(wsId);
                WORD_CLONES.child(wordId).setValue(null);
                if(listenerMap.get(listenerKey1)!=null){
                    mRef1.removeEventListener(listenerMap.get(listenerKey1));
                    listenerMap.remove(listenerKey1);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError){}
        };
        mRef1.addValueEventListener(listener1);
        listenerMap.put(listenerKey1, listener1);

        if (!reduceWordCount) return;
        final int listenerKey2 = ++listenerCounter;
        final DatabaseReference mRef2 = WORD_SET.child(wsId).child("wordCount");
        ValueEventListener listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    long count = (long) dataSnapshot.getValue();
                    mRef2.setValue(count - 1);
                }
                if(listenerMap.get(listenerKey2)!=null){
                    mRef2.removeEventListener(listenerMap.get(listenerKey2));
                    listenerMap.remove(listenerKey2);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError){}
        };
        mRef2.addValueEventListener(listener2);
        listenerMap.put(listenerKey2, listener2);
    }

    public static void reduceListWordCount(String wsId) {
        for (int i = 0; i < listIds1.size(); i++) {
            String listId = listIds1.get(i);
            final int listenerKey = ++listenerCounter;
            final DatabaseReference mRef = WORD_LIST.child(wsId).child(listId).child("wordCount");
            ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        long count = (long) dataSnapshot.getValue();
                        mRef.setValue(count - 1);
                    }
                    if(listenerMap.get(listenerKey)!=null){
                        mRef.removeEventListener(listenerMap.get(listenerKey));
                        listenerMap.remove(listenerKey);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mRef.addValueEventListener(listener);
            listenerMap.put(listenerKey, listener);
        }
    }

    public static void removeWordClone(final String wsId, final String listId, final String wordId, final String cloneId, boolean reduceCount) {

        initDB();
        WORD.child(listId).child(cloneId).setValue(null);

        WORD_CLONES.child(wordId).orderByChild("cloneId")
                .equalTo(cloneId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WORD_CLONES.child(wordId).child(ds.getKey()).setValue(null);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        if(!reduceCount)return;
        final int listenerKey = ++listenerCounter;
        final DatabaseReference mRef = WORD_LIST.child(wsId).child(listId).child("wordCount");
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    long count = (long) dataSnapshot.getValue();
                    mRef.setValue(count - 1);
                }
                if(listenerMap.get(listenerKey)!=null){
                    mRef.removeEventListener(listenerMap.get(listenerKey));
                    listenerMap.remove(listenerKey);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mRef.addValueEventListener(listener);
        listenerMap.put(listenerKey, listener);
    }

    public static void deleteList(final String wsId, final String listId, final boolean isMainList) {

        initDB();

        if (isMainList) {
            WORD_SET.child(wsId).setValue(null);
            WORD_LIST.child(wsId).setValue(null);

            final int listenerKey1 = ++listenerCounter;
            final DatabaseReference mRef1 = WORD.child(listId);
            ValueEventListener listener1 = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        deleteWord(wsId, ds.child("cloneOf").getValue().toString(), false, false);
                    }
                    if(listenerMap.get(listenerKey1)!=null){
                        mRef1.removeEventListener(listenerMap.get(listenerKey1));
                        listenerMap.remove(listenerKey1);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError){}
            };
            mRef1.addValueEventListener(listener1);
            listenerMap.put(listenerKey1, listener1);

        } else {
            WORD_LIST.child(wsId).child(listId).setValue(null);
            final int listenerKey2 = ++listenerCounter;
            final DatabaseReference mRef2 = WORD.child(listId);
            ValueEventListener listener2 = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String wordId = ds.getKey();
                        String cloneOf = ds.child("cloneOf").getValue().toString();

                        if (wordId.equals(cloneOf))
                            deleteWord(wsId, ds.child("cloneOf").getValue().toString(), true, false);
                        else removeWordClone(wsId, listId, wordId, cloneOf, false);
                    }
                    if(listenerMap.get(listenerKey2)!=null){
                        mRef2.removeEventListener(listenerMap.get(listenerKey2));
                        listenerMap.remove(listenerKey2);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError){}
            };
            mRef2.addValueEventListener(listener2);
            listenerMap.put(listenerKey2, listener2);
        }
    }

    public static void setLastListId(String listId){
        initDB();
        LAST_LIST.setValue(listId);
    }

    public static void setLastWordSetId(String wsId){
        initDB();
        LAST_SET.setValue(wsId);
    }


//    public static void initNewUser(String uid, String uname, Context context) {
//
//        DatabaseReference _db = FirebaseDatabase.getInstance().getReference();
//        ArrayList<String> initWords = new FeedTestData().getWords(context);
//        int i;
//        for (i = 8; i >= 1; i--) {
//            String wsId = _db.child(USER_WORD).child(uid).child(WORDSET).push().getKey();
//            String allListId = _db.child(USER_WORD).child(uid).child(WORDLIST).push().getKey();
//            _db.child(USER_WORD).child(uid).child(WORDSET).child(wsId).setValue(new WordSet("GRE: Set " + i, uname, allListId, 80, 0));
//            _db.child(USER_WORD).child(uid).child(WORDLIST).child(allListId).setValue(new List(wsId, "All words", 80));
//
//            for (int j = (i - 1) * 80; j < (i) * 80; j++) {
//                _db.child(USER_WORD).child(uid).child(WORD).push().setValue(new Word("", allListId, initWords.get(j), "All words", false, 0, 0, 1, 0));
//            }
//        }
//    }

    public static void initNewUser(String uId, Context context) {

        DatabaseReference UW_USER = FirebaseDatabase.getInstance().getReference().child("UserWords").child(uId);
        USER_ID = uId;
        USER_DATA = FirebaseDatabase.getInstance().getReference().child("UserData");
        USER_WORD = FirebaseDatabase.getInstance().getReference().child("UserWords");
        WORD_SET = UW_USER.child("WordSet");
        WORD_LIST = UW_USER.child("List");
        WORD = UW_USER.child("Word");
        WORD_DATA = UW_USER.child("WordData");
        WORD_DEF = UW_USER.child("WordDef");
        SENTENCE = UW_USER.child("Sentence");
        IMAGE = UW_USER.child("Image");
        WORD_CLONES = UW_USER.child("WordClones");
        LAST_LIST = UW_USER.child("LastListId");
        LAST_SET = UW_USER.child("LastWordSetId");


        Random random = new Random();

        final String mainListName = "All Words";

        ArrayList<String> initWords = new FeedTestData().getWords(context);

        for (int wsi = 3; wsi >= 1; wsi--) {

            String wsId = WORD_SET.push().getKey();
            String mainListId = WORD_LIST.child(wsId).push().getKey();

            WORD_SET.child(wsId).setValue(new WordSet("GRE: Word Set "+wsi, mainListId, 350));
            WORD_LIST.child(wsId).child(mainListId).setValue(new List(mainListName, 350));

            for (int ls = 7; ls >= 1; ls--) {
                String listId =  WORD_LIST.child(wsId).push().getKey();
                WORD_LIST.child(wsId).child(listId).setValue(new List("List "+ls, 50));
                for (int i = 0; i < 50; i++) {
                    int index = random.nextInt(initWords.size());
                    String wordValue = initWords.get(index);
                    initWords.remove(index);
                    String wordId = WORD.child(mainListId).push().getKey();
                    String cloneId = WORD.child(listId).push().getKey();
                    WORD.child(mainListId).child(wordId).setValue(Word.newWord(mainListName, wordId, wordValue));
                    WORD.child(listId).child(cloneId).setValue(Word.newWord(mainListName, wordId, wordValue));
                    WORD_CLONES.child(wordId).push().setValue(new WordClones(cloneId, listId));
                    WORD_CLONES.child(wordId).push().setValue(new WordClones(wordId, mainListId));
                }
            }
        }
    }
}
