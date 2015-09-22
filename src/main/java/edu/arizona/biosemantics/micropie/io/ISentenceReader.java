package edu.arizona.biosemantics.micropie.io;

import java.util.List;

import edu.arizona.biosemantics.micropie.model.RawSentence;

public interface ISentenceReader {

	public List<RawSentence> read() throws Exception;
	
}
