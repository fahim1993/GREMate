package com.example.fahim.gremate;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.FeedTestData;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordDef;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class PracticingActivity extends AppCompatActivity {

    private ArrayList<Word> words;
    private int index;
    private String def;
    private String[][] OD;

    private Query query1;
    private ValueEventListener listener1;

    private TextView[] ansTVs;
    private TextView questionTV;

    private int ansIndex;
    private int noQuestions;
    private int noCorrect;

    private LinearLayout wordLevelLL;
    private SeekBar levelSb;
    private TextView levelTv;
    private  int wordLevel;

    private Word word;

    private boolean thisJudged;

    private AppCompatButton nextButton;

    private ScrollView practicingSV;

    private ProgressBar practicingLoading;

    private TextView practicingScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practicing);

        practicingSV = (ScrollView) findViewById(R.id.practicingSV);
        practicingSV.setVisibility(View.GONE);

        practicingLoading = (ProgressBar) findViewById(R.id.practicingLoading);
        practicingLoading.setVisibility(View.VISIBLE);

        Bundle b = getIntent().getBundleExtra("bundle");
        words = b.getParcelableArrayList("words");

        setTitle("SCORE: 0");

        randomizeWords();
        index = 0;
        noQuestions = 0;
        noCorrect = 0;

        OD = new FeedTestData().getPracticeWords();

        ansTVs = new TextView[5];

        ansTVs[0] = (TextView) findViewById(R.id.ansTV1);
        ansTVs[1] = (TextView) findViewById(R.id.ansTV2);
        ansTVs[2] = (TextView) findViewById(R.id.ansTV3);
        ansTVs[3] = (TextView) findViewById(R.id.ansTV4);
        ansTVs[4] = (TextView) findViewById(R.id.ansTV5);

        questionTV = (TextView) findViewById(R.id.questionTv);

        loadWordDef(words.get(index).getCopyOf());

        nextButton = (AppCompatButton) findViewById(R.id.nextBtn);
        nextButton.setVisibility(View.GONE);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(wordLevel!=word.getLevel()){
                    DB.setWordLevel(word.getCopyOf(), wordLevel);
                    Log.d("wordlevel", word.getCopyOf() + " " + wordLevel + " " + word.getLevel());
                }
                index++;
                if(index==words.size()){
                    index = 0;
                    randomizeWords();
                }
                for(int i=0; i<5; i++)ansTVs[i].setTextColor(Color.parseColor("#000000"));

                nextButton.setVisibility(View.GONE);

                practicingSV.setVisibility(View.GONE);
                wordLevelLL.setVisibility(View.GONE);
                practicingLoading.setVisibility(View.VISIBLE);
                loadWordDef(words.get(index).getCopyOf());

            }
        });

        levelSb = (SeekBar) findViewById(R.id.diffSeekBar);
        levelTv = (TextView) findViewById(R.id.diff);
        levelTv.setText("Easy");
        levelTv.setTextColor(Color.parseColor("#006400"));

        wordLevelLL = (LinearLayout) findViewById(R.id.wordLevel);
        wordLevelLL.setVisibility(View.GONE);

        levelSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                wordLevel = i;
                switch(i){
                    case 0:
                        levelTv.setText("Easy");
                        levelTv.setTextColor(Color.parseColor("#006400"));
                        break;
                    case 1:
                        levelTv.setText("Medium");
                        levelTv.setTextColor(Color.parseColor("#00008B"));
                        break;
                    case 2:
                        levelTv.setText("Hard");
                        levelTv.setTextColor(Color.parseColor("#8B0000"));
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(wordLevel!=word.getLevel()){
            DB.setWordLevel(word.getCopyOf(), wordLevel);
            Log.d("wordlevel", word.getCopyOf() + " " + wordLevel + " " + word.getLevel());
        }
    }

    private void loadWordDef(final String id){
        query1 = FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(FirebaseAuth
                .getInstance().getCurrentUser().getUid()).child(DB.WORDDEF).orderByChild("word").equalTo(id);

        listener1 =  new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                query1.removeEventListener(listener1);
                ArrayList<String> defs = new ArrayList<String>();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    WordDef w = ds.getValue(WordDef.class);
                    defs.add(w.getDef());
                }
                Random rn = new Random();
                int modVal = defs.size();
                int i = Math.abs(rn.nextInt())%modVal;
                def = defs.get(i);

                setupQuestion();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        query1.addValueEventListener(listener1);

        FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(FirebaseAuth
                .getInstance().getCurrentUser().getUid()).child(DB.WORD).child(id).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        word = dataSnapshot.getValue(Word.class);
                        word.setCopyOf(id);
                        wordLevel = word.getLevel();
                        Log.d("wordlevelFB", " " + wordLevel + " " + word.getCopyOf());
                        levelSb.setProgress(wordLevel);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    private void setupQuestion(){

        Random rn = new Random();
        int qsType = 0;
        HashMap<String, Integer> mp = new HashMap<>();

//        if(qsType == 0){
            questionTV.setText("Meaning of the word "+words.get(index).getValue() + " is?" );
            ArrayList<String> otDefs = new ArrayList<>();
            mp.put(words.get(index).getValue().toLowerCase(), 1);
            while(mp.size()!=5){
                int ind = Math.abs(rn.nextInt())%1536;
                String w = OD[ind][0];
                if(mp.containsKey(w.toLowerCase()))continue;
                otDefs.add(OD[ind][1]);
                mp.put(w.toLowerCase(), 1);
            }
            ansIndex = Math.abs(rn.nextInt())%5;
            ansTVs[ansIndex].setText(fromHtml( "<b>"+(ansIndex+1)+".</b> " + def));

            int j = 0;
            for(int i=0; i<5; i++){
                if(i==ansIndex)continue;
                ansTVs[i].setText( fromHtml("<b>"+(i+1)+".</b> " + otDefs.get(j++)));
            }
//        }
//        else {
//            questionTV.setText("\""+def+"\"" + " - is the meaning of: ");
//            ArrayList<String> otWords = new ArrayList<>();
//            mp.put(words.get(index).getValue().toLowerCase(), 1);
//            while(mp.size()!=5){
//                int ind = Math.abs(rn.nextInt())%1536;
//                String w = OD[ind][0];
//                if(mp.containsKey(w.toLowerCase()))continue;
//                otWords.add(OD[ind][0]);
//                mp.put(w.toLowerCase(), 1);
//            }
//            ansIndex = Math.abs(rn.nextInt())%5;
//            ansTVs[ansIndex].setText( fromHtml( "<b>"+(ansIndex+1)+".</b> " + words.get(index).getValue()));
//
//            int j = 0;
//            for(int i=0; i<5; i++){
//                if(i==ansIndex)continue;
//                ansTVs[i].setText( fromHtml( "<b>"+(i+1)+".</b> " + otWords.get(j++)));
//            }
//        }
        practicingLoading.setVisibility(View.GONE);
        practicingSV.setVisibility(View.VISIBLE);

        thisJudged = false;

        noQuestions++;
    }

    private void randomizeWords(){
        ArrayList<Word> tmpWord = new ArrayList<>();
        Random rn = new Random();
        while(words.size()!=0){
            int ind = Math.abs(rn.nextInt())%words.size();
            tmpWord.add(words.get(ind));
            words.remove(ind);
        }
        words = tmpWord;
    }

    public void validateResult(View v){
        int ind = Integer.valueOf(v.getTag().toString());
        if(ind == ansIndex){
            ansTVs[ind].setTextColor(Color.parseColor("#007200"));
            if(!thisJudged){
                noCorrect++;
            }
//            Toast.makeText(PracticingActivity.this, "Correct!", Toast.LENGTH_SHORT).show();
        }
        else {
            ansTVs[ind].setTextColor(Color.parseColor("#720000"));
        }
        thisJudged = true;
        nextButton.setVisibility(View.VISIBLE);
        wordLevelLL.setVisibility(View.VISIBLE);
        setTitle("SCORE: "+noCorrect+"/"+noQuestions);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                new AlertDialog.Builder(PracticingActivity.this)
                        .setTitle("End practice?")
                        .setMessage("You correctly answered " +noCorrect+ " out of "+noQuestions+". Do you want to stop?")
                        .setPositiveButton("STOP", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(PracticingActivity.this)
                .setTitle("End practice?")
                .setMessage("You correctly answered " +noCorrect+ " out of "+noQuestions+". Do you want to stop?")
                .setPositiveButton("STOP", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).show();
    }
}
