package edu.arizona.biosemantics.micropie.extract.regex;

import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;

public interface ICharacterValueExtractorProvider {

	public Set<ICharacterValueExtractor> getContentExtractor(Label label);

	public boolean hasExtractor(Label label);
	
}
