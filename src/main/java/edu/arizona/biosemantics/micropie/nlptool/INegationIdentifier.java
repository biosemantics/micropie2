package edu.arizona.biosemantics.micropie.nlptool;

import java.util.List;

import edu.stanford.nlp.ling.TaggedWord;



public interface INegationIdentifier {
	/**
	 * identify a negation near an index
	 */
	public String detectNeighbourNegation(List<TaggedWord> taggedwordsList, int sourceIndex);
	
	/**
	 * identify a negation near an given wordindex
	 */
	public String detectNeighbourNegation(List<TaggedWord> taggedwordsList, TaggedWord verbWord);
}
