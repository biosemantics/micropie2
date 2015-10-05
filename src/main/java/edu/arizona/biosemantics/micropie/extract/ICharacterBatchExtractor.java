package edu.arizona.biosemantics.micropie.extract;

import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.model.Sentence;

/**
 * batch extractor for files, or sentences
 * @author maojin
 *
 */
public interface ICharacterBatchExtractor {
	/**
	 * extract the values of the given characters
	 * @param sentences
	 */
	public void extractSentences(List sentences, Set<ILabel> charLabels);
	
	
	/**
	 * extract the values
	 * @param sentences
	 */
	public void extractSentences(List sentences);
	
}