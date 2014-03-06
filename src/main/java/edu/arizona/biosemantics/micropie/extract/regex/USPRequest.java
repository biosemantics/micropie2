package edu.arizona.biosemantics.micropie.extract.regex;

public class USPRequest {

	private String keyword;
	private String keywordType;
	private String keywordObject;
	
	public USPRequest(String keyword, String keywordType, String keywordObject) {
		super();
		this.keyword = keyword;
		this.keywordType = keywordType;
		this.keywordObject = keywordObject;
	}
	
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String getKeywordType() {
		return keywordType;
	}
	public void setKeywordType(String keywordType) {
		this.keywordType = keywordType;
	}
	public String getKeywordObject() {
		return keywordObject;
	}
	public void setKeywordObject(String keywordObject) {
		this.keywordObject = keywordObject;
	}
	
	

}
