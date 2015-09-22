package edu.arizona.biosemantics.micropie.model;

import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.stanford.nlp.ling.TaggedWord;

/**
 * Multi Classified Sentence and its Metadata
 * 
 * @author maojin
 *
 */
public class MultiClassifiedSentence extends Sentence{

	private Set<ILabel> predictions;// the predicted categories
	private SentenceMetadata sentMetadata; // some metadata
	private List<SubSentence> subSentence;
	private List<List<TaggedWord>> subSentTaggedWords;
	
	
	public MultiClassifiedSentence(String text) {
		this.text = text;
	}
	
	
	public MultiClassifiedSentence(RawSentence sentence, Set<ILabel> predictions) {
		this.text = sentence.getText();
		this.predictions = predictions;
	}

	public Set<ILabel> getPredictions() {
		return predictions;
	}

	public void setPredictions(Set<ILabel> predictions) {
		this.predictions = predictions;
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

	public List getSubSentTaggedWords() {
		return subSentTaggedWords;
	}

	public void setSubSentTaggedWords(List subSentTaggedWords) {
		this.subSentTaggedWords = subSentTaggedWords;
	}
	
}