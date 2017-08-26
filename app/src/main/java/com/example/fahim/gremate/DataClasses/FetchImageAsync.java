package com.example.fahim.gremate.DataClasses;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.example.fahim.gremate.R;

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
    private ArrayList<String> urls;
    private Context context;

    public FetchImageAsync(Context context, String wordId, ArrayList<String>urls) {
        this.context = context;
        this.wordId = wordId;
        this.urls = urls;
    }

    @Override
    protected String doInBackground(String... strings) {
        images = new ArrayList<>();
        SqliteDBHelper db = new SqliteDBHelper(context);
        for(int i=0; i<urls.size(); i++) {
            url = urls.get(i);
            try {
                WordImage image = db.getImage(wordId, url);
                if (image.isValid()) {
                    images.add(image);
                    continue;
                }
                Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
                image = new WordImage(bitmap);
                if(image.isValid()){
                    db.addImage(wordId, url, image);
                    images.add(image);
                }
                else{
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
        return null;
    }
}