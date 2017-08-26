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
    public static String IMAGE = "Image";
    public static String WORDCLONE = "WordClone";
    public static String FRIEND = "Friend";
    public static String FRIENDNOTF = "FriendNotf";


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
    private static ValueEventListener listener6;
    private static ValueEventListener listener7;


    private static void initDB() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        userid = auth.getCurrentUser().getUid();
    }

    private static void getUserName() {
        if (username.equals("-1")) {
            initDB();
            DatabaseReference mref = db.getReference().child(USER_DATA).child(userid);
            mref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserData user = dataSnapshot.getValue(UserData.class);
                    username = user.getUserName();
                    newWordSet(wordSet);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public static void newWordSet(String wsname) {
        wordSet = wsname;
        if (username.equals("-1")) getUserName();
        else {
            String wskey = db.getReference().child(USER_WORD).child(userid).child(WORDSET).push().getKey();

            ref = db.getReference().child(USER_WORD).child(userid).child(WORDLIST);
            String allListKey = ref.push().getKey();
            ref.child(allListKey).setValue(WordList.getAllList(wskey));

            db.getReference().child(USER_WORD).child(userid).child(WORDSET).child(wskey).
                    setValue(WordSet.newWordSet(wsname, username, allListKey));
        }
    }

    public static String newList(String listname, String wsId) {

        if (userid.equals("-1")) {
            initDB();
        }

        wordList = listname;
        wordSetId = wsId;

        String key = db.getReference().child(USER_WORD).child(userid).child(WORDLIST)
                .push().getKey();

        db.getReference().child(USER_WORD).child(userid).child(WORDLIST).child(key)
                .setValue(WordList.getNewList(wsId, listname));

        return  key;
    }

    public static void newWord(String word, String listId, String listName, String wsId, String allListId) {
        wordVal = word;
        wordListId = listId;

        if (userid.equals("-1")) initDB();

        Word w = Word.newWord(listId, listName, word);

        DatabaseReference ref1 = db.getReference().child(USER_WORD).child(userid).child(WORD);
        String key = ref1.push().getKey();
        ref1.child(key).setValue(w);

        if (!allListId.equals(listId)) {
            w.setCopyOf(key);
            w.setListId(allListId);
            addWordToAnotherList(w);
        }

        final DatabaseReference mRef1 = db.getReference().child(USER_WORD).child(userid).child(WORDSET).child(wsId).child("wordCount");
        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = (long) dataSnapshot.getValue();
                mRef1.setValue(count + 1);
                mRef1.removeEventListener(listener1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRef1.addValueEventListener(listener1);
    }

    public static void addWordToAnotherList(Word word_) {

        if (userid.equals("-1")) initDB();

        DatabaseReference ref1 = db.getReference().child(USER_WORD).child(userid).child(WORD);
        String wordId = ref1.push().getKey();
        ref1.child(wordId).setValue(word_);

        db.getReference().child(USER_WORD).child(userid).child(WORDCLONE).child(word_.getCopyOf()).
                push().setValue(new WordClones(wordId));
    }

    public static void setWordSetLastOpen(String wsId) {
        if (userid.equals("-1")) initDB();

        long time = getCurrentMin();

        db.getReference().child(USER_WORD).child(userid).child(WORDSET).child(wsId).child("lastOpen").
                setValue(time);
    }

    public static void setWordData(WordAllData wordAllData, String wordId) {
        Log.d("setWordData", "");

        if (userid.equals("-1")) initDB();

        setWordLastOpen(wordId);
        setWordPracticable(wordId, wordAllData.getWord().isPracticable());
        setWordValidity(wordId, wordAllData.getWord().getValidity());

        db.getReference().child(USER_WORD).child(userid).child(WORDDATA).push().setValue(wordAllData.getWordData());
        for (WordDef def : wordAllData.getWordDefs()) {
            db.getReference().child(USER_WORD).child(userid).child(WORDDEF).push().setValue(def);
        }
        for (Sentence s : wordAllData.getSentences()) {
            db.getReference().child(USER_WORD).child(userid).child(SENTENCE).push().setValue(s);
        }
        for (WordImageFB im : wordAllData.getImages()) {
            db.getReference().child(USER_WORD).child(userid).child(IMAGE).push().setValue(im);
        }
    }

    public static void setWordLastOpen(String wordId) {
        if (userid.equals("-1")) initDB();

        final long time = getCurrentMin();

        db.getReference().child(USER_WORD).child(userid).child(WORD).child(wordId).child("lastOpen").
                setValue(time);

        final DatabaseReference mRef = db.getReference().child(USER_WORD).child(userid).child(WORDCLONE).child(wordId);

        listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
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

    public static void setWordLevel(String wordId, final int level) {
        if (userid.equals("-1")) initDB();

        db.getReference().child(USER_WORD).child(userid).child(WORD).child(wordId).child("level").setValue(level);

        final DatabaseReference mRef = db.getReference().child(USER_WORD).child(userid).child(WORDCLONE).child(wordId);
        listener3 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
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

    public static void setWordPracticable(String wordId, final boolean val) {
        if(userid.equals("-1")) initDB();

        db.getReference().child(USER_WORD).child(userid).child(WORD).child(wordId).child("practicable").setValue(val);

        final DatabaseReference mRef = db.getReference().child(USER_WORD).child(userid).child(WORDCLONE).child(wordId);
        listener4 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
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

    public static void setWordValidity(String wordId, final int val) {
        if(userid.equals("-1")) initDB();
        db.getReference().child(USER_WORD).child(userid).child(WORD).child(wordId).child("validity").setValue(val);

        final DatabaseReference mRef = db.getReference().child(USER_WORD).child(userid).child(WORDCLONE).child(wordId);
        listener5 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
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

    public static long getCurrentMin() {
        return System.currentTimeMillis();
    }

    public static void deleteWord(final String wordId, final String wsId, boolean reduceWordCount, boolean isEdit) {

        if(userid.equals("-1")) initDB();

        if(!isEdit)db.getReference().child(USER_WORD).child(userid).child(WORD).child(wordId).setValue(null);

        db.getReference().child(USER_WORD).child(userid).child(IMAGE).orderByChild("word").
                equalTo(wordId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    db.getReference().child(USER_WORD).child(userid).child(IMAGE).child(ds.getKey()).setValue(null);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        db.getReference().child(USER_WORD).child(userid).child(SENTENCE).orderByChild("word").
                equalTo(wordId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    db.getReference().child(USER_WORD).child(userid).child(SENTENCE).child(ds.getKey()).setValue(null);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        db.getReference().child(USER_WORD).child(userid).child(WORDDEF).orderByChild("word").
                equalTo(wordId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    db.getReference().child(USER_WORD).child(userid).child(WORDDEF).child(ds.getKey()).setValue(null);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        db.getReference().child(USER_WORD).child(userid).child(WORDDATA).orderByChild("word").
                equalTo(wordId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    db.getReference().child(USER_WORD).child(userid).child(WORDDATA).child(ds.getKey()).setValue(null);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        if(isEdit)return;

        db.getReference().child(USER_WORD).child(userid).child(WORDCLONE).child(wordId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String clId = (String) ds.child("wordId").getValue();
                            db.getReference().child(USER_WORD).child(userid).child(WORD).child(clId).setValue(null);
                        }
                        db.getReference().child(USER_WORD).child(userid).child(WORDCLONE).child(wordId).setValue(null);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        if (!reduceWordCount) return;

        final DatabaseReference mRef1 = db.getReference().child(USER_WORD).child(userid).child(WORDSET).child(wsId).child("wordCount");
        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = (long) dataSnapshot.getValue();
                mRef1.setValue(count - 1);
                mRef1.removeEventListener(listener1);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRef1.addValueEventListener(listener1);
    }

    public static void removeWordClone(String cloneId, final String wordId) {

        if(userid.equals("-1")) initDB();

        db.getReference().child(USER_WORD).child(userid).child(WORD).child(cloneId).setValue(null);

        db.getReference().child(USER_WORD).child(userid).child(WORDCLONE).child(wordId).orderByChild("wordId")
                .equalTo(cloneId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    db.getReference().child(USER_WORD).child(userid).child(WORDCLONE).child(wordId).child(ds.getKey()).setValue(null);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public static void deleteList(String listId, final String wsId, final boolean isAllList) {

        if(userid.equals("-1")) initDB();

        if (isAllList) {
            db.getReference(DB.USER_WORD).child(userid).child(WORDSET).child(wsId).setValue(null);

            final Query qr = db.getReference(DB.USER_WORD).child(userid).child(WORDLIST).orderByChild("wordSet")
                    .equalTo(wsId);
            listener7 = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String key = ds.getKey();
                        db.getReference(DB.USER_WORD).child(userid).child(WORDLIST).child(key).setValue(null);
                    }
                    qr.removeEventListener(listener7);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            qr.addValueEventListener(listener7);

        } else
            db.getReference(DB.USER_WORD).child(userid).child(WORDLIST).child(listId).setValue(null);

        final Query q = db.getReference(DB.USER_WORD).child(userid).child(DB.WORD).orderByChild("listId")
                .equalTo(listId);
        listener6 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String key = ds.getKey();
                    String copy = (String) ds.child("copyOf").getValue();

                    if (isAllList || copy == null || copy.length() < 1)
                        deleteWord(key, wsId, true, false);
                    else
                        removeWordClone(key, copy);
                }
                q.removeEventListener(listener6);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        q.addValueEventListener(listener6);
    }

    public static void initNewUser(String uid, String uname) {

        DatabaseReference _db = FirebaseDatabase.getInstance().getReference();
        String[] initWords = new FeedTestData().getWords();
        int i;
        for (i = 8; i >= 1; i--) {
            String wsId = _db.child(USER_WORD).child(uid).child(WORDSET).push().getKey();
            String allListId = _db.child(USER_WORD).child(uid).child(WORDLIST).push().getKey();
            _db.child(USER_WORD).child(uid).child(WORDSET).child(wsId).setValue(new WordSet("GRE: Set " + i, uname, allListId, 80, 0));
            _db.child(USER_WORD).child(uid).child(WORDLIST).child(allListId).setValue(new WordList(wsId, "All words", 80));


            for (int j = (i - 1) * 80; j < (i) * 80; j++) {
                _db.child(USER_WORD).child(uid).child(WORD).push().setValue(new Word("", allListId, initWords[j], "All words", false, 0, 0, 1, 0));
            }
        }
    }
}
