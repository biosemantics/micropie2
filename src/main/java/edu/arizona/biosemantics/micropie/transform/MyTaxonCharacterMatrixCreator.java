package edu.arizona.biosemantics.micropie.transform;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.model.ClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.transform.regex.CellSizeExtractor;
import edu.arizona.biosemantics.micropie.transform.regex.GcExtractor;
import edu.arizona.biosemantics.micropie.transform.regex.GrowthPhExtractor;
import edu.arizona.biosemantics.micropie.transform.regex.IContentExtractor;
import edu.arizona.biosemantics.micropie.transform.regex.IContentExtractorProvider;

/**
 * Taxon x Character matrix
 * @author rodenhausen
 */
public class MyTaxonCharacterMatrixCreator implements IMatrixCreator {

	private LinkedHashSet<String> characters;
	private IContentExtractorProvider contentExtractorProvider;


	public MyTaxonCharacterMatrixCreator() {
		String characterListString = "16S rRNA accession #|Family|Genus|Species|Strain|Genome size|%G+C|Other genetic characteristics|Cell shape|Pigments|Cell wall|Motility|Biofilm formation|Habitat isolated from|Oxygen use|Salinity preference|pH minimum|pH optimum|pH maximum|Temperature minimum|Temperature optimum|Temperature maximum|NaCl minimum|NaCl optimum|NaCl maximum|Host|Symbiotic|Pathogenic|Disease caused|Metabolism (energy & carbon source)|Mono & di-saccharides|Polysaccharides|Amino acids|Alcohols|Fatty acids|Other energy or carbon sources|Fermentation products|Polyalkanoates (plastics)|Other metabolic product|Antibiotic sensitivity|Antibiotic resistant";
		this.characters = new LinkedHashSet<String>(Arrays.asList(characterListString.split("\\|")));
		
		this.contentExtractorProvider = new IContentExtractorProvider() {
			@Override
			public IContentExtractor getContentExtractor(Label label) {
				switch(label) {
				case c0:
					return new GcExtractor();
				case c1:
					return new GrowthPhExtractor();
				case c2:
					return new CellSizeExtractor();
				default:
					return null;
				}
			}
		};
	}
	
	@Override
	public TaxonCharacterMatrix create(Map<String, List<Sentence>> taxonSentencesMap, Map<Sentence, SentenceMetadata> sentenceMetadata, Map<Sentence, ClassifiedSentence> classifiedSentences) {
			TaxonCharacterMatrix result = new TaxonCharacterMatrix();
		result.setTaxa(taxonSentencesMap.keySet());
		result.setCharacters(characters);
		
		//<Taxon, <Character, Set<Value>>>
		Map<String, Map<String, Set<String>>> taxonCharacterMap = new HashMap<String, Map<String, Set<String>>>();
		for(String taxon : taxonSentencesMap.keySet()) {
			HashMap<String, Set<String>> characterMap = new HashMap<String, Set<String>>();
			for(String character : characters) {
				characterMap.put(character, new HashSet<String>());
			}
			taxonCharacterMap.put(taxon, characterMap);
			
			List<Sentence> sentences = taxonSentencesMap.get(taxon);
			for(Sentence sentence : sentences) {
				SentenceMetadata metadata = sentenceMetadata.get(sentence);
				ClassifiedSentence classifiedSentence = classifiedSentences.get(sentence);
				Set<ILabel> predictions = classifiedSentence.getPredictions();
				for(ILabel label : predictions) {
					if(label instanceof Label) {
						IContentExtractor extractor = contentExtractorProvider.getContentExtractor((Label)label);
						String character = extractor.getCharacter();
						if(characters.contains(character)) {
							Set<String> content = extractor.getContent(sentence.getText());
							characterMap.get(character).addAll(content);
						}
					}
				}
			}
		}
		result.setTaxonCharacterMap(taxonCharacterMap);
		return result;
	}
}
