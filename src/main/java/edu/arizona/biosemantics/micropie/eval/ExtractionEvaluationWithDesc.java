package edu.arizona.biosemantics.micropie.eval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import org.jdom2.JDOMException;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.classify.NumericLabels;
import edu.arizona.biosemantics.micropie.extract.ValueFormatterUtil;
import edu.arizona.biosemantics.micropie.io.CharacterReader;
import edu.arizona.biosemantics.micropie.io.FileReaderUtil;
import edu.arizona.biosemantics.micropie.io.MatrixReader;
import edu.arizona.biosemantics.micropie.io.xml.XMLTextReader;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.NewTaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.nlptool.StringUtil;


/**
 * 
 *  1: each taxon
 * 	2: each character
 * 	3: all the extraction
 * 
 * output the details into CSV files
 * 
 * @author maojin
 *
 */
public class ExtractionEvaluationWithDesc extends ExtractionEvaluation{
	
	
	public ExtractionEvaluationWithDesc(String basicFields, String keyField,
			String categoryMappingFile) {
		super(basicFields, keyField, categoryMappingFile);
	}


	public ExtractionEvaluationWithDesc(String gstBasicFields,
			String tgBasicFields, String gstKeyField, String tgKeyField,
			String gstXMLField, String tgXMLField, String categoryMappingFile) {
		super(gstBasicFields, tgBasicFields, gstKeyField,tgKeyField,gstXMLField,tgXMLField,categoryMappingFile);
	}


	private String inputFolder = null;
	
	public void setInputFolder(String inputFolder){
		this.inputFolder = inputFolder;
	}
	
	protected Map<TaxonTextFile, Map> charValueDifResults = new LinkedHashMap();
	protected Map<TaxonTextFile, Map> charRelaxedValueDifResults = new LinkedHashMap();
	protected Map<TaxonTextFile, Map> charDescriptionResults = new LinkedHashMap();
	protected Map<TaxonTextFile, Map> charGsmValuesResults = new LinkedHashMap();
	
	/**
	 * make evaluation
	 * @param basicFields
	 * @param keyField
	 * @param labelmappingFile
	 * @param goldStandardsMatrixFile
	 * @param targetMatrixFile
	 */
	public void evaluate(String goldStandardsMatrixFile, String targetMatrixFile){
		
		//parse gold standard matrix and taget matrix
		MatrixReader gsdMatrixReader = new MatrixReader(gstBasicFields, gstKeyField,gstXMLField, characterNameLabelMapping,characterLabelNameMapping);
		gsdMatrixReader.readMatrixFromFile(goldStandardsMatrixFile);
		this.goldStdMatrixFile = goldStandardsMatrixFile;
		NewTaxonCharacterMatrix gsdMatrix = gsdMatrixReader.parseMatrix(this.gstSeparator, false);
		
		//readTaxonDetail(gsdMatrix.getTaxonFiles());
		
		this.taxonFileMap = gsdMatrixReader.parseTaxonFiles(false);
		
		MatrixReader tgMatrixReader = new MatrixReader(tgBasicFields, tgKeyField,tgXMLField, characterNameLabelMapping,characterLabelNameMapping);
		tgMatrixReader.readMatrixFromFile(targetMatrixFile);
		this.testMatrixFile = targetMatrixFile;
		NewTaxonCharacterMatrix tgMatrix = tgMatrixReader.parseMatrix(this.tgSeparator,false);
		
		compareResults(gsdMatrix, tgMatrix);
	}
	

