package usp.syntax;

import java.io.*;
import java.util.*;
import usp.util.*;

public class CONLLParseReader implements ParseReader {
	static boolean isDebug_=false;

	public void chkToken(String fileName) throws Exception
	{
	}
		
	// assuming full path
	public Article readParse(String fileName) throws Exception
	{
		//Utils.println("read parse: "+fileName);
		
		String fn, bloc, tagFileName, depFileName;		
		
		//boolean isGenia=fileName.indexOf("genia")>=0;		
		
		// dev1
		int k=fileName.lastIndexOf('/');
		fn=fileName.substring(k+1);
		
		fileName=fileName.substring(0,k);
		k=fileName.lastIndexOf('/');
		bloc=fileName.substring(k+1);
		
		fileName=fileName.substring(0,k);
		k=fileName.lastIndexOf('/');
		tagFileName=depFileName=fileName.substring(0,k);
		tagFileName+="/tag/"+bloc+"/"+fn+".tag";
		depFileName+="/MST/"+bloc+"/"+fn+".mst";
		
		//
		Article doc=new Article(fn);
		
		String s, s2;
		String[] ts, ts2;
		Sentence currSent=null;
		int tknId=0;
		
		// read tag, mst -> tkns
		BufferedReader in1=new BufferedReader(new FileReader(depFileName));
		BufferedReader in2=new BufferedReader(new FileReader(tagFileName));
		while ((s=in1.readLine())!=null) {
			s=s.trim();
			s2=in2.readLine().trim();
			if (s.length()==0) {
				currSent=null;
				continue;
			}
			if (currSent==null) {
				currSent=new Sentence();
				doc.sentences_.add(currSent);
				//currSent.tokens_.add(new Token("ROOT","ROOT","ROOT"));	// dummy root
				currSent.tokens_.add(new Token("ROOT","ROOT"));	// dummy root
				tknId=0;
			}
			
			tknId++;
			
			ts=s.split("\\t");
			ts2=s2.split("\\t");
			
			String form=ts[0];
			String pos=ts2[2];
			String lemma=ts2[1].toLowerCase();
			
			if (Token.isContent(pos)) pos=""+pos.charAt(0);	// for content word, ignore tense
			
			//Token tkn=new Token(form,pos,lemma);
			Token tkn=new Token(pos,lemma);
			currSent.tokens_.add(tkn);
			int par=Integer.parseInt(ts[6]);
			String lbl=ts[7];
			//Edge e=new Edge(lbl,par,tknId);
			
			//
			currSent.tkn_par_.put(tknId, new Pair<String,Integer>(lbl,par));
			Set<Pair<String,Integer>> children=currSent.tkn_children_.get(par);
			if (children==null) {
				children=new TreeSet<Pair<String,Integer>>();
				currSent.tkn_children_.put(par, children);
			}
			children.add(new Pair<String,Integer>(lbl,tknId));
//			Set<Edge> edges=currSent.tkn_edges_.get(tknId);
//			if (edges==null) {
//				edges=new HashSet<Edge>();
//				currSent.tkn_edges_.put(tknId, edges);
//			}
//			edges.add(e);
		}
		in1.close();
		in2.close();
		
		return doc;
	}
}
