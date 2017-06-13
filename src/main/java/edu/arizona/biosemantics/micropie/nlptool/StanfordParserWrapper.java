package edu.arizona.biosemantics.micropie.nlptool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.InvalidFormatException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.Document;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;


/**
 * wrap simple standford parser functions
 * @author maojin
 *
 */
public class StanfordParserWrapper {
	
	private StanfordCoreNLP sfCoreNLP;
	private LexicalizedParser lexParser;
	private AbstractSequenceClassifier nerClassifier;
	
	@Inject
	public StanfordParserWrapper(StanfordCoreNLP sfCoreNLP, LexicalizedParser lexParser, 
			@Named("nerClassifier")AbstractSequenceClassifier nerClassifier){
		this.sfCoreNLP = sfCoreNLP;
		this.lexParser = lexParser;
		this.nerClassifier = nerClassifier;
	}
	
	/**
	 * tokenize the string and convert the results to a new string
	 * @param oriSent
	 * @return
	 */
	public String tokenizer2Str(String str) {
		String tokenStr = null;
		StringBuffer returnString = new StringBuffer();
				
		Annotation annotation = new Annotation(str);
		this.sfCoreNLP.annotate(annotation);
		List<CoreMap> sentenceAnnotations = annotation.get(SentencesAnnotation.class);
		
		if (sentenceAnnotations.size() > 0) {
			for (CoreMap sentenceAnnotation : sentenceAnnotations) {
				// result.add(sentenceAnnotation.toString());
				for (CoreLabel token : sentenceAnnotation.get(TokensAnnotation.class)) {
					returnString.append(" ").append(token);
				}
			}
			tokenStr = returnString.substring(1);	
		} else {
			tokenStr = str;
		}
		
		return tokenStr;
	}
	
	/**
	 * tokenize the string and convert the results to a new string
	 * @param oriSent
	 * @return
	 */
	public List<CoreMap> tokenize(String str) {
		Annotation annotation = new Annotation(str);
		this.sfCoreNLP.annotate(annotation);
		List<CoreMap> sentenceAnnotations = annotation.get(SentencesAnnotation.class);
		
		return sentenceAnnotations;
	}

	

	public void annotate(Annotation annotation) {
		this.sfCoreNLP.annotate(annotation);
	}
	
