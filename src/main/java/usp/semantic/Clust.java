package usp.semantic;

import java.util.*;

import usp.syntax.ArgType;
import usp.syntax.RelType;
import usp.util.*;

public class Clust {
	boolean isDebug_=false;
	
	static int whereasClustIdx_=-1; 
	
	static Map<Pair<Integer,Integer>,Integer> pairClustIdxs_conjCnt_=new HashMap<Pair<Integer,Integer>,Integer>();
		// c1(<)c2 -> num of conjunctions
	
	//
	static Map<Integer,Clust> clusts_=new HashMap<Integer,Clust>();	
	public static Clust getClust(int idx) {return clusts_.get(idx);}
	static int nxtClustIdx_=1;

	// for search
	static Map<Integer,Set<Integer>> relTypeIdx_clustIdx_=new HashMap<Integer,Set<Integer>>();
	public static Set<Integer> getClustsWithRelType(int relTypeIdx) {
		return relTypeIdx_clustIdx_.get(relTypeIdx);
	}
	
	// for score: par-arg cnts
	static Map<Integer,Map<Pair<Integer,Integer>,Integer>> clustIdx_parArgs_=new HashMap<Integer,Map<Pair<Integer,Integer>,Integer>>();
		// clust -> par<clustIdx, argClustIdx> -> cnt
	static Map<Integer,Integer> clustIdx_rootCnt_=new HashMap<Integer,Integer>();
		// clust -> cnt for it to be root
	static int ttlRootCnt_=0;
	
	// NEW: ArgComb - chd clust comb (pair, ...)
	static Map<String,Integer> argComb_cnt_=new HashMap<String,Integer>();
	static Map<Integer,Set<String>> clustIdx_argCombs_=new HashMap<Integer,Set<String>>();
		// idx -> comb that contains the idx

	// VAR
	private Clust() {}
	int clustIdx_=-1;
	
	char type_;	// content 'C' or non 'N'
	boolean isStop_=false;	// V:be, N:$, N:%
		
	int ttlCnt_=0;	
	Map<Integer,Integer> relTypeIdx_cnt_=new HashMap<Integer,Integer>();
	Map<Integer,Set<Integer>> argTypeIdx_argClustIdxs_=new HashMap<Integer,Set<Integer>>();
	
	// use map since some could get merge
	Map<Integer,ArgClust> argClusts_=new HashMap<Integer,ArgClust>();
	int nxtArgClustIdx_=0;
	
	public char getType() {return type_;}
	public boolean isStop() {return isStop_;}
	
	public static int createClust(int relTypeIdx) {		 
		Clust cl=new Clust();
		cl.clustIdx_=nxtClustIdx_++;
		
		RelType rt=RelType.getRelType(relTypeIdx);
		cl.type_=rt.getType();
		String rts=rt.toString();
		if (rts.equals("(V:be)") || rts.equals("(N:%)")) cl.isStop_=true; // genia
		if (rts.equals("(V:say)") || rts.equals("($:$)")) cl.isStop_=true;	// wsj
		
		if (whereasClustIdx_==-1 && rts.equals("(IN:whereas)")) whereasClustIdx_=cl.clustIdx_;	// wsj
		
		clusts_.put(cl.clustIdx_,cl);
		Set<Integer> cls=relTypeIdx_clustIdx_.get(relTypeIdx);
		if (cls==null) {
			cls=new HashSet<Integer>();
			relTypeIdx_clustIdx_.put(relTypeIdx, cls);
		}
		cls.add(cl.clustIdx_);
		return cl.clustIdx_;
	}
	
	public static void removeClust(Clust clust) {
		clusts_.remove(clust.clustIdx_);
	}
	
	//
	public void incRootCnt() {
		ttlRootCnt_++;
		if (clustIdx_rootCnt_.get(clustIdx_)==null) clustIdx_rootCnt_.put(clustIdx_,1);
		else clustIdx_rootCnt_.put(clustIdx_,clustIdx_rootCnt_.get(clustIdx_).intValue()+1);
	}
	public void decRootCnt() {
		ttlRootCnt_--;
		clustIdx_rootCnt_.put(clustIdx_,clustIdx_rootCnt_.get(clustIdx_).intValue()-1);
		if (clustIdx_rootCnt_.get(clustIdx_)==0) clustIdx_rootCnt_.remove(clustIdx_);
		
	}
	
