package com.example.fahim.gremate;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.FetchDataAsync;
import com.example.fahim.gremate.DataClasses.Sentence;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordAllData;
import com.example.fahim.gremate.DataClasses.WordData;
import com.example.fahim.gremate.DataClasses.WordDef;
import com.example.fahim.gremate.DataClasses.WordDefP;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_word);

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            finish();
        }

        wordId = extras.getString("word_key");
        WORD = extras.getParcelable("Word");

        Log.d("ShowWordActivity", WORD.getValue());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(WORD.getValue());

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
    private  void getStates(){
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
            editor.putInt("defState", 1); defState = 1;
            editor.putInt("desState", 1); desState = 1;
            editor.putInt("senState", 1); senState = 1;
            editor.putInt("mnState", 1); mnState = 1;
            editor.commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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
    }

    private void setSenence(){
        String sen = "";
        for (int i = 0; i < wordAllData_.getSentences().size(); i++) {
            Sentence s = wordAllData_.getSentences().get(i);

            if (i != 0) sen += "<br>";
            sen += "<b>" + (i + 1) + ".</b> " + s.getValue() + "<br>";
        }
        sentenceText.setText(fromHtml(sen));
    }

    private void setMN(){
        String mn = wordAllData_.getWordData().getMn();
        mnText.setText(mn);
    }

    private void setTextViews(){
        mnText = (TextView) findViewById(R.id.showWordMNText);
        sentenceText = (TextView) findViewById(R.id.showWordSentenceText);
        definitionText = (TextView) findViewById(R.id.showWordDefinitionText);
        descriptionText = (TextView) findViewById(R.id.showWordDescriptionText);
    }

    private void setContents() {
        try {

            getStates();
            setTextViews();
            setDef();
            setDes();
            setSenence();;
            setMN();

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

            if (mnState == 1) {
                ImageButton mnButton = (ImageButton) findViewById(R.id.showWordMNButton);
                mnText.setVisibility(View.VISIBLE);
                mnButton.setImageResource(R.drawable.up);
                mnButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            } else {
                ImageButton mnButton = (ImageButton) findViewById(R.id.showWordMNButton);
                sentenceText.setVisibility(View.GONE);
                mnButton.setImageResource(R.drawable.down);
                mnButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }

            final ScrollView sv = (ScrollView) findViewById(R.id.showWordSv);
            sv.post(new Runnable() {
                public void run() {
                    sv.smoothScrollTo(0, 0);
                }
            });

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

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.getReference().child(DB.USER_WORD).child(uid).child(DB.WORD).child(wordId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wordAllData_.setWord(dataSnapshot.getValue(Word.class));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        db.getReference().child(DB.USER_WORD).child(uid).child(DB.WORDDATA).orderByChild("word").equalTo(wordId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                WordData wd = dataSnapshot.getValue(WordData.class);
                wordAllData_.setWordData(wd);
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
                wordAllData_.setWord(new Word(WORD.getWordSet(), WORD.getWordList(), WORD.getValue(), true, false, 0, 0, 1));
                setContents();
                DB.setWordData(wordAllData_, wordId);
            }
            else {
                DB.updateWord(new Word(WORD.getWordSet(), WORD.getWordList(), WORD.getValue(), WORD.isPracticable(), WORD.isLearned(), WORD.getAppeared(),
                        WORD.getCorrect(), 2), wordId);
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
