package edu.arizona.biosemantics.micropie.transform.regex;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;

public interface IContentExtractorProvider {

	public IContentExtractor getContentExtractor(Label label);
	
}
