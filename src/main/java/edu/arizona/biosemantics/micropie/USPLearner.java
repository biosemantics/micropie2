package edu.arizona.biosemantics.micropie;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.extract.ExtractorType;
import edu.arizona.biosemantics.micropie.io.xml.XMLOldestTextReader;
import edu.arizona.biosemantics.micropie.io.xml.XMLTextReader;
import edu.arizona.biosemantics.micropie.model.IndexMapping;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.nlptool.CollapseUSPSentByCategoryChar;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.arizona.biosemantics.micropie.nlptool.SeperatorTokenizer;
import edu.arizona.biosemantics.micropie.nlptool.StanfordParserWrapper;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import usp.semantic.Parse;


/**
 * USP Generator---generate USP input files
 * @author maojin
 *
 */
public class USPLearner {
	
	private String uspBaseFolder = null;//the base folder path
	private String textFolder = null;
	private String depFolder = null;
	private String indexFolder = null;
	private String morphFolder = null;
	private String parseFolder = null;
	
	private String characterValueExtractorsFolder;
	private StanfordParserWrapper stanfordWrapper;
	private CollapseUSPSentByCategoryChar collapseUSPSentByCateogryChar = new CollapseUSPSentByCategoryChar();
	int counter = 1;//sentence counter
	private Hashtable<String, String> kwdListByCategory;
	private XMLOldestTextReader XMLTextReader;//= new XMLTextReader();
	private SentenceSpliter sentenceSpliter =null;
	
