package edu.arizona.biosemantics.micropie.io;

import java.util.List;
import java.util.Map;

import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;

public interface IClassifiedSentenceWriter {

	public void write(List<MultiClassifiedSentence> classifiedSentences) throws Exception;
	
}
