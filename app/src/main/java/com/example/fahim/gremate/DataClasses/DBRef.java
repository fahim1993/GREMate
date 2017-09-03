package com.example.fahim.gremate.DataClasses;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

/**
 * Created by Fahim on 31-Aug-17.
 */

//        DatabaseReference UW_USER = FirebaseDatabase.getInstance().getReference().child("UserWords").child(uId);
//        USER_ID = uId;
//        USER_DATA = FirebaseDatabase.getInstance().getReference().child("UserData");
//        USER_WORD = FirebaseDatabase.getInstance().getReference().child("UserWords");
//        WORD_SET = UW_USER.child("WordSet");
//        WORD_LIST = UW_USER.child("List");
//        WORD = UW_USER.child("Word");
//        WORD_DATA = UW_USER.child("WordData");
//        WORD_DEF = UW_USER.child("WordDef");
//        SENTENCE = UW_USER.child("Sentence");
//        IMAGE = UW_USER.child("Image");
//        WORD_CLONES = UW_USER.child("WordClones");
//        LAST_LIST = UW_USER.child("LastListId");
//        LAST_SET = UW_USER.child("LastWordSetId");

//        public static DatabaseReference USER_DATA;
//        public static DatabaseReference USER_WORD;
//        public static DatabaseReference WORD_SET;
//        public static DatabaseReference WORD_LIST;
//        public static DatabaseReference WORD;
//        public static DatabaseReference WORD_DATA;
//        public static DatabaseReference WORD_DEF;
//        public static DatabaseReference SENTENCE;
//        public static DatabaseReference IMAGE;
//        public static DatabaseReference WORD_CLONES;
//        public static DatabaseReference LAST_LIST;
//        public static DatabaseReference LAST_SET;


//        USER_DATA = db.getReference().child("UserData");
//        USER_WORD = db.getReference().child("UserWords");
//        WORD_SET = USER_WORD.child(USER_ID).child("WordSet");
//        WORD_LIST = USER_WORD.child(USER_ID).child("List");
//        WORD = USER_WORD.child(USER_ID).child("Word");
//        WORD_DATA = USER_WORD.child(USER_ID).child("WordData");
//        WORD_DEF = USER_WORD.child(USER_ID).child("WordDef");
//        SENTENCE = USER_WORD.child(USER_ID).child("Sentence");
//        IMAGE = USER_WORD.child(USER_ID).child("Image");
//        WORD_CLONES = USER_WORD.child(USER_ID).child("WordClones");
//        LAST_LIST = USER_WORD.child(USER_ID).child("LastListId");
//        LAST_SET = USER_WORD.child(USER_ID).child("LastWordSetId");
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//        FirebaseDatabase db = FirebaseDatabase.getInstance();
//        USER_ID = auth.getCurrentUser().getUid();


public class DBRef {
    private final String   USER_DATA = "UserData";
    private final String   USER_WORD = "UserWords";
    private final String   WORD_SET = "WordSet";
    private final String   WORD_LIST = "List";
    private final String   WORD = "Word";
    private final String   WORD_DATA = "WordData";
    private final String   WORD_DEF = "WordDef";
    private final String   SENTENCE = "Sentence";
    private final String   IMAGE = "Image";
    private final String   WORD_CLONES = "WordClones";
    private final String   LAST_LIST = "LastListId";
    private final String   LAST_SET = "LastWordSetId";

