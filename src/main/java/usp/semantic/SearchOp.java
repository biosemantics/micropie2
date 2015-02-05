package usp.semantic;

public class SearchOp implements Comparable<SearchOp> {
	public final static char OP_MERGE_CLUST_='0';
	public final static char OP_MERGE_ROLE_='1';
	public final static char OP_COMPOSE_='2';
	
	public char op_;
	
	// merge clust
	public int clustIdx1_, clustIdx2_;
	
	// merge role
	public int clustIdx_, argIdx1_, argIdx2_;
	
	// absorb
	public int parClustIdx_, chdClustIdx_;
	
	String str_=null;
	
	public int compareTo(SearchOp z) {		
		//System.out.println("Compare: "+toString()+" - "+x.toString());
		return toString().compareTo(z.toString());		
	}
	public boolean equals(Object o) {return compareTo((SearchOp)o)==0;}
	public int hashCode() {return toString().hashCode();}
	public String toString() {
		if (str_!=null) return str_;
		genString();
		return str_;
	}
	public void genString() {
		str_="OP_"+op_+":";
		switch(op_) {
		case OP_MERGE_CLUST_:
			Clust c1=Clust.getClust(clustIdx1_);
			Clust c2=Clust.getClust(clustIdx2_);
//			str_+=clustIdx1_+"-"+c1+"~"+clustIdx2_+"-"+c2; break;
			str_+=c1+" == "+c2; break;
		case OP_MERGE_ROLE_: str_+=clustIdx_+":"+argIdx1_+":"+argIdx2_; break;
		case OP_COMPOSE_: 
			Clust rc=Clust.getClust(parClustIdx_);
			Clust ac=Clust.getClust(chdClustIdx_);			
			str_+=rc+" ++ "+ac; break;
//			str_+=parClustIdx_+"-"+rc+":"+chdClustIdx_+"-"+ac; break;
		}		
	}
}
