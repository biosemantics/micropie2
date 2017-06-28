package edu.arizona.biosemantics.micropie.extract.crf;

import java.util.HashMap;
import java.util.Map;


/**
 * Token models
 * @author maojin
 *
 */
public class Token implements Cloneable{
	
	private String fileName;
	private int paragraphId;
	private int sentenceId;
	private int tokenId;
	private int offset;
	private int offend;
	private int sentOffset;
	private int sentOffend;
	private String text;
	private String lemma;
	
	public Token(){
		this.attributes = new HashMap();
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getParagraphId() {
		return paragraphId;
	}

	public void setParagraphId(int paragraphId) {
		this.paragraphId = paragraphId;
	}

	public int getSentenceId() {
		return sentenceId;
	}

	public void setSentenceId(int sentenceId) {
		this.sentenceId = sentenceId;
	}

	public int getTokenId() {
		return tokenId;
	}

	public void setTokenId(int tokenId) {
		this.tokenId = tokenId;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getOffend() {
		return offend;
	}

	public void setOffend(int offend) {
		this.offend = offend;
	}

	public int getSentOffset() {
		return sentOffset;
	}

	public void setSentOffset(int sentOffset) {
		this.sentOffset = sentOffset;
	}

	public int getSentOffend() {
		return sentOffend;
	}

	public void setSentOffend(int sentOffend) {
		this.sentOffend = sentOffend;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	

	public String getLemma() {
		return lemma;
	}

	public void setLemma(String lemma) {
		this.lemma = lemma;
	}



	private Map attributes;
	
	public Object getAttribute(TokenAttribute attKey){
		return attributes.get(attKey);
	}

	public void setAttribute(TokenAttribute tokentype, Object value) {
		this.attributes.put(tokentype, value);
	}

	@Override
	public Object clone() throws CloneNotSupportedException{  
			return (Token)super.clone();
    }

	public String getLabel() {
		String label = (String) this.getAttribute(TokenAttribute.NER);
		return label;
	}  
}