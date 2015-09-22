package edu.arizona.biosemantics.micropie.model;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * 
 * the model of taxon character matrix
 * 
 */
public class TaxonCharacterMatrix implements Matrix{

	private Set<TaxonTextFile> taxonFiles;
	private Map<TaxonTextFile, Map<String, Set<CharacterValue>>> taxonCharacterMap;
	private LinkedHashSet<String> characters;
	
	public void setTaxonFiles(Set<TaxonTextFile> taxonFiles) {
		this.taxonFiles = taxonFiles;
	}

	public void setCharacters(LinkedHashSet<String> characters) {
		this.characters = characters;
	}

	public void setTaxonCharacterMap(Map<TaxonTextFile, Map<String, Set<CharacterValue>>> taxonCharacterMap) {
		this.taxonCharacterMap = taxonCharacterMap;
	}

	public Set<TaxonTextFile> getTaxonFiles() {
		return taxonFiles;
	}

	public Map<TaxonTextFile, Map<String, Set<CharacterValue>>> getTaxonCharacterMap() {
		return taxonCharacterMap;
	}

	public LinkedHashSet<String> getCharacters() {
		return characters;
	}
	
	

}
