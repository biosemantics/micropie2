package edu.arizona.biosemantics.micropie.model;

import edu.arizona.biosemantics.micropie.classify.ILabel;

public class ClassifiedSentence {

	private ILabel prediction;
	private Sentence sentence;
	
	public ClassifiedSentence(Sentence sentence, ILabel prediction) {
		this.sentence = sentence;
		this.prediction = prediction;
	}

	public ILabel getPrediction() {
		return prediction;
	}

	public void setPredictions(ILabel prediction) {
		this.prediction = prediction;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}
	
}
