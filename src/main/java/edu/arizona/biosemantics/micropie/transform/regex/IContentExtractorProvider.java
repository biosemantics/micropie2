package edu.arizona.biosemantics.micropie.transform.regex;

import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;

public interface IContentExtractorProvider {

	public Set<IContentExtractor> getContentExtractor(Label label);

	public boolean hasExtractor(Label label);
	
}
