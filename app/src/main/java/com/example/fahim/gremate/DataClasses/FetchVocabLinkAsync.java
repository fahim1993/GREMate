package com.example.fahim.gremate.DataClasses;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by Fahim on 04-Sep-17.
 */

public abstract class FetchVocabLinkAsync  extends AsyncTask<String, Void, String> {

    protected String link="";

    @Override
    protected String doInBackground(String... strings) {

        try {
            String word = strings[0];
            String wordId = strings[1];


            if(wordId.length()<1 || word.length()<1) return null;

            String LINK_DAT = "https://www.vocabulary.com/dictionary/";
            String LINK_VOC = "https://audio.vocab.com/1.0/us/";

            Document document = Jsoup.connect(LINK_DAT + word).get();
            Element element = document.select(".audio").first();

            String pl = element.attr("data-audio");

            if(pl.length()>0) {
                link = LINK_VOC + pl +".mp3";
                DB.setWordPronunciation(wordId, link);
                Log.d("FetchDataAsync", link);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
