package edu.arizona.biosemantics.micropie.io;

import java.util.List;

import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;

public interface IClassifiedSentenceWriter {

	public void write(List<MultiClassifiedSentence> classifiedSentences) throws Exception;
	
}
