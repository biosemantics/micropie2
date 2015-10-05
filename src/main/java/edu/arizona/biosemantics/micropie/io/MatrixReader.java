package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.extract.NumericValueFormatter;
import edu.arizona.biosemantics.micropie.extract.StringValueFormatter;
import edu.arizona.biosemantics.micropie.extract.ValueFormatterUtil;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.NewTaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.nlptool.StringUtil;
import au.com.bytecode.opencsv.CSVReader;

/**
 * read matrix
 * 
 * Notes:
 * 	1, Fields must start with basicFields that do not contain any characters.
 * 	2, All fields are read from the first line
 * 	3, The basicFields must be assigned Manually.
 * @author maojin
 *
 */
public class MatrixReader {
	//the matrix file
	public String matrixFile = null;
	//all the fields including basic heading fields
	public String[] allFields = null;
	//basic fields, describing the taxon
	public String[] basicFields = null;
	//the character fields
	public String[] characterFields = null;
	public ILabel[] characterLabels = null;
	
	public String keyField = null;
	public int keyFieldIndex;
	
	public Map<String, ILabel> characterNameLabelMapping = null;
	public Map<ILabel, String> characterLabelNameMapping = null;
	
	//all the lines in the CSV file
	public List<String[]> lineData = null;
	
	//matrix results
	public NewTaxonCharacterMatrix matrix = null;
	
	public MatrixReader(String[] basicFields, String keyField, Map<String, ILabel> characterNameLabelMapping,Map<ILabel, String> characterLabelNameMapping){
		this.basicFields = basicFields;
		this.keyField = keyField;
		this.characterNameLabelMapping = characterNameLabelMapping;
		this.characterLabelNameMapping = characterLabelNameMapping;
	}
	
	/**
	 * First step: readMatrixFromFile
	 * 
	 * @param file
	 */
	public void readMatrixFromFile(String matrixFile){
		this.matrixFile = matrixFile;
		InputStream inputStream;
		try {			
			inputStream = new FileInputStream(matrixFile);
			CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));

			this.lineData = reader.readAll();
			
			this.readHeader();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	/**
	 * read the first line to parser headers 
	 */
	public void readHeader(){
		allFields = lineData.get(0);
		//System.out.println(basicFields.length+" "+allFields.length);
		characterFields = Arrays.copyOfRange(allFields, basicFields.length, allFields.length);
		for(int i=0;i<basicFields.length;i++){
			if(keyField.equalsIgnoreCase(basicFields[i].trim())){
				this.keyFieldIndex = i;
				break;
			}
		}
		//System.out.println(" keyFieldIndex "+keyFieldIndex);
		characterLabels = new ILabel[characterFields.length];
		for(int i=0;i<characterFields.length;i++){
			characterLabels[i] = characterNameLabelMapping.get(characterFields[i].toLowerCase().trim());
		}
	}
	
