package edu.arizona.biosemantics.micropie;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import edu.arizona.biosemantics.micropie.io.XMLTextReader;
import edu.arizona.biosemantics.micropie.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.transform.ITextTransformer;
import edu.arizona.biosemantics.micropie.transform.MyTextSentenceTransformer;

public class TestSentencesExtractor implements Callable<TestSentenceExtractResult> {

	private File inputFile;
	private String taxon;
	private String text;
	private ITextTransformer textNormalizer;
	private MyTextSentenceTransformer textSentenceTransformer;
	private CountDownLatch countDownLatch;

	public TestSentencesExtractor(File inputFile, String taxon, String text,
			ITextTransformer textNormalizer, MyTextSentenceTransformer textSentenceTransformer, CountDownLatch countDownLatch) {
		this.inputFile = inputFile;
		this.taxon = taxon;
		this.text = text;
		this.textNormalizer = textNormalizer;
		this.textSentenceTransformer = textSentenceTransformer;
		this.countDownLatch = countDownLatch;
	}

	@Override
	public TestSentenceExtractResult call() throws Exception {
		TestSentenceExtractResult result = new TestSentenceExtractResult();
		
		text = textNormalizer.transform(text);
		log(LogLevel.INFO, "Normalized text: " + text);
		List<Sentence> sentences = textSentenceTransformer.transform(text);
		result.setSentences(sentences);
		for(int i=0; i<sentences.size(); i++) {
			Sentence sentence = sentences.get(i);
			SentenceMetadata metadata = new SentenceMetadata();
			metadata.setSourceFile(inputFile.getName());
			metadata.setSourceId(i);
			metadata.setTaxon(taxon);
			metadata.setCompoundSplitSentence(sentences.size() > 1);
			metadata.setParseResult(textSentenceTransformer.getCachedParseResult(sentence));
			result.putSentenceMetadata(sentence, metadata);
			result.addTaxonSentence(taxon, sentence);
		}
		countDownLatch.countDown();
		System.out.println("Count " + countDownLatch.getCount());
		return result;
	}
}
