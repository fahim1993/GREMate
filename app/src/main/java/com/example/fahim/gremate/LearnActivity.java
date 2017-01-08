package com.example.fahim.gremate;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.Adapters.WordSetAdapter;
import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.UserData;
import com.example.fahim.gremate.DataClasses.WordList;
import com.example.fahim.gremate.DataClasses.WordSet;
import com.example.fahim.gremate.DataClasses.WordSetwID;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class LearnActivity extends NavDrawerActivity {

    private ArrayList<WordSetwID> wordSets;

    private String uid;

    private RecyclerView wsRecyclerView;

    private ProgressBar loadWordSet;

    private TextView wordSetTitle;

    DatabaseReference ref1;
    ValueEventListener listener1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        setupNavDrawerClick();

        wordSetTitle = (TextView) findViewById(R.id.wordSetTitle);

        setWsTitle();
        setTitle("LEARN");

        wsRecyclerView = (RecyclerView)findViewById(R.id.rvWordSet);
        wsRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        wsRecyclerView.setLayoutManager(llm);

        loadWordSet = (ProgressBar) findViewById(R.id.loadWordSetRV);

    }

    @Override
    protected void onResume() {
        super.onResume();

        wsRecyclerView.setVisibility(View.GONE);
        loadWordSet.setVisibility(View.VISIBLE);

        setWordSet();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(listener1!=null){
            ref1.removeEventListener(listener1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.learn_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addSet:
                AlertDialog.Builder builder = new AlertDialog.Builder(LearnActivity.this);
                builder.setTitle("WORDSET NAME");

                final EditText input = new EditText(LearnActivity.this);

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String wsname = input.getText().toString();
                        if(wsname.length()<3){
                            Toast.makeText(LearnActivity.this, "Failed! Name must be at least 3 characters long.", Toast.LENGTH_LONG).show();
                        }
                        else{
                            DB.newWordSet(wsname);
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

    private void setWsTitle(){
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference().child(DB.USER_DATA).child(uid);
        mref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserData user = dataSnapshot.getValue(UserData.class);
                wordSetTitle.setText(user.getUserName() + "'s Word Set");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setWordSet(){
        if(listener1 != null){
            ref1.removeEventListener(listener1);
        }

        ref1 = FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid).child(DB.WORDSET);
        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wordSets = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    WordSet w = ds.getValue(WordSet.class);
                    String id = ds.getKey();
                    WordSetwID wg = new WordSetwID(w, id);
                    wordSets.add(wg);
                }
                Collections.reverse(wordSets);
                wsRecyclerView.setAdapter(new WordSetAdapter(wordSets, LearnActivity.this));
                wsRecyclerView.setVisibility(View.VISIBLE);
                loadWordSet.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref1.addValueEventListener(listener1);
    }

    private void setupNavDrawerClick(){
        NavigationView mNavigationView = (NavigationView)findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.nav_learn:
                        Intent intent = new Intent(LearnActivity.this, LearnActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_signout:
                        new AlertDialog.Builder(LearnActivity.this)
                                .setTitle("Confirm Sign Out")
                                .setMessage( "Are you sure you want to sign out?")
                                .setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(LearnActivity.this, LoginActivity.class);
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
                }
                return true;
            }
        });
    }
}
