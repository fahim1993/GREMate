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
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.FetchDataAsync;
import com.example.fahim.gremate.DataClasses.FetchImageAsync;
import com.example.fahim.gremate.DataClasses.Sentence;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordAllData;
import com.example.fahim.gremate.DataClasses.WordData;
import com.example.fahim.gremate.DataClasses.WordDef;
import com.example.fahim.gremate.DataClasses.WordImage;
import com.example.fahim.gremate.DataClasses.WordImageFB;
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
    private boolean []loadFlags;

    private int defState;
    private int desState;
    private int senState;
    private int imgState;
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
    Query query5;
    ValueEventListener listener1;
    ValueEventListener listener2;
    ValueEventListener listener3;
    ValueEventListener listener4;
    ValueEventListener listener5;

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

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

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
            imgState = prefs.getInt("imgState", -1);
            mnState = prefs.getInt("mnState", -1);
        }
        else {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("defState", 0); defState = 0;
            editor.putInt("desState", 0); desState = 0;
            editor.putInt("senState", 0); senState = 0;
            editor.putInt("imgState", 0); imgState = 0;
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
        editor.putInt("imgState", imgState);
        editor.putInt("mnState", mnState);

        editor.commit();
        removeListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void removeListeners(){
        if(listener1!=null)ref1.removeEventListener(listener1);
        if(listener2!=null)query2.removeEventListener(listener2);
        if(listener3!=null)query3.removeEventListener(listener3);
        if(listener4!=null)query4.removeEventListener(listener4);
        if(listener5!=null)query5.removeEventListener(listener5);
    }

    private void loadWord(){
        removeListeners();

        loading = true;
        loadFlags = new boolean[] {true, true, true, true, true};

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

        if(wordAllData_.getWordData().getDes().length()<1){
            findViewById(R.id.showWordDesLL).setVisibility(View.GONE);
            return;
        }

        findViewById(R.id.showWordDesLL).setVisibility(View.VISIBLE);

        descriptionText.setText( fromHtml(wordAllData_.getWordData().getDes().replaceAll("\\n", "<br>")));

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
    }

    private void setDef(){
        if(wordAllData_.getWordDefs().size()==0){
            findViewById(R.id.showWordDefiLL).setVisibility(View.GONE);
            return;
        }

        findViewById(R.id.showWordDefiLL).setVisibility(View.VISIBLE);

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
    }

    private void setSentence(){

        if(wordAllData_.getSentences().size()==0){
            findViewById(R.id.showWordSenLL).setVisibility(View.GONE);
            return;
        }
        findViewById(R.id.showWordSenLL).setVisibility(View.VISIBLE);

        String sen = "";
        for (int i = 0; i < wordAllData_.getSentences().size(); i++) {
            Sentence s = wordAllData_.getSentences().get(i);

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
    }

    private void setMN(){

        if(wordAllData_.getWordData().getMn().length()<1){
            findViewById(R.id.showWordMneLL).setVisibility(View.GONE);
            return;
        }

        String mn = wordAllData_.getWordData().getMn();
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
            setSentence();
            setMN();

            setImages(new ArrayList<WordImage>());
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

        if(defState == 1) {
            defState = 0;
            hideView(defTV);
            defButton.setImageResource(R.drawable.down);
        }
        else {
            defState = 1;
            showView(defTV);
            defButton.setImageResource(R.drawable.up);

        }
        defButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        defButton.getParent().requestChildFocus(defButton,defButton);

    }

    public void toggleDescription(View v) {
        TextView desTV = (TextView) findViewById(R.id.showWordDescriptionText);
        ImageView desButton = (ImageView) findViewById(R.id.showWordDescriptionIB);

        if(desState == 1) {
            desState = 0;
            hideView(desTV);
            desButton.setImageResource(R.drawable.down);
        }
        else {
            desState = 1;
            showView(desTV);
            desButton.setImageResource(R.drawable.up);
        }
        desButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        desButton.getParent().requestChildFocus(desButton,desButton);
    }

    public void toggleSentence(View v) {
        TextView senTV = (TextView) findViewById(R.id.showWordSentenceText);
        ImageView sentenceButton = (ImageView) findViewById(R.id.showWordSentenceIB);

        if(senState == 1) {
            senState = 0;
            hideView(senTV);
            sentenceButton.setImageResource(R.drawable.down);
        }
        else {
            senState = 1;
            showView(senTV);
            sentenceButton.setImageResource(R.drawable.up);

        }
        sentenceButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        sentenceButton.getParent().requestChildFocus(sentenceButton,sentenceButton);
    }

    public void toggleImage(View v) {

        LinearLayout imageLL = (LinearLayout) findViewById(R.id.showWordImgAddLL);
        ImageView imageButton = (ImageView) findViewById(R.id.showWordImageIB);

        if(imgState == 1) {
            imgState = 0;
            hideView(imageLL);
            imageButton.setImageResource(R.drawable.down);
        }
        else {
            imgState = 1;
            showView(imageLL);
            imageButton.setImageResource(R.drawable.up);

        }
        imageButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageButton.getParent().requestChildFocus(imageButton,imageButton);
    }

    public void toggleMN(View v) {
        TextView mnTV = (TextView) findViewById(R.id.showWordMNText);
        ImageView mnButton = (ImageView) findViewById(R.id.showWordMNIB);

        if(mnState == 1) {
            mnState = 0;
            hideView(mnTV);
            mnButton.setImageResource(R.drawable.down);
        }
        else {
            mnState = 1;
            showView(mnTV);
            mnButton.setImageResource(R.drawable.up);

        }
        mnButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mnButton.getParent().requestChildFocus(mnButton,mnButton);
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

    public void retrieveData(){

        removeListeners();

        setTextViews();
        wordAllData_ = new WordAllData();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref1 = FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid).child(DB.WORD).child(wordId);
        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wordAllData_.setWord(dataSnapshot.getValue(Word.class));
                setLevel();
                countLoaded(0);
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
                WordData wd = new WordData();
                wordAllData_.setWordData(wd);
                if(!dataSnapshot.hasChildren()) return;
                DataSnapshot ds = dataSnapshot.getChildren().iterator().next();
                wd = ds.getValue(WordData.class);
                wordAllData_.setWordData(wd);
                setDes();
                setMN();
                countLoaded(1);
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
                countLoaded(2);
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
                countLoaded(3);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        query4.addValueEventListener(listener4);

        query5 = FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid).child(DB.IMAGE).orderByChild("word").equalTo(wordId);
        listener5 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> urls = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    WordImageFB wordImageFB = ds.getValue(WordImageFB.class);
                    String url = wordImageFB.getUrl();
                    urls.add(url);
                }
                FetchImage fetchImage = new FetchImage(getApplicationContext(), wordId, urls);
                fetchImage.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        query5.addValueEventListener(listener5);
    }

    private void setImages(ArrayList<WordImage> images){

        if(images.size()==0){
            findViewById(R.id.showWordImgLL).setVisibility(View.GONE);
            countLoaded(4);
            return;
        }

        findViewById(R.id.showWordImgLL).setVisibility(View.VISIBLE);

        LinearLayout imageLL = (LinearLayout) findViewById(R.id.showWordImgAddLL);
        imageLL.removeAllViews();

        for(WordImage image : images){
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10,5,10,5);
            imageView.setLayoutParams(params);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            Bitmap bitmap = image.getImage();

            int imageWidth = bitmap.getWidth();
            int imageHeight = bitmap.getHeight();

            DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();

            int newWidth = metrics.widthPixels;
            float scaleFactor = (float)newWidth/(float)imageWidth;
            if(scaleFactor>3)scaleFactor = 3;
            int newHeight = (int)(imageHeight * scaleFactor);
            newWidth = (int)(imageWidth * scaleFactor);

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

        countLoaded(4);
    }

    void countLoaded(int no){
        if(loading) {
            loadFlags[no] = false;
            for(int i=0; i<5; i++) if(loadFlags[i])return;
            allLoaded();
        }
    }

    private void allLoaded(){
        scrollSV();
        loadingPB.setVisibility(View.GONE);
        showWordSV.setVisibility(View.VISIBLE);
        loading = false;
    }

    private void scrollSV(){
        sv.post(new Runnable() {
            public void run() {
                sv.scrollTo(0,0);
            }
        });
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
                wordAllData_.setImages(new ArrayList<WordImageFB>());

                DB.setWordData(wordAllData_, wordId);
                setContents();
                scrollSV();
                loadingPB.setVisibility(View.GONE);
                showWordSV.setVisibility(View.VISIBLE);
                loading = false;
            }
            else {
                loadingPB.setVisibility(View.GONE);
                showWordSV.setVisibility(View.GONE);
                errorTextV.setVisibility(View.VISIBLE);
                WORD.setValidity(2);
                DB.setWordValidity(wordId, 2);
                loading = false;
            }
        }
    }

    private class FetchImage extends FetchImageAsync{

        public FetchImage(Context context, String wordId, ArrayList<String> urls) {
            super(context, wordId, urls);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setImages(images);
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
                break;

            case R.id.edit:
                Intent intent = new Intent(this, EditActivity.class);

                Bundle b = new Bundle();
                b.putString("word_id", wordId);
                intent.putExtras(b);
                startActivity(intent);
                break;

            case R.id.reload:
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Reload")
                        .setMessage("Are you sure you want to reload this word? ")
                        .setPositiveButton("Reload", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                removeListeners();
                                DB.deleteWord(wordId, "", false, true);

                                loading = true;
                                showWordSV.setVisibility(View.GONE);
                                errorTextV.setVisibility(View.GONE);
                                loadingPB.setVisibility(View.VISIBLE);

                                new FetchData().execute(WORD.getValue(), wordId);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).show();

        }

        return super.onOptionsItemSelected(item);
    }

    private class PlaybackPronunciation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            if (isNetworkConnected()) {
                try {
                    String link = "https://ssl.gstatic.com/dictionary/static/sounds/de/0/" + WORD.getValue().toLowerCase() + ".mp3";
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
                    else{
                        ShowWordActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Error...", Toast.LENGTH_SHORT).show();
                            }
                        });
//                        Toast toast = Toast.makeText(getApplicationContext(), "Error...", Toast.LENGTH_SHORT);
//                        toast.show();
                    }
                    return "";
                } catch (Exception e) {
                    e.printStackTrace();
                    ShowWordActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Error...", Toast.LENGTH_SHORT).show();
                        }
                    });
//                    Toast toast = Toast.makeText(getApplicationContext(), "Error...", Toast.LENGTH_SHORT);
//                    toast.show();
                    return "";
                }
            } else {
                ShowWordActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Internet connection required!", Toast.LENGTH_SHORT).show();
                    }
                });
//                Toast toast = Toast.makeText(getApplicationContext(), "Internet connection required!", Toast.LENGTH_SHORT);
//                toast.show();
                return "";
            }
        }
    }

}
