package edu.arizona.biosemantics.micropie.extract;

import java.util.List;
import java.util.Properties;

import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.nlptool.PhraseParser;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.StanfordParserWrapper;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;



public class PhraseTest {
	public static void main(String[] args){
		String posTagModel = "edu/stanford/nlp/models/pos-tagger/english-bidirectional/english-bidirectional-distsim.tagger";
		PosTagger postagger = new PosTagger(posTagModel);
		
		
		Properties stanfordCoreProperties = new Properties();
		stanfordCoreProperties.put("annotators", "tokenize, ssplit, parse");// ,parse, pos, lemma, ner, , dcoref
		StanfordCoreNLP sfCoreNLP = new StanfordCoreNLP(stanfordCoreProperties);
		
		
		LexicalizedParser lexParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		 
		String serializedClassifierModel = "F:\\MicroPIE\\micropieInput\\nlpmodel/english.all.3class.distsim.crf.ser.gz";
		AbstractSequenceClassifier nerClassifier=CRFClassifier.getClassifierNoExceptions(serializedClassifierModel);
		
		StanfordParserWrapper stanfordWrapper = new StanfordParserWrapper(sfCoreNLP, lexParser,nerClassifier);
		PhraseParser pp = new PhraseParser();
		//"After 48 h, colonies grown on tryptic soy agar are 1.0-1.5 mm in diameter, round, entire and very waxy."
		String sent = "Enlarged cells and filamentous cells are seen occasionally in stationary phase in broth culture.";
		List sentTaggedWords = postagger.tagString(sent);
		List<Phrase> phList = pp.extract(sentTaggedWords);
		//Tree phraseTree = stanfordWrapper.parseDepTree(sent);
		//List<String> phList = pp.getNounPhrases(phraseTree);
		for(Phrase p: phList){
			System.out.println(p);
		}
	}
}
