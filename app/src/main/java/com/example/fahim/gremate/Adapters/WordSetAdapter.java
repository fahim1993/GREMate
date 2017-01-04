package com.example.fahim.gremate.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.WordSetwID;
import com.example.fahim.gremate.R;
import com.example.fahim.gremate.WordSetActivity;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Fahim on 24-Dec-16.
 */

public class WordSetAdapter extends RecyclerView.Adapter<WordSetAdapter.WSViewHolder>{


    private static ArrayList<WordSetwID> wsList;
    private static Context context;

    public WordSetAdapter(ArrayList<WordSetwID> wordSetList, Context context ) {
        this.wsList = wordSetList;
        this.context = context;
    }

    @Override
    public WSViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.wordsetcard, parent, false);
        WSViewHolder wsv = new WSViewHolder(v );
        return wsv;
    }

    @Override
    public void onBindViewHolder(WSViewHolder holder, final int position) {
        WordSetwID ws = wsList.get(position);
        holder.wordSet.setText(ws.getName());
        holder.wordSetData.setText( "" + ws.getWordCount() + " words" );
        holder.delbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Del Btn", "Clicked");
            }
        });
        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DB.setWordSetLastOpen(wsList.get(position).getId());
                Intent intent = new Intent(context, WordSetActivity.class);
                intent.putExtra("wordset_key", wsList.get(position).getId());
                intent.putExtra("wordset_title", wsList.get(position).getName());
                context.startActivity(intent);
                Log.d("Wordset", "Clicked");
            }
        });
        long time= System.currentTimeMillis();
        time /= (60000);
        int min_diff = (int)time - wsList.get(position).getLastOpen() ;

        if(min_diff <= 10080){
            holder.img.setImageResource(R.drawable.ic_green1);
        }
        else if ( min_diff <= 43200 ){
            holder.img.setImageResource(R.drawable.ic_green2);
        }
        else {
            holder.img.setImageResource(R.drawable.ic_gray);
        }
    }

    @Override
    public int getItemCount() {
        return wsList.size();
    }


    public static class WSViewHolder extends RecyclerView.ViewHolder{
        CardView cv;
        TextView wordSet;
        TextView wordSetData;
        ImageButton delbtn;
        ImageView img;

        public WSViewHolder(View itemView) {
            super(itemView);
            img = (ImageView)itemView.findViewById(R.id.img);
            cv = (CardView) itemView.findViewById(R.id.wordSetCardView);
            wordSet = (TextView) itemView.findViewById(R.id.wordSetName);
            wordSetData = (TextView) itemView.findViewById(R.id.wordSetData);
            delbtn = (ImageButton) itemView.findViewById(R.id.delwordset);
        }
    }
}
