package edu.arizona.biosemantics.micropie.model;

/**
 * the super class of sentences
 * @author maojin
 *
 */
public class Sentence {
	protected String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}