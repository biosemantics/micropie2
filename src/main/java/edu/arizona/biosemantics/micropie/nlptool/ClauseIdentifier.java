package edu.arizona.biosemantics.micropie.nlptool;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.ling.TaggedWord;


/**
 * some simple tools for clause identifier
 * @author maojin
 *
 */
public class ClauseIdentifier {
	
	private Set separatorSet = new HashSet();
	
	/**
	 * separate different clauses
	 * 1, identify and/or/but/while/however
	 * 2, whether there are many verbs: VB, conj+VBP
	 * @param taggedwordsList
	 * @return
	 */
	private int detectSeparate(List<TaggedWord> taggedwordsList) {
		
		//1, identify and/or/but/while/however
		for(int index=0;index<taggedwordsList.size();index++){
			
		}
		return 0;
	}
}
