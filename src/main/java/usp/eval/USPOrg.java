package usp.eval;

import java.util.*;
import java.io.*;


import usp.semantic.*;
import usp.syntax.*;
import usp.util.Pair;
import usp.util.Utils;


public class USPOrg {

	static Set<Character> vowels_=new HashSet<Character>();
	static {
		vowels_.add('a');
		vowels_.add('e');
		vowels_.add('i');
		vowels_.add('o');
		vowels_.add('u');
	}

	static Map<Question,Set<Answer>> qas_=new TreeMap<Question,Set<Answer>>();
	static String evalDir_="eval";
	static String rstDir_="results";
	static String dataDir_="genia";
	
	static Map<String, ArrayList<Question>> rel_qs_=new TreeMap<String, ArrayList<Question>>();  
	
	// identify verbs: dep->multiple argclust
	static Map<String,Integer> rel_clustIdx_=new HashMap<String,Integer>();
	
	static Set<String> qForms_=new HashSet<String>();
	static Set<String> qLemmas_=new HashSet<String>();
	static Map<String,Set<String>> form_lemma_=new HashMap<String,Set<String>>();

	// AB: B->ci,A
	static Map<Pair<String,String>,String> headDep_clustIdxs_=new HashMap<Pair<String,String>,String>();
	
	static Map<String,Set<String>> lemma_clustIdxs_=new HashMap<String,Set<String>>();
	static Map<Integer,Map<String,Integer>> clustIdx_depArgClustIdx_=new HashMap<Integer,Map<String,Integer>>();

	static Map<String,ArrayList<ArrayList<String>>> arg_cis_=new HashMap<String,ArrayList<ArrayList<String>>>();
		// multiple cis: each a possible parse
		// cis: multiple node, each w. multiple possible cis
	
	// part
	static Map<String, Pair<Integer,String>> ptId_clustIdxStr_=new HashMap<String,Pair<Integer,String>>();
	static Map<String, Map<Integer,Set<String>>> ptId_aciChdIds_=new HashMap<String, Map<Integer,Set<String>>>();
	static Map<String,String> ptId_parDep_=new HashMap<String,String>();	// ptid -> par dep
	static Map<Integer,Set<String>> clustIdx_ptIds_=new HashMap<Integer,Set<String>>();

	// sent
	static Map<String,String> id_sent_=new HashMap<String,String>();
	
	// dep: to generate final answer
	static Map<String,Article> id_article_=new HashMap<String, Article>();
	
	static Set<String> allowedDeps_=new HashSet<String>();
	static {
		allowedDeps_.add("nn");
		allowedDeps_.add("amod");
		allowedDeps_.add("prep_of");
		allowedDeps_.add("num");
		allowedDeps_.add("appos");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		if (args.length!=3) {
			Utils.println("java -cp usp.jar eval.USP <evalDir> <resultDir> <dataDir>");
			return;
		}
		
		// proc params
		evalDir_=args[0];
		rstDir_=args[1];
		dataDir_=args[2];
		
		// read questions
		readQuestions();
		
		// read morph: map form to lemma
		readMorph();
						
		// identify clust for question rels
		String dir=rstDir_;
		String fid=dataDir_;
		if (fid.indexOf(Utils.FILE_SEP)>=0) fid=fid.substring(fid.lastIndexOf(Utils.FILE_SEP));
		
		String fileName=dir+Utils.FILE_SEP+fid+".mln";
		readClust(fileName);	
		
		fileName=dir+Utils.FILE_SEP+fid+".parse";
		readPart(fileName);

		// read text
		readSents();	// also read dep
		preprocArgs(); 
		
		// match
		match();		
		printAns();
	}

