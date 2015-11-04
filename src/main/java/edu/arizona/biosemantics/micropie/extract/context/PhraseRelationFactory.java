package edu.arizona.biosemantics.micropie.extract.context;

import org.jgrapht.EdgeFactory;

import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.model.PhraseRelation;

/**
 * phraserelation factory 
 * used in the graph building
 * @author maojin
 *
 */
public class PhraseRelationFactory implements EdgeFactory<Phrase, PhraseRelation>{

	public PhraseRelation createEdge(Phrase	sourceVertex, Phrase targetVertex) {
		return new PhraseRelation(sourceVertex, targetVertex);
	}
	
	public PhraseRelation createEdge(Phrase	sourceVertex, Phrase targetVertex, double weight) {
		return new PhraseRelation(sourceVertex, targetVertex, weight);
	}
}
