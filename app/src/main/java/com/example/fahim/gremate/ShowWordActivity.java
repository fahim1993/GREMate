package com.example.fahim.gremate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import com.example.fahim.gremate.DataClasses.FetchImageAsync;
import com.example.fahim.gremate.DataClasses.WordSentence;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordAllData;
import com.example.fahim.gremate.DataClasses.WordData;
import com.example.fahim.gremate.DataClasses.WordDef;
import com.example.fahim.gremate.DataClasses.WordImage;
import com.example.fahim.gremate.DataClasses.WordImageFB;
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
    private String mainListId;
    private String currentListId;

    private ArrayList<Word> words;
    private Word WORD;
    private WordAllData _wordAllData;

    private int index;
    private boolean loading;
    private boolean fetching;
    private boolean fetchingData;
    private boolean fetchingImage;
    private boolean[] loadFlags;

    private int autoPronounce=0;
    private int defState;
    private int desState;
    private int senState;
    private int imgState;
    private int mnState;
    private float textSize;

    private TextView mnText;
    private TextView sentenceText;
    private TextView definitionText;
    private TextView descriptionText;
    private TextView levelTv;

    private SeekBar levelSb;

    private LinearLayout dummyFocus;

    private ScrollView showWordSV;
    private ProgressBar loadingPB;
    private TextView errorTextV;


    private TextView wordTitle;

    private int wordLevel;

    MediaPlayer player;

    DatabaseReference ref1;
    DatabaseReference ref2;
    DatabaseReference ref3;
    DatabaseReference ref4;
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
        if (extras == null) finish();
        words = extras.getParcelableArrayList("words");
        index = extras.getInt("index");
        wsId = extras.getString("wsId");
        currentListId = extras.getString("listId");
        mainListId = extras.getString("mainListId");

        getSharedPrefValues();
        setTextViews();

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
        loadWord();
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
        editor.putInt("senState", senState);
        editor.putInt("imgState", imgState);
        editor.putInt("mnState", mnState);
        editor.putFloat("textSize", textSize);
        editor.putInt("index", index);

        editor.apply();
        player.reset();
        removeListeners();
    }

    private void getSharedPrefValues(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        index = prefs.getInt("index", -1);
        if(index==-1)finish();
        if (prefs.getInt("defState", -1) != -1) {
            defState = prefs.getInt("defState", -1);
            desState = prefs.getInt("desState", -1);
            senState = prefs.getInt("senState", -1);
            imgState = prefs.getInt("imgState", -1);
            mnState = prefs.getInt("mnState", -1);
            textSize = prefs.getFloat("textSize", 25);
        } else {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("defState", 0);
            defState = 0;
            editor.putInt("desState", 0);
            desState = 0;
            editor.putInt("senState", 0);
            senState = 0;
            editor.putInt("imgState", 0);
            imgState = 0;
            editor.putInt("mnState", 0);
            mnState = 0;

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

    private void removeListeners() {
        if (listener1 != null) ref1.removeEventListener(listener1);
        if (listener2 != null) ref2.removeEventListener(listener2);
        if (listener3 != null) ref3.removeEventListener(listener3);
        if (listener4 != null) ref4.removeEventListener(listener4);
    }

    private void loadWord() {
        removeListeners();

        loading = true;
        fetching = false;
        fetchingData = false;
        fetchingImage = false;
        loadFlags = new boolean[]{true, true, true, true, true};

        showWordSV.setVisibility(View.GONE);
        errorTextV.setVisibility(View.GONE);
        loadingPB.setVisibility(View.VISIBLE);

        WORD = words.get(index);
        wordId = WORD.getCloneOf();
        levelSb.setProgress(WORD.getLevel());

        String titleText = WORD.getValue().toLowerCase();
        char[] ttext = titleText.toCharArray();
        ttext[0] = Character.toUpperCase(ttext[0]);
//        setTitle(new String(ttext));
        wordTitle.setText(""+(index+1)+". "+ (new String(ttext)));

        switch (WORD.getValidity()) {
            case Word.UNKNOWN:
                if (isNetworkConnected()) {
                    fetching = true;
                    fetchingData = true;
                    fetchingImage = true;
                    new FetchData().execute(WORD.getValue(), wordId);
                    (new FetchImage(getApplicationContext(), wordId, null)).execute("NEW", WORD.getValue().toLowerCase());
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

        String def = "";
        for (int i = 0; i < _wordAllData.getWordDefs().size(); i++) {
            WordDef d = _wordAllData.getWordDefs().get(i);
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


        ImageView defButton = (ImageView) findViewById(R.id.showWordDefinitionIB);

        if (defState == 1) {
            definitionText.setVisibility(View.VISIBLE);
            defButton.setImageResource(R.drawable.up);
        } else {
            definitionText.setVisibility(View.GONE);
            definitionText.setTranslationY(-getHeight(definitionText));
            defButton.setImageResource(R.drawable.down);
        }
        defButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        scrollSV();
    }

    private void setSentence() {

        if (_wordAllData.getWordSentences().size() == 0) {
            findViewById(R.id.showWordSenLL).setVisibility(View.GONE);
            return;
        }
        findViewById(R.id.showWordSenLL).setVisibility(View.VISIBLE);

        String sen = "";
        for (int i = 0; i < _wordAllData.getWordSentences().size(); i++) {
            WordSentence s = _wordAllData.getWordSentences().get(i);

            if (i != 0) sen += "<br>";
            sen += "<b>" + (i + 1) + ".</b> " + s.getValue() + "<br>";
        }
        sentenceText.setText(fromHtml(sen));


        ImageView sentenceButton = (ImageView) findViewById(R.id.showWordSentenceIB);

        if (senState == 1) {
            sentenceText.setVisibility(View.VISIBLE);
            sentenceButton.setImageResource(R.drawable.up);
        } else {
            sentenceText.setVisibility(View.GONE);
            sentenceText.setTranslationY(-getHeight(sentenceText));
            sentenceButton.setImageResource(R.drawable.down);
        }
        sentenceButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        scrollSV();
    }

    private void setMN() {

        if (_wordAllData.getWordData().getMn().length() < 1) {
            findViewById(R.id.showWordMneLL).setVisibility(View.GONE);
            return;
        }

        String mn = _wordAllData.getWordData().getMn();
        mnText.setText(mn);

        findViewById(R.id.showWordMneLL).setVisibility(View.VISIBLE);

        ImageView mnButton = (ImageView) findViewById(R.id.showWordMNIB);

        if (mnState == 1) {
            mnText.setVisibility(View.VISIBLE);
            mnButton.setImageResource(R.drawable.up);
        } else {
            mnText.setVisibility(View.GONE);
            mnText.setTranslationY(-getHeight(mnText));
            mnButton.setImageResource(R.drawable.down);
        }
        mnButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        scrollSV();
    }

    private void setTextViews() {
        mnText = (TextView) findViewById(R.id.showWordMNText);
        sentenceText = (TextView) findViewById(R.id.showWordSentenceText);
        definitionText = (TextView) findViewById(R.id.showWordDefinitionText);
        descriptionText = (TextView) findViewById(R.id.showWordDescriptionText);

        wordTitle = (TextView) findViewById(R.id.wordTitle);

        mnText.setTextSize(textSize);
        sentenceText.setTextSize(textSize);
        definitionText.setTextSize(textSize);
        descriptionText.setTextSize(textSize);

        wordTitle.setTextSize(textSize+2);
        ((TextView) findViewById(R.id.showWordDescription)).setTextSize(textSize + 2);
        ((TextView) findViewById(R.id.showWordDefinition)).setTextSize(textSize + 2);
        ((TextView) findViewById(R.id.showWordSentence)).setTextSize(textSize + 2);
        ((TextView) findViewById(R.id.showWordImage)).setTextSize(textSize + 2);
        ((TextView) findViewById(R.id.showWordMN)).setTextSize(textSize + 2);
    }

    private void setLevel() {
        wordLevel = _wordAllData.getWord().getLevel();

        levelSb.setProgress(wordLevel);
        switch (_wordAllData.getWord().getLevel()) {
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
            setDef();
            setDes();
            setSentence();
            setMN();

            setLevel();
            scrollSV();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getHeight(TextView t) {
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
        TextView defTV = (TextView) findViewById(R.id.showWordDefinitionText);
        ImageView defButton = (ImageView) findViewById(R.id.showWordDefinitionIB);

        if (defState == 1) {
            defState = 0;
            hideView(defTV);
            defButton.setImageResource(R.drawable.down);
        } else {
            defState = 1;
            showView(defTV);
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

    public void toggleSentence(View v) {
        TextView senTV = (TextView) findViewById(R.id.showWordSentenceText);
        ImageView sentenceButton = (ImageView) findViewById(R.id.showWordSentenceIB);

        if (senState == 1) {
            senState = 0;
            hideView(senTV);
            sentenceButton.setImageResource(R.drawable.down);
        } else {
            senState = 1;
            showView(senTV);
            sentenceButton.setImageResource(R.drawable.up);

        }
        sentenceButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        sentenceButton.getParent().requestChildFocus(sentenceButton, sentenceButton);
    }

    public void toggleImage(View v) {

        LinearLayout imageLL = (LinearLayout) findViewById(R.id.showWordImgAddLL);
        ImageView imageButton = (ImageView) findViewById(R.id.showWordImageIB);

        if (imgState == 1) {
            imgState = 0;
            hideView(imageLL);
            imageButton.setImageResource(R.drawable.down);
        } else {
            imgState = 1;
            showView(imageLL);
            imageButton.setImageResource(R.drawable.up);

        }
        imageButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageButton.getParent().requestChildFocus(imageButton, imageButton);
    }

    public void toggleMN(View v) {
        TextView mnTV = (TextView) findViewById(R.id.showWordMNText);
        ImageView mnButton = (ImageView) findViewById(R.id.showWordMNIB);

        if (mnState == 1) {
            mnState = 0;
            hideView(mnTV);
            mnButton.setImageResource(R.drawable.down);
        } else {
            mnState = 1;
            showView(mnTV);
            mnButton.setImageResource(R.drawable.up);

        }
        mnButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mnButton.getParent().requestChildFocus(mnButton, mnButton);
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

    public void retrieveData() {

        removeListeners();
        DBRef db = new DBRef();

        setTextViews();
        _wordAllData = new WordAllData();

        _wordAllData.setWord(words.get(index));

        ref1 = db.wordDataRef(wordId);
        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                WordData wd = new WordData();
                _wordAllData.setWordData(wd);
                if (dataSnapshot.exists()) {
                    DataSnapshot ds = dataSnapshot.getChildren().iterator().next();
                    wd = ds.getValue(WordData.class);
                }
                _wordAllData.setWordData(wd);
                setDes();
                setMN();
                countLoaded(0);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        };
        ref1.addValueEventListener(listener1);

        ref2 = db.wordDefinitionRef(wordId);
        listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<WordDef> wordDefs = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordDef w = ds.getValue(WordDef.class);
                    wordDefs.add(w);
                }
                _wordAllData.setWordDefs(wordDefs);
                setDef();
                countLoaded(1);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        };
        ref2.addValueEventListener(listener2);

        ref3 = db.wordSentenceRef(wordId);
        listener3 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<WordSentence> wordSentences = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordSentence w = ds.getValue(WordSentence.class);
                    wordSentences.add(w);
                }
                _wordAllData.setWordSentences(wordSentences);
                setSentence();
                countLoaded(2);
            }
            @Override
            public void onCancelled(DatabaseError databaseError){ }
        };
        ref3.addValueEventListener(listener3);

        ref4 = db.wordImageRef(wordId);
        listener4 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<WordImageFB> images = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordImageFB wordImageFB = ds.getValue(WordImageFB.class);
                    images.add(wordImageFB);
                }
                (new FetchImage(getApplicationContext(), wordId, images)).execute("NN", "NN");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref4.addValueEventListener(listener4);
    }

    private void setImages(ArrayList<WordImage> images) {

//        if (!loading) {
//            if (wordLevel != _wordAllData.getWord().getLevel()) {
//                DB.setWordLevel(wordId, wordLevel);
//                words.get(index).setLevel(wordLevel);
//            }
//            index++;
//            if (index >= words.size()) index = 0;
//            loadWord();
//        }

        if (images.size() == 0) {
            findViewById(R.id.showWordImgLL).setVisibility(View.GONE);
            countLoaded(3);
            return;
        }

        findViewById(R.id.showWordImgLL).setVisibility(View.VISIBLE);

        LinearLayout imageLL = (LinearLayout) findViewById(R.id.showWordImgAddLL);
        imageLL.removeAllViews();

        for (WordImage image : images) {
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 5, 10, 5);
            imageView.setLayoutParams(params);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            Bitmap bitmap = image.getImage();

            int imageWidth = bitmap.getWidth();
            int imageHeight = bitmap.getHeight();

            DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();

            int newWidth = metrics.widthPixels;
            float scaleFactor = (float) newWidth / (float) imageWidth;
            if (scaleFactor > 3) scaleFactor = 3;
            int newHeight = (int) (imageHeight * scaleFactor);
            newWidth = (int) (imageWidth * scaleFactor);

            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

            imageView.setImageBitmap(bitmap);
            imageLL.addView(imageView);
        }

        ImageView imgButton = (ImageView) findViewById(R.id.showWordImageIB);

        if (imgState == 1) {
            imageLL.setVisibility(View.VISIBLE);
            imgButton.setImageResource(R.drawable.up);

        } else {
            imageLL.setVisibility(View.GONE);
            imageLL.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            imageLL.setTranslationY(-imageLL.getMeasuredHeight());
            imgButton.setImageResource(R.drawable.down);
        }
        imgButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        scrollSV();
        countLoaded(3);
    }

    void countLoaded(int no) {
        if (loading) {
            loadFlags[no] = false;
            for (int i = 0; i < 4; i++) if (loadFlags[i]) return;
            allLoaded();
        }
    }

    private void allLoaded() {
        scrollSV();
        loadingPB.setVisibility(View.GONE);
        showWordSV.setVisibility(View.VISIBLE);
        loading = false;
        if(autoPronounce==1){
            (new PlaybackPronunciation()).execute();
        }
        onToNext();
    }

    private void scrollSV() {
//        sv.post(new Runnable() {
//            public void run() {
//                sv.scrollTo(0, 0);
//                Log.d("SCROLL", "DONE");
//            }
//        });
        dummyFocus.requestFocus();
    }

    private class FetchData extends FetchDataAsync {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (wordAllData != null) {
                _wordAllData = wordAllData;

                WORD.setPracticable(true);

                _wordAllData.setWord(WORD);

                words.get(index).setValidity(Word.VALID);

                DB.setWordData(_wordAllData, wordId);
                setContents();

                fetchingData = false;
                if(!fetchingImage) {
                    fetching = false;
                    loading = false;
                    scrollSV();
                    loadingPB.setVisibility(View.GONE);
                    showWordSV.setVisibility(View.VISIBLE);
                    onToNext();
                }
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

    private class FetchImage extends FetchImageAsync {

        public FetchImage(Context context, String wordId, ArrayList<WordImageFB> wordImageFBs) {
            super(context, wordId, wordImageFBs);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setImages(images);
            fetchingImage = false;

            if(!fetchingData && fetching) {
                fetching = false;
                loading = false;
                scrollSV();
                loadingPB.setVisibility(View.GONE);
                showWordSV.setVisibility(View.VISIBLE);
                onToNext();
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
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
                                    removeListeners();
                                    DB.deleteWord(wordId, true);

                                    loading = true;
                                    showWordSV.setVisibility(View.GONE);
                                    errorTextV.setVisibility(View.GONE);
                                    loadingPB.setVisibility(View.VISIBLE);

                                    if (isNetworkConnected()) {
                                        fetchingData = true;
                                        fetchingImage = true;
                                        fetching = true;
                                        new FetchData().execute(WORD.getValue(), wordId);
                                        (new FetchImage(getApplicationContext(), wordId, null)).execute("NEW", WORD.getValue().toLowerCase());
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
                                setTextViews();
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

    private void onToNext() {
        if (wordLevel != _wordAllData.getWord().getLevel()) {
            DB.setWordLevel(wordId, wordLevel);
            words.get(index).setLevel(wordLevel);
        }
        index++;
        if (index >= words.size()) index = 0;
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        loadWord();
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
}

