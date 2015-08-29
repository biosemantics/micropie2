package edu.arizona.biosemantics.micropie.transform;

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
	private StanfordWrapper stanfordWrapper;
	
	@Inject
	public SentenceSpliter(ITextNormalizer normalizer,
			//LearnerUtility tester,
			StanfordWrapper stanfordWrapper){
		this.textNormalizer = normalizer;
		//this.tester = tester;
		//this.celsius_degreeReplaceSourcePattern = celsius_degreeReplaceSourcePattern;
		this.stanfordWrapper = stanfordWrapper;
	}
	
	public List<String> split(String text){
		long b = System.currentTimeMillis();
		long b1 = System.currentTimeMillis();
		text = textNormalizer.transform(text);
		text = textNormalizer.transformEntity(text);
		text = textNormalizer.transformDash(text); // replace \"–\" to \"-\" ..."
		text = textNormalizer.transformPeriod(text); // · =>.
		text = replaceCapitalPeriod(text);
		long e = System.currentTimeMillis();
		System.out.println("long replacements costs "+(e-b)+" ms");
		
		b = System.currentTimeMillis();
		//Standford parser for long text 
		String[] preSents = preSplit(text);
		List<String> sentences = new LinkedList();
		for(String sent: preSents){
			//if(sent.split("\\s+").length>13){//what should it be?
				sentences.addAll(stanfordWrapper.getSentences(sent));
			//}else{
			//	sentences.add(sent);
			//}
		}
		e = System.currentTimeMillis();
		System.out.println("stanford parser costs "+(e-b)+" ms");
		
		//pipeline 
		sentences = splitSentencesBySemicolon(sentences); // run ";" semicolon separator

		// replace abbreviation back to original sentence
		for (String sentence: sentences) {
			//sentence = textNormalizer.transformDash(sentence); // replace \"–\" to \"-\" ..."
			//sentence = textNormalizer.transformPeriod(sentence); // · =>.
			
			//what's the purpose?....
			sentence = textNormalizer.transformBack(sentence);
			
			sentence = textNormalizer.transformCelsiusDegree(sentence); // °C => celsius_degree
			System.out.println(sentence);
		}
		
		long e1 = System.currentTimeMillis();
		System.out.println("all spliting costs "+(e1-b1)+" ms");
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
	public String[] preSplit(String text){
		String reg = "[.?]\\s+[A-Z]";
		return text.split(reg);
	}
	
}
