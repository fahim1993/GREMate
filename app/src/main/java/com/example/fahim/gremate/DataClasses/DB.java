package com.example.fahim.gremate.DataClasses;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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

    private String lastListId;
    private String lastSetId;

    private static ValueEventListener listener1;
    private static ValueEventListener listener2;
    private static ValueEventListener listener3;
    private static ValueEventListener listener4;
    private static ValueEventListener listener5;
    private static ValueEventListener listener6;
    private static ValueEventListener listener7;
    private static ValueEventListener listener8;
    private static ValueEventListener listener9;
    private static ValueEventListener listener10;
    private static ValueEventListener listener11;
    private static ValueEventListener listener12;
    private static ValueEventListener listener13;

    private static ArrayList<ValueEventListener> listeners;

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

//    public static void setCurrentWordSet(String wsId, Context context) {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString("wordSetId", wsId);
//        editor.apply();
//    }
//
//    public static String getCurrentWordSet(Context context){
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        return prefs.getString("wordSetId", "-1");
//    }
//
//    public static void setCurrentList(String wlId, Context context) {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString("wordListId", wlId);
//        editor.apply();
//    }
//
//    public static String getCurrentList(Context context){
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        return prefs.getString("wordListId", "-1");
//    }

    public static void newWordSet(final String wsName) {
        initDB();
        String wsId = WORD_SET.push().getKey();
        String mainListId = WORD_LIST.child(wsId).push().getKey();
        WORD_LIST.child(wsId).child(mainListId).setValue(List.getAllList());
        WORD_SET.child(wsId).setValue(WordSet.newWordSet(wsName, mainListId));
    }

    public static String newList(final String wsId, final String listName) {
        initDB();
        String listId = WORD_LIST.child(wsId).push().getKey();
        WORD_LIST.child(wsId).child(listId).setValue(List.getNewList(listName));
        return listId;
    }

    public static void newWord(final String wsId, final String listId, final String listName,
                               final String mainListId, final String wordValue) {

        initDB();

        String wordId = WORD.child(listId).push().getKey();
        Word w = Word.newWord(listName, wordId, wordValue);
        WORD.child(listId).child(wordId).setValue(w);
        WORD_CLONES.child(wordId).push().setValue(new WordClones(wordId, listId));

        if (!mainListId.equals(listId)) {
            addWordToAnotherList(wsId, mainListId, w);
        }

        final DatabaseReference mRef1 = WORD_LIST.child(wsId).child(mainListId).child("wordCount");
        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = (long) dataSnapshot.getValue();
                mRef1.setValue(count + 1);
                mRef1.removeEventListener(listener1);
                listener1 = null;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mRef1.addValueEventListener(listener1);


        final DatabaseReference mRef2 = WORD_SET.child(wsId).child("wordCount");
        listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = (long) dataSnapshot.getValue();
                mRef2.setValue(count + 1);
                mRef2.removeEventListener(listener2);
                listener2 = null;
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mRef2.addValueEventListener(listener2);
    }

    public static void addWordToAnotherList(String wsId, String toListId, Word _word) {
        initDB();
        String wordId = WORD.child(toListId).push().getKey();
        WORD.child(toListId).child(wordId).setValue(_word);
        WORD_CLONES.child(_word.getCloneOf()).push().setValue(new WordClones(wordId, toListId));

        final DatabaseReference mRef = WORD_LIST.child(wsId).child(toListId).child("wordCount");
        listener3 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = (long) dataSnapshot.getValue();
                mRef.setValue(count + 1);
                mRef.removeEventListener(listener3);
                listener3 = null;
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mRef.addValueEventListener(listener3);

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

        final DatabaseReference mRef = WORD_CLONES.child(wordId);
        listener4 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordClones wcl = ds.getValue(WordClones.class);
                    WORD.child(wcl.getListId()).child(wcl.getCloneId()).child("level").setValue(level);
                }
                mRef.removeEventListener(listener4);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mRef.addValueEventListener(listener4);
    }

    public static void setWordPracticable(String wordId, final boolean val) {
        initDB();

        final DatabaseReference mRef = WORD_CLONES.child(wordId);
        listener5 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordClones wcl = ds.getValue(WordClones.class);
                    WORD.child(wcl.getListId()).child(wcl.getCloneId()).child("practicable").setValue(val);
                }
                mRef.removeEventListener(listener5);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mRef.addValueEventListener(listener5);
    }

    public static void setWordValidity(String wordId, final int val) {
        initDB();

        final DatabaseReference mRef = WORD_CLONES.child(wordId);
        listener6 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordClones wcl = ds.getValue(WordClones.class);
                    WORD.child(wcl.getListId()).child(wcl.getCloneId()).child("validity").setValue(val);
                }
                mRef.removeEventListener(listener6);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mRef.addValueEventListener(listener6);
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

        final DatabaseReference mRef = WORD_CLONES.child(wordId);
        listener7 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordClones wcl = ds.getValue(WordClones.class);
                    WORD.child(wcl.getListId()).child(wcl.getCloneId()).setValue(null);
                    listIds1.add(wcl.getListId());
                }
                if(reduceWordCount)reduceListWordCount(wsId);
                WORD_CLONES.child(wordId).setValue(null);
                mRef.removeEventListener(listener7);
                listener7 = null;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mRef.addValueEventListener(listener7);

        if (!reduceWordCount) return;

        final DatabaseReference mRef1 = WORD_SET.child(wsId).child("wordCount");
        listener8 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = (long) dataSnapshot.getValue();
                mRef1.setValue(count - 1);
                mRef1.removeEventListener(listener8);
                listener8 = null;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mRef1.addValueEventListener(listener8);
    }

    public static void reduceListWordCount(String wsId) {
        listeners = new ArrayList<>();
        for (int i = 0; i < listIds1.size(); i++) {
            String listId = listIds1.get(i);
            final int listenerIndex = i;
            final DatabaseReference mRef = WORD_LIST.child(wsId).child(listId).child("wordCount");
            ValueEventListener mListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long count = (long) dataSnapshot.getValue();
                    mRef.setValue(count - 1);
                    mRef.removeEventListener(listeners.get(listenerIndex));
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mRef.addValueEventListener(mListener);
            listeners.add(mListener);
        }
        listIds1 = new ArrayList<>();
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
        final DatabaseReference mRef2 = WORD_LIST.child(wsId).child(listId).child("wordCount");
        listener9 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = (long) dataSnapshot.getValue();
                mRef2.setValue(count - 1);
                mRef2.removeEventListener(listener9);
                listener9 = null;
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mRef2.addValueEventListener(listener9);
    }

    public static void deleteList(final String wsId, final String listId, final boolean isMainList) {

        initDB();

        if (isMainList) {
            WORD_SET.child(wsId).setValue(null);
            WORD_LIST.child(wsId).setValue(null);

            final DatabaseReference mRef = WORD.child(listId);
            listener10 = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        deleteWord(wsId, ds.child("cloneOf").toString(), false, false);
                    }
                    mRef.removeEventListener(listener10);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mRef.addValueEventListener(listener10);
        } else {
            WORD_LIST.child(wsId).child(listId).setValue(null);

            final DatabaseReference mRef = WORD.child(listId);
            listener11 = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String wordId = ds.getKey();
                        String cloneOf = ds.child("cloneOf").toString();

                        if (wordId.equals(cloneOf))
                            deleteWord(wsId, ds.child("cloneOf").toString(), true, false);
                        else removeWordClone(wsId, listId, wordId, cloneOf, false);
                    }
                    mRef.removeEventListener(listener11);
                    listener11 = null;
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mRef.addValueEventListener(listener11);

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

    public static void initNewUser(String uid, String uname, Context context) {

//        if (USER_ID.equals("-1")) initDB();
//
//        ArrayList<String> initWords = new FeedTestData().getWords(context);
//        int sz = initWords.size();
//
//        for (int wsi = 0; wsi < 3; wsi++) {
//
//            String wsId = WORD_SET.push().getKey();
//            String allListId = WORD_LIST.child(wsId).push().getKey();
//
//            WORD_SET.child(wsId).setValue(new WordSet("GRE: Word Set " + (wsi + 1), initWords.size()));
//            WORD_LIST.child(wsId).child(allListId).setValue(new List("All words", initWords.size()));
//
//            int stIndex = (sz * wsi) / 3;
//            int enIndex = ((sz * (wsi + 1)) / 3) - 1;
//            int wordsInSet = enIndex - stIndex + 1;
//
//            int numberOfLists = wordsInSet / 50;
//
//            ArrayList<String> listIds = new ArrayList<>();
//
//            for (int i = 1; i <= numberOfLists + 1; i++) {
//                int listWordCount = (50 > wordsInSet - ((i - 1) * 50)) ? wordsInSet - ((i - 1) * 50) : 50;
//                listIds.add(WORD_LIST.child(wsId).push().getKey());
//                WORD_LIST.child(wsId).child(listIds.get(i - 1)).setValue(new List("List " + i, listWordCount));
//            }
//            for (int i = stIndex, j = 0; i <= enIndex; i++, j++) {
//                String wId = WORD.push().getKey();
//                int listIndex = (j / 50) + 1;
//                WORD.child(listIds.get(listIndex - 1)).child(wId).setValue(new Word(wId, initWords.get(i), "All words", false, 0, 1));
//                DB.addWordToAnotherList(new Word(wId, initWords.get(i), "List " + listIndex, false, 0, 1), listIds.get(listIndex - 1));
//            }
//        }
    }
}
