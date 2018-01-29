package com.example.fahim.gremate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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

public class SearchActivity extends AppCompatActivity {

    private WordAllData _wordAllData;
    private int index;
    private String wordId;
    private String wsId;

    private ArrayList<TextView> nonTitlesTV;
    private ArrayList<TextView> titlesTV;


    private ScrollView showWordSV;
    private ProgressBar loadingPB;
    private TextView errorTextV;

    private TextView descriptionText;

    private AppCompatImageButton webSearchButton;
    private EditText input;

    private boolean viewWord;

    private boolean[] showMoreStatus;
    private int defState;
    private int desState;
    private float textSize;

    DatabaseReference ref1;
    DatabaseReference ref2;
    ValueEventListener listener1;
    ValueEventListener listener2;

    private boolean loading;
    private boolean[] loadFlags;

    private Menu menu;

    private ArrayList<Word> words;

    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewWord = false;

        setContentView(R.layout.activity_search);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        showWordSV = (ScrollView)findViewById(R.id.showWordSV);
        loadingPB = (ProgressBar)findViewById(R.id.loadWordPB);
        errorTextV = (TextView)findViewById(R.id.errorTV);

        descriptionText = (TextView) findViewById(R.id.showWordDescriptionText);

        titlesTV = new ArrayList<>();
        titlesTV.add((TextView) findViewById(R.id.showWordDescription));
        titlesTV.add((TextView) findViewById(R.id.showWordDefinition));

        nonTitlesTV = new ArrayList<>();
        nonTitlesTV.add(descriptionText);

        defState = 1;
        desState = 1;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setTitle("Search");

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        input = (EditText) findViewById(R.id.searchWordET);

