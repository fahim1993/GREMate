package com.example.fahim.gremate;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.Adapters.WordAdapter;
import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.DBRef;
import com.example.fahim.gremate.DataClasses.ListWithId;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordWithId;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class ListWordsActivity extends NavDrawerActivity {

    private String wsId;
    private String mainListId;
    private String currentListId;
    private String currentListName;

    private ArrayList<ListWithId> otherLists;
    private ArrayList<WordWithId> words;

    private RecyclerView wordsInListRV;
    private LinearLayoutManager llm;

    DatabaseReference ref1;
    private ValueEventListener listener1;

    private WordAdapter rvAdapter;
    private ProgressBar loadWordRV;

    private int sortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_words);

        setupNavDrawerClick();
        getListSortOrder();

        Bundle extras = getIntent().getExtras();
        if (extras == null) finish();

        otherLists = extras.getParcelableArrayList("otherLists");
        currentListId = extras.getString("listId");
        wsId = extras.getString("wsId");
        mainListId = extras.getString("mainListId");
        currentListName = extras.getString("listTitle");

        setTitle(currentListName.toUpperCase());

        wordsInListRV = (RecyclerView) findViewById(R.id.wordInListRV);
        wordsInListRV.setHasFixedSize(true);
        llm = new LinearLayoutManager(this);
        wordsInListRV.setLayoutManager(llm);

        loadWordRV = (ProgressBar) findViewById(R.id.loadWordRV);

        AppCompatImageButton addWord = (AppCompatImageButton) findViewById(R.id.addWordBtn);
        addWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ListWordsActivity.this);
                builder.setTitle("Add Word");

                final EditText input = new EditText(ListWordsActivity.this);

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Word");
                builder.setView(input);

                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String word = input.getText().toString();
                        if (word.length() < 1) {
                            Toast.makeText(ListWordsActivity.this,
                                    "Failed! Word must be at least 1 character long.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ListWordsActivity.this,
                                    word + " added", Toast.LENGTH_SHORT).show();
                            DB.newWord(wsId, currentListId, currentListName, mainListId, word);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        AppCompatImageButton sortBtn = (AppCompatImageButton) findViewById(R.id.sortBtn);
        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ListWordsActivity.this);
                LayoutInflater inflater = (ListWordsActivity.this).getLayoutInflater();

                builder.setTitle("Sort Words");
                final View layout = inflater.inflate(R.layout.ws_sort, null);
                final RadioButton alph = (RadioButton) layout.findViewById(R.id.alphabetical);
                final RadioButton diff = (RadioButton) layout.findViewById(R.id.difficulty);

                final RadioButton asc = (RadioButton) layout.findViewById(R.id.ascending);
                final RadioButton dsc = (RadioButton) layout.findViewById(R.id.descending);

                if (sortOrder == 31) {
                    alph.setChecked(true);
                    asc.setChecked(true);
                } else if (sortOrder == 32) {
                    alph.setChecked(true);
                    dsc.setChecked(true);
                } else if (sortOrder == 41) {
                    diff.setChecked(true);
                    asc.setChecked(true);
                } else if (sortOrder == 42) {
                    diff.setChecked(true);
                    dsc.setChecked(true);
                }

                final int prevSortOrder = sortOrder;

                builder.setView(layout)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if (alph.isChecked() && asc.isChecked()) {
                                    sortOrder = 31;
                                } else if (alph.isChecked() && dsc.isChecked()) {
                                    sortOrder = 32;
                                } else if (diff.isChecked() && asc.isChecked()) {
                                    sortOrder = 41;
                                } else if (diff.isChecked() && dsc.isChecked()) {
                                    sortOrder = 42;
                                }
                                if (prevSortOrder == sortOrder) return;
                                setListSortOrder();
                                sortWords(true);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.create();
                builder.show();
            }
        });

        AppCompatImageButton searchButton = (AppCompatImageButton) findViewById(R.id.searchBtn);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (words == null) return;
                AlertDialog.Builder builder = new AlertDialog.Builder(ListWordsActivity.this);
                builder.setTitle("Search Word");

                final EditText input = new EditText(ListWordsActivity.this);

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Word");
                builder.setView(input);

                builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        char[] word = input.getText().toString().toLowerCase().toCharArray();
                        for (int i = 0; i < words.size(); i++) {
                            char[] listWord = words.get(i).getValue().toLowerCase().toCharArray();
                            if (listWord.length < word.length) continue;
                            boolean match = true;
                            for (int j = 0; j < word.length; j++) {
                                if (word[j] != listWord[j]) {
                                    match = false;
                                    break;
                                }
                            }
                            if (match) {
                                llm.scrollToPositionWithOffset(i, 0);
                                return;
                            }
                        }
                        Toast.makeText(ListWordsActivity.this,
                                input.getText().toString() + " was not found in this list!", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        AppCompatImageButton practiceButton = (AppCompatImageButton) findViewById(R.id.practiceBtn);
        practiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListWordsActivity.this, PracticeActivity.class);
                Bundle b = new Bundle();
                b.putString("wsId", wsId);
                b.putString("listId", currentListId);
                intent.putExtras(b);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        hideWordRv();
        getListWords();
    }

    @Override
    protected void onPause() {
        super.onPause();

        setListState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeListeners();
    }

    private void removeListeners() {
        if (listener1 != null) ref1.removeEventListener(listener1);
    }

    public void getListWords() {
        if (listener1 != null) ref1.removeEventListener(listener1);

        DBRef db = new DBRef();
        ref1 = db.listWordsRef(currentListId);
        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                words = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Word word = ds.getValue(Word.class);
                    WordWithId wordWithId = new WordWithId(word, ds.getKey());
                    words.add(wordWithId);
                }
                sortWords(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref1.addValueEventListener(listener1);
    }

    private void sortWords(boolean resetList) {

        if (words == null) return;
        else if (sortOrder == 31) {
            Collections.sort(words, WordWithId.alphabetical_Asc);
        } else if (sortOrder == 32) {
            Collections.sort(words, WordWithId.alphabetical_Dsc);
        } else if (sortOrder == 41) {
            Collections.sort(words, WordWithId.difficulty_Asc);
        } else if (sortOrder == 42) {
            Collections.sort(words, WordWithId.difficulty_Dsc);
        }

        if (resetList) resetListState();
        else if (rvAdapter != null) setListState();

        rvAdapter = new WordAdapter(words, otherLists, ListWordsActivity.this, wsId, mainListId, currentListId);
        wordsInListRV.setAdapter(rvAdapter);
        restoreListState();

        showWordRv();
    }

    private void setListSortOrder() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putInt(currentListId + "`~", sortOrder).apply();
    }

    private void getListSortOrder() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        sortOrder = prefs.getInt(currentListId + "`~", 31);
    }

    private void resetListState() {
        if (wordsInListRV == null) return;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        preferences.edit().putInt(currentListId, 0).apply();
        preferences.edit().putInt(currentListId + "~~", 0).apply();
    }

    private void setListState() {
        if (wordsInListRV == null) return;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        View firstChild = wordsInListRV.getChildAt(0);
        if (firstChild == null) return;
        int firstVisiblePosition = wordsInListRV.getChildAdapterPosition(firstChild);
        int offset = firstChild.getTop();

        preferences.edit().putInt(currentListId, firstVisiblePosition).apply();
        preferences.edit().putInt(currentListId + "~~", offset).apply();
        setListSortOrder();

    }

    private void restoreListState() {
        if (wordsInListRV == null) return;
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        wordsInListRV.scrollToPosition(preferences.getInt(currentListId, 0));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                wordsInListRV.scrollBy(0, -preferences.getInt(currentListId + "~~", 0));
            }
        }, 5);
    }

    private void hideWordRv() {
        wordsInListRV.setVisibility(View.GONE);
        loadWordRV.setVisibility(View.VISIBLE);
    }

    private void showWordRv() {
        loadWordRV.setVisibility(View.GONE);
        wordsInListRV.setVisibility(View.VISIBLE);
    }

    private void setupNavDrawerClick() {
        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Intent intent;
                switch (menuItem.getItemId()) {
                    case R.id.nav_learn:
                        intent = new Intent(ListWordsActivity.this, WordSetActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_signout:
                        new AlertDialog.Builder(ListWordsActivity.this)
                                .setTitle("Confirm Sign Out")
                                .setMessage("Are you sure you want to sign out?")
                                .setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(ListWordsActivity.this, LoginActivity.class);
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
                        intent = new Intent(ListWordsActivity.this, SearchActivity.class);
                        ListWordsActivity.this.startActivity(intent);
                        break;
                    case R.id.nav_exercise:
                        intent = new Intent(ListWordsActivity.this, PracticeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                }
                return true;
            }
        });
    }
}

































