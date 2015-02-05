package usp.util;

public class Pair<X,Y> implements Comparable<Pair<X,Y>> {
	X first_;
	Y second_;	
	public Pair(X first, Y second) {
		first_=first; second_=second;		
	}
	public X getFirst() {return first_;}
	public Y getSecond() {return second_;}
	public void setFirst(X first) {first_=first;}
	public void setSecond(Y second) {second_=second;}
	/*
	public int compareTo(Pair x) {
		System.out.println("Compare: "+toString()+" - "+x.toString());
		if (first_.compareTo(x.first_)!=0) return first_.compareTo(x.first_);
		else return second_.compareTo(x.second_);
	}
	*/
	public int compareTo(Pair<X,Y> z) {		
		//System.out.println("Compare: "+toString()+" - "+x.toString());
		int rst=((Comparable)first_).compareTo(z.first_);
		if (rst!=0) return rst;
		else return ((Comparable)second_).compareTo(z.second_);
	}
	public boolean equals(Object o) {return compareTo((Pair<X,Y>)o)==0;}
	public int hashCode() {return toString().hashCode();}
	public String toString() {return "<"+first_+" , "+second_+">";}
}
