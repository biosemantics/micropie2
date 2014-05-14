package usp.semantic;

import java.io.*;
import java.util.*;

import usp.util.*;
import usp.syntax.*;

public class Parse {
	static String outputDir_="sem";
	
	static boolean isDebug_=false;
	
	//
	ArrayList<String> inputFileNames_;
	ParseReader parseReader_;
	Scorer scorer_;
	Agenda agenda_;
	Executor executor_;
	
	int numSents_=0, numTkns_=0;
	Map<String,Article> id_article_=new TreeMap<String,Article>();

	// HACK: ignore stop-nouns TO-DO: more principled way
//	static int leafNounFreqThreshold_=1000;	// for genia
//	static Set<Token> ignoredLeafNouns_=new HashSet<Token>();
	
	Set<String> rootTreeNodeIds_=new TreeSet<String>();	

	static String rstDir_="results";
	static String dataDir_="temp";
	
	
	
	
	public static void main(String[] args) throws Exception {
		if (args.length==0) {
			Utils.println("java -cp usp.jar semantic.Parse <dataDir> <resultDir> [priorNumParam=<val>] [priorNumConj=<val>]");
			Utils.println("Options and default values:");
			Utils.println("   priorNumParam=-5 (prior on parameter number)");
			Utils.println("   priorNumConj=-10 (prior on number of conjunctive parts assigned to same cluster");
			return;
		}
		
		usp.util.Timer timer=new usp.util.Timer();
		
		PrintStream out=null;	
		
		// proc params
		dataDir_=args[0];
		rstDir_=args[1];
		for (int i=2; i<args.length; i++) {
			String s=args[i];
			String attr=s.substring(0,s.indexOf('='));
			String val=s.substring(s.indexOf('=')+1);
			if (attr.equals("priorNumParam")) 
				ParseParams.priorNumParam_=-Double.parseDouble(val);
			else if (attr.equals("priorNumConj")) 
				ParseParams.priorNumConj_=-Double.parseDouble(val);
		}
		
		String outputId=dataDir_;
		if (outputId.indexOf(Utils.FILE_SEP)>=0) outputId=outputId.substring(outputId.lastIndexOf(Utils.FILE_SEP));

		//
		out=new PrintStream(rstDir_+Utils.FILE_SEP+outputId+".log");
		Utils.setOut(out);

		// log params
		Utils.println("=== Params ===");
		Utils.println("resultDir\t"+rstDir_);
		Utils.println("dataDir\t"+dataDir_);
		Utils.println("priorNumParam\t"+(-ParseParams.priorNumParam_));
		Utils.println("priorNumConj\t"+(-ParseParams.priorNumConj_));
		Utils.println();
		
		// System.out.println("inputFimeNames is :" + dataDir_);
		// gather files
		ArrayList<String> inputFileNames=new ArrayList<String>();
		
		// gather input filenames: dev1
		Utils.println("Gathering filenames ...");
		
//		String rootDir="/projects/dm/2/hoifung/projects/bio/data/pubmed/dev1";
//		String rootDir="/projects/dm/2/hoifung/projects/bio/data/genia";
//		if (data.equals("wsj")) rootDir="/projects/dm/2/hoifung/projects/bio/data/wsj";

		File rdf=new File(dataDir_+"/dep");
		String[] dirs=rdf.list();

		Arrays.sort(dirs);
		
		int maxFold=dirs.length;
		// System.out.println("maxFold dir "+ maxFold);
		for (int i=0; i<maxFold; i++) {	// try 2k files first
		//for (int i=0; i<dirs.length; i++) {
//			Utils.println("Processing dir "+dirs[i]);
			// System.out.println("Processing dir " + dirs[i]);
			
			File d=new File(rdf.getPath()+Utils.FILE_SEP+dirs[i]);
			//Utils.println("proc "+d);
			// System.out.println("proc " + d);
			if (!d.isDirectory()) continue;
			String[] fns=d.list();
			// System.out.println("fns is " + fns.toString());
			Arrays.sort(fns);
			for (int j=0; j<fns.length; j++) {
				// System.out.println("fns.length is " + fns.length);
				if (fns[j].indexOf(".dep")<0) continue;
				String fn=d.getPath()+Utils.FILE_SEP+fns[j].substring(0,fns[j].indexOf('.'));
				inputFileNames.add(fn);
			}
		}
		Utils.println("<PREPROC> Filenames gathered: "+inputFileNames.size()+" files");
		// System.out.println("<PREPROC> Filenames gathered: "+inputFileNames.size()+" files");
		
		Parse parse=new Parse(inputFileNames);
		parse.parse();
		out.close();
		
		long time=timer.getTimeElapsed();
		Utils.println("<TIME> Total="+((time/1000))+" s");
		
		MLN.printModel(rstDir_+Utils.FILE_SEP+outputId);
	}
	
