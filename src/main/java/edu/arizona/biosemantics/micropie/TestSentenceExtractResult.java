package edu.arizona.biosemantics.micropie;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;

public class TestSentenceExtractResult {

	private Map<Sentence, SentenceMetadata> sentenceMetadataMap = new HashMap<Sentence, SentenceMetadata>();
	private Map<String, List<Sentence>> taxonSentencesMap = new HashMap<String, List<Sentence>>();
	private List<Sentence> sentences = new LinkedList<Sentence>();
	
	public SentenceMetadata putSentenceMetadata(Sentence sentence, SentenceMetadata sentenceMetadata) {
		return sentenceMetadataMap.put(sentence, sentenceMetadata);
	}

	public void addTaxonSentence(String taxon, Sentence sentence) {
		if(!taxonSentencesMap.containsKey(taxon))
			taxonSentencesMap.put(taxon, new LinkedList<Sentence>());
		taxonSentencesMap.get(taxon).add(sentence);
	}

	public List<Sentence> getSentences() {
		return sentences;
	}

	public Map<Sentence, SentenceMetadata> getSentenceMetadataMap() {
		return sentenceMetadataMap;
	}

	public Map<String, List<Sentence>> getTaxonSentencesMap() {
		return taxonSentencesMap;
	}

	public void setSentences(List<Sentence> sentences) {
		this.sentences = sentences;
	}
}