	/**
	 * compare the two character matrix
	 * @param gsdMatrix
	 * @param tgMatrix
	 */
	public void compareResults(NewTaxonCharacterMatrix gsdMatrix, NewTaxonCharacterMatrix tgMatrix){
		
		//for evaluation
		double gstTotalValueNum = 0;
		double tgTotalValueNum = 0;
		double matchedTotalCredit = 0;
		double matchedTotalRelxedCredit = 0;
		
		int charLength = comparedCharacterLabels.length;
		double[] charTotal = new double[charLength];
		double[] charFound = new double[charLength];
		double[] charHit = new double[charLength];
		double[] charRelaxedHit = new double[charLength];
		
		//compare all the taxa
		Set<String> taxaAndXMLFiles = gsdMatrix.keySet();//taxon_xml file
		Iterator<String> taxaIter = taxaAndXMLFiles.iterator();
		//System.out.println(taxa.size()+" goldstands "+tgMatrix.keySet().size()+" files need to compare!");
		while(taxaIter.hasNext()){
			String taxaAndXMLFile = taxaIter.next();
			//System.out.println(taxaAndXMLFile);
			TaxonTextFile taxonTextFile = this.taxonFileMap.get(taxaAndXMLFile);
			Map<ILabel, List> gstAllCharacterValues = gsdMatrix.getAllTaxonCharacterValues(taxaAndXMLFile);
			Map<ILabel, List> tgAllCharacterValues = tgMatrix.getAllTaxonCharacterValues(taxaAndXMLFile);
			
			String taxonName = taxaAndXMLFile.substring(0,taxaAndXMLFile.indexOf("_"));
			String taxonXMLFile = taxaAndXMLFile.substring(taxaAndXMLFile.indexOf("_")+1,taxaAndXMLFile.length());
			double taxonTotal = 0;
			double taxonHit = 0;
			double taxonRelaxedHit = 0;
			double taxonFound = 0;
			if(gstAllCharacterValues==null){
				System.err.println(taxaAndXMLFile+" gst is empity!");
				continue;
			}
			if(tgAllCharacterValues==null){
				System.err.println(taxaAndXMLFile+" in test Matrix is empity!");
				continue;
			}
			//System.out.println("["+taxonName+"] gst:"+gstAllCharacterValues.size()+" tg:"+tgAllCharacterValues.size() );
			//compare all the characters
			Map<String,Measurement> charHitMap = new LinkedHashMap();
			Map<String,Measurement> charRelaxedHitMap = new LinkedHashMap();
			
			Map<String,Measurement> charDifHitMap = new LinkedHashMap();
			Map<String,Measurement> charDifRelaxedHitMap = new LinkedHashMap();
			Map<String,String> charDescriptionMap = new LinkedHashMap();
			Map<String,String> charGsmValueMap = new LinkedHashMap();
			
			for(int ch = 0; ch<charLength;ch++){
				ILabel charLabel = comparedCharacterLabels[ch];
				
				List<CharacterValue> gstCharValue = gstAllCharacterValues.get(charLabel);
				taxonTotal += gstCharValue==null?0:gstCharValue.size();
				charTotal[ch] += gstCharValue==null?0:gstCharValue.size();
				gstTotalValueNum += gstCharValue==null?0:gstCharValue.size();
				
				List<CharacterValue> tgCharValue = tgAllCharacterValues.get(charLabel);
				taxonFound += tgCharValue==null?0:tgCharValue.size();
				charFound[ch] += tgCharValue==null?0:tgCharValue.size();
				tgTotalValueNum += tgCharValue==null?0:tgCharValue.size();
				
				double matched = 0;
				double relaxedMatched = 0;
				if((tgCharValue!=null&&tgCharValue.size()!=0)||(gstCharValue!=null&&gstCharValue.size()!=0)){//when at least one has a value, compare
					//System.out.println(charLabel);
					if(numericLabels.contains(charLabel)){
						matched = numericValueComparator.compare(tgCharValue,gstCharValue);
						relaxedMatched = numericRelaxedComparator.compare(tgCharValue, gstCharValue);
					}else{
						matched = stringValueComparator.compare(tgCharValue,gstCharValue);
						relaxedMatched= matched;
					}
					
					//System.out.println(tgCharValue+" "+gstCharValue+" final value:"+matched);
					taxonHit += matched;
					taxonRelaxedHit += relaxedMatched;
					charHit[ch] += matched;
					charRelaxedHit[ch] +=relaxedMatched;
					
					matchedTotalCredit += matched;
					matchedTotalRelxedCredit +=relaxedMatched;
					
					int gstSize = gstCharValue==null?0:gstCharValue.size();
					int tgSize = tgCharValue==null?0:tgCharValue.size();
					
					charGsmValueMap.put(comparedCharacterNames[ch], ValueFormatterUtil.format(gstCharValue));
					if(Math.ceil(relaxedMatched)!=gstSize||Math.ceil(relaxedMatched)!=tgSize){//need to output a new file
						
						
						
						//highlight keywords
						String highlightedDesc = highlightDesc(taxonXMLFile,gstCharValue,tgCharValue);
						charDescriptionMap.put(comparedCharacterNames[ch], highlightedDesc);
						
						highlightCommon(gstCharValue,tgCharValue);
						charDifHitMap.put(comparedCharacterNames[ch], new DetailMeasurement("hit",matched,ValueFormatterUtil.format(gstCharValue),ValueFormatterUtil.format(tgCharValue),gstSize,tgSize));
						charDifRelaxedHitMap.put(comparedCharacterNames[ch], new DetailMeasurement("relaxed_hit",relaxedMatched,ValueFormatterUtil.format(gstCharValue),ValueFormatterUtil.format(tgCharValue),gstSize,tgSize));
					}else{
						
						charHitMap.put(comparedCharacterNames[ch], new DetailMeasurement("hit",matched,ValueFormatterUtil.format(gstCharValue),ValueFormatterUtil.format(tgCharValue),gstSize,tgSize));
						charRelaxedHitMap.put(comparedCharacterNames[ch], new DetailMeasurement("relaxed_hit",relaxedMatched,ValueFormatterUtil.format(gstCharValue),ValueFormatterUtil.format(tgCharValue),gstSize,tgSize));
					}
					
					
				}else{
					//System.out.println("weird "+tgCharValue+" "+gstCharValue);
				}
			}
			
			charValueResults.put(taxonTextFile, charHitMap);
			charRelaxedValueResults.put(taxonTextFile, charRelaxedHitMap);
			
			charValueDifResults.put(taxonTextFile, charDifHitMap);
			charRelaxedValueDifResults.put(taxonTextFile, charDifRelaxedHitMap);
			charDescriptionResults.put(taxonTextFile, charDescriptionMap);
			charGsmValuesResults.put(taxonTextFile, charGsmValueMap);
			
			
			List taxonEvalResults = this.calMeasure(taxonFound, taxonHit, taxonTotal);
			this.taxonResults.put(taxonTextFile, taxonEvalResults);
			//System.out.println(taxonName+" measure: "+taxonEvalResults.toString());
		}
		
		for(int ch = 0; ch<charLength;ch++){
			
			List charEvalResults = this.calMeasure(charFound[ch], charHit[ch], charTotal[ch]);
			String charName = this.comparedCharacterNames[ch];
			//System.out.println(ch+" "+charName+" "+charFound[ch]+" "+charHit[ch]+" "+charTotal[ch]);
			this.charResults.put(charName, charEvalResults);
			
			
			List charRelaxedEvalResults = this.calMeasure(charFound[ch], charRelaxedHit[ch], charTotal[ch]);
			this.charRelaxedResults.put(charName, charRelaxedEvalResults);
		}
		//measure report
		matrixResult = this.calMeasure(tgTotalValueNum, matchedTotalCredit, gstTotalValueNum);
		
		//System.out.println("final measure: "+matrixResult.toString());
	}


