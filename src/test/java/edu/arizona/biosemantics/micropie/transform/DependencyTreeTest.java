package edu.arizona.biosemantics.micropie.transform;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.PostProcessor;
import edu.arizona.biosemantics.micropie.extract.regex.AntibioticSyntacticExtractor;
import edu.arizona.biosemantics.micropie.nlptool.StanfordParserWrapper;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.GrammaticalStructure;

public class DependencyTreeTest {
	
	
	public static void main(String[] args) {
		Properties stanfordCoreProperties = new Properties();
		stanfordCoreProperties.put("annotators", "tokenize,ssplit");//,pos,lemma,ner, parse,lemma,ner
		StanfordCoreNLP sfCoreNLP = new StanfordCoreNLP(stanfordCoreProperties);
		
		String serializedClassifierModel = "F:\\MicroPIE\\micropieInput\\nlpmodel/english.all.3class.distsim.crf.ser.gz";
		AbstractSequenceClassifier nerClassifier=CRFClassifier.getClassifierNoExceptions(serializedClassifierModel);
		LexicalizedParser lexParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		
		StanfordParserWrapper stanfordWrapper = new StanfordParserWrapper(sfCoreNLP, lexParser,nerClassifier);
		
		/*
		HashSet patterns = new HashSet();
		patterns.add("[Ss]usceptible");
		patterns.add("[Ss]ensitive");
		AntibioticSyntacticExtractor antiExtractor = new AntibioticSyntacticExtractor(Label.c32, null,patterns,null,null);
		
		
		long btime = System.currentTimeMillis();
		String cleanSent = "Susceptible to penicillin G, chloramphenicol, cephalothin, lincomycin and oleandomycin, but not to good polymyxin B, gentamicin, novobiocin, kanamycin or neomycin.";
		//String cleanSent = "Sensitive to rifampicin, bacitracin and novobiocin.";
		Annotation annotation = new Annotation(cleanSent);
		annotation.set(CoreAnnotations.PartOfSpeechAnnotation.class, cleanSent);
		stanfordWrapper.annotate(annotation);
		long etime = System.currentTimeMillis();
		System.out.println(etime-btime);
		*/
		
		String cleanSent = "Lisa will go to school at Stanford University, which is located in California.";
		List<String> nerList = stanfordWrapper.getNER(cleanSent, "LOCATION");//ORGANIZATION
		for(String st:nerList){
			System.out.println(st);
		}
	    /*
		// create an empty Annotation just with the given text
	    Annotation document = new Annotation(cleanSent);
	    Properties props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    // run all Annotators on this text
	    pipeline.annotate(document);
	    
	
	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    for(CoreMap sentence: sentences) {
	        // traversing the words in the current sentence
	        // a CoreLabel is a CoreMap with additional token-specific methods
	        for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	        	 // this is the text of the token
		          String word = token.get(TextAnnotation.class);
		          // this is the POS tag of the token
		          String pos = token.get(PartOfSpeechAnnotation.class);
		          // this is the NER label of the token
		          String ne = token.get(NamedEntityTagAnnotation.class);   
		          System.out.println(word+" "+pos+" "+ne+" "+token.toString());
	        }
	    }*/
	}
}
