package edu.arizona.biosemantics.micropie.run;

import java.util.Properties;

import edu.arizona.biosemantics.micropie.USPLearner;
import edu.arizona.biosemantics.micropie.io.XMLOldestTextReader;
import edu.arizona.biosemantics.micropie.io.XMLTextReader;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.arizona.biosemantics.micropie.nlptool.StanfordParserWrapper;
import edu.arizona.biosemantics.micropie.nlptool.TextNormalizer;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;


/**
 * process the dataset to generate various indexes
 * @author maojin
 *
 */
public class USPGenerateIndex {

	public static void main(String[] args){
		String uspBaseFolder = "F:\\MicroPIE\\micropieInput\\usp_base_1035";
		String databaseFolder  = "F:\\MicroPIE\\datasets\\USP_625+344+66";
		String characterValueExtractorsFolder = null;
		
		//NLP Tool
		Properties stanfordCoreProperties = new Properties();
		stanfordCoreProperties.put("annotators", "tokenize, ssplit, parse");// ,parse, pos, lemma, ner, , dcoref
		StanfordCoreNLP sfCoreNLP = new StanfordCoreNLP(stanfordCoreProperties);
		
		
		LexicalizedParser lexParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		 
		String serializedClassifierModel = "F:\\MicroPIE\\micropieInput\\nlpmodel/english.all.3class.distsim.crf.ser.gz";
		AbstractSequenceClassifier nerClassifier=CRFClassifier.getClassifierNoExceptions(serializedClassifierModel);
		
		StanfordParserWrapper stanfordWrapper = new StanfordParserWrapper(sfCoreNLP, lexParser,nerClassifier);
		
		XMLOldestTextReader XMLTextReader = new XMLOldestTextReader();
		TextNormalizer textNormalizer = new TextNormalizer(null, characterValueExtractorsFolder);
		SentenceSpliter sentenceSpliter = new SentenceSpliter(textNormalizer, stanfordWrapper);
		
		USPLearner uspLearner = new USPLearner(uspBaseFolder, characterValueExtractorsFolder,stanfordWrapper,XMLTextReader,sentenceSpliter);
		
		//create folders
		uspLearner.initialize();
		
		uspLearner.buildFromDataset(databaseFolder);
	}
}