	public Parse () {
		// Constructor
	}
	
	public void runParse(String dataDir_, String rstDir_) throws Exception {
		// System.out.println("dataDir_::" + dataDir_);
		// System.out.println("rstDir_::" + rstDir_);
		
		new File("usp_results").mkdirs();
		
		usp.util.Timer timer=new usp.util.Timer();

		PrintStream out=null;	
		
		String outputId=dataDir_;
		if (outputId.indexOf(Utils.FILE_SEP)>=0) outputId=outputId.substring(outputId.lastIndexOf(Utils.FILE_SEP));

		//
		out=new PrintStream(rstDir_+Utils.FILE_SEP+outputId+".log");
		Utils.setOut(out);

		// log params
		Utils.println("=== Params ===");
		Utils.println("resultDir\t"+rstDir_);
		Utils.println("dataDir\t"+dataDir_);
		Utils.println("priorNumParam\t"+(-ParseParams.priorNumParam_));
		Utils.println("priorNumConj\t"+(-ParseParams.priorNumConj_));
		Utils.println();
		
		// System.out.println("inputFimeNames is :" + dataDir_);
		// gather files
		ArrayList<String> inputFileNames=new ArrayList<String>();
		
		// gather input filenames: dev1
		Utils.println("Gathering filenames ...");
		
//		String rootDir="/projects/dm/2/hoifung/projects/bio/data/pubmed/dev1";
//		String rootDir="/projects/dm/2/hoifung/projects/bio/data/genia";
//		if (data.equals("wsj")) rootDir="/projects/dm/2/hoifung/projects/bio/data/wsj";

		File rdf=new File(dataDir_+"/dep");
		String[] dirs=rdf.list();

		Arrays.sort(dirs);
		
		int maxFold=dirs.length;
		// System.out.println("maxFold dir "+ maxFold);
		for (int i=0; i<maxFold; i++) {	// try 2k files first
		//for (int i=0; i<dirs.length; i++) {
//			Utils.println("Processing dir "+dirs[i]);
			// System.out.println("Processing dir " + dirs[i]);
			
			File d=new File(rdf.getPath()+Utils.FILE_SEP+dirs[i]);
			//Utils.println("proc "+d);
			// System.out.println("proc " + d);
			if (!d.isDirectory()) continue;
			String[] fns=d.list();
			// System.out.println("fns is " + fns.toString());
			Arrays.sort(fns);
			for (int j=0; j<fns.length; j++) {
				// System.out.println("fns.length is " + fns.length);
				if (fns[j].indexOf(".dep")<0) continue;
				String fn=d.getPath()+Utils.FILE_SEP+fns[j].substring(0,fns[j].indexOf('.'));
				inputFileNames.add(fn);
			}
		}
		Utils.println("<PREPROC> Filenames gathered: "+inputFileNames.size()+" files");
		// System.out.println("<PREPROC> Filenames gathered: "+inputFileNames.size()+" files");
		
		// System.out.println("inputFileNames ::" + inputFileNames); // Elvis Wu's test
		
		Parse parse=new Parse(inputFileNames);
		parse.parse();
		out.close();
		
		long time=timer.getTimeElapsed();
		Utils.println("<TIME> Total="+((time/1000))+" s");
		
		MLN.printModel(rstDir_+Utils.FILE_SEP+outputId);
	}
	
	
	public Parse(ArrayList<String> fileNames) {		
		inputFileNames_=fileNames;
		//parseReader_=new CONLLParseReader();
		parseReader_=new StanfordParseReader();
		scorer_=new Scorer(this);
		agenda_=new Agenda(this);
		executor_=new Executor(this);
	}
	
