package edu.arizona.biosemantics.micropie.extract.regex;

import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;

public interface ICharacterValueExtractor {

	public Set<String> getCharacterValue(String text);
	
	public String getCharacter();
	
	public ILabel getLabel();
	
}
