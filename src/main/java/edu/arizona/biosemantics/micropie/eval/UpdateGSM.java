package edu.arizona.biosemantics.micropie.eval;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.extract.ValueFormatterUtil;
import edu.arizona.biosemantics.micropie.io.CSVTaxonCharacterMatrixWriter;
import edu.arizona.biosemantics.micropie.io.CharacterReader;
import edu.arizona.biosemantics.micropie.io.MatrixReader;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.NewTaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.nlptool.StringUtil;


/**
 * use manual scoring GSM to update the original one
 * @author maojin
 *
 */
public class UpdateGSM {
	protected String[] gstBasicFields;//basic fields used to describe the taxon/taxon file
	protected String gstKeyField;//the field of the unique taxon name
	protected String gstSeparator;
	protected String gstXMLField;
	protected Map<String, ILabel> characterNameLabelMapping;
	protected Map<ILabel, String> characterLabelNameMapping;
	protected String goldStdMatrixFile;
	
	protected String categoryMappingFile; 
	
	protected ILabel[] comparedCharacterLabels;
	protected String[] comparedCharacterNames;
	
	
	
	/**
	 * the gold standard matrix and the target matrix have the same structures
	 * @param gstBasicFields
	 * @param tgBasicFields
	 * @param gstKeyField
	 * @param tgKeyField
	 * @param categoryMappingFile
	 */
	public UpdateGSM(String gstBasicFields,String gstKeyField,String gstXMLField, String categoryMappingFile){
		
		String[] gstFields = gstBasicFields.split("\\|");
		this.gstBasicFields = gstFields;
		this.gstKeyField = gstKeyField;
		this.gstXMLField = gstXMLField;
		this.categoryMappingFile = categoryMappingFile;
	}
	
	
	/**
	 * set the compared characters
	 * @param comparedCharacters
	 */
	public void setComparedCharacter(String comparedCharacters){
		comparedCharacterNames = comparedCharacters.split("\\|");
		comparedCharacterLabels = new ILabel[comparedCharacterNames.length];
		for(int i = 0;i<comparedCharacterNames.length;i++){
			comparedCharacterNames[i] = comparedCharacterNames[i].trim();
			comparedCharacterLabels[i] = this.characterNameLabelMapping.get(comparedCharacterNames[i].toLowerCase());
			//System.out.println(comparedCharacterLabels[i]+"===>"+comparedCharacterNames[i]);
		}
	}

	/**
	 * set the separators for matrixes.
	 * @param gstSeparator
	 * @param tgSeparator
	 */
	public void setSeparators(String gstSeparator){
		this.gstSeparator= gstSeparator;
	}
	
	public void readCategoryMapping(){
		CharacterReader cateReader = new CharacterReader();
		cateReader.setCategoryFile(this.categoryMappingFile);
		cateReader.read();
		this.characterNameLabelMapping = cateReader.getCategoryNameLabelMap();
		this.characterLabelNameMapping = cateReader.getLabelCategoryNameMap();
	}
	
	
	
