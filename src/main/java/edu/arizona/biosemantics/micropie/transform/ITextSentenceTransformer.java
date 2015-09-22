package edu.arizona.biosemantics.micropie.transform;

import java.util.List;

import edu.arizona.biosemantics.micropie.model.RawSentence;

public interface ITextSentenceTransformer {

	public List<RawSentence> transform(String text);
	
}
