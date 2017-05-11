package edu.arizona.biosemantics.micropie.eval;

public class EvaluationTest {
	
	public static void main(String[] args){
		
		//String comparedCharacterNames ="%G+C|Cell shape|Cell wall|Motility|Pigment compounds|Biofilm formation|Habitat isolated from|NaCl minimum|NaCl optimum|NaCl maximum|pH minimum|pH optimum|pH maximum|Temperature minimum|Temperature optimum|Temperature maximum|Oxygen use|Salinity preference|Host|Symbiotic relationship|Pathogenic|Disease caused|Metabolism (energy & carbon source)|Metabolism (energy & carbon source) NOT used|Fermentation products|Polyalkanoates|Antibiotic sensitivity|Antibiotic resistant";
		//student output
		//String comparedCharacterNames ="%G+C|Cell shape|Gram stain type|Motility|Pigment compounds|Biofilm formation|Habitat isolated from|NaCl minimum|NaCl optimum|NaCl maximum|pH minimum|pH optimum|pH maximum|Temperature minimum|Temperature optimum|Temperature maximum|Aerophilicity|Salinity preference|Host|Symbiotic relationship|Pathogenic|Disease caused|Metabolism (energy & carbon source)|Metabolism (energy & carbon source) NOT used|Fermentation products|Alcohols|Amino acids|Carbohydrates (mono & disaccharides)|Polysaccharides|Fatty acids|Polyalkanoates|Antibiotic sensitivity|Antibiotic resistant";
		
		//micropie 1.5
		//String comparedCharacterNames ="%G+C|Cell shape|Gram stain type|Motility|Pigment compounds|Biofilm formation|Habitat isolated from|NaCl minimum|NaCl optimum|NaCl maximum|pH minimum|pH optimum|pH maximum|Temperature minimum|Temperature optimum|Temperature maximum|Aerophilicity|Salinity preference|Host|Symbiotic relationship|Pathogenic|Disease caused|organic compounds used or hydrolyzed|organic compounds not used or not hydrolyzed|inorganic substances used|inorganic substances not used|fermentation substrates used|fermentation substrates not used|Fermentation products|Alcohols|Amino acids|Carbohydrates (mono & disaccharides)|Polysaccharides|Fatty acids|Polyalkanoates|Antibiotic sensitivity|Antibiotic resistant";
		//58 character version
		/**/
		String comparedCharacterNames ="%g+c|cell shape|cell diameter|cell length|cell width|"
				+ "cell relationships&aggregations|gram stain type|cell membrane & cell wall components|external features|"
				+ "internal features|motility|pigment compounds|biofilm formation|filterability|lysis susceptibility|cell division pattern & reproduction|"
				+ "habitat isolated from|salinity preference|nacl minimum|nacl optimum|nacl maximum|ph minimum|"
				+ "ph optimum|ph maximum|temperature minimum|temperature optimum|temperature maximum|"
				+ "pressure preference|aerophilicity|magnesium requirement for growth|vitamins and cofactors required for growth|"
				+ "geographic location|antibiotic sensitivity|antibiotic resistant|antibiotic production|"
				+ "colony shape|colony margin|colony texture|colony color|film test result|spot test result|"
				+ "fermentation products|methanogenesis products|other metabolic product|tests positive|"
				+ "tests negative|symbiotic relationship|host|pathogenic|disease caused|pathogen target organ|haemolytic&haemadsorption properties|"
				+ "organic compounds used or hydrolyzed|organic compounds NOT used or NOT hydrolyzed|inorganic substances used|inorganic substances not used|fermentation substrates used|fermentation substrates not used";
		
		String categoryMappingFile = "F:/MicroPIE/micropieInput/svmlabelandcategorymapping/categoryMapping_MicroPIE1.5.txt";
		
		String charValEvalResultFile = "F:/MicroPIE/evaluation/output/stu/charValEvalResult.csv";
		String charAllEvalResultFile = "F:/MicroPIE/evaluation/output/stu/charAllEvalResult.csv";
		String taxonEvalResultFile = "F:/MicroPIE/evaluation/output/stu/taxonEvalResult.csv";
		String matrixEvalResultFile = "F:/MicroPIE/evaluation/output/stu/matrixEvalResult.csv";
		
		/*
		String charValEvalResultFile = "F:/MicroPIE/evaluation/output/charValEvalResult_58.csv";
		String charAllEvalResultFile = "F:/MicroPIE/evaluation/output/charAllEvalResult_58.csv";
		String taxonEvalResultFile = "F:/MicroPIE/evaluation/output/taxonEvalResult_58.csv";
		String matrixEvalResultFile = "F:/MicroPIE/evaluation/output/matrixEvalResult_58.csv";
		*/
		
		String gstBasicFields = "Taxon|XML file|Genus|Species|Strain";
		String gstKeyField = "Taxon";
		String gstXMLField = "XML file";
		//String gstMatrixFile ="F:/MicroPIE/evaluation/GSM_MicroPIE1.5_010316_111_final.csv";//Gold_matrix_22_1213.csv.csv";
		String gstMatrixFile ="F:\\MicroPIE\\manuscript\\results\\GSM_MicroPIE1.5_after_MS_0208.csv";
		//gstMatrixFile
		//Taxon	Family	Genus	Species	Strain	16S rRNA accession #	XMsL file
		String tgBasicFields = "Taxon|XML file|Genus|Species|Strain";
		String tgKeyField = "Taxon";
		String tgXMLField = "XML file";
		//String tgMatrixFile ="F:/MicroPIE/ext/final111/PartOne111_NOSVM_matrix.csv";//final_114_1214.csv
		String tgMatrixFile ="F:/MicroPIE/ext/studentoutput/Microbial Phenomics Project Data Form experiment output 031715_filename&taxon_stu344.csv";//student output
		//tgMatrixFile
		//String tgBasicFields = "XML file|Taxon|Genus|Species|Strain";//STUEXP_040214_28ch.csv
		//String tgKeyField = "XML file";
		//String tgMatrixFile ="F:/MicroPIE/evaluation/STUEXP_040214_28ch.csv";
		
		ExtractionEvaluation extEval = new ExtractionEvaluation(gstBasicFields,tgBasicFields,gstKeyField,tgKeyField,gstXMLField,tgXMLField,categoryMappingFile);
		extEval.readCategoryMapping();
		extEval.setComparedCharacter(comparedCharacterNames);
		extEval.setSeparators("#", ",");
		
		extEval.evaluate(gstMatrixFile, tgMatrixFile);
		extEval.outputResults(charValEvalResultFile,charAllEvalResultFile,taxonEvalResultFile,matrixEvalResultFile);
	}

}
