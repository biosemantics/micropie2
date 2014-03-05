package usp.eval;

public class Question implements Comparable<Question> {
	String rel_, arg_, dep_;
	
	// dep = the arg given in question
	
	String argClustIdxSeq_;	// arg in clustIdx seq
	
	public Question(String rel, String arg, String dep) {
		rel_=rel; arg_=arg; dep_=dep;		
	}
	public String getRel() {return rel_;}
	public String getArg() {return arg_;}
	public String getDep() {return dep_;}
	public int compareTo(Question q) {		
		//System.out.println("Compare: "+toString()+" - "+x.toString());
		int rst;
		rst=((Comparable)dep_).compareTo(q.dep_);		
		if (rst!=0) return rst;
		rst=((Comparable)rel_).compareTo(q.rel_);
		if (rst!=0) return rst;
		else return ((Comparable)arg_).compareTo(q.arg_);
	}
	public String getPattern() {
		if (dep_.equals("nsubj")) return arg_+" "+rel_;
		else if (dep_.equals("dobj")) return rel_+" "+arg_;
		return null;
	}
	public boolean equals(Object o) {return compareTo((Question)o)==0;}
	public int hashCode() {return toString().hashCode();}
	public String toString() {
//		if (dep_.equals("nsubj")) return ""+arg_+" "+rel_+" ?";
//		else if (dep_.equals("dobj")) return "? "+rel_+" "+arg_+"";
//		else return ""+rel_+" ::: "+dep_+" ::: "+arg_+"";
		if (dep_.equals("nsubj")) return "What does "+arg_+" "+rel_+"?";
		else if (dep_.equals("dobj")) return "What "+rel_+"s "+arg_+"?";
		else return ""+rel_+" ::: "+dep_+" ::: "+arg_+"";
	}
}
