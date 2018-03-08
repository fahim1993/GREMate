package com.example.fahim.gremate.DataClasses;

/**
 * Created by Fahim on 08-Mar-18.
 */

public class Highlighter {
    public static String highlight(String textRaw, String word){
        StringBuilder sb = new StringBuilder();

        String text = textRaw.toLowerCase();
        word = word.toLowerCase();
        int i=0, j=0, pl1 = 0;
        if(word.length()>3 && word.substring(word.length()-3, word.length()).equals("ing")) pl1 = 3;
        if(word.length()>2 && word.substring(word.length()-2, word.length()).equals("ed")) pl1 = 2;
        if(word.length()>1 && word.substring(word.length()-1, word.length()).equals("s")) pl1 = 1;
        if(word.length()>2 && word.substring(word.length()-2, word.length()).equals("es")) pl1 = 2;
        if(word.length()>2 && word.substring(word.length()-2, word.length()).equals("ie")) pl1 = 1;

        while (j < text.length()) {
            while (j < text.length() && text.charAt(j) != ' ' && text.charAt(j) != ','
                    && text.charAt(j) != '.'  && text.charAt(j) != '\n'
                    && text.charAt(j) != '!'  && text.charAt(j) != '?'
                    && text.charAt(j) != ';'  && text.charAt(j) != '"'
                    && text.charAt(j) != '\''  && text.charAt(j) != '’') j++;

            int it = i;
            while (it < j && text.charAt(it) != word.charAt(0)) it++;

            if (it == j) {
                sb.append(textRaw.substring(i, j));
            } else {
                int jt = it, k = 0;
                while (jt < j && k < word.length() && text.charAt(jt) == word.charAt(k)) {
                    k++;
                    jt++;
                }

                System.out.println("K: " + k + ";  TWordLen: " + (j-i) + ";  WordLen: " + word.length());

                int pl2 = 0;
                if(j-i>3 && text.substring(j-3, j).equals("ing")) pl2 = 3;
                if(j-i>2 && text.substring(j-2, j).equals("ed")) pl2 = 2;
                if(j-i>1 && text.substring(j-1, j).equals("s")) pl2 = 1;
                if(j-i>2 && text.substring(j-2, j).equals("es")) pl2 = 2;
                if(j-i>2 && text.substring(j-2, j).equals("ie")) pl2 = 1;

                if (k>=1 && word.length() - k <= 1+pl1 && j-i-k <= 1+pl2 ) {
                    int flag1 = 0;

                    if (textRaw.charAt(i) == '"' || textRaw.charAt(i) == '‘' ||
                            textRaw.charAt(i) == '\'') flag1 = 1;

                    if (flag1 == 1) sb.append(textRaw.charAt(i));
                    sb.append("<b>");
                    sb.append(textRaw.substring(i + flag1, j));
                    sb.append("</b>");

                } else {
                    sb.append(textRaw.substring(i, j));
                }
            }
            if (j < text.length()) sb.append(textRaw.charAt(j));
            i = ++j;
        }
        return sb.toString();
    }
}
