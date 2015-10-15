package edu.arizona.biosemantics.micropie.model;


/**
 * The general concept of Phrase:
 * noun phrase: egg yolk agar
 * verb pharse: take out
 * a long part of term sequence: colonies on solid medium
 * @author maojin
 * 
 */
public class Phrase {
	private String text;
	private int start;
	private int end;
	private int role;//according to the language 
	private String negation;//the negation word
	
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	public int getRole() {
		return role;
	}
	public void setRole(int role) {
		this.role = role;
	}
	public String getNegation() {
		return negation;
	}
	public void setNegation(String negation) {
		this.negation = negation;
	}
}
