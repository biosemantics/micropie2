package edu.arizona.biosemantics.micropie.model;

import java.util.Collection;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;

public class ParseResult {

	private Tree tree;
	private Collection<TypedDependency> typedDependencies;
	
	public ParseResult(Tree tree, Collection<TypedDependency> typedDependencies) {
		super();
		this.tree = tree;
		this.typedDependencies = typedDependencies;
	}


	public Tree getTree() {
		return tree;
	}


	public void setTree(Tree tree) {
		this.tree = tree;
	}


	public Collection<TypedDependency> getTypedDependencies() {
		return typedDependencies;
	}


	public void setTypedDependencies(Collection<TypedDependency> typedDependencies) {
		this.typedDependencies = typedDependencies;
	}
		
}
