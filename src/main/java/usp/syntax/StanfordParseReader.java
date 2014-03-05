package usp.syntax;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import usp.util.*;

public class StanfordParseReader implements ParseReader {

	static boolean isDebug_=false;
	
	static Set<String> ignored_deps_ = new HashSet<String>();	// dep to ignore
	static 
	{		
		// sure
		ignored_deps_.add("aux");
		ignored_deps_.add("auxpass");
		ignored_deps_.add("det");		
		ignored_deps_.add("cop");
		ignored_deps_.add("complm");		
//		ignored_deps_.add("num");
//		ignored_deps_.add("number");
		ignored_deps_.add("preconj");
		ignored_deps_.add("predet");
		ignored_deps_.add("punct");
		ignored_deps_.add("quantmod");
		
		// ?
		ignored_deps_.add("expl");
		ignored_deps_.add("mark");		
		//ignored_deps_.add("parataxis");			
	};
	
	
	// assuming full path
	public Article readParse(String fileName) throws Exception
	{		
		return readParse(fileName, true);
	}
	public Article readParse(String fileName, boolean ignoreDep) throws Exception
	{		
//		Utils.println("read parse: "+fileName);
		
		String fn, bloc, morphInputFileName, morphFileName, tagFileName, depFileName;		
		
		int k=fileName.lastIndexOf(Utils.FILE_SEP);
		fn=fileName.substring(k+1);
		
		fileName=fileName.substring(0,k);
		k=fileName.lastIndexOf(Utils.FILE_SEP);
		bloc=fileName.substring(k+1);
		
		fileName=fileName.substring(0,k);
		k=fileName.lastIndexOf(Utils.FILE_SEP);
		morphInputFileName=morphFileName=tagFileName=depFileName=fileName.substring(0,k);
		morphInputFileName+=Utils.FILE_SEP+"morph"+Utils.FILE_SEP+bloc+Utils.FILE_SEP+fn+".input";
		morphFileName+=Utils.FILE_SEP+"morph"+Utils.FILE_SEP+bloc+Utils.FILE_SEP+fn+".morph";
		depFileName+=Utils.FILE_SEP+"dep"+Utils.FILE_SEP+bloc+Utils.FILE_SEP+fn+".dep";		
		
		// --------------------------------------------------------------- //
		// Read input, morph
		// --------------------------------------------------------------- //
		Article doc=new Article(fn);
		readTokens(doc,morphInputFileName, morphFileName);		

		// --------------------------------------------------------------- //
		// Read dep
		// --------------------------------------------------------------- //
		readDeps(doc,depFileName,ignoreDep);		
		
		return doc;
	}	
		
