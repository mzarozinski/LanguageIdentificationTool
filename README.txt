UMASS Language Identification Tool for Long Noisy Texts (V1.0) 
Copyright (C) 2013 by the University of Massachusetts at Amherst 
released under the GNU  GPL v3.0 (see GNU_license.txt)

Written by I. Zeki Yalniz
Maintained by Michael Zarozinski (MichaelZ@cs.umass.edu)

ABOUT THE TOOL
==============
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

This code is a prototype and still under development. It can be easily extended for other languages by
1 - writing/using a proper TextPreprocessor object and
2 - learning a stopword list along with term probabilities for the intended languages.

Sample command line( See also the main method for sample usage ):
"LanguageIdentifier /desktop/myfolder/"

ACKNOWLEDGMENT
==============
This software was developed at the Center for Intelligent Information Retrieval (CIIR), University of Massachusetts Amherst.  
Basic research to develop the software was funded by the CIIR and the National Science Foundation while its 
application was supported by a grant from the Mellon Foundation. Any opinions, findings and conclusions or 
recommendations expressed in this material are the authors' and do not necessarily reflect those of the sponsor. 
Please see the GNU licence version 3.0 for copyright issues. 

CONTACT INFORMATION
================================
For further information please contact either 
I. Zeki Yalniz (zeki@cs.umass.edu) or R. Manmatha (manmatha@cs.umass.edu) or info@ciir.cs.umass.edu. 

HOW TO COMPILE:
===============
Inside the source folder, type the following command to compile the code (tested for Java version 1.6):
"javac *.java"

HOW TO USE THE TOOL
===================

1 - COMMAND LINE INTERFACE:
---------------------------
USAGE: LanguageIdentifierTool <inputFileORfolderName>  

PARAMETERS:
<inputFileORfolderName>
	full path for input text file or folder name. If the input filename is a folder, all the files in the folder are processed RECURSIVELY.
 
SAMPLE USAGE(s):
LanguageIdentifier /desktop/myfolder/ 
SAMPLE OUTPUT:
/desktop/myfolder/myfile.txt	eng 0.7%	ger 97.4%	fre 0.1% ... 
 