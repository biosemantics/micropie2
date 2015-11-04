package edu.arizona.biosemantics.micropie.model;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * An edge of the graph model
 * @author maojin
 *
 */
public class PhraseRelation extends DefaultEdge{
	
	private PhraseRelationType type;
	
	private Object source;
	private Object target;
	
	double weight = WeightedGraph.DEFAULT_EDGE_WEIGHT;

	public PhraseRelation() {
	}

  
	public PhraseRelation(Phrase sourcep, Phrase targetp) {
		source = sourcep;
		target = targetp;
	}
	
	public PhraseRelation(Phrase sourcep, Phrase targetp,
			PhraseRelationType type) {
		source = sourcep;
		target = targetp;
		this.type = type;
	}
	
	public PhraseRelation(Phrase sourceVertex, Phrase targetVertex,
			double weight2) {
		source = sourceVertex;
		target = targetVertex;
		this.weight = weight2;
	}


	/**
     * Retrieves the weight of this edge. This is protected, for use by
     * subclasses only (e.g. for implementing toString).
     *
     * @return weight of this edge
     */
    protected double getWeight()
    {
        return weight;
    }
	
    
	public PhraseRelationType getType() {
		return type;
	}
	public void setType(PhraseRelationType type) {
		this.type = type;
	}
	
	public Phrase getSource(){
		return (Phrase)this.source;
	}
	
	public Phrase getTarget(){
		return (Phrase)this.target;
	}
	
	
	/**
	 * get the other node of this phrase
	 * @param phrase
	 * @return
	public String getTarget(String phrase){
		if(source.equals(phrase)){
			return target;
		}else if(target.equals(phrase)){
			return source;
		}
		return null;
	} */
	public boolean equals(PhraseRelation pr){
		if(pr==this) return true;
		if(!pr.source.equals(this.source)) return false;
		if(!pr.target.equals(this.target)) return false;
		if(!pr.type.equals(this.type)) return false;
		return true;
	}
	
}
