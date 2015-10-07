package edu.arizona.biosemantics.micropie.nlptool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.InvalidFormatException;

import com.google.inject.Inject;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
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
	
	@Inject
	public StanfordParserWrapper(StanfordCoreNLP sfCoreNLP, LexicalizedParser lexParser){
		this.sfCoreNLP = sfCoreNLP;
		this.lexParser = lexParser;
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
	 * Obtain dependency tree
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
	 * Obtain dependency tree
	 * @param sent
	 * @return
	 */
	public List<TypedDependency> depParse(String sent){
        Tree parse = parseDepTree(sent);
  
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();  
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();  
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);  
        //Choose the type of dependenciesCollapseTree  
        //so that dependencies which do not   
        //preserve the tree structure are omitted  
	    return (List<TypedDependency>) gs.typedDependenciesCollapsedTree(); 
	}
	
}
