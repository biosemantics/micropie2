package edu.arizona.biosemantics.micropie.transform;

import java.util.List;

import edu.arizona.biosemantics.micropie.model.Sentence;

public interface ITextSentenceTransformer {

	public List<Sentence> transform(String text);
	
}
