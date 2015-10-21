package edu.arizona.biosemantics.micropie.model;

import java.util.List;

import edu.stanford.nlp.ling.TaggedWord;


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
	private String negation;//the negation word
	private String type;
	private String core;
	private String modifer;
	private List<TaggedWord> wordTags;
	private int startIndex;
	private int endIndex;
	private int start;
	private int end;
	private int role;//according to the language 
	
	private CharacterValue charValue;
	
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCore() {
		return core;
	}
	public void setCore(String core) {
		this.core = core;
	}
	
	public String getModifer() {
		return modifer;
	}
	public void setModifer(String modifer) {
		this.modifer = modifer;
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
	public List<TaggedWord> getWordTags() {
		return wordTags;
	}
	public void setWordTags(List<TaggedWord> wordTags) {
		this.wordTags = wordTags;
	}
	public CharacterValue getCharValue() {
		return charValue;
	}
	public void setCharValue(CharacterValue charValue) {
		this.charValue = charValue;
	}
	public int getStartIndex() {
		return startIndex;
	}
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}
	public int getEndIndex() {
		return endIndex;
	}
	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}
	
}
