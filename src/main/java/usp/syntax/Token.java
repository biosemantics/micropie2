package usp.syntax;

import java.util.*;

import usp.util.Pair;

/*
 * 
 */

public class Token implements Comparable<Token>
{
	//
	public static Map<Token,Integer> tkn_cnt_=new HashMap<Token,Integer>();
	
	
	String form_;	// original form
	String pos_;
	String lemma_;

	public Token(String pos, String lemma) {		
		pos_=pos;
		if (isContent(pos)) pos_=""+pos.charAt(0);
		else pos_=pos;
		lemma_=lemma;	
	}
	public String getForm() {return form_;}
	public String getPOS() {return pos_;}
	public String getLemma() {return lemma_;}
	
	//
	public static boolean isVerb(Token t) {return t.pos_.charAt(0)=='V';}
	public static boolean isNoun(Token t) {return t.pos_.charAt(0)=='N' || t.pos_.indexOf("PRP")==0;}
	
	static Set<Character> contentPOS_=new HashSet<Character>();
	static {
		contentPOS_.add('J');
		contentPOS_.add('R');
		contentPOS_.add('V');
		contentPOS_.add('N');
	}
	public static boolean isContent(String pos) {return contentPOS_.contains(pos.charAt(0));}
	
	// 
	public int compareTo(Token z) {		
		//System.out.println("Compare: "+toString()+" - "+x.toString());
		int rst=lemma_.compareTo(z.lemma_);
		if (rst!=0) return rst;
		else return pos_.compareTo(z.pos_);
	}
	public boolean equals(Object o) {return compareTo((Token)o)==0;}
	public int hashCode() {return toString().hashCode();}
	public String toString() {return pos_+":"+lemma_;}
}
