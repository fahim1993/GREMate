package com.example.fahim.gremate.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.WordSetwID;
import com.example.fahim.gremate.LearnActivity;
import com.example.fahim.gremate.R;
import com.example.fahim.gremate.WordSetActivity;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Fahim on 24-Dec-16.
 */

public class WordSetAdapter extends RecyclerView.Adapter<WordSetAdapter.WSViewHolder>{


    private ArrayList<WordSetwID> wsList;
    private Context context;

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
                new AlertDialog.Builder(context)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this word set?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = wsList.get(position).getName();
                                DB.deleteList(wsList.get(position).getAllList(), wsList.get(position).getId(), true);
                                Toast.makeText(context,
                                        "Word set " + name + " deleted" , Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).show();
            }
        });
        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DB.setWordSetLastOpen(wsList.get(position).getId());
                Intent intent = new Intent(context, WordSetActivity.class);
                intent.putExtra("wordset_key", wsList.get(position).getId());
                intent.putExtra("allList_key", wsList.get(position).getAllList());
                intent.putExtra("wordset_title", wsList.get(position).getName());
                context.startActivity(intent);
                Log.d("Wordset", "Clicked");
            }
        });
        long time = DB.getCurrentMin();

        long min_diff = time - wsList.get(position).getLastOpen() ;

        if(min_diff <= 604800000L){
            holder.img.setImageResource(R.drawable.ic_green1);
        }
        else if ( min_diff <= 2592000000L ){
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
        AppCompatImageButton delbtn;
        ImageView img;

        public WSViewHolder(View itemView) {
            super(itemView);
            img = (ImageView)itemView.findViewById(R.id.img);
            cv = (CardView) itemView.findViewById(R.id.wordSetCardView);
            wordSet = (TextView) itemView.findViewById(R.id.wordSetName);
            wordSetData = (TextView) itemView.findViewById(R.id.wordSetData);
            delbtn = (AppCompatImageButton) itemView.findViewById(R.id.delwordset);
        }
    }
}