	/**
	 * parse taxon files
	 * @return
	 */
	public Map<String, TaxonTextFile> parseTaxonFiles(boolean isTransFileName){
		Map<String, TaxonTextFile> taxonFileMap = new HashMap();
		//start from the first row
		int maxRow = lineData.size();
		for (int row = 1; row < maxRow; row++) {// Go through to each column
			String[] rowFieldValues = lineData.get(row);

			TaxonTextFile taxonFile = new TaxonTextFile();
			String taxon = rowFieldValues[keyFieldIndex]; //taxa name
			if(isTransFileName) taxon = StringUtil.standFileName(taxon);
			System.out.println(taxon);
			//Taxon|XML file|Genus|Species|Strain
			String taxonName = rowFieldValues[0];
			taxonFile.setTaxon(taxonName);
			String taxonFileName = rowFieldValues[1];
			if(isTransFileName){
				taxonFile.setXmlFile(StringUtil.standFileName(taxonFileName));
			}else{
				taxonFile.setXmlFile(taxonFileName);
			}
			
			String genus = rowFieldValues[2];
			taxonFile.setGenus(genus);
			String species = rowFieldValues[3];
			taxonFile.setSpecies(species);
			String strain = rowFieldValues[4];
			taxonFile.setStrain_number(strain);
			
			taxonFileMap.put(taxon, taxonFile);
		}
		
		return taxonFileMap;
	}
	
	
	/**
	 * Parse the character values for all the character field
	 * @param multiValueSign
	 */
	public NewTaxonCharacterMatrix parseMatrix(String multiValueSign, boolean isTransFileName){
		matrix = new NewTaxonCharacterMatrix();
		
		StringValueFormatter svFormatter = new StringValueFormatter(multiValueSign);
		NumericValueFormatter nvFormatter = new NumericValueFormatter(multiValueSign);
		
		//start from the first row
		int maxRow = lineData.size();
		int charStartIndex = basicFields.length;
		int fieldsLength = allFields.length;
		int notNullChars = 0;
		
		for (int row = 1; row < maxRow; row++) {// Go through to each column
			String[] rowFieldValues = lineData.get(row);
			
			
			String taxon = rowFieldValues[keyFieldIndex]; //taxa name
			if(isTransFileName) taxon = StringUtil.standFileName(taxon);//transform the file name
			//System.out.println("parseMatrix="+taxon);
			if(taxon==null) continue;//it means it's empty
			Map<ILabel, List<CharacterValue>> taxonValues = new HashMap<ILabel, List<CharacterValue>>();
			matrix.put(taxon, taxonValues);
			
			notNullChars = 0;
			int isNotEmptyStringCounter = 0;
			for (int j = charStartIndex; j < fieldsLength; j++) { // Go through to each row from row 1
				// Start from first row
				String cellValue = rowFieldValues[j].trim();
				ILabel label = characterLabels[j-charStartIndex];
				
				if(label!=null){
					//System.out.println(taxon+" "+characterLabelNameMapping.get(label)+" "+cellValue);
					List multiValues = ValueFormatterUtil.parse(label, cellValue);
					//splitValue(label, multiValueSign, cellValue);
					System.out.println(label+":"+multiValues);
					taxonValues.put(label, multiValues);
					notNullChars++;
				}
				//else{
				//	System.err.println(characterFields[j-charStartIndex]+" is not mapped!");
				//}
			}
		}
		
		System.out.println(this.matrixFile+" taxa:"+matrix.size()+" basicFields="+basicFields.length+" characterLabels:"+characterLabels.length+" characters read:"+notNullChars);
		
		return matrix;
	}
	
	/**
	 * split the values into multiple values with given splitSign
	 * 
	 * @param label
	 * @param multiValueSign
	 * @param cellValue
	 * @return
	 */
	public List splitValue(ILabel label, String multiValueSign, String cellValue) {
		String[] values = cellValue.split(multiValueSign);
		List charValues = new LinkedList();
		for(String value : values){
			//TODO:preprocessing for strings
			value = value.trim().replace("[\\s]+", " ")
					.replace("–", "-")//
					.replace("[\\s-\\s]", "-")//
					.replace("·", ".")//decimal number
					;
			
			charValues.add(CharacterValueFactory.create(label,value.trim()));
		}
		return charValues;
	}

	/**
	 * Counter how many values are extracted
	 * @param goldStandardMatrixFileName
	 */
	public void characterValueCounter(String goldStandardMatrixFileName) {
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(goldStandardMatrixFileName);
			CSVReader reader = new CSVReader(new BufferedReader(
					new InputStreamReader(inputStream, "UTF8")));

			List<String[]> lines = reader.readAll();


			// System.out.println(linesGold.get(0)[0]);//taxon
			// System.out.println(linesGold.get(0)[1]);//16S rRNA accession #

			StringBuilder outputStringBuilder = new StringBuilder("");

			
			outputStringBuilder.append("\"Character Name\",\"Count\"," + "\n");

			
			for (int i = 1; i < lines.get(0).length; i++) { // Go through to each column
				String charName = lines.get(0)[i];
				System.out.println("charName: " + charName);
				outputStringBuilder.append("\"" + charName + "\",");
				// System.out.println(lines.size()); // Number of rows

				int isNotEmptyStringCounter = 0;
				for (int j = 1; j < lines.size(); j++) { // Go through to each row from row 1
					// Start from first row
					String cellValue = lines.get(j)[i];
					if ( !cellValue.equals("")) {
						isNotEmptyStringCounter +=1;
					}

				}
				outputStringBuilder.append("\"" + isNotEmptyStringCounter + "\"," + "\n");
				System.out.println("isNotEmptyStringCounter: " + isNotEmptyStringCounter);
			}

			try (PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter("GoldStandardCharacterCounter.csv")))) {
				out.println(outputStringBuilder);
			} catch (IOException e) {
				// exception handling left as an exercise for the reader
			}

			System.out.println("Done on creating Gold Standard Character Counter!");

			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}//count values end
	
}
