package edu.arizona.biosemantics.micropie.nlptool;

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
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.util.CoreMap;



/**
 * TODO: This class needs to be rewritten.
 * 
 * @author
 *
 */
public class SentenceSplitRun implements Callable<List<String>> {
	
	private String text;
	private CountDownLatch sentenceSplitLatch;
	//private LearnerUtility tester;
	private SentenceSpliter sentenceSpliter;

	
	public SentenceSplitRun(String text, 
			CountDownLatch sentenceSplitLatch,
			SentenceSpliter sentenceSpliter) {
		this.text = text;
		this.sentenceSplitLatch = sentenceSplitLatch;
		this.sentenceSpliter = sentenceSpliter;
	}

	@Override
	public List<String> call() throws Exception {
		List sentencesBack = sentenceSpliter.split(text);
		sentenceSplitLatch.countDown();
		return sentencesBack;		
	}
	
	
}
