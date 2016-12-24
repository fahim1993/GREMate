package com.example.fahim.gremate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.example.fahim.gremate.Adapters.WordSetAdapter;
import com.example.fahim.gremate.DataClasses.UserData;
import com.example.fahim.gremate.DataClasses.WordSet;
import com.example.fahim.gremate.DataClasses.WordSetGet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LearnActivity extends NavDrawerActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference ref;

    private UserData user;
    private ArrayList<WordSetGet> wordSets;

    private String uid;

    private RecyclerView wsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        Button b = (Button) findViewById(R.id.addWordset);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user==null)return;
                WordSet s = new WordSet("MY WORDSET", user.getUserName() ,0,0);
                ref.push().setValue(s);
                getWordSets(true);

            }
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        uid = auth.getCurrentUser().getUid();

        ref = db.getReference("UserWord/"+uid+"/WordSet");

        db.getReference("UserData/"+uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(UserData.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setWordSet();

    }

    private void setWordSet(){

        wsRecyclerView = (RecyclerView)findViewById(R.id.rvWordSet);
        getWordSets(false);

        wsRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);

        wsRecyclerView.setLayoutManager(llm);
        wsRecyclerView.setAdapter(new WordSetAdapter(wordSets));

    }

    private void getWordSets(final boolean notify){
        wordSets = new ArrayList<>();
        db.getReference("UserWord/"+uid+"/WordSet/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    WordSet w = ds.getValue(WordSet.class);
                    String id = ds.getKey();
                    WordSetGet wg = new WordSetGet(w, id);
                    wordSets.add(wg);
                }
                if(notify){
                    wsRecyclerView.setAdapter(new WordSetAdapter(wordSets));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
