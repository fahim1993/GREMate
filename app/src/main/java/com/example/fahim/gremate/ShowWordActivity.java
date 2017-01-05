package com.example.fahim.gremate;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.FetchDataAsync;
import com.example.fahim.gremate.DataClasses.Sentence;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordAllData;
import com.example.fahim.gremate.DataClasses.WordData;
import com.example.fahim.gremate.DataClasses.WordDef;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class ShowWordActivity extends AppCompatActivity {

    private String wordId;

    private Word WORD;
    private WordAllData wordAllData_;

    private int defState;
    private int desState;
    private int senState;
    private int mnState;

    private TextView mnText;
    private TextView sentenceText;
    private TextView definitionText;
    private TextView descriptionText;
    private TextView levelTv;

    private SeekBar levelSb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_word);

//        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.95);
//        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.80);

//        getWindow().setLayout(width, height);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            finish();
        }

        levelSb = (SeekBar) findViewById(R.id.diffSeekBar);
        levelTv = (TextView) findViewById(R.id.diff);

        levelSb.setVisibility(View.GONE);
        levelTv.setVisibility(View.GONE);

        wordId = extras.getString("wordId");
        DB.setWordLastOpen(wordId);

        WORD = extras.getParcelable("Word");

        levelSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                DB.setWordLevel(wordId, i);
                switch(i){
                    case 0:
                        levelTv.setText("Difficulty: Easy");
                        levelTv.setTextColor(Color.parseColor("#006400"));
                        break;
                    case 1:
                        levelTv.setText("Difficulty: Medium");
                        levelTv.setTextColor(Color.parseColor("#00008B"));
                        break;
                    case 2:
                        levelTv.setText("Difficulty: Hard");
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



        Log.d("ShowWordActivity", WORD.getValue());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //setTitle(WORD.getValue().toUpperCase());
        setTitle(WORD.getValue());

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        switch (WORD.getValidity()){
            case 0:
                if(isNetworkConnected())
                    new FetchData().execute(WORD.getValue(), wordId);
                break;
            case 1:
                retriveData();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getInt("defState", -1) != -1){
            defState = prefs.getInt("defState", -1);
            desState = prefs.getInt("desState", -1);
            senState = prefs.getInt("senState", -1);
            mnState = prefs.getInt("mnState", -1);

            Log.d("DefState: ", ""+defState);
        }
        else {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("defState", 0); defState = 0;
            editor.putInt("desState", 0); desState = 0;
            editor.putInt("senState", 0); senState = 0;
            editor.putInt("mnState", 0); mnState = 0;
            editor.commit();
        }

        final ScrollView sv = (ScrollView) findViewById(R.id.showWordSv);
        sv.post(new Runnable() {
            public void run() {
                sv.smoothScrollTo(0, 0);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("ON PAUSE ", "CALLED");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("defState", defState);
        editor.putInt("desState", desState);
        editor.putInt("senState", senState);
        editor.putInt("mnState", mnState);
        editor.commit();

    }

    private void setDes(){
        descriptionText.setText( fromHtml(wordAllData_.getWordData().getDes().replaceAll("\\n", "<br>")));

        if (desState == 1) {
            ImageButton descriptionButton = (ImageButton) findViewById(R.id.showWordDescriptionButton);
            descriptionText.setVisibility(View.VISIBLE);
            descriptionButton.setImageResource(R.drawable.up);
            descriptionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            ImageButton descriptionButton = (ImageButton) findViewById(R.id.showWordDescriptionButton);
            descriptionText.setVisibility(View.GONE);
            descriptionButton.setImageResource(R.drawable.down);
            descriptionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
    }

    private void setDef(){
        String def = "";
        for (int i = 0; i < wordAllData_.getWordDefs().size(); i++) {
            WordDef d = wordAllData_.getWordDefs().get(i);
            if (i != 0) def += "<br><br>";
            def += "<b>" + d.getTitle() + "</b><br>";
            def += "<i>" + d.getDef() + "</i><br>";
            if (d.getSyn().length() > 0) {
                def += "<b>Synonyms:</b><br>";
                def += d.getSyn() + "<br>";
            }

            if (d.getAnt().length() > 0) {
                def += "<b>Antonyms:</b><br>";
                def += d.getAnt();
            }
        }
        definitionText.setText(fromHtml(def));

        if (defState == 1) {
            ImageButton definitionButton = (ImageButton) findViewById(R.id.showWordDefinitionButton);
            definitionText.setVisibility(View.VISIBLE);
            definitionButton.setImageResource(R.drawable.up);
            definitionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            ImageButton definitionButton = (ImageButton) findViewById(R.id.showWordDefinitionButton);
            definitionText.setVisibility(View.GONE);
            definitionButton.setImageResource(R.drawable.down);
            definitionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
    }

    private void setSenence(){
        String sen = "";
        for (int i = 0; i < wordAllData_.getSentences().size(); i++) {
            Sentence s = wordAllData_.getSentences().get(i);

            if (i != 0) sen += "<br>";
            sen += "<b>" + (i + 1) + ".</b> " + s.getValue() + "<br>";
        }
        sentenceText.setText(fromHtml(sen));

        if (senState == 1) {
            ImageButton sentenceButton = (ImageButton) findViewById(R.id.showWordSentenceButton);
            sentenceText.setVisibility(View.VISIBLE);
            sentenceButton.setImageResource(R.drawable.up);
            sentenceButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            ImageButton sentenceButton = (ImageButton) findViewById(R.id.showWordSentenceButton);
            sentenceText.setVisibility(View.GONE);
            sentenceButton.setImageResource(R.drawable.down);
            sentenceButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

    }

    private void setMN(){

        String mn = wordAllData_.getWordData().getMn();
        mnText.setText(mn);

        if (mnState == 1) {
            ImageButton mnButton = (ImageButton) findViewById(R.id.showWordMNButton);
            mnText.setVisibility(View.VISIBLE);
            mnButton.setImageResource(R.drawable.up);
            mnButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            ImageButton mnButton = (ImageButton) findViewById(R.id.showWordMNButton);
            mnText.setVisibility(View.GONE);
            mnButton.setImageResource(R.drawable.down);
            mnButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
    }

    private void setTextViews(){
        mnText = (TextView) findViewById(R.id.showWordMNText);
        sentenceText = (TextView) findViewById(R.id.showWordSentenceText);
        definitionText = (TextView) findViewById(R.id.showWordDefinitionText);
        descriptionText = (TextView) findViewById(R.id.showWordDescriptionText);
    }

    private void setLevel(){
        levelSb.setProgress(wordAllData_.getWord().getLevel());
        switch(wordAllData_.getWord().getLevel()){
            case 0:
                levelTv.setText("Difficulty: Easy");
                levelTv.setTextColor(Color.parseColor("#007200"));
                break;
            case 1:
                levelTv.setText("Difficulty: Medium");
                levelTv.setTextColor(Color.parseColor("#000072"));
                break;
            case 2:
                levelTv.setText("Difficulty: Hard");
                levelTv.setTextColor(Color.parseColor("#720000"));
                break;
        }
        levelSb.setVisibility(View.VISIBLE);
        levelTv.setVisibility(View.VISIBLE);
    }

    private void setContents() {
        try {
            setTextViews();
            setDef();
            setDes();
            setSenence();;
            setMN();
            setLevel();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    public void toggleDefinition(View v) {
        if(defState == 1) {
            defState = 0;
            TextView definitionText = (TextView) findViewById(R.id.showWordDefinitionText);
            definitionText.setVisibility(View.GONE);
            ImageButton definitionButton = (ImageButton) findViewById(R.id.showWordDefinitionButton);
            definitionButton.setImageResource(R.drawable.down);

            definitionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            definitionButton.getParent().requestChildFocus(definitionButton,definitionButton);
        }
        else {
            defState = 1;
            TextView definitionText = (TextView) findViewById(R.id.showWordDefinitionText);
            definitionText.setVisibility(View.VISIBLE);
            ImageButton definitionButton = (ImageButton) findViewById(R.id.showWordDefinitionButton);
            definitionButton.setImageResource(R.drawable.up);

            definitionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            definitionButton.getParent().requestChildFocus(definitionButton,definitionButton);
        }

    }

    public void toggleDescription(View v) {
        if(desState == 1) {
            desState = 0;
            TextView descriptionText = (TextView) findViewById(R.id.showWordDescriptionText);
            descriptionText.setVisibility(View.GONE);
            ImageButton descriptionButton = (ImageButton) findViewById(R.id.showWordDescriptionButton);
            descriptionButton.setImageResource(R.drawable.down);

            descriptionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            descriptionButton.getParent().requestChildFocus(descriptionButton,descriptionButton);
        }
        else {
            desState = 1;
            TextView descriptionText = (TextView) findViewById(R.id.showWordDescriptionText);
            descriptionText.setVisibility(View.VISIBLE);
            ImageButton descriptionButton = (ImageButton) findViewById(R.id.showWordDescriptionButton);
            descriptionButton.setImageResource(R.drawable.up);

            descriptionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            descriptionButton.getParent().requestChildFocus(descriptionButton,descriptionButton);
        }

    }

    public void toggleSentence(View v) {
        if(senState == 1) {
            senState = 0;
            TextView sentenceText = (TextView) findViewById(R.id.showWordSentenceText);
            sentenceText.setVisibility(View.GONE);
            ImageButton sentenceButton = (ImageButton) findViewById(R.id.showWordSentenceButton);
            sentenceButton.setImageResource(R.drawable.down);

            sentenceButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            sentenceButton.getParent().requestChildFocus(sentenceButton,sentenceButton);
        }
        else {
            senState = 1;
            TextView sentenceText = (TextView) findViewById(R.id.showWordSentenceText);
            sentenceText.setVisibility(View.VISIBLE);
            ImageButton sentenceButton = (ImageButton) findViewById(R.id.showWordSentenceButton);
            sentenceButton.setImageResource(R.drawable.up);

            sentenceButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            sentenceButton.getParent().requestChildFocus(sentenceButton,sentenceButton);
        }
    }

    public void toggleMN(View v) {
        if(mnState == 1) {
            mnState = 0;
            TextView mnText = (TextView) findViewById(R.id.showWordMNText);
            mnText.setVisibility(View.GONE);
            ImageButton mnButton = (ImageButton) findViewById(R.id.showWordMNButton);
            mnButton.setImageResource(R.drawable.down);

            mnButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mnButton.getParent().requestChildFocus(mnButton,mnButton);
        }
        else {
            mnState = 1;
            TextView sentenceText = (TextView) findViewById(R.id.showWordMNText);
            sentenceText.setVisibility(View.VISIBLE);
            ImageButton mnButton = (ImageButton) findViewById(R.id.showWordMNButton);
            mnButton.setImageResource(R.drawable.up);

            mnButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mnButton.getParent().requestChildFocus(mnButton,mnButton);
        }
    }

    public void retriveData(){

        setTextViews();
        wordAllData_ = new WordAllData();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.getReference().child(DB.USER_WORD).child(uid).child(DB.WORD).child(wordId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                wordAllData_.setWord(dataSnapshot.getValue(Word.class));
                setLevel();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        db.getReference().child(DB.USER_WORD).child(uid).child(DB.WORDDATA).orderByChild("word").equalTo(wordId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot ds = dataSnapshot.getChildren().iterator().next();
                WordData wd = ds.getValue(WordData.class);
                wordAllData_.setWordData(wd);
                Log.d("ShowWordActivity >>> ", wordAllData_.getWordData().getDes());
                Log.d("ShowWordActivity >>> ", ""+dataSnapshot.getChildrenCount());
                setDes();
                setMN();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        db.getReference().child(DB.USER_WORD).child(uid).child(DB.WORDDEF).orderByChild("word").equalTo(wordId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<WordDef> wordDefs = new ArrayList<WordDef>();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    WordDef w = ds.getValue(WordDef.class);
                    wordDefs.add(w);
                }
                wordAllData_.setWordDefs(wordDefs);
                setDef();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        db.getReference().child(DB.USER_WORD).child(uid).child(DB.SENTENCE).orderByChild("word").equalTo(wordId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Sentence> sentences = new ArrayList<Sentence>();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Sentence w = ds.getValue(Sentence.class);
                    sentences.add(w);
                }
                wordAllData_.setSentences(sentences);
                setSenence();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private class FetchData extends FetchDataAsync{
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(wordAllData != null){
                wordAllData_ = wordAllData;
                wordAllData_.setWord(new Word(WORD.getListId(), WORD.getValue(), true, 1, DB.getCurrentMin(), 1, DB.getCurrentMin()));
                setContents();
                DB.setWordData(wordAllData_, wordId);
            }
            else {
                DB.updateWord(new Word(WORD.getListId(), WORD.getValue(), WORD.isPracticable(), 2,
                        DB.getCurrentMin(), WORD.getLevel(), WORD.getAdded() ), wordId);
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

}
