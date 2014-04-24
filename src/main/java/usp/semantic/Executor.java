package usp.semantic;

import java.util.*;

import usp.syntax.*;
import usp.util.*;

public class Executor {
	Parse parse_;
	
	public Executor(Parse parse) {
		parse_=parse;
	}
	
	public int executeOp(SearchOp op) {
		int nci=-1;
		if (op.op_==SearchOp.OP_MERGE_CLUST_) nci=execMC(op);
		else if (op.op_==SearchOp.OP_COMPOSE_) nci=execCompose(op);
		return nci;
	}
	
	//
	int execMC(SearchOp op) {
		int clustIdx1=op.clustIdx1_,clustIdx2=op.clustIdx2_;
		Scorer scorer=parse_.scorer_;
		int nci=clustIdx1;
		
		/*
		 * fix parts, clust
		 * update agenda, filtered op scores
		 * remove from agenda
		 * 
		 */
		
		boolean isMyDebug=false;
		
		// 
		Clust cl1=Clust.getClust(clustIdx1), cl2=Clust.getClust(clustIdx2);
		
		// TO-DO:
		if (cl1==null || cl2==null) {
			Utils.println("ERR: EXECMC clust=null "+cl1+" "+cl2);
			return -1;
		}
		
		Clust clx1=cl1, clx2=cl2;
		int cix1=clustIdx1, cix2=clustIdx2;
		if (cl1.argClusts_.size()<cl2.argClusts_.size()) {
			clx1=cl2; clx2=cl1;
			cix1=clustIdx2; cix2=clustIdx1;
			nci=cix1;
		}
		
		Utils.println("\tMerge: "+clx1+" "+clx2);
		
		// --- find optimal arg mapping
		Map<Integer,Integer> aci2_aci1=new HashMap<Integer,Integer>();
		scorer.scoreMCForAlign(clx1, clx2, aci2_aci1);

		// --- create new argclusts		
		for (Integer aci2: clx2.argClusts_.keySet()) {
			if (!aci2_aci1.containsKey(aci2)) {
				ArgClust ac2=clx2.argClusts_.get(aci2);
				for (Integer ati: ac2.argTypeIdx_cnt_.keySet()) {
					int aci1=clx1.createArgClust(ati);
					aci2_aci1.put(aci2, aci1);
					break;
				}
			}
		}
		
		Set<String> pids2=new HashSet<String>();
		pids2.addAll(Part.getPartRootNodeIds(cix2));
		
		// --- exec for each part; log affected parties
		for (String rnId: pids2) {
			/* --- do this more efficiently by only tackling affected parts
			// call reparse
			Pair<String,Integer> aid_si=Utils.decodeTreeNodeId(rnId);
			parse_.reparse(aid_si.getFirst(), aid_si.getSecond());		
			 */
			
			Part pt=Part.getPartByRootNodeId(rnId);
			for (Integer argIdx:pt.getArguments().keySet()) {
				Argument arg=pt.getArgument(argIdx);
				arg.argPart_.unsetParent();
			}
			pt.changeClust(cix1, aci2_aci1);
			for (Integer argIdx:pt.getArguments().keySet()) {
				Argument arg=pt.getArgument(argIdx);
				arg.argPart_.setParent(pt, argIdx);
			}
		}		
		Clust.removeClust(clx2);
		
		return nci;
	}

	
	//
	int execCompose(SearchOp op) {		
		int parClustIdx=op.parClustIdx_, chdClustIdx=op.chdClustIdx_;
		
		int nci=-1;
		boolean isMyDebug=false;
		
		// 
		Clust pcl=Clust.getClust(parClustIdx), ccl=Clust.getClust(chdClustIdx);

		// TO-DO:
		if (pcl==null || ccl==null) {
			Utils.println("ERR: EXECAbs clust=null "+pcl+" "+ccl);
			return -1;
		}		
		
		Clust ncl=null;
		Pair<Integer,Integer> pci=new Pair<Integer,Integer>(parClustIdx,chdClustIdx);	

		// update parts/idx: assuming part.changeclust etc only update idx for par/self
		Set<Pair<String,String>> prnids=new HashSet<Pair<String,String>>();
		prnids.addAll(Part.pairClustIdxs_pairPartRootNodeIds_.get(pci));
		for (Pair<String,String> rnids: prnids) {
			// gen new reltype, get new clust
			String pid=rnids.getFirst();
			String cid=rnids.getSecond();
			
			/* --- do this more efficiently by only tackling affected parts
			// call reparse
			Pair<String,Integer> aid_si=Utils.decodeTreeNodeId(pid);
			parse_.reparse(aid_si.getFirst(), aid_si.getSecond());		
			 */
			
			Part pp=Part.getPartByRootNodeId(pid);
			Part cp=Part.getPartByRootNodeId(cid);
			int pai=cp.parArgIdx_;
			// if (pai==null) continue;
			
			Argument pcarg=pp.getArguments().get(pai);
			String dep=pcarg.path_.getDep();
			TreeNode ptn=pp.relTreeRoot_;
			TreeNode ctn=cp.relTreeRoot_;
			int orti=pp.relTypeIdx_;
			ptn.addChild(dep, ctn);
			int nrti=RelType.getRelType(ptn);			
			
			if (ncl==null) {
				if (Clust.getClustsWithRelType(nrti)==null) {
					ncl=Clust.getClust(Clust.createClust(nrti));
				}
				else if (Clust.getClustsWithRelType(nrti).size()>1) { 
					Utils.println("ERR: multiple clusts for same reltype "+pcl+"-"+ccl);
					System.exit(-1);
				}
				else {
					ncl=Clust.getClust(Clust.getClustsWithRelType(nrti).iterator().next());
				}
				nci=ncl.clustIdx_;
				Utils.println("new clust="+nci+":"+ncl);
			}

			// first unsetArg, then changeclust; need old clust for remove/unset old args
			pp.removeArgument(pai);
			
			if (pp.clustIdx_!=nci) {
				for (Integer argIdx:pp.getArguments().keySet()) {				
					pp.unsetArgClust(argIdx);
					Argument arg=pp.getArgument(argIdx);
					arg.argPart_.unsetParent();
				}
				pp.changeClust(nci,nrti);
				
				// set new args
				for (Integer argIdx:pp.getArguments().keySet()) {				
					Argument arg=pp.getArgument(argIdx);
					int ati=arg.path_.getArgType();
					int aci=-1;
					if (ncl.argTypeIdx_argClustIdxs_.get(ati)==null || ncl.argTypeIdx_argClustIdxs_.get(ati).size()==0) {
						aci=ncl.createArgClust(ati);
					}
					else aci=ncl.argTypeIdx_argClustIdxs_.get(ati).iterator().next();
					arg.argPart_.setParent(pp, argIdx);
					pp.setArgClust(argIdx,aci);			
				}
			}
			else {
				pp.unsetRelType();
				pp.setRelType(nrti);
			}
			pp.setRelType(nrti);
			for (Integer argIdx:cp.getArguments().keySet()) {
				Argument arg=cp.getArgument(argIdx);				
				int ati=arg.path_.getArgType();
				int aci=-1;
				if (ncl.argTypeIdx_argClustIdxs_.get(ati)==null || ncl.argTypeIdx_argClustIdxs_.get(ati).size()==0) {
					aci=ncl.createArgClust(ati);
				}
				else aci=ncl.argTypeIdx_argClustIdxs_.get(ati).iterator().next();
				
				// can't call clust.unset; assume that part.unset is done
				cp.unsetArgClust(argIdx);
				
				int newArgIdx=pp.addArgument(arg);
				pp.setArgClust(newArgIdx, aci);
				Part ccp=arg.argPart_;
				ccp.setParent(pp, newArgIdx);				
			}
			cp.destroy();			
		} // part-pair
		
		//
		Part.clustIdx_pairClustIdxs_.get(parClustIdx).remove(pci);
		Part.clustIdx_pairClustIdxs_.get(chdClustIdx).remove(pci);
		Part.pairClustIdxs_pairPartRootNodeIds_.remove(pci);
		
		return nci;
	}

