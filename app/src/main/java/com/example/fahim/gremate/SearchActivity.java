package com.example.fahim.gremate;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.fahim.gremate.DataClasses.FetchDataAsync;
import com.example.fahim.gremate.DataClasses.WordSentence;
import com.example.fahim.gremate.DataClasses.WordAllData;
import com.example.fahim.gremate.DataClasses.WordDef;

public class SearchActivity extends AppCompatActivity {

    private WordAllData wordAllData_;

    private int defState;
    private int desState;
    private int senState;
    private int mnState;

    private TextView mnText;
    private TextView sentenceText;
    private TextView definitionText;
    private TextView descriptionText;

    private ScrollView showWordSV;
    private ProgressBar loadingPB;
    private TextView errorTextV;

    private AppCompatImageButton webSearchButton;
    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        showWordSV = (ScrollView)findViewById(R.id.showWordSV);
        loadingPB = (ProgressBar)findViewById(R.id.loadWordPB);
        errorTextV = (TextView)findViewById(R.id.errorTV);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        showWordSV.setVisibility(View.GONE);
        errorTextV.setVisibility(View.GONE);
        loadingPB.setVisibility(View.GONE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getInt("SRdefState", -1) != -1){
            defState = prefs.getInt("SRdefState", -1);
            desState = prefs.getInt("SRdesState", -1);
            senState = prefs.getInt("SRsenState", -1);
            mnState = prefs.getInt("SRmnState", -1);
        }
        else {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("SRdefState", 0); defState = 0;
            editor.putInt("SRdesState", 0); desState = 0;
            editor.putInt("SRsenState", 0); senState = 0;
            editor.putInt("SRmnState", 0); mnState = 0;
            editor.commit();
        }

        final ScrollView sv = (ScrollView) findViewById(R.id.showWordSV);
        sv.post(new Runnable() {
            public void run() {
                sv.smoothScrollTo(0, 0);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("SRdefState", defState);
        editor.putInt("SRdesState", desState);
        editor.putInt("SRsenState", senState);
        editor.putInt("SRmnState", mnState);

        editor.commit();
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

        if(wordAllData_.getWordSentences().size()==0)sentenceText.setText("(No data)");
        else {
            String sen = "";
            for (int i = 0; i < wordAllData_.getWordSentences().size(); i++) {
                WordSentence s = wordAllData_.getWordSentences().get(i);

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

    private void setContents() {
        try {
            setTextViews();
            setDef();
            setDes();
            setSentence();;
            setMN();

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

    private class FetchData extends FetchDataAsync{
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(wordAllData != null){
                wordAllData_ = wordAllData;
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
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

}
