package edu.arizona.biosemantics.micropie.model;

import java.io.File;

/**
 * The model of taxon XML file
 *
 */
public class TaxonTextFile {

	private String taxon;

	private String family;
	private String genus;
	private String species;
	private String strain_number;
	private String the16SrRNAAccessionNumber;
	private String xmlFile;

	
	private String text;
	private File inputFile;

	public TaxonTextFile(String taxon, String family, String genus, String species, String strain_number, String the16SrRNAAccessionNumber, String text, File inputFile) {
		this.taxon = taxon;
		
		this.family = family;
		this.genus = genus;
		this.species = species;
		this.strain_number = strain_number;
		this.the16SrRNAAccessionNumber = the16SrRNAAccessionNumber;
		
		this.text = text;
		this.inputFile = inputFile;
	}
	
	public TaxonTextFile(String taxon, String family, String genus, String species, String strain_number, String the16SrRNAAccessionNumber, String text) {
		this.taxon = taxon;
		
		this.family = family;
		this.genus = genus;
		this.species = species;
		this.strain_number = strain_number;
		this.the16SrRNAAccessionNumber = the16SrRNAAccessionNumber;
		
		this.text = text;
	}
	
	public TaxonTextFile(){
		
	}
	

	public String getTaxon() {
		return taxon;
	}

	public void setTaxon(String taxon) {
		this.taxon = taxon;
	}

	
	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}	

	
	public String getGenus() {
		return genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}
	
	
	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}	
	

	public String getStrain_number() {
		return strain_number;
	}

	public void setStrain_number(String strain_number) {
		this.strain_number = strain_number;
	}

	public String getThe16SrRNAAccessionNumber() {
		return the16SrRNAAccessionNumber;
	}

	public void setThe16SrRNAAccessionNumber(String strain_number) {
		this.the16SrRNAAccessionNumber = the16SrRNAAccessionNumber;
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

	public String getXmlFile() {
		return xmlFile;
	}

	public void setXmlFile(String xmlFile) {
		this.xmlFile = xmlFile;
	}
	
	

}
