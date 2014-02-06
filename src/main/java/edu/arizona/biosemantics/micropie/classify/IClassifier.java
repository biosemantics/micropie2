package edu.arizona.biosemantics.micropie.classify;

import edu.arizona.biosemantics.micropie.model.Sentence;

/**
 * A classifier predicts the {@link edu.arizona.biosemantics.micropie.classify.sista.ista555.Label} for a review
 * @author rodenhausen
 */
public interface IClassifier {

	/**
	 * @param review
	 * @return the predicted label
	 * @throws Exception
	 */
	public ILabel getClassification(Sentence sentence) throws Exception;
	
	
}
