package usp.semantic;

import java.util.*;

import usp.util.*;
import usp.syntax.*;

public class Part {
	static boolean isDebug_=false;
	
	// index
	static Map<String, Part> rootNodeId_part_=new TreeMap<String, Part>();
	public static Part getPartByRootNodeId(String rnId) {return rootNodeId_part_.get(rnId);}
		
	// clust -> part
	static Map<Integer,TreeSet<String>> clustIdx_partRootNodeIds_=new HashMap<Integer,TreeSet<String>>();
	public final static TreeSet<String> getPartRootNodeIds(int clustIdx) {return clustIdx_partRootNodeIds_.get(clustIdx);}
	public final static Map<Integer,TreeSet<String>> getClustPartRootNodeIds() {return clustIdx_partRootNodeIds_;}
	
	// par-chd: clust -> parts
	static Map<Pair<Integer,Integer>,Set<Pair<String,String>>> pairClustIdxs_pairPartRootNodeIds_=new HashMap<Pair<Integer,Integer>,Set<Pair<String,String>>>();
	public final static Set<Pair<String,String>> getPairPartRootNodeIds(int parClustIdx, int chdClustIdx) {return pairClustIdxs_pairPartRootNodeIds_.get(new Pair<Integer,Integer>(parClustIdx,chdClustIdx));}
	public final static Map<Pair<Integer,Integer>,Set<Pair<String,String>>> getPairPartRootNodeIds() {return pairClustIdxs_pairPartRootNodeIds_;}
		
	static Map<Integer,Set<Pair<Integer,Integer>>> clustIdx_pairClustIdxs_=new HashMap<Integer,Set<Pair<Integer,Integer>>>();
	
	//
	TreeNode relTreeRoot_;
	int relTypeIdx_=-1;
	int clustIdx_=-1;
	
	Part parPart_=null;
	int parArgIdx_=-1;
	
	Map<Integer,Argument> args_=new TreeMap<Integer,Argument>();
	Map<Integer,Integer> argIdx_argClustIdx_=new HashMap<Integer,Integer>();
	Map<Integer,Set<Integer>> argClustIdx_argIdxs_=new HashMap<Integer,Set<Integer>>();
	int nxtArgIdx_=0;

	public void setRelType(int newRelTypeIdx) {
		relTypeIdx_=newRelTypeIdx;
		Clust cl=Clust.getClust(clustIdx_);
		cl.onPartSetRelTypeIdx(newRelTypeIdx);		
	}
	public void unsetRelType() {
		int oldRelTypeIdx=relTypeIdx_; 
		Clust cl=Clust.getClust(clustIdx_);
		cl.onPartUnsetRelTypeIdx(oldRelTypeIdx);		
	}
	
	public void destroy() {
		clustIdx_partRootNodeIds_.get(clustIdx_).remove(relTreeRoot_.getId());
		if (clustIdx_partRootNodeIds_.get(clustIdx_).size()==0) clustIdx_partRootNodeIds_.remove(clustIdx_);
		Part.rootNodeId_part_.remove(relTreeRoot_.getId());
	}
	
	public void unsetParent() {
		if (parPart_==null) return;
		
		Pair<Integer,Integer> pcci=new Pair<Integer,Integer>(parPart_.clustIdx_,clustIdx_);

		Set<Pair<Integer,Integer>> x=clustIdx_pairClustIdxs_.get(parPart_.clustIdx_);
		x.remove(pcci);
		x=clustIdx_pairClustIdxs_.get(clustIdx_);
		x.remove(pcci);
			
		Set<Pair<String,String>> pcris=pairClustIdxs_pairPartRootNodeIds_.get(pcci);
		Pair<String,String> pids=new Pair<String,String>(parPart_.relTreeRoot_.getId(),relTreeRoot_.getId());
		pcris.remove(pids);

		// conj
		Argument arg=parPart_.getArgument(parArgIdx_);
		String dep=arg.path_.getDep();
		if (parPart_.clustIdx_!=clustIdx_ && dep.indexOf("conj_")==0) {
			int ci1,ci2;
			if (parPart_.clustIdx_<clustIdx_) {
				ci1=parPart_.clustIdx_; ci2=clustIdx_;
			}
			else {
				ci2=parPart_.clustIdx_; ci1=clustIdx_;
			}
			Pair<Integer,Integer> pci=new Pair<Integer,Integer>(ci1,ci2);
			if (Clust.pairClustIdxs_conjCnt_.get(pci)!=null) {
				if (Clust.pairClustIdxs_conjCnt_.get(pci)==1) Clust.pairClustIdxs_conjCnt_.remove(pci);
				else Clust.pairClustIdxs_conjCnt_.put(pci,Clust.pairClustIdxs_conjCnt_.get(pci)-1);
			}
			 
		}

		parPart_=null; parArgIdx_=-1;
	}

