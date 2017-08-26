package com.example.fahim.gremate;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.fahim.gremate.Adapters.FriendAdapter;
import com.example.fahim.gremate.Adapters.FriendNotfAdapter;
import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.Friend;
import com.example.fahim.gremate.DataClasses.FriendNotf;
import com.example.fahim.gremate.DataClasses.UserData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class FriendActivity extends NavDrawerActivity {

    private RecyclerView friendsRV;
    private RecyclerView friendNotfsRV;

    private DatabaseReference ref1;
    private DatabaseReference ref2;
    private DatabaseReference ref3;
    private Query query2;
    private ValueEventListener listener1;
    private ValueEventListener listener2;
    private ValueEventListener listener3;

    private ArrayList<Friend> friends;
    private ArrayList<FriendNotf> friendNotfs;

    private ProgressBar loadFrndsPB;
    private ProgressBar loadFrndsNotfPB;

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        setupNavDrawerClick();

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        friendsRV = (RecyclerView) findViewById(R.id.friendsRV);
        friendNotfsRV = (RecyclerView) findViewById(R.id.friendsNotfRV);

        friendsRV.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        friendsRV.setLayoutManager(llm);

        friendNotfsRV.setHasFixedSize(true);
        LinearLayoutManager llm1 = new LinearLayoutManager(this);
        friendNotfsRV.setLayoutManager(llm1);

        loadFrndsNotfPB = (ProgressBar) findViewById(R.id.loadFriendsNotfRV);
        loadFrndsPB = (ProgressBar) findViewById(R.id.loadFriendsRV);

        setFriends();
        setFriendNotfs();

        setTitle("Friends");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(ref1!=null)ref1.removeEventListener(listener1);
        if(ref3!=null)ref3.removeEventListener(listener3);
    }

    private void setFriendNotfs(){

        ref3 = FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid)
                .child(DB.FRIENDNOTF);

        listener3 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friendNotfs = new ArrayList<>();
                ArrayList<String> keys = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    FriendNotf fn = ds.getValue(FriendNotf.class);
                    friendNotfs.add(fn);
                    keys.add(ds.getKey());
                    Log.d(fn.getValue(), ds.getKey());
                }
                Collections.reverse(friendNotfs);
                FriendNotfAdapter adapter = new FriendNotfAdapter(friendNotfs, keys, uid, FriendActivity.this);
                friendNotfsRV.setAdapter(adapter);
                Log.d(dataSnapshot.getKey(), "Friend activity");
                loadFrndsNotfPB.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {           }
        };
        ref3.addValueEventListener(listener3);
    }

    private void setFriends(){

        ref1 = FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid)
                .child(DB.FRIEND);

        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friends = new ArrayList<>();
                ArrayList<String> keys = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Friend f = ds.getValue(Friend.class);
                    friends.add(f);
                    keys.add(ds.getKey());
                    Log.d(f.getId(), f.getName());
                }
                FriendAdapter adapter = new FriendAdapter(friends, keys, uid, FriendActivity.this);
                friendsRV.setAdapter(adapter);
                Log.d(dataSnapshot.getKey(), "Friend activity");
                loadFrndsPB.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {           }
        };
        ref1.addValueEventListener(listener1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.friend_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addFriend:
                AlertDialog.Builder builder = new AlertDialog.Builder(FriendActivity.this);
                builder.setTitle("Add friend");

                final EditText input = new EditText(FriendActivity.this);
                input.setHint("Email address");

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email = input.getText().toString();
                        addFriend(email);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void addFriend(final String email){
        Log.d("FriendActivity ", email);
//        query2 = FirebaseDatabase.getInstance().getReference().child(DB.USER_DATA)
//                .orderByChild("userEmail").equalTo(email);


        ref2 = FirebaseDatabase.getInstance().getReference().child(DB.USER_DATA);
        listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean flag = true;
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    UserData u = ds.getValue(UserData.class);
                    if(u.getUserEmail().equals(email)){
                        String id = ds.getKey();

                        Friend f = new Friend(u.getUserName(), id);
                        FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid).child(DB.FRIEND)
                                .push().setValue(f);

                        Toast.makeText(FriendActivity.this, "Friend added", Toast.LENGTH_SHORT).show();

                        flag = false;
                    }
                }

                if(flag)Toast.makeText(FriendActivity.this, "Email doesn't exist", Toast.LENGTH_SHORT).show();

                ref2.removeEventListener(listener2);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref2.addValueEventListener(listener2);
    }

    private void setupNavDrawerClick(){
        NavigationView mNavigationView = (NavigationView)findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Intent intent;
                switch(menuItem.getItemId()){
                    case R.id.nav_learn:
                        intent = new Intent(FriendActivity.this, LearnActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_signout:
                        new AlertDialog.Builder(FriendActivity.this)
                                .setTitle("Confirm Sign Out")
                                .setMessage( "Are you sure you want to sign out?")
                                .setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(FriendActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                }).show();
                        break;
                    case R.id.nav_search:
                        intent = new Intent(FriendActivity.this, SearchActivity.class);
                        FriendActivity.this.startActivity(intent);
                        break;
                    case R.id.nav_exercise:
                        intent = new Intent(FriendActivity.this, PracticeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_friend:
                        intent = new Intent(FriendActivity.this, FriendActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mDrawerLayout.isDrawerOpen(Gravity.LEFT))
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            else {
                new AlertDialog.Builder(FriendActivity.this)
                        .setTitle("Close")
                        .setMessage("Are you sure you want to close GREMate?")
                        .setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finishAffinity();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).show();
            }
        }
        return true;
    }


}