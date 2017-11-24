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
import android.util.Log;
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

import static android.view.View.GONE;

public class PracticingActivity extends AppCompatActivity {

    private ArrayList<Word> words;
    private int index;
    private String def;
    private String[][] OD;

    private DatabaseReference ref1;
    private ValueEventListener listener1;

    private TextView[] ansTVs;
    private TextView questionTV;

    private int ansIndex;
    private int noQuestions;
    private int noCorrect;

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
                loadWordDef(words.get(index).getCloneOf());

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

        loadWordDef(words.get(index).getCloneOf());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wordLevel != word.getLevel()) {
            DB.setWordLevel(word.getCloneOf(), wordLevel);
        }
    }

    private void loadWordDef(final String id) {
        DBRef db = new DBRef();

        word = words.get(index);
        wordLevel = word.getLevel();
        wordLevelLL.setVisibility(GONE);
        levelSb.setProgress(wordLevel);

        ref1 = db.wordDefinitionRef(id);
        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<String> defs = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordDef w = ds.getValue(WordDef.class);
                    defs.add(w.getDef());
                }
                Random rn = new Random();
                int modVal = defs.size();
                int i = Math.abs(rn.nextInt()) % modVal;
                def = defs.get(i);

                setupQuestion();
                ref1.removeEventListener(listener1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        ref1.addValueEventListener(listener1);

//        FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(FirebaseAuth
//                .getInstance().getCurrentUser().getUid()).child(DB.WORD).child(id).addListenerForSingleValueEvent(
//                new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        word = dataSnapshot.getValue(Word.class);
//                        word.setCloneOf(id);
//                        wordLevel = word.getLevel();
//                        levelSb.setProgress(wordLevel);
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                }
//        );
    }

    private void setupQuestion() {

        Random rn = new Random();
        HashMap<String, Integer> mp = new HashMap<>();

        questionTV.setText("Meaning of the word " + words.get(index).getValue() + " is?");
        ArrayList<String> otDefs = new ArrayList<>();
        mp.put(words.get(index).getValue().toLowerCase(), 1);
        while (mp.size() != 5) {
            int ind = Math.abs(rn.nextInt()) % 1536;
            String w = OD[ind][0];
            if (mp.containsKey(w.toLowerCase())) continue;
            otDefs.add(OD[ind][1]);
            mp.put(w.toLowerCase(), 1);
        }
        ansIndex = Math.abs(rn.nextInt()) % 5;
        ansTVs[ansIndex].setText(fromHtml("<b>" + (ansIndex + 1) + ".</b> " + def));

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
