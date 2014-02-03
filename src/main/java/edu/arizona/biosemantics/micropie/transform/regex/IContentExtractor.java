package edu.arizona.biosemantics.micropie.transform.regex;

import java.util.Set;

public interface IContentExtractor {

	public Set<String> getContent(String text);
	
	public String getCharacter();
	
}