	// record input file names
	public void parse() throws Exception {
		usp.util.Timer timer = new usp.util.Timer();
		
		//
		Utils.println("[Parse] read syntax ...");		
		ArrayList<Article> articles=new ArrayList<Article>();
		for (int i=0; i<inputFileNames_.size(); i++) {
			Article a=parseReader_.readParse(inputFileNames_.get(i));
			// System.out.println("inputFileNames_::" + inputFileNames_);
			// System.out.println("Elvis Test::inputFileNames_.get(i)::" + inputFileNames_.get(i).toString());
			articles.add(a);
		}
		
		Utils.println("<TIME> Read parse "+((int)(timer.getTimeElapsed()/1000))+"s");
		timer.timerStart();


		// create initial parts
		Utils.println("[Parse] initial partition/clust/agenda ...");
		initialize(articles);
		
		Utils.println("<TIME> Init "+((int)(timer.getTimeElapsed()/1000))+"s");
		timer.timerStart();
		
		// merge args for bootstrap clusters
		mergeArgs();
		
		Utils.println("<TIME> Merge args "+((int)(timer.getTimeElapsed()/1000))+"s");
		timer.timerStart();

		// generate initial agenda from bootstrap clusts
		agenda_.createAgenda();
		Utils.println("<TIME> Create agenda "+((int)(timer.getTimeElapsed()/1000))+"s");
		timer.timerStart();
	
		// proc agenda
		agenda_.procAgenda();
		
		Utils.println("<TIME> Proc agenda "+((int)(timer.getTimeElapsed()/1000))+"s");
		timer.timerStart();
	}	
		
	void initialize(ArrayList<Article> articles) {
		for (int ai=0; ai<articles.size(); ai++) {
			Article a=articles.get(ai);			
			String aid=a.uid_;
			
			id_article_.put(aid, a);
			
//			Utils.println("[Parse] init: "+aid+" "+a.sentences_.size()+" sentences");
			
			numSents_+=a.sentences_.size();
			
			for (int si=0; si<a.sentences_.size(); si++) {				
				Sentence sent=a.sentences_.get(si);
//				Utils.println("\tsent "+si+" "+sent.tokens_.size()+" tkns");
				
				initialize(aid, si, sent);
			} // for sentences
		} // for article
		
		Utils.println("<INFO> numSent="+numSents_+" numTkn="+numTkns_);
		
	} // initialize
	
	void initialize(String aid, int si, Sentence sent) {
		numTkns_+=sent.tokens_.size()-1; // root
		
		if (sent.tkn_children_.get(0)==null || sent.tkn_children_.get(0).size()==0) return;
						
		// build treenode, part, clust for all nodes	// SKIP root
		for (int ni=1; ni<sent.tokens_.size(); ni++) {
			if (isIgnore(sent, ni)) continue;				
			
			String tnId=Utils.genTreeNodeId(aid,si,ni);
			TreeNode tn=new TreeNode(tnId,sent.tokens_.get(ni));
			Part part=new Part(tn);
			
			int relTypeIdx=part.getRelTypeIdx();
			int clustIdx=-1;
			Set<Integer> clustIdxs=Clust.getClustsWithRelType(relTypeIdx);
			if (clustIdxs!=null) {
				clustIdx=clustIdxs.iterator().next();
			}
			else clustIdx=Clust.createClust(relTypeIdx);
			part.setClust(clustIdx);									
		}
		
		// build args; link part
		Set<Pair<String,Integer>> roots=sent.tkn_children_.get(0);
		assert roots.size()==1;		
		Iterator<Pair<String,Integer>> it=roots.iterator();
		while (it.hasNext()) {
			Pair<String,Integer> dep_idx=it.next();
			int idx=dep_idx.getSecond();
			String nid=Utils.genTreeNodeId(aid,si,idx);
			rootTreeNodeIds_.add(nid);
			Part np=Part.getPartByRootNodeId(nid);
			if (np==null) {Utils.println("Empty part: art="+aid+" sent="+si); continue;}
			Clust ncl=Clust.getClust(np.getClustIdx());
			ncl.incRootCnt();
			createArgs(aid, si, sent, idx);					
		}
	}
	
