package edu.arizona.biosemantics.micropie.model;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class TaxonCharacterMatrix {

	private Set<String> taxa;
	private Map<String, Map<String, Set<String>>> taxonCharacterMap;
	private LinkedHashSet<String> characters;
	
	public void setTaxa(Set<String> taxa) {
		this.taxa = taxa;
	}

	public void setCharacters(LinkedHashSet<String> characters) {
		this.characters = characters;
	}

	public void setTaxonCharacterMap(Map<String, Map<String, Set<String>>> taxonCharacterMap) {
		this.taxonCharacterMap = taxonCharacterMap;
	}

	public Set<String> getTaxa() {
		return taxa;
	}

	public Map<String, Map<String, Set<String>>> getTaxonCharacterMap() {
		return taxonCharacterMap;
	}

	public LinkedHashSet<String> getCharacters() {
		return characters;
	}
	
	

}
