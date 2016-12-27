package com.example.fahim.gremate;

import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.fahim.gremate.DataClasses.FetchDataAsync;
import com.example.fahim.gremate.DataClasses.Sentence;
import com.example.fahim.gremate.DataClasses.WordAllData;
import com.example.fahim.gremate.DataClasses.WordData;
import com.example.fahim.gremate.DataClasses.WordDef;
import com.example.fahim.gremate.DataClasses.WordDefP;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class ShowWordActivity extends AppCompatActivity {

    private String wordId;
    private String wordValue;
    private boolean isShadow;
    private WordAllData wordAllData_;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_word);

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            finish();
        }

        wordId = extras.getString("word_key");
        wordValue = extras.getString("word_value");
        isShadow = extras.getBoolean("is_shadow");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(wordValue);

        if(isShadow){
            new FetchData().execute(wordValue, wordId);
        }

    }
    private void setContents() {
        try {

            final TextView descriptionText = (TextView) findViewById(R.id.showWordDescriptionText);
            final TextView definitionText = (TextView) findViewById(R.id.showWordDefinitionText);
            final TextView sentenceText = (TextView) findViewById(R.id.showWordSentenceText);

            descriptionText.setText( fromHtml(wordAllData_.getWordData().getDes().replaceAll("\\n", "<br>")));
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

            String sen = "";
            for (int i = 0; i < wordAllData_.getSentences().size(); i++) {
                Sentence s = wordAllData_.getSentences().get(i);

                if (i != 0) def += "<br>";
                sen += "<b>" + (i + 1) + ".</b> " + s.getValue() + "<br>";
            }
            sentenceText.setText(fromHtml(sen));

//            if (settings.getDefinitionState() == 0) {
//                ImageButton definitionButton = (ImageButton) findViewById(R.id.wordDetailsDefinitionButton);
//                definitionText.setVisibility(View.VISIBLE);
//                definitionButton.setImageResource(R.drawable.up);
//                definitionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//            } else {
//                ImageButton definitionButton = (ImageButton) findViewById(R.id.wordDetailsDefinitionButton);
//                definitionText.setVisibility(View.GONE);
//                definitionButton.setImageResource(R.drawable.down);
//                definitionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//            }
//
//
//            if (settings.getDescriptionState() == 0) {
//                ImageButton descriptionButton = (ImageButton) findViewById(R.id.wordDetailsDescriptionButton);
//                descriptionText.setVisibility(View.VISIBLE);
//                descriptionButton.setImageResource(R.drawable.up);
//                descriptionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//            } else {
//                ImageButton descriptionButton = (ImageButton) findViewById(R.id.wordDetailsDescriptionButton);
//                descriptionText.setVisibility(View.GONE);
//                descriptionButton.setImageResource(R.drawable.down);
//                descriptionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//            }
//
//
//            if (settings.getSentenceState() == 0) {
//                ImageButton sentenceButton = (ImageButton) findViewById(R.id.wordDetailsSentenceButton);
//                sentenceText.setVisibility(View.VISIBLE);
//                sentenceButton.setImageResource(R.drawable.up);
//                sentenceButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//            } else {
//                ImageButton sentenceButton = (ImageButton) findViewById(R.id.wordDetailsSentenceButton);
//                sentenceText.setVisibility(View.GONE);
//                sentenceButton.setImageResource(R.drawable.down);
//                sentenceButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//            }

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

    private class FetchData extends FetchDataAsync{
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            wordAllData_ = wordAllData;
            setContents();
            Log.d("ShowWordActivity MN", wordAllData.getWordData().getMn());

        }
    }

}
