package edu.arizona.biosemantics.micropie.model;



/**
 * sub-sentence for a sentence that have bricks
 * 
 * @author maojin
 *
 */
public class SubSentence {
	public String content; 
	public int start;
	public int length;
	private Sentence mainSentence;
	
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public Sentence getMainSentence() {
		return mainSentence;
	}
	public void setMainSentence(Sentence mainSentence) {
		this.mainSentence = mainSentence;
	}
	
}
