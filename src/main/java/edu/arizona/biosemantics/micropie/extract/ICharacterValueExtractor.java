package edu.arizona.biosemantics.micropie.extract;

import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.Sentence;


/**
 * The interface of Character Value Extractor
 * @author maojin
 *
 */
public interface ICharacterValueExtractor {

	public List<CharacterValue> getCharacterValue(Sentence sentence);
	
	public String getCharacterName();
	
	public ILabel getLabel();
	
}
