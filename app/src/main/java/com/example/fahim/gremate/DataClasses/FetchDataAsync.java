package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/16/16.
 */

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public abstract class FetchDataAsync extends AsyncTask<String, Void, String> {

    private final String url1 = "http://www.synonym.com/synonyms/";
    private final String url2 = "https://www.vocabulary.com/dictionary/";
    private final String url3 = "http://www.dictionary.com/browse/";
    private final String url4 = "http://www.mnemonicdictionary.com/?word=";

    protected WordAllData wordAllData;

    private boolean error = true;

    @Override
    protected String doInBackground(String... strings) {

        String word = strings[0];
        String wordId = strings[1];

        try {
            Document doc = Jsoup.connect(url1 + word).get();
            Document doc1 = Jsoup.connect(url2 + word).get();
            Document doc2 = Jsoup.connect(url3 + word).get();
            Document doc3 = Jsoup.connect(url4 + word).get();

            Elements[] elems = new Elements[8];

            elems[0] = doc.select("h3.term");
            elems[1] = doc.select("p.definition");
            elems[2] = doc.select("ul.synonyms");
            elems[3] = doc.select("ul.antonyms");
            elems[4] = doc1.select("p.short");
            elems[5] = doc1.select("p.long");
            elems[6] = doc2.select("p.partner-example-text");
            elems[7] = doc3.select("div.span9");

            if (elems[0].hasText()) error = false;

            String[] titles;
            String[] defs;
            String[] syns;
            String[] ants;
            String[] sens;
            String shortds, longds, mn="";

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
                    syns[i] = e.text();
                    i++;
                    if (i == 6) break;
                }

                i = 0;
                ants = new String[]{"", "", "", "", "", ""};
                for (Element e : elems[3]) {
                    ants[i] = e.text();
                    i++;
                    if (i == 6) break;
                }

                ArrayList<WordDef> wordDefs = new ArrayList<>();
                ArrayList<WordDefP> wordDefPs = new ArrayList<>();

                for(i=0; i<no; i++){
                    wordDefPs.add(new WordDefP(wordId, defs[i]));
                    wordDefs.add(new WordDef(wordId, titles[i], syns[i], ants[i], defs[i]));
                }
                wordAllData.setWordDefPs(wordDefPs);
                wordAllData.setWordDefs(wordDefs);

                shortds = elems[4].text();
                longds = elems[5].text();

                // Set it from the calling class
                // Word word1 = new Word(wordSetId, wordListId, word, true, false, false, 0, 0);

                wordAllData.setWordData(new WordData(wordId, shortds + "\n\n" + longds, mn));

                i = 0;
                sens = new String[]{"", "", "", "", "", "", "", "", "", ""};
                for (Element e : elems[6]) {
                    sens[i] = e.text();
                    i++;
                    if (i == 6) break;
                }
                ArrayList<Sentence> sentences = new ArrayList<>();
                for(int j=0; j<i; j++){
                    sentences.add(new Sentence(wordId, sens[j]));
                }
                wordAllData.setSentences(sentences);

                for(Element e: elems[7]){
                    String mns = e.text();
                    wordAllData.getWordData().setMn(mns);
                    break;
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return "";
    }

}
