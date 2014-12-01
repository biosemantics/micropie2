package edu.arizona.biosemantics.micropie.transform;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;



import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.name.Named;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.InvalidFormatException;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.util.CoreMap;
import semanticMarkup.know.lib.WordNetPOSKnowledgeBase;
import semanticMarkup.knowledge.KnowledgeBase;
import semanticMarkup.ling.learn.Configuration;
import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.utility.LearnerUtility;
import semanticMarkup.ling.Token;
import semanticMarkup.ling.transform.ITokenizer;
import semanticMarkup.ling.transform.lib.OpenNLPSentencesTokenizer;
import semanticMarkup.ling.transform.lib.OpenNLPTokenizer;


public class SentenceSplitRun implements Callable<List<String>> {
	
	private String text;
	private CountDownLatch sentenceSplitLatch;
	private ITextNormalizer normalizer;
	private StanfordCoreNLP stanfordCoreNLP;
	private String celsius_degreeReplaceSourcePattern;
	
	

	private LearnerUtility tester;
	private String resFolder;
	private String kbFolder;


	
	public SentenceSplitRun(String text, ITextNormalizer normalizer, StanfordCoreNLP stanfordCoreNLP, 
			CountDownLatch sentenceSplitLatch,
			@Named("celsius_degreeReplaceSourcePattern") String celsius_degreeReplaceSourcePattern,
			@Named("resFolder") String resFolder) {
		this.text = text;
		this.sentenceSplitLatch = sentenceSplitLatch;
		this.normalizer = normalizer;
		this.stanfordCoreNLP = stanfordCoreNLP;
		this.celsius_degreeReplaceSourcePattern = celsius_degreeReplaceSourcePattern;
		this.resFolder = resFolder;
	}