	public void setParent(Part parPart,int parArgIdx) {
		if (isDebug_) Utils.println("setParent: "+relTreeRoot_.getId()+((parPart_!=null)?" oldpar="+parPart_.relTreeRoot_.getId():"")+" npar="+parPart.relTreeRoot_.getId());
		if (isDebug_) Utils.println("\t"+clustIdx_+" "+Clust.getClust(clustIdx_));
		
		// old
		if (parPart_!=null) {
//			Pair<Integer,Integer> pcci=new Pair<Integer,Integer>(parPart_.clustIdx_,clustIdx_);
//
//			Set<Pair<Integer,Integer>> x=clustIdx_pairClustIdxs_.get(parPart_.clustIdx_);
//			x.remove(pcci);
//			x=clustIdx_pairClustIdxs_.get(clustIdx_);
//			x.remove(pcci);
//			
//			Set<Pair<String,String>> pcris=pairClustIdxs_pairPartRootNodeIds_.get(pcci);
//			Pair<String,String> pids=new Pair<String,String>(parPart_.relTreeRoot_.getId(),relTreeRoot_.getId());
//			pcris.remove(pids);
			unsetParent();
		}
		
		// new
		parPart_=parPart;
		parArgIdx_=parArgIdx;
		
		assert parPart.clustIdx_>=0 && clustIdx_>=0;
		Pair<Integer,Integer> pcci=new Pair<Integer,Integer>(parPart_.clustIdx_,clustIdx_);

		Set<Pair<Integer,Integer>> x=clustIdx_pairClustIdxs_.get(parPart_.clustIdx_);
		if (x==null) {
			x=new HashSet<Pair<Integer,Integer>>();
			clustIdx_pairClustIdxs_.put(parPart.clustIdx_, x);			
		}
		x.add(pcci);
		x=clustIdx_pairClustIdxs_.get(clustIdx_);
		if (x==null) {
			x=new HashSet<Pair<Integer,Integer>>();
			clustIdx_pairClustIdxs_.put(clustIdx_, x);			
		}
		x.add(pcci);
		
		Set<Pair<String,String>> pcris=pairClustIdxs_pairPartRootNodeIds_.get(pcci);
		if (pcris==null) {
				pcris=new HashSet<Pair<String,String>>();
				pairClustIdxs_pairPartRootNodeIds_.put(pcci,pcris);
		}
		Pair<String,String> pids=new Pair<String,String>(parPart.relTreeRoot_.getId(),relTreeRoot_.getId());
		pcris.add(pids);
		
		// conj
		if (parPart_!=null) {
			Argument arg=parPart_.getArgument(parArgIdx_);
			String dep=arg.path_.getDep();
			if (parPart_.clustIdx_!=clustIdx_ && dep.indexOf("conj_")==0) {
				int ci1,ci2;
				if (parPart_.clustIdx_<clustIdx_) {
					ci1=parPart_.clustIdx_; ci2=clustIdx_;
				}
				else {
					ci2=parPart_.clustIdx_; ci1=clustIdx_;
				}
				Pair<Integer,Integer> pci=new Pair<Integer,Integer>(ci1,ci2);
				if (Clust.pairClustIdxs_conjCnt_.get(pci)==null) Clust.pairClustIdxs_conjCnt_.put(pci,1);
				else Clust.pairClustIdxs_conjCnt_.put(pci,Clust.pairClustIdxs_conjCnt_.get(pci)+1); 
			}
		}
	}
	public Part getParPart() {return parPart_;}
	public int getParArgIdx() {return parArgIdx_;}
		
