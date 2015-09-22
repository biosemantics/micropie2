package edu.arizona.biosemantics.micropie.classify;

import java.util.Set;

import edu.arizona.biosemantics.micropie.model.RawSentence;

public interface IMultiClassifier {

	public Set<ILabel> predict(RawSentence sentence) throws Exception;
	
}
