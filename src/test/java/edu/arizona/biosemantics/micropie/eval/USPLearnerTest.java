package edu.arizona.biosemantics.micropie.eval;

import java.util.Properties;

import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.USPLearner;
import edu.arizona.biosemantics.micropie.nlptool.StanfordParserWrapper;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class USPLearnerTest {
	
	
	
	public static void main(String[] args){
		Properties stanfordCoreProperties = new Properties();
		stanfordCoreProperties.put("annotators", "tokenize, ssplit");
		StanfordCoreNLP sfCoreNLP = new StanfordCoreNLP(stanfordCoreProperties);
		
		
		LexicalizedParser lexParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		 
		StanfordParserWrapper stanfordWrapper = new StanfordParserWrapper(sfCoreNLP, lexParser);
		
		
		String uspBaseFolder = "";
		String characterValueExtractorsFolder = null;
			
			
		USPLearner uspLearner = new USPLearner(null, null, null);
	}
}
