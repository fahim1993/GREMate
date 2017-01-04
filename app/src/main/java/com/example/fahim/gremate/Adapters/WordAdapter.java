package com.example.fahim.gremate.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.fahim.gremate.DataClasses.WordwID;
import com.example.fahim.gremate.R;
import com.example.fahim.gremate.ShowWordActivity;
import com.example.fahim.gremate.WordSetActivity;

import java.util.ArrayList;


public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder>{


    private ArrayList<WordwID> wordList;
    private Context context;

    public WordAdapter(ArrayList<WordwID> wordList, Context context ) {
        this.wordList = wordList;
        this.context = context;
    }

    @Override
    public WordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.wordcard, parent, false);
        WordViewHolder wv = new WordViewHolder(v );
        return wv;
    }

    @Override
    public void onBindViewHolder(WordViewHolder holder, final int position) {
        holder.wordValue.setText(wordList.get(position).getValue());
        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ShowWordActivity.class);
                Bundle b = new Bundle();
                String key = wordList.get(position).getId();
                Log.d("WORD ADAPTER KEY", key);
                b.putString("wordId", key);
                b.putParcelable("Word", wordList.get(position));

                intent.putExtras(b);
                context.startActivity(intent);
            }
        });
        int lvl = wordList.get(position).getLevel();
        int time = DB.getCurrentMin() - wordList.get(position).getLastOpen();
        Log.d("WORDADAPTER >> ", wordList.get(position).getValue() + "  " + time);
        if( time >= 43200){
            holder.img.setImageResource(R.drawable.ic_gray);
        }
        else if (time>=10080){
            if(lvl == 0)
                holder.img.setImageResource(R.drawable.ic_green2);
            else if (lvl == 1)
                holder.img.setImageResource(R.drawable.ic_blue2);
            else
                holder.img.setImageResource(R.drawable.ic_red2);
        }
        else {
            if(lvl == 0)
                holder.img.setImageResource(R.drawable.ic_green1);
            else if (lvl == 1)
                holder.img.setImageResource(R.drawable.ic_blue1);
            else
                holder.img.setImageResource(R.drawable.ic_red1);
        }
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }


    public static class WordViewHolder extends RecyclerView.ViewHolder{
        CardView cv;
        TextView wordValue;
        ImageButton delbtn;
        ImageView img;

        public WordViewHolder(View itemView) {
            super(itemView);

            cv = (CardView) itemView.findViewById(R.id.wordCV);
            wordValue = (TextView) itemView.findViewById(R.id.wordValue);
            delbtn = (ImageButton) itemView.findViewById(R.id.delWord);
            img = (ImageView) itemView.findViewById(R.id.img);
        }
    }
}