	// part update
	public void onPartUnsetRelTypeIdx(int oldRelTypeIdx) {		
		relTypeIdx_cnt_.put(oldRelTypeIdx, relTypeIdx_cnt_.get(oldRelTypeIdx)-1);
	}
	public void onPartSetRelTypeIdx(int newRelTypeIdx) {		
		if (relTypeIdx_cnt_.get(newRelTypeIdx)==null) relTypeIdx_cnt_.put(newRelTypeIdx, 1);
		else relTypeIdx_cnt_.put(newRelTypeIdx, relTypeIdx_cnt_.get(newRelTypeIdx)+1);
	}

	public void onPartSetClust(Part part) {
		ttlCnt_++;
		int ridx=part.getRelTypeIdx();
		if (relTypeIdx_cnt_.get(ridx)==null) relTypeIdx_cnt_.put(ridx, 1);
		else relTypeIdx_cnt_.put(ridx, relTypeIdx_cnt_.get(ridx)+1);
	}
	public void onPartUnsetClust(Part part) {
		ttlCnt_--;
		int ridx=part.getRelTypeIdx();
		relTypeIdx_cnt_.put(ridx, relTypeIdx_cnt_.get(ridx)-1);
	}
	
	// arg
	public int createArgClust(int argTypeIdx) {
		assert argTypeIdx_argClustIdxs_.get(argTypeIdx)==null;
		int argClustIdx=nxtArgClustIdx_++;
		ArgClust ac=new ArgClust();
		argClusts_.put(argClustIdx, ac);
		Set<Integer> acs=new HashSet<Integer>();
		argTypeIdx_argClustIdxs_.put(argTypeIdx, acs);
		acs.add(argClustIdx);
		
		if (isDebug_) Utils.println("createargcl: "+clustIdx_+" "+toString()+" argcl="+argClustIdx+" "+argClusts_.get(argClustIdx)+" ati="+argTypeIdx+":"+ArgType.getArgType(argTypeIdx));
		
		return argClustIdx;
	}
	
	public void removeArgClust(int argClustIdx) {
		argClusts_.remove(argClustIdx);
		Set<Integer> toDel=new HashSet<Integer>();
		for (Integer ati:argTypeIdx_argClustIdxs_.keySet()) {
			argTypeIdx_argClustIdxs_.get(ati).remove(argClustIdx);
			if (argTypeIdx_argClustIdxs_.get(ati).size()==0) {
				toDel.add(ati);
			}
		}
		for (Integer ati:toDel) argTypeIdx_argClustIdxs_.remove(ati);
	}
	
	public static void addArgComb(int clustIdx, int chdClustIdx1, int chdClustIdx2) {
		String ac=genArgCombStr(clustIdx,chdClustIdx1,chdClustIdx2);
		
		Set<String> acs=clustIdx_argCombs_.get(clustIdx);
		if (acs==null) {
			acs=new HashSet<String>();
			clustIdx_argCombs_.put(clustIdx, acs);
		}
		acs.add(ac);
		acs=clustIdx_argCombs_.get(chdClustIdx1);
		if (acs==null) {
			acs=new HashSet<String>();
			clustIdx_argCombs_.put(chdClustIdx1, acs);
		}
		acs.add(ac);
		acs=clustIdx_argCombs_.get(chdClustIdx2);
		if (acs==null) {
			acs=new HashSet<String>();
			clustIdx_argCombs_.put(chdClustIdx2, acs);
		}
		acs.add(ac);
		
		if (argComb_cnt_.get(ac)==null) argComb_cnt_.put(ac, 1);
		else argComb_cnt_.put(ac, argComb_cnt_.get(ac)+1);
	}
	
