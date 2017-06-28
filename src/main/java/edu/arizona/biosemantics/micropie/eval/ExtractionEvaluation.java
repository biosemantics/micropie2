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
import java.util.Set;

import org.jdom2.JDOMException;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.classify.NumericLabels;
import edu.arizona.biosemantics.micropie.extract.ValueFormatterUtil;
import edu.arizona.biosemantics.micropie.io.CharacterReader;
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
public class ExtractionEvaluation {
	protected String[] gstBasicFields;//basic fields used to describe the taxon/taxon file
	protected String[] tgBasicFields;
	protected String gstKeyField;//the field of the unique taxon name
	protected String tgKeyField;
	protected String gstSeparator;
	protected String tgSeparator;
	
	protected String goldStdMatrixFile;
	protected String testMatrixFile;
	
	protected String categoryMappingFile; 
	
	protected ILabel[] comparedCharacterLabels;
	protected String[] comparedCharacterNames;
	
	protected IValueComparator stringValueComparator = new KeywordStringComparator();
	protected IValueComparator  cosineComparator = new CosineComparator();
	protected IValueComparator numericValueComparator = new NumericComparator();
	protected IValueComparator numericRelaxedComparator = new NumericRelaxedComparator();
	protected NumericLabels numericLabels = new NumericLabels();
	
	protected Map<String, ILabel> characterNameLabelMapping;
	protected Map<ILabel, String> characterLabelNameMapping;
	
	//taxonfilename taxonfile
	protected Map<String, TaxonTextFile> taxonFileMap = new HashMap();
	
	protected Map<TaxonTextFile, List> taxonResults = new LinkedHashMap();
	protected Map<String, List> charResults = new LinkedHashMap();
	protected Map<String, List> charRelaxedResults = new LinkedHashMap();
	protected Map<TaxonTextFile, Map> charValueResults = new LinkedHashMap();
	protected Map<TaxonTextFile, Map> charRelaxedValueResults = new LinkedHashMap();
	protected List<Measurement> matrixResult = null;
	
	protected String datasetFolder = "F:\\MicroPIE\\datasets\\exp1";
	protected String gstXMLField;
	protected String tgXMLField;
	
	/**
	 * the gold standard matrix and the target matrix have the same structure
	 * @param basicFields
	 * @param keyField
	 * @param categoryMappingFile
	 */
	public ExtractionEvaluation(String basicFields,String keyField,String categoryMappingFile){
		String[] fields = basicFields.split(",");
		this.gstBasicFields = fields;
		this.tgBasicFields = fields;
		this.gstKeyField = keyField;
		this.tgKeyField = keyField;
		
		this.categoryMappingFile = categoryMappingFile;
	}
	