	/**
	 * split text to sentences using stanford corenlp pipeline...
	 * @param text
	 * @return
	 */
	public List<String> getSentences(String text) {
		List<String> result = new LinkedList<String>();
		Annotation annotation = new Annotation(text);
		this.sfCoreNLP.annotate(annotation);
		List<CoreMap> sentenceAnnotations = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentenceAnnotation : sentenceAnnotations) {
			result.add(sentenceAnnotation.toString());
		}
		return result;
	}
	
	/**
	 * split text to sentences using stanford corenlp pipeline...
	 * ORGANIZATION
	 * LOCATION
	 * PERSON
	 * @param text
	 * @return
	 */
	public List<String> getNER(String text, String type) {
		List<List<CoreLabel>> clList = nerClassifier.classify(text);
		List<String> nerList = new ArrayList();
		
		String curNerType = "";
		String curWord = "";
		for (List<CoreLabel> lcl : clList){
			for (CoreLabel cl : lcl) {
		        String word = cl.word();
		        String nerType =  cl.get(AnswerAnnotation.class);
		        //System.out.println(word+" "+nerType);
		       
		        if(curNerType.equals(nerType)&&type.equals(nerType)){
		        	curWord+=" "+word;
		        }else if(!curNerType.equals(nerType)&&type.equals(nerType)){
		        	curWord+=word;
		        }else{
		        	if(curWord!=null&&!"".equals(curWord)){
		        		nerList.add(curWord);
		        		curWord="";
		        	}
		        }
		        curNerType = nerType;
		    }
		}
		
		if(curWord!=null&&!"".equals(curWord)){
    		nerList.add(curWord);
    	}
		return nerList;
	}
	
	/**
	 * split text to sentences using stanford corenlp pipeline...
	 * ORGANIZATION
	 * LOCATION
	 * PERSON
	 * @param text
	 * @return
	 */
	public List<String> getLocationNER(String text) {
		String type = "LOCATION";
		List<List<CoreLabel>> clList = nerClassifier.classify(text);
		List<String> nerList = new ArrayList();
		
		String curNerType = "";
		String curWord = "";
		for (List<CoreLabel> lcl : clList){
			for (int i=0;i<lcl.size();i++) {
				CoreLabel cl = lcl.get(i);
		        String word = cl.word();
		        String nerType =  cl.get(AnswerAnnotation.class);
		        //System.out.println(word+" "+nerType+" "+curNerType);
		       
		        if(",".equals(word)&&curWord!=null&&"O".equals(nerType)&&!"O".equals(curNerType)&&curNerType.equals(lcl.get(i+1).get(AnswerAnnotation.class))) {
		        	curWord+=" "+word;
		        }else{
		        	if(curNerType.equals(nerType)&&type.equals(nerType)){
				        curWord+=" "+word;
			        }else if(!curNerType.equals(nerType)&&type.equals(nerType)){
			        	curWord+=word;
			        }else{
			        	if(curWord!=null&&!"".equals(curWord)){
			        		nerList.add(curWord);
			        		curWord="";
			        	}
			        }
			        curNerType = nerType;
		        }
		    }
		}
		
		if(curWord!=null&&!"".equals(curWord)){
    		nerList.add(curWord);
    	}
		return nerList;
	}
	
	
	
	/**
	 * 
	 * Obtain Phrase Structure
	 * The difference between phrase structure and dependency structure can be seen in
	 *     Phrase Structure Parsing with Dependency Structure
	 * 
	 * @param sent
	 * @return
	 */
	public Tree parsePhraseTree(String sent){
		//tokenize the sentence
		TokenizerFactory<CoreLabel> tokenizerFactory =  PTBTokenizer.factory(new CoreLabelTokenFactory(), "");  
	   	List<CoreLabel> rawWords =    tokenizerFactory.getTokenizer(new StringReader(sent)).tokenize();  
	   	
	   	//[penn, oneline, rootSymbolOnly, words, wordsAndTags, dependencies, typedDependencies, typedDependenciesCollapsed, latexTree, xmlTree, collocations, semanticGraph, conllStyleDependencies, conll2007]
	   	lexParser.setOptionFlags("-outputFormat", "xmlTree");
	   	//parse the sentence
        Tree parse = lexParser.apply(rawWords); 
        return parse;
	}
	
	
	/**
	 * Obtain dependency phrase
	 * @param sent
	 * @return
	 */
	public Tree parseDepTree(String sent){
		//tokenize the sentence
		TokenizerFactory<CoreLabel> tokenizerFactory =  PTBTokenizer.factory(new CoreLabelTokenFactory(), "");  
	   	List<CoreLabel> rawWords =    tokenizerFactory.getTokenizer(new StringReader(sent)).tokenize();  
	   	
	   	//lexParser.setOptionFlags(flags);
	   	//parse the sentence
        Tree parse = lexParser.apply(rawWords); 
        return parse;
	}
	
	/**
	 * Obtain dependency phrase
	 * @param sent
	 * @return
	 */
	public Tree parseDepTreeByCoreNLP(String sentence){
		Annotation annotation = new Annotation(sentence);
		this.sfCoreNLP.annotate(annotation);
		//CoreAnnotations.SentencesAnnotation.class
		//Tree tree = annotation.get(TreeAnnotation.class);
		//SemanticGraph dependencies = annotation.get(CollapsedCCProcessedDependenciesAnnotation.class);
		//System.out.println(dependencies);

      	List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
      	for(CoreMap sent: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sent.get(TokensAnnotation.class)) {
              // this is the text of the token
              String word = token.get(TextAnnotation.class);
              // this is the POS tag of the token
              String pos = token.get(PartOfSpeechAnnotation.class);
              // this is the NER label of the token
              String ne = token.get(NamedEntityTagAnnotation.class);       
            }

            // this is the parse tree of the current sentence
           return sent.get(TreeAnnotation.class);

            // this is the Stanford dependency graph of the current sentence
           // System.out.println(sent.get(CollapsedCCProcessedDependenciesAnnotation.class));
          }
        return null;
	}
	
	
	
	/**
	 * Obtain dependency tree
	 * @param sent
	 * @return
	 */
	public GrammaticalStructure depParse(String sent){
       // Tree parse = parseDepTree(sent);
        Tree parse = parseDepTreeByCoreNLP(sent);
//        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
//      	GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
//      	GrammaticalStructure gs = gsf.newGrammaticalStructure(parse); 
      	
        //TreebankLanguagePack tlp = new PennTreebankLanguagePack();  
        //GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();  
        //GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);  
        //Choose the type of dependenciesCollapseTree  
        //so that dependencies which do not   
        //preserve the tree structure are omitted  
	   // return (List<TypedDependency>) gs.typedDependenciesCollapsedTree(); 
        return this.depParse(parse);
	}
	
	
	/**
	 * Obtain dependency tree
	 * @param sent
	 * @return
	 */
	public GrammaticalStructure depParse( Tree parse){
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
      	GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
      	GrammaticalStructure gs = gsf.newGrammaticalStructure(parse); 
        return gs;
	}
	
	
	
	public List<TypedDependency> parseDepList(String sent){
		//tokenize the sentence
		TokenizerFactory<CoreLabel> tokenizerFactory =  PTBTokenizer.factory(new CoreLabelTokenFactory(), "");  
	   	List<CoreLabel> rawWords =    tokenizerFactory.getTokenizer(new StringReader(sent)).tokenize();  
	   	
	   	//parse the sentence
        Tree parse = lexParser.apply(rawWords);  
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();  
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();  
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);  
	    return (List<TypedDependency>) gs.typedDependenciesCollapsedTree(); 
	}
}