	// assume chd is sorted
	public static void addArgComb(int clustIdx, int[] chdClustIdxs) {
		String ac=genArgCombStr(clustIdx,chdClustIdxs);
		
		Set<String> acs=clustIdx_argCombs_.get(clustIdx);
		if (acs==null) {
			acs=new HashSet<String>();
			clustIdx_argCombs_.put(clustIdx, acs);
		}
		acs.add(ac);
		for (int i=0; i<chdClustIdxs.length; i++) {
			acs=clustIdx_argCombs_.get(chdClustIdxs[i]);
			if (acs==null) {
				acs=new HashSet<String>();
				clustIdx_argCombs_.put(chdClustIdxs[i], acs);
			}
			acs.add(ac);
		}
		if (argComb_cnt_.get(ac)==null) argComb_cnt_.put(ac, 1);
		else argComb_cnt_.put(ac, argComb_cnt_.get(ac)+1);
	}
	
	static String genArgCombStr(int clustIdx, int clustIdx1, int clustIdx2) {
		return clustIdx+":"+clustIdx1+":"+clustIdx2;
		
//		if (clustIdx1<=clustIdx2) return clustIdx+":"+clustIdx1+":"+clustIdx2;
//		else return clustIdx+":"+clustIdx2+":"+clustIdx1;
	}
	static String genArgCombStr(int clustIdx, int[] clustIdxs) {
		String s=""+clustIdx;
		for (int i=0; i<clustIdxs.length; i++) s+=":"+clustIdxs[i];
		return s;
	}
	
	public Set<Integer> getArgClustIdxs(int argTypeIdx) {return argTypeIdx_argClustIdxs_.get(argTypeIdx);}

