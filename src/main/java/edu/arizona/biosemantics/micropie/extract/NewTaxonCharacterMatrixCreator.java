package edu.arizona.biosemantics.micropie.extract;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.regex.CellScaleExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.PHTempNaClExtractor;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.NewTaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.Matrix;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.NumericCharacterValue;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;

/**
 * 
 * Jin's algorithm
 * 
 * @author maojin
 *
 */
public class NewTaxonCharacterMatrixCreator implements
		ITaxonCharacterMatrixCreator {

	private LinkedHashSet<ILabel> characterLabels;// the characters need to be parsed
	private LinkedHashSet<String> characterNames;
	private ICharacterValueExtractorProvider contentExtractorProvider;// extractors

	private Map<TaxonTextFile, List<MultiClassifiedSentence>> taxonSentencesMap;

	private NewTaxonCharacterMatrix results;// the results of the extraction

	@Inject
	public NewTaxonCharacterMatrixCreator(
			ICharacterValueExtractorProvider contentExtractorProvider) {
		this.contentExtractorProvider = contentExtractorProvider;
	}

	public NewTaxonCharacterMatrix getResults() {
		return results;
	}

	public void setCharacterLabels(LinkedHashSet<ILabel> characterLabels) {
		this.characterLabels = characterLabels;
	}
	
	public void setCharacterNames(LinkedHashSet<String> characterNames) {
		this.characterNames = characterNames;
	}

	public void setTaxonSentencesMap(
			Map<TaxonTextFile, List<MultiClassifiedSentence>> taxonSentencesMap) {
		this.taxonSentencesMap = taxonSentencesMap;
	}

	@Override
	public Matrix create() {

		// <Taxon, <Character, List<Value>>>
		results = new NewTaxonCharacterMatrix();
		
		results.setTaxonFiles(taxonSentencesMap.keySet());
		results.setCharacterLabels(characterLabels);
		results.setCharacterNames(characterNames);
		
		//process all the files
		for (TaxonTextFile taxonFile : taxonSentencesMap.keySet()) {
			LinkedList<MultiClassifiedSentence> sentences = (LinkedList<MultiClassifiedSentence>) taxonSentencesMap.get(taxonFile);
			this.create(results, taxonFile, sentences);
		}
		return results;
	}

	
	/**
	 * parse the results
	 * @param extResults
	 * @param taxonFile
	 * @param charValues
	 */
	private void parseResult(NewTaxonCharacterMatrix extResults, TaxonTextFile taxonFile, ILabel label,
			List<CharacterValue> charValues) {
		Map<ILabel, List> charMap = extResults.getAllTaxonCharacterValues(taxonFile);
		if(label!=null){//not a mixed value extractor
			charMap.get(label).addAll(charValues);
		}else{
			for(CharacterValue value : charValues){
				NumericCharacterValue nvalue = (NumericCharacterValue)value;
				ILabel clabel = nvalue.getCharacter();//find the label
				if(clabel!=null) charMap.get(clabel).add(nvalue);
			}
		}
	}

	
	/**
	 * extract a taxonfile and extract the result into a given matrix
	 * @param matrix
	 * @param textFile
	 * @param sentences
	 */
	public void create(NewTaxonCharacterMatrix matrix, TaxonTextFile taxonFile,
			List<MultiClassifiedSentence> sentences) {
		String taxon = taxonFile.getTaxon();
		boolean hasGC = false;
		boolean hasCellScale = false;
		boolean hasPTN = false;//has parsed the ph temp nacl
		
		//initialize the character values
		Map charaMap = new HashMap();
		Iterator<ILabel> charIter = characterLabels.iterator();
		while(charIter.hasNext()){
			ILabel ilabel = charIter.next();
			charaMap.put(ilabel, new LinkedList());
		}
		matrix.put(taxonFile, charaMap);
		
		// the sentences in the file
		for (MultiClassifiedSentence classifiedSentence : sentences) {// process one sentence
			Set<ILabel> predictions = classifiedSentence.getPredictions();
			if (predictions.size() == 0) {// it can be any character
				Label[] labelList = Label.values();
				for (int i = 0; i < labelList.length; i++) {
					predictions.add(labelList[i]);
				}
			}

			// Reference:
			// get the character extractors for this sentence
			Set<ICharacterValueExtractor> extractors = new HashSet<ICharacterValueExtractor>();
			for (ILabel label : predictions) {// get all the extractors ready
				//if (label instanceof Label && characterLabels.contains(label)) {
				//if (characterLabels.contains(label)) {
					extractors.addAll(contentExtractorProvider.getContentExtractor((Label) label));
				//}
			}

			System.out.println("predictions:"+predictions.size()+" extractors: "+extractors.size());
			// call the extractors one by one
			for (ICharacterValueExtractor extractor : extractors) {
				String character = extractor.getCharacterName();
				ILabel label = extractor.getLabel();
				List<CharacterValue> charValues = null;
				
				if(extractor instanceof CellScaleExtractor && !hasCellScale){
					charValues = extractor.getCharacterValue(classifiedSentence);
				}else if(extractor instanceof CellScaleExtractor && hasCellScale){
					continue;
				}else if(extractor instanceof PHTempNaClExtractor && !hasPTN){
					charValues = extractor.getCharacterValue(classifiedSentence);
				}else if(extractor instanceof PHTempNaClExtractor && hasPTN){
					charValues = extractor.getCharacterValue(classifiedSentence);
				}else{
					charValues = extractor.getCharacterValue(classifiedSentence);
				}
				
				System.out.println(character+" "+classifiedSentence.getText()+" "+charValues);
				parseResult(matrix, taxonFile, label, charValues);
			}
		}
	}

	
}
