package edu.arizona.biosemantics.micropie.model;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;

/**
 * <String, Map<ILabel, Set<CharacterValue>>>
 * @author maojin
 */
public class NewTaxonCharacterMatrix<ILabel> extends HashMap<TaxonTextFile, Map<ILabel, List<CharacterValue>>> implements Matrix {
	private Set<TaxonTextFile> taxonFiles;
	private LinkedHashSet<ILabel> characterLabels;
	private LinkedHashSet<String> characterNames;
	
	
	public Set<TaxonTextFile> getTaxonFiles() {
		return taxonFiles;
	}

	public void setTaxonFiles(Set<TaxonTextFile> taxonFiles) {
		this.taxonFiles = taxonFiles;
	}

	public LinkedHashSet<ILabel> getCharacterLabels() {
		return characterLabels;
	}

	public void setCharacterLabels(LinkedHashSet<ILabel> characterLabels) {
		this.characterLabels = characterLabels;
	}

	public LinkedHashSet<String> getCharacterNames() {
		return characterNames;
	}

	public void setCharacterNames(LinkedHashSet<String> characterNames) {
		this.characterNames = characterNames;
	}

	/**
	 * get all the characters by specifying a given taxon
	 * @param taxon
	 * @return
	 */
	public Map<ILabel, List<CharacterValue>> getAllTaxonCharacterValues(TaxonTextFile taxon){
		return this.get(taxon);
	}
	
	/**
	 * get one specified character of the taxon
	 * @param taxon
	 * @param label
	 * @return
	 */
	public List<CharacterValue> getTaxonCharacter(TaxonTextFile taxon, ILabel label){
		//character map for this taxon
		Map<ILabel,List<CharacterValue>> taxonCharMap = this.get(taxon);
		if(taxonCharMap==null){
			System.err.println("These is no taxon:"+taxon);
			return null;
		}else{
			//character value set for this character
			return taxonCharMap.get(label);
		}
	}

	
	public Map<ILabel, List<CharacterValue>> getAllTaxonCharacterValues(String taxonName) {
		return this.get(taxonName);
	}
}