	// argclust: initial set
	public void onPartSetArg(Part part, Argument arg, int argClustIdx) {
		onPartSetArg(part,arg,argClustIdx,-1);
	}
	public void onPartSetArg(Part part, Argument arg, int argClustIdx, int oldArgClustIdx) {
		// new
		int argTypeIdx=arg.path_.getArgType(); 
		int chdClustIdx=arg.argPart_.getClustIdx();
		ArgClust ac=argClusts_.get(argClustIdx);
		if (ac==null) {
			Utils.println("part="+part.relTreeRoot_.getId()+" arg="+arg.path_+" chd="+Clust.getClust(chdClustIdx));
			Utils.println("cl="+toString()+" "+clustIdx_+" argcl="+argClustIdx);
		}
		if (ac.argTypeIdx_cnt_.get(argTypeIdx)==null) ac.argTypeIdx_cnt_.put(argTypeIdx,1);
		else ac.argTypeIdx_cnt_.put(argTypeIdx,ac.argTypeIdx_cnt_.get(argTypeIdx).intValue()+1);
		if (ac.chdClustIdx_cnt_.get(chdClustIdx)==null) ac.chdClustIdx_cnt_.put(chdClustIdx,1);
		else ac.chdClustIdx_cnt_.put(chdClustIdx,ac.chdClustIdx_cnt_.get(chdClustIdx)+1);
		ac.ttlArgCnt_++;		
		
		Map<Pair<Integer,Integer>,Integer> parArgs=clustIdx_parArgs_.get(chdClustIdx);
		if (parArgs==null) {
			parArgs=new HashMap<Pair<Integer,Integer>,Integer>();
			clustIdx_parArgs_.put(chdClustIdx,parArgs);
		}
		Pair<Integer,Integer> cl_ac=new Pair<Integer,Integer>(clustIdx_,argClustIdx);
		if (parArgs.get(cl_ac)==null) parArgs.put(cl_ac, 1);
		else parArgs.put(cl_ac, parArgs.get(cl_ac)+1);
		
		int newArgNum=part.argClustIdx_argIdxs_.get(argClustIdx).size();		
		if (ac.argNum_cnt_.get(newArgNum)==null) ac.argNum_cnt_.put(newArgNum, 1); 
		else ac.argNum_cnt_.put(newArgNum, ac.argNum_cnt_.get(newArgNum)+1);		
		if (newArgNum>1) {
			if (ac.argNum_cnt_.get(newArgNum-1)==1) ac.argNum_cnt_.remove(newArgNum-1); 
			else ac.argNum_cnt_.put(newArgNum-1, ac.argNum_cnt_.get(newArgNum-1)-1);
		}
		
		ac.partRootTreeNodeIds_.add(part.relTreeRoot_.getId());
		
		// old
		if (oldArgClustIdx<0) return;		
		onPartUnsetArg(part,arg,oldArgClustIdx);
		
//		ArgClust oac=argClusts_.get(oldArgClustIdx);
//		oac.ttlArgCnt_--;		
//		oac.argTypeIdx_cnt_.put(argTypeIdx,oac.argTypeIdx_cnt_.get(argTypeIdx)-1);
//		oac.chdClustIdx_cnt_.put(chdClustIdx,oac.chdClustIdx_cnt_.get(chdClustIdx)-1);
//		
//		cl_ac=new Pair<Integer,Integer>(clustIdx_,oldArgClustIdx);
//		if (parArgs.get(cl_ac)==1) parArgs.remove(cl_ac);
//		else parArgs.put(cl_ac, parArgs.get(cl_ac)-1);
//
//		int oldArgNum=0;
//		if (part.argClustIdx_argIdxs_.get(oldArgClustIdx)!=null) oldArgNum=part.argClustIdx_argIdxs_.get(oldArgClustIdx).size();	
//		if (oldArgNum>0) {
//			if (oac.argNum_cnt_.get(oldArgNum)==null) oac.argNum_cnt_.put(oldArgNum, 1); 
//			else oac.argNum_cnt_.put(oldArgNum, oac.argNum_cnt_.get(oldArgNum)+1);
//		}
//		if (oac.argNum_cnt_.get(oldArgNum+1)==1) {
//			oac.argNum_cnt_.remove(oldArgNum+1); 
//		}
//		else oac.argNum_cnt_.put(oldArgNum+1, oac.argNum_cnt_.get(oldArgNum+1)-1);		
//		oac.partRootTreeNodeIds_.remove(part.relTreeRoot_.getId());		
	}
	public void onPartUnsetArg(Part part, Argument arg, int argClustIdx) {
		if (isDebug_) Utils.println("part="+Clust.getClust(part.clustIdx_)+" arg="+arg.path_+" "+Clust.getClust(arg.argPart_.clustIdx_));
		
		int argTypeIdx=arg.path_.getArgType(); 
		int chdClustIdx=arg.argPart_.getClustIdx();
		ArgClust ac=argClusts_.get(argClustIdx);
		if (ac.argTypeIdx_cnt_.get(argTypeIdx)==1)
			ac.argTypeIdx_cnt_.remove(argTypeIdx);
		else 
			ac.argTypeIdx_cnt_.put(argTypeIdx,ac.argTypeIdx_cnt_.get(argTypeIdx).intValue()-1);
		if (ac.chdClustIdx_cnt_.get(chdClustIdx)==1)
			ac.chdClustIdx_cnt_.remove(chdClustIdx);
		else 
			ac.chdClustIdx_cnt_.put(chdClustIdx,ac.chdClustIdx_cnt_.get(chdClustIdx)-1);
		ac.ttlArgCnt_--;		
		
		Map<Pair<Integer,Integer>,Integer> parArgs=clustIdx_parArgs_.get(chdClustIdx);		
		Pair<Integer,Integer> cl_ac=new Pair<Integer,Integer>(clustIdx_,argClustIdx);
		if (parArgs==null) Utils.println("ERR: parArgs==null");
		if (parArgs.get(cl_ac)==null) Utils.println("ERR: cl_al==null");
		if (parArgs.get(cl_ac)==1) parArgs.remove(cl_ac);
		else parArgs.put(cl_ac, parArgs.get(cl_ac)-1);
		if (parArgs.size()==0) clustIdx_parArgs_.remove(chdClustIdx);

		ac.partRootTreeNodeIds_.remove(part.relTreeRoot_.getId());
		
		if (ac.ttlArgCnt_==0) {
			removeArgClust(argClustIdx);
			assert part.argClustIdx_argIdxs_.get(argClustIdx)==null;
		}
		else {
			int oldArgNum=0;
			if (part.argClustIdx_argIdxs_.get(argClustIdx)!=null) oldArgNum=part.argClustIdx_argIdxs_.get(argClustIdx).size();	
			if (oldArgNum>0) {
				if (ac.argNum_cnt_.get(oldArgNum)==null) ac.argNum_cnt_.put(oldArgNum, 1); 
				else ac.argNum_cnt_.put(oldArgNum, ac.argNum_cnt_.get(oldArgNum)+1);
			}
			if (ac.argNum_cnt_.get(oldArgNum+1)==1) {
				ac.argNum_cnt_.remove(oldArgNum+1); 
			}
			else ac.argNum_cnt_.put(oldArgNum+1, ac.argNum_cnt_.get(oldArgNum+1)-1);
		}		
	}