	/**
	 * highlight the value keywords
	 * common==light green   #8DE0A2
	 * gst==#8DA2E0
	 * tg==#E0CB8D
	 * 
	 * @param taxonXMLFile
	 * @param gstCharValue
	 * @param tgCharValue
	 * @return
	 */
	private String highlightDesc(String taxonXMLFile,
			List<CharacterValue> gstCharValue, List<CharacterValue> tgCharValue) {
		FileReaderUtil fr = new FileReaderUtil();
		String description = fr.readFile(inputFolder+"/"+taxonXMLFile);
		
		List<CharacterValue> commonSet = new ArrayList();
		List<CharacterValue> gstSet = new ArrayList();
		List<CharacterValue> tgSet = new ArrayList();
		if(gstCharValue==null) gstCharValue = new ArrayList();
		if(tgCharValue==null) tgCharValue = new ArrayList();
		int[] tgDis = new int[tgCharValue.size()];
		for(CharacterValue gstValue: gstCharValue){//find the common character values
			String gstValueStr = gstValue.getValue();
			boolean containCommon = false;
			for(int i=0;i<tgCharValue.size();i++){
				CharacterValue tgValue = tgCharValue.get(i);
				String tgValueStr = tgValue.getValue();
				if(gstValueStr.trim().equalsIgnoreCase(tgValueStr.trim())){//common
					containCommon = true;
					tgDis[i] = 1;
					commonSet.add(gstValue);
					//System.out.println("common="+replaceKeyword(gstValueStr, gstValueStr, "#8DE0A2"));
					//gstValue.setValue(replaceKeyword(gstValueStr, gstValueStr, "#8DE0A2"));
					//tgValue.setValue(replaceKeyword(gstValueStr, gstValueStr, "#8DE0A2"));
					break;
				}
			}
			
			if(!containCommon){
				gstSet.add(gstValue);
			}
		}
		
		//System.out.println(tgSet.size()+" "+tgDis.length);
		for(int i=0;tgCharValue.size()>0&&i<tgCharValue.size();i++){
			if(tgDis[i]==0){//not common
				tgSet.add(tgCharValue.get(i));
			}
		}
		
		
		//hightlight common
		for(CharacterValue value: commonSet){
			String keywordString = removeSquBrackets(value.getValue());
			description = replaceKeyword(description, keywordString, "#8DE0A2");
		}
		
		//hightlight gst
		for(CharacterValue value: gstSet){
			String keywordString = removeSquBrackets(value.getValue());
			description = replaceKeyword(description, keywordString, "#8DA2E0");
		}
		
		//hightlight gst
		for(CharacterValue value: tgSet){
			String keywordString = removeSquBrackets(value.getValue());
			description = replaceKeyword(description, keywordString, "#E0CB8D");
		}
		
		return description;
	}
	
	
	public void highlightCommon(List<CharacterValue> gstCharValue, List<CharacterValue> tgCharValue) {
		
		if(gstCharValue==null) gstCharValue = new ArrayList();
		if(tgCharValue==null) tgCharValue = new ArrayList();
		int[] tgDis = new int[tgCharValue.size()];
		for(CharacterValue gstValue: gstCharValue){//find the common character values
			String gstValueStr = gstValue.getValue();
			for(int i=0;i<tgCharValue.size();i++){
				CharacterValue tgValue = tgCharValue.get(i);
				String tgValueStr = tgValue.getValue();
				if(gstValueStr.trim().equalsIgnoreCase(tgValueStr.trim())){//common
					tgDis[i] = 1;
					//System.out.println("common="+replaceKeyword(gstValueStr, gstValueStr, "#8DE0A2"));
					gstValue.setValue(replaceKeyword(gstValueStr, gstValueStr, "#8DE0A2"));
					tgValue.setValue(replaceKeyword(gstValueStr, gstValueStr, "#8DE0A2"));
					break;
				}
			}
			
		}
	}
	
