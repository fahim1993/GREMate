package com.example.fahim.gremate;

import android.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.Html;
import android.text.Spanned;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.DBRef;
import com.example.fahim.gremate.DataClasses.FeedTestData;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordPractice;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static android.view.View.GONE;

public class PracticingActivity extends AppCompatActivity {

    private ArrayList<Word> words;
    private int index;
    private String ans;
    private String[][] OD;

    private String wsId;

    private TextView[] ansTVs;
    private TextView questionTV;

    private int ansIndex;
    private int noQuestions;
    private int noCorrect;

    private int prvCorrect;
    private String prvTitle;

    private WordPractice wordPractice;

    private RadioGroup diffRadioGroup;
    private int wordLevel;

    private Word word;

    private boolean thisJudged;

    private AppCompatButton nextButton;
    private AppCompatButton viewButton;

    private ScrollView practicingSV;

    private ProgressBar practicingLoading;

    private ArrayList<String> wrongAns;

    private HashMap<String, ArrayList<Integer>> levelMap;

    private PlaybackPronunciation playbackPronunciation;
    private boolean pronunciationPlaying;
    MediaPlayer mediaPlayer;

    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practicing);

        practicingSV = (ScrollView) findViewById(R.id.practicingSV);
        practicingSV.setVisibility(GONE);

        practicingLoading = (ProgressBar) findViewById(R.id.practicingLoading);
        practicingLoading.setVisibility(View.VISIBLE);

        wordPractice = null;

        Bundle b = getIntent().getExtras();
        words = b.getParcelableArrayList("words");
        wsId = b.getString("wsId");

        ArrayList<Word> temp = new ArrayList<>();
        for(int lvl=0; lvl<=Word.LVL_VHARD; lvl++) {
            for (int i = 0; i < words.size(); i++) {
                if(words.get(i).getLevel()<=lvl) {
                    temp.add(words.get(i));
                }
            }
        }
        words = temp;

        random = new Random();

        diffRadioGroup = (RadioGroup) findViewById(R.id.diffRadioGroup);

        setTitle(Html.fromHtml("<font color='#BDCBDA'>SCORE: 0</font>"));

        randomizeWords();
        index = 0;
        noQuestions = 0;
        noCorrect = 0;
        prvCorrect = 0;
        prvTitle = "SCORE: 0";

        OD = new FeedTestData().getPracticeWords();

        wrongAns = new ArrayList<>();

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

                wordPractice = null;

                if (wordLevel != word.getLevel()) {
                    DB.setWordLevel(word.getCloneOf(), wordLevel);
                    if(levelMap == null || !levelMap.containsKey(word.getCloneOf())){
                        createLevelMap();
                    }
                    ArrayList<Integer> al = levelMap.get(word.getCloneOf());
                    for(int i: al) words.get(i).setLevel(wordLevel);
                }
                index++;
                if (index == words.size()) {
                    index = 0;
                    randomizeWords();
                    createLevelMap();
                }
                for (int i = 0; i < 5; i++) {
                    ansTVs[i].setTextColor(getResources().getColor(R.color.darkFore1));
                    ansTVs[i].setTypeface(null, Typeface.NORMAL);
                }

                nextButton.setVisibility(GONE);
                viewButton.setVisibility(GONE);

                practicingSV.setVisibility(GONE);
                diffRadioGroup.setVisibility(GONE);
                practicingLoading.setVisibility(View.VISIBLE);
                loadWordPracticeData(words.get(index).getCloneOf());

            }
        });

        viewButton = (AppCompatButton) findViewById(R.id.viewBtn);
        viewButton.setVisibility(GONE);
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PracticingActivity.this, SearchActivity.class);

                ArrayList<Word> temp = new ArrayList<>();
                temp.add(words.get(index));
                intent.putParcelableArrayListExtra("words", temp);
                startActivity(intent);

            }
        });

        diffRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.radio1:
                        wordLevel = 0;
                        break;
                    case R.id.radio2:
                        wordLevel = 1;
                        break;
                    case R.id.radio3:
                        wordLevel = 2;
                        break;
                    case R.id.radio4:
                        wordLevel = 3;
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if(savedInstanceState != null){
            try {
                ArrayList<Word> ws = savedInstanceState.getParcelableArrayList("words");
                if(ws != null) {
                    words = ws;
                    index = savedInstanceState.getInt("index");
                    setTitle(savedInstanceState.getString("prvTitle"));
                    noQuestions = savedInstanceState.getInt("noQuestions");
                    noCorrect = savedInstanceState.getInt("prvCorrect");
                    wrongAns = savedInstanceState.getStringArrayList("wrongAns");
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        pronunciationPlaying = false;
        mediaPlayer = new MediaPlayer();

        createLevelMap();
        loadWordPracticeData(words.get(index).getCloneOf());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("words", words);
        outState.putInt("index", index);
        outState.putInt("noQuestions", noQuestions - 1);
        outState.putInt("prvCorrect", prvCorrect);
        outState.putString("prvTitle", prvTitle);
        outState.putStringArrayList("wrongAns", wrongAns);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.practicing_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wordLevel != word.getLevel()) {
            DB.setWordLevel(word.getCloneOf(), wordLevel);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!=null){
            mediaPlayer.release();
        }

        if(playbackPronunciation!=null){
            playbackPronunciation.cancel(true);
            playbackPronunciation = null;
        }
    }

    private void loadWordPracticeData(final String id) {
        DBRef db = new DBRef();

        word = words.get(index);
        wordLevel = word.getLevel();
        diffRadioGroup.setVisibility(GONE);
        ((RadioButton)diffRadioGroup.getChildAt(wordLevel)).setChecked(true);

        final DatabaseReference ref = db.wordPracticeRef(id);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wordPractice = dataSnapshot.getValue(WordPractice.class);
                setupQuestion();
                ref.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setupQuestion() {

        HashMap<String, Integer> mp = new HashMap<>();

        String [] defs = wordPractice.getDefinitions().split(DB.DELIM);
        String [] syns = wordPractice.getSynonyms().split(DB.DELIM);

        ArrayList<Pair<String, Integer>> dns = new ArrayList<>();
        for(String s: syns) {
            if(s!=null && s.length()>0) dns.add(new Pair<>(s, 0));
        }
        for(String s: defs) {
            if(s!=null && s.length()>0) dns.add(new Pair<>(s, 1));
        }

        int rv = (int)(System.currentTimeMillis()%((long)dns.size()));
        Pair<String, Integer> ansPair = dns.get(rv);
        ans = ansPair.first.toLowerCase();
        if(ans.charAt(ans.length()-1)=='.') ans = ans.substring(0, ans.length()-1);
        int type = ansPair.second;

        if(type == 0) questionTV.setText("Synonym of the word " + wordPractice.getWord().toUpperCase() + " is?");
        else questionTV.setText("Meaning of the word " + wordPractice.getWord().toUpperCase() + " is?");

        ArrayList<String> otDefs = new ArrayList<>();
        String currentWord = words.get(index).getValue().toLowerCase();
        mp.put(currentWord, 1);
        while (mp.size() != 5) {
            int ind = Math.abs(random.nextInt()) % 1536;
            String w = OD[ind][0].toLowerCase();
            if (mp.containsKey(w)) continue;
            if(w.equals(currentWord))continue;

            otDefs.add(OD[ind][type].toLowerCase());
            mp.put(w, 1);
        }

        ansIndex = Math.abs(random.nextInt()) % 5;
        ansTVs[ansIndex].setText(fromHtml("<b>" + (ansIndex + 1) + ".</b> " + ans));

        int j = 0;
        for (int i = 0; i < 5; i++) {
            if (i == ansIndex) continue;
            ansTVs[i].setText(fromHtml("<b>" + (i + 1) + ".</b> " + otDefs.get(j++)));
        }

        practicingLoading.setVisibility(GONE);
        practicingSV.setVisibility(View.VISIBLE);

        thisJudged = false;

        prvTitle = getTitle().toString();
        prvCorrect = noCorrect;
        noQuestions++;
    }

    private void randomizeWords() {
        for(int i=0; i<5; i++)
            Collections.shuffle(words);
    }

    public void validateResult(View v) {
        int ind = Integer.valueOf(v.getTag().toString());
        if (ind == ansIndex) {
            ansTVs[ind].setTextColor(getResources().getColor(R.color.easy));
            ansTVs[ind].setTypeface(null, Typeface.BOLD);
            if (!thisJudged) {
                noCorrect++;
            }
        } else {
            ansTVs[ind].setTextColor(getResources().getColor(R.color.vhard));
            ansTVs[ind].setTypeface(null, Typeface.BOLD);
            if(!thisJudged){
                wrongAns.add("<b>"+wordPractice.getWord().toUpperCase()+": </b>" + ans);
            }
        }
        thisJudged = true;
        nextButton.setVisibility(View.VISIBLE);
        viewButton.setVisibility(View.VISIBLE);
        diffRadioGroup.setVisibility(View.VISIBLE);
        setTitle(Html.fromHtml("<font color='#BDCBDA'>SCORE: " + noCorrect + "/" + noQuestions +  " (" + words.size() + ") </font>"));

    }

    public void createLevelMap(){
        levelMap = new HashMap<>();
        if(words != null){
            int i = 0;
            for(Word w: words){
                if(levelMap.containsKey(w.getCloneOf())){
                    ArrayList<Integer> al = levelMap.get(w.getCloneOf());
                    al.add(i++);
                }
                else {
                    ArrayList<Integer> al = new ArrayList<>();
                    al.add(i++);
                    levelMap.put(w.getCloneOf(), al);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.pronounce:
                mediaPlayer.reset();
                pronunciationPlaying = false;

                pronunciationInit(word.getValue().toLowerCase());
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
        StringBuilder msg = new StringBuilder();
        if(wrongAns != null && wrongAns.size()>0) {
            msg.append("<b><u>REVIEW</u></b>");
            for(String s: wrongAns){
                msg.append("<br>");
                msg.append(s);
            }
        }
        if(msg.length()>0)msg.append("<br><br>");
        msg.append( "You correctly answered " );
        msg.append(noCorrect);
        msg.append(" out of ");
        msg.append(noQuestions);
        msg.append(". Do you want to stop?");

        final AlertDialog dialog = new AlertDialog.Builder(PracticingActivity.this, R.style.AlertDialogTheme)
                .setTitle("End practice?")
                .setMessage(fromHtml(msg.toString()))
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
                }).create();

        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                if ((dialog.findViewById(android.R.id.message)) != null) {
                    ((TextView)dialog.findViewById(android.R.id.message)).setLineSpacing(0.0f, 1.15f);
                }
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(null, Typeface.BOLD);
            }
        });

        dialog.show();
    }

    private void pronunciationInit(String word){

        int permission = ActivityCompat.checkSelfPermission(PracticingActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    PracticingActivity.this,
                    PERMISSIONS_STORAGE, 0
            );
        } else{
            pronunciationPlay(word);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        pronunciationPlay(word.getValue().toLowerCase());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void pronunciationPlay(String word){
        if(!pronunciationPlaying) {
            pronunciationPlaying = true;
            File mp3File = new File(Environment.getExternalStorageDirectory(), "pmp3/" + word + ".mp3");
            if (mp3File.exists()) {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(mp3File.getPath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            pronunciationPlaying = false;
                        }
                    });
                } catch (IOException e) {
                    if(playbackPronunciation != null) playbackPronunciation.cancel(true);
                    playbackPronunciation = new PlaybackPronunciation(this);
                    playbackPronunciation.execute(wordPractice.getPronunciation(), wordPractice.getWord());
                }
            } else {
                if(playbackPronunciation != null) playbackPronunciation.cancel(true);
                playbackPronunciation = new PlaybackPronunciation(this);
                playbackPronunciation.execute(wordPractice.getPronunciation(), wordPractice.getWord());
            }
        }
    }

    private static class PlaybackPronunciation extends AsyncTask<String, Void, String> {

        protected WeakReference<PracticingActivity> activityWeakReference;

        public PlaybackPronunciation(PracticingActivity activity){
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(String... strings) {
            if(activityWeakReference != null) {
                MediaPlayer player = activityWeakReference.get().mediaPlayer;
                if (activityWeakReference.get().isNetworkConnected()) {
                    try {
                        String link = strings[0];
                        String word = strings[1].toLowerCase();
                        if (link.length() < 1) {
                            activityWeakReference.get().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(activityWeakReference.get(), "Pronunciation not found!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            return "";
                        }

                        URLConnection conn = new URL(link).openConnection();
                        InputStream is = conn.getInputStream();
                        File mp3File = new File(Environment.getExternalStorageDirectory(), "pmp3/" + word + ".mp3");
                        OutputStream outStream = new FileOutputStream(mp3File);
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            outStream.write(buffer, 0, len);
                        }
                        outStream.close();

                        player.reset();
                        player.setDataSource(mp3File.getPath());
                        player.prepare();
                        player.start();
                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                if(activityWeakReference != null) activityWeakReference.get().pronunciationPlaying = false;
                            }
                        });

                        return "";

                    } catch (Exception e) {
                        e.printStackTrace();
                        if(activityWeakReference != null) activityWeakReference.get().pronunciationPlaying = false;
                        activityWeakReference.get().runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(activityWeakReference.get(), "Error...", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return "";
                    }
                } else {
                    if(activityWeakReference != null) activityWeakReference.get().pronunciationPlaying = false;
                    activityWeakReference.get().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activityWeakReference.get(), "Internet connection required!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return "";
                }
            }
            return "";
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

}
