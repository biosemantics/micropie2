package edu.arizona.biosemantics.micropie.extract.context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import edu.arizona.biosemantics.micropie.Config;
import edu.arizona.biosemantics.micropie.io.xml.XMLTextReader;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.model.PhraseRelation;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.nlptool.PhraseParser;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.stanford.nlp.ling.TaggedWord;


/**
 * to find all the term cooccurrence
 * 
 * @author maojin
 *
 */
public class TermCooccurrenceFinder {
	
	private XMLTextReader xmlTextReader = new XMLTextReader();
	private SentenceSpliter sentenceSplitter;
	private RelationParser relationParser;
	private PosTagger posTagger;
	
	@Inject
	public TermCooccurrenceFinder(SentenceSpliter sentenceSplitter,
			 RelationParser relationParser,
			 PosTagger posTagger){
		this.posTagger = posTagger;
		this.relationParser = relationParser;
		this.sentenceSplitter = sentenceSplitter;
	}
	
	
	/**
	 * 
	 * @param inputFolder
	 * @param predictionFile
	 * @param outputMatrixFile
	 * @return
	 */
	public List processFolder(String inputFolder){
		File inputFolderFile = new File(inputFolder);
		File[] inputFiles = inputFolderFile.listFiles();
		
		PhraseParser extractor = new PhraseParser();
		List<PhraseRelation> corpusRelationList = new ArrayList();
		for (File inputFile : inputFiles) {//process all the files
			//parse the taxon file information
			TaxonTextFile taxonFile = xmlTextReader.readTaxonFile(inputFile);
			
			//STEP 1: split sentences
			List<MultiClassifiedSentence> sentences = sentenceSplitter.createSentencesFromFile(taxonFile);
			
			//STEP 2: predict the classifications of the sentences, i.e., the characters in each sentences
			for (MultiClassifiedSentence testSentence : sentences) {
				List<TaggedWord> taggedWords = posTagger.tagString(testSentence.getText());
				List<Phrase> phraseList = extractor.extract(taggedWords);
				
				PhraseRelationGraph graph = relationParser.parseCoordinativeRelationGraph(phraseList, taggedWords);
				//List<PhraseRelation> prList = relationParser.parseCoordinativeRelation(phraseList, taggedWords);
				//corpusRelationList.addAll(prList);
				System.out.println("\n"+testSentence.getText());
				for(Phrase p : phraseList){
					Set<Phrase> ahead = graph.getAheadPhrase(p);
					Set<Phrase> follow = graph.getFollowPhrase(p);
					System.out.println(p.getText()+" before it:"+ahead+" follow it:"+follow);
				}
				
			}
			
			
			break;
		}
		
		return corpusRelationList;
	}

	/*
	public void output(List<PhraseRelation> corpusRelationList){
		for(PhraseRelation pr : corpusRelationList){
			System.out.println(pr.getSource()+","+pr.getTarget()+","+pr.getWeight());
		}
	}*/
	
	public static void main(String[] args){
		// TODO Auto-generated method stub
		Config config = new Config();
		String prjInputFolder = "F:/MicroPIE/micropieInput";
		String prjOutputFolder ="F:/MicroPIE/micropieInput/output";
		config.setInputDirectory(prjInputFolder);
		config.setOutputDirectory(prjOutputFolder);
		
		Injector injector = Guice.createInjector(config);
		
		//String inputFolder = "F:\\MicroPIE\\micropieInput\\input";
		String inputFolder = "F:\\MicroPIE\\datasets\\goldtest";
		
		
		TermCooccurrenceFinder tcf = injector.getInstance(TermCooccurrenceFinder.class);
		List prList = tcf.processFolder(inputFolder);
		//tcf.output(prList);
	}
}