	public Part(TreeNode relTreeRoot) {
		relTreeRoot_=relTreeRoot;
		relTypeIdx_=RelType.getRelType(relTreeRoot_);
		rootNodeId_part_.put(relTreeRoot_.getId(), this);
	}

	// called during initialize, no idea of parent/chd etc yet
	public void setClust(int clustIdx) {		
		clustIdx_=clustIdx;
		TreeSet<String> rnIds=clustIdx_partRootNodeIds_.get(clustIdx);
		if (rnIds==null) {
			rnIds=new TreeSet<String>();
			clustIdx_partRootNodeIds_.put(clustIdx, rnIds);
		}
		rnIds.add(relTreeRoot_.getId());
		
		Clust cl=Clust.getClust(clustIdx);
		cl.onPartSetClust(this);
	}
	
	// change clust only; leave args intact -> used in ABSORB
	public void changeClust(int newClustIdx,int newRelTypeIdx) {
		if (isDebug_) Utils.println("ChangeClust part="+relTreeRoot_.getId());
		if (isDebug_) Utils.println("\tnewcl="+newClustIdx+" "+Clust.getClust(newClustIdx));
		int oldClustIdx=clustIdx_;		
		Clust ocl=Clust.getClust(oldClustIdx);
				
		// unset old clust
		clustIdx_=newClustIdx;
		Set<String> rnIds=clustIdx_partRootNodeIds_.get(oldClustIdx);		
		rnIds.remove(relTreeRoot_.getId());
		ocl.onPartUnsetClust(this);		
		
		// set new clust
		relTypeIdx_=newRelTypeIdx;
		setClust(newClustIdx);
		
		// root: not done this in clust.setclust because that might be called when parPart not known yet
		if (parPart_==null) {
//			Utils.println("\tpar=null "+ocl+" "+Clust.clustIdx_rootCnt_.get(oldClustIdx));
			
			if (Clust.clustIdx_rootCnt_.get(newClustIdx)==null) Clust.clustIdx_rootCnt_.put(newClustIdx,1);
			else Clust.clustIdx_rootCnt_.put(newClustIdx,Clust.clustIdx_rootCnt_.get(newClustIdx).intValue()+1);
			Clust.clustIdx_rootCnt_.put(oldClustIdx,Clust.clustIdx_rootCnt_.get(oldClustIdx).intValue()-1);
		}
		else {
			// fix parent part arg
			int paci=parPart_.argIdx_argClustIdx_.get(parArgIdx_);
			Clust pcl=Clust.getClust(parPart_.clustIdx_);
			ArgClust pac=pcl.argClusts_.get(paci);
			
//			Utils.println("cl="+ocl+" pac="+pac+" oci="+oldClustIdx+" nci="+newClustIdx);
//			Utils.println("\tchd="+pac.chdClustIdx_cnt_);
			pac.chdClustIdx_cnt_.put(oldClustIdx, pac.chdClustIdx_cnt_.get(oldClustIdx)-1);
			if (pac.chdClustIdx_cnt_.get(newClustIdx)==null)
				pac.chdClustIdx_cnt_.put(newClustIdx, 1);
			else
				pac.chdClustIdx_cnt_.put(newClustIdx, pac.chdClustIdx_cnt_.get(newClustIdx)+1);
			
			// parArgs
			Pair<Integer,Integer> pa=new Pair<Integer,Integer>(parPart_.clustIdx_,paci);
			Map<Pair<Integer,Integer>,Integer> pa_cnt=Clust.clustIdx_parArgs_.get(oldClustIdx);
			pa_cnt.put(pa,pa_cnt.get(pa)-1);
			pa_cnt=Clust.clustIdx_parArgs_.get(newClustIdx);
			if (pa_cnt==null) {
				pa_cnt=new HashMap<Pair<Integer,Integer>, Integer>();
				Clust.clustIdx_parArgs_.put(newClustIdx, pa_cnt);
			}
			if (pa_cnt.get(pa)==null) pa_cnt.put(pa, 1);
			else pa_cnt.put(pa, pa_cnt.get(pa)+1);
			
			// pairclust->part
			Pair<Integer,Integer> opci=new Pair<Integer,Integer>(parPart_.clustIdx_,oldClustIdx);
			Pair<Integer,Integer> npci=new Pair<Integer,Integer>(parPart_.clustIdx_,newClustIdx);
			Pair<String,String> ptnid=new Pair<String,String>(parPart_.relTreeRoot_.getId(),relTreeRoot_.getId());

			Set<Pair<String,String>> ptnids=pairClustIdxs_pairPartRootNodeIds_.get(opci);
			if (ptnids==null) Utils.println("ERR: ptnid="+ptnid+" opar="+parPart_.clustIdx_+":"+Clust.getClust(parPart_.clustIdx_)+" oldclust="+oldClustIdx+":"+Clust.getClust(oldClustIdx));
			ptnids.remove(ptnid);
			if (ptnids.isEmpty()) {
				// remove opci
				Set<Pair<Integer,Integer>> pcis=clustIdx_pairClustIdxs_.get(oldClustIdx);
				pcis.remove(opci);
				pcis=clustIdx_pairClustIdxs_.get(parPart_.clustIdx_);
				pcis.remove(opci);
			}
			ptnids=pairClustIdxs_pairPartRootNodeIds_.get(npci);
			if (ptnids==null) {
				ptnids=new HashSet<Pair<String,String>>();
				pairClustIdxs_pairPartRootNodeIds_.put(npci, ptnids);
			}
			ptnids.add(ptnid);
			Set<Pair<Integer,Integer>> pcis=clustIdx_pairClustIdxs_.get(parPart_.clustIdx_);
			pcis.add(npci);			
			pcis=clustIdx_pairClustIdxs_.get(newClustIdx);
			if (pcis==null) {
				pcis=new HashSet<Pair<Integer,Integer>>();
				clustIdx_pairClustIdxs_.put(newClustIdx, pcis);
			}
			pcis.add(npci);
		}
	}
	