	public String replaceKeyword(String description, String keywordString, String color){
		keywordString = keywordString.toLowerCase();
		String patternString = "^"+keywordString+"[,.?\\s]|[,.?\\s]"+keywordString+"[,.?\\s]|\\s"+keywordString+"$|^"+keywordString+"$"; // regular expression pattern
		description = description.replaceAll(patternString," <span style='background:"+color+"'>"+keywordString+"</span> ");
		
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(description.toLowerCase());			
		if (matcher.find() && keywordString.length() > 1) {
			String repl = description.substring(matcher.start(),matcher.end());
			description = description.replaceAll(repl," <span style='background:#8DE0A2'>"+repl+"</span> ");
		}
		return description;
	}
	
	
	/**
	 * output results
	 * @param charValEvalResultFile
	 * @param charAllEvalResultFile
	 * @param taxonEvalResultFile
	 * @param matrixEvalResultFile
	 */
	public void outputResults(String charValEvalResultFile, String charAllEvalResultFile,String charAllDifEvalResultFile, String taxonEvalResultFile, String matrixEvalResultFile){
		this.outputCharEval(charValEvalResultFile);
		this.outputCharAllEval(charAllEvalResultFile);
		this.outputCharDifAllEval(charAllDifEvalResultFile);
		this.outputTaxonEval(taxonEvalResultFile);
		this.outputMatrixEval(matrixEvalResultFile);
	}
	
	
	
