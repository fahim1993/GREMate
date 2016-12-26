package com.example.fahim.gremate;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.WordList;
import com.example.fahim.gremate.DataClasses.WordListwID;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class WordSetActivity extends NavDrawerActivity {

    String wordSetID;
    String userid;

    private static FirebaseAuth auth;
    private static FirebaseDatabase db;
    private static DatabaseReference ref;

    private ArrayList<WordListwID> wordLists;

    private Button changeList;
    private Button addWord;
    private TextView listTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_set);

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            finish();
        }
        wordSetID = extras.getString("wordset_key");

        getWordList_list(wordSetID);

        listTitle = (TextView)findViewById(R.id.listTitle);

        changeList = (Button)findViewById(R.id.changeList);
        changeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WordSetActivity.this);
                if(wordLists == null)return;
                builder.setTitle("Select a list");
                CharSequence [] listNames = new CharSequence[wordLists.size()];
                for(int i=0; i<wordLists.size(); i++){
                    listNames[i] = wordLists.get(i).getName();
                }
                builder.setItems(listNames, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listTitle.setText("List: "+wordLists.get(i).getName());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        addWord = (Button) findViewById(R.id.addWord);
        addWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public  void getWordList_list (final String wsid){
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        userid = auth.getCurrentUser().getUid();
        ref = db.getReference().child(DB.USER_WORD).child(userid).child(DB.WORDLIST);
        Query q = ref.orderByChild("wordSet").equalTo(wsid);

        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wordLists = new ArrayList<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    WordList wl = ds.getValue(WordList.class);
                    WordListwID wlid = new WordListwID(wl, ds.getKey());
                    wordLists.add(wlid);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
