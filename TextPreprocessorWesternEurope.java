/*  Copyright (C) <2013>  University of Massachusetts Amherst

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/ 
/*
 * @author Ismet Zeki Yalniz
 * March 18,2013
 * U.Massachusetts-Amherst
 * zeki@cs.umass.edu
 */

import java.util.Locale;

public class TextPreprocessorWesternEurope extends TextPreprocessor{

    public TextPreprocessorWesternEurope(){
         locale = new Locale("en", "US"); // ?
    }

    public String processText(String s) {

        char[] charAr = s.toCharArray();
        char[] output = new char[charAr.length + 1];
        int backIndex = 0;

        for (int i = 0; i < charAr.length; i++) {
            char ch = charAr[i];

            // connect phonemes of words which are seperated by a dash
            // -------------------------------------------------------------
            // MERGE HYPHENATED WORDS:
            if (ch == '-' && i < (charAr.length - 1)) {

                // CASE1: check if characters on the left and right hand side are valid, if so merge.
                if( isValidChar(charAr[i+1]) && i >= 1 && isValidChar(charAr[i-1]) ){
                     // i = j-1;
                     continue;
                }

                // CASE2: Merge hyphenated words at the end of each line.
                // Regular exp: HYPHEN (SPACE|TAB)* (NEWLINE|RETURN)
                int j = i + 1;
                while (j < charAr.length && (charAr[j] == ' ' || charAr[j] == '\t')) {
                    j++;
                }
                if ( j == charAr.length){
                    break;
                }
                if (charAr[j] == '\n' || charAr[j] == '\r') {
                //    j++;
                    i = j;
                    continue;
                }
                //i = j-1;
            }

            // always keep apostrophy if it is between two chars
            if (ch == '\'') {
                if ( i > 0 && (i+1) < charAr.length && isValidChar(charAr[i-1]) && isValidChar(charAr[i+1]) ){
                    output[backIndex] = ch;
                    backIndex ++;
                    continue;
                }
            }

            // output the char
            if (isValidChar(ch)) {
                output[backIndex] = ch;
            } else {
                output[backIndex] = ' ';
            }
            backIndex++;
        }
        output[backIndex] = '\0';
        return (new String(output).substring(0, backIndex));
    }

    public boolean isValidChar(char a){

        if (a >= 'a' && a <= 'z')  {
            return true;
        }else if (a >= 'A' && a <= 'Z'){
            return true;
        }else if (a >= '\u00C0' && a <= '\u00FF'){
            return true;
        }else if (!ELIMINATE_NUMERIC_CHARS && (a >= '0' && a <= '9')){
            return true;
        }else if (a == '\u0152' || a == '\u0153'){ // OE oe
            return true;
        }
        return false;
    }
/* // debugging and testing
    public static void main(String [] arg){
        // test for upper and lower case chars
        char a = '\u00C0';
        String str = ""+a;
        for ( int i = 0; i < 64; i++){
             System.out.println(a+i);
             str += (char)(a+i);
        }
        str += "\u0152\u0153";
        System.out.println(str);
        System.out.println(str.toLowerCase(Locale.GERMAN));
        System.out.println(str.toUpperCase(Locale.GERMAN)); // works
    } */
}
