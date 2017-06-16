
<img src="http://biosemantics.github.io/micropie2/MicroPIE_logo.png">
=========================================


MicroPIE 0.1.0 
Introduction 
====================
<B>MicroPIE (Microbial Phenomics Information Extractor)</B>is a text-mining tool that utilizes domain experts' knowledge, NLP (Natural Language Processing) and machine learning techniques (Support Vector Machine) to extract phenotypic (i.e., phenomic) characters and character states from microbial taxonomic descriptions. MicroPIE was developed asis part of an NSF funded research project entitled AVAToL: Next Generation Phenomics for the Tree of Life (NSF DEB- #1208256 , DEB-1208567, DEB-1208534, DEB-1208685, and DBI-1147266).

MicroPIE 0.1.0 (released Apr 25, 2016) can cope with 42 types of phenotypic characters and character states, including mol %G+C, cell shape, cell diameter, cell length, cell width, cell relationships & aggregations, Gram stain type, external features, internal features, motility, pigment compounds, salinity used for growth, NaCl minimum, NaCl optimum, NaCl maximum, pH minimum, pH optimum, pH maximum, temperature minimum, temperature optimum, temperature maximum, aerophilicity, magnesium requirement for growth, vitamins and co-factors used for growth, antibiotic sensitivity, antibiotic resistancet, colony shape , colony margin, colony texture, colony color, fermentation products, other metabolic products, pathogenic phenotype, diseases caused, pathogen target organ, haemolytic & haemadsorption properties, organic compounds used or hydrolyzed, organic compounds not used or not hydrolyzed, inorganic substances used, inorganic substances not used, fermentation substrates used, and fermentation substrates not used.

A online demo MicroPIEWeb using MicroPIE 0.1.0 can be found at: <a href="http://biosemantics.arizona.edu/micropieweb/" title="MicroPIE Web Project">http://biosemantics.arizona.edu/micropieweb/</a>. A perl script MicroPIEDigester (<a href="https://github.com/carrineblank/MicroPIEDigester">https://github.com/carrineblank/MicroPIEDigester</a>) contributed by Carrine E. Blank can convert MicroPIE output to the nexus format for Mesquite (<a href="http://mesquiteproject.org/">http://mesquiteproject.org/</a>)

MicroPIE is a collaborative project created by Jin Mao, Elvis Hsin-Hui Wu, and Hong Cui (University of Arizona), by Lisa R. Moore and Marcia Ackerman (University of Southern Maine), and by Carrine E. Blank (University of Montana).  The authors would like to acknowledge assistance from Gail Gasparich (Towson University), Joan Slonczewski  and Daniel Barich (Kenyon College), Rachel Larson (University of Southern Maine), and Thomas Rodenhausen (University of Arizona).

Publications and Presentations
=================
Mao J, Moore LR, Blank CE, Wu EH-H, Ackerman M, Ranade S and Cui H.  2016.  Microbial Phenomics Information Extractor (MicroPIE): A Natural Language Processing Tool for the Automated Acquisition of Prokaryotic Phenotypic Characters from Text Sources. BMC Bioinformatics, 17 (1), 528.

Moore L, Mao J, Blank C, Ackerman M, Hong Cui.  2016.  Automated Text Mining of Prokaryotic Phenotypic Characters using MicroPIE.  The annual American Society for Microbiology general meeting, June 16-20, Boston, MA.

Mao J, Moore L, Blank C, Cui H.  2016.  An Information Extraction Tool for Microbial Characters.  Poster presented at the iConference 2016, Philadelphia, PA, March 20-23.

Blank CE, Hsin-Hui Wu E, Cui H, Moore LR, Burleigh JG, Liu J, Gasparich GE.  2014.  AVAToL microbial phenomics: an ontology and natural language processing tools to facilitate trait evolution studies for the archaeal domain of life.  Talk given at the Evolution 2014 meeting, Raleigh, NC, June 20-24.

Blank CE, Moore LR, Cui H, Hsin-Hui Wu E, Burleigh G, Liu J, Slonczewski JL, Barich D, Gasparich GE.  2014.  AVAToL microbial phenomics: developing a microbial ontology and natural language processing tools to automate the study of the evolution of microbial traits.  Poster presented at the Joint Aquatic Sciences Meeting, Portland, OR, May 18-23.

Ranade S, Cui H, Moore L, Blank C, Gasparich G, and Burleigh JG.  2013. A preliminary analysis of application of Stanford Parser and OSCAR4 for parsing and annotating microbial descriptions. Talk presented at the iEvoBio conference.

Compile Instruction
====================
MicroPIE is an open source application. The source codes are organized with Maven. The Java sources are under the “src” folder. The “models” folders contains SVM- trained model files, Term Lists and other configuration files. After downloading all the source files, you can place the “models” folder out ofin your IDE(e.g.,Eclipse)  workspace.

You are suggested to use <a href="https://eclipse.org">Eclipse(https://eclipse.org)</a>to compile MicroPIE. After downloading the project, transfer it into a <a href="https://maven.apache.org/">Maven(https://maven.apache.org/)</a> project. You can compile the project into a .jar file.

Usage
====================
If you run MicroPIE using Eclipse, find the Main class, a main function is like this:

Main main = new Main();

args = "-i F:/MicroPIE/datasets/Part_One_111_final -o F:/MicroPIE/ext/craft -m F:/MicroPIE/MicroPIEWEB/models".split("\\s+");
		
main.parse(args);

If you have compiled MicroPIE as a jar file, you can use command line to run MicroPIE as:
	Java –cp micropieproject_lib –jar micropie-0.1.0.jar -i F:/MicroPIE/datasets/Part_One_111_final -o F:/MicroPIE/ext/craft -m F:/MicroPIE/MicroPIEWEB/models

The major parameters are:

-cp: the classpath;  micropieproject_lib should be the folder that contains all the dependency jar libraries.

-jar: the name of the compiled MicroPIE jar file

-i: the folder holding taxonomic descriptions

-o: the output folder to place the generated taxon-by-character matrix

-m: the “models” folder.



Other information
=========
The trained SVM models are in the folder, models\character_model0.1.0.

The Term Lists of characters are in the folder, models\CharacterValueExtractors0.1.0.