	boolean isIgnore(Sentence sent, int tknIdx) {
		int ancestor=tknIdx;
		while (sent.tkn_par_.get(ancestor)!=null) {
			ancestor=sent.tkn_par_.get(ancestor).getSecond();
		}
		return (ancestor>0);
	}
	
	
	void createArgs(String articleId, int sentIdx, Sentence sent, int nodeIdx) {		
//		
		System.out.println("articleId:"+ articleId);
		System.out.println("sentIdx:"+ sentIdx);
		System.out.println("nodeIdx:"+ nodeIdx);
		
		
		Utils.println("createArgs: "+articleId+" sent="+sentIdx+" node="+nodeIdx);
		System.out.println("createArgs: "+articleId+" sent="+sentIdx+" node="+nodeIdx);
		
		String nid=Utils.genTreeNodeId(articleId,sentIdx,nodeIdx);
		TreeNode node=TreeNode.getTreeNode(nid);
		Part np=Part.getPartByRootNodeId(nid);
		Clust ncl=Clust.getClust(np.getClustIdx());
		Set<Pair<String,Integer>> chds=sent.tkn_children_.get(nodeIdx);
		System.out.println("chds::" + chds);
		if (chds==null) return;
		
		Iterator<Pair<String,Integer>> it=chds.iterator();
		while (it.hasNext()) {
			Pair<String,Integer> dep_chd=it.next();
			String dep=dep_chd.getFirst();
			
			int cidx=dep_chd.getSecond();
			String cid=Utils.genTreeNodeId(articleId,sentIdx,cidx);
			Path p=new Path(dep);			
			int argTypeIdx=p.getArgType();
//			Utils.println("---> dep="+dep+" path="+p.toString()+" argTypeIdx="+argTypeIdx+" argType="+ArgType.getArgType(argTypeIdx));
			
			System.out.println("---> dep="+dep+" path="+p.toString()+" argTypeIdx="+argTypeIdx+" argType="+ArgType.getArgType(argTypeIdx));
			
			Part cp=Part.getPartByRootNodeId(cid);
			
			// Original code
			// if (cp==null) Utils.println("ERR: cp=null "+cid+" "+cidx+" "+dep+" "+nodeIdx+" "+sentIdx);

			// // TO-DO: fix this, should have unique par
			// if (cp.parPart_!=null) {
			//	Utils.println("ERR: Multiple parents, skip np: cp="+cp.relTreeRoot_.getId()+" par="+cp.parPart_.relTreeRoot_.getId()+" np="+np.relTreeRoot_.getId());
			//	continue;
			// }
			// Original code
			
			// Modified code by Elvis
			if (cp==null) {
				Utils.println("ERR: cp=null "+cid+" "+cidx+" "+dep+" "+nodeIdx+" "+sentIdx);
				System.out.println("ERR: cp=null "+cid+" "+cidx+" "+dep+" "+nodeIdx+" "+sentIdx);
				// continue; // Elvis add on 3/11/2014 to prevent the parsing error
			} else{ 
				// TO-DO: fix this, should have unique par
				if (cp.parPart_!=null) {
					Utils.println("ERR: Multiple parents, skip np: cp="+cp.relTreeRoot_.getId()+" par="+cp.parPart_.relTreeRoot_.getId()+" np="+np.relTreeRoot_.getId());
					System.out.println("ERR: Multiple parents, skip np: cp="+cp.relTreeRoot_.getId()+" par="+cp.parPart_.relTreeRoot_.getId()+" np="+np.relTreeRoot_.getId());
					
				} else {
					
					System.out.println("Create Argument - Start");
					Argument arg=new Argument(node,p,cp);
					int argIdx=np.addArgument(arg);		
					cp.setParent(np, argIdx);
					
					// arg
					Set<Integer> argClustIdxs=ncl.getArgClustIdxs(argTypeIdx);
					int argClustIdx=-1;
					if (argClustIdxs==null) {
						argClustIdx=ncl.createArgClust(argTypeIdx);
						System.out.println("argClustIdxs is null");

					}
					else {
						// TO-DO: multiple ones??
						
						System.out.println("argClustIdxs.size()::" + argClustIdxs.size());
						
						argClustIdx=argClustIdxs.iterator().next();
						
						System.out.println("iterate argClustIdx");
						
					}
					System.out.println("Set ArgClust - Start");
					np.setArgClust(argIdx, argClustIdx);
					System.out.println("Set ArgClust - End");
					
					// recursive
					System.out.println("Go to recursive createArgs() 1");
					createArgs(articleId, sentIdx, sent, cidx);
					System.out.println("Go to recursive createArgs() 2");
				}
		
			}
			// Modified code by Elvis


		}
	}
		
