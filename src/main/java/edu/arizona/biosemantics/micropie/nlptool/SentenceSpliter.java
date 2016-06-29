package edu.arizona.biosemantics.micropie.nlptool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import semanticMarkup.know.lib.WordNetPOSKnowledgeBase;
import semanticMarkup.ling.Token;
import semanticMarkup.ling.learn.utility.LearnerUtility;
import semanticMarkup.ling.transform.ITokenizer;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SubSentence;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;



/**
 * 
 * @author maojin
 * Used as a tool
 */
public class SentenceSpliter {
	
	private ITextNormalizer textNormalizer;
	//private LearnerUtility tester;
	//private String celsius_degreeReplaceSourcePattern;
	private StanfordParserWrapper stanfordWrapper;
	
	@Inject
	public SentenceSpliter(ITextNormalizer normalizer,
			//LearnerUtility tester,
			StanfordParserWrapper stanfordWrapper){
		this.textNormalizer = normalizer;
		//this.tester = tester;
		//this.celsius_degreeReplaceSourcePattern = celsius_degreeReplaceSourcePattern;
		this.stanfordWrapper = stanfordWrapper;
	}
	
	public List<String> split(String text){
		//long b = System.currentTimeMillis();
		//long b1 = System.currentTimeMillis();
		//text = textNormalizer.transform(text);
		//System.out.println(text);
		if(text==null) return null;
		text = textNormalizer.toDBC(text);
		text = textNormalizer.transformEntity(text);
		text = textNormalizer.transformDash(text); // replace \"–\" to \"-\" ..."
		text = textNormalizer.transformPeriod(text); // · =>.
		text = textNormalizer.transformSpchar(text); 
		//System.out.println(text);
		text = replaceCapitalPeriod(text);
		//long e = System.currentTimeMillis();
		//System.out.println("long replacements costs "+(e-b)+" ms");
		
		//b = System.currentTimeMillis();
		//Standford parser for long text 
		List<String> preSents = preSplit(text);
		List<String> sentences = new LinkedList();
		
		
		
		//TODO: FOR Long sentences, the computation is heavy.		
		
		
		for(String sent: preSents){
			//System.out.println(sent);
			if(sent.length()>=450&&sent.split("[\\.\\?]").length<=2){
				sentences.add(sent);
			}else if(sent.split("\\s+").length>13){//what should it be?
				sentences.addAll(stanfordWrapper.getSentences(sent));
			}else{
				sentences.add(sent);
			}
		}
		//e = System.currentTimeMillis();
		//System.out.println("stanford parser costs "+(e-b)+" ms");
		
		//pipeline 
		sentences = splitSentencesBySemicolon(sentences); // run ";" semicolon separator
		//System.out.println("total sentences:"+ sentences.size());
		// replace abbreviation back to original sentence
		//for (String sentence: sentences) {
			//sentence = textNormalizer.transformDash(sentence); // replace \"–\" to \"-\" ..."
			//sentence = textNormalizer.transformPeriod(sentence); // · =>.
			
			//what's the purpose?....
			//sentence = textNormalizer.transformBack(sentence);
			
			//sentence = textNormalizer.transformCelsiusDegree(sentence); // °C => celsius_degree
			//System.out.println(sentence);
		//}
		
		//long e1 = System.currentTimeMillis();
		//System.out.println("all spliting costs "+(e1-b1)+" ms");
		return sentences;
	}
	
