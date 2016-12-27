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
import android.widget.TextView;

import com.example.fahim.gremate.DataClasses.WordSetwID;
import com.example.fahim.gremate.DataClasses.WordwID;
import com.example.fahim.gremate.R;
import com.example.fahim.gremate.WordSetActivity;

import java.util.ArrayList;


public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder>{


    private static ArrayList<WordwID> wsList;
    private static Context context;

    public WordAdapter(ArrayList<WordwID> wordSetList, Context context ) {
        this.wsList = wordSetList;
        this.context = context;
    }

    @Override
    public WordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.wordsetcard, parent, false);
        WordViewHolder wsv = new WordViewHolder(v );
        return wsv;
    }

    @Override
    public void onBindViewHolder(WordViewHolder holder, int position) {
//        WordSetwID ws = wsList.get(position);
//        holder.wordSet.setText(ws.getName());
//        holder.wordSetData.setText( "" + ws.getLearned() + " learned out of " + ws.getWordCount() );
    }

    @Override
    public int getItemCount() {
        return wsList.size();
    }


    public static class WordViewHolder extends RecyclerView.ViewHolder{
        CardView cv;
        TextView wordSet;
        TextView wordSetData;
        ImageButton delbtn;

        public WordViewHolder(View itemView) {
            super(itemView);

            cv = (CardView) itemView.findViewById(R.id.wordSetCardView);
            wordSet = (TextView) itemView.findViewById(R.id.wordSetName);
            wordSetData = (TextView) itemView.findViewById(R.id.wordSetData);
            delbtn = (ImageButton) itemView.findViewById(R.id.delwordset);

            delbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("Del Btn", "Clicked");
                }
            });
            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, WordSetActivity.class);
                    intent.putExtra("wordset_key", wsList.get(getAdapterPosition()).getId());
                    intent.putExtra("wordset_title", wsList.get(getAdapterPosition()).getName());
                    context.startActivity(intent);
                    Log.d("Wordset", "Clicked");
                }
            });
        }


    }
}
