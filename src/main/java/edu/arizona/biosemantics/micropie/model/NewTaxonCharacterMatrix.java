package edu.arizona.biosemantics.micropie.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

	
	/**
	 * when species has no value, propagate values from genus to species.
	 */
	public void propagateGenus() {
		//identify genus, only Genus Name or like: Paludibacter gen.
		Set<TaxonTextFile> genusFiles = new HashSet();
		
		Iterator<TaxonTextFile> taxonFilesIter = taxonFiles.iterator();
		Map<String, TaxonTextFile> genusTaxonMap = new HashMap();
		Map<String, TaxonTextFile> speciesTaxonMap = new HashMap();
		while(taxonFilesIter.hasNext()){
			TaxonTextFile taxonFile = taxonFilesIter.next();
			String taxonName = taxonFile.getTaxon();
			taxonName = taxonName.trim();
			String[] fields = taxonName.split("[\\s]+");
			if(fields.length>1||taxonName.endsWith("gen.")){//it's a genus
				genusTaxonMap.put(fields[0], taxonFile);
			}else{
				speciesTaxonMap.put(taxonFile.getTaxon(), taxonFile);
			}
		}
		
		
		//when species has no value, propagate values from genus to species
		Iterator speciesIter = speciesTaxonMap.keySet().iterator();
		while(speciesIter.hasNext()){
			String speciesName = (String) speciesIter.next();
			TaxonTextFile speciesFile = speciesTaxonMap.get(speciesName);
			String[] fields = speciesName.split("[\\s]+");
			String genusName = fields[0];
			TaxonTextFile genusFile = genusTaxonMap.get(genusName);
			
			if(genusFile!=null){//has a genus, copy the values
				Map<ILabel, List<CharacterValue>> genusValues = this.getAllTaxonCharacterValues(genusFile);
				Map<ILabel, List<CharacterValue>> speciesValues = this.getAllTaxonCharacterValues(speciesFile);
				
				Iterator<ILabel> labels = genusValues.keySet().iterator();
				while(labels.hasNext()){
					ILabel label = labels.next();
					List<CharacterValue> genusValue = genusValues.get(label);
					List<CharacterValue> speciesValue = speciesValues.get(label);
					if(speciesValue==null||speciesValue.size()==0){
						speciesValues.put(label, genusValue);
						System.out.println("propagate Genus to Species:"+genusName+" ===> "+speciesName+"  for "+label);
					}
				}
			}
			
		}
	}
}
