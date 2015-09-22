package edu.arizona.biosemantics.micropie.classify;

import java.util.List;

import edu.arizona.biosemantics.micropie.model.RawSentence;

/**
 * A TrainableClassifier can be trained to improve prediction capability of {@link edu.arizona.biosemantics.micropie.classify.sista.ista555.Label} 
 * @author rodenhausen
 */
public interface ITrainableClassifier {

	/**
	 * @param trainingData
	 * @throws Exception
	 */
	public void train(List<RawSentence> trainingData) throws Exception;
	
}
