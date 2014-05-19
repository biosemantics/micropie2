package edu.arizona.biosemantics.micropie.transform;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import edu.arizona.biosemantics.micropie.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.util.CoreMap;


public class SentenceSplitRun implements Callable<List<String>> {
	
	private String text;
	private CountDownLatch sentenceSplitLatch;
	private ITextNormalizer normalizer;
	private StanfordCoreNLP stanfordCoreNLP;


	public SentenceSplitRun(String text, ITextNormalizer normalizer, StanfordCoreNLP stanfordCoreNLP, 
			CountDownLatch sentenceSplitLatch) {
		this.text = text;
		this.sentenceSplitLatch = sentenceSplitLatch;
		this.normalizer = normalizer;
		this.stanfordCoreNLP = stanfordCoreNLP;
	}

	@Override
	public List<String> call() throws Exception {
		log(LogLevel.INFO, "Normalize text... : " + text);
		text = normalizer.transform(text);
		log(LogLevel.INFO, "Done normalizing, resulting text: " + text);
		
		
		
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
		
		
		log(LogLevel.INFO, "Replacing abbreviation back to original sentence");
		// replace abbreviation back to original sentence
		List<String> sentencesBack = new ArrayList<String>();
		for (String sentence: sentences) {
			sentence = normalizer.transformBack(sentence);
			sentencesBack.add(sentence);
		}
		log(LogLevel.INFO, "Done replacing abbreviation back to original sentence");

		List<String> sentences2 = new ArrayList<String>();
		sentences2 =  getSentencesStep2(sentencesBack);
		sentences2 =  getSentencesStep3(sentences2);
		
		
		
		sentenceSplitLatch.countDown();
		// return sentences;
		// return sentencesBack;
		return sentences2;
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
	
	
	
	private List<String> getSentencesStep2(List<String> sentences) {
		log(LogLevel.INFO, "ssplit2:: split text to sentences using stanford corenlp pipeline...");
		List<String> result = new LinkedList<String>();
		
		for (String sentence : sentences) {
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
	
	private List<String> getSentencesStep3(List<String> sentences) {
		log(LogLevel.INFO, "ssplit3:: split text to sentences using stanford corenlp pipeline...");
		List<String> result = new LinkedList<String>();
		
		for (String sentence : sentences) {
			String[] sentenceArray = sentence.split("\\;");
			for ( int i = 0; i < sentenceArray.length; i++ ) {
				String subSentence = sentenceArray[i];
				subSentence = subSentence.trim();
				subSentence = subSentence.substring(0, 1).toUpperCase() + subSentence.substring(1);
				
				String lastCharInSubSentence = subSentence.substring(subSentence.length()-1, subSentence.length());
				System.out.println("lastCharInSubSentence::" + lastCharInSubSentence);
				
				if ( ! lastCharInSubSentence.equals(".")) {
					subSentence += ".";
				}
				
				result.add(subSentence);
			}	
		}			
		
		log(LogLevel.INFO, "done ssplit3:: splitting text to sentences using stanford corenlp pipeline. Created " + result.size() + " sentences");
		
		return result;
	}	
	

}
