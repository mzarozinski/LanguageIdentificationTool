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
  
 UMASS language identifier tool is designed for estimating the language distribution 
of long noisy texts, such as OCR outputs of scanned book collections. Current version 
works for "english", "french", "german", "spanish", "italian", "latin", "portuguese",
"dutch", "danish" and "swedish". There is also an unknown language field which indicates 
that there exists some text written either in a language which is not listed above or the text 
has OCR errors. In a nut-shell, this is achieved by finding the frequency of top 5 
stopwords in the text and this is done for each all languages. These frequencies are 
later used for estimating the size of the text which can generate this many number of 
stopwords. Each language obtain a score in this way. If there is any remaining portion 
of the text for which we do not know the source language, then that portion is labelled 
as unknown language. Notice that this is different from letter n-gram based approaches. 

 
 This code can be easily extended for other languages by
 1 - writing/using a proper TextPreprocessor object and
 2 - learning a stopword list along with term probabilities for the intended languages.

 This code is still under development. 
 
 Sample command line( See also the main method for sample usage ):
 "LanguageIdentifier /desktop/myfolder/ -o output.txt"

*/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class LanguageIdentifierTool {

    String languages[] = null;
    TextPreprocessor[] tps = null;
    HashMap<String, IndexEntry> hashedStopWords = null;
    ArrayList selectedStopwordLists[] = null;
    NumberFormat ffp = NumberFormat.getInstance(Locale.ENGLISH);
    private static int BEST_K_STOPWORDS = 5;
    double EXPECTED_FREQUENCIES[] = null;
    double STOPWORD_RELATIVE_FREQUENCY_THRESHOLD = 50;
    
    private static String languageAcronyms[] = {"ENG", "FRE", "GER", "SPA", "ITA", "LAT", "POR", "DUT", "DAN", "SWE", "UND"};
    private static String langs[] = {"english", "french", "german", "spanish", "italian", "latin", "portuguese", "dutch", "danish", "swedish"};

    public static String USAGE =
            "USAGE: LanguageIdentifierTool <inputFileORfolderName> <options>\n"
            + "\nPARAMETERS:\n"
            + "<inputFileORfolderName>\n\t"
            + "full path for input text file or folder name. If the input filename is a folder, all the files in the folder are processed RECURSIVELY."
            + "\nOPTIONAL:\n"
            + "-o <outputfilename>\n\tappend the output to the specified file(default: screen output)\n"
            + "\nSAMPLE USAGE(s):\n"
            + "LanguageIdentifier /desktop/myfolder/ -o output.txt\n"
            + "SAMPLE OUTPUT:\n/desktop/myfolder/myfile.txt	eng 0.7%	ger 97.4%	fre 0.1% ... \n";
            
    public static void main(String args[]) {

        int argc = args.length;
        String outfile = null, listFile = null, folderPath = "";
        String inputFile[] = new String[1];

 
        if (args.length < 1 || args.length > 3) {
            System.out.println(USAGE);
            System.exit(0);
        }

        // parse options
        boolean error = false;
        inputFile[0] = args[0];
        for (int i = 0; i < args.length && !error; i++) {
           /* if (args[i].equalsIgnoreCase("-o")) {
                if ((i + 1) < args.length) {
                    outfile = args[i + 1];
                    i++;
                } else {
                    error = true;
                }
            } else 
            */
            if (args[i].equalsIgnoreCase("-l")) { // list of files
                if ( (i + 1) < args.length ) {
                    listFile = args[i + 1];
                    i++;
                } else {
                    error = true;
                }
            }else if (args[i].equalsIgnoreCase("-f")) { // list of files 
                if ( (i + 1) < args.length ) {
                    folderPath = args[i + 1];
                    i++;
                } else {
                    error = true;
                }
            }
            // else if (args[i].equalsIgnoreCase("-f")) { // working folder
            // }
        }
        if (error) {
            System.out.println(USAGE);
            System.exit(0);
        }

        // initialize language identifier
        LanguageIdentifierTool id = new LanguageIdentifierTool(langs);
       // String result = "";
        
        if (listFile != null){
          // System.out.println("" + listFile);
           id.processFilesInTheList(folderPath, listFile);
        }else{
           id.processFiles(inputFile);   
        }
        // by default, the output is printed on the screen
        // output the results into a file (append to the end)    
    /*    if (outfile != null) {
            FileWriter writer;
            try {
                writer = new FileWriter(new File(outfile), true);
                writer.append(result);
                writer.close();
            } catch (IOException ex) {
                System.out.println("Error: Can not write to the output file:" + outfile);
            }
        }*/
       // System.out.println("Success");
    }

    public LanguageIdentifierTool(String languages[]) {

        this.languages = languages;
        EXPECTED_FREQUENCIES = new double[languages.length];
        ffp.setMaximumFractionDigits(1);
        initializeTextPreprocessors();
        selectedStopwordLists = new ArrayList[languages.length];
        hashedStopWords = new HashMap<String, IndexEntry>(languages.length);
        setStopWordLists(languages);
    }

    private void initializeTextPreprocessors() {
        tps = new TextPreprocessor[languages.length];

      //  TextPreprocessor tpwesteu = new TextPreprocessorWesternEurope();
        TextPreprocessor tpuni = new TextPreprocessorUniversal(); // it does not exclude punctuation marks and uses English locale
        tpuni.ELIMINATE_NUMERIC_CHARS = true;
        tpuni.ELIMINATE_PUNCTUATION = true;
        
        for (int i = 0; i < languages.length; i++) {
            String lang = languages[i].toLowerCase();

         /*   if (TextPreprocessor.isWesternEuropeanLanguage(lang)) {
                tps[i] = tpwesteu;
            } else {
                tps[i] = tpuni;
            }
         */
            tps[i] = tpuni; // this helps determine the underdetermined text which includes characters in other scripts          
            
        }
        sortLanguagesBasedOnTextPrerocessors();
    }
    // Simply groups languages which use the same textprocessor in the list.
    // Redundant text preprocessor calls are avoided in countStopWords() method.

    private void sortLanguagesBasedOnTextPrerocessors() {
        TextPreprocessor updatedTps[] = new TextPreprocessor[languages.length];
        String updatedLan[] = new String[languages.length];
        boolean isListed[] = new boolean[languages.length];
        int cur_index = 0;
        for (int i = 0; i < languages.length; i++) {
            if (isListed[i]) {
                continue;
            } else {
                // first list the text preprocessor itself
                TextPreprocessor cur = tps[i];
                updatedTps[cur_index] = cur;
                updatedLan[cur_index] = languages[i];
                isListed[i] = true;
                cur_index++;

                // then mark all text preprocessors in the list
                for (int j = 0; j < languages.length; j++) {
                    if (j != i) {
                        if (!isListed[j] && cur == tps[j]) {
                            isListed[j] = true;
                            updatedTps[cur_index] = cur;
                            updatedLan[cur_index] = languages[j];
                            cur_index++;
                        }
                    }
                }
            }
        }
        tps = updatedTps;
        languages = updatedLan;
    }

    private long countStopwordsForLanguage(int lan_index) {
        long total = 0;
        ArrayList<IndexEntry> curlist = selectedStopwordLists[lan_index];
        for (int j = 0; j < curlist.size(); j++) {
            IndexEntry ent = curlist.get(j);
            total += ent.getFrequency();
        }
        // reset the frequencies after using term counts
  /*      for (int i = 0; i < stopWordLists.length; i++) {
         ArrayList<IndexEntry> curlist = stopWordLists[i];
         for (int j = 0; j < curlist.size(); j++) {
         curlist.get(j).setFrequency(0);
         }
         } */
        return total;
    }

    private void setStopWordLists(String languages[]) {
        
        String[] words = {};
        IndexEntry ent;
        double fre = 0.0;
        for (int i = 0; i < languages.length; i++) {  
            if (languages[i].equals("english")) {
                words = new String[]{"the", "and", "to", "that", "he"};
                fre = 0.14472745554974284;
            }else if (languages[i].equals("french")) {
                words = new String[]{"dans", "une", "qu", "pas", "vous"};
                fre = 0.040078403400685025;
            }else if (languages[i].equals("german")) {
                words = new String[]{"und", "zu", "ich", "sie", "nicht"};
                fre = 0.08242893499547081;
            }else if (languages[i].equals("spanish")) {
                words = new String[]{"el", "los", "las", "sus", "este"};
                fre = 0.05689023079830319;
            }else if (languages[i].equals("italian")) {
                words = new String[]{"di", "che", "della", "gli", "io"};
                fre = 0.07018559904753736;
            }else if (languages[i].equals("latin")) {
                words = new String[]{"cum", "quod", "sed", "quae", "quam"};
                fre = 0.027898304433148044;
            }else if (languages[i].equals("portuguese")) {
                words = new String[]{"lat", "mesmo", "em", "uma", "com"};
                fre = 0.039766029237540586;
            }else if (languages[i].equals("dutch")) {
                words = new String[]{"van", "het", "dat", "zijn", "hij"};
                fre = 0.08567563487732092;
            }else if (languages[i].equals("danish")) {
                words = new String[]{"og", "til", "jeg", "ikke", "paa"};
                fre = 0.08666808590191499;
            }else if (languages[i].equals("swedish")) {
                words = new String[]{"och", "jag", "hon", "ett", "hade"};
                fre = 0.06926453228889749;
            }else{
                System.out.println("Unknown language identifier: " + languages[i] + " -> quitting");
                System.exit(0);
            }

            ArrayList<IndexEntry> list = new ArrayList<IndexEntry>();
            for (int j = 0; j < words.length; j++) {
                ent = new IndexEntry(words[j], 0, 0, 1);
                hashedStopWords.put(words[j], ent);
                list.add(ent);
            }
            selectedStopwordLists[i] = list;
            EXPECTED_FREQUENCIES[i] = fre; 
        }

    }

    private double[] countStopWords(File file) {

        double[] ratios = new double[languages.length];
        String text;
        String terms[] = null;
        IndexEntry ent;
        long total;
        
        for (int i = 0; i < languages.length; i++) {

            // preprocess the file: the question is which text preprocessor to use?
            // if the preprocessor object is the same as the one before, then no need to retokenize the file again.
            // PERFORMANCE HINT:
            // Languages using the same TextPreprocessor object must follow each other in the languages list for maximum performance
            // Therefore languages are sorted based on their TextPreprocessors in the constructor

            if (i == 0 || (i > 0 && tps[i - 1] != tps[i])) {

                // preprocess and tokenize the document
                text = TextPreprocessor.readFile(file);
                text = tps[i].processText(text);
                text = tps[i].toLowerCase(text);
                terms = text.split("\\s+");

                // reset frequencies before counting
                Collection<IndexEntry> col = hashedStopWords.values();
                Iterator<IndexEntry> it = col.iterator();
                while (it.hasNext()) {
                    IndexEntry e = it.next();
                    e.setFrequency(0);
                }

                // count stopwords
                for (int j = 0; j < terms.length; j++) {
                    ent = hashedStopWords.get(terms[j]);
                    if (ent != null) {
                        ent.incrementFre();
                    }
                }
            }

            // sum frequencies of stopwords in language i
            total = countStopwordsForLanguage(i);
            ratios[i] = (double) total / (double) terms.length;
        }

        return ratios;
    }

    public double processFile2(File file, double[] distribution) {
        // String result = "";
        //double accuracy = 0;
        double values[] = countStopWords(file);
        //   result += file.getAbsolutePath();
        //  System.out.println("Processing ->" + result);
        double unknownLanguageRatio, total = 0.0;
        //  ESTIMATE THE DISTRIBUTION OF LANGUAGES GIVEN A FILE
        //  double[] distribution = new double[values.length];
        double normalizer = 0; // normalizer constant
        for (int i = 0; i < values.length; i++) {
            // NOTE: if OCR accuracy for a single language is known, then use the following line
            // denom = EXPECTED_FREQUENCIES[i] * OCR_ACCURACY_FOR_LAN[i];
            double denom = EXPECTED_FREQUENCIES[i];
            distribution[i] = values[i] / denom;
            total += distribution[i];
        }
        unknownLanguageRatio = 1.0 - total;
        if (unknownLanguageRatio < 0.0f) {
            unknownLanguageRatio = 0.0f;
        }
        

        // normalize distribution
        total += unknownLanguageRatio;
        for (int i = 0; i < values.length; i++) {
            distribution[i] = distribution[i] / total;
        }
        unknownLanguageRatio = unknownLanguageRatio / total;

        
        // =============================================
        // eliminate languages whose percentage is less than 1 percent and renormalize
        total = 0.0;
        for (int i = 0; i < values.length; i++) {    
           if ( distribution[i] >= 0.01){
                total += distribution[i];
           }else{
               distribution[i] = 0.0;
           }
        }
        if ( unknownLanguageRatio >= 0.01){
            total += unknownLanguageRatio;
        }else{
            unknownLanguageRatio = 0.0;
        }
        
        for (int i = 0; i < values.length; i++) {
            distribution[i] = distribution[i] / total;
        }
        unknownLanguageRatio = unknownLanguageRatio / total;
        // ================================================
        
        return unknownLanguageRatio;
    }

    private String processFile2(File file) {
        String result = "";
        result += file.getAbsolutePath();
        //System.out.println("Processing ->" + result);
        
        // ESTIMATE THE DISTRIBUTION OF LANGUAGES GIVEN A FILE
        double[] distribution = new double[languages.length];
        double unknownLanguageRatio = processFile2(file, distribution);

        // print out the distribution of languages
        for (int i = 0; i < languages.length; i++) {
            result += "\t" + languageAcronyms[i] + "\t" + ffp.format(100*distribution[i]);
        }
        result += "\tUND\t" + ffp.format(100*unknownLanguageRatio) + "\n";

        return result;
    }
    
    private void processFilesInTheList(String folder, String listFileFullPath){
               
        try
        {
            FileInputStream in = new FileInputStream(listFileFullPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
           
            while((strLine = br.readLine())!= null) {
                File file;
               // if ( folder.length() == 0 ){
              //      file = new File(strLine.trim());
               // }else{
                    file = new File(folder + "/" + strLine.trim());
               // }
                
                if (file.exists()){
                    String r = processFile2(file);
                    System.out.print(r);
               }else{
                    System.out.println("File does not exist: " + folder + "/" + strLine.trim());
               }
            }            
        }catch(Exception e){
            System.out.println(e);
        }
    }

    private void processFiles(String list[]) {

      //  StringBuilder results = new StringBuilder();
        for (int i = 0; i < list.length; i++) {
            File file = new File(list[i]);
            if (file.exists()) {
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    for (int j = 0; j < files.length; j++) {
                        if (!files[j].isDirectory()) {
                            String r = processFile2(files[j]);
                            System.out.print(r);
              //              results.append(r); // TODO: append to the output file 
                        }
                    }
                } else {
                    String r =processFile2(file);
                    System.out.print(r);
            //        results.append(processFile2(file));
                }
            } else {
                System.out.println("LanguageIndentifier: Input folder or file \"" + file.getAbsolutePath() + "\" either does not exist");
            }
        }
    //    return results.toString();
    }

    public void printLanguages() {
        System.out.print("SORTED_LANGUAGES -> ");
        for (int j = 0; j < languages.length; j++) {
            System.out.print(languages[j] + ", ");
        }
        System.out.println();
    }

    public class GTentry {

        public String fname = "";
        public String[] langs = null;
        public float[] perc = null;
        String fraktur = null;

        public GTentry(String filename, String[] languages, float[] percentages, String f) {
            fname = filename;
            langs = languages;
            perc = percentages;
            fraktur = f;
        }
    }
}