	/**
	 * the gold standard matrix and the target matrix have the same structures
	 * @param gstBasicFields
	 * @param tgBasicFields
	 * @param gstKeyField
	 * @param tgKeyField
	 * @param categoryMappingFile
	 */
	public ExtractionEvaluation(String gstBasicFields,String tgBasicFields,String gstKeyField,String tgKeyField,String gstXMLField,String tgXMLField,String categoryMappingFile){
		
		String[] gstFields = gstBasicFields.split("\\|");
		String[] tsFields = tgBasicFields.split("\\|");
		this.gstBasicFields = gstFields;
		this.tgBasicFields = tsFields;
		this.gstKeyField = gstKeyField;
		this.tgKeyField = tgKeyField;
		this.gstXMLField = gstXMLField;
		this.tgXMLField = tgXMLField;
		
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
		}
	}

	/**
	 * set the separators for matrixes.
	 * @param gstSeparator
	 * @param tgSeparator
	 */
	public void setSeparators(String gstSeparator, String tgSeparator){
		this.gstSeparator= gstSeparator;
		this.tgSeparator = tgSeparator;
	}
	
	public void readCategoryMapping(){
		CharacterReader cateReader = new CharacterReader();
		cateReader.setCategoryFile(this.categoryMappingFile);
		cateReader.read();
		this.characterNameLabelMapping = cateReader.getCategoryNameLabelMap();
		this.characterLabelNameMapping = cateReader.getLabelCategoryNameMap();
	}
	
	
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
	 * read the fields from files
	 * @param taxonFiles
	 */
	protected void readTaxonDetail(Set<TaxonTextFile> taxonFiles) {
		XMLTextReader xmlTextReader = new XMLTextReader();
		for(TaxonTextFile tx: taxonFiles){
			String xmlPath = tx.getXmlFile();
			File newFile = new File(this.datasetFolder + File.separator
					+ StringUtil.standFileName(xmlPath));
			try {
				xmlTextReader.setInputStream(new FileInputStream(newFile));
				tx.setFamily(xmlTextReader.getFamily());
				tx.setGenus(xmlTextReader.getGenus());
				tx.setSpecies(xmlTextReader.getSpecies());
				tx.setStrain_number(xmlTextReader.getStrain_number());
				tx.setTaxon(xmlTextReader.getTaxon());
				tx.setThe16SrRNAAccessionNumber(xmlTextReader.get16SrRNAAccessionNumber());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
		}
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
			
			//String taxonName = taxaAndXMLFile.substring(0,taxaAndXMLFile.indexOf("_"));
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
						if(charLabel.equals(Label.c17)||charLabel.equals(Label.c31)){
							matched = cosineComparator.compare(tgCharValue,gstCharValue);
							relaxedMatched= matched;
						}else{
							matched = stringValueComparator.compare(tgCharValue,gstCharValue);
							relaxedMatched= matched;
						}
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
					charHitMap.put(comparedCharacterNames[ch], new DetailMeasurement("hit",matched,ValueFormatterUtil.format(gstCharValue),ValueFormatterUtil.format(tgCharValue),gstSize,tgSize));
					charRelaxedHitMap.put(comparedCharacterNames[ch], new DetailMeasurement("relaxed_hit",relaxedMatched,ValueFormatterUtil.format(gstCharValue),ValueFormatterUtil.format(tgCharValue),gstSize,tgSize));
				}else if(gstCharValue!=null&&gstCharValue.size()!=0){
					charHitMap.put(comparedCharacterNames[ch], new DetailMeasurement("hit",-1,ValueFormatterUtil.format(gstCharValue),ValueFormatterUtil.format(tgCharValue),gstCharValue.size(),0));
					charRelaxedHitMap.put(comparedCharacterNames[ch], new DetailMeasurement("relaxed_hit",-1,ValueFormatterUtil.format(gstCharValue),ValueFormatterUtil.format(tgCharValue),gstCharValue.size(),0));
				}else if(tgCharValue!=null&&tgCharValue.size()!=0){
					charHitMap.put(comparedCharacterNames[ch], new DetailMeasurement("hit",-2,ValueFormatterUtil.format(gstCharValue),ValueFormatterUtil.format(tgCharValue),0,tgCharValue.size()));
					charRelaxedHitMap.put(comparedCharacterNames[ch], new DetailMeasurement("relaxed_hit",-2,ValueFormatterUtil.format(gstCharValue),ValueFormatterUtil.format(tgCharValue),0,tgCharValue.size()));
				}
			}
			
			charValueResults.put(taxonTextFile, charHitMap);
			charRelaxedValueResults.put(taxonTextFile, charRelaxedHitMap);
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
	 * output results
	 * @param charValEvalResultFile
	 * @param charAllEvalResultFile
	 * @param taxonEvalResultFile
	 * @param matrixEvalResultFile
	 */
	public void outputResults(String charValEvalResultFile, String charAllEvalResultFile, String taxonEvalResultFile, String matrixEvalResultFile){
		this.outputCharEval(charValEvalResultFile);
		this.outputCharAllEval(charAllEvalResultFile);
		this.outputTaxonEval(taxonEvalResultFile);
		this.outputMatrixEval(matrixEvalResultFile);
	}
	
	
	/**
	 * output the matrix evaluation
	 * @param matrixEvalResultFile
	 */
	protected void outputMatrixEval(String matrixEvalResultFile) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(matrixEvalResultFile, true)));
			out.print("Gold Standard Matrix,");
			out.print(this.goldStdMatrixFile);
			out.print("\nTest Matrix,");
			out.print(this.testMatrixFile);
			out.print("\nEvaluation Time,");
			out.print(new Date());
			out.print("\nP,R,F1\n");
			out.print(matrixResult.get(0).getValue());
			out.print(",");
			out.print(matrixResult.get(1).getValue());
			out.print(",");
			out.print(matrixResult.get(2).getValue());
			out.print("\n");
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * output every characters
	 * @param charValEvalResultFile
	 */
	protected void outputCharEval(String charValEvalResultFile) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(charValEvalResultFile, true)));
			int charLength = comparedCharacterNames.length;
			out.print("Gold Standard Matrix,");
			out.print(this.goldStdMatrixFile);
			out.print("\nTest Matrix,");
			out.print(this.testMatrixFile);
			out.print("\nEvaluation Time,");
			out.print(new Date());
			out.print("\nCharacter,P,R,F1,Relaxed_P,Relaxed_R,Relaxed_F1\n");
			for(int ch = 0; ch<charLength;ch++){
				String charName = comparedCharacterNames[ch];
				List<Measurement> charEvalResults = charResults.get(charName);
				List<Measurement> charRelaxedEvalResults = charRelaxedResults.get(charName);
				out.print("\""+charName+"\"");
				out.print(",");
				out.print(charEvalResults.get(0).getValue());
				out.print(",");
				out.print(charEvalResults.get(1).getValue());
				out.print(",");
				out.print(charEvalResults.get(2).getValue());
				out.print(",");
				out.print(charRelaxedEvalResults.get(0).getValue());
				out.print(",");
				out.print(charRelaxedEvalResults.get(1).getValue());
				out.print(",");
				out.print(charRelaxedEvalResults.get(2).getValue());
				out.print("\n");
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * put(taxonTextFile, taxonEvalResults);
	 * print taxon evaluation values
	 * @param taxonEvalResultFile
	 */
	protected void outputTaxonEval(String taxonEvalResultFile) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(taxonEvalResultFile, true)));
			out.print("Gold Standard Matrix,");
			out.print(this.goldStdMatrixFile);
			out.print("\nTest Matrix,");
			out.print(this.testMatrixFile);
			out.print("\nEvaluation Time,");
			out.print(new Date());
			out.print("\nTaxon,XML file,Genus,Species,Strain,P,R,F1\n");
			for(Entry<TaxonTextFile, List> entry:taxonResults.entrySet()){
				TaxonTextFile taxonFile = entry.getKey();
				if(taxonFile==null) continue;
				out.print("\""+taxonFile.getTaxon()+"\"");
				out.print(",");
				out.print("\""+taxonFile.getXmlFile()+"\"");
				out.print(",");
				out.print("\""+taxonFile.getGenus()+"\"");
				out.print(",");
				out.print("\""+taxonFile.getSpecies()+"\"");
				out.print(",");
				out.print("\""+taxonFile.getStrain_number()+"\"");
				out.print(",");
				
				List<Measurement> charEvalResults = entry.getValue();
				out.print(charEvalResults.get(0).getValue());//P
				out.print(",");
				out.print(charEvalResults.get(1).getValue());//R
				out.print(",");
				out.print(charEvalResults.get(2).getValue());//F1
				out.print("\n");
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * put(taxonTextFile, taxonEvalResults);
	 * @param charAllEvalResultFile
	 */
	protected void outputCharAllEval(String charAllEvalResultFile) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(charAllEvalResultFile, true)));
			out.print("Gold Standard Matrix,");
			out.print(this.goldStdMatrixFile);
			out.print("\nTest Matrix,");
			out.print(this.testMatrixFile);
			out.print("\nEvaluation Time,");
			out.print(new Date());
			out.print("\nTaxon,XML file,Genus,Species,Strain,Character,GSM Value,Extracted Value,GSM_NUM,EXT_NUM,HIT,Relaxed_HIT\n");
			
			for(Entry<TaxonTextFile,Map> entry : charValueResults.entrySet()){
				TaxonTextFile taxonFile = entry.getKey();
				Map<String, DetailMeasurement> charValues = entry.getValue();
				Map<String, DetailMeasurement> charRelaxedValues = charRelaxedValueResults.get(taxonFile);
				for(Entry<String, DetailMeasurement> valueEntry : charValues.entrySet()){
					if(taxonFile==null) continue;
					out.print("\""+taxonFile.getTaxon()+"\"");
					out.print(",");
					out.print("\""+taxonFile.getXmlFile()+"\"");
					out.print(",");
					out.print("\""+taxonFile.getGenus()+"\"");
					out.print(",");
					out.print("\""+taxonFile.getSpecies()+"\"");
					out.print(",");
					out.print("\""+taxonFile.getStrain_number()+"\"");
					out.print(",");
					out.print(valueEntry.getKey());//Character
					out.print(",");
					out.print("\""+valueEntry.getValue().getGstValue()+"\"");//gold standard
					out.print(",");
					out.print("\""+valueEntry.getValue().getTgValue()+"\"");//Extracted
					out.print(",");
					out.print(valueEntry.getValue().getGstNum());//GST_NUM
					out.print(",");
					out.print(valueEntry.getValue().getTgNum());//TG_NUM, EXT_NUM
					out.print(",");
					out.print(valueEntry.getValue().getValue());//RIGID HIT
					out.print(",");
					out.print(charRelaxedValues.get(valueEntry.getKey()).getValue());//RELAXED HIT
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
	 * 
	 * @param totalExtValueSize
	 * @param positiveSize
	 * @param totalGstValueSize
	 * @return
	 */
	protected List calMeasure(double totalExtValueSize, double positiveSize,
			double totalGstValueSize) {
		double p = positiveSize/totalExtValueSize;
		double r =positiveSize/totalGstValueSize;
		double f1 = 2*p*r/(p+r);
		
		List meaList = new ArrayList();
		meaList.add(new Measurement("P",p));
		meaList.add(new Measurement("R",r));
		meaList.add(new Measurement("F1",f1));
		return meaList;
	}
}