	static void readQuestions() throws Exception {
		BufferedReader in=new BufferedReader(new FileReader(evalDir_+Utils.FILE_SEP+"question.sbj.txt"));
		String s;
		while ((s=in.readLine())!=null) {
			s=s.trim();
			if (s.length()==0) 	continue;
			int k=s.lastIndexOf(" ");
			String v=s.substring(k+1,s.length()-1);
			String q=s.substring("What does ".length(),k).trim().toLowerCase();
			ArrayList<Question> qs=rel_qs_.get(v);
			if (qs==null) {
				qs=new ArrayList<Question>();
				rel_qs_.put(v, qs);
			}
			Question qu=new Question(v,q,"nsubj");
			qs.add(qu);
			String[] ts=q.split(" ");
			for (int i=0; i<ts.length; i++) { 
				qForms_.add(ts[i]);
			}
			qForms_.add(v);
		}
		in.close();
		in=new BufferedReader(new FileReader(evalDir_+Utils.FILE_SEP+"question.obj.txt"));
		
		while ((s=in.readLine())!=null) {
			s=s.trim();
			if (s.length()==0) 	continue;
			int i=s.indexOf(" ");
			s=s.substring(i+1).trim();
			String q=s.substring(s.indexOf(' ')+1,s.length()-1).trim().toLowerCase();	// remove .
			String v=removeThirdPerson(s.substring(0,s.indexOf(' ')));
			//String v=s.substring(0,s.indexOf(' '));
						
			ArrayList<Question> qs=rel_qs_.get(v);
			if (qs==null) {
				qs=new ArrayList<Question>();
				rel_qs_.put(v, qs);
			}
			Question qu=new Question(v,q,"dobj");
			qs.add(qu);
			String[] ts=q.split(" ");
			for (i=0; i<ts.length; i++) {
				qForms_.add(ts[i]);
			}
			qForms_.add(v);
		}
		in.close();		
	}

	static void readMorph() throws Exception {		
		File rdf=new File(dataDir_+Utils.FILE_SEP+"morph");
		String[] dirs=rdf.list();
		Arrays.sort(dirs);
		
		for (int i=0; i<dirs.length; i++) {	// try 2k files first
			File d=new File(rdf.getPath()+Utils.FILE_SEP+dirs[i]);
			if (!d.isDirectory()) continue;
			String[] fns=d.list();
			Arrays.sort(fns);
			for (int j=0; j<fns.length; j++) {
				if (fns[j].indexOf(".morph")<0) continue;				
				String fid=fns[j].substring(0,fns[j].indexOf('.'));
				String mfn=d.getPath()+Utils.FILE_SEP+fns[j];
				String ifn=d.getPath()+Utils.FILE_SEP+fid+".input";
				readMorph(fns[j].substring(0,fns[j].indexOf('.')), mfn, ifn);
			}
		}
	}
	static void readMorph(String aid,String mfileName,String ifileName) throws Exception {
		BufferedReader in=new BufferedReader(new FileReader(mfileName));
		BufferedReader in2=new BufferedReader(new FileReader(ifileName));
		String s,s2;
		while ((s=in.readLine())!=null) {
			s2=in2.readLine();
			s=s.trim(); s2=s2.trim();
			if (s.length()==0) continue;
			if (s.indexOf("_nn")>=0) s=s.substring(0,s.indexOf("_nn"));
			
			s=s.toLowerCase(); s2=s2.toLowerCase();
			s2=s2.substring(0,s2.indexOf('_'));
			if (!qForms_.contains(s2)) continue;
			Set<String> ls=form_lemma_.get(s2);
			if (ls==null) {
				ls=new HashSet<String>();
				form_lemma_.put(s2, ls);
			}
			ls.add(s);
			qLemmas_.add(s);
		}
		in.close();
		in2.close();
		
	}
	static void readSents() throws Exception {		
		
		StanfordParseReader reader=new StanfordParseReader();
		
		File rdf=new File(dataDir_+Utils.FILE_SEP+"text");
		String[] dirs=rdf.list();
		Arrays.sort(dirs);
		
		for (int i=0; i<dirs.length; i++) {	// try 2k files first
			File d=new File(rdf.getPath()+Utils.FILE_SEP+dirs[i]);
			if (!d.isDirectory()) continue;
			String[] fns=d.list();
			Arrays.sort(fns);
			for (int j=0; j<fns.length; j++) {
				if (fns[j].indexOf(".txt")<0) continue;
				String aid=fns[j].substring(0,fns[j].indexOf('.'));
				String fn=d.getPath()+Utils.FILE_SEP+aid;
				readSents(aid, fn+".txt");
				id_article_.put(aid, reader.readParse(fn,false));	// don't ignore deps
			}
		}
	}
	static void readSents(String aid,String fileName) throws Exception {
		BufferedReader in=new BufferedReader(new FileReader(fileName));
		String s;
		int idx=0;
		while ((s=in.readLine())!=null) {
			s=s.trim();
			if (s.length()==0) continue;
			String id=aid+":"+(idx++);
			id_sent_.put(id, s);
		}
		in.close();
	}	

