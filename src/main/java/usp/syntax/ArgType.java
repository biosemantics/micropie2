package usp.syntax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ArgType implements Comparable<ArgType> {
	static ArrayList<ArgType> argTypes_=new ArrayList<ArgType>();	
	static Map<String, Integer> argTypeStr_idx_=new HashMap<String,Integer>();
	
	String dep_=null;	// connect to reltype
	int relTypeIdx_=-1;	// optional: dep_ + prepositional phrase + dep2
	String dep2_=null;
	
	String str_=null;
	
	// for argComb
	public static int ARGTYPEIDX_SUBJ_=-1;
	public static int ARGTYPEIDX_OBJ_=-1;
	public static int ARGTYPEIDX_IN_=-1;
	
	private ArgType() {}	
	public static int getArgType(Path p) {
		String s=p.toString();
		if (argTypeStr_idx_.get(s)==null) {
//			t.idx_=relTypes_.size();
//			relTypeStr_idx_.put(s, t.idx_);			
			ArgType t=new ArgType();
			t.dep_=p.dep_;
			t.dep2_=p.dep2_;
			t.relTypeIdx_=(p.treeRoot_!=null)?RelType.getRelType(p.treeRoot_):-1;
//			t.str_=s;
			argTypes_.add(t);
			int ati=argTypes_.size()-1;
			argTypeStr_idx_.put(s, ati);
			
			if (p.dep_.equals("nsubj") && p.treeRoot_==null) ARGTYPEIDX_SUBJ_=ati;
			else if (p.dep_.equals("dobj") && p.treeRoot_==null) ARGTYPEIDX_OBJ_=ati;
			else if (p.dep_.equals("prep_in") && p.treeRoot_==null) ARGTYPEIDX_IN_=ati;
		}
		return argTypeStr_idx_.get(s);		
	}
	public static ArgType getArgType(int idx) {return argTypes_.get(idx);}
	
	public int compareTo(ArgType z) {		
		//System.out.println("Compare: "+toString()+" - "+x.toString());
		int rst=(dep_).compareTo(z.dep_);
		if (rst!=0) return rst;
		else if (relTypeIdx_!=z.relTypeIdx_) return relTypeIdx_-z.relTypeIdx_;
		else if (dep2_!=null) return dep2_.compareTo(z.dep2_);
		else if (z.dep2_!=null) return -1;
		else return 0;
	}
	public boolean equals(Object o) {return compareTo((ArgType)o)==0;}
	public int hashCode() {return toString().hashCode();}
	public String toString() {
		//return "<"+dep_+((relTypeIdx_>=0)?":"+relTypeIdx_+":"+dep2_:"")+">";
		if (str_==null) str_="<"+dep_+((relTypeIdx_>=0)?":"+relTypeIdx_+":"+dep2_:"")+">";
		return str_;
	}
}