package edu.arizona.biosemantics.micropie.model;


/**
 * used in USP
 *
 */
public class SentenceDependency {
	
	private String relString;
	private String govString;
	private String govIdx;
	private String depString;
	private String depIdx;
	
	
	
	public String getRelString() {
		return relString;
	}
	
	public void setRelString(String relString) {
		this.relString = relString;
	}

	public String getGovString() {
		return govString;
	}
	
	public void setGovString(String govString) {
		this.govString = govString;
	}
	
	public String getGovIdx() {
		return govIdx;
	}
	
	public void setGovIdx(String govIdx) {
		this.govIdx = govIdx;
	}	

	public String getDepString() {
		return depString;
	}
	
	public void setDepString(String depString) {
		this.depString = depString;
	}
	
	public String getDepIdx() {
		return depIdx;
	}
	
	public void setDepIdx(String depIdx) {
		this.depIdx = depIdx;
	}	
	
	
	public SentenceDependency() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
