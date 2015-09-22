package edu.arizona.biosemantics.micropie.extract.usp;


/**
 * 
 * 
 * isolated	V	prep_from	parse
 * found	V	prep_in	parse
 * located V prep_in	parse
 * pigmented	J	amod	dep
 * keyword: pigmented
 * keywordType: J
 * keywordObject:amod
 * extractionType: dep
 */
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
