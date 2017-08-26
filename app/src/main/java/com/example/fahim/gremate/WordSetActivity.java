package com.example.fahim.gremate;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.Adapters.WordAdapter;
import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.Friend;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordList;
import com.example.fahim.gremate.DataClasses.WordListwID;
import com.example.fahim.gremate.DataClasses.WordwID;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class WordSetActivity extends NavDrawerActivity {

    private String wordSetId;
    private String allListId;
    private String uid;
    private String uname;
    private String title;
    private String currentListId;
    private String currentListName;
    private String lastSavedList;


    private static FirebaseAuth auth;
    private static FirebaseDatabase db;
    private static DatabaseReference ref;

    private static DatabaseReference wordsRef;

    private ArrayList<WordListwID> wordLists;
    private ArrayList<WordwID> wordwIDs;
    private ArrayList<Friend> friends;

    private TextView listTitle;

    private RecyclerView wordsInListRV;
    private LinearLayoutManager llm;

    private Query rvQuery;
    private Query q;
    private DatabaseReference ref1;
    private ValueEventListener rvQueryListener;
    private ValueEventListener listener;
    private ValueEventListener listener1;

    private WordAdapter rvAdapter;
    private ProgressBar loadWordRV;

    private int sortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_set);

        setupNavDrawerClick();

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            finish();
        }
        wordSetId = extras.getString("wordset_key");
        allListId = extras.getString("allList_key");
        title = extras.getString("wordset_title");
        uname = extras.getString("user_name");

        setTitle(title.toUpperCase());

        AppCompatImageButton changeList = (AppCompatImageButton) findViewById(R.id.changeList);
        changeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setListState();
                AlertDialog.Builder builder = new AlertDialog.Builder(WordSetActivity.this);
                if(wordLists == null)return;
                builder.setTitle("Change list");
                final ArrayList<WordListwID> tempList = getOtherLists(false);
                if(tempList.size() == 0){
                    Toast.makeText(WordSetActivity.this,
                            "Please create a new list first!", Toast.LENGTH_LONG).show();
                    return;
                }
                CharSequence [] listNames = new CharSequence[tempList.size()];
                for(int i=0; i<tempList.size(); i++){
                    listNames[i] = tempList.get(i).getName();
                }
                builder.setItems(listNames, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        hideWordRv();
                        rvAdapter = null;
                        currentListId = tempList.get(i).getId();
                        currentListName = tempList.get(i).getName();
                        listTitle.setText(tempList.get(i).getName().toUpperCase());
                        setListWords();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        listTitle = (TextView)findViewById(R.id.listTitle);

        wordsInListRV = (RecyclerView) findViewById(R.id.wordInListRV);
        wordsInListRV.setHasFixedSize(true);
        llm = new LinearLayoutManager(this);
        wordsInListRV.setLayoutManager(llm);

        loadWordRV = (ProgressBar)findViewById(R.id.loadWordRV);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        uid = auth.getCurrentUser().getUid();

        AppCompatImageButton addWord = (AppCompatImageButton) findViewById(R.id.addWordBtn);
        addWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(WordSetActivity.this);
                builder.setTitle("Add Word");

                final EditText input = new EditText(WordSetActivity.this);

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Word");
                builder.setView(input);

                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String word = input.getText().toString();
                        if(word.length()<1){
                            Toast.makeText(WordSetActivity.this,
                                    "Failed! Word must be at least 1 character long.", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(WordSetActivity.this,
                                    word + " added", Toast.LENGTH_SHORT).show();
                            DB.newWord(word, currentListId, currentListName, wordSetId, allListId);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        AppCompatImageButton deleteList = (AppCompatImageButton) findViewById(R.id.deleteBtn);
        deleteList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(WordSetActivity.this)
                        .setTitle("Confirm Delete")
                        .setMessage( (currentListId.equals(allListId))? "Are you sure you want to " +
                                "delete this list? The word set will also be deleted." : "Are you sure you want to delete this list?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(rvQueryListener!= null){
                                    rvQuery.removeEventListener(rvQueryListener);
                                }
                                Toast.makeText(context,
                                        "List " + currentListName + " deleted", Toast.LENGTH_LONG).show();
                                if(currentListId.equals(allListId)){
                                    DB.deleteList(currentListId, wordSetId, true);
                                    Intent intent = new Intent(WordSetActivity.this, LearnActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    WordSetActivity.this.finish();
                                }
                                else{
                                    DB.deleteList(currentListId, wordSetId, false);
                                    currentListId = allListId;
                                    currentListName = "All words";
                                    listTitle.setText(currentListName);
                                    setListWords();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).show();
            }
        });

        AppCompatImageButton sortBtn = (AppCompatImageButton) findViewById(R.id.sortBtn);
        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(WordSetActivity.this);
                LayoutInflater inflater = (WordSetActivity.this).getLayoutInflater();

                builder.setTitle("Sort Words");
                final View layout = inflater.inflate(R.layout.ws_sort, null);
                final RadioButton recAdd = (RadioButton) layout.findViewById(R.id.recAdded);
                final RadioButton recVwd = (RadioButton) layout.findViewById(R.id.recViewed);
                final RadioButton alph = (RadioButton) layout.findViewById(R.id.alphabetical);
                final RadioButton diff = (RadioButton) layout.findViewById(R.id.difficulty);

                final RadioButton asc = (RadioButton) layout.findViewById(R.id.ascending);
                final RadioButton dsc = (RadioButton) layout.findViewById(R.id.descending);

                if(sortOrder == 11){recAdd.setChecked(true); asc.setChecked(true);}
                else if (sortOrder == 12) {recAdd.setChecked(true); dsc.setChecked(true);}
                else if (sortOrder == 21) {recVwd.setChecked(true); asc.setChecked(true);}
                else if (sortOrder == 22) {recVwd.setChecked(true); dsc.setChecked(true);}
                else if (sortOrder == 31) {alph.setChecked(true); asc.setChecked(true);}
                else if (sortOrder == 32) {alph.setChecked(true); dsc.setChecked(true);}
                else if (sortOrder == 41) {diff.setChecked(true); asc.setChecked(true);}
                else if (sortOrder == 42) {diff.setChecked(true); dsc.setChecked(true);}

                final int prevSortOrder = sortOrder;

                builder.setView(layout)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if(recAdd.isChecked() && asc.isChecked()){sortOrder = 11;}
                                else if (recAdd.isChecked() && dsc.isChecked()) {sortOrder = 12;}
                                else if (recVwd.isChecked() && asc.isChecked()) {sortOrder = 21;}
                                else if (recVwd.isChecked() && dsc.isChecked()) {sortOrder = 22;}
                                else if (alph.isChecked() && asc.isChecked()) {sortOrder = 31;}
                                else if (alph.isChecked() && dsc.isChecked()) {sortOrder = 32;}
                                else if (diff.isChecked() && asc.isChecked()) {sortOrder = 41;}
                                else if (diff.isChecked() && dsc.isChecked()) {sortOrder = 42;}
                                if(prevSortOrder == sortOrder)return;
                                setListSortOrder();
                                sortWords(true);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.create();
                builder.show();
            }
        });

        AppCompatImageButton searchButton = (AppCompatImageButton)findViewById(R.id.searchBtn);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(wordwIDs == null)return;
                AlertDialog.Builder builder = new AlertDialog.Builder(WordSetActivity.this);
                builder.setTitle("Search Word");

                final EditText input = new EditText(WordSetActivity.this);

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Word");
                builder.setView(input);

                builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        char[] word = input.getText().toString().toLowerCase().toCharArray();
                        for(int i=0; i<wordwIDs.size(); i++){
                            char[] listWord = wordwIDs.get(i).getValue().toLowerCase().toCharArray();
                            if(listWord.length < word.length) continue;
                            boolean match = true;
                            for(int j =0; j<word.length; j++){
                                if(word[j]!=listWord[j]){
                                    match = false;
                                    break;
                                }
                            }
                            if(match == true){
                                llm.scrollToPositionWithOffset(i, 0);
                                return;
                            }
                        }
                        Toast.makeText(WordSetActivity.this,
                                input.getText().toString() + " was not found in this list!", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        AppCompatImageButton practiceButton = (AppCompatImageButton) findViewById(R.id.practiceBtn);
        practiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WordSetActivity.this, PracticeActivity.class);
                Bundle b = new Bundle();
                b.putString("ws_id", wordSetId);
                b.putString("list_id", currentListId);
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        setFriends();
        hideWordRv();
        getWordSetList();
        restoreList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setListState();
        setList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeListeners();
    }

    private void removeListeners(){
        if(listener != null) q.removeEventListener(listener);
        if(rvQueryListener != null) rvQuery.removeEventListener(rvQueryListener);
        if(listener1!=null)ref1.removeEventListener(listener1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wordset_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addList:
                AlertDialog.Builder builder = new AlertDialog.Builder(WordSetActivity.this);
                builder.setTitle("Add list");

                final EditText input = new EditText(WordSetActivity.this);
                input.setHint("List name");
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String listname = input.getText().toString();
                        if(listname.length()<2){
                            Toast.makeText(WordSetActivity.this,
                                    "Failed! Name must be at least 2 characters long.", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(WordSetActivity.this,
                                    "List "+listname+" created", Toast.LENGTH_LONG).show();
                            DB.newList(listname, wordSetId);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void getWordSetList(){

        ref = db.getReference().child(DB.USER_WORD).child(uid).child(DB.WORDLIST);
        q = ref.orderByChild("wordSet").equalTo(wordSetId);
        final String[] tempName = new String[1];

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wordLists = new ArrayList<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    WordList wl = ds.getValue(WordList.class);
                    WordListwID wlid = new WordListwID(wl, ds.getKey());
                    wordLists.add(wlid);
                    if(lastSavedList!=null && lastSavedList.equals(wlid.getId())) tempName[0] = wlid.getName();
                }
                if(currentListId == null){
                    if(lastSavedList != null){
                        listTitle.setText(tempName[0]);
                        currentListName = tempName[0];
                        currentListId = lastSavedList;
                        setListWords();
                    }
                    else {
                        listTitle.setText(wordLists.get(0).getName().toUpperCase());
                        currentListId = wordLists.get(0).getId();
                        currentListName = wordLists.get(0).getName();
                        setListWords();
                    }
                }
                if(rvAdapter!=null) rvAdapter.setOtherLists(getOtherLists(true));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        q.addValueEventListener(listener);
    }

    public void setListWords(){

        if(rvQueryListener!= null){
            rvQuery.removeEventListener(rvQueryListener);
        }
        rvQuery = db.getReference(DB.USER_WORD).child(uid).child(DB.WORD).orderByChild("listId")
                .equalTo(currentListId);
        rvQueryListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wordwIDs = new ArrayList<WordwID>();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Word word = ds.getValue(Word.class);
                    WordwID wordwID = new WordwID(word, ds.getKey());
                    wordwIDs.add(wordwID);
                }
                if(title.length() >= 8 && title.substring(0,8).equals("GRE: Set")){
                    if(wordLists.size()==1){
                        int j = 0;
                        for(int i=0; i<4; i++){
                            String listKey = DB.newList("List "+(i+1), wordSetId);
                            int lim = (wordwIDs.size() * (i+1)) / 4 ;
                            for( ; j<lim; j++) {
                                Word nword = wordwIDs.get(j).toWord();
                                nword.setCopyOf(wordwIDs.get(j).getId());
                                nword.setListId(listKey);
                                DB.addWordToAnotherList(nword);
                            }
                        }
                    }
                }
                sortWords(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        rvQuery.addValueEventListener(rvQueryListener);
    }

    private ArrayList<WordListwID> getOtherLists(boolean removeListAll){
        ArrayList<WordListwID> tempList = new ArrayList<>();
        for(int i=0; i<wordLists.size(); i++){
            if(removeListAll && wordLists.get(i).getId().equals(allListId)) continue;
            if(wordLists.get(i).getId().equals(currentListId))continue;
            tempList.add(wordLists.get(i));
        }
        return  tempList;
    }

    private void sortWords(boolean resetList){

        if (wordwIDs == null) return;
        getListSortOrder();
        if(sortOrder == 11){Collections.sort(wordwIDs, WordwID.recAdded_Asc);}
        else if (sortOrder == 12) {Collections.sort(wordwIDs, WordwID.recAdded_Dsc);}
        else if (sortOrder == 21) {Collections.sort(wordwIDs, WordwID.recViewed_Asc);}
        else if (sortOrder == 22) {Collections.sort(wordwIDs, WordwID.recViewed_Dsc);}
        else if (sortOrder == 31) {Collections.sort(wordwIDs, WordwID.alphabetical_Asc);}
        else if (sortOrder == 32) {Collections.sort(wordwIDs, WordwID.alphabetical_Dsc);}
        else if (sortOrder == 41) {Collections.sort(wordwIDs, WordwID.difficulty_Asc);}
        else if (sortOrder == 42) {Collections.sort(wordwIDs, WordwID.difficulty_Dsc);}

        if(resetList) resetListState();
        else if(rvAdapter != null)setListState();
        rvAdapter = new WordAdapter(wordwIDs, friends, WordSetActivity.this, wordSetId, allListId, currentListId, userName);
        wordsInListRV.setAdapter(rvAdapter);
        restoreListState();
        rvAdapter.setOtherLists(getOtherLists(true));

        showWordRv();
    }

    private void setListSortOrder(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putInt(currentListId+"`~", sortOrder).apply();
    }

    private void getListSortOrder(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        sortOrder = prefs.getInt(currentListId+"`~", 11);
    }

    private void resetListState(){
        if(wordsInListRV == null)return;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        preferences.edit().putInt(currentListId, 0).apply();
        preferences.edit().putInt(currentListId+"`~", 0).apply();
    }

    private void setListState(){
        if(wordsInListRV == null)return;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        View firstChild = wordsInListRV.getChildAt(0);
        if(firstChild == null)return;
        int firstVisiblePosition = wordsInListRV.getChildAdapterPosition(firstChild);
        int offset = firstChild.getTop();

        preferences.edit().putInt(currentListId, firstVisiblePosition).apply();
        preferences.edit().putInt(currentListId+"~~", offset).apply();
        setListSortOrder();

    }

    private void restoreListState(){
        if(wordsInListRV == null)return;
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("RESTORE LIST STATE", currentListId + preferences.getInt(currentListId, 0));
        wordsInListRV.scrollToPosition(preferences.getInt(currentListId, 0));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                wordsInListRV.scrollBy(0, - preferences.getInt(currentListId+"~~", 0));
            }
        }, 5);
    }

    private void hideWordRv(){
        wordsInListRV.setVisibility(View.GONE);
        loadWordRV.setVisibility(View.VISIBLE);
    }

    private void showWordRv(){
        loadWordRV.setVisibility(View.GONE);
        wordsInListRV.setVisibility(View.VISIBLE);
    }

    private void setList(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //Log.d("WORDSETACTIVITY >> ", "Postition: " + firstVisiblePosition);
        preferences.edit().putString(wordSetId, currentListId).apply();
    }

    private void restoreList(){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        lastSavedList = preferences.getString(wordSetId, null);
    }

    private void setupNavDrawerClick(){
        NavigationView mNavigationView = (NavigationView)findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Intent intent;
                switch(menuItem.getItemId()){
                    case R.id.nav_learn:
                        intent = new Intent(WordSetActivity.this, LearnActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_signout:
                        new AlertDialog.Builder(WordSetActivity.this)
                                .setTitle("Confirm Sign Out")
                                .setMessage( "Are you sure you want to sign out?")
                                .setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(WordSetActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                }).show();
                        break;
                    case R.id.nav_search:
                        intent = new Intent(WordSetActivity.this, SearchActivity.class);
                        WordSetActivity.this.startActivity(intent);
                        break;
                    case R.id.nav_exercise:
                        intent = new Intent(WordSetActivity.this, PracticeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_friend:
                        intent = new Intent(WordSetActivity.this, FriendActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
    }

    private void setFriends(){

        ref1 = FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid)
                .child(DB.FRIEND);

        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friends = new ArrayList<>();
                ArrayList<String> keys = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Friend f = ds.getValue(Friend.class);
                    friends.add(f);
                    keys.add(ds.getKey());
                    Log.d(f.getId(), f.getName());
                }
                if(rvAdapter!=null)rvAdapter.setFriends(friends);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {           }
        };
        ref1.addValueEventListener(listener1);
    }

}