	// change clust and remap argclust -> used in MC
	public void changeClust(int newClustIdx, Map<Integer,Integer> argClustIdx_newArgClustIdx) {
//		Utils.println("ChangeClust part="+relTreeRoot_.getId());
		int oldClustIdx=clustIdx_;
		Clust ocl=Clust.getClust(oldClustIdx);				

		// handle clust change: no change in relTypeIdx_
//		changeClust(newClustIdx, relTypeIdx_);
		changeClust(newClustIdx,relTypeIdx_);

		// rermove old acs
		Map<Integer,Integer> argIdx_newArgClustIdx=new HashMap<Integer,Integer>();	
		for (Integer ai:args_.keySet()) {
			Argument arg=args_.get(ai);
			int oaci=argIdx_argClustIdx_.get(ai);
			argIdx_argClustIdx_.remove(ai);
			argClustIdx_argIdxs_.get(oaci).remove(ai);
			if (argClustIdx_argIdxs_.get(oaci).isEmpty()) argClustIdx_argIdxs_.remove(oaci);
			argIdx_newArgClustIdx.put(ai, argClustIdx_newArgClustIdx.get(oaci));
			
			ocl.onPartUnsetArg(this, arg, oaci);
		}
		
		// set new acs
		for (Integer ai:args_.keySet()) {			
			int aci=argIdx_newArgClustIdx.get(ai);
			setArgClust(ai, aci);
		}		
	}
	
	
	public int getClustIdx() {return clustIdx_;}
	public TreeNode getRelTreeRoot() {return relTreeRoot_;}
	public int getRelTypeIdx() {return relTypeIdx_;}
	public int addArgument(Argument arg) {		
		int argIdx=nxtArgIdx_++;
		args_.put(argIdx,arg);
		return argIdx;
	}
	public Argument getArgument(int argIdx) {return args_.get(argIdx);}
	public Map<Integer,Argument> getArguments() {return args_;}
	
