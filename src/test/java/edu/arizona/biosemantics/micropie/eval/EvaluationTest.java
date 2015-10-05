package edu.arizona.biosemantics.micropie.eval;

public class EvaluationTest {
	
	public static void main(String[] args){
		
		//String comparedCharacterNames ="%G+C|Cell shape|Cell wall|Motility|Pigment compounds|Biofilm formation|Habitat isolated from|NaCl minimum|NaCl optimum|NaCl maximum|pH minimum|pH optimum|pH maximum|Temperature minimum|Temperature optimum|Temperature maximum|Oxygen use|Salinity preference|Host|Symbiotic relationship|Pathogenic|Disease caused|Metabolism (energy & carbon source)|Metabolism (energy & carbon source) NOT used|Fermentation products|Polyalkanoates|Antibiotic sensitivity|Antibiotic resistant";
		String comparedCharacterNames ="%G+C|Cell shape|Cell wall|Motility|Pigment compounds|Biofilm formation|Habitat isolated from|NaCl minimum|NaCl optimum|NaCl maximum|pH minimum|pH optimum|pH maximum|Temperature minimum|Temperature optimum|Temperature maximum|Oxygen use|Salinity preference|Host|Symbiotic relationship|Pathogenic|Disease caused|Metabolism (energy & carbon source)|Metabolism (energy & carbon source) NOT used|Fermentation products|Alcohols|Amino acids|Carbohydrates (mono & disaccharides)|Polysaccharides|Fatty acids|Polyalkanoates|Antibiotic sensitivity|Antibiotic resistant";
		
		String categoryMappingFile = "F:/MicroPIE/micropieInput/svmlabelandcategorymapping_data/categoryMapping_paper.txt";
		
		String charValEvalResultFile = "F:/MicroPIE/evaluation/output/charValEvalResult.csv";
		String charAllEvalResultFile = "F:/MicroPIE/evaluation/output/charAllEvalResult.csv";
		String taxonEvalResultFile = "F:/MicroPIE/evaluation/output/taxonEvalResult.csv";
		String matrixEvalResultFile = "F:/MicroPIE/evaluation/output/matrixEvalResult.csv";
		
		String gstBasicFields = "Taxon|XML file|Genus|Species|Strain";
		String gstKeyField = "XML file";
		String gstMatrixFile ="F:/MicroPIE/evaluation/gold_matrix.csv";
		
		//Taxon	Family	Genus	Species	Strain	16S rRNA accession #	XML file
		String tgBasicFields = "Taxon|XML file|Genus|Species|Strain";
		String tgKeyField = "XML file";
		String tgMatrixFile ="F:/MicroPIE/evaluation/MICROPIE_OUT.csv";
		
		//String tgBasicFields = "XML file|Taxon|Genus|Species|Strain";//STUEXP_040214_28ch.csv
		//String tgKeyField = "XML file";
		//String tgMatrixFile ="F:/MicroPIE/evaluation/STUEXP_040214_28ch.csv";
		
		ExtractionEvaluation extEval = new ExtractionEvaluation(gstBasicFields,tgBasicFields,gstKeyField,tgKeyField,categoryMappingFile);
		extEval.readCategoryMapping();
		extEval.setComparedCharacter(comparedCharacterNames);
		extEval.setSeparators("#", ",");
		extEval.setValueComparator(new StringComparator());
		
		extEval.evaluate(gstMatrixFile, tgMatrixFile);
		extEval.outputResults(charValEvalResultFile,charAllEvalResultFile,taxonEvalResultFile,matrixEvalResultFile);
	}

}
