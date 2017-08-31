package com.example.fahim.gremate.DataClasses;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.example.fahim.gremate.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by fahim on 23-Aug-17.
 */

public abstract class FetchImageAsync extends AsyncTask<String, Void, String> {
    private String url, wordId;
    protected ArrayList<WordImage> images;
    protected SqliteDBHelper db;
    private ArrayList<WordImageFB> wordImageFBs;
    private Context context;

    public FetchImageAsync(Context context, String wordId, ArrayList<WordImageFB> wordImageFBs) {
        this.context = context;
        this.wordId = wordId;
        this.wordImageFBs = wordImageFBs;
    }

    @Override
    protected String doInBackground(String... strings) {
        images = new ArrayList<>();
        db = new SqliteDBHelper(context);

        String st = strings[0];
        String word = strings[1];


        try{
            if(st.equals("NEW")) {
                final String url = "http://wordinfo.info/results/";
                Document doc = Jsoup.connect(url + word).timeout(10000).get();
                Elements elements = doc.select("div.definition").select("img");
                wordImageFBs = new ArrayList<>();
                for (Element e : elements) {
                    String imgUrl = e.absUrl("src");
                    wordImageFBs.add(new WordImageFB(wordId, imgUrl));
                    if(wordImageFBs.size()>1)break;
                }
                if(wordImageFBs.size()>0){
                    DBRef db = new DBRef();
                    for(int i=0; i<wordImageFBs.size(); i++)
                        db.setImageData(wordId, wordImageFBs.get(i));
                }
            }

            if (wordImageFBs != null) {
                for (int i = 0; i < wordImageFBs.size(); i++) {
                    url = wordImageFBs.get(i).getUrl();
                    try {
                        WordImage image = db.getImage(wordId, url);
                        if (image.isValid()) {
                            images.add(image);
                            continue;
                        }
                        Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
                        image = new WordImage(bitmap);
                        if (image.isValid()) {
                            db.addImage(wordId, url, image);
                            images.add(image);
                        } else {
                            image.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.notfound));
                            db.addImage(wordId, url, image);
                            images.add(image);
                        }

                    } catch (IOException e) {
                        WordImage image = new WordImage();
                        image.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.notfound));
                        db.addImage(wordId, url, image);
                        images.add(image);
                        e.printStackTrace();
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(db!=null)db.close();
        }

        return null;
    }
}
