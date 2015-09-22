package edu.arizona.biosemantics.micropie.extract;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.regex.CellSizeExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GcExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthPhExtractor;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;

/**
 * Taxon x Character matrix
 * @author rodenhausen
 */
public class TaxonCharacterMatrixCreator implements ITaxonCharacterMatrixCreator {

	private LinkedHashSet<String> characters;//the characters need to be parsed
	private ICharacterValueExtractorProvider contentExtractorProvider;// extractors
	
	//it's better to send to this class as parameters
	private Map<TaxonTextFile, List<MultiClassifiedSentence>> taxonSentencesMap;
	private Map<RawSentence, MultiClassifiedSentence> classifiedSentencesMap;

	@Inject
	public TaxonCharacterMatrixCreator(@Named("Characters")LinkedHashSet<String> characters, 
			@Named("TaxonSentencesMap")Map<TaxonTextFile, List<MultiClassifiedSentence>> taxonSentencesMap, 
			@Named("SentenceClassificationMap")Map<RawSentence, MultiClassifiedSentence> classifiedSentencesMap,
			ICharacterValueExtractorProvider contentExtractorProvider) {
		this.characters = characters;
		this.taxonSentencesMap = taxonSentencesMap;
		this.classifiedSentencesMap = classifiedSentencesMap;
		this.contentExtractorProvider = contentExtractorProvider;
	}
	
	@Override
	public TaxonCharacterMatrix create() {
		TaxonCharacterMatrix result = new TaxonCharacterMatrix();
		log(LogLevel.INFO, "Creating matrix...");
		result.setTaxonFiles(taxonSentencesMap.keySet());
		result.setCharacters(characters);
		
		//<Taxon, <Character, Set<Value>>>
		Map<TaxonTextFile, Map<String, Set<CharacterValue>>> taxonCharacterMap = new HashMap<TaxonTextFile, Map<String, Set<CharacterValue>>>();
		for(TaxonTextFile taxonFile : taxonSentencesMap.keySet()) {//process one file
			//initialize all the characters
			HashMap<String, Set<CharacterValue>> characterMap = new HashMap<String, Set<CharacterValue>>();
			for(String character : characters) {
				characterMap.put(character, new HashSet<CharacterValue>());
			}
			taxonCharacterMap.put(taxonFile, characterMap);
			
			//the sentences in the file
			List<MultiClassifiedSentence> sentences = taxonSentencesMap.get(taxonFile);
			for(MultiClassifiedSentence sentence : sentences) {//process one sentence
				Set<ILabel> predictions = sentence.getPredictions();
				
				if (predictions.size() == 0 ) {//it can be any character
					Label[] labelList = Label.values();
					for ( int i = 0; i < labelList.length; i++ ) {
						predictions.add(labelList[i]);
					}
				}
				 
				// Reference: 
				// get the character extractors for this sentence
				Set<ICharacterValueExtractor> extractors = new HashSet<ICharacterValueExtractor>();
				for(ILabel label : predictions) {//get all the extractors ready
					if(label instanceof Label) {
						extractors.addAll(contentExtractorProvider.getContentExtractor((Label)label));
					}
				}
				
				//call the extractors one by one
				for(ICharacterValueExtractor extractor : extractors) {
					String character = extractor.getCharacterName();
					if(characters.contains(character)) {//will one have many characters?
						List<CharacterValue> content = extractor.getCharacterValue(sentence);
						
						System.out.println("character::" + character + " => content::Before::" + Arrays.toString(content.toArray()));
						content.remove(null);
						content.removeAll(Collections.singleton(null));
						content.removeAll(Collections.singleton(""));
						//System.out.println("character::" + character + " => content::After::" + Arrays.toString(content.toArray()));

						
						characterMap.get(character).addAll(content);
					}
				}
			}
		}
		result.setTaxonCharacterMap(taxonCharacterMap);
		log(LogLevel.INFO, "Done creating matrix");
		return result;
	}
}