	// read parts
	static void readPart(String fileName) throws Exception {
		BufferedReader in;
		String s;
		String[] ts;
		
		in=new BufferedReader(new FileReader(fileName));
		while ((s=in.readLine())!=null) {
			// id/str
			int i=s.indexOf('\t');
			String id=s.substring(0,i);
			String str=s.substring(i+1);
			
			// clustIdx/clust
			s=in.readLine().trim();
			i=s.indexOf('\t');
			int ci=Integer.parseInt(s.substring(0,i));
			
			Set<String> pids=clustIdx_ptIds_.get(ci);
			if (pids==null) {
				pids=new HashSet<String>();
				clustIdx_ptIds_.put(ci, pids);
			}
			pids.add(id);
			
			ptId_clustIdxStr_.put(id, new Pair<Integer,String>(ci,str));

			// par/arg
			s=in.readLine().trim();
			String s2=in.readLine().trim();
			if (s.length()>0) {
				// parid/clust
				ts=s.split("\\t");
				String pid=ts[0];				
								
				ts=s2.split("\\t");
				int aci=Integer.parseInt(ts[0]);
				String dep=ts[2].substring(1,ts[2].length()-1);	// "<>"
				ptId_parDep_.put(id, dep);
				
				Map<Integer,Set<String>> aci_cids=ptId_aciChdIds_.get(pid);
				if (aci_cids==null) {
					aci_cids=new HashMap<Integer,Set<String>>();
					ptId_aciChdIds_.put(pid, aci_cids);
				}
				Set<String> cids=aci_cids.get(aci);
				if (cids==null) {
					cids=new HashSet<String>();
					aci_cids.put(aci, cids);
				}
				cids.add(id);
			}
		}
		in.close();
	}
	
	// TO-DO: only handle 2 layer for now; ignore rt
	static void procRelType(int clustIdx, String pos, String relType) {
		// 19508	[(N:b (nn (N:nf-kappa))):1466,	(N:b (dep (N:nf-kappa))):2]
	
		// rel only				
		if (rel_qs_.containsKey(relType) && pos.equals("V")) {					
			if (rel_clustIdx_.containsKey(relType)) {
				Utils.println("ERR: multiple clusts "+relType);
			}
			rel_clustIdx_.put(relType, clustIdx);
		}
		
		// arbitrary str
		if (qLemmas_.contains(relType)) {
			Set<String> cis=lemma_clustIdxs_.get(relType);
			if (cis==null) {
				cis=new HashSet<String>();
				lemma_clustIdxs_.put(relType, cis);
			}
			cis.add(""+clustIdx);
		}
		else {
			// probably multiple?
			int k=relType.indexOf(' ');
			if (k<0) return;			
			String head=relType.substring(0,k);
			k=relType.indexOf('(',k);
			int j=relType.indexOf(':',k);
			k=relType.indexOf(')',j);
			if (j<0 || k<0) {Utils.println("ERR: reltype="+relType); return;}
			String dep=relType.substring(j+1,k);
			
			if (!qLemmas_.contains(head) || !qLemmas_.contains(dep)) return;
			
			Pair<String,String> hd=new Pair<String,String>(head,dep);
			headDep_clustIdxs_.put(hd, ""+clustIdx);
		}
	}
	
