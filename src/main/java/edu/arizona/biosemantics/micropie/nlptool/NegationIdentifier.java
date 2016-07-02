package edu.arizona.biosemantics.micropie.nlptool;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.TaggedWord;


/**
 * identify explicit negative expressions
 * 
 * @author maojin
 *
 */
public class NegationIdentifier implements INegationIdentifier{
	
	private Set<String> privativeSet = new HashSet();
	{
		privativeSet.add("no");
		privativeSet.add("not");
		privativeSet.add("doesn't");
		privativeSet.add("don't");
		privativeSet.add("didn't");
		privativeSet.add("can't");
		privativeSet.add("cannot");
		privativeSet.add("neither");
		privativeSet.add("nor");
		privativeSet.add("unable");
		privativeSet.add("impossible");
	}
	
	
	private Set<String> negPrefixSet = new HashSet();
	{
		negPrefixSet.add("non");
		negPrefixSet.add("in");
		negPrefixSet.add("un");
		negPrefixSet.add("im");
	}
	
	
	/**
	 * detect the negation which should be near the tagged word
	 * @param taggedwordsList
	 * @param requireIndex
	 * @return
	 */
	public String detectNeighbourNegation(List<TaggedWord> taggedwordsList, int sourceIndex) {
		return detectNegationWithinWindow(taggedwordsList,sourceIndex,2);
	}
	
	
	/**
	 * detect the negation which should be near the tagged word
	 * @param taggedwordsList
	 * @param requireIndex
	 * @return
	 */
	public String detectNegationWithinWindow(List<TaggedWord> taggedwordsList, int sourceIndex, int window) {
		
		for(int checkIndex = sourceIndex-window;checkIndex>=0&&checkIndex<=sourceIndex+window&&checkIndex<taggedwordsList.size();checkIndex++){
			String word = taggedwordsList.get(checkIndex).word().toLowerCase();
			
			if(privativeSet.contains(word)){
				return word;
			}
		}
		return null;
	}



	/**
	 * identify a negation near an given wordindex
	 */
	public String detectNeighbourNegation(List<TaggedWord> taggedwordsList,
			TaggedWord verbWord) {
		for(int i=0;i<taggedwordsList.size();i++){
			if(taggedwordsList.get(i) == verbWord)
			return detectNegationWithinWindow(taggedwordsList, i, 2);
		}
		return null;
	}
	
	
	/**
	 * detect whether it is a negation or not
	 * @param term
	 * @return
	 */
	public static boolean detectInlineNegation(String term){
		String patternString = "\\snon(?=[a-z\\-])|^non(?=[a-z\\-])|negative"; // regular expression pattern
		Matcher matcher = Pattern.compile(patternString).matcher(term);
		//String negation = null;
		if (matcher.find()) {
			//negation = matcher.group().trim();
			return true;
		}
		return false;
	}


	@Override
	public String detectFirstNegation(List<TaggedWord> taggedwordsList) {
		for(int checkIndex = 0;checkIndex<taggedwordsList.size();checkIndex++){
			String word = taggedwordsList.get(checkIndex).word().toLowerCase();
			
			if(privativeSet.contains(word)){
				return word;
			}
		}
		return null;
	}
	
	@Override
	public int detectFirstNegationIndex(List<TaggedWord> taggedwordsList) {
		for(int checkIndex = 0;checkIndex<taggedwordsList.size();checkIndex++){
			String word = taggedwordsList.get(checkIndex).word().toLowerCase();
			
			if(privativeSet.contains(word)){
				return checkIndex;
			}
		}
		return -1;
	}

}
