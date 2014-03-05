package usp.syntax;

import java.util.*;
import usp.syntax.*;
import usp.util.*;

public class RelType implements Comparable<RelType> {
	static ArrayList<RelType> relTypes_=new ArrayList<RelType>();	
	static Map<String, Integer> relTypeStr_idx_=new HashMap<String,Integer>();
	
	//int idx_=-1;
	String str_=null;
	char type_;	// C:content, N:non-content; det by root treenode's POS

	public char getType() {return type_;}
	
	private RelType() {}
	public static int getRelType(TreeNode tree) {
		// TO-DO: use inverted index for more efficient impl		
		//
		String s=genTypeStr(tree);
		if (relTypeStr_idx_.get(s)==null) {
			RelType t=new RelType();
			t.str_=s;
			t.type_=(Token.isContent(tree.tkn_.pos_))?'C':'N';
			relTypes_.add(t);
			relTypeStr_idx_.put(s, relTypes_.size()-1);			
		}
		return relTypeStr_idx_.get(s);		
	}
	public static RelType getRelType(int idx) {return relTypes_.get(idx);}
	
	// gen unique tree type string	
	static String genTypeStr(TreeNode tn) {
		String type="(";
		type+=tn.getToken();
		if (tn.getChildren()!=null) {
			Iterator<String> it=tn.getChildren().keySet().iterator();
			while (it.hasNext()) {
				String dep=it.next();
				type+=" ("+dep;
				Set<TreeNode> tns=tn.getChildren().get(dep);
				Iterator<TreeNode> it1=tns.iterator();
				while (it1.hasNext()) {
					TreeNode tn1=it1.next();
					type+=" "+genTypeStr(tn1);					
				}
				type+=")";
			}
		}
		type+=")";
		return type;
	}

	//
	public int compareTo(RelType z) {		
		//System.out.println("Compare: "+toString()+" - "+x.toString());
		return toString().compareTo(z.toString());
	}
	public boolean equals(Object o) {return compareTo((RelType)o)==0;}
	public int hashCode() {return toString().hashCode();}
	public String toString() {
		return str_;		
	}
}
