package edu.arizona.biosemantics.micropie.extract.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.model.PhraseRelation;


/**
 * phrase relation graph
 * @author maojin
 *
 */
public class PhraseRelationGraph extends DefaultDirectedWeightedGraph<Phrase, PhraseRelation>{


	public PhraseRelationGraph(EdgeFactory<Phrase, PhraseRelation> ef) {
		super(ef);
	}

	/**
	 * get the phrases ahead of the phrase
	 * @param p
	 * @return
	 */
	public Set getAheadPhrase(Phrase p){
		Set aheadPhrases = null;
		try{
			Set<PhraseRelation> aheadPhrasesEdges = this.incomingEdgesOf(p);
			aheadPhrases = new HashSet();
			for(PhraseRelation edge: aheadPhrasesEdges){
				Phrase pharse = (Phrase) edge.getSource();
				aheadPhrases.add(pharse);
			}
		}catch(IllegalArgumentException e){
			aheadPhrases = null;
		}
		return aheadPhrases;
	}
	
	/**
	 * get the phrases following the phrase
	 * @param p
	 * @return
	 */
	public Set getFollowPhrase(Phrase p){
		Set followPhrases = null;
		try{
			Set<PhraseRelation> followPhrasesEdges = this.outgoingEdgesOf(p);
			followPhrases = new HashSet();
			for(PhraseRelation edge: followPhrasesEdges){
				Phrase pharse = (Phrase) edge.getTarget();
				followPhrases.add(pharse);
			}
		}catch(IllegalArgumentException e){
			followPhrases = null;
		}
		return followPhrases;
	}
	
	
	/**
	 * find the candidate phrases according to the given distance from the target node
	 * @param p
	 * @param maxDistance
	 * @return
	 */
	public Map<Phrase, Integer> expandSubgraph(Phrase p, int maxDistance){
		Map<Phrase, Integer> candPhrases = new HashMap();
		
		//outer
		List<Phrase> newOuter = new ArrayList();
		newOuter.add(p);
		int start = 0;
		
		for(int level=1;level<=maxDistance;level++){//level by level
			int end = newOuter.size();
			for(;start<end;start++){
				Set<Phrase> fset = getFollowPhrase(newOuter.get(start));
				if(fset!=null){
					newOuter.addAll(fset);//add this level to the candidates
					for(Phrase f:fset){
						candPhrases.put(f, level);
					}
				}
			}
		}
		
		//inner
		newOuter.clear();
		start = 0;
		for(int level=1;level<=maxDistance;level++){//level by level
			int end = newOuter.size();
			for(;start<end;start++){
				Set<Phrase> fset = getAheadPhrase(newOuter.get(start));
				if(fset!=null){
					newOuter.addAll(fset);//add this level to the candidates
					for(Phrase f:fset){
						candPhrases.put(f, level);
					}
				}
			}
		}
		
		return candPhrases;
	}
}
