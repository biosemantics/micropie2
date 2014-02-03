package edu.arizona.biosemantics.micropie.io;

import java.util.List;

import edu.arizona.biosemantics.micropie.model.Sentence;

public interface ISentenceReader {

	public List<Sentence> read() throws Exception;
	
}
