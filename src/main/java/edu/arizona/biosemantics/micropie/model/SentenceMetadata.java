package edu.arizona.biosemantics.micropie.model;

public class SentenceMetadata {

	private String sourceFile;
	private int sourceId;
	private String taxon;
	private ParseResult parseResult;
	private boolean compoundSplitSentence;
	
	public SentenceMetadata() {
		
	}

	public String getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	public int getSourceId() {
		return sourceId;
	}

	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}

	public String getTaxon() {
		return taxon;
	}

	public void setTaxon(String taxon) {
		this.taxon = taxon;
	}

	public ParseResult getParseResult() {
		return parseResult;
	}

	public void setParseResult(ParseResult parseResult) {
		this.parseResult = parseResult;
	}

	public boolean isCompoundSplitSentence() {
		return compoundSplitSentence;
	}

	public void setCompoundSplitSentence(boolean compoundSplitSentence) {
		this.compoundSplitSentence = compoundSplitSentence;
	}
		
	
	
}