	private void outputCharDifAllEval(String charAllDifEvalResultFile) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(charAllDifEvalResultFile, true)));
			out.print("Taxon,XML_file,Character,GSM_Value,GSM_Value_Org,Extracted_Value,GSM_NUM,EXT_NUM,HIT,Relaxed_HIT,Description\n");
			
			for(Entry<TaxonTextFile,Map> entry : charValueDifResults.entrySet()){
				TaxonTextFile taxonFile = entry.getKey();
				Map<String, DetailMeasurement> charValues = entry.getValue();
				Map<String, DetailMeasurement> charRelaxedValues = charRelaxedValueDifResults.get(taxonFile);
				Map<String, String> charDescriptionValues = charDescriptionResults.get(taxonFile);
				Map<String, String> charGsmValues = charGsmValuesResults.get(taxonFile);
				for(Entry<String, DetailMeasurement> valueEntry : charValues.entrySet()){
					if(taxonFile==null) continue;
					String character = valueEntry.getKey();
					out.print("\""+taxonFile.getTaxon()+"\"");
					out.print(",");
					out.print("\""+taxonFile.getXmlFile()+"\"");
					out.print(",");
					out.print(character);//Character
					out.print(",");
					out.print("\""+valueEntry.getValue().getGstValue()+"\"");//gold standard with html tags
					out.print(",");
					out.print("\""+charGsmValues.get(character)+"\"");//gold standard
					out.print(",");
					out.print("\""+valueEntry.getValue().getTgValue()+"\"");//Extracted
					out.print(",");
					out.print(valueEntry.getValue().getGstNum());//GST_NUM
					out.print(",");
					out.print(valueEntry.getValue().getTgNum());//TG_NUM, EXT_NUM
					out.print(",");
					out.print(valueEntry.getValue().getValue());//RIGID HIT
					out.print(",");
					out.print(charRelaxedValues.get(character).getValue());//RELAXED HIT
					out.print(",");
					out.print("\""+charDescriptionValues.get(character)+"\"");//RELAXED HIT
					out.println();
				}
				out.flush();
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	/**
	 * 1, seprate by ; 
	 * 2, extract the inner clause embedded by brackets.
	 * @return
	 */
	public String removeSquBrackets(String sent){
		//int leftBracket = sent.indexOf("(");
		//int rightBracket = sent.indexOf(")");
		Pattern pattern = Pattern.compile("(?<=\\[)(.+?)(?=\\])");
        Matcher matcher = pattern.matcher(sent);
        while(matcher.find()){
        	String innerClause = matcher.group();
			sent = sent.replace(innerClause, "");
        }
        sent = sent.replace("[", "");
        sent = sent.replace("]", "");
		return sent;
	}
	
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
		
		String charValEvalResultFile = "F:/MicroPIE/evaluation/output/charValEvalResult.csv";
		String charAllEvalResultFile = "F:/MicroPIE/evaluation/output/charAllEvalResult.csv";
		String taxonEvalResultFile = "F:/MicroPIE/evaluation/output/taxonEvalResult.csv";
		String matrixEvalResultFile = "F:/MicroPIE/evaluation/output/matrixEvalResult.csv";
		String charAllDifEvalResultFile = "F:/MicroPIE/evaluation/output/charAllDifEvalResult.csv";
		/*
		String charValEvalResultFile = "F:/MicroPIE/evaluation/output/charValEvalResult_58.csv";
		String charAllEvalResultFile = "F:/MicroPIE/evaluation/output/charAllEvalResult_58.csv";
		String taxonEvalResultFile = "F:/MicroPIE/evaluation/output/taxonEvalResult_58.csv";
		String matrixEvalResultFile = "F:/MicroPIE/evaluation/output/matrixEvalResult_58.csv";
		*/
		
		String gstBasicFields = "Taxon|XML file|Genus|Species|Strain";
		String gstKeyField = "Taxon";
		String gstXMLField = "XML file";
		String gstMatrixFile ="F:\\MicroPIE\\manuscript\\results\\GSM_MicroPIE1.5_after_MS_0208.csv";//Gold_matrix_22_1213.csv.csv";
		//gstMatrixFile
		//Taxon	Family	Genus	Species	Strain	16S rRNA accession #	XMsL file
		String tgBasicFields = "Taxon|XML file|Genus|Species|Strain";
		String tgKeyField = "Taxon";
		String tgXMLField = "XML file";
		String tgMatrixFile ="F:/MicroPIE/ext/final111/PartOne111_NOSVM_matrix.csv";//final_114_1214.csv
		//tgMatrixFile
		//String tgBasicFields = "XML file|Taxon|Genus|Species|Strain";//STUEXP_040214_28ch.csv
		//String tgKeyField = "XML file";
		//String tgMatrixFile ="F:/MicroPIE/evaluation/STUEXP_040214_28ch.csv";
		
		ExtractionEvaluationWithDesc extEval = new ExtractionEvaluationWithDesc(gstBasicFields,tgBasicFields,gstKeyField,tgKeyField,gstXMLField,tgXMLField,categoryMappingFile);
		extEval.setInputFolder("F:/MicroPIE/datasets/Part_One_111_final_preprocess/");
		extEval.readCategoryMapping();
		extEval.setComparedCharacter(comparedCharacterNames);
		extEval.setSeparators("#", ",");
		
		extEval.evaluate(gstMatrixFile, tgMatrixFile);
		extEval.outputResults(charValEvalResultFile,charAllEvalResultFile,charAllDifEvalResultFile,taxonEvalResultFile,matrixEvalResultFile);
	}
	
}