	/**
	 * update the GSM with manual scoring corrected GSM values
	 * @param orgGSMFile
	 * @param manualScoreFile
	 * @param newGSMFile
	 */
	public void update(String orgGSMFile, String manualScoreFile, String newGSMFile){
		//read the orgGsmFile into a matrix
		//parse gold standard matrix and taget matrix
		MatrixReader gsdMatrixReader = new MatrixReader(gstBasicFields, gstKeyField,gstXMLField, characterNameLabelMapping,characterLabelNameMapping);
		gsdMatrixReader.readMatrixFromFile(orgGSMFile);
		NewTaxonCharacterMatrix gsdMatrix = gsdMatrixReader.parseMatrix(this.gstSeparator, false);


		
		
		//read the manualScoreFile
		NewTaxonCharacterMatrix manualScoreMatrix = parseMatrixFromCSV(manualScoreFile); 
		
		//replace the gsdMatrix with manualScoreMatrix
		for(String taxonFile:(Set<String>)manualScoreMatrix.keySet()){
			System.out.println(taxonFile);
			Map<ILabel, List<CharacterValue>> taxonValues = manualScoreMatrix.getAllTaxonCharacterValues(taxonFile);
			for(ILabel label:taxonValues.keySet() ){
				List<CharacterValue> chacValue = taxonValues.get(label);
				Map<ILabel, List<CharacterValue>> gsmTaxonValues = gsdMatrix.getAllTaxonCharacterValues(taxonFile);
				if(gsmTaxonValues!=null) gsmTaxonValues.put(label, chacValue);
				System.out.println(label+" "+chacValue);
			}
		}
		
		//save as a new file ---- newGSMFile
		String svmLabelAndCategoryMappingFile = "F:/MicroPIE/micropieInput/svmlabelandcategorymapping/categoryMapping_micropie1.5.txt";
		CharacterReader characterReader = new CharacterReader();
		characterReader.setCategoryFile(svmLabelAndCategoryMappingFile);
		characterReader.read();
		
		
		
		LinkedHashSet<ILabel> characterLabels = new LinkedHashSet();
		LinkedHashSet<String> characterNames = new LinkedHashSet();
		for(String characterName : comparedCharacterNames){
			ILabel label = characterReader.getCategoryNameLabelMap().get(characterName.trim().toLowerCase());
			characterLabels.add(label);
			characterNames.add(characterName);
		}
		gsdMatrix.setCharacterLabels(characterLabels);
		gsdMatrix.setCharacterNames(characterNames);
		
		CSVTaxonCharacterMatrixWriter matrixWriter =  new CSVTaxonCharacterMatrixWriter();
		try {
			matrixWriter.setOutputStream(new FileOutputStream(newGSMFile, true));
			matrixWriter.writeWithTaxonName(gsdMatrix, characterReader.getLabelCategoryNameMap());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * parse a matrix from a CSV file
	 * @param manualScoreFile
	 * @return
	 */
	public NewTaxonCharacterMatrix parseMatrixFromCSV(String manualScoreFile) {
		NewTaxonCharacterMatrix matrix = new NewTaxonCharacterMatrix();
		// 0 Taxon	XML_file	Genus	Species	Strain	Character	GSM Value	Extracted Value	GSM_NUM	EXT_NUM	HIT	Relaxed_HIT
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(manualScoreFile);
			CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
		    List<String[]> lines = reader.readAll();
		    for(int i=1;i<lines.size();i++){
		    	String[] line = lines.get(i);
		    	
		    	String[] newLine = new String[12];
		    	//Taxon,XML_file,Character
		    	String taxon =line[2];//taxon
		    	String taxonXMLFile =line[3];//XML_file
		    	String character = line[4];//Character
		    	String gsmValue = line[14];//GSM value

		    	if("{}".equals(gsmValue)){
		    		gsmValue="";
		    	}
		    	
		    	String keyFile = taxon.trim()+"_"+taxonXMLFile.trim();
		    	Map<ILabel, List<CharacterValue>> taxonValues = matrix.getAllTaxonCharacterValues(keyFile);
		    	if(taxonValues==null){
		    		taxonValues = new HashMap<ILabel, List<CharacterValue>>();
		    		matrix.put(keyFile, taxonValues);//put both taxon and xml file
		    	}
		    	ILabel label = characterNameLabelMapping.get(character);
				List multiValues = ValueFormatterUtil.parse(label, gsmValue);
				taxonValues.put(label, multiValues);
		    }
		    
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return matrix;
	}
	
	
	public static void main(String[] args){
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
		
		String charValEvalResultFile = "F:/MicroPIE/evaluation/output/charValEvalResult.csv";
		String charAllEvalResultFile = "F:/MicroPIE/evaluation/output/charAllEvalResult.csv";
		String taxonEvalResultFile = "F:/MicroPIE/evaluation/output/taxonEvalResult.csv";
		String matrixEvalResultFile = "F:/MicroPIE/evaluation/output/matrixEvalResult.csv";
				
				
		String gstBasicFields = "Taxon|XML file|Genus|Species|Strain";
		String gstKeyField = "Taxon";
		String gstXMLField = "XML file";
		
		
		String orgGSMFile ="F:/MicroPIE/evaluation/GSM_MicroPIE1.5_010316_111_final.csv";//Gold_matrix_22_1213.csv.csv";
		String manualScoreFile = "F:\\MicroPIE\\manuscript\\results\\MicroPIE manual scoring turk v1-lisa all-final(with NOT SCORED).csv";
		String newGSMFile ="F:\\MicroPIE\\manuscript\\results\\GSM_MicroPIE1.5_after_MS_0208.csv";//Gold_matrix_22_1213.csv.csv";
	
		UpdateGSM extEval = new UpdateGSM(gstBasicFields,gstKeyField,gstXMLField,categoryMappingFile);
		extEval.readCategoryMapping();
		extEval.setComparedCharacter(comparedCharacterNames);
		extEval.setSeparators("#");
		
		extEval.update(orgGSMFile, manualScoreFile, newGSMFile);
	}

}