	void execComposePart(Part pp, Part cp) {		
		int parClustIdx=pp.clustIdx_, chdClustIdx=cp.clustIdx_;
		boolean isMyDebug=false;
		
		// 
		Clust pcl=Clust.getClust(parClustIdx), ccl=Clust.getClust(chdClustIdx);
		int pai=cp.parArgIdx_;
		Argument pcarg=pp.getArguments().get(pai);
		String dep=pcarg.path_.getDep();
		TreeNode ptn=pp.relTreeRoot_;
		TreeNode ctn=cp.relTreeRoot_;
		int orti=pp.relTypeIdx_;
		ptn.addChild(dep, ctn);
		int nrti=RelType.getRelType(ptn);			
		
		Clust ncl=Clust.getClust(Clust.getClustsWithRelType(nrti).iterator().next());
		int	nci=ncl.clustIdx_;
		
		// first unsetArg, then changeclust; need old clust for remove/unset old args
		pp.removeArgumentOnly(pai);
			for (Integer argIdx:pp.getArguments().keySet()) {				
				pp.unsetArgClust(argIdx);
				Argument arg=pp.getArgument(argIdx);
				arg.argPart_.unsetParent();
			}
			pp.changeClustOnly(nci,nrti);
			
			// set new args
			for (Integer argIdx:pp.getArguments().keySet()) {				
				Argument arg=pp.getArgument(argIdx);
				int ati=arg.path_.getArgType();
				int aci=-1;
				if (ncl.argTypeIdx_argClustIdxs_.get(ati)==null || ncl.argTypeIdx_argClustIdxs_.get(ati).size()==0) {
					aci=ncl.createArgClust(ati);
				}
				else aci=ncl.argTypeIdx_argClustIdxs_.get(ati).iterator().next();
				arg.argPart_.setParent(pp, argIdx);
				pp.setArgClustOnly(argIdx,aci);			
			}
		pp.setRelType(nrti);
		for (Integer argIdx:cp.getArguments().keySet()) {
			Argument arg=cp.getArgument(argIdx);				
			int ati=arg.path_.getArgType();
			int aci=-1;
			if (ncl.argTypeIdx_argClustIdxs_.get(ati)==null || ncl.argTypeIdx_argClustIdxs_.get(ati).size()==0) {
				aci=ncl.createArgClust(ati);
			}
			else aci=ncl.argTypeIdx_argClustIdxs_.get(ati).iterator().next();
			
			// can't call clust.unset; assume that part.unset is done
			cp.unsetArgClustOnly(argIdx);
			
			int newArgIdx=pp.addArgument(arg);
			pp.setArgClust(newArgIdx, aci);
			Part ccp=arg.argPart_;
			ccp.setParent(pp, newArgIdx);				
		}
		cp.destroy();			
	}

	
	// merge the second argclust into the first in the given clust
	public void mergeArg(Clust clust, int aci1, int aci2) {
		ArgClust ac1=clust.argClusts_.get(aci1), ac2=clust.argClusts_.get(aci2);
	
		// part: argclust maps
		Set<String> ids=(Set<String>)ac2.partRootTreeNodeIds_.clone();
		Iterator<String> sit=ids.iterator();
		while (sit.hasNext()) {
			String id=sit.next();
			Part p=Part.getPartByRootNodeId(id);
			Iterator<Integer> iit=p.argIdx_argClustIdx_.keySet().iterator();
			while (iit.hasNext()) {
				int ai=iit.next();
				int acix=p.argIdx_argClustIdx_.get(ai);
				if (acix==aci2) {
					p.setArgClust(ai, aci1);
				}
			}			
		}
	}

}
