package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/16/16.
 */

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public abstract class FetchDataAsync extends AsyncTask<String, Void, String> {

    private final String url1 = "http://www.synonym.com/synonyms/";
    private final String url2 = "https://www.vocabulary.com/dictionary/";
    private final String url3 = "http://corpus.vocabulary.com/api/1.0/examples.json?query=";
    private final String url4 = "http://www.mnemonicdictionary.com/?word=";
    private final String LINK_VOC = "https://audio.vocab.com/1.0/us/";


    protected WordAllData wordAllData;

    private boolean error = true;

    @Override
    protected String doInBackground(String... strings) {

        String word = strings[0].toLowerCase();
        String wordId = strings[1];

        try {
            Document doc = Jsoup.connect(url1 + word).timeout(10000).get();
            Document doc1 = Jsoup.connect(url2 + word).timeout(10000).get();
            Document doc2 = Jsoup.connect(url3 + word + "&maxResults=20").ignoreContentType(true).timeout(10000).get();
            Document doc3 = Jsoup.connect(url4 + word).timeout(10000).get();


            Elements[] elems = new Elements[9];

            elems[0] = doc.select("h3.term");
            elems[1] = doc.select("p.definition");
            elems[2] = doc.select("ul.synonyms");
            elems[3] = doc.select("ul.antonyms");
            elems[4] = doc1.select("p.short");
            elems[5] = doc1.select("p.long");
            elems[7] = doc3.select("div.span9");

            if (elems[0].hasText()) error = false;

            String[] titles;
            String[] defs;
            String[] syns;
            String[] ants;
            String shortds, longds, mn = "";

            if (!error) {

                wordAllData = new WordAllData();

                int i = 0, no;
                titles = new String[]{"", "", "", "", "", ""};
                for (Element e : elems[0]) {
                    titles[i] = e.text();
                    i++;
                    if (i == 6) break;
                }
                no = i;
                i = 0;
                defs = new String[]{"", "", "", "", "", ""};
                for (Element e : elems[1]) {
                    defs[i] = e.text();
                    i++;
                    if (i == 6) break;
                }

                i = 0;
                syns = new String[]{"", "", "", "", "", ""};
                for (Element e : elems[2]) {
                    String s = "";
                    if (e.hasText()) {
                        Elements x = e.select("li.syn");
                        for (Element ex : x) {
                            s += (ex.text() + ", ");
                        }
                        s = s.substring(0, s.length() - 2);
                    }
                    syns[i] = s;

                    i++;
                    if (i == 6) break;
                }

                i = 0;
                ants = new String[]{"", "", "", "", "", ""};
                for (Element e : elems[3]) {
                    String s = "";
                    if (e.hasText()) {
                        Elements x = e.select("li.ant");
                        for (Element ex : x) {
                            s += (ex.text() + ", ");
                        }
                        s = s.substring(0, s.length() - 2);
                    }
                    ants[i] = s;

                    i++;
                    if (i == 6) break;
                }

                ArrayList<WordDef> wordDefs = new ArrayList<>();

                for (i = 0; i < no; i++) {
                    wordDefs.add(new WordDef(titles[i], syns[i], ants[i], defs[i]));
                }
                wordAllData.setWordDefs(wordDefs);

                shortds = elems[4].text();
                longds = elems[5].text();

                // Set it from the calling class
                // Word word1 = new Word(wordSetId, wordListId, word, true, false, false, 0, 0);

                if (shortds.length() < 1) wordAllData.setWordData(new WordData("", mn, ""));
                else wordAllData.setWordData(new WordData(shortds + "\n\n" + longds, mn, ""));

                ArrayList<WordSentence> wordSentences = new ArrayList<>();
                String jsn = doc2.text();
                try{
                    JSONObject obj = new JSONObject(jsn);
                    JSONArray sts = obj.getJSONObject("result").getJSONArray("sentences");
                    for (int ii = 0; ii < sts.length(); ii++) {
                        if (ii == 8) break;
                        JSONObject ith = (JSONObject) sts.get(ii);
                        String sentence = (String) ith.get("sentence");
                        JSONArray offset = (JSONArray) ith.get("offsets");
                        String finalSentence = "";
                        int pv = 0;
                        for (int j = 0; j < offset.length(); j += 2) {
                            int st = (int) offset.get(j);
                            int en = (int) offset.get(j + 1);
                            finalSentence += sentence.substring(pv, st);
                            finalSentence += "<b>";
                            finalSentence += sentence.substring(st, en);
                            finalSentence += "</b>";
                            pv = en;
                        }
                        if (pv < sentence.length()) {
                            finalSentence += sentence.substring(pv, sentence.length());
                        }
                        wordSentences.add(new WordSentence(finalSentence));
                    }

                    String pl = doc1.select(".audio").first().attr("data-audio");
                    if(pl.length()>0) {
                        String pronunciationLink = LINK_VOC + pl + ".mp3";
                        wordAllData.getWordData().setPronunciation(pronunciationLink);
                    }

                }
                catch (Exception e){
                    e.printStackTrace();
                }

                wordAllData.setWordSentences(wordSentences);

                for (Element e : elems[7]) {
                    String mns = e.text();
                    wordAllData.getWordData().setMn(mns);
                    break;
                }
                wordAllData.setImages(new ArrayList<WordImageFB>());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return "";
    }

}
