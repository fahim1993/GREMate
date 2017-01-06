package com.example.fahim.gremate;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.Adapters.WordAdapter;
import com.example.fahim.gremate.DataClasses.DB;
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
    private String title;
    private String currentListId;
    private String currentListName;


    private static FirebaseAuth auth;
    private static FirebaseDatabase db;
    private static DatabaseReference ref;

    private static DatabaseReference wordsRef;

    private ArrayList<WordListwID> wordLists;
    private ArrayList<WordwID> wordwIDs;

    private ImageButton listOptions;
    private ImageButton changeList;
    private TextView listTitle;

    private  RecyclerView wordsInListRV;

    private Query rvQuery;
    private ValueEventListener rvQueryListener;

    private AppCompatImageButton addWord;
    private ImageButton deleteList;

    private WordAdapter rvAdapter;
    private ProgressBar loadWordRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_set);


        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            finish();
        }
        wordSetId = extras.getString("wordset_key");
        allListId = extras.getString("allList_key");
        title = extras.getString("wordset_title");

        setTitle(title.toUpperCase());

        changeList = (ImageButton)findViewById(R.id.changeList);
        changeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WordSetActivity.this);
                if(wordLists == null)return;
                builder.setTitle("Change list");
                final ArrayList<WordListwID> tempList = getOtherLists(false);
                CharSequence [] listNames = new CharSequence[tempList.size()];
                for(int i=0; i<tempList.size(); i++){
                    listNames[i] = tempList.get(i).getName();
                }
                builder.setItems(listNames, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
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
        LinearLayoutManager llm = new LinearLayoutManager(this);
        wordsInListRV.setLayoutManager(llm);

        loadWordRV = (ProgressBar)findViewById(R.id.loadWordRV);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        uid = auth.getCurrentUser().getUid();

        addWord = (AppCompatImageButton) findViewById(R.id.addWordBtn);
        addWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(WordSetActivity.this);
                builder.setTitle("ADD WORD");

                final EditText input = new EditText(WordSetActivity.this);

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Word");
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String word = input.getText().toString();
                        if(word.length()<1){
                            Toast.makeText(WordSetActivity.this,
                                    "Failed! Word must be at least 1 character long.", Toast.LENGTH_LONG).show();
                        }
                        else{
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

        deleteList = (ImageButton) findViewById(R.id.deleteBtn);
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

        getWordSet_list();
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
                            rvAdapter.otherLists = getOtherLists(true);
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

    public void getWordSet_list(){

        ref = db.getReference().child(DB.USER_WORD).child(uid).child(DB.WORDLIST);
        Query q = ref.orderByChild("wordSet").equalTo(wordSetId);

        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wordLists = new ArrayList<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    WordList wl = ds.getValue(WordList.class);
                    WordListwID wlid = new WordListwID(wl, ds.getKey());
                    wordLists.add(wlid);
                }
                if(currentListId == null){
                    listTitle.setText(wordLists.get(0).getName().toUpperCase());
                    currentListId = allListId;
                    currentListName = "All words";
                    setListWords();
                }
                if(rvAdapter != null)
                    rvAdapter.otherLists = getOtherLists(true);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setListWords(){

        wordsInListRV.setVisibility(View.GONE);
        loadWordRV.setVisibility(View.VISIBLE);

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
                Collections.reverse(wordwIDs);
                rvAdapter = new WordAdapter(wordwIDs, WordSetActivity.this, wordSetId, allListId, currentListId);
                rvAdapter.otherLists = getOtherLists(true);
                Log.d("WORDSET ACTIVITY", "ADAPTER CHANGED");
                wordsInListRV.setAdapter(rvAdapter);
                getWordSet_list();
                loadWordRV.setVisibility(View.GONE);
                wordsInListRV.setVisibility(View.VISIBLE);
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
}