	// find clustIdx for rel in questions
	static void readClust(String fileName) throws Exception {
		BufferedReader in;
		String s;
		String[] ts;
		
		int currCi=-1;
		Map<String,Integer> dep_aci=null;
		in=new BufferedReader(new FileReader(fileName));
		while ((s=in.readLine())!=null) {			
			if (!Character.isDigit(s.charAt(0))) {
				// aci/argnum 
				int i=s.indexOf('\t');
				int j=s.indexOf('\t',i+1);
				int aci=Integer.parseInt(s.substring(i+1,j));				
				
				// ati
				s=in.readLine().trim();
				ts=s.split("\\t");
				for (int k=0; k<ts.length; k++) {
					String x=ts[k];
					i=x.indexOf(':');
					j=x.indexOf(':',i+1);
					String dep=x.substring(i+2,j-1);	// "<...>"
					dep_aci.put(dep, aci);
				}
				
				// chdcl
				s=in.readLine().trim();
				continue;
			}
//			Utils.println(s);
			int i=s.indexOf('\t');
			int ci=Integer.parseInt(s.substring(0,i));
			String cs=s.substring(i+1);			
			i=cs.indexOf('(');
			while (i>=0) {
				int j=cs.indexOf(':',i);
				String pos=cs.substring(i+1,j);
				int k=cs.indexOf("):",j);
				i=cs.indexOf('(',k);
				String rt=cs.substring(j+1,k);

				// process multiple piece				
				procRelType(ci,pos,rt);			
			}
			currCi=ci;
			dep_aci=new HashMap<String,Integer>();
			clustIdx_depArgClustIdx_.put(ci, dep_aci);
		}
		in.close();		
	}
	static void printAns() {
		for (Question q:qas_.keySet()) {			
			Set<Answer> as=qas_.get(q);
			for (Answer ans:as) {
				String sent=id_sent_.get(ans.sid_);
				Utils.println("<question str=\""+q+"\">");
				Utils.println("<label></label>");
				Utils.println("<answer>"+ans.rst_+"</answer>");
				Utils.println("<sentence id=\""+ans.sid_+"\">"+sent+"</sentence>");
				Utils.println("</question>\n");
			}
		}
	}
	
	// based on clust
	static String getTreeStr(String ptId) {
		Map<String, String> id_str=new TreeMap<String,String>();
		if (ptId_aciChdIds_.get(ptId)!=null) {
			for (Integer aci: ptId_aciChdIds_.get(ptId).keySet()) {
				Set<String> cids=ptId_aciChdIds_.get(ptId).get(aci);
				for (String cid: cids) {
					String s="";
					String dep=ptId_parDep_.get(cid);
					
					// skip 
					if (!allowedDeps_.contains(dep)) continue;
					
					s+=getTreeStr(cid);
					id_str.put(cid,s);
				}
			}
		}
		id_str.put(ptId,""+ptId_clustIdxStr_.get(ptId).getFirst());
		
		String x="";
		for (String id:id_str.keySet()) {
			if (x.length()>0) x+=" ";
			x+=id_str.get(id);
		}
		return x;
	}
	
	// based on word
	static String getTreeStrOld(String ptId) {
		Map<String, String> id_str=new TreeMap<String,String>();
		if (ptId_aciChdIds_.get(ptId)!=null) {
			for (Integer aci: ptId_aciChdIds_.get(ptId).keySet()) {
				Set<String> cids=ptId_aciChdIds_.get(ptId).get(aci);
				for (String cid: cids) {
					String s="";
					String dep=ptId_parDep_.get(cid);
					
					// skip 
					if (!allowedDeps_.contains(dep)) continue;
					
					if (dep.indexOf("prep_")==0) {
						s=dep.substring(5)+" ";
					}
					
					s+=getTreeStrOld(cid);
					id_str.put(cid,s);
				}
			}
		}
		id_str.put(ptId,ptId_clustIdxStr_.get(ptId).getSecond());
		
		String x="";
		for (String id:id_str.keySet()) {
			if (x.length()>0) x+=" ";
			x+=id_str.get(id);
		}
		return x;
	}


	static boolean contains(String cs,String c) {
		String[] x=cs.split(" ");
		for (int i=0; i<x.length; i++) if (c.equals(x[i])) return true;
		return false;
	}
	static boolean contains(ArrayList<String> cis, int ci) {
		String x=""+ci;
		for (int i=0; i<cis.size(); i++) {
			if (contains(cis.get(i),x)) return true;
		}
		return false;
	}

	static Set<String> getTreeCis(String ptId) {
		Set<String> cis=new TreeSet<String>();
		cis.add(ptId_clustIdxStr_.get(ptId).getFirst()+"");
		if (ptId_aciChdIds_.get(ptId)!=null) {
			for (Integer aci: ptId_aciChdIds_.get(ptId).keySet()) {
				Set<String> cids=ptId_aciChdIds_.get(ptId).get(aci);
				for (String cid: cids) {
					String s="";
					String dep=ptId_parDep_.get(cid);
					
					// skip 
					if (!allowedDeps_.contains(dep)) continue;
										
					Set<String> x=getTreeCis(cid);
					cis.addAll(x);
				}
			}
		}
		return cis;
	}
	

