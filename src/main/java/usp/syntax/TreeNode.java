package usp.syntax;

import java.util.*;

import usp.util.*;

public class TreeNode implements Comparable<TreeNode> {
	static Map<String,TreeNode> id_treeNodes_=new HashMap<String,TreeNode>();
	public static TreeNode getTreeNode(String id) {return id_treeNodes_.get(id);}
	
	TreeMap<String, Set<TreeNode>> children_;	// dep_child	
	// TO-DO: unlikely same dep -> same pos/lemma; but could it?
	// Want to use a set since could add other things along the way
	
	String id_;	// article + sent + word
	Token tkn_;
	
	public TreeNode(String id, Token tkn) {
		id_=id;
		tkn_=tkn;
		id_treeNodes_.put(id, this);
	}
	public void addChild(String dep, TreeNode child) {
		if (children_==null) children_=new TreeMap<String, Set<TreeNode>>();
		Set<TreeNode> tns=children_.get(dep);
		if (tns==null) {
			tns=new TreeSet<TreeNode>();
			children_.put(dep, tns);
		}
		tns.add(child);
	}
	public String getId() {return id_;}
	public Token getToken() {return tkn_;}
	public TreeMap<String, Set<TreeNode>> getChildren() {return children_;}
	
	public int compareTo(TreeNode z) {		
		//System.out.println("Compare: "+toString()+" - "+x.toString());
		return tkn_.compareTo(z.tkn_);
	}
	public boolean equals(Object o) {return compareTo((TreeNode)o)==0;}
	public int hashCode() {return toString().hashCode();}
	public String toString() {return tkn_.toString();}
	
	public String getTreeStr() {
		Map<String, String> id_str=new TreeMap<String,String>();
		if (children_!=null) {
			for (String dep: children_.keySet()) {
				Set<TreeNode> nodes=children_.get(dep);
				for (TreeNode node: nodes) {
					String s="";
					if (dep.indexOf("prep_")==0) {
						s=dep.substring(5)+" ";
					}
					else if (dep.indexOf("conj_")==0) {
						s=dep.substring(5)+" ";
					}
					s+=node.getTreeStr();
					id_str.put(node.id_,s);
				}
			}
		}
		id_str.put(id_,tkn_.getLemma());
		
		String x="";
		for (String id:id_str.keySet()) {
			if (x.length()>0) x+=" ";
			x+=id_str.get(id);
		}
		return x;
	}
}
