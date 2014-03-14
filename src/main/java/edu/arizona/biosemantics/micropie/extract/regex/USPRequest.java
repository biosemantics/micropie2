package edu.arizona.biosemantics.micropie.extract.regex;

public class USPRequest {

	private String keyword;
	private String keywordType;
	private String keywordObject;
	private String extractionType;
	
	public USPRequest(String keyword, String keywordType, String keywordObject, String extractionType) {
		super();
		this.keyword = keyword;
		this.keywordType = keywordType;
		this.keywordObject = keywordObject;
		this.extractionType = extractionType;
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
	public String getExtractionType() {
		return extractionType;
	}
	public void setExtractionType(String extractionType) {
		this.extractionType = extractionType;
	}	
	

}
