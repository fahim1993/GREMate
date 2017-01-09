package com.example.fahim.gremate.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordListwID;
import com.example.fahim.gremate.DataClasses.WordSetwID;
import com.example.fahim.gremate.DataClasses.WordwID;
import com.example.fahim.gremate.EditActivity;
import com.example.fahim.gremate.R;
import com.example.fahim.gremate.ShowWordActivity;
import com.example.fahim.gremate.WordSetActivity;

import java.util.ArrayList;


public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {


    private ArrayList<WordwID> wordList;
    public ArrayList<WordListwID> otherLists;
    private Context context;
    private String wsId;
    private String allListId;
    private String currentListId;

    public WordAdapter(ArrayList<WordwID> wordList, Context context, String wsId, String allListId, String currentListId) {
        this.wordList = wordList;
        this.context = context;
        this.wsId = wsId;
        this.allListId = allListId;
        this.currentListId = currentListId;
    }

    @Override
    public WordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.wordcard, parent, false);
        WordViewHolder wv = new WordViewHolder(v);
        return wv;
    }

    @Override
    public void onBindViewHolder(WordViewHolder holder, final int position) {
        holder.wordValue.setText(wordList.get(position).getValue().toUpperCase());
        if(wordList.get(position).getCopyOf().length()<1 || currentListId.equals(allListId)) holder.sourceListName.setText(wordList.get(position).getSourceListName());
        else holder.sourceListName.setText(wordList.get(position).getSourceListName() + " (c)");

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ShowWordActivity.class);

                ArrayList<Word> words = new ArrayList<>();
                for (int i=0; i<wordList.size(); i++) {
                    Word w = wordList.get(i).toWord();
                    if(w.getCopyOf().length()<1)w.setCopyOf(wordList.get(i).getId());
                    words.add(w);
                }
                Bundle b = new Bundle();
                b.putParcelableArrayList("words", words);
                b.putInt("index", position);
                intent.putExtra("bundle", b);
                context.startActivity(intent);
            }
        });

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final WordwID word_ = wordList.get(position);
                builder.setTitle(word_.getValue().toUpperCase());
                CharSequence[] listNames;
                if (word_.getCopyOf().length() < 1 || word_.getListId().equals(allListId))
                    listNames = new CharSequence[]{"Add to another list", "Edit", "Delete"};
                else
                    listNames = new CharSequence[]{"Add to another list", "Edit", "Remove from list", "Delete"};

                builder.setItems(listNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            if (otherLists.size() == 0){
                                Toast.makeText(context,
                                        "Please create a new list first!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Select a list");
                            CharSequence[] listNames = new CharSequence[otherLists.size()];
                            for (int j = 0; j < otherLists.size(); j++) {
                                listNames[j] = otherLists.get(j).getName();
                            }
                            builder.setItems(listNames, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    WordwID wid = wordList.get(position);
                                    Word nword = wid.toWord();
                                    if (nword.getCopyOf().length() < 1)
                                        nword.setCopyOf(wid.getId());
                                    nword.setListId(otherLists.get(i).getId());
                                    Toast.makeText(context,
                                            word_.getValue() + " added to " + otherLists.get(i).getName(), Toast.LENGTH_SHORT).show();
                                    DB.addWordToAnotherList(nword);
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                        if(i==1){
                            Intent intent = new Intent(context, EditActivity.class);

                            Bundle b = new Bundle();
                            b.putString("word_id", wordList.get(position).getId());
                            intent.putExtras(b);
                            context.startActivity(intent);
                        }
                        else {
                            if (word_.getCopyOf().length() < 1 || word_.getListId().equals(allListId)) {
                                new AlertDialog.Builder(context)
                                        .setTitle("Confirm Delete")
                                        .setMessage("Are you sure you want to delete this word?")
                                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String wid;
                                                if(word_.getCopyOf().length()<1)wid = word_.getId();
                                                else wid = word_.getCopyOf();

                                                DB.deleteWord(wid, wsId, true, false);
                                                Toast.makeText(context,
                                                        "Deleted " + word_.getValue(), Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                            }
                                        }).show();

                            } else {
                                if (i == 2) {
                                    new AlertDialog.Builder(context)
                                            .setTitle("Confirm Remove")
                                            .setMessage("Are you sure you want to remove this word from this list?")
                                            .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    DB.removeWordClone(word_.getId(), word_.getCopyOf());
                                                    Toast.makeText(context,
                                                            "Removed " + word_.getValue() + " from this list", Toast.LENGTH_LONG).show();
                                                }
                                            })
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                }
                                            }).show();


                                } else {
                                    new AlertDialog.Builder(context)
                                            .setTitle("Confirm Delete")
                                            .setMessage("Are you sure you want to delete this word?")
                                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    DB.deleteWord(word_.getCopyOf(), wsId, true, false);
                                                    Toast.makeText(context,
                                                            "Deleted " + word_.getValue(), Toast.LENGTH_LONG).show();
                                                }
                                            })
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                }
                                            }).show();
                                }
                            }
                        }
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
        long time = DB.getCurrentMin() - wordList.get(position).getLastOpen();
        if (time >= 2592000000L)
            holder.img.setImageResource(R.drawable.ic_gray);
        else if (time >= 604800000L)
            holder.img.setImageResource(R.drawable.ic_green2);
        else
            holder.img.setImageResource(R.drawable.ic_green1);

        if (lvl == 0)
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


    public static class WordViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView wordValue;
        TextView sourceListName;
        AppCompatImageButton moreBtn;
        ImageView img;

        public WordViewHolder(View itemView) {
            super(itemView);

            cv = (CardView) itemView.findViewById(R.id.wordCV);
            wordValue = (TextView) itemView.findViewById(R.id.wordValue);
            sourceListName = (TextView) itemView.findViewById(R.id.sourceListName);
            moreBtn = (AppCompatImageButton) itemView.findViewById(R.id.moreBtn);
            img = (ImageView) itemView.findViewById(R.id.img);
        }
    }
}