	// merge args for initial clusts
	void mergeArgs() {
		if (isDebug_) Utils.println("MergeArgs ...");
		Iterator<Integer> iit=Clust.clusts_.keySet().iterator();
		while (iit.hasNext()) {
			int clustIdx=iit.next();
			Clust cl=Clust.getClust(clustIdx);
			if (isDebug_) Utils.println("\tclust="+cl+" ttlCnt="+cl.ttlCnt_);
			Map<Integer,ArgClust> newArgClusts=new HashMap<Integer,ArgClust>();
			
			// generate an ordering: in decreasing order of freq
			Object[] cnt_acis=new Object[cl.argClusts_.size()];			
			Iterator<Integer> iit2=cl.argClusts_.keySet().iterator();
			for (int i=0; iit2.hasNext(); i++) {
				int argClustIdx=iit2.next();
				ArgClust acl=cl.argClusts_.get(argClustIdx);
				int cnt=acl.ttlArgCnt_;
				cnt_acis[i]=new Pair<Integer,Integer>(cnt,argClustIdx);
			}									
			Arrays.sort(cnt_acis);			
			
			// greedily add arg & merge
			for (int i=cnt_acis.length-1; i>=0; i--) {
				Pair<Integer,Integer> cnt_aci=(Pair<Integer,Integer>) cnt_acis[i];
				int aci=cnt_aci.getSecond();
				ArgClust ac=cl.argClusts_.get(aci);
				if (newArgClusts.size()==0) {
					if (isDebug_) Utils.println("\t\t"+ac+" -> ");
					newArgClusts.put(aci,ac); continue;
				}
				
				// determine which to add
				if (isDebug_) Utils.println("\t\t"+ac+" -> ");
				double maxScore=0;	// no merge
				int maxMap=-1;
				Iterator<Integer> iitx=newArgClusts.keySet().iterator();
				while (iitx.hasNext()) {
					int acix=iitx.next();
					double score=scorer_.scoreMergeArgs(cl, acix, aci);
					ArgClust acx=cl.argClusts_.get(acix);
					if (isDebug_) Utils.println("\t\t\t"+acx.toString()+" "+score);
					if (score>maxScore) {
						maxScore=score;
						maxMap=acix;
					}
				}
				
				// add				
				if (maxMap>=0) {
					ArgClust acx=cl.argClusts_.get(maxMap);
					if (isDebug_) Utils.println("\t\t\t===> "+acx.toString());
					executor_.mergeArg(cl,maxMap,aci);
				}
				else {
					newArgClusts.put(aci,ac);
				}
				if (isDebug_) Utils.println();
			}
			
			// reset argClust
			cl.argClusts_=newArgClusts;
		}
	}

