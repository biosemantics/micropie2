package edu.arizona.biosemantics.micropie.extract;

import com.google.inject.Inject;

import edu.arizona.biosemantics.micropie.classify.ILabel;

/**
 * Extractors
 * @author maojin
 *
 */
public abstract class AbstractCharacterValueExtractor implements ICharacterValueExtractor {

	protected String characterName;
	protected ILabel label;
	
	@Inject
	public AbstractCharacterValueExtractor(ILabel label, String characterName) {
		this.label = label;
		this.characterName = characterName;
	}
	
	@Override
	public String getCharacterName() {
		return characterName;
	}

	@Override
	public ILabel getLabel() {
		return label;
	}
	
	
	public String toString(){
		return this.getClass().getName();
	}

}
