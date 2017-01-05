package com.example.fahim.gremate.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import com.example.fahim.gremate.DataClasses.WordListwID;
import com.example.fahim.gremate.DataClasses.WordSetwID;
import com.example.fahim.gremate.DataClasses.WordwID;
import com.example.fahim.gremate.R;
import com.example.fahim.gremate.ShowWordActivity;
import com.example.fahim.gremate.WordSetActivity;

import java.util.ArrayList;


public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder>{


    private ArrayList<WordwID> wordList;
    public ArrayList<WordListwID> otherLists;
    private Context context;

    public WordAdapter(ArrayList<WordwID> wordList, Context context ) {
        this.wordList = wordList;
        this.otherLists = otherLists;
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
                if(wordList.get(position).getCopyOf().length()>=1) key = wordList.get(position).getCopyOf();
                b.putString("wordId", key);
                b.putParcelable("Word", wordList.get(position));

                intent.putExtras(b);
                context.startActivity(intent);
            }
        });

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(wordList.get(position).getValue().toUpperCase());
                CharSequence [] listNames = new CharSequence[] {"Delete", "Add to another list"};
                builder.setItems(listNames, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Select a list");
                        CharSequence [] listNames = new CharSequence[otherLists.size()];
                        for(int j=0; j<otherLists.size(); j++){
                            listNames[j] = otherLists.get(j).getName();
                        }
                        builder.setItems(listNames, new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DB.addWordToAnotherList(wordList.get(position), otherLists.get(i).getId());
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });


//        int lvl = wordList.get(position).getLevel();
//        int time = DB.getCurrentMin() - wordList.get(position).getLastOpen();
//        Log.d("WORDADAPTER >> ", wordList.get(position).getValue() + "  " + time);
//        if( time >= 43200){
//            holder.img.setImageResource(R.drawable.ic_gray);
//        }
//        else if (time>=10080){
//            if(lvl == 0)
//                holder.img.setImageResource(R.drawable.ic_green2);
//            else if (lvl == 1)
//                holder.img.setImageResource(R.drawable.ic_blue2);
//            else
//                holder.img.setImageResource(R.drawable.ic_red2);
//        }
//        else {
//            if(lvl == 0)
//                holder.img.setImageResource(R.drawable.ic_green1);
//            else if (lvl == 1)
//                holder.img.setImageResource(R.drawable.ic_blue1);
//            else
//                holder.img.setImageResource(R.drawable.ic_red1);
//        }

        int lvl = wordList.get(position).getLevel();
        int time = DB.getCurrentMin() - wordList.get(position).getLastOpen();
        if(time>=43200)
            holder.img.setImageResource(R.drawable.ic_gray);
        else if(time>=10080)
            holder.img.setImageResource(R.drawable.ic_green2);
        else
            holder.img.setImageResource(R.drawable.ic_green1);

        if(lvl == 0)
            holder.wordValue.setTextColor(Color.parseColor("#007200"));
        else if (lvl == 1)
            holder.wordValue.setTextColor(Color.parseColor("#000072"));
        else
            holder.wordValue.setTextColor(Color.parseColor("#720000"));

    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }


    public static class WordViewHolder extends RecyclerView.ViewHolder{
        CardView cv;
        TextView wordValue;
        ImageButton moreBtn;
        ImageView img;

        public WordViewHolder(View itemView) {
            super(itemView);

            cv = (CardView) itemView.findViewById(R.id.wordCV);
            wordValue = (TextView) itemView.findViewById(R.id.wordValue);
            moreBtn = (ImageButton) itemView.findViewById(R.id.moreBtn);
            img = (ImageView) itemView.findViewById(R.id.img);
        }
    }
}
