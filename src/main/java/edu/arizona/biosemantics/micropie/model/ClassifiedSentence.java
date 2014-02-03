package edu.arizona.biosemantics.micropie.model;

import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;

public class ClassifiedSentence {

	private Set<ILabel> predictions;
	private Sentence sentence;
	
	public ClassifiedSentence(Sentence sentence, Set<ILabel> predictions) {
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
