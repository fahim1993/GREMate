package com.example.fahim.gremate;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.Adapters.WordSetAdapter;
import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordList;
import com.example.fahim.gremate.DataClasses.WordListwID;
import com.example.fahim.gremate.DataClasses.WordSet;
import com.example.fahim.gremate.DataClasses.WordSetwID;
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

import static android.view.View.GONE;

public class PracticeActivity extends NavDrawerActivity {

    private String wsId;
    private String listId;

    private String uid;

    private Spinner wsSpinner;
    private Spinner listSpinner;

    private ProgressBar wsPB;
    private ProgressBar listPB;

    private DatabaseReference ref1;
    private DatabaseReference ref2;
    private Query query3;

    private ValueEventListener listener1;
    private ValueEventListener listener2;
    private ValueEventListener listener3;

    private ArrayList<WordSetwID> wordSets;
    private ArrayList<String> wordSetNames;

    private ArrayList<WordListwID> wordLists;
    private ArrayList<String> wordListNames;
    private ArrayList<Integer> wordListInd;
    private ArrayList<WordwID> words;

    private AppCompatButton loadButton;
    private AppCompatButton startPracBtn;

    private ProgressBar loadPracPB;

    private LinearLayout ll3;

    private boolean clickLoadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        setupNavDrawerClick();

        setTitle("Practice");

        Bundle b = getIntent().getExtras();

        clickLoadButton = false;

        if(b  != null){
            wsId = b.getString("ws_id");
            listId = b.getString("list_id");

            clickLoadButton = true;
        }

        ll3 = (LinearLayout)findViewById(R.id.ll3);
        ll3.setVisibility(GONE);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        wsSpinner = (Spinner) findViewById(R.id.wsSpinner);
        wsSpinner.setVisibility(GONE);
        listSpinner = (Spinner) findViewById(R.id.listSpinner);
        listSpinner.setVisibility(GONE);

        wsPB = (ProgressBar) findViewById(R.id.wsLoading);
        listPB = (ProgressBar) findViewById(R.id.listLoading);

