package edu.arizona.biosemantics.micropie.transform;

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
		sentenceSplitLatch.countDown();
		return sentences;
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

}
