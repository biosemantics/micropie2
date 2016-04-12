package edu.arizona.biosemantics.micropie.nlptool;

import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.EnglishGrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeGraphNode;

/**
 * 
 * @author maojin
 *
 */
public class SemanticRoleLabeler {

	
	/**
	 * label the phrases
	 * @param phraseList
	 * @param deptTree
	 * @return
	 */
	public Set<Phrase> labelPrepPhrases(List<Phrase> phraseList,
			GrammaticalStructure deptTree) {
		SemanticGraph semanticGraph = new SemanticGraph(deptTree.typedDependenciesCollapsedTree());
		//find the dependent relationship for all the verb phrases
		List<SemanticGraphEdge> edgeList = semanticGraph.edgeListSorted();
		for(SemanticGraphEdge edge : edgeList){
			
		}
		return null;
	}

}
