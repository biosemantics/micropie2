package edu.arizona.biosemantics.micropie.extract.keyword;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.Sentence;


/**
 * 
 * @author maojin
 *
 */
public class InorganicSubstanceExtractor extends PhraseBasedExtractor{
	public InorganicSubstanceExtractor(ILabel label, String character,
			Set<String> keywords, Map<String, List> subKeyword) {
		super(Label.c55, character, keywords, subKeyword);
	}
	
	
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		return super.getCharacterValue(sentence);
	}
	
	
}
