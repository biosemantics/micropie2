package edu.arizona.biosemantics.micropie.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import edu.arizona.biosemantics.micropie.io.xml.XMLNewSchemaTextReader;
import edu.arizona.biosemantics.micropie.io.xml.XMLTextReader;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.nlptool.ITextNormalizer;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.arizona.biosemantics.micropie.nlptool.StanfordParserWrapper;
import edu.arizona.biosemantics.micropie.nlptool.TextNormalizer;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;


/**
 * read the description field and save to a new file
 * @author maojin
 *
 */
public class DescriptionReader {
	
	private SentenceSpliter sentenceSpliter;
	
	public DescriptionReader(SentenceSpliter sentenceSpliter){
		this.sentenceSpliter = sentenceSpliter;
	}
	
	
	
	/**
	 * @param inputFolder
	 * @param predictionFile if it's null, donot printout
	 * @param outputMatrixFile
	 */
	public void processFolder(String inputFolder,String outputFolder){
		
		File inputFolderFile = new File(inputFolder);
		File[] inputFiles = inputFolderFile.listFiles();
		for (File inputFile : inputFiles) {//process all the files
			TaxonTextFile taxonFile = readTaxonFile(inputFile);
			//STEP 1: split sentences
			List<MultiClassifiedSentence> sentences = sentenceSpliter.createSentencesFromFile(taxonFile);
			
			String newFile = outputFolder+"/"+taxonFile.getXmlFile();
			
			OutputStreamWriter fw;
			try {
				fw = new OutputStreamWriter(new FileOutputStream(newFile,true), "UTF8");
				for(MultiClassifiedSentence sent : sentences){
					fw.write(sent.getText());
				}
				fw.flush();
				fw.close();
			} catch ( IOException e) {
				e.printStackTrace();
			}
			
		}
		
		
		
	}
	
	
	
	/**
	 * read a taxonfile
	 * @param inputFile
	 * @return
	 */
	private TaxonTextFile readTaxonFile(File inputFile) {

		XMLTextReader textReader = new XMLTextReader();
		//System.out.println(inputFile);
		textReader.setInputStream(inputFile);
		if(textReader.isNew()){
			textReader = new XMLNewSchemaTextReader();
			textReader.setInputStream(inputFile);
		}
		TaxonTextFile taxonFile = textReader.readFile();
		if(taxonFile.getSpecies()==null){
			taxonFile.setTaxon(taxonFile.getGenus());
		}else{
			taxonFile.setTaxon(taxonFile.getGenus()+" "+taxonFile.getSpecies());
		}
		
		
		
		String text = textReader.read();
		taxonFile.setInputFile(null);
		taxonFile.setText(text);
		taxonFile.setXmlFile(inputFile.getName());
		
		return taxonFile;
	}

	
	public static void main(String[] args){
		String inputDirectory = "F:/MicroPIE/micropieInput";
		String abbreviationFile = inputDirectory + File.separator + "abbrevlist/abbrevlist.csv";
		LinkedHashMap<String, String> abbreviations = null;
		CSVAbbreviationReader abbreviationReader = new CSVAbbreviationReader();
		try {
			abbreviationReader.setInputStream(new FileInputStream(abbreviationFile));
			abbreviations =  abbreviationReader.read();
		} catch (Exception e) {
			e.printStackTrace();
		}
		 String celsius_degreeReplaceSourcePattern = "(" +
					"\\s?˚C\\s?|" +
					"\\s?˚ C\\s?|" +
					"\\s?\"C\\s?|" +
					"\\s?\" C\\s?|" +
					"\\s?◦C\\s?|" +
					"\\s?◦ C\\s?|" +
					"\\s?°C\\s?|" +
					"\\s?° C\\s?|" +
					"\\s?\\”C\\s?|" +
					"\\s?\\” C\\s?|" +
					"\\s?u C\\s?" +
					")";
		ITextNormalizer textNormalizer = new TextNormalizer(abbreviations, celsius_degreeReplaceSourcePattern);
		
		
		Properties stanfordCoreProperties = new Properties();
		stanfordCoreProperties.put("annotators", "tokenize, ssplit, parse");// ,parse, pos, lemma, ner, , dcoref
		StanfordCoreNLP sfCoreNLP = new StanfordCoreNLP(stanfordCoreProperties);
		
		
		LexicalizedParser lexParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		 
		String serializedClassifierModel = "F:\\MicroPIE\\micropieInput\\nlpmodel/english.all.3class.distsim.crf.ser.gz";
		AbstractSequenceClassifier nerClassifier=CRFClassifier.getClassifierNoExceptions(serializedClassifierModel);
		
		StanfordParserWrapper stanfordWrapper = new StanfordParserWrapper(sfCoreNLP, lexParser,nerClassifier);
		
		SentenceSpliter sentSplitter = new SentenceSpliter(textNormalizer, stanfordWrapper);
		
		
		
		DescriptionReader descReader = new DescriptionReader(sentSplitter);
		
		descReader.processFolder("F:\\MicroPIE\\datasets\\Part_One_111_final", "F:\\MicroPIE\\datasets\\Part_One_111_final_preprocess");
		
	}
}
