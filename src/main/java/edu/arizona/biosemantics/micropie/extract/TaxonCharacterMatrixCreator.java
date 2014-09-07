package edu.arizona.biosemantics.micropie.extract;

import java.util.Arrays;
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
import edu.arizona.biosemantics.micropie.extract.regex.ICharacterValueExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.ICharacterValueExtractorProvider;
import edu.arizona.biosemantics.micropie.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;

/**
 * Taxon x Character matrix
 * @author rodenhausen
 */
public class TaxonCharacterMatrixCreator implements ITaxonCharacterMatrixCreator {

	private LinkedHashSet<String> characters;
	private ICharacterValueExtractorProvider contentExtractorProvider;
	private Map<TaxonTextFile, List<Sentence>> taxonSentencesMap;
	private Map<Sentence, SentenceMetadata> sentenceMetadataMap;
	private Map<Sentence, MultiClassifiedSentence> classifiedSentencesMap;

	@Inject
	public TaxonCharacterMatrixCreator(@Named("Characters")LinkedHashSet<String> characters, 
			@Named("TaxonSentencesMap")Map<TaxonTextFile, List<Sentence>> taxonSentencesMap, 
			@Named("SentenceMetadataMap")Map<Sentence, SentenceMetadata> sentenceMetadataMap, 
			@Named("SentenceClassificationMap")Map<Sentence, MultiClassifiedSentence> classifiedSentencesMap,
			ICharacterValueExtractorProvider contentExtractorProvider) {
		this.characters = characters;
		this.taxonSentencesMap = taxonSentencesMap;
		this.sentenceMetadataMap = sentenceMetadataMap;
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
		Map<TaxonTextFile, Map<String, Set<String>>> taxonCharacterMap = new HashMap<TaxonTextFile, Map<String, Set<String>>>();
		for(TaxonTextFile taxonFile : taxonSentencesMap.keySet()) {
			HashMap<String, Set<String>> characterMap = new HashMap<String, Set<String>>();
			for(String character : characters) {
				characterMap.put(character, new HashSet<String>());
			}
			taxonCharacterMap.put(taxonFile, characterMap);
			
			List<Sentence> sentences = taxonSentencesMap.get(taxonFile);
			for(Sentence sentence : sentences) {
				SentenceMetadata metadata = sentenceMetadataMap.get(sentence);
				MultiClassifiedSentence classifiedSentence = classifiedSentencesMap.get(sentence);
				
				// Set<ILabel> predictions = classifiedSentence.getPredictions();
				Set<ILabel> predictions = new HashSet<ILabel>();
				predictions.add(Label.c1);
				predictions.add(Label.c2);
				predictions.add(Label.c3);
				predictions.add(Label.c4);
				predictions.add(Label.c5);
				predictions.add(Label.c6);
				predictions.add(Label.c7);
				predictions.add(Label.c8);
				predictions.add(Label.c9);
				predictions.add(Label.c10);
				predictions.add(Label.c11);
				
				// Reference: 
				
				Set<ICharacterValueExtractor> extractors = new HashSet<ICharacterValueExtractor>();
				for(ILabel label : predictions) {
					if(label instanceof Label) {
						extractors.addAll(contentExtractorProvider.getContentExtractor((Label)label));
					}
				}
				for(ICharacterValueExtractor extractor : extractors) {
					String character = extractor.getCharacter();
					if(characters.contains(character)) {
						Set<String> content = extractor.getCharacterValue(sentence.getText());
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
