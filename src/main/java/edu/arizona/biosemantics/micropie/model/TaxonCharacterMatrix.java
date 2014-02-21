package edu.arizona.biosemantics.micropie.model;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class TaxonCharacterMatrix {

	private Set<TaxonTextFile> taxonFiles;
	private Map<TaxonTextFile, Map<String, Set<String>>> taxonCharacterMap;
	private LinkedHashSet<String> characters;
	
	public void setTaxonFiles(Set<TaxonTextFile> taxonFiles) {
		this.taxonFiles = taxonFiles;
	}

	public void setCharacters(LinkedHashSet<String> characters) {
		this.characters = characters;
	}

	public void setTaxonCharacterMap(Map<TaxonTextFile, Map<String, Set<String>>> taxonCharacterMap) {
		this.taxonCharacterMap = taxonCharacterMap;
	}

	public Set<TaxonTextFile> getTaxonFiles() {
		return taxonFiles;
	}

	public Map<TaxonTextFile, Map<String, Set<String>>> getTaxonCharacterMap() {
		return taxonCharacterMap;
	}

	public LinkedHashSet<String> getCharacters() {
		return characters;
	}
	
	

}
