package edu.arizona.biosemantics.micropie.model;


/**
 * USP term index model
 */
public class USPTermIndex {
	private int collapsedTermIndex;
	private String collapsedTermName;
	private String termIndex;
	private int termStartIndex;
	private String termName;
	
	
	
	public int getCollapsedTermIndex() {
		return collapsedTermIndex;
	}
	
	public void setCollapsedTermIndex(int collapsedTermIndex) {
		this.collapsedTermIndex = collapsedTermIndex;
	}

	public String getCollapsedTermName() {
		return collapsedTermName;
	}
	
	public void setCollapsedTermName(String collapsedTermName) {
		this.collapsedTermName = collapsedTermName;
	}
	
	public String getTermIndex() {
		return termIndex;
	}
	
	public void setTermIndex(String termIndex) {
		this.termIndex = termIndex;
	}	

	public int getTermStartIndex() {
		return termStartIndex;
	}
	
	public void setTermStartIndex(int termStartIndex) {
		this.termStartIndex = termStartIndex;
	}
	
	public String getTermName() {
		return termName;
	}
	
	public void setTermName(String termName) {
		this.termName = termName;
	}
}