/*
Stopwords for english
--- word | expected frequency ---
the	0.06682428928279445
and	0.032314900998955974
to	0.025159604222293182
that	0.010381351613933295
he	0.01004730943176594
TOTAL	0.14472745554974284
Stopwords for french
--- word | expected frequency ---
une	0.009734095577936691
qu	0.007966270003393379
dans	0.007779778344088647
elle	0.007464472858253842
vous	0.0071337866170124616
TOTAL	0.040078403400685025
Stopwords for german
--- word | expected frequency ---
und	0.03806021653798042
zu	0.013065608419962904
ich	0.011877237631022733
sie	0.009815381960919639
nicht	0.009610490445585127
TOTAL	0.08242893499547081
Stopwords for spanish
--- word | expected frequency ---
el	0.021820025810112526
los	0.018897067619742303
las	0.009913285220799267
sus	0.0041086565508246424
este	0.0021511955968244456
TOTAL	0.05689023079830319
Stopwords for italian
--- word | expected frequency ---
di	0.03074517084293952
che	0.023980062858497752
della	0.006801430711875009
gli	0.004585744338447103
io	0.004073190295777987
TOTAL	0.07018559904753736
Stopwords for latin
--- word | expected frequency ---
cum	0.006871038711627818
quod	0.0062253497991287135
sed	0.0051174559682100875
quae	0.004981153572608111
quam	0.004703306381573314
TOTAL	0.027898304433148044
Stopwords for portuguese
--- word | expected frequency ---
lat	0.011298412508963695
mesmo	0.009598123224992882
em	0.008566014367346619
com	0.005840953492490063
uma	0.004462525643747328
TOTAL	0.039766029237540586
Stopwords for dutch
--- word | expected frequency ---
van	0.02745503315180158
het	0.023638221530672673
dat	0.012291132038226175
zijn	0.01188007292235163
hij	0.010411175234268861
TOTAL	0.08567563487732092
Stopwords for danish
--- word | expected frequency ---
og	0.03850630172869337
til	0.014560489139068124
jeg	0.014193072907267386
ikke	0.01080744039482116
paa	0.008600781732064956
TOTAL	0.08666808590191499
Stopwords for swedish
--- word | expected frequency ---
och	0.04071238407131619
jag	0.008694772829369701
hon	0.0079324487521805
ett	0.006678114492519695
hade	0.005246812143511397
TOTAL	0.06926453228889749
  
*/