    private String uId;
    private DatabaseReference userWord;
    public DBRef(){
        uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userWord = FirebaseDatabase.getInstance().getReference().child(USER_WORD).child(uId);
    }
    public DBRef(String uId){
        this.uId = uId;
        userWord = FirebaseDatabase.getInstance().getReference().child(USER_WORD).child(uId);
    }
    public String getWordSetKey(){
        return userWord.child(WORD_SET).push().getKey();
    }
    public String getListKey(String wsId){
        return userWord.child(WORD_LIST).child(wsId).push().getKey();
    }
    public void setWordSetData(String wsId, WordSet data){
        userWord.child(WORD_SET).child(wsId).setValue(data);
    }
    public void setListData(String wsId, String listId, List data){
        userWord.child(WORD_LIST).child(wsId).child(listId).setValue(data);
    }
    public String getWordId(String listId){
        return userWord.child(WORD).child(listId).push().getKey();
    }
    public void setWordData(String listId, String wordId, Word data){
        userWord.child(WORD).child(listId).child(wordId).setValue(data);
    }
    public void setWordClone(String listId, String wordId, String cloneId){
        userWord.child(WORD_CLONES).child(wordId).push().setValue(new WordClones(listId, cloneId));
    }
    public DatabaseReference listCountRef(String wsId, String listId){
        return userWord.child(WORD_LIST).child(wsId).child(listId).child("wordCount");
    }
    public DatabaseReference wordSetCountRef(String wsId){
        return userWord.child(WORD_SET).child(wsId).child("wordCount");
    }
    public void setWordDataData(String wordId, WordData data){
        userWord.child(WORD_DATA).child(wordId).push().setValue(data);
    }
    public void setWordDefData(String wordId, WordDef data){
        userWord.child(WORD_DEF).child(wordId).push().setValue(data);
    }
    public void setSentenceData(String wordId, WordSentence sentence){
        userWord.child(SENTENCE).child(wordId).push().setValue(sentence);
    }
    public void setImageData(String wordId, WordImageFB image){
        userWord.child(IMAGE).child(wordId).push().setValue(image);
    }
    public DatabaseReference wordCloneRef(String wordId){
        return userWord.child(WORD_CLONES).child(wordId);
    }
    public void setWordLevel(String listId, String wordId, int level){
        userWord.child(WORD).child(listId).child(wordId).child("level").setValue(level);
    }
    public void setWordPracticable(String listId, String wordId, boolean practicable){
        userWord.child(WORD).child(listId).child(wordId).child("practicable").setValue(practicable);
    }
    public void setWordValidity(String listId, String wordId, int validity){
        userWord.child(WORD).child(listId).child(wordId).child("validity").setValue(validity);
    }
    public void deleteWordData(String wordId){
        userWord.child(IMAGE).child(wordId).setValue(null);
        userWord.child(SENTENCE).child(wordId).setValue(null);
        userWord.child(WORD_DEF).child(wordId).setValue(null);
        userWord.child(WORD_DATA).child(wordId).setValue(null);
    }
    public void deleteWord(String listId, String wordId){
        userWord.child(WORD).child(listId).child(wordId).setValue(null);
    }
    public void deleteWordClones(String wordId){
        userWord.child(WORD_CLONES).child(wordId).setValue(null);
    }
    public void deleteWordSingleClone(String wordId, String cloneKey){
        userWord.child(WORD_CLONES).child(wordId).child(cloneKey).setValue(null);
    }
    public Query getClonesQuery(String wordId, String cloneId){
        return userWord.child(WORD_CLONES).child(wordId).orderByChild("cloneId").equalTo(cloneId);
    }
    public void setLastList(String listId){
        userWord.child(LAST_LIST).setValue(listId);
    }
    public DatabaseReference lastListRef(){
        return userWord.child(LAST_LIST);
    }
    public void setLastWordSet(String wsId){
        userWord.child(LAST_SET).setValue(wsId);
    }
    public DatabaseReference lastWordSetRef(){
        return userWord.child(LAST_SET);
    }
    public void deleteWordSet(String wsId){
        userWord.child(WORD_SET).child(wsId).setValue(null);
    }
    public void deleteWordList(String wsId, String listId, boolean isMainList){
        if(!isMainList) userWord.child(WORD_LIST).child(wsId).child(listId).setValue(null);
        else userWord.child(WORD_LIST).child(wsId).setValue(null);
    }
    public DatabaseReference wordListWordsRef(String listId){
        return userWord.child(WORD).child(listId);
    }
    public DatabaseReference wordDataRef(String wordId){
        return userWord.child(WORD_DATA).child(wordId);
    }
    public DatabaseReference wordDefinitionRef(String wordId){
        return userWord.child(WORD_DEF).child(wordId);
    }
    public DatabaseReference wordSentenceRef(String wordId){
        return userWord.child(SENTENCE).child(wordId);
    }
    public DatabaseReference wordImageRef(String wordId){
        return userWord.child(IMAGE).child(wordId);
    }
    public DatabaseReference wordSetListsRef(String wsId){
        return userWord.child(WORD_LIST).child(wsId);
    }
    public DatabaseReference listWordsRef(String listId){
        return userWord.child(WORD).child(listId);
    }
    public DatabaseReference userDataRef(){
        return FirebaseDatabase.getInstance().getReference().child(USER_DATA).child(uId);
    }
    public DatabaseReference wordSetRef(){
        return userWord.child(WORD_SET);
    }

    public void setWordPronunciation(String listId, String wordId, String link){
        userWord.child(WORD).child(listId).child(wordId).child("pronunciation").setValue(link);
    }
}