	/**
	 * 
	 * @param sentences
	 * @return
	public List<String> reSplitWithExceptions(List<String> sentences) {
		List<String> result = new LinkedList<String>();
		for (String sentence : sentences) {
			sentence = splitByCapitalPeriod(sentence); // => add explanation here!! what is the purpose?
			result.addAll(stanfordWrapper.getSentences(sentence));
		}
		return result;
	}
	 */
	
	
	/**
	 * split the strings in the list into sentences by semicolons.
	 * @param sentences
	 * @return
	 */
	public List<String> splitSentencesBySemicolon(List<String> sentences) {
		List<String> result = new LinkedList<String>();
		for (String sentence : sentences) {
			result.addAll(splitSentencesBySemicolon(sentence));
		}
		
		return result;
	}

	
	/**
	 * split the given string into sentences by semicolons.
	 * @param sentence
	 * @return
	 */
	public List<String> splitSentencesBySemicolon(String sentence) {
		List<String> result = new LinkedList<String>();
		String[] sentenceArray = sentence.split("\\;");
		for ( int i = 0; i < sentenceArray.length; i++ ) {
			String subSentence = sentenceArray[i];
			subSentence = subSentence.trim();
			subSentence = subSentence.substring(0, 1).toUpperCase() + subSentence.substring(1);
			
			String lastCharInSubSentence = subSentence.substring(subSentence.length()-1, subSentence.length());
			
			if ( ! lastCharInSubSentence.equals(".")) {
				subSentence += ".";
			}
			result.add(subSentence);
		}	
		
		return result;
	}	
	
	
	/**
	 * CharaParser's segmentSentence
	 * @param sentences
	 * @param sentenceDetector
	 * @param tokenizer
	 * @param wordNetPOSKnowledgeBase
	 * @return
	 */
	private List<String> getSentencesDongyeMengSegmentSent(List<String> sentences,
			ITokenizer sentenceDetector,
			ITokenizer tokenizer,
			WordNetPOSKnowledgeBase wordNetPOSKnowledgeBase) {
		
		//System.out.println("Go to getSentencesStep4()");
		List<String> result = new LinkedList<String>();		


		for (String sentence : sentences) {
			//System.out.println("sentence::" + sentence);
			
			List<Token> tokenList = new ArrayList<Token>();
			// this.tester = new LearnerUtility(sentenceDetector, tokenizer, wordNetPOSKnowledgeBase);
			//tokenList = this.tester.segmentSentence(sentence);
			LearnerUtility tester = new LearnerUtility(sentenceDetector, tokenizer, wordNetPOSKnowledgeBase);
			tokenList = tester.segmentSentence(sentence);
			
			for ( int i = 0; i < tokenList.size(); i++ ) {
				String subSent = tokenList.get(i).getContent();
				//System.out.println("segmentSentence()::" + subSent);
				result.add(subSent);
			}			
		}
		return result;
	}
	

