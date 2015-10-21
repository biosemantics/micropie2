package edu.arizona.biosemantics.micropie.extract.regex;

import java.util.List;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.Sentence;


/**
 * extract salinity preferences
 * @author maojin
 *
 */
public class SalinityPreferenceExtractor extends AbstractCharacterValueExtractor{

	public SalinityPreferenceExtractor(ILabel label, String characterName) {
		super(label, characterName);
	}

	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		// TODO Auto-generated method stub
		return null;
	}

}
