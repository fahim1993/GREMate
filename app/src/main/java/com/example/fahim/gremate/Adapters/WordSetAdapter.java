package com.example.fahim.gremate.Adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.fahim.gremate.DataClasses.WordSetwID;
import com.example.fahim.gremate.R;

import java.util.ArrayList;

/**
 * Created by Fahim on 24-Dec-16.
 */

public class WordSetAdapter extends RecyclerView.Adapter<WordSetAdapter.WSViewHolder>{


    private ArrayList<WordSetwID> wsList;

    public WordSetAdapter(ArrayList<WordSetwID> wordSetList ) {
        this.wsList = wordSetList;
    }

    @Override
    public WSViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.wordsetcard, parent, false);
        WSViewHolder wsv = new WSViewHolder(v );
        return wsv;
    }

    @Override
    public void onBindViewHolder(WSViewHolder holder, int position) {
        WordSetwID ws = wsList.get(position);
        holder.wordSet.setText(ws.getName());
        holder.wordSetData.setText( "" + ws.getLearned() + " learned out of " + ws.getWordCount() );
    }

    @Override
    public int getItemCount() {
        return wsList.size();
    }


    public static class WSViewHolder extends RecyclerView.ViewHolder{
        CardView cv;
        TextView wordSet;
        TextView wordSetData;


        public WSViewHolder(View itemView) {
            super(itemView);

            cv = (CardView) itemView.findViewById(R.id.wordSetCardView);
            wordSet = (TextView) itemView.findViewById(R.id.wordSetName);
            wordSetData = (TextView) itemView.findViewById(R.id.wordSetData);
        }

    }
}
