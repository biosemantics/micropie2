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
 *
 */
public class SentenceSpliter {
	
	private ITextNormalizer normalizer;
	//private LearnerUtility tester;
	private String celsius_degreeReplaceSourcePattern;
	private StanfordCoreNLP stanfordCoreNLP;
	
	@Inject
	public SentenceSpliter(ITextNormalizer normalizer,
			//LearnerUtility tester,
			@Named("celsius_degreeReplaceSourcePattern") String celsius_degreeReplaceSourcePattern,
			StanfordCoreNLP stanfordCoreNLP){
		this.normalizer = normalizer;
		//this.tester = tester;
		this.celsius_degreeReplaceSourcePattern = celsius_degreeReplaceSourcePattern;
		this.stanfordCoreNLP = stanfordCoreNLP;
	}
	
	public List<String> split(String text){
		text = normalizer.transform(text);
		System.out.println("normalized text::" + text);
		
		Set<String> termWithPeriod = getTermWithPeriod(text);
		String[] termWithPeriodArray = termWithPeriod.toArray(new String[0]);
		
		List<String> sentences =  getSentences(text);
		
		sentences = getSentencesSsplit(sentences); // run Stanford Corenlp again
		sentences = getSentencesSemicolon(sentences); // run ";" semicolon separator
		// sentences = getSentencesOpennlp(sentences, source);
		// sentences = getSentencesDongyeMengSegmentSent(sentences, sentenceDetector, tokenizer, wordNetPOSKnowledgeBase); // run CharaParser's segmentSentence

		sentences = getTransformedDash(sentences);
		sentences = getTransformedPeriod(sentences); // · =>
		// replace abbreviation back to original sentence
		List<String> sentencesBack = new ArrayList<String>();
		for (String sentence: sentences) {
			sentence = normalizer.transformBack(sentence);
			sentencesBack.add(sentence);
		}
		
		sentencesBack = getTransformedCelsiusDegree(sentencesBack); // °C => celsius_degree
		
		for(String sent: sentencesBack){
			System.out.println(sent);
		}
		return sentencesBack;
	}
	
	
	private List<String> getSentences(String text) {
		log(LogLevel.INFO, "split text to sentences using stanford corenlp pipeline...");
		List<String> result = new LinkedList<String>();
		Annotation annotation = new Annotation(text);
		stanfordCoreNLP.annotate(annotation);
		List<CoreMap> sentenceAnnotations = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentenceAnnotation : sentenceAnnotations) {
			result.add(sentenceAnnotation.toString());
		}
		return result;
	}
	

	
	
	private List<String> getSentencesSsplit(List<String> sentences) {
		List<String> result = new LinkedList<String>();
		
		for (String sentence : sentences) {
			
			sentence = getSplitCapitalPeriodSent(sentence); // => add explanation here!!
			
			Annotation annotation = new Annotation(sentence);
			stanfordCoreNLP.annotate(annotation);
			List<CoreMap> sentenceAnnotations = annotation
					.get(SentencesAnnotation.class);
			
			//System.out.println("sentence::" + sentence);
			for (CoreMap sentenceAnnotation : sentenceAnnotations) {
				//System.out.println("Sub-sentence::" + sentenceAnnotation.toString());
				
				result.add(sentenceAnnotation.toString());
			}	
		}			
		
		
		return result;
	}
	
	private List<String> getSentencesSemicolon(List<String> sentences) {
		List<String> result = new LinkedList<String>();
		
		for (String sentence : sentences) {
			String[] sentenceArray = sentence.split("\\;");
			for ( int i = 0; i < sentenceArray.length; i++ ) {
				String subSentence = sentenceArray[i];
				subSentence = subSentence.trim();
				subSentence = subSentence.substring(0, 1).toUpperCase() + subSentence.substring(1);
				
				String lastCharInSubSentence = subSentence.substring(subSentence.length()-1, subSentence.length());
				// System.out.println("lastCharInSubSentence::" + lastCharInSubSentence);
				
				if ( ! lastCharInSubSentence.equals(".")) {
					subSentence += ".";
				}
				
				result.add(subSentence);
			}	
		}			
		
		
		return result;
	}	
	
	
	
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
				System.out.println("segmentSentence()::" + subSent);
				result.add(subSent);
			}			
		}
		return result;
	}
	
	private List<String> getTransformedDash(List<String> sentences){
		log(LogLevel.INFO, "getTransformedDash:: replace \"–\" to \"-\" ...");

		List<String> result = new LinkedList<String>();		
		
		for (String sentence : sentences) {
			
			log(LogLevel.INFO, "replacAll:: " + sentence);
			
			sentence = sentence.replaceAll("–", "-"); // To avoid the error ClausIE spliter: the dash will disappear
			// https://d5gate.ag5.mpi-sb.mpg.de/ClausIEGate/ClausIEGate?inputtext=Optimal+temperature+and+pH+for+growth+are+25%E2%80%9330+%CB%9AC+and+pH+7%2C+respectively.&processCcAllVerbs=true&processCcNonVerbs=true&type=true&go=Extract
			sentence = sentence.replaceAll("\\s?-\\s?", "-"); // To avoid the error ClausIE spliter: the dash will disappear
			result.add(sentence);
		}

		log(LogLevel.INFO, "done getTransformedDash:: replace \"–\" to \"-\". Transformed " + result.size() + " sentences");
		return result;
	}

	
	private List<String> getTransformedPeriod(List<String> sentences){
		log(LogLevel.INFO, "getTransformedPeriod:: replace \"·\" to \".\" ...");

		List<String> result = new LinkedList<String>();		
		
		for (String sentence : sentences) {
			log(LogLevel.INFO, "replacAll::2:: " + sentence);
			sentence = sentence.replaceAll("\\·", "."); // To avoid the error ClausIE spliter: the dash will disappear
			// https://d5gate.ag5.mpi-sb.mpg.de/ClausIEGate/ClausIEGate?inputtext=Optimal+temperature+and+pH+for+growth+are+25%E2%80%9330+%CB%9AC+and+pH+7%2C+respectively.&processCcAllVerbs=true&processCcNonVerbs=true&type=true&go=Extract
			sentence = sentence.replaceAll("\\s?\\·\\s?", "."); // To avoid the error ClausIE spliter: the dash will disappear
			result.add(sentence);
		}

		log(LogLevel.INFO, "done getTransformedPeriod:: replace \"·\" to \".\". Transformed " + result.size() + " sentences");
		return result;
	}
	
	
	private List<String> getTransformedCelsiusDegree(List<String> sentences) {
		log(LogLevel.INFO, "getTransformedCelsiusDegree:: replace \"°C\" to \"celsius_degree\" ...");

		List<String> result = new LinkedList<String>();		
		
		// System.out.println("celsius_degreeReplaceSourcePattern::" + celsius_degreeReplaceSourcePattern);
		
		
		for (String sentence : sentences) {
			log(LogLevel.INFO, "replacAll::3:: " + sentence);
			sentence = sentence.replaceAll(celsius_degreeReplaceSourcePattern, " celsius_degree "); // To avoid the error ClausIE spliter: the dash will disappear
			// https://d5gate.ag5.mpi-sb.mpg.de/ClausIEGate/ClausIEGate?inputtext=Optimal+temperature+and+pH+for+growth+are+25%E2%80%9330+%CB%9AC+and+pH+7%2C+respectively.&processCcAllVerbs=true&processCcNonVerbs=true&type=true&go=Extract
			
			// System.out.println("getTransformedCelsiusDegree::" + sentence);
			
			result.add(sentence);
		}
		

		log(LogLevel.INFO, "done getTransformedCelsiusDegree:: replace \"°C\" to \"celsius_degree\". Transformed " + result.size() + " sentences");
		return result;
	}
	
	private String getSplitCapitalPeriodSent(String text) {
		// String testString = "AAA is bbb C. BBB is ccc D. The strain number is 123.";
		
		String testString = text;
		
		/*
		testString = testString.replaceAll("\\·", "."); // To avoid the error ClausIE spliter: the dash will disappear
		// https://d5gate.ag5.mpi-sb.mpg.de/ClausIEGate/ClausIEGate?inputtext=Optimal+temperature+and+pH+for+growth+are+25%E2%80%9330+%CB%9AC+and+pH+7%2C+respectively.&processCcAllVerbs=true&processCcNonVerbs=true&type=true&go=Extract
		testString = testString.replaceAll("\\s?\\·\\s?", "."); // To avoid the error ClausIE spliter: the dash will disappear
		
		testString = testString.replaceAll("–", "-"); // To avoid the error ClausIE spliter: the dash will disappear
		// https://d5gate.ag5.mpi-sb.mpg.de/ClausIEGate/ClausIEGate?inputtext=Optimal+temperature+and+pH+for+growth+are+25%E2%80%9330+%CB%9AC+and+pH+7%2C+respectively.&processCcAllVerbs=true&processCcNonVerbs=true&type=true&go=Extract
		testString = testString.replaceAll("\\s?-\\s?", "-"); // To avoid the error ClausIE spliter: the dash will disappear
		*/
		
		// System.out.println("testString::Before::" + testString);
		
		String targetPatternString = "(\\s[A-Z]\\.\\s)";
		Pattern pattern = Pattern.compile(targetPatternString);
		Matcher matcher = pattern.matcher(testString);
		
		while (matcher.find()) {
			// System.out.println("Whloe Sent::" + matcher.group());
			// System.out.println("Part 1::" + matcher.group(1));
			// System.out.println("Part 2::" + matcher.group(2));
			// System.out.println("Part 3::" + matcher.group(3));
			
			String matchString = matcher.group(1);
			
			String newMatchString = "";
			for ( int j = 0; j < matchString.length(); j++ ) {
				newMatchString += matchString.substring(j, j+1) + " ";
			}
			
			System.out.println("getSplitCapitalPeriodSent::OriginalSent::" + text);
			System.out.println("matchString:: " + matchString);
			System.out.println("newMatchString:: " + newMatchString);
			
			
			testString = testString.replaceAll(matcher.group(1), newMatchString);
			
			// String matchResult = matcher.group(1);
			// System.out.println("matchResult::" + matchResult);
		}
		
		// System.out.println("testString::After::" + testString);
		
		return testString;
		
	}
	
	private Set<String> getTermWithPeriod(String text) {
		Set<String> termWithPeriod = new HashSet();
		
		String textTokens[] = text.split(" ");
		
		for ( int i = 0; i < textTokens.length; i++ ) {
			String tokenString = textTokens[i];
			
			if ( tokenString.matches("(.*)([A-Z]+\\.)(.*)")) {
				// System.out.println("getTermWithPeriod::" + tokenString);
				termWithPeriod.add(tokenString);
			}
			
		}
		
		return termWithPeriod;
	}
}
