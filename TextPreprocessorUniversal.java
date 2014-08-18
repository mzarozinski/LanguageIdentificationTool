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


/**
 * This class simply merges hyphenated tokens at the end of each line. It does not filter any punctuation either.
 * @author Zeki
 */

public class TextPreprocessorUniversal extends TextPreprocessor{

    
    public TextPreprocessorUniversal(){
        locale = new Locale("en", "US");
    }

    public String processText(String s) {

        char[] charAr = s.toCharArray();
        char[] output = new char[charAr.length + 1];
        int backIndex = 0;

        for (int i = 0; i < charAr.length; i++) {
            char ch = charAr[i]; 

            // connect phonemes of words which are separated by a dash
            // -------------------------------------------------------------
            // MERGE HYPHENATED WORDS:
            if (ch == '-' && i < (charAr.length - 1)) {

                // CASE: Merge hyphenated words at the end of each line.
                // Regular exp: HYPHEN (SPACE|TAB)* (NEWLINE|RETURN)
                int j = i + 1;
                while (charAr[j] == ' ' || charAr[j] == '\t') {
                    j++;
                }
                if (charAr[j] == '\n' || charAr[j] == '\r') {
                    i = j;
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
        if (ELIMINATE_NUMERIC_CHARS && (a >= '0' && a <= '9')){
            return false;
        }else if ( ELIMINATE_PUNCTUATION && punctuation.indexOf(a) != -1 ) {
            return false;
        }
        return true;
    }
}