	static boolean isMatchFromHead(String chdPtId, ArrayList<String> cis) {
		int hci=ptId_clustIdxStr_.get(chdPtId).getFirst();
		if (!contains(cis,hci)) return false;
	
		Set<String> tcis=getTreeCis(chdPtId);
		for (int i=0; i<cis.size(); i++) {			
			String x=cis.get(i);
			String[] ts=x.split(" ");
			boolean ok=false;
			for (int k=0; k<ts.length; k++) if (tcis.contains(ts[k])) {ok=true;break;}
			if (!ok) return false;
		}
		return true;
	}
	
	static boolean isMatch(String chdPtId, String arg) {	
		ArrayList<ArrayList<String>> allcis=arg_cis_.get(arg);
		for (ArrayList<String> cis:allcis) {
			if (isMatchFromHead(chdPtId,cis)) return true;
		}
		
		if (ptId_aciChdIds_.get(chdPtId)==null) return false;
		for (Integer aci:ptId_aciChdIds_.get(chdPtId).keySet()) {
			Set<String> cids=ptId_aciChdIds_.get(chdPtId).get(aci);
			for (String cid:cids) {
				String dep=ptId_parDep_.get(cid);
				if ((dep.indexOf("conj_")==0 && !dep.equals("conj_negcc")) || dep.equals("appos")) {
					for (ArrayList<String> cis:allcis) {
						if (isMatchFromHead(cid,cis)) return true;
					}
				}
			}
		}
		return false;
	}

	// aci: given; aci2: ans
	static void match(Question q, String pid, int aci, int aci2) {
		if (ptId_aciChdIds_.get(pid)==null) return;
		if (ptId_aciChdIds_.get(pid).get(aci)==null || ptId_aciChdIds_.get(pid).get(aci2)==null) return;

		// negation
		for (Integer x:ptId_aciChdIds_.get(pid).keySet()) {
			if (x==aci || x==aci2) continue;
			for (String cid:ptId_aciChdIds_.get(pid).get(x)) {
				String dep=ptId_parDep_.get(cid);
				if (dep.equals("neg")) {
					return;
				}
			}
		}
		
		// match aci w. arg
		boolean isMatch=false;
		for (String cid:ptId_aciChdIds_.get(pid).get(aci)) {
			if (isMatch(cid,q.arg_)) {
				isMatch=true;
				break;
			}
		}
		
		// retrieve aci2
		if (!isMatch) return;
		for (String cid:ptId_aciChdIds_.get(pid).get(aci2)) {
			findAns(q,cid);	// recursively construct ans; take care of and/appos
		}
	}
	
	static String getSentId(String ptId) {
		int i=ptId.lastIndexOf(':');
		return ptId.substring(0,i);
	}	
	static String getArticleId(String ptId) {
		int i=ptId.indexOf(':');
		return ptId.substring(0,i);
	}
	static int getSentIdx(String ptId) {
		int i1=ptId.indexOf(':');
		int i2=ptId.lastIndexOf(':');
		return Integer.parseInt(ptId.substring(i1+1,i2));
	}
	static int getTknIdx(String ptId) {
		int i=ptId.lastIndexOf(':');
		return Integer.parseInt(ptId.substring(i+1));
	}
	
