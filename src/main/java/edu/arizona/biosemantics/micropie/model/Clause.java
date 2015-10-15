package edu.arizona.biosemantics.micropie.model;


/**
 * find the clause 
 * @author maojin
 *
 */
public class Clause {
	private String text;
	private int startPosition;
	private int endPosition;
	
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getStartPosition() {
		return startPosition;
	}
	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}
	public int getEndPosition() {
		return endPosition;
	}
	public void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}
}
