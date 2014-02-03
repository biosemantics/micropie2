package edu.arizona.biosemantics.micropie.classify;

import java.util.Set;

import edu.arizona.biosemantics.micropie.model.Sentence;

public interface IMultiClassifier {

	public Set<ILabel> getClassification(Sentence sentence) throws Exception;
	
}
