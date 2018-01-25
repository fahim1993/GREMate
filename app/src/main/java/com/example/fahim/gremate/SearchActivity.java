package com.example.fahim.gremate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.fahim.gremate.DataClasses.FetchDataAsync;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordAllData;
import com.example.fahim.gremate.DataClasses.WordDef;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private WordAllData _wordAllData;

    private ArrayList<TextView> nonTitlesTV;
    private ArrayList<TextView> titlesTV;

    private ScrollView showWordSV;
    private ProgressBar loadingPB;
    private TextView errorTextV;

    private TextView descriptionText;

    private AppCompatImageButton webSearchButton;
    private EditText input;


    private boolean[] showMoreStatus;
    private int defState;
    private int desState;
    private float textSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        textSize = prefs.getFloat("textSize", 20);
    }

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
        LinearLayout defDataLL = (LinearLayout)findViewById(R.id.showWordDefDataLL);
        defDataLL.removeAllViews();

        int tag = 0;
        for(WordDef def: _wordAllData.getWordDefs()){
            View defView = getLayoutInflater().inflate(R.layout.def_view, null);
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

        defDataLL.setVisibility(View.VISIBLE);
        defButton.setImageResource(R.drawable.up);
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
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

}
