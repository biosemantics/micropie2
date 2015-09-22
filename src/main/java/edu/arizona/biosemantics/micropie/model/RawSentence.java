package edu.arizona.biosemantics.micropie.model;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.common.log.LogLevel;


/**
 * Basic model of sentences
 * one sentence with one classification
 */
public class RawSentence extends Sentence implements Cloneable {

	private ILabel label = null;
	
	public RawSentence(String text) {
		this.text = text;
	}
	
	public RawSentence(String text, ILabel label) {
		this.text = text;
		this.label = label;
	}
	
	/*public Sentence(String text, String sourceFile, int idInSourceFile) {
		this.text = text;
		this.sourceFile = sourceFile;
		this.idInSourceFile = idInSourceFile;
	}*/
	

	public ILabel getLabel() {
		return label;
	}

	public void setLabel(ILabel label) {
		this.label = label;
	}

	@Override
	public Object clone() {
		try {
			RawSentence clone = (RawSentence)super.clone();
			clone.label = this.label;
			clone.text = this.text;
			return clone;
		} catch (CloneNotSupportedException e) {
			log(LogLevel.ERROR, "Could not clone sentence", e);
		}
		return null;
	}
}