	@Inject
	public USPLearner(@Named("uspFolder") String uspBaseFolder,
			@Named("characterValueExtractorsFolder") String characterValueExtractorsFolder,
			StanfordParserWrapper stanfordWrapper,
			XMLOldestTextReader XMLTextReader,
			SentenceSpliter sentenceSpliter){
		this.uspBaseFolder = uspBaseFolder;
		this.characterValueExtractorsFolder = characterValueExtractorsFolder;
		this.stanfordWrapper = stanfordWrapper;
		this.XMLTextReader = XMLTextReader;
		this.sentenceSpliter = sentenceSpliter;
	}
	
	
	public static void main(String[] args){
		Parse uspParse = new Parse();
		String uspModelDir = "F:\\MicroPIE\\micropieInput\\usp_base";
		try {
			uspParse.runParse(uspModelDir, "F:\\MicroPIE\\micropieInput\\output\\usp_results");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 1, create folders
	 * 
	 * 
	 */
	public void initialize(){
		//if the folder "usp" exists, delete it
		//FileUtils.deleteDirectory(new File(uspBaseFolder));
		File baseFile = new File(uspBaseFolder);
		if(baseFile.exists()) System.out.println(uspBaseFolder+" is existed.");
				
		//create the folder structure for USP
		createBasicFolders();

	}
	
	
	/**
	 * process one file
	 * 1, text file: each sentence in a line
	 * 2, morph: a blank line between each sentence
	 * 3, input: a blank line between each sentence
	 * 4, dep: a blank line bewteen each sentence
	 * 
	 * @param inputFile
	 * @throws IOException 
	 */
	public void processFile(File inputFile) throws IOException{
		System.out.println(inputFile);
		//parse the taxon file information
		XMLTextReader.setInputStream(inputFile);
		TaxonTextFile taxonFile = XMLTextReader.readTaxonFile(inputFile);
		//STEP 1: split sentences
		List<MultiClassifiedSentence> sentences = sentenceSpliter.createSentencesFromFile(taxonFile);
		if(sentences==null) return;
		
		String textSubFolder = getSubFolder(textFolder,counter);
		BufferedWriter textFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(textSubFolder+counter+".txt",true), "UTF8"));
		String depSubFolder = getSubFolder(depFolder,counter);
		BufferedWriter depFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(depSubFolder+counter+".dep",true), "UTF8"));
		
		String indexSubFolder = getSubFolder(indexFolder,counter);
		BufferedWriter indexFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(indexSubFolder+counter+".index",true), "UTF8"));
		
		String morphSubFolder = getSubFolder(morphFolder,counter);
		BufferedWriter morphFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(morphSubFolder+counter+".morph",true), "UTF8"));
		BufferedWriter morphIndexFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(morphSubFolder+counter+".input",true), "UTF8"));
		
		String parseSubFolder = getSubFolder(parseFolder,counter);
		BufferedWriter parseFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(parseSubFolder+counter+".parse",true), "UTF8"));
		
		
		for(MultiClassifiedSentence sent : sentences){
			String sentText = sent.getText(); // it is sentence based not text
			
			StringTokenizer textToken = new StringTokenizer(sentText, " ");
			if (textToken.countTokens() < 40) {// if the sentence is larger than 40 tokens. will it encounter some errors?
				appendTextFile(sentText,textFileWriter);
				
				appendParsedResults(sentText,depFileWriter,indexFileWriter,morphFileWriter,morphIndexFileWriter,parseFileWriter);
			}
		}
		
		textFileWriter.flush();
		textFileWriter.close();
		depFileWriter.flush();
		depFileWriter.close();
		indexFileWriter.flush();
		indexFileWriter.close();
		morphFileWriter.flush();
		morphFileWriter.close();
		morphIndexFileWriter.flush();
		morphIndexFileWriter.close();
		parseFileWriter.flush();
		parseFileWriter.close();
		
	}
	
	
	
	
	private void appendParsedResults(String sentText,
			BufferedWriter depFileWriter, BufferedWriter indexFileWriter,
			BufferedWriter morphFileWriter, BufferedWriter morphInputFileWriter,
			BufferedWriter parseFileWriter) throws IOException {
		Annotation annotation = new Annotation(sentText);
		stanfordWrapper.annotate(annotation);
		
		//termlist, for index file
		List<CoreLabel> tokens = annotation.get(TokensAnnotation.class);
		int index = 0;
		for (CoreLabel token : tokens) {
			indexFileWriter.append(token.word());
			indexFileWriter.append("\t");
			indexFileWriter.append(index+"\n");
		}
		indexFileWriter.append("\n");//leave a blank line
		
		
		//morph
		//Flexithrix_FW
		List<CoreLabel> tagList = annotation.get(TokensAnnotation.class);
		for (CoreLabel wordTag : tagList) {
			//input
			morphInputFileWriter.append(wordTag.word());
			morphInputFileWriter.append("_");
			morphInputFileWriter.append(wordTag.tag());
			morphInputFileWriter.append("\n");
			
			//morph
			morphFileWriter.append(wordTag.word());
			morphFileWriter.append("\n");
		}
		morphInputFileWriter.append("\n");
		morphFileWriter.append("\n");
		
		//parse
		Tree parse = stanfordWrapper.parsePhraseTree(sentText);
		parseFileWriter.append(parse.toString());
		parseFileWriter.append("\n");
		
		//dep
		List<TypedDependency> depList = stanfordWrapper.parseDepList(sentText);
		for(TypedDependency td : depList){
			depFileWriter.append(td.toString());
			depFileWriter.append("\n");
		}
		depFileWriter.append("\n");
	}


	private void appendTextFile(String sentText, BufferedWriter textFileWriter) throws IOException {
		textFileWriter.append(sentText);
		textFileWriter.append("\n");
	}


	/**
	 * restructure the folder
	 * @param folder
	 * @param counter
	 * @return
	 */
	private String getSubFolder(String folder, int counter) {
		// TODO Auto-generated method stub
		String subFolder = folder+"\\"+counter/1000+"\\";
		File folderFile = new File(subFolder);
		if(!folderFile.exists()) folderFile.mkdir();
		return subFolder;
	}


	/**
	 * build USP repository from the database
	 * @param databaseFolder
	 */
	public void buildFromDataset(String databaseFolder){
		File inputFolderFile = new File(databaseFolder);
		File[] inputFiles = inputFolderFile.listFiles();
		for (File inputFile : inputFiles) {//process all the files
			
			try {
				processFile(inputFile);
				counter++;
				//break;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}



	/**
	 * create basic folders for USP
	 * including: text, text_o, morph, morph_o, dep,parse,
	 * 
	 * TODO: let each folder hold 1000 files at most
	 */
	public void createBasicFolders() {
		new File(uspBaseFolder).mkdirs();
		textFolder = uspBaseFolder + "/text";
		depFolder = uspBaseFolder + "/dep";
		indexFolder = uspBaseFolder + "/index";
		morphFolder = uspBaseFolder + "/morph";
		parseFolder = uspBaseFolder + "/parse";
		
		new File(textFolder).mkdirs();
		new File(morphFolder).mkdirs();
		new File(depFolder).mkdirs();
		new File(parseFolder).mkdirs();
		new File(indexFolder).mkdirs();
	}


	
}
