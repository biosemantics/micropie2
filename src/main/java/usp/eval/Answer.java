package usp.eval;

public class Answer implements Comparable<Answer> {
	String sid_, rst_;	
	public Answer(String sid, String rst) {
		sid_=sid; rst_=rst;		
	}
	public String getSentId() {return sid_;}
	public String getRst() {return rst_;}
	public int compareTo(Answer a) {		
		//System.out.println("Compare: "+toString()+" - "+x.toString());
		int rst=rst_.compareTo(a.rst_);
		if (rst!=0) return rst;
		else return sid_.compareTo(a.sid_);
	}
	public boolean equals(Object o) {return compareTo((Answer)o)==0;}
	public int hashCode() {return toString().hashCode();}
	public String toString() {		
		return sid_+" "+rst_;
	}

}