	// --- used by reparse
	// use ordinary methods to update stat
	public static void removePartAndUpdateStat(Map<String,Part> nid_part) {
		for (String nid: nid_part.keySet()) {
			Part p=nid_part.get(nid);
			Clust cl=Clust.getClust(p.clustIdx_);
			if (p.parPart_==null) cl.decRootCnt();
		}
		for (String nid: nid_part.keySet()) {
			Part p=nid_part.get(nid);		
			for (Integer ai: p.args_.keySet()) {
				p.removeArgument(ai);
				Part cp=p.args_.get(ai).argPart_;
				cp.unsetParent();
			}			
			p.unsetRelType();
		}
		
		// garbage collect
		for (String nid: nid_part.keySet()) {
			Part p=nid_part.get(nid);
			Part.clustIdx_partRootNodeIds_.get(p.clustIdx_).remove(p.relTreeRoot_.getId());
			if (Part.clustIdx_partRootNodeIds_.get(p.clustIdx_).size()==0) Part.clustIdx_partRootNodeIds_.remove(p.clustIdx_);
		}
		nid_part.clear();
	}
	
	// suffice to call clust/argclust and update their stat only
	public static void updatePartStat(Map<String,Part> nid_part) {				
		for (String nid: nid_part.keySet()) {
			Part p=nid_part.get(nid);
			
			// clust
			Clust cl=Clust.getClust(p.clustIdx_);
			cl.onPartSetClust(p);

			if (p.parPart_==null) cl.incRootCnt();				

			// arg
			for (Integer ai: p.args_.keySet()) {
				Argument arg=p.getArgument(ai);
				int aci=p.argIdx_argClustIdx_.get(ai);
				cl.onPartSetArg(p, arg, aci);
			}
		}
	}
	
	public String toString() {
		String s="";
		Iterator<Integer> iit=relTypeIdx_cnt_.keySet().iterator();
		while (iit.hasNext()) {
			int rti=iit.next();
			RelType rt=RelType.getRelType(rti);
			if (s.length()>0) s+=",\t";
			s+=rt.toString()+":"+relTypeIdx_cnt_.get(rti);
		}
		s='['+s+']';
		return s;
	}
}

class ArgClust {
	// argtype + chdclust
	Map<Integer,Integer> argTypeIdx_cnt_=new HashMap<Integer,Integer>();
	Map<Integer,Integer> chdClustIdx_cnt_=new HashMap<Integer,Integer>();
	int ttlArgCnt_=0;	// cnt of presences
	
	// number
	Map<Integer,Integer> argNum_cnt_=new HashMap<Integer,Integer>();	
		// num of ac args -> cnt of instances
		// zero given by absent
	TreeSet<String> partRootTreeNodeIds_=new TreeSet<String>();	// part pts
	
	public String toString() {
		String s="";
		Iterator<Integer> itx=argTypeIdx_cnt_.keySet().iterator();
		while (itx.hasNext()) {
			int ati=itx.next();
			int c=argTypeIdx_cnt_.get(ati);
			if (s.length()>0) s+=" ";
			s+=ArgType.getArgType(ati)+":"+c;
		}
		return s;
	}
}

