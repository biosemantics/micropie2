MicroPIE
=========================================


Introduction
====================
MicroPIE (Microbial Phenomics Information Extractor) is a text mining tool that utilizes domain experts' knowledge, NLP and machine learning techniques to extract phenomic characters from microbial descriptions. MicroPIE is part of an NSF funded research project entitled AVAToL: Next Generation Phenomics for the Tree of Life (NSF DEB #1208256).

MicroPIE 0.1.0 can cope with 42 characters, including G+C, Cell shape, Cell diameter, Cell length, Cell width, Cell relationships&aggregations, Gram stain type, External features, Internal features, Motility, Pigment compounds, Salinity preference, NaCl minimum, NaCl optimum, NaCl maximum, pH minimum, pH optimum, pH maximum, Temperature minimum, Temperature optimum, Temperature maximum, Aerophilicity, Magnesium requirement for growth, Vitamins and Cofactors required for growth, Antibiotic sensitivity, Antibiotic resistant, Colony shape , Colony margin, Colony texture, Colony color, Fermentation Products, Other Metabolic Product, Pathogenic, Disease caused, Pathogen target Organ, Haemolytic&haemadsorption properties, organic compounds used or hydrolyzed, organic compounds not used or not hydrolyzed, inorganic substances used, inorganic substances not used, fermentation substrates used, fermentation substrates not used.

Compile Instruction
====================
MicroPIE is an open source application. The source codes are organized with Maven.  The Java sources are under the “src” folder. The “models” folders include SVM trained model files, Term Lists and other configuration files. After download all the source files, you can place the “models” folder out of your IDE workspace.

You are suggested to use Eclipse to compile MicroPIE. After download the project, transfer it into a Maven project. You can compile the project into a jar file.

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
The Term Lists of characters are in the folder, F:\MicroPIE\micropieweb\models\CharacterValueExtractors0.1.0.