	void chkArgs() {
		Utils.println("chkArgs ...");
		for (Integer idx: Clust.clusts_.keySet()) {
			Clust cl=Clust.getClust(idx);
			for (Integer aci: cl.argClusts_.keySet()) {
				ArgClust ac=cl.argClusts_.get(aci);
				
				int pc=ac.partRootTreeNodeIds_.size();
				int xpc=0;
				for (Integer an: ac.argNum_cnt_.keySet()) {
					if (an==0) {
						Utils.println("ERR: argnum=0 "+cl+" : "+ac);
					}
					else if (ac.argNum_cnt_.get(an)==0) {						
						Utils.println("ERR: argnum cnt=0 "+cl+" : "+ac);
					}
					else xpc+=ac.argNum_cnt_.get(an);
				}
				if (pc!=xpc) Utils.println("ERR: arg partcnt!=argnum_cnt: "+cl+" : "+ac);
				if (pc>cl.ttlCnt_) Utils.println("ERR: arg partcnt>ttlcnt! "+cl+" : "+ac+" pc="+pc+" ttl="+cl.ttlCnt_);

				int tac=ac.ttlArgCnt_;
				int xac=0;
				for (Integer ati: ac.argTypeIdx_cnt_.keySet()) {
					if (ac.argTypeIdx_cnt_.get(ati)==0) {
						Utils.println("ERR: argtype cnt=0 "+cl+" : "+ac);
					}
					else xac+=ac.argTypeIdx_cnt_.get(ati);
				}
				if (tac!=xac) Utils.println("ERR: argcnt!=argtype_cnt: "+cl+" : "+ac);
				
				xac=0;
				for (Integer cci: ac.chdClustIdx_cnt_.keySet()) {
					if (ac.chdClustIdx_cnt_.get(cci)==0) {
						Utils.println("ERR: chdclust cnt=0 "+cl+" : "+ac+" : "+Clust.getClust(cci));
					}
					else xac+=ac.chdClustIdx_cnt_.get(cci);
				}
				if (tac!=xac) Utils.println("ERR: argcnt!=chdclust_cnt: "+cl+" : "+ac);
			}
		}
	}

	// reparse a sentence
	public void reparse(String aid, int si) {
		Article a=id_article_.get(aid);
		Sentence sent=a.sentences_.get(si);

		if (sent.tkn_children_.get(0)==null || sent.tkn_children_.get(0).size()==0) return;

		// collect existing parts; remove pointers
		Map<String, Part> old_nid_part=new HashMap<String,Part>();		
		for (int ni=0; ni<sent.tokens_.size(); ni++) {
			if (isIgnore(sent, ni)) continue;			
			String nid=Utils.genTreeNodeId(aid,si,ni);
			Part np=Part.getPartByRootNodeId(nid);
			Part.rootNodeId_part_.remove(nid);
			old_nid_part.put(nid, np);
		}
		
		// ---------------------------- //
		// generate new parse: initialize, assign arg, search
		//		- do not alter stat
		// ---------------------------- //
		Map<String, Part> nid_part=new HashMap<String,Part>();
		
		// build treenode, part, clust for all nodes	// SKIP root
		for (int ni=1; ni<sent.tokens_.size(); ni++) {
			if (isIgnore(sent, ni)) continue;				
			
			String tnId=Utils.genTreeNodeId(aid,si,ni);
			TreeNode tn=new TreeNode(tnId,sent.tokens_.get(ni));
			Part part=new Part(tn);
			nid_part.put(tnId, part);
			
			int relTypeIdx=part.getRelTypeIdx();
			int clustIdx=-1;
			Set<Integer> clustIdxs=Clust.getClustsWithRelType(relTypeIdx);
			if (clustIdxs!=null) {
				clustIdx=clustIdxs.iterator().next();
			}
			else clustIdx=Clust.createClust(relTypeIdx);
			part.setClustOnly(clustIdx);
		}
		
		// build args; link part
		Set<Pair<String,Integer>> roots=sent.tkn_children_.get(0);
		assert roots.size()==1;		
		Iterator<Pair<String,Integer>> it=roots.iterator();
		while (it.hasNext()) {
			Pair<String,Integer> dep_idx=it.next();
			int idx=dep_idx.getSecond();
			String nid=Utils.genTreeNodeId(aid,si,idx);
			Part np=Part.getPartByRootNodeId(nid);
			if (np==null) {Utils.println("Empty part: art="+aid+" sent="+si); continue;}
			setArgs(aid,si,sent,idx);
		}
		
		// recursively find lambda-reducible parts that improve prb
		while (true) {
			Part rp=null, ap=null;
			double maxImp=0;
			for (String nid: nid_part.keySet()) {
				Part rp1=nid_part.get(nid);
				
				// score
				for (Integer ai: rp1.args_.keySet()) {
					Part ap1=rp1.getArgument(ai).argPart_;
					double score=scorer_.scoreOpComposePart(rp1, ap1);
					if (score>maxImp) {
						maxImp=score;
						rp=rp1; ap=ap1;
					}
				}
			}
			if (maxImp<=0) break;
			
			// execute change
			executor_.execComposePart(rp, ap);
			nid_part.remove(ap.relTreeRoot_.getId());
		}
		
		// ---------------------------- //
		// update pointer and stat
		// ---------------------------- //
		// garbage-collect existing parts
		Clust.removePartAndUpdateStat(old_nid_part);
		
		// for new parse
		Clust.updatePartStat(nid_part);
	}
	
