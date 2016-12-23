package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/16/16.
 */

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public abstract class FetchDataAsync extends AsyncTask<String, Void, String> {

    private final String url1 = "https://www.vocabulary.com/dictionary/";
    public String lng;

    @Override
    protected String doInBackground(String... strings) {
        String word = strings[0];

        try {
            Document doc1 = Jsoup.connect(url1+word).get();
            Elements[] elements = new Elements[2];
            elements[0] = doc1.select("p.short");
            elements[1] = doc1.select("p.long");

            lng = elements[1].text();

            Log.d("SHORT", elements[0].text());
            Log.d("LONG", elements[1].text());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
