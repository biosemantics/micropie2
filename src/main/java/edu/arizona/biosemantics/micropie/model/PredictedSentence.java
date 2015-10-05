package edu.arizona.biosemantics.micropie.model;

import edu.arizona.biosemantics.micropie.classify.ILabel;


/**
 * predicted sentences
 * @author maojin
 *
 */
public class PredictedSentence extends Sentence{
	private ILabel label = null;
	private ILabel prediction = null;
	
	public ILabel getLabel() {
		return label;
	}
	public void setLabel(ILabel label) {
		this.label = label;
	}
	public ILabel getPrediction() {
		return prediction;
	}
	public void setPrediction(ILabel prediction) {
		this.prediction = prediction;
	}
}