	void setArgs(String articleId, int sentIdx, Sentence sent, int nodeIdx) {
//		Utils.println("createArgs: "+articleId+" sent="+sentIdx+" node="+nodeIdx);
		String nid=Utils.genTreeNodeId(articleId,sentIdx,nodeIdx);
		TreeNode node=TreeNode.getTreeNode(nid);
		Part np=Part.getPartByRootNodeId(nid);
		Clust ncl=Clust.getClust(np.getClustIdx());
		Set<Pair<String,Integer>> chds=sent.tkn_children_.get(nodeIdx);
		if (chds==null) return;
		Iterator<Pair<String,Integer>> it=chds.iterator();
		while (it.hasNext()) {
			Pair<String,Integer> dep_chd=it.next();
			String dep=dep_chd.getFirst();
			
			int cidx=dep_chd.getSecond();
			String cid=Utils.genTreeNodeId(articleId,sentIdx,cidx);
			Path p=new Path(dep);			
			int argTypeIdx=p.getArgType();
//			Utils.println("---> dep="+dep+" path="+p.toString()+" argTypeIdx="+argTypeIdx+" argType="+ArgType.getArgType(argTypeIdx));
			Part cp=Part.getPartByRootNodeId(cid);
			if (cp==null) Utils.println("ERR: cp=null "+cid+" "+cidx+" "+dep+" "+nodeIdx+" "+sentIdx);

			// TO-DO: fix this, should have unique par
			if (cp.parPart_!=null) {
				Utils.println("ERR: Multiple parents, skip np: cp="+cp.relTreeRoot_.getId()+" par="+cp.parPart_.relTreeRoot_.getId()+" np="+np.relTreeRoot_.getId());
				continue;
			}

			Argument arg=new Argument(node,p,cp);
			int argIdx=np.addArgument(arg);		
			cp.setParent(np, argIdx);
			
			// arg
			Set<Integer> argClustIdxs=ncl.getArgClustIdxs(argTypeIdx);
			int argClustIdx=-1;
			if (argClustIdxs==null) {
				argClustIdx=ncl.createArgClust(argTypeIdx);
			}
			else {
				// TO-DO: multiple ones??
				argClustIdx=argClustIdxs.iterator().next();
			}
			np.setArgClustOnly(argIdx, argClustIdx);
			
			// recursive
			setArgs(articleId, sentIdx, sent, cidx);		
		}
	}
}

