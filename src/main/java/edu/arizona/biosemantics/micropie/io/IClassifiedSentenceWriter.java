package edu.arizona.biosemantics.micropie.io;

import java.util.List;

import edu.arizona.biosemantics.micropie.model.ClassifiedSentence;

public interface IClassifiedSentenceWriter {

	public void write(List<ClassifiedSentence> classifiedSentences) throws Exception;
	
}