        webSearchButton = (AppCompatImageButton) findViewById(R.id.webSearchBtn);
        webSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String qr = input.getText().toString().replaceAll("\\s","");
                if(qr.length()<1)return;
                new FetchData().execute(qr, "");
                showWordSV.setVisibility(View.GONE);
                errorTextV.setVisibility(View.GONE);
                loadingPB.setVisibility(View.VISIBLE);

            }
        });

        player = new MediaPlayer();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        textSize = prefs.getFloat("textSize", 20);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            words = extras.getParcelableArrayList("words");
            wsId = extras.getString("wsId");
            _wordAllData = new WordAllData();
            _wordAllData.setWord(words.get(0));
            findViewById(R.id.searchLL).setVisibility(View.GONE);
            loadingPB.setVisibility(View.VISIBLE);
            index = 0;
            wordId = _wordAllData.getWord().getCloneOf();
            loading = true;
            loadFlags = new boolean[2];
            setTitle(_wordAllData.getWord().getValue().toUpperCase());
            viewWord = true;

            if(menu!=null){
                menu.findItem(R.id.edit).setVisible(true);
                menu.findItem(R.id.pronounce).setVisible(true);
            }
            retrieveData();
        }
        else {
            if(menu!=null){
                menu.findItem(R.id.edit).setVisible(false);
                menu.findItem(R.id.pronounce).setVisible(false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(listener1!=null && ref1!=null) ref1.addValueEventListener(listener1);
        if(listener2!=null && ref2!=null) ref2.addValueEventListener(listener2);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(listener1!=null && ref1!=null) ref1.removeEventListener(listener1);
        if(listener2!=null && ref2!=null) ref2.removeEventListener(listener2);
        player.reset();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        this.menu = menu;

        if(!viewWord) {
            menu.findItem(R.id.edit).setVisible(false);
            menu.findItem(R.id.pronounce).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    private void setTextViewsSize() {
        for(TextView v: titlesTV){
            v.setTextSize(textSize+2);
        }
        for(TextView v: nonTitlesTV){
            v.setTextSize(textSize);
            v.setLineSpacing(0, 1.12f);
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

        descriptionText.setVisibility(View.VISIBLE);
        desButton.setImageResource(R.drawable.up);

        desButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

    }

    private void setDef() {
        if (_wordAllData.getWordDefs().size() == 0) {
            findViewById(R.id.showWordDefiLL).setVisibility(View.GONE);
            return;
        }

        findViewById(R.id.showWordDefiLL).setVisibility(View.VISIBLE);

        showMoreStatus = new boolean[_wordAllData.getWordDefs().size()];
        for(int i=0; i<showMoreStatus.length; i++) showMoreStatus[i] = true;

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

            firstText.setTextSize(textSize);
            secondText.setTextSize(textSize);

            firstText.setLineSpacing(0, 1.135f);
            secondText.setLineSpacing(0, 1.135f);

            secondText.setVisibility(View.VISIBLE);
            secondText.setTag(tag++);

            nonTitlesTV.add(firstText);
            nonTitlesTV.add(secondText);

            firstText.setText(fromHtml(def.getFirstHtml(tag)));
            if(def.haveMoreData()){
                secondText.setText(fromHtml(def.getSecondHtml()));

                (defView.findViewById(R.id.defShowMoreRL)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        toggleShowMore(view);
                    }
                });
            }
            else {
                (defView.findViewById(R.id.defShowMoreRL)).setVisibility(View.GONE);
                secondText.setVisibility(View.GONE);
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
    }

    private void setContents() {
        try {
            nonTitlesTV.clear();
            nonTitlesTV.add(descriptionText);

            setDef();
            setDes();
            setTextViewsSize();

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

    public void retrieveData() {

        DBRef db = new DBRef();

        if(listener1!=null && ref1!=null) ref1.removeEventListener(listener1);
        if(listener2!=null && ref2!=null) ref2.removeEventListener(listener2);

        _wordAllData = new WordAllData();
        _wordAllData.setWord(words.get(index));

        ref1 = db.wordDataRef(wordId);
        ref2 = db.wordDefinitionRef(wordId);

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
                if(loading) countLoaded(0);
                else  setDes();
            }

            @Override
            public void onCancelled(DatabaseError databaseError){}
        };
        ref1.addValueEventListener(listener1);

        listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<WordDef> wordDefs = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordDef w = ds.getValue(WordDef.class);
                    wordDefs.add(w);
                }
                _wordAllData.setWordDefs(wordDefs);
                if(loading) countLoaded(1);
                else setDef();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        ref2.addValueEventListener(listener2);
    }

    void countLoaded(int no) {
        if (loading) {
            loadFlags[no] = false;
            for (int i = 0; i < 2; i++) if (loadFlags[i]) return;
            setContents();
            loading = false;
            loadingPB.setVisibility(View.GONE);
            showWordSV.setVisibility(View.VISIBLE);
        }
    }

    private class FetchData extends FetchDataAsync{
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(wordAllData != null){
                _wordAllData = wordAllData;
                setContents();
                loadingPB.setVisibility(View.GONE);
                showWordSV.setVisibility(View.VISIBLE);
            }
            else {
                loadingPB.setVisibility(View.GONE);
                showWordSV.setVisibility(View.GONE);
                errorTextV.setVisibility(View.VISIBLE);
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

            case R.id.pronounce:
                if (loading) break;
                (new PlaybackPronunciation()).execute();
                break;

            case R.id.edit:
                Intent intent = new Intent(this, EditActivity.class);
                intent.putExtra("word", words.get(0));
                intent.putExtra("wsId", wsId);
                intent.putExtra("wordId", wordId);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private class PlaybackPronunciation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            if (isNetworkConnected()) {
                try {
                    String link = _wordAllData.getWordData().getPronunciation();
                    if(link.length()<1){
                        SearchActivity.this.runOnUiThread(new Runnable() {
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
                        SearchActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Error...", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    return "";
                } catch (Exception e) {
                    e.printStackTrace();
                    SearchActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Error...", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return "";
                }
            } else {
                SearchActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Internet connection required!", Toast.LENGTH_SHORT).show();
                    }
                });
                return "";
            }
        }
    }


}
