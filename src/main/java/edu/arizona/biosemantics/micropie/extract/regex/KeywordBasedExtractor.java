package edu.arizona.biosemantics.micropie.extract.regex;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		text = text.substring(0, text.length()-1);
		text = " " + text + " ";
		
		Set<String> returnCharacterStrings = new HashSet<String>();
		for (String keywordString : keywords) {
			keywordString = keywordString.toLowerCase();
			String patternString = "\\s"+keywordString+"\\,|\\s"+keywordString+"\\s|^"+keywordString+"\\s|^"+keywordString+"\\,"; // regular expression pattern
			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(text.toLowerCase());			
			if (matcher.find() && keywordString.length() > 1) {
				String matchString = matcher.group().trim();
				if(matchString.substring(matchString.length()-1, matchString.length()).equals(",")) {
					matchString = matchString.substring(0, matchString.length()-1);
				}
				returnCharacterStrings.add(matchString);
				// System.out.println(keywordString + "::" + matchString);
			}
		}			
		return returnCharacterStrings;
	}

}
