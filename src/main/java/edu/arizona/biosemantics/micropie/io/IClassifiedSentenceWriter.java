package edu.arizona.biosemantics.micropie.io;

import java.util.List;
import java.util.Map;

import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;

/**
 * write sentences
 * @author maojin
 */
public interface IClassifiedSentenceWriter {

	/**
	 * output the results in a CSV file
	 * Format: categoryLabel,sentence
	 * 
	 */
	public void write(List<MultiClassifiedSentence> classifiedSentences) throws Exception;
	
}
