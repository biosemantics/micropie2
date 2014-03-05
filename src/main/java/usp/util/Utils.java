package usp.util;

import java.util.Vector;
import java.io.*;

public class Utils {
	public static String FILE_SEP = System.getProperty("file.separator");
	
	public static PrintStream out_=System.out;
	
	public static String leftpad(String s, int len, char c) {
		String t=s;
		for (int i=0; i<len-s.length(); i++) t=c+t;
		return t;
	}
	
	public static void setOut(PrintStream out) {out_=out;}
	
	public static void print(String s) {out_.print(s);}
	public static void println(String s) {print(s+"\n");}
	public static void println() {print("\n");}

	// tree
	public static String genTreeNodeId(String articleId, int sentIdx, int wordIdx) {
		return articleId+":"+sentIdx+":"+leftpad(""+wordIdx,3, '0');
	}
	
	public static Pair<String,Integer> decodeTreeNodeId(String nid) {
		int i=nid.indexOf(":");
		int j=nid.indexOf(":",i+1);
		String aid=nid.substring(0,i);
		int si=Integer.parseInt(nid.substring(i+1,j));
		return new Pair<String,Integer>(aid,si);
	}
	
	// treat consecutive space separately
	public static Object[] split(String s, String sep) {
		Vector<String> ts=new Vector<String>();
		while (s.indexOf(" ")>=0) {
			ts.add(s.substring(0,s.indexOf(" ")));
			s=s.substring(s.indexOf(" ")+1);
		}
		ts.add(s);
		return ts.toArray();
	}
	public static boolean isReverseRole(String lbl) {
		return lbl.equals("rcmod") || lbl.equals("partmod"); 
	}
	
	public static double round(double x) {return round(x,2);}
	public static double round(double x, int n) {
		double k=1.0;
		for (int i=0; i<n; i++)	k*=10;
		int y=(int)(x*k);
		return y*1.0/k;
	}
	
	public static double xlogx(double x) {
		assert x>=0;
		if (x<=0) return 0;
		return x*Math.log(x);
	}

}