	static void findAns(Question q,String pid) {
		String sid=getSentId(pid);
		
		String aid=getArticleId(pid);
		int sIdx=getSentIdx(pid);
		Article art=id_article_.get(aid);
		Sentence sent=art.sentences_.get(sIdx);
		
		Map<String,String> pid_minPid=new HashMap<String,String>();
		ArrayList<TreeSet<String>> ans=findAns(pid, pid_minPid);
		for (int i=0; i<ans.size(); i++) {
			TreeSet<String> a=ans.get(i);
			TreeSet<Integer> na=new TreeSet<Integer>();	// tknIdx; may add some prep/det
			
			TreeMap<Integer,String> idx_prep=new TreeMap<Integer,String>();
			
			for (String id:a) {
				// look up dependency
				int tknIdx=getTknIdx(id);
				na.add(tknIdx);
				
				// det
				int detIdx=-1;
				Set<Pair<String,Integer>> chd=sent.tkn_children_.get(tknIdx);
				if (chd!=null)
				for (Pair<String,Integer> depChd: chd) {
					if (depChd.getFirst().equals("det")) {
						detIdx=depChd.getSecond();
						na.add(detIdx);
						break;
					}
				}

				// prep?
				Pair<String,Integer> par=sent.tkn_par_.get(tknIdx);
				if (par!=null && par.getFirst().indexOf("prep_")==0) {
					int parIdx=par.getSecond();
					String parId=Utils.genTreeNodeId(aid, sIdx, parIdx);
					if (a.contains(parId)) {
						// should include the preposition in the answer string
						String prep=par.getFirst().substring("prep_".length());
						String mpid=pid_minPid.get(id);
						int midx=getTknIdx(mpid);
						if (detIdx>=0 && detIdx<midx) midx=detIdx;
						idx_prep.put(midx, prep);
					}
				}
			}
			String s="";
			for (Integer idx:na) {
				// prep
				if (!idx_prep.isEmpty()) {
					int pidx=idx_prep.firstKey();
					if (idx>=pidx) {
						String prep=idx_prep.get(pidx);
						if (s.length()>0) s+=" ";
						s+=prep;
						idx_prep.remove(pidx);
					}
				}
				
				// word
				String word=sent.tokens_.get(idx).getForm();
				
				String xid=Utils.genTreeNodeId(aid, sIdx, idx);
				if (ptId_clustIdxStr_.get(xid)!=null) {
				String xs=ptId_clustIdxStr_.get(xid).getSecond();
				if (xs.indexOf(' ')>0) word=xs;
				}
				
				if (s.length()>0) s+=" ";
				s+=word;				
			}
			Answer x=new Answer(sid,s);
			Set<Answer> y=qas_.get(q);
			if (y==null) {
				y=new TreeSet<Answer>();
				qas_.put(q, y);
			}
			y.add(x);
		}
	}
	
	// minPid for inserting prep, if any
	static ArrayList<TreeSet<String>> findAns(String pid, Map<String, String> pid_minPid) {
		ArrayList<TreeSet<String>> ans=new ArrayList<TreeSet<String>>();
		ArrayList<TreeSet<String>> curr=new ArrayList<TreeSet<String>>();
		TreeSet<String> z=new TreeSet<String>();
		z.add(pid);
		curr.add(z);
		pid_minPid.put(pid, pid);
		
		if (ptId_aciChdIds_.get(pid)!=null) { 
		for (Integer x:ptId_aciChdIds_.get(pid).keySet()) {
			for (String cid:ptId_aciChdIds_.get(pid).get(x)) {
				
				String dep=ptId_parDep_.get(cid);
			
				if (dep.indexOf("conj_")==0 && !dep.equals("conj_negcc") || dep.equals("appos")) {
					ArrayList<TreeSet<String>> y=findAns(cid, pid_minPid);
					ans.addAll(y);
					if (pid_minPid.get(cid).compareTo(pid_minPid.get(pid))<0)
						pid_minPid.put(pid, pid_minPid.get(cid));
				}
				else if (allowedDeps_.contains(dep)) {
					ArrayList<TreeSet<String>> curr1=new ArrayList<TreeSet<String>>();
					ArrayList<TreeSet<String>> y=findAns(cid, pid_minPid);
					if (pid_minPid.get(cid).compareTo(pid_minPid.get(pid))<0)
						pid_minPid.put(pid, pid_minPid.get(cid));
					for (int i=0; i<curr.size(); i++) {
						TreeSet<String> a=curr.get(i);
						for (int k=0; k<y.size(); k++) {
							TreeSet<String> b=y.get(k);
							TreeSet<String> c=new TreeSet<String>();
							c.addAll(a); c.addAll(b);
							curr1.add(c);
						}
					}
					curr=curr1;
				}
			}
		}
		}
		ans.addAll(curr);
		return ans;
	}
	
	static void match() {
		for (String rel:rel_qs_.keySet()) {
//			Utils.println("Processing "+rel);
			ArrayList<Question> qs=rel_qs_.get(rel);
			int ci=rel_clustIdx_.get(rel);//identify verbs, ci, verb cluster id
			Set<String> pids=clustIdx_ptIds_.get(ci);//get verbs partid

			for (Question q:qs) {
//				Utils.println("\tProcess " +q);
				
				// find aci
				String dep=q.dep_;
				String dep2=(q.dep_.equals("nsubj"))?"dobj":"nsubj";
				int aci=clustIdx_depArgClustIdx_.get(ci).get(dep), aci2=clustIdx_depArgClustIdx_.get(ci).get(dep2);
				for (String pid:pids) match(q,pid,aci,aci2);
			}
		}
	}
	