	@Override
	public List<String> call() throws Exception {

		Configuration myConfiguration = new Configuration(resFolder);
		ITokenizer sentenceDetector = new OpenNLPSentencesTokenizer(
				myConfiguration.getOpenNLPSentenceDetectorDir());
		ITokenizer tokenizer = new OpenNLPTokenizer(myConfiguration.getOpenNLPTokenizerDir());
		
		System.out.println("myConfiguration.getOpenNLPSentenceDetectorDir()::" + myConfiguration.getOpenNLPSentenceDetectorDir());
		System.out.println("myConfiguration.getWordNetDictDir()::" + myConfiguration.getWordNetDictDir());
		String source = myConfiguration.getOpenNLPSentenceDetectorDir();
		// openNlpSentSplitter(source);
		
		
		WordNetPOSKnowledgeBase wordNetPOSKnowledgeBase = null;
		try {
			wordNetPOSKnowledgeBase = new WordNetPOSKnowledgeBase(myConfiguration.getWordNetDictDir(), false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		log(LogLevel.INFO, "Normalize text... : " + text);
		
		text = normalizer.transform(text);
		// System.out.println("normalized text::" + text);
		
		log(LogLevel.INFO, "Done normalizing, resulting text: " + text);
		
		Set<String> termWithPeriod = getTermWithPeriod(text);
		String[] termWithPeriodArray = termWithPeriod.toArray(new String[0]);
		System.out.println(Arrays.toString(termWithPeriodArray));
		
		
		log(LogLevel.INFO, "Splitting text into sentences");
		List<String> sentences =  getSentences(text);
		log(LogLevel.INFO, "Done splitting text into sentences. Created " + sentences.size() + " + sentences");
		
		
		// TODO:: Splitting
		// Example: Sent1; Sent2; Sent3; Sent4.
		// =>
		// Sent1
		// Sent2
		// Sent3
		// Sent4
		// => See getSentencesStep3(sentences2)
		
		
		/*
		log(LogLevel.INFO, "Replacing abbreviation back to original sentence");
		// replace abbreviation back to original sentence
		List<String> sentencesBack = new ArrayList<String>();
		for (String sentence: sentences) {
			sentence = normalizer.transformBack(sentence);
			sentencesBack.add(sentence);
		}
		log(LogLevel.INFO, "Done replacing abbreviation back to original sentence");

		// List<String> sentences2 = new ArrayList<String>();
		List<String> sentences2 = new LinkedList<String>();
		
		// sentences2 = getSentencesDongyeMengSegmentSent(sentencesBack, sentenceDetector, tokenizer, wordNetPOSKnowledgeBase);
		
		sentences2 = getSentencesSsplit(sentencesBack); // run Stanford Corenlp again
		sentences2 = getSentencesSemicolon(sentences2); // run ";" semicolon separator
		// sentences2 = getSentencesOpennlp(sentences2, source);
		// sentences2 = getSentencesDongyeMengSegmentSent(sentences2, sentenceDetector, tokenizer, wordNetPOSKnowledgeBase); // run CharaParser's segmentSentence

		sentences2 = getTransformedDash(sentences2);
		sentences2 = getTransformedPeriod(sentences2);
		
		sentenceSplitLatch.countDown();
		// return sentences;
		// return sentencesBack;
		return sentences2;
		*/


		// List<String> sentences2 = new ArrayList<String>();
		List<String> sentences2 = new LinkedList<String>();
		
		// sentences2 = getSentencesDongyeMengSegmentSent(sentencesBack, sentenceDetector, tokenizer, wordNetPOSKnowledgeBase);
		
		sentences = getSentencesSsplit(sentences); // run Stanford Corenlp again
		sentences = getSentencesSemicolon(sentences); // run ";" semicolon separator
		// sentences = getSentencesOpennlp(sentences, source);
		// sentences = getSentencesDongyeMengSegmentSent(sentences, sentenceDetector, tokenizer, wordNetPOSKnowledgeBase); // run CharaParser's segmentSentence

		sentences = getTransformedDash(sentences);
		sentences = getTransformedPeriod(sentences); // · => .
		
		

		
		
		log(LogLevel.INFO, "Replacing abbreviation back to original sentence");
		// replace abbreviation back to original sentence
		List<String> sentencesBack = new ArrayList<String>();
		for (String sentence: sentences) {
			sentence = normalizer.transformBack(sentence);
			sentencesBack.add(sentence);
		}
		log(LogLevel.INFO, "Done replacing abbreviation back to original sentence");
		
		sentencesBack = getTransformedCelsiusDegree(sentencesBack); // °C => celsius_degree
		
		sentenceSplitLatch.countDown();
		// return sentences;
		// return sentencesBack;
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
		log(LogLevel.INFO, "done splitting text to sentences using stanford corenlp pipeline. Created " + result.size() + " sentences");
		return result;
	}
	

	
	
	private List<String> getSentencesSsplit(List<String> sentences) {
		log(LogLevel.INFO, "ssplit2:: split text to sentences using stanford corenlp pipeline...");
		List<String> result = new LinkedList<String>();
		
		for (String sentence : sentences) {
			
			sentence = getSplitCapitalPeriodSent(sentence); // => add explanation here!!
			
			Annotation annotation = new Annotation(sentence);
			stanfordCoreNLP.annotate(annotation);
			List<CoreMap> sentenceAnnotations = annotation
					.get(SentencesAnnotation.class);
			
			System.out.println("sentence::" + sentence);
			for (CoreMap sentenceAnnotation : sentenceAnnotations) {
				System.out.println("Sub-sentence::" + sentenceAnnotation.toString());
				
				result.add(sentenceAnnotation.toString());
			}	
		}			
		
		log(LogLevel.INFO, "done ssplit2:: splitting text to sentences using stanford corenlp pipeline. Created " + result.size() + " sentences");
		
		return result;
	}
	
	private List<String> getSentencesSemicolon(List<String> sentences) {
		log(LogLevel.INFO, "ssplit3:: split text to sentences using semicolon separator...");
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
		
		log(LogLevel.INFO, "done ssplit3:: splitting text to sentences using semicolon separator. Created " + result.size() + " sentences");
		
		return result;
	}	
	
	
	
	private List<String> getSentencesDongyeMengSegmentSent(List<String> sentences,
			ITokenizer sentenceDetector,
			ITokenizer tokenizer,
			WordNetPOSKnowledgeBase wordNetPOSKnowledgeBase) {
		log(LogLevel.INFO, "ssplit3:: split text to sentences using Dongye Meng's segmentSentence function...");
		
		System.out.println("Go to getSentencesStep4()");
		List<String> result = new LinkedList<String>();		


		for (String sentence : sentences) {
			System.out.println("sentence::" + sentence);
			
			List<Token> tokenList = new ArrayList<Token>();
			// this.tester = new LearnerUtility(sentenceDetector, tokenizer, wordNetPOSKnowledgeBase);
			//tokenList = this.tester.segmentSentence(sentence);
			tester = new LearnerUtility(sentenceDetector, tokenizer, wordNetPOSKnowledgeBase);
			tokenList = tester.segmentSentence(sentence);
			
			for ( int i = 0; i < tokenList.size(); i++ ) {
				String subSent = tokenList.get(i).getContent();
				System.out.println("segmentSentence()::" + subSent);
				result.add(subSent);
			}			
		}
		log(LogLevel.INFO, "done ssplit3:: splitting text to sentences using Dongye Meng's segmentSentence function. Created " + result.size() + " sentences");
		return result;
	}
	
	private List<String> getTransformedDash(List<String> sentences) throws FileNotFoundException {
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

	
	private List<String> getTransformedPeriod(List<String> sentences) throws FileNotFoundException {
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
	
	
	

	private List<String> getSentencesOpennlp(List<String> sentences, String opennlpSentDetectorSource) throws FileNotFoundException {
		log(LogLevel.INFO, "ssplit3:: split text to sentences using Opennlp sentence detector...");

		List<String> result = new LinkedList<String>();		

		InputStream modelIn = new FileInputStream(opennlpSentDetectorSource);
		try {
			
			SentenceModel model = new SentenceModel(modelIn);
			SentenceDetectorME sdetector = new SentenceDetectorME(model);
			for (String sentence : sentences) {
				String subSentences[] = sdetector.sentDetect(sentence);
				for ( int i = 0; i < subSentences.length; i++ ) {
					String subSent = subSentences[i];
					System.out.println("subSent::" + subSent);
					result.add(subSent);
				}
			}
			modelIn.close();		  
		}
		catch (IOException e) {
		  e.printStackTrace();
		}
		finally {
		  if (modelIn != null) {
		    try {
		      modelIn.close();
		    }
		    catch (IOException e) {
		    }
		  }
		}
		log(LogLevel.INFO, "done ssplit3:: splitting text to sentences using Opennlp sentence detector. Created " + result.size() + " sentences");
		return result;
	}	

	private void openNlpSentSplitter(String source) throws InvalidFormatException, IOException {
		// This is just for testing
		String paragraph = "Hi. How are you? This is Mike. This is Elvis A. A cat in the hat. The type strain is KOPRI 21160T (= KCTC 23670T= JCM 18092T), isolated from a soil sample collected near the King Sejong Station on King George Island, Antarctica. The DNA G+ C content of the type strain is 30.0 mol%.";

		InputStream modelIn = new FileInputStream(source);

		try {
		    // SentenceModel model = new SentenceModel(modelIn);
			// InputStream is = new FileInputStream(myConfiguration.getOpenNLPTokenizerDir());
			
			SentenceModel model = new SentenceModel(modelIn);
			SentenceDetectorME sdetector = new SentenceDetectorME(model);
			String sentences[] = sdetector.sentDetect(paragraph);
			for ( int i = 0; i < sentences.length; i++ ) {
				System.out.println(sentences[i]);
			}
			modelIn.close();		  
		}
		catch (IOException e) {
		  e.printStackTrace();
		}
		finally {
		  if (modelIn != null) {
		    try {
		      modelIn.close();
		    }
		    catch (IOException e) {
		    }
		  }
		}
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
			
			log(LogLevel.INFO, "getSplitCapitalPeriodSent::OriginalSent::" + text);
			log(LogLevel.INFO, "matchString:: " + matchString);
			log(LogLevel.INFO, "newMatchString:: " + newMatchString);
			
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
