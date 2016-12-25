package com.example.fahim.gremate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class WordSetActivity extends AppCompatActivity {

    String wordSetID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_set);

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            finish();
        }
        wordSetID = extras.getString("wordset_key");
        Log.d(wordSetID, wordSetID);
    }
}
