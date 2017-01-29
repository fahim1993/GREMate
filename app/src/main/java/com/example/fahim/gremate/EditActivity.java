package com.example.fahim.gremate;

import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.Sentence;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordAllData;
import com.example.fahim.gremate.DataClasses.WordData;
import com.example.fahim.gremate.DataClasses.WordDef;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EditActivity extends AppCompatActivity {

    private String wordId;
    private int dummyHeight;
    private int delbtnSize;

    private WordAllData wordAllData;

    private ArrayList<SentenceView> sentenceViews;
    private int sentenceViewID;
    private ArrayList<DefinitionView> definitionViews;
    private int definitionViewID;
    private EditText description;
    private EditText mnemonic;

    DatabaseReference ref1;
    Query query2;
    Query query3;
    Query query4;
    ValueEventListener listener1;
    ValueEventListener listener2;
    ValueEventListener listener3;
    ValueEventListener listener4;

    LinearLayout defsLL;
    LinearLayout desLL;
    LinearLayout senLL;
    LinearLayout mnLL;
    LinearLayout ll1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        wordAllData = new WordAllData();


        Bundle bundle = getIntent().getExtras();
        wordId = bundle.getString("word_id");
        Log.d("ID", wordId);

        final float scale = this.getResources().getDisplayMetrics().density;
        delbtnSize = (int) (45 * scale);

        sentenceViewID = 0;
        sentenceViews = new ArrayList<>();
        definitionViews = new ArrayList<>();

        definitionViewID = 100000;
        dummyHeight = 65;

        defsLL = (LinearLayout) findViewById(R.id.WordOperationLLDef);
        senLL = (LinearLayout) findViewById(R.id.WordOperationLLSen);
        desLL = (LinearLayout) findViewById(R.id.WordOperationLLDes);
        mnLL = (LinearLayout) findViewById(R.id.WordOperationLLMN);
        ll1 = (LinearLayout) findViewById(R.id.WordOperationLL);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editWordSetup();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(listener1!=null)ref1.removeEventListener(listener1);
        if(listener2!=null)query2.removeEventListener(listener2);
        if(listener3!=null)query3.removeEventListener(listener3);
        if(listener4!=null)query4.removeEventListener(listener4);
    }

    public void editWordSetup() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref1 = FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid).child(DB.WORD).child(wordId);
        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wordAllData.setWord(dataSnapshot.getValue(Word.class));
                TextView wrd = (TextView) findViewById(R.id.WordOperationWord);
                wrd.setText(wordAllData.getWord().getValue().toUpperCase());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref1.addValueEventListener(listener1);

        query2 = FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid).child(DB.WORDDATA).orderByChild("word").equalTo(wordId);
        listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 0)return;
                for(DataSnapshot ds :dataSnapshot.getChildren()){
                    WordData wd = ds.getValue(WordData.class);
                    wordAllData.setWordData(wd);
                    if (wordAllData.getWordData().getDes().length() > 0) {
                        desLL.addView(addDescLL(true, wordAllData.getWordData().getDes()));
                    }
                    if (wordAllData.getWordData().getMn().length() > 0) {
                        mnLL.addView(addMnLL(true, wordAllData.getWordData().getMn()));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        query2.addValueEventListener(listener2);

        query3 = FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid).child(DB.WORDDEF).orderByChild("word").equalTo(wordId);
        listener3 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 0)return;
                ArrayList<WordDef> wordDefs = new ArrayList<WordDef>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordDef w = ds.getValue(WordDef.class);
                    wordDefs.add(w);
                }
                wordAllData.setWordDefs(wordDefs);
                if (wordDefs.size() > 0) {
                    for (int i = 0; i < wordDefs.size(); i++) {
                        LinearLayout ll = addDefiLL(true, wordDefs.get(i));
                        defsLL.addView(ll, defsLL.getChildCount() - 1);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        query3.addValueEventListener(listener3);

        query4 = FirebaseDatabase.getInstance().getReference().child(DB.USER_WORD).child(uid).child(DB.SENTENCE).orderByChild("word").equalTo(wordId);
        listener4 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 0)return;
                ArrayList<Sentence> sentences = new ArrayList<Sentence>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Sentence w = ds.getValue(Sentence.class);
                    sentences.add(w);
                }
                wordAllData.setSentences(sentences);
                if (sentences.size() > 0) {
                    for (int i = 0; i < sentences.size(); i++) {
                        LinearLayout ll = addSentLL(true, sentences.get(i));
                        senLL.addView(ll, senLL.getChildCount() - 1);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        query4.addValueEventListener(listener4);

    }


    public void addDesc(View v) {
        LinearLayout llp = (LinearLayout) v.getParent();
        llp.addView(addDescLL(false, null), llp.getChildCount() - 1);
    }

    private LinearLayout addDescLL(boolean flg, String des) {

        LinearLayout ll = new LinearLayout(EditActivity.this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tvDesc = new TextView(EditActivity.this);
        tvDesc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvDesc.setText("Description");
        EditText edDesc = new EditText(EditActivity.this);
        edDesc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        if (flg) edDesc.setText(des);

        ImageButton delBtn = new ImageButton(EditActivity.this);
        delBtn.setImageResource(R.drawable.deltbtn);
        delBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        delBtn.setLayoutParams(new LinearLayout.LayoutParams(delbtnSize, delbtnSize));

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LinearLayout) v.getParent()).setVisibility(View.GONE);
                description = null;
                Button btnDes = (Button) findViewById(R.id.WordOperationBtnDes);
                btnDes.setVisibility(View.VISIBLE);
            }
        });

        Button btnDes = (Button) findViewById(R.id.WordOperationBtnDes);
        btnDes.setVisibility(View.GONE);

        description = edDesc;

        ll.addView(tvDesc);
        ll.addView(edDesc);
        ll.addView(delBtn);

        return ll;
    }

    public void addMn(View v) {
        LinearLayout llp = (LinearLayout) v.getParent();
        llp.addView(addMnLL(false, null), llp.getChildCount() - 1);
    }

    private LinearLayout addMnLL(boolean flg, String mn) {

        LinearLayout ll = new LinearLayout(EditActivity.this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tvDesc = new TextView(EditActivity.this);
        tvDesc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvDesc.setText("Mnemonic");
        EditText edMn = new EditText(EditActivity.this);
        edMn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        if (flg) edMn.setText(mn);

        ImageButton delBtn = new ImageButton(EditActivity.this);
        delBtn.setImageResource(R.drawable.deltbtn);
        delBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        delBtn.setLayoutParams(new LinearLayout.LayoutParams(delbtnSize, delbtnSize));

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LinearLayout) v.getParent()).setVisibility(View.GONE);
                mnemonic = null;
                Button btnDes = (Button) findViewById(R.id.WordOperationBtnDes);
                btnDes.setVisibility(View.VISIBLE);
            }
        });

        Button btnMn = (Button) findViewById(R.id.WordOperationBtnMN);
        btnMn.setVisibility(View.GONE);

        mnemonic = edMn;

        ll.addView(tvDesc);
        ll.addView(edMn);
        ll.addView(delBtn);

        return ll;
    }


    public void addDefi(View v) {
        LinearLayout llp = (LinearLayout) v.getParent();
        llp.addView(addDefiLL(false, null), llp.getChildCount() - 1);
    }

    private LinearLayout addDefiLL(boolean flg, WordDef defi) {

        DefinitionView dv = new DefinitionView();

        LinearLayout ll = new LinearLayout(EditActivity.this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        View v = new View(EditActivity.this);
        v.setLayoutParams(new ActionBar.LayoutParams(0, dummyHeight));

        TextView tvTitle = new TextView(EditActivity.this);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvTitle.setText("Title");
        EditText edTitle = new EditText(EditActivity.this);
        edTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        if (flg) edTitle.setText(defi.getTitle());

        TextView tvDef = new TextView(EditActivity.this);
        tvDef.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvDef.setText("Definition");
        EditText edDef = new EditText(EditActivity.this);
        edDef.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        if (flg) edDef.setText(defi.getDef());

        TextView tvSyn = new TextView(EditActivity.this);
        tvSyn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvSyn.setText("Synonyms");
        EditText edSyn = new EditText(EditActivity.this);
        edSyn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        if (flg) edSyn.setText(defi.getSyn());

        TextView tvAnt = new TextView(EditActivity.this);
        tvAnt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvAnt.setText("Antonyms");
        EditText edAnt = new EditText(EditActivity.this);
        edAnt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        if (flg) edAnt.setText(defi.getAnt());

        ImageButton delBtn = new ImageButton(EditActivity.this);
        delBtn.setImageResource(R.drawable.deltbtn);
        delBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        delBtn.setLayoutParams(new LinearLayout.LayoutParams(delbtnSize, delbtnSize));

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId(), i;
                LinearLayout llp = (LinearLayout) v.getParent();
                llp.setVisibility(View.GONE);

                for (i = 0; i < definitionViews.size(); i++) {
                    if (definitionViews.get(i).slNo == id) break;
                }
                if (i != definitionViews.size()) definitionViews.remove(i);

                if (definitionViews.size() == 0) {
                    Button b = (Button) findViewById(R.id.WordOperationBtnDef);
                    b.setText("ADD DEFINITION");
                }
            }
        });

        dv.edTitle = edTitle;
        dv.edDef = edDef;
        dv.edSyn = edSyn;
        dv.edAnt = edAnt;

        Button b = (Button) findViewById(R.id.WordOperationBtnDef);
        b.setText("ADD ANOTHER DEFINITION");

        delBtn.setId(definitionViewID);
        dv.slNo = definitionViewID++;

        definitionViews.add(dv);

        ll.addView(v);
        ll.addView(tvTitle);
        ll.addView(edTitle);
        ll.addView(tvDef);
        ll.addView(edDef);
        ll.addView(tvSyn);
        ll.addView(edSyn);
        ll.addView(tvAnt);
        ll.addView(edAnt);
        ll.addView(delBtn);

        return ll;

    }

    public void addSent(View v) {
        LinearLayout llp = (LinearLayout) v.getParent();
        llp.addView(addSentLL(false, null), llp.getChildCount() - 1);
    }

    private LinearLayout addSentLL(boolean flg, Sentence s) {

        SentenceView st = new SentenceView();

        LinearLayout ll = new LinearLayout(EditActivity.this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        View v = new View(EditActivity.this);
        v.setLayoutParams(new ActionBar.LayoutParams(0, dummyHeight));

        TextView tvSent = new TextView(EditActivity.this);
        tvSent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvSent.setText("Sentence");
        EditText edSent = new EditText(EditActivity.this);
        edSent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        if (flg) edSent.setText(s.getValue());

        ImageButton delBtn = new ImageButton(EditActivity.this);
        delBtn.setImageResource(R.drawable.deltbtn);
        delBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        delBtn.setLayoutParams(new LinearLayout.LayoutParams(delbtnSize, delbtnSize));

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId(), i;
                LinearLayout llp = (LinearLayout) v.getParent();
                llp.setVisibility(View.GONE);

                for (i = 0; i < sentenceViews.size(); i++) {
                    if (sentenceViews.get(i).slNo == id) break;
                }
                if (i != sentenceViews.size()) sentenceViews.remove(i);

                if (sentenceViews.size() == 0) {
                    Button b = (Button) findViewById(R.id.WordOperationBtnDef);
                    b.setText("ADD SENTENCE");
                }
            }
        });

        st.edSent = edSent;

        Button b = (Button) findViewById(R.id.WordOperationBtnSen);
        b.setText("ADD ANOTHER SENTENCE");

        delBtn.setId(sentenceViewID);
        st.slNo = sentenceViewID++;
        sentenceViews.add(st);

        ll.addView(v);
        ll.addView(tvSent);
        ll.addView(edSent);
        ll.addView(delBtn);

        return ll;
    }

    public void save(View v) {

        boolean practicable = false;
        for (DefinitionView dv : definitionViews) {
            String df = dv.edDef.getText().toString().replaceAll("\\s","");
            if(df.length()>0)practicable = true;
        }
        if(practicable)saveData();
        else{
            new AlertDialog.Builder(EditActivity.this)
                    .setTitle("Word is not practicable!")
                    .setMessage("Please add a definition of this word to make it practicable.")
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setNegativeButton("Skip", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            saveData();
                        }
                    }).show();
        }


    }

    public void saveData(){

        Toast.makeText(EditActivity.this, "Saved", Toast.LENGTH_SHORT).show();

        boolean practicable = false;
        for (DefinitionView dv : definitionViews) {
            String df = dv.edDef.getText().toString().replaceAll("\\s","");
            if(df.length()>0)practicable = true;
        }

        DB.deleteWord(wordId, " ", false, true);

        ArrayList<WordDef> defs = new ArrayList<>();
        ArrayList<Sentence> sentences = new ArrayList<>();

        for (SentenceView sv : sentenceViews) {
            if (sv.edSent.getText().length() > 0) {
                Sentence s = new Sentence();
                s.setWord(wordId);
                s.setValue(sv.edSent.getText().toString());
                sentences.add(s);
            }
        }
        wordAllData.setSentences(sentences);

        for (DefinitionView dv : definitionViews) {
            if (dv.edTitle.getText().length() > 0 ||
                    dv.edDef.getText().length() > 0 ||
                    dv.edSyn.getText().length() > 0 ||
                    dv.edAnt.getText().length() > 0) {
                WordDef d = new WordDef();
                d.setWord(wordId);
                d.setTitle(dv.edTitle.getText().toString());
                d.setDef(dv.edDef.getText().toString());
                d.setSyn(dv.edSyn.getText().toString());
                d.setAnt(dv.edAnt.getText().toString());
                defs.add(d);
            }
        }
        wordAllData.setWordDefs(defs);
        wordAllData.getWord().setValidity(1);

        WordData wordData = new WordData();
        wordData.setWord(wordId);

        if(description == null)
            wordData.setDes("");
        else
            wordData.setDes(description.getText().toString());

        if(mnemonic == null)
            wordData.setMn("");
        else
            wordData.setMn(mnemonic.getText().toString());


        wordAllData.setWordData(wordData);

        wordAllData.getWord().setPracticable(practicable);

        DB.setWordData(wordAllData, wordId);

        finish();
    }

    private class SentenceView {
        public EditText edSent;
        public int slNo;
    }

    private class DefinitionView {
        public EditText edTitle;
        public EditText edDef;
        public EditText edSyn;
        public EditText edAnt;
        public int slNo;
    }

}
