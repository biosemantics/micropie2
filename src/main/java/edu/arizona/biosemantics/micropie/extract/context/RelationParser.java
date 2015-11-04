package edu.arizona.biosemantics.micropie.extract.context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultWeightedEdge;

import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.model.PhraseRelation;
import edu.arizona.biosemantics.micropie.model.PhraseRelationType;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;


/**
 * 
 * @author maojin
 *
 */
public class RelationParser {

	private Set conjSet = new HashSet();
	
	
	{
		conjSet.add("and");
		conjSet.add("or");
		conjSet.add(",");
	}
	
	/**
	 * Coordinative Relation means that two phrases have the coordinatative relation.
	 * the phrases with a coordinative relation should have the same phrase value type and have a neighbourhood relationship
	 * the neighbourhood means they are one after the other excluding the terms "and","or" and ",".
	 * @param phraseList reversed phraselist
	 * @param twList
	 * @return
	
	public List<PhraseRelation> parseCoordinativeRelation(List<Phrase> phraseList, List<TaggedWord> twList){
		List<PhraseRelation> relationList = new ArrayList();
		int phsize = phraseList.size();
		
		//notice: the phrase list is reversed
		for(int i=0;i<phsize-1;i++){
			Phrase curPhrase = phraseList.get(i);
			int curStartIndex = curPhrase.getStartIndex();
			String curType = curPhrase.getType();
			
			Phrase formerPhrase = phraseList.get(i+1);
			int formerEndIndex = formerPhrase.getEndIndex();
			String formerType = formerPhrase.getType();
			
			boolean isCoor = true;
			if(!curType.equals(formerType)&&"N".equals(curType)){//must be the same type
				isCoor = false;
			}else{//must not be interrupted by non conj words as "and","or" and ",".
				for(int inter = formerEndIndex+1; inter <curStartIndex; inter++ ){
					TaggedWord tw = twList.get(inter);
					if(!this.conjSet.contains(tw.word())){
						isCoor = false;
						break;
					}
				}
			}
			
			if(isCoor){
				PhraseRelation pr = buildRelation(curPhrase, formerPhrase, PhraseRelationType.COR );
				relationList.add(pr);
			}
		}
		
		return relationList;
	}
	 */
	
	
	/**
	 * Coordinative Relation means that two phrases have the coordinative relation.
	 * the phrases with a coordinative relation should have the same phrase value type and have a neighbourhood relationship
	 * the neighbourhood means they are one after the other excluding the terms "and","or" and ",".
	 * @param phraseList reversed phraselist
	 * @param twList
	 * @return
	 */
	public PhraseRelationGraph parseCoordinativeRelationGraph(List<Phrase> phraseList, List<TaggedWord> twList){
		PhraseRelationGraph graph = new PhraseRelationGraph(new PhraseRelationFactory());
		
		int phsize = phraseList.size();
		
		//notice: the phrase list is reversed
		for(int i=0;i<phsize-1;i++){
			Phrase curPhrase = phraseList.get(i);
			int curStartIndex = curPhrase.getStartIndex();
			String curType = curPhrase.getType();
			
			Phrase formerPhrase = phraseList.get(i+1);
			int formerEndIndex = formerPhrase.getEndIndex();
			String formerType = formerPhrase.getType();
			
			boolean isCoor = true;
			if(!curType.equals(formerType)&&"N".equals(curType)){//must be the same type
				isCoor = false;
			}else{//must not be interrupted by non conj words as "and","or" and ",".
				for(int inter = formerEndIndex+1; inter <curStartIndex&&inter<twList.size(); inter++ ){
					TaggedWord tw = twList.get(inter);
					if(!this.conjSet.contains(tw.word())){
						isCoor = false;
						break;
					}
				}
			}
			
			if(isCoor){//
				graph.addVertex(formerPhrase);
				graph.addVertex(curPhrase);
				PhraseRelation directEdge = graph.getEdgeFactory().createEdge(formerPhrase, curPhrase);
				directEdge.setType(PhraseRelationType.COR);
				//graph.addEdge(formerPhrase, curPhrase);
				graph.addEdge(formerPhrase, curPhrase, directEdge);
			}
		}
		
		return graph;
	}

	
	/**
	 * BuildPhrase Graph according to the SemanticGraph of the sentence
	 * @param sentSemGraph
	 * @return
	 */
	public PhraseRelationGraph parseCoordinativeRelationGraph(SemanticGraph sentSemGraph){
		//TODO 
		return null;
	}
	
	
	/**
	 * return the  lists of coordinative phrases
	 * @param phraseList
	 * @param tagList
	 * @return
	 */
	public List<List<Phrase>> getCoordList(List<Phrase> phraseList,List<TaggedWord> tagList) {
		PhraseRelationGraph coordGraph = parseCoordinativeRelationGraph(phraseList, tagList);
		
		//each connected subgraph is a coordinative list
		List phraseLists = new ArrayList();
		Set<Phrase> pharseSet = new HashSet();
		pharseSet.addAll(coordGraph.vertexSet());
		while(pharseSet!=null&&pharseSet.size()>0){
			Phrase onePhrase = (Phrase) pharseSet.toArray()[0];
			List<Phrase> corphraseList = new ArrayList();
			phraseLists.add(corphraseList);
			corphraseList.add(onePhrase);
			pharseSet.remove(onePhrase);
			//deep-first traverse strategy
			traverseConnectedGraph(corphraseList,pharseSet,onePhrase,coordGraph);
		}
		
		return phraseLists;
	}


	/**
	 * 
	 * @param corphraseList
	 * @param onePhrase
	 * @param coordGraph
	 */
	public void traverseConnectedGraph(List<Phrase> corphraseList,Set<Phrase> pharseSet, Phrase onePhrase, PhraseRelationGraph coordGraph) {
		Set<PhraseRelation> neighbours = coordGraph.edgesOf(onePhrase);
		if(neighbours!=null){
			Iterator<PhraseRelation> niter = neighbours.iterator();
			while(niter.hasNext()){
				PhraseRelation nphraseRel = niter.next();
				Phrase source = nphraseRel.getSource();
				Phrase target = nphraseRel.getTarget();
				
				if(!corphraseList.contains(source)){
					corphraseList.add(source);
					pharseSet.remove(source);
					traverseConnectedGraph(corphraseList,pharseSet, source,coordGraph);
				}
				
				if(!corphraseList.contains(target)){
					corphraseList.add(target);
					pharseSet.remove(target);
					traverseConnectedGraph(corphraseList,pharseSet, target,coordGraph);
				}
			}
		
		}
		
	}
	
	
	
	
	/**
	 * build relation type
	 * 
	 * TODO: different words: core words, modifiers plus core words or the whole phrase?
	 * 
	 * @param source
	 * @param target
	 * @param type
	 * @return
	
	public PhraseRelation buildRelation(Phrase source, Phrase target, PhraseRelationType type){
		String sourcePh = source.getCore();
		String targetPh = target.getCore();
		return new PhraseRelation(sourcePh, targetPh, type);
	}
	 */
	
}