	static void readDeps(Article doc, String depFileName,boolean ignoreDep) throws Exception {
//		Utils.println("proc "+depFileName);

		// read dep: all info vs tree-spec (ignore many rels, key=whole string // for conj: abc-2' )
		int numSent=doc.sentences_.size();
				
		int numBlankLine=0;
		int senId=0;
		BufferedReader in = new BufferedReader(new FileReader(depFileName));
		String s;
		Sentence currSent=doc.sentences_.get(senId);
		currSent.tkn_children_.put(0,new HashSet<Pair<String,Integer>>());
		Set<Integer> currNonRoots=new HashSet<Integer>();
		Set<Integer> currRoots=new HashSet<Integer>();
		while ((s=in.readLine())!=null)  
		{
			s=s.trim();
//			if (s.length()==0) {
			if (s.length()==0 || s.equals("SENTENCE_SKIPPED_OR_UNPARSABLE")) {
//				System.out.println("\tdone w. "+(senId)+".");
				numBlankLine++;
				if (currRoots!=null) {
					Set<Pair<String,Integer>> dep_chds=currSent.tkn_children_.get(0);
					Iterator<Integer> iit=currRoots.iterator();
					while (iit.hasNext()) {
						int id=iit.next();
						dep_chds.add(new Pair<String,Integer>("ROOT",id));
						currSent.tkn_par_.put(id, new Pair<String,Integer>("ROOT",0));
					}
					currSent=null;
					currNonRoots=null;
					currRoots=null;
				}
				
				//
//				break;
				
//				senId++; 
				continue;
			}

			if (numBlankLine>0) {
				// if type dependencies are generated from tree				
				senId+=(numBlankLine+1)/2;
				// if directly generated from text; Bug in Stanford Dep. Parsing?
//				senId+=numBlankLine;

				if (senId>=doc.sentences_.size()) 
					Utils.println("\tsenid="+senId+" "+doc.sentences_.size());
				currSent=doc.sentences_.get(senId);
				currSent.tkn_children_.put(0,new HashSet<Pair<String,Integer>>());
				currNonRoots=new HashSet<Integer>();
				currRoots=new HashSet<Integer>();
				numBlankLine=0;				
			}

//			Utils.println("\ts="+s);
			

			
			// nn(monkeys-2, Squirrel-1) 
			String rel=s.substring(0,s.indexOf("("));
			int i1=s.indexOf("(")+1, i3=s.lastIndexOf(")"), i2=s.indexOf(", ");
			while (i1==i2 || (!Character.isDigit(s.charAt(i2-1)) && s.charAt(i2-1)!='\'')) {i2=s.indexOf(",",i2+1);}; 
			
//			Utils.println("s="+s+" i1="+i1+" i2="+i2+" i3="+i3);
			
			String gov=s.substring(i1,i2).trim();			
			String dep=s.substring(i2+1,i3).trim();
			
//			Utils.println("gov="+gov+" dep="+dep);
			
			// all-info
			if (gov.charAt(gov.length()-1)=='\'') gov=gov.substring(0, gov.length()-1);
			if (dep.charAt(dep.length()-1)=='\'') dep=dep.substring(0, dep.length()-1);			
			int govId=Integer.parseInt(gov.substring(gov.lastIndexOf("-")+1));
			int depId=Integer.parseInt(dep.substring(dep.lastIndexOf("-")+1));

			// skip conj splitting to avoid infinite loop
			if (rel.indexOf("conj")==0 && govId==depId) continue;
			
			currNonRoots.add(depId);			
			if (currRoots.contains(depId)) currRoots.remove(depId); 
			if (!currNonRoots.contains(govId)) currRoots.add(govId);
			
			if (govId>=currSent.tokens_.size() || depId>=currSent.tokens_.size())
				Utils.println("ERR: exceed token # "+depFileName+" senId="+senId+" currSize="+currSent.tokens_.size()+" govId="+govId+" depId="+depId);
			
			// skip sem-empty dep
			if (ignoreDep && ignored_deps_.contains(rel)) continue;		
			
			// set dep
			currSent.tkn_par_.put(depId, new Pair<String,Integer>(rel,govId));
			Set<Pair<String,Integer>> dep_chds=currSent.tkn_children_.get(govId);
			if (dep_chds==null) {				
				dep_chds=new TreeSet<Pair<String,Integer>>();
				currSent.tkn_children_.put(govId,dep_chds);
			}
			dep_chds.add(new Pair<String,Integer>(rel,depId));
//			Utils.println("\t\t-> "+govId+" "+rel+" "+depId);
		}
		if (currRoots!=null) {
			Set<Pair<String,Integer>> dep_chds=currSent.tkn_children_.get(0);
			Iterator<Integer> iit=currRoots.iterator();
			while (iit.hasNext()) {
				int id=iit.next();
				dep_chds.add(new Pair<String,Integer>("ROOT",id));
				currSent.tkn_par_.put(id, new Pair<String,Integer>("ROOT",0));
			}
		}
		in.close();
	}
	static void readTokens(Article doc,String tagFileName) throws Exception
	{			
		// read words: MST; entity (N-)
		BufferedReader in = new BufferedReader(new FileReader(tagFileName));
		String s;
		String[] ts;
		boolean isNew=true;
		while ((s=in.readLine())!=null)  
		{
			s=s.trim();
			if (s.length()==0) {
				isNew=true;
				continue;
			}
			ts = s.split("\\s");
			ArrayList<Token> v;
			if (isNew) {
				Sentence sent=new Sentence();
				sent.tokens_.add(new Token("ROOT","ROOT"));
				doc.sentences_.add(sent);
				isNew=false;
			}
			v=doc.sentences_.get(doc.sentences_.size()-1).tokens_;			
			String pos=ts[2];
			String lemma=ts[1].toLowerCase();			
			v.add(new Token(pos,lemma));	// form, pos, lemma
			//if (ts[7].equals("ROOT")) sent_root.add(Integer.parseInt(ts[0]));
		}
		in.close();
	}

	static void readTokens(Article doc, String morphInputFileName,String morphFileName) throws Exception
	{		
//		Utils.println("file: "+morphInputFileName+" "+morphFileName);
		
		BufferedReader in = new BufferedReader(new FileReader(morphInputFileName));
		BufferedReader in2 = new BufferedReader(new FileReader(morphFileName));
		String s,s2;
		String[] ts;
		boolean isNew=true;
		while ((s=in.readLine())!=null)  
		{
			s2=in2.readLine();
			s=s.trim();
			if (s.length()==0) {
				isNew=true;
				continue;
			}
			
			// System.out.println(morphInputFileName+":s:"+s);
			// System.out.println(morphFileName+":s2:"+s2);
			
			ts = s.split("_");
			ArrayList<Token> v;
			if (isNew) {
				Sentence sent=new Sentence();
				sent.tokens_.add(new Token("ROOT","ROOT"));
				doc.sentences_.add(sent);
				isNew=false;
			}
			v=doc.sentences_.get(doc.sentences_.size()-1).tokens_;
			
			String pos=ts[1];
			String lemma=s2.replace(':', '.').toLowerCase();
			if (lemma.indexOf("_nn")>0) lemma=lemma.substring(0,lemma.indexOf("_nn"));
			Token t=new Token(pos,lemma);
			t.form_=ts[0];
			v.add(t);	// form, pos, lemma
			if (Token.tkn_cnt_.get(t)==null) Token.tkn_cnt_.put(t, 1);
			else Token.tkn_cnt_.put(t, Token.tkn_cnt_.get(t)+1);
			//if (ts[7].equals("ROOT")) sent_root.add(Integer.parseInt(ts[0]));
		}
		in.close();
		in2.close();
		
	}
}


