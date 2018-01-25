package com.example.fahim.gremate.DataClasses;

import com.google.firebase.database.Exclude;

/**
 * Created by fahim on 12/23/16.
 */

public class WordDef {
    private String title, def, boldSyns, syns, sentences;

    public WordDef() {
        this.title = "";
        this.def = "";
        this.boldSyns = "";
        this.syns = "";
        this.sentences = "";
    }

    public WordDef(String title, String def, String boldSyns, String syns, String sentences) {
        setTitle(title);
        this.def = def;
        this.boldSyns = boldSyns;
        this.syns = syns;
        this.sentences = sentences;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        title = title.substring(0, 1).toUpperCase() + title.substring(1).toLowerCase();
        this.title = title;
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public String getBoldSyns() {
        return boldSyns;
    }

    public void setBoldSyns(String boldSyns) {
        this.boldSyns = boldSyns;
    }

    public String getSyns() {
        return syns;
    }

    public void setSyns(String syns) {
        this.syns = syns;
    }

    public String getSentences() {
        return sentences;
    }

    public void setSentences(String sentences) {
        this.sentences = sentences;
    }

    @Exclude
    public String[] getSentencesArray(){
        return sentences.split(DB.DELIM);
    }

    @Exclude
    public String[] getBoldSynsArray(){
        return boldSyns.split(DB.DELIM);
    }

    @Exclude
    public String[] getSynsArray(){
        return syns.split(DB.DELIM);
    }

    @Exclude
    public String getFirstHtml(int no){
        StringBuilder sb = new StringBuilder();

        sb.append("<b>");
        sb.append(String.valueOf(no));
        sb.append(". ");
        sb.append(getTitle());
        sb.append("</b><br>");

        sb.append("<i>");
        sb.append(getDef());
        sb.append("</i>");

        if(boldSyns.length()>0){
            sb.append("<br><b>Synonyms:</b> ");
            String [] boldSynsArray = getBoldSynsArray();
            for(int i=0; i<boldSynsArray.length; i++){
                if(i>0) sb.append(", ");
                sb.append(boldSynsArray[i]);
            }
            sb.append(".");
        }

        if(sentences.length()>0){
            sb.append("<br>");
            String [] sents = getSentencesArray();
            sb.append(sents[0]);
        }

        return sb.toString();
    }

    @Exclude
    public String getSecondHtml(){
        StringBuilder sb = new StringBuilder();

        if(syns.length() > 0){
            String [] synsArray = getSynsArray();
            sb.append("<b>More Synonyms:</b> ");
            for(int i=0; i<synsArray.length; i++){
                if(i>0) sb.append(", ");
                sb.append(synsArray[i]);
            }
        }

        String [] sents = getSentencesArray();
        if(sents.length > 1) {
            if(sb.length()>0)sb.append("<br>");
            sb.append("<b>Sentences:</b>");
            for(int i = 1; i< sents.length; i++) {
                sb.append("<br><b>");
                sb.append(i);
                sb.append(". </b>");
                sb.append(sents[i]);
            }
        }

        return sb.toString();
    }

    public boolean haveMoreData(){
        if( (syns!=null && syns.length()>1) || (sentences!=null && sentences.length()>1) ) return true;
        return false;
    }
}