	// needed when execabs
	public void removeArgument(int argIdx) {
		int oldArgClustIdx=argIdx_argClustIdx_.get(argIdx);			
		Clust cl=Clust.getClust(clustIdx_);
		Argument arg=args_.get(argIdx);
		argIdx_argClustIdx_.remove(argIdx);
		argClustIdx_argIdxs_.get(oldArgClustIdx).remove(argIdx);
		if (argClustIdx_argIdxs_.get(oldArgClustIdx).size()==0)
			argClustIdx_argIdxs_.remove(oldArgClustIdx);
		cl.onPartUnsetArg(this, arg, oldArgClustIdx);
		args_.remove(argIdx);		
	}

	public void unsetArgClust(int argIdx) {
//		if (!argIdx_argClustIdx_.containsKey(argIdx)) return;
		
		int oldArgClustIdx=argIdx_argClustIdx_.get(argIdx);			
 		argIdx_argClustIdx_.remove(argIdx);
		Argument arg=args_.get(argIdx);
		Clust cl=Clust.getClust(clustIdx_);
		argClustIdx_argIdxs_.get(oldArgClustIdx).remove(argIdx);
		if (argClustIdx_argIdxs_.get(oldArgClustIdx).size()==0)
			argClustIdx_argIdxs_.remove(oldArgClustIdx);
		cl.onPartUnsetArg(this, arg, oldArgClustIdx);						
	}

	public void setArgClust(int argIdx, int argClustIdx) {
		int oldArgClustIdx=-1;
		if (argIdx_argClustIdx_.containsKey(argIdx)) {
			oldArgClustIdx=argIdx_argClustIdx_.get(argIdx);			
		}
		if (oldArgClustIdx==argClustIdx) return;
 		argIdx_argClustIdx_.put(argIdx, argClustIdx);
 		
 		Set<Integer> argIdxs=argClustIdx_argIdxs_.get(argClustIdx);
 		if (argIdxs==null) {
 			argIdxs=new HashSet<Integer>();
 			argClustIdx_argIdxs_.put(argClustIdx, argIdxs);
 		}
 		argIdxs.add(argIdx); 		
 		
		Argument arg=args_.get(argIdx);
		Clust cl=Clust.getClust(clustIdx_);
		
		if (oldArgClustIdx<0) {			
			cl.onPartSetArg(this, arg, argClustIdx);
		}
		else {
			argClustIdx_argIdxs_.get(oldArgClustIdx).remove(argIdx);
			if (argClustIdx_argIdxs_.get(oldArgClustIdx).size()==0)
				argClustIdx_argIdxs_.remove(oldArgClustIdx);
			cl.onPartSetArg(this, arg, argClustIdx, oldArgClustIdx);						
		}				
	}

	public int getArgClust(int argIdx) {
		return argIdx_argClustIdx_.get(argIdx);
	}

