package com.example.fahim.gremate;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class LearnActivity extends NavDrawerActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase FBDB;
    private DatabaseReference UDATA;
    private DatabaseReference UWORD;

    private UserData user;
    private ArrayList<WordSetwID> wordSets;

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
            }
        });

        auth = FirebaseAuth.getInstance();
        FBDB = FirebaseDatabase.getInstance();

        UDATA = FBDB.getReference(DB.USER_DATA);
        UWORD = FBDB.getReference(DB.USER_WORD);

        uid = auth.getCurrentUser().getUid();

        UDATA.child(uid).addValueEventListener(new ValueEventListener() {
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

        wsRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);

        wsRecyclerView.setLayoutManager(llm);
        UWORD.child(uid).child(DB.WORDSET).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wordSets = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    WordSet w = ds.getValue(WordSet.class);
                    String id = ds.getKey();
                    WordSetwID wg = new WordSetwID(w, id);
                    wordSets.add(wg);
                }
                wsRecyclerView.setAdapter(new WordSetAdapter(wordSets, LearnActivity.this));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
