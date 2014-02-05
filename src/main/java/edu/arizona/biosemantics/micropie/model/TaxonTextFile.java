package edu.arizona.biosemantics.micropie.model;

import java.io.File;

public class TaxonTextFile {

	private String taxon;
	private String text;
	private File inputFile;

	public TaxonTextFile(String taxon, String text, File inputFile) {
		this.taxon = taxon;
		this.text = text;
		this.inputFile = inputFile;
	}

	public String getTaxon() {
		return taxon;
	}

	public void setTaxon(String taxon) {
		this.taxon = taxon;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}
	
	

}