	// --------------------------------- //
	// no update to clust stat; called in reparse
	// --------------------------------- //
	public void changeClustOnly(int newClustIdx,int newRelTypeIdx) {
		if (isDebug_) Utils.println("ChangeClust part="+relTreeRoot_.getId());
		if (isDebug_) Utils.println("\tnewcl="+newClustIdx+" "+Clust.getClust(newClustIdx));
		int oldClustIdx=clustIdx_;		
		Clust ocl=Clust.getClust(oldClustIdx);
				
		// unset old clust
		clustIdx_=newClustIdx;
		Set<String> rnIds=clustIdx_partRootNodeIds_.get(oldClustIdx);		
		rnIds.remove(relTreeRoot_.getId());
//		ocl.onPartUnsetClust(this);		
		
		// set new clust
		relTypeIdx_=newRelTypeIdx;
		setClustOnly(newClustIdx);
		
		// root: not done this in clust.setclust because that might be called when parPart not known yet
		if (parPart_==null) {
//			Utils.println("\tpar=null "+ocl+" "+Clust.clustIdx_rootCnt_.get(oldClustIdx));
			
			if (Clust.clustIdx_rootCnt_.get(newClustIdx)==null) Clust.clustIdx_rootCnt_.put(newClustIdx,1);
			else Clust.clustIdx_rootCnt_.put(newClustIdx,Clust.clustIdx_rootCnt_.get(newClustIdx).intValue()+1);
			Clust.clustIdx_rootCnt_.put(oldClustIdx,Clust.clustIdx_rootCnt_.get(oldClustIdx).intValue()-1);
		}
		else {
			// fix parent part arg
			int paci=parPart_.argIdx_argClustIdx_.get(parArgIdx_);
			Clust pcl=Clust.getClust(parPart_.clustIdx_);
			ArgClust pac=pcl.argClusts_.get(paci);
			
//			Utils.println("cl="+ocl+" pac="+pac+" oci="+oldClustIdx+" nci="+newClustIdx);
//			Utils.println("\tchd="+pac.chdClustIdx_cnt_);
			pac.chdClustIdx_cnt_.put(oldClustIdx, pac.chdClustIdx_cnt_.get(oldClustIdx)-1);
			if (pac.chdClustIdx_cnt_.get(newClustIdx)==null)
				pac.chdClustIdx_cnt_.put(newClustIdx, 1);
			else
				pac.chdClustIdx_cnt_.put(newClustIdx, pac.chdClustIdx_cnt_.get(newClustIdx)+1);
			
			// parArgs
			Pair<Integer,Integer> pa=new Pair<Integer,Integer>(parPart_.clustIdx_,paci);
			Map<Pair<Integer,Integer>,Integer> pa_cnt=Clust.clustIdx_parArgs_.get(oldClustIdx);
			pa_cnt.put(pa,pa_cnt.get(pa)-1);
			pa_cnt=Clust.clustIdx_parArgs_.get(newClustIdx);
			if (pa_cnt==null) {
				pa_cnt=new HashMap<Pair<Integer,Integer>, Integer>();
				Clust.clustIdx_parArgs_.put(newClustIdx, pa_cnt);
			}
			if (pa_cnt.get(pa)==null) pa_cnt.put(pa, 1);
			else pa_cnt.put(pa, pa_cnt.get(pa)+1);
			
			// pairclust->part
			Pair<Integer,Integer> opci=new Pair<Integer,Integer>(parPart_.clustIdx_,oldClustIdx);
			Pair<Integer,Integer> npci=new Pair<Integer,Integer>(parPart_.clustIdx_,newClustIdx);
			Pair<String,String> ptnid=new Pair<String,String>(parPart_.relTreeRoot_.getId(),relTreeRoot_.getId());

			Set<Pair<String,String>> ptnids=pairClustIdxs_pairPartRootNodeIds_.get(opci);
			if (ptnids==null) Utils.println("ERR: ptnid="+ptnid+" opar="+parPart_.clustIdx_+":"+Clust.getClust(parPart_.clustIdx_)+" oldclust="+oldClustIdx+":"+Clust.getClust(oldClustIdx));
			ptnids.remove(ptnid);
			if (ptnids.isEmpty()) {
				// remove opci
				Set<Pair<Integer,Integer>> pcis=clustIdx_pairClustIdxs_.get(oldClustIdx);
				pcis.remove(opci);
				pcis=clustIdx_pairClustIdxs_.get(parPart_.clustIdx_);
				pcis.remove(opci);
			}
			ptnids=pairClustIdxs_pairPartRootNodeIds_.get(npci);
			if (ptnids==null) {
				ptnids=new HashSet<Pair<String,String>>();
				pairClustIdxs_pairPartRootNodeIds_.put(npci, ptnids);
			}
			ptnids.add(ptnid);
			Set<Pair<Integer,Integer>> pcis=clustIdx_pairClustIdxs_.get(parPart_.clustIdx_);
			pcis.add(npci);			
			pcis=clustIdx_pairClustIdxs_.get(newClustIdx);
			if (pcis==null) {
				pcis=new HashSet<Pair<Integer,Integer>>();
				clustIdx_pairClustIdxs_.put(newClustIdx, pcis);
			}
			pcis.add(npci);
		}
	}
	public void changeClustOnly(int newClustIdx, Map<Integer,Integer> argClustIdx_newArgClustIdx) {
//		Utils.println("ChangeClust part="+relTreeRoot_.getId());
		int oldClustIdx=clustIdx_;
		Clust ocl=Clust.getClust(oldClustIdx);				

		// handle clust change: no change in relTypeIdx_
//		changeClust(newClustIdx, relTypeIdx_);
		changeClustOnly(newClustIdx,relTypeIdx_);

		// remove old acs
		Map<Integer,Integer> argIdx_newArgClustIdx=new HashMap<Integer,Integer>();	
		for (Integer ai:args_.keySet()) {
			Argument arg=args_.get(ai);
			int oaci=argIdx_argClustIdx_.get(ai);
			argIdx_argClustIdx_.remove(ai);
			argClustIdx_argIdxs_.get(oaci).remove(ai);
			if (argClustIdx_argIdxs_.get(oaci).isEmpty()) argClustIdx_argIdxs_.remove(oaci);
			argIdx_newArgClustIdx.put(ai, argClustIdx_newArgClustIdx.get(oaci));
			
			//ocl.onPartUnsetArg(this, arg, oaci);
		}
		
		// set new acs
		for (Integer ai:args_.keySet()) {			
			int aci=argIdx_newArgClustIdx.get(ai);
			setArgClustOnly(ai, aci);
		}		
	}

