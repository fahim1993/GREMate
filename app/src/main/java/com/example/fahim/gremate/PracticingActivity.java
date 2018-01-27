package com.example.fahim.gremate;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.DBRef;
import com.example.fahim.gremate.DataClasses.FeedTestData;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordPractice;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static android.view.View.GONE;

public class PracticingActivity extends AppCompatActivity {

    private ArrayList<Word> words;
    private int index;
    private String ans;
    private String[][] OD;

    private TextView[] ansTVs;
    private TextView questionTV;

    private int ansIndex;
    private int noQuestions;
    private int noCorrect;
    private int divider;

    private LinearLayout wordLevelLL;
    private SeekBar levelSb;
    private TextView levelTv;
    private int wordLevel;

    private Word word;

    private boolean thisJudged;

    private AppCompatButton nextButton;

    private ScrollView practicingSV;

    private ProgressBar practicingLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practicing);

        practicingSV = (ScrollView) findViewById(R.id.practicingSV);
        practicingSV.setVisibility(GONE);

        practicingLoading = (ProgressBar) findViewById(R.id.practicingLoading);
        practicingLoading.setVisibility(View.VISIBLE);

        Bundle b = getIntent().getExtras();
        words = b.getParcelableArrayList("words");

        ArrayList<Word> temp = new ArrayList<>();
        for(int i = 0; i<words.size(); i++){
            for(int j=0; j<=words.get(i).getLevel(); j++){
                temp.add(words.get(i));
            }
        }
        words = temp;

        setTitle("SCORE: 0");

        randomizeWords();
        index = 0;
        noQuestions = 0;
        noCorrect = 0;

        divider = 2;

        OD = new FeedTestData().getPracticeWords();

        ansTVs = new TextView[5];

        ansTVs[0] = (TextView) findViewById(R.id.ansTV1);
        ansTVs[1] = (TextView) findViewById(R.id.ansTV2);
        ansTVs[2] = (TextView) findViewById(R.id.ansTV3);
        ansTVs[3] = (TextView) findViewById(R.id.ansTV4);
        ansTVs[4] = (TextView) findViewById(R.id.ansTV5);

        questionTV = (TextView) findViewById(R.id.questionTv);

        nextButton = (AppCompatButton) findViewById(R.id.nextBtn);
        nextButton.setVisibility(GONE);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (wordLevel != word.getLevel()) {
                    DB.setWordLevel(word.getCloneOf(), wordLevel);
                }
                index++;
                if (index == words.size()) {
                    index = 0;
                    randomizeWords();
                }
                for (int i = 0; i < 5; i++) ansTVs[i].setTextColor(Color.parseColor("#000000"));

                nextButton.setVisibility(GONE);

                practicingSV.setVisibility(GONE);
                wordLevelLL.setVisibility(GONE);
                practicingLoading.setVisibility(View.VISIBLE);
                loadWordPracticeData(words.get(index).getCloneOf());

            }
        });

        levelSb = (SeekBar) findViewById(R.id.diffSeekBar);
        levelTv = (TextView) findViewById(R.id.diff);
        levelTv.setText("Easy");
        levelTv.setTextColor(Color.parseColor("#00B200"));

        wordLevelLL = (LinearLayout) findViewById(R.id.wordLevel);
        wordLevelLL.setVisibility(GONE);

        levelSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                wordLevel = i;
                switch (i) {
                    case Word.LVL_EASY:
                        levelTv.setText("Easy");
                        levelTv.setTextColor(Color.parseColor("#00B200"));
                        break;
                    case Word.LVL_NORMAL:
                        levelTv.setText("Normal");
                        levelTv.setTextColor(Color.parseColor("#005999"));
                        break;
                    case Word.LVL_HARD:
                        levelTv.setText("Hard");
                        levelTv.setTextColor(Color.parseColor("#F07F00"));
                        break;
                    case Word.LVL_VHARD:
                        levelTv.setText("Very Hard");
                        levelTv.setTextColor(Color.parseColor("#990019"));
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

        loadWordPracticeData(words.get(index).getCloneOf());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wordLevel != word.getLevel()) {
            DB.setWordLevel(word.getCloneOf(), wordLevel);
        }
    }

    private void loadWordPracticeData(final String id) {
        DBRef db = new DBRef();

        word = words.get(index);
        wordLevel = word.getLevel();
        wordLevelLL.setVisibility(GONE);
        levelSb.setProgress(wordLevel);

        final DatabaseReference ref = db.wordPracticeRef(id);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                WordPractice p = dataSnapshot.getValue(WordPractice.class);
                setupQuestion(p);
                ref.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setupQuestion(WordPractice practiceData) {

        Random rn = new Random();
        HashMap<String, Integer> mp = new HashMap<>();

        int type = getType();

        if(type == 0){
            if(practiceData.hasSynonyms()){
                ans = practiceData.getRandomSynonym();
            }
            else {
                type = 1;
                ans = practiceData.getRandomDefinition();
            }
        }
        else{
            if(practiceData.hasDefinitions()){
                ans = practiceData.getRandomDefinition();
            }
            else {
                type = 0;
                ans = practiceData.getRandomSynonym();
            }
        }

        if(type == 0) questionTV.setText("Synonym of the word " + practiceData.getWord() + " is?");
        else questionTV.setText("Meaning of the word " + practiceData.getWord() + " is?");

        ArrayList<String> otDefs = new ArrayList<>();
        mp.put(words.get(index).getValue().toLowerCase(), 1);
        while (mp.size() != 5) {
            int ind = Math.abs(rn.nextInt()) % 1536;
            String w = OD[ind][0];
            if (mp.containsKey(w.toLowerCase())) continue;
            otDefs.add(OD[ind][type]);
            mp.put(w.toLowerCase(), 1);
        }

        ansIndex = Math.abs(rn.nextInt()) % 5;
        ansTVs[ansIndex].setText(fromHtml("<b>" + (ansIndex + 1) + ".</b> " + ans));

        int j = 0;
        for (int i = 0; i < 5; i++) {
            if (i == ansIndex) continue;
            ansTVs[i].setText(fromHtml("<b>" + (i + 1) + ".</b> " + otDefs.get(j++)));
        }

        practicingLoading.setVisibility(GONE);
        practicingSV.setVisibility(View.VISIBLE);

        thisJudged = false;

        noQuestions++;
    }

    private int getType(){
        int mod = (int)System.currentTimeMillis()%4;
        if(mod<divider){
            divider--;
            return 0;
        }
        else {
            divider++;
            return 1;
        }
    }

    private void randomizeWords() {
        ArrayList<Word> tmpWord = new ArrayList<>();
        Random rn = new Random();
        while (words.size() != 0) {
            int ind = Math.abs(rn.nextInt()) % words.size();
            tmpWord.add(words.get(ind));
            words.remove(ind);
        }
        words = tmpWord;
    }

    public void validateResult(View v) {
        int ind = Integer.valueOf(v.getTag().toString());
        if (ind == ansIndex) {
            ansTVs[ind].setTextColor(Color.parseColor("#007200"));
            if (!thisJudged) {
                noCorrect++;
            }
//            Toast.makeText(PracticingActivity.this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            ansTVs[ind].setTextColor(Color.parseColor("#720000"));
        }
        thisJudged = true;
        nextButton.setVisibility(View.VISIBLE);
        wordLevelLL.setVisibility(View.VISIBLE);
        setTitle("SCORE: " + noCorrect + "/" + noQuestions);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                new AlertDialog.Builder(PracticingActivity.this)
                        .setTitle("End practice?")
                        .setMessage("You correctly answered " + noCorrect + " out of " + noQuestions + ". Do you want to stop?")
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
                .setMessage("You correctly answered " + noCorrect + " out of " + noQuestions + ". Do you want to stop?")
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