	/**
	 * @param text
	 * @return
	 */
	public String replaceCapitalPeriod(String text) {
		String testString = text;
		
		String targetPatternString = "(\\s[A-Z]\\.\\s)";
		Pattern pattern = Pattern.compile(targetPatternString);
		Matcher matcher = pattern.matcher(testString);
		
		while (matcher.find()) {
			String matchString = matcher.group(1);
			String newMatchString = "";
			for ( int j = 0; j < matchString.length(); j++ ) {
				newMatchString += matchString.substring(j, j+1) + " ";
			}
			testString = testString.replaceAll(matcher.group(1), newMatchString);
		}
		return testString;
	}
	
	
	/**
	 * . or ? plus spaces and the latter character is captalized
	 * @param text
	 * @return
	 */
	public List<String> preSplit(String text){
		String reg = "[.?]\\s+[A-Z]";
		Pattern p = Pattern.compile(reg);
		Matcher m = p.matcher(text);
		int start = 0;
		int end = 0;
		List<String> sentences = new ArrayList<String>();
		while(m.find())
		{
			end = m.start()+1;
			sentences.add(text.substring(start,end).trim());
			start = end;
		}
		end = text.length();
		sentences.add(text.substring(start,end).trim());
		return sentences;
	}
	
	
	/**
	 * 1, seprate by ; 
	 * 2, extract the inner clause embedded by brackets.
	 * @return
	 */
	public List<SubSentence> detectSnippet(Sentence sentence){
		List<SubSentence> sentArr = new ArrayList();
		String[] sentences = splitBySemicolon(sentence.getText());
		for(String sent: sentences){
			//int leftBracket = sent.indexOf("(");
			//int rightBracket = sent.indexOf(")");
			Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");
	        Matcher matcher = pattern.matcher(sent);
	        while(matcher.find()){
	        	String innerClause = matcher.group();
	        	
	        	SubSentence ic = new SubSentence();
				ic.setContent(innerClause);
				ic.setLength(innerClause.length());
				ic.setMainSentence(sentence);
				//ic.setStart(leftBracket+1);
				sentArr.add(ic);
				sent = sent.replace(innerClause, "");
	        }
	        sent = sent.replace("(", "");
	        sent = sent.replace(")", "");
	        /*
			if(leftBracket>-1&&rightBracket>-1){
				//System.out.println(sent);
				String innerClause = sent.substring(leftBracket+1, rightBracket);
				String outerClause  = sent.substring(0, leftBracket)+sent.substring(rightBracket+1, sent.length());
				
				StringSnippet ic = new StringSnippet();
				ic.setContent(innerClause);
				ic.setDocId(docId);
				ic.setSentId(sentId);
				ic.setLength(innerClause.length());
				ic.setStart(leftBracket+1);
				sentArr.add(ic);
				
				StringSnippet oc = new StringSnippet();
				oc.setContent(outerClause);
				oc.setDocId(docId);
				oc.setSentId(sentId);
				oc.setLength(outerClause.length());
				oc.setStart(0);
				sentArr.add(oc);
			}
	         */
				SubSentence oc = new SubSentence();
				oc.setContent(sent);
				oc.setMainSentence(sentence);
				oc.setLength(sent.length());
				oc.setStart(0);
				sentArr.add(oc);
		}
		return sentArr;
	}
	
	
	/**
	 * 1, seprate by ; 
	 * 2, extract the inner clause embedded by brackets.
	 * @return
	 */
	public String removeBrackets(String sent){
		//int leftBracket = sent.indexOf("(");
		//int rightBracket = sent.indexOf(")");
		Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");
        Matcher matcher = pattern.matcher(sent);
        while(matcher.find()){
        	String innerClause = matcher.group();
			sent = sent.replace(innerClause, "");
        }
        sent = sent.replace("(", "");
        sent = sent.replace(")", "");
        sent = sent.replace(" ,", ",").replace(" :", ":").replace(" .", ".");
		return sent;
	}
	
	/**
	 * 1, seprate by ; 
	 * 2, extract the inner clause embedded by brackets.
	 * @return
	 */
	public String removeSquBrackets(String sent){
		//int leftBracket = sent.indexOf("(");
		//int rightBracket = sent.indexOf(")");
		Pattern pattern = Pattern.compile("(?<=\\[)(.+?)(?=\\])");
        Matcher matcher = pattern.matcher(sent);
        while(matcher.find()){
        	String innerClause = matcher.group();
			sent = sent.replace(innerClause, "");
        }
        sent = sent.replace("[", "");
        sent = sent.replace("]", "");
		return sent;
	}
	
	/**
	 * split a sentence into sentences
	 * @param sentence
	 * @return
	 */
	public String[] splitBySemicolon(String sentence){
		return sentence.split(";");
	}
	
	
	
	/**
	 * Split sentences from a single file
	 * @param inputFile
	 * @return
	 */
	public List<MultiClassifiedSentence> createSentencesFromFile(TaxonTextFile taxonFile) {
		List<String> sentences = this.split(taxonFile.getText());
		if(sentences==null) return null;
		List<MultiClassifiedSentence> result = new LinkedList<MultiClassifiedSentence>();
		for (String subsentence : sentences) {
			MultiClassifiedSentence sentence = new MultiClassifiedSentence(subsentence);
			result.add(sentence);
		}
		
		return result;
	}
	
	
	public static void main(String[] args){
		SentenceSpliter spliter = new SentenceSpliter(null, null); 
		System.out.println(spliter.removeBrackets("The following amounts of fermentation acids (in milliequivalents per 100 ml of culture; mean % standard error of the mean), are produced in PYG-serum broth cultures: succinic acid, 2.9 2 0.5; acetic acid, 0.9 -+ 0.2; and lactic acid, 0.2 k 0.1."));
	}
}
