package edu.arizona.biosemantics.micropie.extract.regex;

import com.google.inject.Inject;

import edu.arizona.biosemantics.micropie.classify.ILabel;

public abstract class AbstractCharacterValueExtractor implements ICharacterValueExtractor {

	private String character;
	private ILabel label;
	
	@Inject
	public AbstractCharacterValueExtractor(ILabel label, String character) {
		this.label = label;
		this.character = character;
	}
	
	@Override
	public String getCharacter() {
		return character;
	}

	@Override
	public ILabel getLabel() {
		return label;
	}

}
