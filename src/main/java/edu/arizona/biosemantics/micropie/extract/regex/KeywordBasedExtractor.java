package edu.arizona.biosemantics.micropie.extract.regex;

import java.util.Set;

import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;

public class KeywordBasedExtractor extends AbstractCharacterValueExtractor {

	private Set<String> keywords;
	
	public KeywordBasedExtractor(@Named("KeywordBasedExtractor_Label")ILabel label, 
			@Named("KeywordBasedExtractor_Character")String character, 
			@Named("KeywordBasedExtractor_Keywords")Set<String> keywords) {
		super(label, character);
		this.keywords = keywords;
	}

	@Override
	public Set<String> getCharacterValue(String text) {
		// TODO 
		// use keywords and regex to extract character values
		return null;
	}

}
