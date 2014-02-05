package edu.arizona.biosemantics.micropie.model;

public class SentenceMetadata {
	
	private int sourceId;
	private ParseResult parseResult;
	private boolean compoundSplitSentence;
	private TaxonTextFile taxonTextFile;
	
	public SentenceMetadata() {
		
	}

	public int getSourceId() {
		return sourceId;
	}

	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
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

	public void setTaxonTextFile(TaxonTextFile taxonTextFile) {
		this.taxonTextFile = taxonTextFile;
	}

	public TaxonTextFile getTaxonTextFile() {
		return taxonTextFile;
	}
		
	
	
}
