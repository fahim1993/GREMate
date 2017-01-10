package com.example.fahim.gremate;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ShowWordActivity extends AppCompatActivity {

    private String wordId;

    private ArrayList<Word> words;
    private Word WORD;
    private WordAllData wordAllData_;

    private int index;
    private boolean loading;

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

    private ScrollView sv;

    private ScrollView showWordSV;
    private ProgressBar loadingPB;
    private TextView errorTextV;


    private TextView wordTitle;

    private int wordLevel;

    DatabaseReference ref1;
    Query query2;
    Query query3;
    Query query4;
    ValueEventListener listener1;
    ValueEventListener listener2;
    ValueEventListener listener3;
    ValueEventListener listener4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_word);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            finish();
        }
        Bundle b = getIntent().getBundleExtra("bundle");
        words = b.getParcelableArrayList("words");

        index = b.getInt("index");

        showWordSV = (ScrollView)findViewById(R.id.showWordSV);
        loadingPB = (ProgressBar)findViewById(R.id.loadWordPB);
        errorTextV = (TextView)findViewById(R.id.errorTV);

        levelSb = (SeekBar) findViewById(R.id.diffSeekBar);
        levelTv = (TextView) findViewById(R.id.diff);

        levelSb.setVisibility(View.GONE);
        levelTv.setVisibility(View.GONE);

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

        wordTitle = (TextView) findViewById(R.id.wordTitle);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.show_word_menu, menu);
        return super.onCreateOptionsMenu(menu);
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
        }
        else {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("defState", 0); defState = 0;
            editor.putInt("desState", 0); desState = 0;
            editor.putInt("senState", 0); senState = 0;
            editor.putInt("mnState", 0); mnState = 0;
            editor.commit();
        }
        sv = (ScrollView) findViewById(R.id.showWordSV);
        loadWord();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if( WORD.getValidity()!=2 && wordLevel != wordAllData_.getWord().getLevel() ){
            DB.setWordLevel(wordId, wordLevel);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("defState", defState);
        editor.putInt("desState", desState);
        editor.putInt("senState", senState);
        editor.putInt("mnState", mnState);

        editor.commit();

        removeListeners();
    }

    private void removeListeners(){
        if(listener1!=null)ref1.removeEventListener(listener1);
        if(listener2!=null)query2.removeEventListener(listener2);
        if(listener3!=null)query3.removeEventListener(listener3);
        if(listener4!=null)query4.removeEventListener(listener4);
    }

    private void loadWord(){
        removeListeners();
        sv.post(new Runnable() {
            public void run() {
                sv.smoothScrollTo(0, 0);
            }
        });
        loading = true;
        showWordSV.setVisibility(View.GONE);
        errorTextV.setVisibility(View.GONE);
        loadingPB.setVisibility(View.VISIBLE);

        WORD = words.get(index);
        wordId = WORD.getCopyOf();
        DB.setWordLastOpen(wordId);

        String titleText = WORD.getValue().toLowerCase();
        char[] ttext = titleText.toCharArray();
        ttext[0] = Character.toUpperCase(ttext[0]);
//        setTitle(new String(ttext));
        wordTitle.setText(new String(ttext));

        switch (WORD.getValidity()){
            case 0:
                if(isNetworkConnected())
                    new FetchData().execute(WORD.getValue(), wordId);
                break;
            case 1:
                retrieveData();
                break;
            case 2:
                loadingPB.setVisibility(View.GONE);
                errorTextV.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setDes(){

        if(wordAllData_.getWordData().getDes().length()<1)descriptionText.setText("(No data)");
        else descriptionText.setText( fromHtml(wordAllData_.getWordData().getDes().replaceAll("\\n", "<br>")));

        if (desState == 1) {
            ImageView descriptionButton = (ImageView) findViewById(R.id.showWordDescriptionIB);
            descriptionText.setVisibility(View.VISIBLE);
            descriptionButton.setImageResource(R.drawable.up);
            descriptionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            ImageView descriptionButton = (ImageView) findViewById(R.id.showWordDescriptionIB);
            descriptionText.setVisibility(View.GONE);
            descriptionButton.setImageResource(R.drawable.down);
            descriptionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
    }

    private void setDef(){
        if(wordAllData_.getWordDefs().size()==0)definitionText.setText("(No data)");
        else {
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

        if (defState == 1) {
            ImageView definitionButton = (ImageView) findViewById(R.id.showWordDefinitionIB);
            definitionText.setVisibility(View.VISIBLE);
            definitionButton.setImageResource(R.drawable.up);
            definitionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            ImageView definitionButton = (ImageView) findViewById(R.id.showWordDefinitionIB);
            definitionText.setVisibility(View.GONE);
            definitionButton.setImageResource(R.drawable.down);
            definitionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
    }

    private void setSentence(){

        if(wordAllData_.getSentences().size()==0)sentenceText.setText("(No data)");
        else {
            String sen = "";
            for (int i = 0; i < wordAllData_.getSentences().size(); i++) {
                Sentence s = wordAllData_.getSentences().get(i);

                if (i != 0) sen += "<br>";
                sen += "<b>" + (i + 1) + ".</b> " + s.getValue() + "<br>";
            }
            sentenceText.setText(fromHtml(sen));
        }

        if (senState == 1) {
            ImageView sentenceButton = (ImageView) findViewById(R.id.showWordSentenceIB);
            sentenceText.setVisibility(View.VISIBLE);
            sentenceButton.setImageResource(R.drawable.up);
            sentenceButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            ImageView sentenceButton = (ImageView) findViewById(R.id.showWordSentenceIB);
            sentenceText.setVisibility(View.GONE);
            sentenceButton.setImageResource(R.drawable.down);
            sentenceButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

    }

    private void setMN(){

        if(wordAllData_.getWordData().getMn().length()<1)mnText.setText("(No data)");
        else {
            String mn = wordAllData_.getWordData().getMn();
            mnText.setText(mn);
        }

        if (mnState == 1) {
            ImageView mnButton = (ImageView) findViewById(R.id.showWordMNIB);
            mnText.setVisibility(View.VISIBLE);
            mnButton.setImageResource(R.drawable.up);
            mnButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            ImageView mnButton = (ImageView) findViewById(R.id.showWordMNIB);
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
        wordLevel = wordAllData_.getWord().getLevel();
        levelSb.setProgress(wordLevel);
        switch(wordAllData_.getWord().getLevel()){
            case 0:
                levelTv.setText("Easy");
                levelTv.setTextColor(Color.parseColor("#007200"));
                break;
            case 1:
                levelTv.setText("Medium");
                levelTv.setTextColor(Color.parseColor("#000072"));
                break;
            case 2:
                levelTv.setText("Hard");
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
            setSentence();;
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
            ImageView definitionButton = (ImageView) findViewById(R.id.showWordDefinitionIB);
            definitionButton.setImageResource(R.drawable.down);

            definitionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            definitionButton.getParent().requestChildFocus(definitionButton,definitionButton);
        }
        else {
            defState = 1;
            TextView definitionText = (TextView) findViewById(R.id.showWordDefinitionText);
            definitionText.setVisibility(View.VISIBLE);
            ImageView definitionButton = (ImageView) findViewById(R.id.showWordDefinitionIB);
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
            ImageView descriptionButton = (ImageView) findViewById(R.id.showWordDescriptionIB);
            descriptionButton.setImageResource(R.drawable.down);

            descriptionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            descriptionButton.getParent().requestChildFocus(descriptionButton,descriptionButton);
        }
        else {
            desState = 1;
            TextView descriptionText = (TextView) findViewById(R.id.showWordDescriptionText);
            descriptionText.setVisibility(View.VISIBLE);
            ImageView descriptionButton = (ImageView) findViewById(R.id.showWordDescriptionIB);
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
            ImageView sentenceButton = (ImageView) findViewById(R.id.showWordSentenceIB);
            sentenceButton.setImageResource(R.drawable.down);

            sentenceButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            sentenceButton.getParent().requestChildFocus(sentenceButton,sentenceButton);
        }
        else {
            senState = 1;
            TextView sentenceText = (TextView) findViewById(R.id.showWordSentenceText);
            sentenceText.setVisibility(View.VISIBLE);
            ImageView sentenceButton = (ImageView) findViewById(R.id.showWordSentenceIB);
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
            ImageView mnButton = (ImageView) findViewById(R.id.showWordMNIB);
            mnButton.setImageResource(R.drawable.down);

            mnButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mnButton.getParent().requestChildFocus(mnButton,mnButton);
        }
        else {
            mnState = 1;
            TextView sentenceText = (TextView) findViewById(R.id.showWordMNText);
            sentenceText.setVisibility(View.VISIBLE);
            ImageView mnButton = (ImageView) findViewById(R.id.showWordMNIB);
            mnButton.setImageResource(R.drawable.up);

            mnButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mnButton.getParent().requestChildFocus(mnButton,mnButton);
        }
    }

    public void retrieveData(){

        setTextViews();
        wordAllData_ = new WordAllData();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref1 = FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid).child(DB.WORD).child(wordId);
        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wordAllData_.setWord(dataSnapshot.getValue(Word.class));
                setLevel();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref1.addValueEventListener(listener1);

        query2 = FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid).child(DB.WORDDATA).orderByChild("word").equalTo(wordId);
        listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot ds = dataSnapshot.getChildren().iterator().next();
                WordData wd = ds.getValue(WordData.class);
                wordAllData_.setWordData(wd);
                setDes();
                setMN();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        query2.addValueEventListener(listener2);

        query3 = FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid).child(DB.WORDDEF).orderByChild("word").equalTo(wordId);
        listener3 = new ValueEventListener() {
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
            public void onCancelled(DatabaseError databaseError) {}
        };
        query3.addValueEventListener(listener3);

        query4 = FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid).child(DB.SENTENCE).orderByChild("word").equalTo(wordId);
        listener4 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Sentence> sentences = new ArrayList<Sentence>();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Sentence w = ds.getValue(Sentence.class);
                    sentences.add(w);
                }
                wordAllData_.setSentences(sentences);
                setSentence();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        query4.addValueEventListener(listener4);

        loadingPB.setVisibility(View.GONE);
        showWordSV.setVisibility(View.VISIBLE);
        loading = false;
    }

    private class FetchData extends FetchDataAsync{
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(wordAllData != null){
                wordAllData_ = wordAllData;

                wordAllData_.setWord(new Word(WORD.getCopyOf(), WORD.getListId(), WORD.getSourceListName(),
                        WORD.getValue(), true, 1, DB.getCurrentMin(), 1, DB.getCurrentMin()));

                words.get(index).setValidity(1);
                DB.setWordData(wordAllData_, wordId);
                setContents();
                loadingPB.setVisibility(View.GONE);
                showWordSV.setVisibility(View.VISIBLE);
                loading = false;
            }
            else {
                loadingPB.setVisibility(View.GONE);
                showWordSV.setVisibility(View.GONE);
                errorTextV.setVisibility(View.VISIBLE);
                DB.setWordValidity(wordId, 2);
                loading = false;
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.prevWord:
                if(loading)break;
                if(wordLevel != wordAllData_.getWord().getLevel() ){
                    DB.setWordLevel(wordId, wordLevel);
                    words.get(index).setLevel(wordLevel);
                }
                index--;
                if(index < 0)index = words.size() - 1;
                loadWord();
                break;

            case R.id.nextWord:
                if(loading)break;
                if(wordLevel != wordAllData_.getWord().getLevel() ){
                    DB.setWordLevel(wordId, wordLevel);
                    words.get(index).setLevel(wordLevel);
                }
                index++;
                if(index >= words.size())index = 0;
                loadWord();
                break;
            case R.id.pronounce:
                if(loading)break;
                PlaybackPronunciation playback = new PlaybackPronunciation();
                playback.execute();
        }

        return super.onOptionsItemSelected(item);
    }

    private class PlaybackPronunciation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            if (isNetworkConnected()) {
                try {
                    String link = "https://ssl.gstatic.com/dictionary/static/sounds/de/0/" + WORD.getValue() + ".mp3";
                    URL url = new URL(link);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("HEAD");
                    con.connect();
                    if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        MediaPlayer player = new MediaPlayer();
                        player.reset();
                        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        player.setDataSource(link);
                        player.prepare();
                        player.start();
                    }
                    return "";
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast toast = Toast.makeText(ShowWordActivity.this, "Error...", Toast.LENGTH_SHORT);
                    toast.show();
                    return "";
                }
            } else {
                Toast toast = Toast.makeText(ShowWordActivity.this, "Internet connection required!", Toast.LENGTH_SHORT);
                toast.show();
                return "";
            }
        }
    }

}
