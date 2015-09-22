package edu.arizona.biosemantics.micropie.model;

import java.util.List;

import edu.stanford.nlp.ling.TaggedWord;

/**
 * sentence metadata
 * 
 */
public class SentenceMetadata {
	private int sourceId;
	private StanfordParseResult parseResult;
	private boolean compoundSplitSentence;
	private TaxonTextFile taxonTextFile;
	private String taxon;
	private List<TaggedWord> taggedWords;
	
	public SentenceMetadata() {
		
	}

	public int getSourceId() {
		return sourceId;
	}

	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}

	public StanfordParseResult getParseResult() {
		return parseResult;
	}

	public void setParseResult(StanfordParseResult parseResult) {
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

	public String getTaxon() {
		return taxon;
	}

	public void setTaxon(String taxon) {
		this.taxon = taxon;
	}
}