        loadButton = (AppCompatButton) findViewById(R.id.loadPracBtn);
        loadButton.setVisibility(GONE);
        loadPracPB =(ProgressBar)findViewById(R.id.pracLoading);
        loadPracPB.setVisibility(GONE);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wsSpinner.setEnabled(false);
                listSpinner.setEnabled(false);
                loadPracPB.setVisibility(View.VISIBLE);
                ll3.setVisibility(GONE);
                getWords();
            }
        });

        startPracBtn = (AppCompatButton)findViewById(R.id.startPracBtn);
        startPracBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PracticeActivity.this, PracticingActivity.class);

                ArrayList<Word> words_ = new ArrayList<>();
                for (int i=0; i<words.size(); i++) {
                    Word w = words.get(i).toWord();
                    if(w.getCopyOf().length()<1)w.setCopyOf(words.get(i).getId());
                    words_.add(w);
                }
                Bundle b = new Bundle();
                b.putParcelableArrayList("words", words_);
                intent.putExtra("bundle", b);
                startActivity(intent);
            }
        });

        wsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(wordLists == null)return;
                wsId = wordSets.get(i).getId();
                wordListNames = new ArrayList<>();
                wordListInd = new ArrayList<>();
                int j = 0;
                for(WordListwID lid : wordLists){
                    if(lid.getWordSet().equals(wsId)){
                        wordListNames.add(lid.getName());
                        wordListInd.add(j);
                    }
                    j++;
                }
                ArrayAdapter<String> listArrayAdapter = new ArrayAdapter<>(
                        PracticeActivity.this, R.layout.spinner_item, wordListNames);
                listArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
                listSpinner.setAdapter(listArrayAdapter);

                listId = wordLists.get(wordListInd.get(0)).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        listSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                listId =  wordLists.get(wordListInd.get(i)).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        getWordSet();
    }

    private void getWordSet(){
        if(listener1 != null){
            ref1.removeEventListener(listener1);
        }
        ref1 = FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid).child(DB.WORDSET);
        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wordSets = new ArrayList<>();
                wordSetNames = new ArrayList<>();
                int i=0,wsIndex=-1;
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    WordSet w = ds.getValue(WordSet.class);
                    String id = ds.getKey();
                    WordSetwID wg = new WordSetwID(w, id);
                    wordSetNames.add(wg.getName());
                    wordSets.add(wg);
                    if(wsId!= null && wg.getId().equals(wsId))wsIndex = i;
                    i++;
                }
                Collections.reverse(wordSets);
                Collections.reverse(wordSetNames);

                ArrayAdapter<String> wsArrayAdapter = new ArrayAdapter<>(
                        PracticeActivity.this, R.layout.spinner_item, wordSetNames);
                wsArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
                wsSpinner.setAdapter(wsArrayAdapter);
                if(wsIndex!=-1)wsSpinner.setSelection(wordSets.size() - 1 - wsIndex, false);
                ref1.removeEventListener(listener1);
                getWordList();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        ref1.addValueEventListener(listener1);
    }

    private void getWordList(){
        if(listener2 != null){
            ref2.removeEventListener(listener2);
        }
        ref2 = FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid).child(DB.WORDLIST);
        listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(wsId == null) wsId = wordSets.get(0).getId();
                wordLists = new ArrayList<>();
                wordListNames = new ArrayList<>();
                wordListInd = new ArrayList<>();
                int i = 0, lsIndex=-1;
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    WordList wl = ds.getValue(WordList.class);
                    String id = ds.getKey();
                    WordListwID lid = new WordListwID(wl, id);
                    if(lid.getWordSet().equals(wsId)){
                        wordListNames.add(lid.getName());
                        wordListInd.add(i);
                        if(listId != null && lid.getId().equals(listId))lsIndex=wordListNames.size()-1;
                    }
                    i++;
                    wordLists.add(lid);
                }
                ref2.removeEventListener(listener2);
                ArrayAdapter<String> listArrayAdapter = new ArrayAdapter<>(
                        PracticeActivity.this, R.layout.spinner_item, wordListNames);
                listArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
                listSpinner.setAdapter(listArrayAdapter);


                if(listId!=null) listSpinner.setSelection(lsIndex, false);
                else listId = wordLists.get(wordListInd.get(0)).getId();

                listPB.setVisibility(View.GONE);
                wsPB.setVisibility(View.GONE);

                wsSpinner.setVisibility(View.VISIBLE);
                listSpinner.setVisibility(View.VISIBLE);

                loadButton.setVisibility(View.VISIBLE);
                if(clickLoadButton)loadButton.performClick();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        ref2.addValueEventListener(listener2);
    }

    private void getWords(){

        if(listId == null){
            Toast.makeText(PracticeActivity.this, "No list selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if(query3!= null){
            query3.removeEventListener(listener3);
        }
        query3 = FirebaseDatabase.getInstance().getReference(DB.USER_WORD).child(uid).child(DB.WORD).orderByChild("listId")
                .equalTo(listId);
        listener3 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                words = new ArrayList<WordwID>();
                String s = "";
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Word word = ds.getValue(Word.class);
                    WordwID wordwID = new WordwID(word, ds.getKey());
                    if(wordwID.isPracticable()){
                        if(wordwID.getCopyOf().length()<1)wordwID.setCopyOf(wordwID.getId());
                        words.add(wordwID);
                        if(s.length()<1)s+=wordwID.getValue();
                        else s+= ", " + wordwID.getValue();
                    }
                }
                wsSpinner.setEnabled(true);
                listSpinner.setEnabled(true);

                ll3.setVisibility(View.VISIBLE);
                loadPracPB.setVisibility(GONE);
                ((TextView)findViewById(R.id.practicableWordsTV)).setText(s);
                query3.removeEventListener(listener3);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        query3.addValueEventListener(listener3);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }



    public void practicableDialog(View v){

    }

    private void setupNavDrawerClick(){
        NavigationView mNavigationView = (NavigationView)findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Intent intent;
                switch(menuItem.getItemId()){
                    case R.id.nav_learn:
                        intent = new Intent(PracticeActivity.this, LearnActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_signout:
                        new AlertDialog.Builder(PracticeActivity.this)
                                .setTitle("Confirm Sign Out")
                                .setMessage( "Are you sure you want to sign out?")
                                .setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(PracticeActivity.this, LoginActivity.class);
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
                        intent = new Intent(PracticeActivity.this, SearchActivity.class);
                        PracticeActivity.this.startActivity(intent);
                        break;
                    case R.id.nav_exercise:
                        intent = new Intent(PracticeActivity.this, PracticeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_friend:
                        intent = new Intent(PracticeActivity.this, FriendActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mDrawerLayout.isDrawerOpen(Gravity.LEFT))
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            else {
                new AlertDialog.Builder(PracticeActivity.this)
                        .setTitle("Close")
                        .setMessage("Are you sure you want to close GREMate?")
                        .setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finishAffinity();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).show();
            }
        }
        return true;
    }


}