	static void preprocArgs() {
		for (String r: rel_qs_.keySet()) {
			ArrayList<Question> qs=rel_qs_.get(r);
			Set<Question> ignoredQs=new HashSet<Question>();	// ignore due to missing form
			for (Question q: qs) {
				if (arg_cis_.containsKey(q.arg_)) continue;
				
				ArrayList<ArrayList<String>> cis=new ArrayList<ArrayList<String>>();
				
				ArrayList<String> x=new ArrayList<String>();
				String[] ts=q.arg_.split(" ");
				
				// find lemmas
				boolean isIgnored=false;
				for (int i=0; i<ts.length; i++) {
					String f=ts[i];
					
					// TO-DO: match rel
					if (f.equals("the") || f.equals("of") || f.equals("in")) continue;
					Set<String> z=new TreeSet<String>();
					Set<String> ls=form_lemma_.get(f);
					
					if (ls==null) {
						//Utils.println("ERR: "+f);
						isIgnored=true;
						break;
					}					
					for (String l:ls) {
						if (lemma_clustIdxs_.get(l)==null) ;//Utils.println("ERR: missing lemma: "+l);
						else z.addAll(lemma_clustIdxs_.get(l));												
					}
					
					String s="";
					for (String ci:z) {
						if (s.length()>0) s+=" ";
						s+=ci;
					}
					x.add(s);
				}
				if (isIgnored) {ignoredQs.add(q); continue;}
				
				// generate all possible matches
				cis.add(x);
				
				// sort all possible match: TO-DO only check last two for now
				if (ts.length>=2) {
					Set<String> z=new TreeSet<String>();
					
					// check if last two are together
					Set<String> hs=form_lemma_.get(ts[ts.length-1]);
					Set<String> ds=form_lemma_.get(ts[ts.length-2]);
					for (String h:hs) {
						for (String d:ds) {
							Pair<String,String> hd=new Pair<String,String>(h,d);
							String ci=headDep_clustIdxs_.get(hd);
							if (ci!=null) {
								z.add(ci);								
							}
						}
					}
					
					if (z.size()>0) {
						ArrayList<String> y=new ArrayList<String>();
						
						// TO-DO: may have delete the/of/ before last?
						for (int i=0; i<x.size()-2; i++) {
							y.add(x.get(i));
						}
						String s="";
						for (String ci:z) {
							if (s.length()>0) s+=" ";
							s+=ci;
						}
						y.add(s);
						cis.add(y);
					}	
								
				}
				
				arg_cis_.put(q.arg_, cis);
			}
			qs.removeAll(ignoredQs);

			ignoredQs=new HashSet<Question>();	// ignore due to missing form
			for (Question q:qs) {
				// find aci
				if (rel_clustIdx_.get(q.rel_)==null) 
					Utils.println("ERR: "+q.rel_);
				int ci=rel_clustIdx_.get(q.rel_);
				String dep=q.dep_;
				String dep2=(q.dep_.equals("nsubj"))?"dobj":"nsubj";
				if (clustIdx_depArgClustIdx_.get(ci).get(dep)==null || clustIdx_depArgClustIdx_.get(ci).get(dep2)==null) {
					ignoredQs.add(q);
					//Utils.println("ERR: missing arg: "+q);
				}
			}
			qs.removeAll(ignoredQs);
		}
		

	}
	static String removeThirdPerson(String v) {
		if (v.length()<=3) Utils.println("*** "+v);
		int l=v.length();
		if (v.charAt(l-2)!='e') {			
			return v.substring(0,l-1);
		}
		else if (v.charAt(l-3)=='i') return v.substring(0,l-3)+"y";
		else if (v.charAt(l-3)=='s' && v.charAt(l-4)=='s') return v.substring(0,l-2);
		else if (v.charAt(l-3)=='h' && v.charAt(l-4)=='s') return v.substring(0,l-2);
		else return v.substring(0,l-1);
	}
}
