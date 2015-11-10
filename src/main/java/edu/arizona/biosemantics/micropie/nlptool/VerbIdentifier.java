package edu.arizona.biosemantics.micropie.nlptool;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.stanford.nlp.ling.TaggedWord;


/**
 * identify verb and verb phrases
 * @author maojin
 *
 */
public class VerbIdentifier {
	/**
	 * allowed tags in the verb phrases
	 */
	private static Set verbTagSet = new HashSet();
	static{
		verbTagSet.add("VBZ");// verb, present tense,3rd person singular
		verbTagSet.add("VBP");//are
		verbTagSet.add("VBD");//verb, past tense
		verbTagSet.add("VBN");// verb, past participle
		verbTagSet.add("VBG");//verb, present participle or gerund
		verbTagSet.add("VB");
	}
	
	/**
	 * identify the direct a head verb
	 * @param preIndex
	 * @param taggedwordsList
	 * @return
	 */
	public TaggedWord identifyAheadVerb(int preIndex, List<TaggedWord> taggedwordsList) {
		int size = taggedwordsList.size();
		for(int checkIndex = size-1;checkIndex>=0;checkIndex--){
			String word = taggedwordsList.get(checkIndex).word();
			String tag = taggedwordsList.get(checkIndex).tag();
			if(verbTagSet.contains(tag)){
				return taggedwordsList.get(checkIndex);
			}
		}
		return null;
	}

	/**
	 * find the nearest verb for a given phrase
	 * @param phrase
	 * @param taggedwordsList
	 * @return
	 */
	public TaggedWord identifyNearestVerb(Phrase phrase, List<TaggedWord> taggedwordsList) {
		int startIndex = phrase.getStartIndex();
		int endIndex = phrase.getEndIndex();
		
		for(int step=1;step<taggedwordsList.size();step++){
			if(startIndex-step>0){
				if(verbTagSet.contains(taggedwordsList.get(startIndex-step).tag())) return taggedwordsList.get(startIndex-step);
			}
			if(endIndex+step<taggedwordsList.size()){
				if(verbTagSet.contains(taggedwordsList.get(endIndex+step).tag())) return taggedwordsList.get(endIndex+step);
			}
		}
		return null;
	}

}
