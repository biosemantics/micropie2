package edu.arizona.biosemantics.micropie.model;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.log.LogLevel;

public class Sentence implements Cloneable {

	private String text;
	private ILabel label = null;
	//private String sourceFile;
	//private int idInSourceFile;
	
	public Sentence(String text) {
		this.text = text;
	}
	
	public Sentence(String text, ILabel label) {
		this.text = text;
		this.label = label;
	}
	
	/*public Sentence(String text, String sourceFile, int idInSourceFile) {
		this.text = text;
		this.sourceFile = sourceFile;
		this.idInSourceFile = idInSourceFile;
	}*/
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public ILabel getLabel() {
		return label;
	}

	public void setLabel(ILabel label) {
		this.label = label;
	}


	/*public String getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	public int getIdInSourceFile() {
		return idInSourceFile;
	}

	public void setIdInSourceFile(int idInSourceFile) {
		this.idInSourceFile = idInSourceFile;
	}
	*/
	
	@Override
	public Object clone() {
		try {
			Sentence clone = (Sentence)super.clone();
			clone.label = this.label;
			clone.text = this.text;
			return clone;
		} catch (CloneNotSupportedException e) {
			log(LogLevel.ERROR, "Could not clone sentence", e);
		}
		return null;
	}
}
