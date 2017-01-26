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
import com.example.fahim.gremate.DataClasses.Friend;
import com.example.fahim.gremate.DataClasses.FriendNotf;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordListwID;
import com.example.fahim.gremate.DataClasses.WordSetwID;
import com.example.fahim.gremate.DataClasses.WordwID;
import com.example.fahim.gremate.EditActivity;
import com.example.fahim.gremate.R;
import com.example.fahim.gremate.ShowWordActivity;
import com.example.fahim.gremate.WordSetActivity;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;


public class FriendNotfAdapter extends RecyclerView.Adapter<FriendNotfAdapter.FriendNotfViewHolder> {

    private ArrayList<FriendNotf> friendNotfs;
    private ArrayList<String> keys;
    private Context context;
    private String uid;
    public FriendNotfAdapter(ArrayList<FriendNotf> friendNotfs, ArrayList<String> keys, String uid, Context context) {
        this.friendNotfs = friendNotfs;
        this.keys = keys;
        this.context = context;
        this.uid = uid;
    }

    @Override
    public FriendNotfViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friendcard, parent, false);
        FriendNotfViewHolder wv = new FriendNotfViewHolder(v);
        return wv;
    }

    @Override
    public void onBindViewHolder(FriendNotfViewHolder holder, final int position) {
        holder.value.setText(friendNotfs.get(position).getValue());
        holder.delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("Confirm remove")
                        .setMessage("Are you sure you want to remove this notification?")
                        .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid)
                                        .child(DB.FRIENDNOTF).child(keys.get(position)).setValue(null);
                                Toast.makeText(context,
                                        "Removed", Toast.LENGTH_LONG).show();
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
                new AlertDialog.Builder(context)
                        .setTitle("Notification")
                        .setMessage(friendNotfs.get(position).getValue())
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendNotfs.size();
    }


    public static class FriendNotfViewHolder extends RecyclerView.ViewHolder {

        TextView value;
        AppCompatImageButton delBtn;
        CardView cv;

        public FriendNotfViewHolder(View itemView) {
            super(itemView);
            value = (TextView) itemView.findViewById(R.id.value);
            delBtn = (AppCompatImageButton) itemView.findViewById(R.id.delBtn);
            cv = (CardView) itemView.findViewById(R.id.friendCardView);
        }
    }
}
