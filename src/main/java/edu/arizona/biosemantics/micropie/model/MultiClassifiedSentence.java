package edu.arizona.biosemantics.micropie.model;

import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;

public class MultiClassifiedSentence {

	private Set<ILabel> predictions;
	private Sentence sentence;
	
	public MultiClassifiedSentence(Sentence sentence, Set<ILabel> predictions) {
		this.sentence = sentence;
		this.predictions = predictions;
	}

	public Set<ILabel> getPredictions() {
		return predictions;
	}

	public void setPredictions(Set<ILabel> predictions) {
		this.predictions = predictions;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}
	
	
	
}