	public void setArgClustOnly(int argIdx, int argClustIdx) {
		int oldArgClustIdx=-1;
		if (argIdx_argClustIdx_.containsKey(argIdx)) {
			oldArgClustIdx=argIdx_argClustIdx_.get(argIdx);			
		}
		if (oldArgClustIdx==argClustIdx) return;
 		argIdx_argClustIdx_.put(argIdx, argClustIdx);
 		
 		Set<Integer> argIdxs=argClustIdx_argIdxs_.get(argClustIdx);
 		if (argIdxs==null) {
 			argIdxs=new HashSet<Integer>();
 			argClustIdx_argIdxs_.put(argClustIdx, argIdxs);
 		}
 		argIdxs.add(argIdx); 		
 		
		Argument arg=args_.get(argIdx);
		//Clust cl=Clust.getClust(clustIdx_);
		
		if (oldArgClustIdx<0) {			
			//cl.onPartSetArg(this, arg, argClustIdx);
		}
		else {
			argClustIdx_argIdxs_.get(oldArgClustIdx).remove(argIdx);
			if (argClustIdx_argIdxs_.get(oldArgClustIdx).size()==0)
				argClustIdx_argIdxs_.remove(oldArgClustIdx);
			//cl.onPartSetArg(this, arg, argClustIdx, oldArgClustIdx);						
		}				
	}
	
	public void removeArgumentOnly(int argIdx) {
		int oldArgClustIdx=argIdx_argClustIdx_.get(argIdx);			
		Clust cl=Clust.getClust(clustIdx_);
		Argument arg=args_.get(argIdx);
		argIdx_argClustIdx_.remove(argIdx);
		argClustIdx_argIdxs_.get(oldArgClustIdx).remove(argIdx);
		if (argClustIdx_argIdxs_.get(oldArgClustIdx).size()==0)
			argClustIdx_argIdxs_.remove(oldArgClustIdx);
//		cl.onPartUnsetArg(this, arg, oldArgClustIdx);
		args_.remove(argIdx);		
	}

	public void unsetArgClustOnly(int argIdx) {
//		if (!argIdx_argClustIdx_.containsKey(argIdx)) return;
		
		int oldArgClustIdx=argIdx_argClustIdx_.get(argIdx);			
 		argIdx_argClustIdx_.remove(argIdx);
		Argument arg=args_.get(argIdx);
		Clust cl=Clust.getClust(clustIdx_);
		argClustIdx_argIdxs_.get(oldArgClustIdx).remove(argIdx);
		if (argClustIdx_argIdxs_.get(oldArgClustIdx).size()==0)
			argClustIdx_argIdxs_.remove(oldArgClustIdx);
//		cl.onPartUnsetArg(this, arg, oldArgClustIdx);						
	}
	public void setClustOnly(int clustIdx) {		
		clustIdx_=clustIdx;
		TreeSet<String> rnIds=clustIdx_partRootNodeIds_.get(clustIdx);
		if (rnIds==null) {
			rnIds=new TreeSet<String>();
			clustIdx_partRootNodeIds_.put(clustIdx, rnIds);
		}
		rnIds.add(relTreeRoot_.getId());
		
//		Clust cl=Clust.getClust(clustIdx);
//		cl.onPartSetClust(this);
	}
	
}

