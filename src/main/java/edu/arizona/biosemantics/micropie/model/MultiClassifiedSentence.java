package edu.arizona.biosemantics.micropie.model;

import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.extract.context.PhraseRelationGraph;
import edu.stanford.nlp.ling.TaggedWord;

/**
 * Multi Classified Sentence and its Metadata
 * 
 * @author maojin
 *
 */
public class MultiClassifiedSentence extends Sentence{

	private Set<ILabel> predictions;// the predicted characters
	private Set<ILabel> categories;// the predicted categories
	private SentenceMetadata sentMetadata; // some metadata
	private List<SubSentence> subSentence; // subsentences if brackets are used
	private List<List<TaggedWord>> subSentTaggedWords; // the pos tagger for the subsentences
	private List<List<Phrase>> phraseList;// the phrases for each subsentence
	private List<PhraseRelationGraph> phraseRelationGraphs;
	
	
	public MultiClassifiedSentence(String text) {
		super(text);
	}
	
	public MultiClassifiedSentence(RawSentence sentence, Set<ILabel> predictions) {
		super(sentence.getText());
		this.predictions = predictions;
	}

	public Set<ILabel> getPredictions() {
		return predictions;
	}

	public void setPredictions(Set<ILabel> predictions) {
		this.predictions = predictions;
	}

	public Set<ILabel> getCategories() {
		return categories;
	}

	public void setCategories(Set<ILabel> categories) {
		this.categories = categories;
	}

	public SentenceMetadata getSentMetadata() {
		return sentMetadata;
	}

	public void setSentMetadata(SentenceMetadata sentMetadata) {
		this.sentMetadata = sentMetadata;
	}

	public List getSubSentence() {
		return subSentence;
	}

	public void setSubSentence(List subSentence) {
		this.subSentence = subSentence;
	}

	public List<List<TaggedWord>> getSubSentTaggedWords() {
		return subSentTaggedWords;
	}

	public void setSubSentTaggedWords(List subSentTaggedWords) {
		this.subSentTaggedWords = subSentTaggedWords;
	}

	public List<List<Phrase>> getPhraseList() {
		return phraseList;
	}

	public void setPhraseList(List<List<Phrase>> phraseList) {
		this.phraseList = phraseList;
	}

	public List<PhraseRelationGraph> getPhraseRelationGraphs() {
		return phraseRelationGraphs;
	}

	public void setPhraseRelationGraphs(
			List<PhraseRelationGraph> phraseRelationGraphs) {
		this.phraseRelationGraphs = phraseRelationGraphs;
	}

}