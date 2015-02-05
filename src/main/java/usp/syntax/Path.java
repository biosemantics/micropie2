package usp.syntax;

public class Path {
	// dep + tree + dep2 (@argNode)
	String dep_=null;
	TreeNode treeRoot_=null;	
	TreeNode argNode_=null;	// node in the tree that connects to dep2
	String dep2_=null;	
	
	int argTypeIdx_=-1;
	
	public Path(String dep) {
		dep_=dep;
		argTypeIdx_=ArgType.getArgType(this);
	}
	public Path(String dep, TreeNode treeRoot, TreeNode argNode, String dep2) {
		dep_=dep;
		treeRoot_=treeRoot;
		argNode_=argNode;
		dep2_=dep2;
		argTypeIdx_=ArgType.getArgType(this);
	}
	public String getDep() {return dep_;}
	public TreeNode getTreeRoot() {return treeRoot_;}
	public TreeNode getArgNode() {return argNode_;}
	public String getDep2() {return dep2_;}
	public int getArgType() {return argTypeIdx_;}
	
	String str_=null;
	public String toString() {
		if (str_==null) {
			str_=genTypeStr();
		}
		return str_;
	}
	String genTypeStr() {
		String type="<"+dep_;
		if (treeRoot_!=null) {
			type+=":"+RelType.genTypeStr(treeRoot_)+":"+dep2_;
		}
		type+=">";
		return type;
	}
}