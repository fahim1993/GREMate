package com.example.fahim.gremate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.DBRef;
import com.example.fahim.gremate.DataClasses.FetchDataAsync;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordAllData;
import com.example.fahim.gremate.DataClasses.WordData;
import com.example.fahim.gremate.DataClasses.WordDef;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ShowWordActivity extends AppCompatActivity {

    private String wordId;
    private String wsId;

    private ArrayList<Word> words;
    private Word WORD;
    private WordAllData _wordAllData;
    private int wordLevel;

    private int index;
    private boolean loading;
    private boolean refreshFlag;

    private boolean[] loadFlags;
    private int autoPronounce=0;
    private int defState;
    private int desState;
    private int extraInfoState;

    private float textSize;

    private TextView wordTitle;
    private TextView descriptionText;
    private TextView extraInfoText;

    private TextView levelTv;
    private SeekBar levelSb;

    private LinearLayout dummyFocus;
    private ScrollView showWordSV;
    private ProgressBar loadingPB;

    private TextView errorTextV;

    private boolean[] showMoreStatus;

    private ArrayList<TextView> nonTitlesTV;
    private ArrayList<TextView> titlesTV;

    MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_word);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        Bundle extras = getIntent().getExtras();
        if (extras == null) finish();
        words = extras.getParcelableArrayList("words");
        index = extras.getInt("index");
        wsId = extras.getString("wsId");
//        String currentListId = extras.getString("listId");
//        String mainListId = extras.getString("mainListId");

        getSharedPrefValues();

        wordTitle = (TextView) findViewById(R.id.wordTitle);
        descriptionText = (TextView) findViewById(R.id.showWordDescriptionText);
        extraInfoText = (TextView) findViewById(R.id.showWordExtraInfoText);

        titlesTV = new ArrayList<>();
        titlesTV.add(wordTitle);
        titlesTV.add((TextView) findViewById(R.id.showWordDescription));
        titlesTV.add((TextView) findViewById(R.id.showWordDefinition));
        titlesTV.add((TextView) findViewById(R.id.showWordExtraInfo));

        nonTitlesTV = new ArrayList<>();
        nonTitlesTV.add(descriptionText);
        nonTitlesTV.add(extraInfoText);

        setTextViewsSize();

        dummyFocus = (LinearLayout)findViewById(R.id.showWordDummyFocus);

        showWordSV = (ScrollView) findViewById(R.id.showWordSV);
        loadingPB = (ProgressBar) findViewById(R.id.loadWordPB);
        errorTextV = (TextView) findViewById(R.id.errorTV);

        levelSb = (SeekBar) findViewById(R.id.diffSeekBar);
        levelTv = (TextView) findViewById(R.id.diff);

        player = new MediaPlayer();

//        levelSb.setVisibility(View.GONE);
//        levelTv.setVisibility(View.GONE);

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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        Log.d("ShowWordActivity ", " onCreate");

        refreshFlag = false;

        loadWord();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.show_word_menu, menu);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        autoPronounce = prefs.getInt("autoPronounce", 0);
        if(autoPronounce == 0){
            menu.findItem(R.id.auto_pronounce).setTitle("Auto pronounce");
        }
        else {
            menu.findItem(R.id.auto_pronounce).setTitle("Stop auto pronounce");
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(refreshFlag) {
            refreshFlag = false;
            loadWord();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (WORD!=null && _wordAllData!=null && WORD.getValidity() != 2 && wordLevel != _wordAllData.getWord().getLevel()) {
            DB.setWordLevel(wordId, wordLevel);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("defState", defState);
        editor.putInt("desState", desState);
        editor.putInt("extraInfoState", extraInfoState);
        editor.putFloat("textSize", textSize);
        editor.putInt("index", index);

        editor.apply();
        player.reset();
    }

    private void getSharedPrefValues(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        index = prefs.getInt("index", -1);
        if(index==-1)finish();
        if (prefs.getInt("defState", -1) != -1) {
            defState = prefs.getInt("defState", -1);
            desState = prefs.getInt("desState", -1);
            extraInfoState = prefs.getInt("extraInfoState", -1);
            textSize = prefs.getFloat("textSize", 25);
        } else {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("defState", 0);
            defState = 0;
            editor.putInt("desState", 0);
            desState = 0;
            editor.putInt("extraInfoState", 0);
            extraInfoState = 0;

            textSize = 25;
            editor.putFloat("textSize", textSize);

            editor.apply();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    private void loadWord() {

        loading = true;
        loadFlags = new boolean[]{true, true};

        showWordSV.setVisibility(View.GONE);
        errorTextV.setVisibility(View.GONE);
        loadingPB.setVisibility(View.VISIBLE);

        WORD = words.get(index);
        wordId = WORD.getCloneOf();
        wordLevel  = WORD.getLevel();
        setLevel();

        String titleText = WORD.getValue().toLowerCase();
        char[] ttext = titleText.toCharArray();
        ttext[0] = Character.toUpperCase(ttext[0]);
//        setTitle(new String(ttext));
        wordTitle.setText(""+(index+1)+". "+ (new String(ttext)));

        switch (WORD.getValidity()) {
            case Word.UNKNOWN:
                if (isNetworkConnected()) {
                    new FetchData().execute(WORD.getValue(), wordId);
                }
                break;
            case Word.VALID:
                retrieveData();
                break;
            case Word.INVALID:
                loading = false;
                loadingPB.setVisibility(View.GONE);
                errorTextV.setVisibility(View.VISIBLE);
                onToNext();
                break;
        }
    }

    /********** setting / altering view functions *************/

    private void setTextViewsSize() {
        for(TextView v: titlesTV){
            v.setTextSize(textSize+2);
        }
        for(TextView v: nonTitlesTV){
            v.setTextSize(textSize);
            v.setLineSpacing(0, 1.15f);
        }
    }

    private void setDes() {

        if (_wordAllData.getWordData().getDes().length() < 1) {
            findViewById(R.id.showWordDesLL).setVisibility(View.GONE);
            return;
        }

        findViewById(R.id.showWordDesLL).setVisibility(View.VISIBLE);

        descriptionText.setText(fromHtml(_wordAllData.getWordData().getDes().replaceAll("\\n", "<br>")));

        ImageView desButton = (ImageView) findViewById(R.id.showWordDescriptionIB);

        if (desState == 1) {
            descriptionText.setVisibility(View.VISIBLE);
            desButton.setImageResource(R.drawable.up);
        } else {
            descriptionText.setVisibility(View.GONE);
            descriptionText.setTranslationY(-getHeight(descriptionText));
            desButton.setImageResource(R.drawable.down);
        }
        desButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        scrollSV();
    }

    private void setDef() {
        if (_wordAllData.getWordDefs().size() == 0) {
            findViewById(R.id.showWordDefiLL).setVisibility(View.GONE);
            return;
        }

        findViewById(R.id.showWordDefiLL).setVisibility(View.VISIBLE);

        showMoreStatus = new boolean[_wordAllData.getWordDefs().size()];
        LinearLayout defDataLL = (LinearLayout)findViewById(R.id.showWordDefDataLL);
        defDataLL.removeAllViews();

        int tag = 0;
        for(WordDef def: _wordAllData.getWordDefs()){
            View defView = getLayoutInflater().inflate(R.layout.def_view, null);
            if(tag%2 == 0) {
                defView.findViewById(R.id.showWordDefinitionIB)
                        .setBackgroundColor(Color.parseColor("#f1f1f1"));
                defView.setBackgroundColor(Color.parseColor("#f1f1f1"));
            }
            TextView firstText = (TextView)defView.findViewById(R.id.defFirstTV);
            TextView secondText = (TextView)defView.findViewById(R.id.defSecondTV);
            secondText.setVisibility(View.GONE);
            secondText.setTag(tag++);

            nonTitlesTV.add(firstText);
            nonTitlesTV.add(secondText);

            firstText.setText(fromHtml(def.getFirstHtml(tag)));
            if(def.haveMoreData()){
                secondText.setText(fromHtml(def.getSecondHtml()));
                secondText.setTranslationY(-getHeight(secondText));

                (defView.findViewById(R.id.defShowMoreRL)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        toggleShowMore(view);
                    }
                });
            }
            else {
                (defView.findViewById(R.id.defShowMoreRL)).setVisibility(View.GONE);
            }

            defDataLL.addView(defView);
        }

        ImageView defButton = (ImageView) findViewById(R.id.showWordDefinitionIB);

        if (defState == 1) {
            defDataLL.setVisibility(View.VISIBLE);
            defButton.setImageResource(R.drawable.up);
        } else {
            defDataLL.setVisibility(View.GONE);
            defDataLL.setTranslationY(-getHeight(defDataLL));
            defButton.setImageResource(R.drawable.down);
        }
        defButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        scrollSV();
    }

    private void setExtraInfo() {

        if (_wordAllData.getWordData().getExtraInfo().length() < 1) {
            findViewById(R.id.showWordExtraInfoLL).setVisibility(View.GONE);
            return;
        }

        String extraInfo = _wordAllData.getWordData().getExtraInfo();
        extraInfoText.setText(extraInfo);

        findViewById(R.id.showWordExtraInfoLL).setVisibility(View.VISIBLE);

        ImageView extraInfoButton = (ImageView) findViewById(R.id.showWordExtraInfoIB);

        if (extraInfoState == 1) {
            extraInfoText.setVisibility(View.VISIBLE);
            extraInfoButton.setImageResource(R.drawable.up);
        } else {
            extraInfoText.setVisibility(View.GONE);
            extraInfoText.setTranslationY(-getHeight(extraInfoText));
            extraInfoButton.setImageResource(R.drawable.down);
        }
        extraInfoButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        scrollSV();
    }

    private void setLevel() {
        levelSb.setProgress(wordLevel);
        switch (wordLevel) {
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
        levelSb.setVisibility(View.VISIBLE);
        levelTv.setVisibility(View.VISIBLE);
    }

    private void setContents() {
        try {
            nonTitlesTV.clear();
            nonTitlesTV.add(descriptionText);
            nonTitlesTV.add(extraInfoText);

            setDef();
            setDes();
            setExtraInfo();
            setLevel();
            setTextViewsSize();

            scrollSV();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getHeight(View t) {
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        int newWidth = metrics.widthPixels;

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(newWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        t.measure(widthMeasureSpec, heightMeasureSpec);
        return t.getMeasuredHeight();
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
        LinearLayout defDataLL = (LinearLayout) findViewById(R.id.showWordDefDataLL);
        ImageView defButton = (ImageView) findViewById(R.id.showWordDefinitionIB);

        if (defState == 1) {
            defState = 0;
            hideView(defDataLL);
            defButton.setImageResource(R.drawable.down);
        } else {
            defState = 1;
            showView(defDataLL);
            defButton.setImageResource(R.drawable.up);

        }
        defButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        defButton.getParent().requestChildFocus(defButton, defButton);

    }

    public void toggleDescription(View v) {
        TextView desTV = (TextView) findViewById(R.id.showWordDescriptionText);
        ImageView desButton = (ImageView) findViewById(R.id.showWordDescriptionIB);

        if (desState == 1) {
            desState = 0;
            hideView(desTV);
            desButton.setImageResource(R.drawable.down);
        } else {
            desState = 1;
            showView(desTV);
            desButton.setImageResource(R.drawable.up);
        }
        desButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        desButton.getParent().requestChildFocus(desButton, desButton);
    }

    public void toggleExtraInfo(View v) {
        TextView extraInfoTV = (TextView) findViewById(R.id.showWordExtraInfoText);
        ImageView extraInfoButton = (ImageView) findViewById(R.id.showWordExtraInfoIB);

        if (extraInfoState == 1) {
            extraInfoState = 0;
            hideView(extraInfoTV);
            extraInfoButton.setImageResource(R.drawable.down);
        } else {
            extraInfoState = 1;
            showView(extraInfoTV);
            extraInfoButton.setImageResource(R.drawable.up);

        }
        extraInfoButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        extraInfoButton.getParent().requestChildFocus(extraInfoButton, extraInfoButton);
    }

    public void toggleShowMore(View v){
        LinearLayout pl = (LinearLayout) v.getParent();
        TextView toToggle = (TextView) pl.findViewById(R.id.defSecondTV);
        ImageView toggleButton = (ImageView) pl.findViewById(R.id.showWordDefinitionIB);

        int tag = (int)toToggle.getTag();
        if(showMoreStatus[tag]){
            ((TextView)pl.findViewById(R.id.defShowMoreTV)).setText("Show more...");
            showMoreStatus[tag] = false;
            hideView(toToggle);
            toggleButton.setImageResource(R.drawable.down);
        }
        else{
            ((TextView)pl.findViewById(R.id.defShowMoreTV)).setText("Hide...");
            showMoreStatus[tag] = true;
            showView(toToggle);
            toggleButton.setImageResource(R.drawable.up);
        }
    }

    private void showView(final View v) {
        v.animate().translationY(0).setDuration(300);
        v.setVisibility(View.VISIBLE);
    }

    private void hideView(final View v) {
        v.animate().setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                v.setVisibility(View.GONE);
                v.animate().setListener(null);
            }
        });
        v.animate().translationY(-v.getHeight()).setDuration(300);
    }

    private void scrollSV() {
        dummyFocus.requestFocus();
    }

    /********** setting / altering view functions ends *************/

    public void retrieveData() {

        DBRef db = new DBRef();

        _wordAllData = new WordAllData();
        _wordAllData.setWord(words.get(index));

        final DatabaseReference ref1 = db.wordDataRef(wordId);
        final DatabaseReference ref2 = db.wordDefinitionRef(wordId);

        if(isNetworkConnected()) {
            ref1.child("mock").setValue("mock", new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    ref1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            WordData wd = new WordData();
                            _wordAllData.setWordData(wd);
                            if (dataSnapshot.exists()) {
                                DataSnapshot ds = dataSnapshot.getChildren().iterator().next();
                                wd = ds.getValue(WordData.class);
                            }
                            _wordAllData.setWordData(wd);
                            countLoaded(0);

                            ref1.child("mock").removeValue();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });

            ref2.child("mock").setValue("mock", new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<WordDef> wordDefs = new ArrayList<>();
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                if(ds.getKey().equals("mock")) continue;
                                WordDef w = ds.getValue(WordDef.class);
                                wordDefs.add(w);
                            }
                            _wordAllData.setWordDefs(wordDefs);
                            countLoaded(1);

                            ref2.child("mock").removeValue();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });
        }
        else {

            ref1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    WordData wd = new WordData();
                    _wordAllData.setWordData(wd);
                    if (dataSnapshot.exists()) {
                        DataSnapshot ds = dataSnapshot.getChildren().iterator().next();
                        wd = ds.getValue(WordData.class);
                    }
                    _wordAllData.setWordData(wd);
                    countLoaded(0);
                    ref1.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError){}
            });

            ref2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<WordDef> wordDefs = new ArrayList<>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        WordDef w = ds.getValue(WordDef.class);
                        wordDefs.add(w);
                    }
                    _wordAllData.setWordDefs(wordDefs);
                    countLoaded(1);
                    ref2.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    void countLoaded(int no) {
        if (loading) {
            loadFlags[no] = false;
            for (int i = 0; i < 2; i++) if (loadFlags[i]) return;
            allLoaded();
        }
    }

    private void allLoaded() {
        setContents();
        loading = false;
        if(autoPronounce==1){
            (new PlaybackPronunciation()).execute();
        }
        loadingPB.setVisibility(View.GONE);
        showWordSV.setVisibility(View.VISIBLE);
        onToNext();
    }

    private void onToNext() {
//        if (wordLevel != _wordAllData.getWord().getLevel()) {
//            DB.setWordLevel(wordId, wordLevel);
//            words.get(index).setLevel(wordLevel);
//        }
//        index++;
//        if (index >= words.size()) index = 0;
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        loadWord();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.prevWord:
                if (loading) break;
                if (wordLevel != _wordAllData.getWord().getLevel()) {
                    DB.setWordLevel(wordId, wordLevel);
                    words.get(index).setLevel(wordLevel);
                }
                index--;
                if (index < 0) index = words.size() - 1;
                loadWord();
                break;

            case R.id.nextWord:
                if (loading) break;
                if ( WORD != null && wordLevel != WORD.getLevel()) {
                    DB.setWordLevel(wordId, wordLevel);
                    words.get(index).setLevel(wordLevel);
                }
                index++;
                if (index >= words.size()) index = 0;
                loadWord();
                break;

            case R.id.pronounce:
                if (loading) break;
                (new PlaybackPronunciation()).execute();
                break;

            case R.id.edit:
                refreshFlag = true;
                Intent intent = new Intent(this, EditActivity.class);
                intent.putExtra("word", WORD);
                intent.putExtra("wsId", wsId);
                intent.putExtra("wordId", wordId);
                startActivity(intent);
                break;

            case R.id.reload:
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Reload")
                        .setMessage("Are you sure you want to reload this word? ")
                        .setPositiveButton("Reload", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                try {
                                    DB.deleteWord(wordId, true);

                                    loading = true;
                                    showWordSV.setVisibility(View.GONE);
                                    errorTextV.setVisibility(View.GONE);
                                    loadingPB.setVisibility(View.VISIBLE);

                                    if (isNetworkConnected()) {
                                        loading = true;
                                        new FetchData().execute(WORD.getValue(), wordId);
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).show();
                break;
            case R.id.text_size:
                AlertDialog.Builder builder = new AlertDialog.Builder(ShowWordActivity.this);
                LayoutInflater inflater = (ShowWordActivity.this).getLayoutInflater();

                builder.setTitle("Set Text Size");
                final View layout = inflater.inflate(R.layout.text_size_view, null);
                final SeekBar textSizePB = (SeekBar) layout.findViewById(R.id.textSizeSB);
                final TextView exampleTV = (TextView) layout.findViewById(R.id.exampleText);

                textSizePB.setProgress((int)((textSize-10)/3));
                exampleTV.setTextSize(textSize);

                textSizePB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser){
                            textSize = (float) ((progress*3.0)+10);
                            exampleTV.setTextSize(textSize);
                        }
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar){}
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar){}
                });

                builder.setView(layout)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                setTextViewsSize();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.create();
                builder.show();
                break;

            case R.id.auto_pronounce:
                if(autoPronounce == 0){
                    autoPronounce = 1;
                    item.setTitle("Stop auto pronounce");
                }
                else {
                    autoPronounce = 0;
                    item.setTitle("Auto pronounce");
                }
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("autoPronounce", autoPronounce);
                editor.apply();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        switch (keyCode){
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(action == KeyEvent.ACTION_DOWN){
                    if (loading) break;
                    if (wordLevel != _wordAllData.getWord().getLevel()) {
                        DB.setWordLevel(wordId, wordLevel);
                        words.get(index).setLevel(wordLevel);
                    }
                    index--;
                    if (index < 0) index = words.size() - 1;
                    loadWord();
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(action == KeyEvent.ACTION_DOWN){
                    if (loading) break;
                    if ( WORD != null && wordLevel != WORD.getLevel()) {
                        DB.setWordLevel(wordId, wordLevel);
                        words.get(index).setLevel(wordLevel);
                    }
                    index++;
                    if (index >= words.size()) index = 0;
                    loadWord();
                }
                break;
            case KeyEvent.KEYCODE_BACK:
                finish();
        }
        return true;
    }

    private class FetchData extends FetchDataAsync {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (wordAllData != null) {
                _wordAllData = wordAllData;
                _wordAllData.setWord(WORD);
                WORD.setPracticable(wordPractice.hasDefinitions() || wordPractice.hasSynonyms());
                words.get(index).setValidity(Word.VALID);

                DB.setWordData(_wordAllData, wordPractice, wordId);

                allLoaded();

            } else {
                loadingPB.setVisibility(View.GONE);
                showWordSV.setVisibility(View.GONE);
                errorTextV.setVisibility(View.VISIBLE);
                WORD.setValidity(Word.INVALID);
                DB.setWordValidity(wordId, Word.INVALID);
                loading = false;
            }
        }
    }

    private class PlaybackPronunciation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            if (isNetworkConnected()) {
                try {
//                    String link = "https://ssl.gstatic.com/dictionary/static/sounds/de/0/" + WORD.getValue().toLowerCase() + ".mp3";

                    String link = _wordAllData.getWordData().getPronunciation();
                    if(link.length()<1){
                        ShowWordActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Pronunciation not found!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return "";
                    }

                    URL url = new URL(link);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("HEAD");
                    con.connect();
                    if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        player.reset();
                        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        player.setDataSource(link);
                        player.prepare();
                        player.start();
                    } else {
                        ShowWordActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Error...", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    return "";
                } catch (Exception e) {
                    e.printStackTrace();
                    ShowWordActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Error...", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return "";
                }
            } else {
                ShowWordActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Internet connection required!", Toast.LENGTH_SHORT).show();
                    }
                });
                return "";
            }
        }
    }

}

