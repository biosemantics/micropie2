package usp.semantic;

import java.util.*;

import usp.syntax.*;
import usp.util.*;

public class Scorer {
	boolean isDebug_=false;
	
	Parse parse_;
	
	public Scorer(Parse parse) {
		parse_=parse;
	}
	
	
	public double scoreOp(SearchOp op) {
		if (isDebug_) Utils.println(""+op+": ");
		switch (op.op_) {
		case SearchOp.OP_MERGE_CLUST_: return scoreOpMC(op);
		case SearchOp.OP_COMPOSE_: return scoreOpCompose(op.parClustIdx_,op.chdClustIdx_);
		default: return -100;
		}
	}
	double scoreOpMC(SearchOp op) {
		int cidx1=op.clustIdx1_,cidx2=op.clustIdx2_;
		
		assert cidx1<cidx2;
		
		double score=0;
		
		// HP
		score-=ParseParams.priorMerge_;
		
		// conj cnt
		Pair<Integer,Integer> pci=new Pair<Integer,Integer>(cidx1,cidx2);
		if (Clust.pairClustIdxs_conjCnt_.get(pci)!=null) {
			int cnt=Clust.pairClustIdxs_conjCnt_.get(pci);
			score-=ParseParams.priorNumConj_*cnt;
		}
		
//		Utils.println("\t"+op+" argComb="+combScore);
		
		Clust cl1=Clust.getClust(cidx1);
		Clust cl2=Clust.getClust(cidx2);
		int tc1=cl1.ttlCnt_;
		int tc2=cl2.ttlCnt_;
		int ci1=cl1.clustIdx_;
		int ci2=cl2.clustIdx_;
				
		// relType:
		score-=(Utils.xlogx(tc1+tc2)-Utils.xlogx(tc1)-Utils.xlogx(tc2));
		Iterator<Integer> iit=cl1.relTypeIdx_cnt_.keySet().iterator();
		while (iit.hasNext()) {
			int ri=iit.next();						
			if (cl2.relTypeIdx_cnt_.containsKey(ri)) {
				int c1=cl1.relTypeIdx_cnt_.get(ri);
				int c2=cl2.relTypeIdx_cnt_.get(ri);
				score+=Utils.xlogx(c1+c2)-Utils.xlogx(c1)-Utils.xlogx(c2);
				score+=ParseParams.priorNumParam_;	// one less param
			}
		}
				
		// Impact for root clust likelihood:		
		if (Clust.clustIdx_rootCnt_.get(ci1)!=null && Clust.clustIdx_rootCnt_.get(ci2)!=null) {
			int rc1=Clust.clustIdx_rootCnt_.get(ci1).intValue();
			int rc2=Clust.clustIdx_rootCnt_.get(ci2).intValue();
			score+=Utils.xlogx(rc1+rc2)-Utils.xlogx(rc1)-Utils.xlogx(rc2);
			score+=ParseParams.priorNumParam_;	// one less param
		}
		
		// Impact for parents:
		score+=scoreMCForParent(cidx1,cidx2);
		Clust clx1=cl1, clx2=cl2;
		// find one slot for each arg2: want the smaller one to save comp	
		//if (cl2.argClusts_.size()>cl1.argClusts_.size()) {
		if (cl2.argClusts_.size()>cl1.argClusts_.size()) {
			clx1=cl2;
			clx2=cl1;
		}
		Map<Integer,Integer> aci2_aci1=new HashMap<Integer,Integer>();
		score+=scoreMCForAlign(clx1,clx2, aci2_aci1);
		return score;
	} // scoreOPMC
	
	double scoreMCForParent(int clustIdx1, int clustIdx2) {
		double score=0;
		Map<Pair<Integer,Integer>,Integer> pars1=Clust.clustIdx_parArgs_.get(clustIdx1);
		Map<Pair<Integer,Integer>,Integer> pars2=Clust.clustIdx_parArgs_.get(clustIdx2);
		if (pars1!=null && pars2!=null) {
			Iterator<Pair<Integer,Integer>> pait=pars1.keySet().iterator();
			while (pait.hasNext()) {
				Pair<Integer,Integer> pa=pait.next();
				if (pars2.containsKey(pa)) {
					
					// lh diff
					int parClustIdx=pa.getFirst();
					int argClustIdx=pa.getSecond();

					// new mdl
					Clust pcl=Clust.getClust(parClustIdx);
					
					if (pcl==null) {
						Utils.println("ERR: ScoreMC parClust==null "+Clust.getClust(clustIdx1)+" "+Clust.getClust(clustIdx2));
						continue;
					}
					
					ArgClust ac=pcl.argClusts_.get(argClustIdx);
					int c1=ac.chdClustIdx_cnt_.get(clustIdx1), c2=ac.chdClustIdx_cnt_.get(clustIdx2);
					
					// only
					score+=ParseParams.priorNumParam_;	// one less param					
					score+=Utils.xlogx(c1+c2)-Utils.xlogx(c1)-Utils.xlogx(c2);
				}
			}
		}
		return score;
	} // scoreMCForArgClust
	
	double scoreOpCompose(int rcidx, int acidx) {
		double score=0;
//		double scoreArgComb=scoreAbsArgComb(rcidx,acidx);

		boolean isMyDebug=false;
		
		/*
		 * <OLD>
		 * A: SUM_i(xlx(reltype_i))-xlx(tc)
		 * foreach A.par: xlx(A) in argclust
		 * A.chd: SUM_i(xlx(argtype_i))+SUM_i(xlx(chdcl_i))-2*xlx(tac)
		 * B: SUM_i(xlx(reltype_i))-xlx(tc)
		 * B.chd: SUM_i(xlx(argtype_i))+SUM_i(xlx(chdcl_i))-2*xlx(tac)
		 * 
		 * <NEW>
		 * A/B: 0 // no variatioon in reltype
		 * A: SUM_i(xlx(rt_i-A/B))-xlx(ttl-A/B)
		 * A.par: xlx(A-A/B)+xlx(A/B)
		 * A.chd: SUM_i(xlx(argtype_i-A/B))+SUM_i(xlx(chdcl_i-A/B))-2*xlx(tac-A/B)
		 * 			+SUM_i(xlx(argtype_i of A/B))+SUM_i(xlx(chdcl_i of A/B))-2*xlx(tac of A/B)	// except B
		 * B.chd: SUM_i(xlx(argtype_i-A/B))+SUM_i(xlx(chdcl_i-A/B))-2*xlx(tac-A/B)
		 * 			+SUM_i(xlx(argtype_i of A/B))+SUM_i(xlx(chdcl_i of A/B))-2*xlx(tac of A/B)
		 */
//*			
		// --- COMP old A,B vs new A,B,A-B --- //	// HP: better than check the difference
		// parent: only change is mix of A -> A & A-B
		Map<Pair<Integer,Integer>,Integer> parArg_cnt=new HashMap<Pair<Integer,Integer>,Integer>();	// par-argclust, cnt of affected (A-B)

		// delta for A,B,A-B
		Map<Integer,Integer> rRelTypeIdx_newcnt=new HashMap<Integer,Integer>();
		Map<Integer,Integer> aRelTypeIdx_newcnt=new HashMap<Integer,Integer>();
		Map<Pair<Integer,Integer>,Integer> raRelTypeIdx_newcnt=new HashMap<Pair<Integer,Integer>,Integer>();
				
		// those that are affected in specific values
		Map<Integer,Map<Integer,Integer>> rArgClustIdx_argNum_cnt=new HashMap<Integer,Map<Integer,Integer>>();
		Map<Integer,Map<Integer,Integer>> aArgClustIdx_argNum_cnt=new HashMap<Integer,Map<Integer,Integer>>();
		Map<Integer,Map<Integer,Integer>> rNewArgClustIdx_argNum_cnt=new HashMap<Integer,Map<Integer,Integer>>();
		Map<Integer,Map<Integer,Integer>> aNewArgClustIdx_argNum_cnt=new HashMap<Integer,Map<Integer,Integer>>();
		
		Map<Integer,Map<Integer,Integer>> rArgClustIdx_argTypeIdx_cnt=new HashMap<Integer,Map<Integer,Integer>>();
		Map<Integer,Map<Integer,Integer>> aArgClustIdx_argTypeIdx_cnt=new HashMap<Integer,Map<Integer,Integer>>();
		Map<Integer,Map<Integer,Integer>> rNewArgClustIdx_argTypeIdx_cnt=new HashMap<Integer,Map<Integer,Integer>>();
		Map<Integer,Map<Integer,Integer>> aNewArgClustIdx_argTypeIdx_cnt=new HashMap<Integer,Map<Integer,Integer>>();
		
		Map<Integer,Map<Integer,Integer>> rArgClustIdx_chdClustIdx_cnt=new HashMap<Integer,Map<Integer,Integer>>();
		Map<Integer,Map<Integer,Integer>> aArgClustIdx_chdClustIdx_cnt=new HashMap<Integer,Map<Integer,Integer>>();
		Map<Integer,Map<Integer,Integer>> rNewArgClustIdx_chdClustIdx_cnt=new HashMap<Integer,Map<Integer,Integer>>();
		Map<Integer,Map<Integer,Integer>> aNewArgClustIdx_chdClustIdx_cnt=new HashMap<Integer,Map<Integer,Integer>>();
				
		Map<Integer,Integer> rArgClustIdx_partCnt=new HashMap<Integer,Integer>();
		Map<Integer,Integer> aArgClustIdx_partCnt=new HashMap<Integer,Integer>();
		Map<Integer,Integer> rNewArgClustIdx_partCnt=new HashMap<Integer,Integer>();
		Map<Integer,Integer> aNewArgClustIdx_partCnt=new HashMap<Integer,Integer>();
		Map<Integer,Integer> rArgClustIdx_argCnt=new HashMap<Integer,Integer>();
		Map<Integer,Integer> aArgClustIdx_argCnt=new HashMap<Integer,Integer>();
		Map<Integer,Integer> rNewArgClustIdx_argCnt=new HashMap<Integer,Integer>();
		Map<Integer,Integer> aNewArgClustIdx_argCnt=new HashMap<Integer,Integer>();
		
		//
		Clust rcl=Clust.getClust(rcidx);
		Clust acl=Clust.getClust(acidx);

		if (isMyDebug) Utils.println("ScoreAbs: "+rcidx+":"+rcl+" + "+acidx+":"+acl);
		int rtc_new=rcl.ttlCnt_, atc_new=acl.ttlCnt_, ratc_new=0;
		int raRootCnt=0;
		
		// gather stat thru parts
		// TO-DO: assume no twice appearance
		Set<Pair<String,String>> parChdNids=Part.getPairPartRootNodeIds(rcidx, acidx);
		
		if (parChdNids==null) {
//			Utils.println("ERR: scoreAbs pairclust=null");
			return -10000;
		}
		
		Iterator<Pair<String,String>> psit=parChdNids.iterator();
		while (psit.hasNext()) {
			Pair<String,String> pcnid=psit.next();
			String pnid=pcnid.getFirst();
			String cnid=pcnid.getSecond();
			Part pp=Part.getPartByRootNodeId(pnid);
			Part cp=Part.getPartByRootNodeId(cnid);
			
			rtc_new--; atc_new--; ratc_new++;

			// reltype
			int rrt=pp.getRelTypeIdx();
			int art=cp.getRelTypeIdx();		
			int raArgClustidx=pp.getArgClust(cp.parArgIdx_);
			
			if (rRelTypeIdx_newcnt.get(rrt)==null) rRelTypeIdx_newcnt.put(rrt, rcl.relTypeIdx_cnt_.get(rrt)-1);
			else rRelTypeIdx_newcnt.put(rrt, rRelTypeIdx_newcnt.get(rrt)-1);
			if (aRelTypeIdx_newcnt.get(art)==null) aRelTypeIdx_newcnt.put(art, acl.relTypeIdx_cnt_.get(art)-1);
			else aRelTypeIdx_newcnt.put(art, aRelTypeIdx_newcnt.get(art)-1);
			
			Pair<Integer,Integer> rart=new Pair<Integer,Integer>(rrt,art);
			if (raRelTypeIdx_newcnt.get(rart)==null) raRelTypeIdx_newcnt.put(rart, 1);
			else raRelTypeIdx_newcnt.put(rart, raRelTypeIdx_newcnt.get(rart)+1);
			
			// --- par of par-chd
			if (pp.getParPart()!=null) {
				Part ppp=pp.getParPart();
				int ai=pp.getParArgIdx();
				int ppi=ppp.getClustIdx();
				int aci=ppp.getArgClust(ai);				
				Pair<Integer,Integer> pa=new Pair<Integer,Integer>(ppi,aci);
				if (parArg_cnt.get(pa)==null) parArg_cnt.put(pa, 1);
				else parArg_cnt.put(pa, parArg_cnt.get(pa)+1);
			}
			else raRootCnt++;
			
			// argNum
			Iterator<Integer> iit=pp.argClustIdx_argIdxs_.keySet().iterator();
			while (iit.hasNext()) {
				int aci=iit.next();
				int an=pp.argClustIdx_argIdxs_.get(aci).size();
				ArgClust ac=rcl.argClusts_.get(aci);				
				if (rArgClustIdx_partCnt.get(aci)==null) 
					rArgClustIdx_partCnt.put(aci,ac.partRootTreeNodeIds_.size()-1);
				else 
					rArgClustIdx_partCnt.put(aci,rArgClustIdx_partCnt.get(aci)-1);
				
				Map<Integer,Integer> an_cnt=rArgClustIdx_argNum_cnt.get(aci);
				if (an_cnt==null) {
					an_cnt=new HashMap<Integer,Integer>();
					rArgClustIdx_argNum_cnt.put(aci,an_cnt);
				}
				if (an_cnt.get(an)==null) an_cnt.put(an, ac.argNum_cnt_.get(an)-1);
				else an_cnt.put(an, an_cnt.get(an)-1);
				
				// argNum for A-B
				int newArgNum=an;
				if (aci==raArgClustidx) newArgNum--;
				if (newArgNum==0) continue;
				
				an_cnt=rNewArgClustIdx_argNum_cnt.get(aci);
				if (an_cnt==null) {
					an_cnt=new HashMap<Integer,Integer>();
					rNewArgClustIdx_argNum_cnt.put(aci,an_cnt);
				}
				if (an_cnt.get(newArgNum)==null) an_cnt.put(newArgNum, 1);
				else an_cnt.put(newArgNum, an_cnt.get(newArgNum)+1);
				
				if (rNewArgClustIdx_partCnt.get(aci)==null) 
					rNewArgClustIdx_partCnt.put(aci, 1);
				else 
					rNewArgClustIdx_partCnt.put(aci, rNewArgClustIdx_partCnt.get(aci)+1);
			}
			
			iit=cp.argClustIdx_argIdxs_.keySet().iterator();
			while (iit.hasNext()) {
				int aci=iit.next();
				int an=cp.argClustIdx_argIdxs_.get(aci).size();
				ArgClust ac=acl.argClusts_.get(aci);				
				if (aArgClustIdx_partCnt.get(aci)==null) 
					aArgClustIdx_partCnt.put(aci,ac.partRootTreeNodeIds_.size()-1);
				else 
					aArgClustIdx_partCnt.put(aci,aArgClustIdx_partCnt.get(aci)-1);
				
				Map<Integer,Integer> an_cnt=aArgClustIdx_argNum_cnt.get(aci);
				if (an_cnt==null) {
					an_cnt=new HashMap<Integer,Integer>();
					aArgClustIdx_argNum_cnt.put(aci,an_cnt);
				}
				if (an_cnt.get(an)==null) an_cnt.put(an, ac.argNum_cnt_.get(an)-1);
				else an_cnt.put(an, an_cnt.get(an)-1);
				
				// argNum for A-B
				an_cnt=aNewArgClustIdx_argNum_cnt.get(aci);
				if (an_cnt==null) {
					an_cnt=new HashMap<Integer,Integer>();
					aNewArgClustIdx_argNum_cnt.put(aci,an_cnt);
				}
				if (an_cnt.get(an)==null) an_cnt.put(an, 1);
				else an_cnt.put(an, an_cnt.get(an)+1);
				
				if (aNewArgClustIdx_partCnt.get(aci)==null) 
					aNewArgClustIdx_partCnt.put(aci, 1);
				else 
					aNewArgClustIdx_partCnt.put(aci, aNewArgClustIdx_partCnt.get(aci)+1);
			}
			
			// --- A.chd
			Map<Integer,Argument> args=pp.getArguments();
			for (Integer ai:args.keySet()) {			
				Argument arg=args.get(ai);
				Part ap=arg.argPart_;
				int cci=ap.clustIdx_;
				int aci=pp.getArgClust(ai);
				ArgClust ac=rcl.argClusts_.get(aci);
				int ati=arg.path_.getArgType();
			
				// old
				if (rArgClustIdx_argCnt.get(aci)==null) 
					rArgClustIdx_argCnt.put(aci,ac.ttlArgCnt_-1);
				else 
					rArgClustIdx_argCnt.put(aci,rArgClustIdx_argCnt.get(aci)-1);
				
				Map<Integer,Integer> ati_cnt=rArgClustIdx_argTypeIdx_cnt.get(aci);
				if (ati_cnt==null) {
					ati_cnt=new HashMap<Integer,Integer>();
					rArgClustIdx_argTypeIdx_cnt.put(aci,ati_cnt);
				}
				if (ati_cnt.get(ati)==null) ati_cnt.put(ati, ac.argTypeIdx_cnt_.get(ati)-1);
				else ati_cnt.put(ati, ati_cnt.get(ati)-1);
				
				Map<Integer,Integer> cci_cnt=rArgClustIdx_chdClustIdx_cnt.get(aci);
				if (cci_cnt==null) {
					cci_cnt=new HashMap<Integer,Integer>();
					rArgClustIdx_chdClustIdx_cnt.put(aci,cci_cnt);
				}
				if (cci_cnt.get(cci)==null) cci_cnt.put(cci, ac.chdClustIdx_cnt_.get(cci)-1);
				else cci_cnt.put(cci, cci_cnt.get(cci)-1);
				
				// new arg				
				if (!ap.getRelTreeRoot().getId().equals(cp.getRelTreeRoot().getId())) {
					if (rNewArgClustIdx_argCnt.get(aci)==null) 
						rNewArgClustIdx_argCnt.put(aci,1);
					else 
						rNewArgClustIdx_argCnt.put(aci,rNewArgClustIdx_argCnt.get(aci)+1);
					
					Map<Integer,Integer> idx_cnt=rNewArgClustIdx_argTypeIdx_cnt.get(aci);
					if (idx_cnt==null) {
						idx_cnt=new HashMap<Integer,Integer>();
						rNewArgClustIdx_argTypeIdx_cnt.put(aci,idx_cnt);
					}
					if (idx_cnt.get(ati)==null) idx_cnt.put(ati, 1);
					else idx_cnt.put(ati, idx_cnt.get(ati)+1);
					
					idx_cnt=rNewArgClustIdx_chdClustIdx_cnt.get(aci);
					if (idx_cnt==null) {
						idx_cnt=new HashMap<Integer,Integer>();
						rNewArgClustIdx_chdClustIdx_cnt.put(aci,idx_cnt);
					}
					if (idx_cnt.get(cci)==null) idx_cnt.put(cci, 1);
					else idx_cnt.put(cci, idx_cnt.get(cci)+1);
				}
			} // pp.getArgument
		
			args=cp.getArguments();
			for (Integer ai:args.keySet()) {
				Argument arg=args.get(ai);
				Part ap=arg.argPart_;
				int cci=ap.clustIdx_;
				int aci=cp.getArgClust(ai);
				int ati=arg.path_.getArgType();
				ArgClust ac=acl.argClusts_.get(aci);
				
				if (aArgClustIdx_argCnt.get(aci)==null) 
					aArgClustIdx_argCnt.put(aci,ac.ttlArgCnt_-1);
				else 
					aArgClustIdx_argCnt.put(aci,aArgClustIdx_argCnt.get(aci)-1);
								
				Map<Integer,Integer> ati_cnt=aArgClustIdx_argTypeIdx_cnt.get(aci);
				if (ati_cnt==null) {
					ati_cnt=new HashMap<Integer,Integer>();
					aArgClustIdx_argTypeIdx_cnt.put(aci,ati_cnt);
				}
				if (ati_cnt.get(ati)==null) ati_cnt.put(ati, ac.argTypeIdx_cnt_.get(ati)-1);
				else ati_cnt.put(ati, ati_cnt.get(ati)-1);
				
				Map<Integer,Integer> cci_cnt=aArgClustIdx_chdClustIdx_cnt.get(aci);
				if (cci_cnt==null) {
					cci_cnt=new HashMap<Integer,Integer>();
					aArgClustIdx_chdClustIdx_cnt.put(aci,cci_cnt);
				}
				if (cci_cnt.get(cci)==null) cci_cnt.put(cci, ac.chdClustIdx_cnt_.get(cci)-1);
				else cci_cnt.put(cci, cci_cnt.get(cci)-1);
				
				// new arg
				if (aNewArgClustIdx_argCnt.get(aci)==null) 
					aNewArgClustIdx_argCnt.put(aci,1);
				else 
					aNewArgClustIdx_argCnt.put(aci,aNewArgClustIdx_argCnt.get(aci)+1);

				Map<Integer,Integer> idx_cnt=aNewArgClustIdx_argTypeIdx_cnt.get(aci);
				if (idx_cnt==null) {
					idx_cnt=new HashMap<Integer,Integer>();
					aNewArgClustIdx_argTypeIdx_cnt.put(aci,idx_cnt);
				}
				if (idx_cnt.get(ati)==null) idx_cnt.put(ati, 1);
				else idx_cnt.put(ati, idx_cnt.get(ati)+1);
				
				idx_cnt=aNewArgClustIdx_chdClustIdx_cnt.get(aci);
				if (idx_cnt==null) {
					idx_cnt=new HashMap<Integer,Integer>();
					aNewArgClustIdx_chdClustIdx_cnt.put(aci,idx_cnt);
				}
				if (idx_cnt.get(cci)==null) idx_cnt.put(cci, 1);
				else idx_cnt.put(cci, idx_cnt.get(cci)+1);							
			}
		} // end while psit.hasNext
		
		// --- render score	
		double denomor, denomnr, denomoa, denomna, denomra;
		Iterator<Integer> iit,iit2;
		Iterator<Pair<Integer,Integer>> piit;
		
		// root
		if (raRootCnt>0) {
			int origRootCnt=Clust.clustIdx_rootCnt_.get(rcidx);
			if (origRootCnt>raRootCnt) {
				score+=Utils.xlogx(raRootCnt)+Utils.xlogx(origRootCnt-raRootCnt)-Utils.xlogx(origRootCnt);
				score-=ParseParams.priorNumParam_;
			}
		}
		
		// reltype			
		denomor=Utils.xlogx(rcl.ttlCnt_);
		denomnr=Utils.xlogx(rtc_new);
		iit=rRelTypeIdx_newcnt.keySet().iterator();
		while (iit.hasNext()) {
			int rrt=iit.next();
			int cnt=rRelTypeIdx_newcnt.get(rrt);
			int origcnt=rcl.relTypeIdx_cnt_.get(rrt);
			assert origcnt>=cnt;
			score-=Utils.xlogx(origcnt);
			if (cnt>0) {
				score+=Utils.xlogx(cnt);				
			}
			else score+=ParseParams.priorNumParam_;	// old params eliminated
		}
		score+=denomor;		
		score-=denomnr;
		
		denomoa=Utils.xlogx(acl.ttlCnt_);
		denomna=Utils.xlogx(atc_new);
		iit=aRelTypeIdx_newcnt.keySet().iterator();
		while (iit.hasNext()) {
			int art=iit.next();
			int cnt=aRelTypeIdx_newcnt.get(art);
			int origcnt=acl.relTypeIdx_cnt_.get(art);
			assert origcnt>=cnt;
			score-=Utils.xlogx(origcnt);
			if (cnt>0) {
				score+=Utils.xlogx(cnt);				
			}
			else score+=ParseParams.priorNumParam_;	// old params eliminated
		}
		score+=denomoa;		
		score-=denomna;
		
		piit=raRelTypeIdx_newcnt.keySet().iterator();
		while (piit.hasNext()) {
			Pair<Integer,Integer> rart=piit.next();
			int cnt=raRelTypeIdx_newcnt.get(rart);
			score-=ParseParams.priorNumParam_;	// new param
			score+=Utils.xlogx(cnt);
		}
		denomra=Utils.xlogx(ratc_new);
		score-=denomra;
		
		// par: no change in denom
		piit=parArg_cnt.keySet().iterator();
		while (piit.hasNext()) {
			Pair<Integer,Integer> pi=piit.next();
			int pci=pi.getFirst();
			int aci=pi.getSecond();
			int cnt=parArg_cnt.get(pi);	// A -> A-B
			Clust pc=Clust.getClust(pci);
//			Utils.println("par clust="+pc);
			ArgClust ac=pc.argClusts_.get(aci);
			int origcnt=ac.chdClustIdx_cnt_.get(rcidx);
			if (cnt==origcnt) continue;
			score-=ParseParams.priorNumParam_;	// extra param for A-B
			score+=Utils.xlogx(cnt)+Utils.xlogx(origcnt-cnt)-Utils.xlogx(origcnt);
		}
		
		if (isMyDebug) Utils.println("\treltype, root, parent: "+score);		
		
		// orig vs new A		
		iit=rcl.argClusts_.keySet().iterator();
		while (iit.hasNext()) {
			int aci=iit.next();
			ArgClust ac=rcl.argClusts_.get(aci);
		
//			int numMore=0, numLess=0;
//			if (isMyDebug) Utils.println("\t\t-> aci="+aci+" ac="+ac+" score="+score);
			
			// absent cnt
			int origPartCnt=ac.partRootTreeNodeIds_.size();
			score-=(Utils.xlogx(rcl.ttlCnt_-origPartCnt)-denomor);
//			score-=2*(Utils.xlogx(rcl.ttlCnt_-origPartCnt)-denomor);
			
			// not affect this arg, except for absent
			if (rArgClustIdx_partCnt.get(aci)==null) {
				score+=(Utils.xlogx(rtc_new-origPartCnt)-denomnr);
//				score+=2*(Utils.xlogx(rtc_new-origPartCnt)-denomnr);
				continue;
			}
			
			// handle delta
			// ==null ~ no chg; ==0 ~ change a lot!
			int newPartCnt=rArgClustIdx_partCnt.get(aci);
			if (newPartCnt>0) {
				// argnum
				score+=(Utils.xlogx(rtc_new-newPartCnt)-denomnr);
			}
			Map<Integer,Integer> idx_cnt=rArgClustIdx_argNum_cnt.get(aci);
			iit2=idx_cnt.keySet().iterator();			
			while (iit2.hasNext()) {
				int an=iit2.next();
				int ocnt=ac.argNum_cnt_.get(an);
				score-=Utils.xlogx(ocnt);
				int cnt=idx_cnt.get(an);				
				if (cnt>0) {
					score+=Utils.xlogx(cnt);					
				}
				else {
					score+=ParseParams.priorNumParam_;
				}
			}
			
//			if (isMyDebug) Utils.println("\t\t\tadd argnum="+score+" numLess="+numLess);
			
			// argtype/chdclust
			score-=2*(Utils.xlogx(rArgClustIdx_argCnt.get(aci))-Utils.xlogx(ac.ttlArgCnt_));
			
			idx_cnt=rArgClustIdx_argTypeIdx_cnt.get(aci);
			iit2=idx_cnt.keySet().iterator();			
			while (iit2.hasNext()) {
				int ati=iit2.next();
				int ocnt=ac.argTypeIdx_cnt_.get(ati);
				score-=Utils.xlogx(ocnt);
				int cnt=idx_cnt.get(ati);
				if (cnt>0) {
					score+=Utils.xlogx(cnt);						
				}
				else score+=ParseParams.priorNumParam_;
			}				
			
			idx_cnt=rArgClustIdx_chdClustIdx_cnt.get(aci);
			iit2=idx_cnt.keySet().iterator();			
			while (iit2.hasNext()) {
				int cci=iit2.next();
				int ocnt=ac.chdClustIdx_cnt_.get(cci);
				score-=Utils.xlogx(ocnt);
				int cnt=idx_cnt.get(cci);
				if (cnt>0) {
					score+=Utils.xlogx(cnt);											
				}
				else score+=ParseParams.priorNumParam_;				
			}
			
//			if (isMyDebug) Utils.println("\t\t\tadd argtype/chd="+score+" numLess="+numLess);
		} // orig vs new A
		
		if (isMyDebug) Utils.println("\tA: "+score);
		
		// orig vs new B
		iit=acl.argClusts_.keySet().iterator();
		while (iit.hasNext()) {
			int aci=iit.next();
			ArgClust ac=acl.argClusts_.get(aci);
			
			// absent cnt
			int origPartCnt=ac.partRootTreeNodeIds_.size();
			score-=(Utils.xlogx(acl.ttlCnt_-origPartCnt)-denomoa);
//			score-=2*(Utils.xlogx(acl.ttlCnt_-origPartCnt)-denomoa);
			
			// not affect this arg, except for absent
			if (aArgClustIdx_partCnt.get(aci)==null) {
				score+=(Utils.xlogx(atc_new-origPartCnt)-denomna);
//				score+=2*(Utils.xlogx(atc_new-origPartCnt)-denomna);
				continue;
			}
			
			// handle delta
			// if newpart=0, arg eliminated
			int newPartCnt=aArgClustIdx_partCnt.get(aci);
			if (newPartCnt>0) {
				// argnum
				score+=(Utils.xlogx(atc_new-newPartCnt)-denomna);
			}
			
//				score+=2*(Utils.xlogx(atc_new-newPartCnt)-denomna);
			Map<Integer,Integer> idx_cnt=aArgClustIdx_argNum_cnt.get(aci);
			iit2=idx_cnt.keySet().iterator();			
			while (iit2.hasNext()) {
				int an=iit2.next();
				int ocnt=ac.argNum_cnt_.get(an);
				score-=Utils.xlogx(ocnt);
				int cnt=idx_cnt.get(an);
				if (cnt>0) {
					score+=Utils.xlogx(cnt);
				}
				else score+=ParseParams.priorNumParam_;
			}
			
			// argtype/chdclust
			score-=2*(Utils.xlogx(aArgClustIdx_argCnt.get(aci))-Utils.xlogx(ac.ttlArgCnt_));
			
			idx_cnt=aArgClustIdx_argTypeIdx_cnt.get(aci);
			iit2=idx_cnt.keySet().iterator();			
			while (iit2.hasNext()) {
				int ati=iit2.next();
				int ocnt=ac.argTypeIdx_cnt_.get(ati);
				score-=Utils.xlogx(ocnt);
				int cnt=idx_cnt.get(ati);
				if (cnt>0) {
					score+=Utils.xlogx(cnt);
				}
				else score+=ParseParams.priorNumParam_;				
			}				
			
			idx_cnt=aArgClustIdx_chdClustIdx_cnt.get(aci);
			iit2=idx_cnt.keySet().iterator();			
			while (iit2.hasNext()) {
				int cci=iit2.next();
				int ocnt=ac.chdClustIdx_cnt_.get(cci);
				score-=Utils.xlogx(ocnt);
				int cnt=idx_cnt.get(cci);
				if (cnt>0) {
					score+=Utils.xlogx(cnt);
				}
				else score+=ParseParams.priorNumParam_;
			}
		} // orig vs new B
		
		if (isMyDebug) Utils.println("\tB: "+score);
		
		// A-B: new A; B		
		int numLess=0;
		iit=rNewArgClustIdx_partCnt.keySet().iterator();
		while (iit.hasNext()) {
			int aci=iit.next();
			int partCnt=rNewArgClustIdx_partCnt.get(aci);
			score+=Utils.xlogx(ratc_new-partCnt)-denomra;
			Map<Integer,Integer> idx_cnt=rNewArgClustIdx_argNum_cnt.get(aci);			
			iit2=idx_cnt.keySet().iterator();
			while (iit2.hasNext()) {
				int idx=iit2.next();
				int cnt=idx_cnt.get(idx);
				score+=Utils.xlogx(cnt);
				score-=ParseParams.priorNumParam_;
//				Utils.println("\t\t\t\tnew argNum="+idx);
			}
		}
		iit=aNewArgClustIdx_partCnt.keySet().iterator();
		while (iit.hasNext()) {
			int aci=iit.next();
			int partCnt=aNewArgClustIdx_partCnt.get(aci);
			score+=Utils.xlogx(ratc_new-partCnt)-denomra;
			Map<Integer,Integer> idx_cnt=aNewArgClustIdx_argNum_cnt.get(aci);			
			iit2=idx_cnt.keySet().iterator();
			while (iit2.hasNext()) {
				int idx=iit2.next();
				int cnt=idx_cnt.get(idx);
				score+=Utils.xlogx(cnt);
				score-=ParseParams.priorNumParam_;
			}
		}		
		
		iit=rNewArgClustIdx_argCnt.keySet().iterator();
		while (iit.hasNext()) {
			int aci=iit.next();
			int argCnt=rNewArgClustIdx_argCnt.get(aci);
			score-=2*Utils.xlogx(argCnt);
			
			Map<Integer,Integer> idx_cnt=rNewArgClustIdx_argTypeIdx_cnt.get(aci);			
			iit2=idx_cnt.keySet().iterator();
			while (iit2.hasNext()) {
				int idx=iit2.next();
				int cnt=idx_cnt.get(idx);
				score+=Utils.xlogx(cnt);
				score-=ParseParams.priorNumParam_;
//				Utils.println("\t\t\t\tnew argType="+ArgType.getArgType(idx));
			}
			idx_cnt=rNewArgClustIdx_chdClustIdx_cnt.get(aci);			
			iit2=idx_cnt.keySet().iterator();
			while (iit2.hasNext()) {
				int idx=iit2.next();
				int cnt=idx_cnt.get(idx);
				score+=Utils.xlogx(cnt);
				score-=ParseParams.priorNumParam_;
//				Utils.println("\t\t\t\tnew chd="+Clust.getClust(idx));
			}
		}
//		if (isMyDebug) Utils.println("\t\t\trNew argType/chd numLess="+numLess);
		iit=aNewArgClustIdx_argCnt.keySet().iterator();
		while (iit.hasNext()) {
			int aci=iit.next();
			int argCnt=aNewArgClustIdx_argCnt.get(aci);
			score-=2*Utils.xlogx(argCnt);
			
			Map<Integer,Integer> idx_cnt=aNewArgClustIdx_argTypeIdx_cnt.get(aci);			
			iit2=idx_cnt.keySet().iterator();
			while (iit2.hasNext()) {
				int idx=iit2.next();
				int cnt=idx_cnt.get(idx);
				score+=Utils.xlogx(cnt);
				score-=ParseParams.priorNumParam_;				
			}
			idx_cnt=aNewArgClustIdx_chdClustIdx_cnt.get(aci);			
			iit2=idx_cnt.keySet().iterator();
			while (iit2.hasNext()) {
				int idx=iit2.next();
				int cnt=idx_cnt.get(idx);
				score+=Utils.xlogx(cnt);
				score-=ParseParams.priorNumParam_;
			}
		}
				
		if (isMyDebug) Utils.println("\tA/B argtype/chd: "+score);
		
		return score;
	}

	// for reparse: eval lambda-reduction
	double scoreOpComposePart(Part pp, Part cp) {
		double score=0;
		boolean isMyDebug=false;
		
		// ------------------------------ //
		// first check if feasible
		// ------------------------------ //
		Clust rcl=Clust.getClust(pp.clustIdx_);
		Clust acl=Clust.getClust(cp.clustIdx_);

		int pai=cp.parArgIdx_;
		Argument pcarg=pp.getArguments().get(pai);		
		String dep=pcarg.path_.getDep();
		TreeNode ptn=pp.relTreeRoot_;
		TreeNode ctn=cp.relTreeRoot_;
		int orti=pp.relTypeIdx_;
		ptn.addChild(dep, ctn);
		int nrti=RelType.getRelType(ptn);			
		
		if (Clust.getClustsWithRelType(nrti)==null) {
			// not feasible
			return 0;
		}
	
		Clust ncl=Clust.getClust(Clust.getClustsWithRelType(nrti).iterator().next());
		int nci=ncl.clustIdx_;
		
		// ------------------------------ //
		// score difference
		// ------------------------------ //
	
		// reltype/root
		if (pp.getParPart()!=null) {
			Part ppp=pp.getParPart();
			int ai=pp.getParArgIdx();
			int ppi=ppp.getClustIdx();
			Clust ppcl=Clust.getClust(ppi);
			ArgClust ac=ppcl.argClusts_.get(ppp.getArgClust(ai));				
			int oc=ac.chdClustIdx_cnt_.get(rcl.clustIdx_);
			int nc=ac.chdClustIdx_cnt_.get(nci);
			score+=Math.log(nc)-Math.log(oc);
		}
		else {
			int oc=Clust.clustIdx_rootCnt_.get(rcl);
			int nc=Clust.clustIdx_rootCnt_.get(ncl);
			score+=Math.log(nc)-Math.log(oc);
		}
		
		// old: rnid -> all args
		for (Integer aci: pp.argClustIdx_argIdxs_.keySet()) {
			Set<Integer> ais=pp.argClustIdx_argIdxs_.get(aci);
			ArgClust ac=rcl.argClusts_.get(aci);
			
			// argnum
			score-=(Math.log(ac.argNum_cnt_.get(ais.size()))-Math.log(ac.ttlArgCnt_));
			
			for (Integer ai: ais) {
				Argument arg=pp.getArgument(ai);
				
				// chdclust
				score-=(Math.log(ac.chdClustIdx_cnt_.get(arg.argPart_.clustIdx_))-Math.log(ac.ttlArgCnt_));
				
				// path
				score-=(Math.log(ac.argTypeIdx_cnt_.get(arg.path_.getArgType()))-Math.log(ac.ttlArgCnt_));
			}
		}

		// new: rnid+anid -> args
		Map<Integer,Integer> ai_newaci=new HashMap<Integer,Integer>();
		for (Integer ai: pp.args_.keySet()) {
			if (ai==pai) continue;
			Argument arg=pp.args_.get(ai);
			int ati=arg.path_.getArgType();
			int aci=ncl.argTypeIdx_argClustIdxs_.get(ati).iterator().next();
			ai_newaci.put(ai, aci);
		}
		
		Map<Integer,Set<Integer>> newArgClustIdx_ais=new HashMap<Integer,Set<Integer>>();
		for (Integer ai: ai_newaci.keySet()) {
			int aci=ai_newaci.get(ai);
			Set<Integer> ais=newArgClustIdx_ais.get(aci);
			if (ais==null) {
				ais=new HashSet<Integer>();
				newArgClustIdx_ais.put(aci, ais);
			}
			ais.add(ai);
		}
		for (Integer aci: newArgClustIdx_ais.keySet()) {
			Set<Integer> ais=newArgClustIdx_ais.get(aci);
			ArgClust ac=ncl.argClusts_.get(aci);
			
			// argnum
			score+=(Math.log(ac.argNum_cnt_.get(ais.size()))-Math.log(ac.ttlArgCnt_));
			
			for (Integer ai: ais) {
				Argument arg=pp.getArgument(ai);
				
				// chdclust
				score-=(Math.log(ac.chdClustIdx_cnt_.get(arg.argPart_.clustIdx_))-Math.log(ac.ttlArgCnt_));
				
				// path
				score-=(Math.log(ac.argTypeIdx_cnt_.get(arg.path_.getArgType()))-Math.log(ac.ttlArgCnt_));
			}
		}
		
		return score;
	}

	
	// ------------------------------------------------------------------ //
	// greedy align
	double scoreMCForAlign(Clust clx1, Clust clx2, Map<Integer,Integer> aci2_aci1) {
		boolean isMyDebug=false;
	
		Integer[] acIdxs1=clx1.argClusts_.keySet().toArray(new Integer[0]);
		Integer[] acIdxs2=clx2.argClusts_.keySet().toArray(new Integer[0]);

		// greedy
		double finalScore=0;
		Map<Integer,Integer> ac1_ac2=new HashMap<Integer,Integer>();
		int tc1=clx1.ttlCnt_,tc2=clx2.ttlCnt_;
		
		// denominator: type/clust
		double denom=Utils.xlogx(tc1+tc2), denom1=Utils.xlogx(tc1), denom2=Utils.xlogx(tc2);		
					
		// subtract new (merged cl, isolated argcl), add old (isolated cl)
		double deltaNoMergeArgClust=0;
		for (int i=0; i<acIdxs1.length; i++) {
			int ai1=acIdxs1[i];
			ArgClust ac1=clx1.argClusts_.get(ai1);
			int pc1=ac1.partRootTreeNodeIds_.size();			
			deltaNoMergeArgClust+=Utils.xlogx(tc1+tc2-pc1)-denom-Utils.xlogx(tc1-pc1)+denom1;			
		}
		for (int i=0; i<acIdxs2.length; i++) {
			int ai2=acIdxs2[i];
			ArgClust ac2=clx2.argClusts_.get(ai2);
			int pc2=ac2.partRootTreeNodeIds_.size();			
			deltaNoMergeArgClust+=Utils.xlogx(tc1+tc2-pc2)-denom-Utils.xlogx(tc2-pc2)+denom2;			
		}
				
		// independently find 1-1 map for each arg2
		for (int i=0; i<acIdxs2.length; i++) {						
			int ai2=acIdxs2[i];
			ArgClust ac2=clx2.argClusts_.get(ai2);
			int pc2=ac2.partRootTreeNodeIds_.size();			
			int tac2=ac2.ttlArgCnt_;
			TreeSet<String> ptids2=ac2.partRootTreeNodeIds_;
			
			/*
			 *  old arg1: Utils.xlogx(tc1-pc1)+SUM_num(Utils.xlogx(cnt_num))-denom1 + <SUM_i(Utils.xlogx(pci1))-denomTTL1>_(argtype / chdclust)
			 *  old arg2: Utils.xlogx(tc2-pc2)+SUM_num(Utils.xlogx(cnt_num))-denom2 + <SUM_i(Utils.xlogx(pci2))-denomTTL2>_(argtype / chdclust)
			 *  -- actually no need to consider old
			 *  
			 *  new arg1 (no merge): Utils.xlogx(tc1+tc2-pc1)+SUM_num(Utils.xlogx(cnt_num))-denom + <SUM_i(Utils.xlogx(pci1))-denomTTL1>_(argtype / chdclust)
			 *  new arg2 (no merge): Utils.xlogx(tc1+tc2-pc2)+SUM_num(Utils.xlogx(cnt_num))-denom + <SUM_i(Utils.xlogx(pci2))-denomTTL2>_(argtype / chdclust)
			 *  new arg1,arg2 (merge): Utils.xlogx(tc1+tc2-pc1-pc2)+SUM_num(Utils.xlogx(new_cnt_num))-denom + <SUM_i(Utils.xlogx(pci_1/2))-denomTTL_1/2>_(argtype / chdclust)
			 *  	- one fewer absentee param; also in cnt/argtype/chdclust
			 */
								
			// NO-MAP: reference state (subsequent scores are delta relative to this one)
			// new prb for arg2 
			// ignore absentee changes in arg1s, subtracted in the following loop if it's changed due to merge
			// common component in new arg1/2 - new arg1 - new arg2 
			double newBaseScore=0;	// reference base to compare against
			newBaseScore+=Utils.xlogx(tc1+tc2-pc2)-denom;
//			maxScore-=ParseParams.priorNumParam_;	// one more absentee type/clust if NO-MAP
			newBaseScore-=2*Utils.xlogx(tac2);
			
			double maxScore=0;
			int maxMap=-1;
			maxScore=newBaseScore;
			
			//
			for (int j=0; j<acIdxs1.length; j++) {						
				int ai1=acIdxs1[j];
				ArgClust ac1=clx1.argClusts_.get(ai1);
				int pc1=ac1.partRootTreeNodeIds_.size();
				int tac1=ac1.ttlArgCnt_;
				TreeSet<String> ptids1=ac1.partRootTreeNodeIds_;
				
				// TO-DO: why empty
				if (ptids1.size()==0) continue;
				if (ptids2.size()==0) {
					aci2_aci1.put(ai2, ai1);
					maxScore=0;
					break;
				}
				
				double score=0;
				
				// HP
				score-=ParseParams.priorMerge_;
				
				// delta arg2 + delta of arg1: compared to no-map
				score+=Utils.xlogx(tc1+tc2-pc1-pc2)-Utils.xlogx(tc1+tc2-pc1)
					+2*Utils.xlogx(tac1)-2*Utils.xlogx(tac1+tac2);
				
				// loop thru all parts: determine chg in cnt
				Map<Integer,Integer> argNum_newCnt=new HashMap<Integer,Integer>();
				Iterator<Integer> iit=ac1.argNum_cnt_.keySet().iterator();
				while (iit.hasNext()) {
					int an=iit.next();
					int c=ac1.argNum_cnt_.get(an);
					if (argNum_newCnt.get(an)==null) argNum_newCnt.put(an, c);
					else argNum_newCnt.put(an, argNum_newCnt.get(an)+c);
				}
				iit=ac2.argNum_cnt_.keySet().iterator();
				while (iit.hasNext()) {
					int an=iit.next();
					int c=ac2.argNum_cnt_.get(an);
					if (argNum_newCnt.get(an)==null) argNum_newCnt.put(an, c);
					else argNum_newCnt.put(an, argNum_newCnt.get(an)+c);
				}
				
				Iterator<String> sit1=ptids1.iterator();
				Iterator<String> sit2=ptids2.iterator();
				String pid1=sit1.next(), pid2=sit2.next();
				while (true) {
					if (pid1.equals(pid2)) {
						Utils.println("ERR: overlapped part in merging diff clust, should NOT happen!");
						System.exit(-1);
						
						// new cnt
						int c1=Part.getPartByRootNodeId(pid1).argClustIdx_argIdxs_.get(ai1).size();
						int c2=Part.getPartByRootNodeId(pid2).argClustIdx_argIdxs_.get(ai2).size();
						int c0=c1+c2;
						if (argNum_newCnt.get(c0)==null) argNum_newCnt.put(c0,1);
						else argNum_newCnt.put(c0,argNum_newCnt.get(c0)+1);
						argNum_newCnt.put(c1,argNum_newCnt.get(c1)-1);
						argNum_newCnt.put(c2,argNum_newCnt.get(c2)-1);
						
						if (!sit1.hasNext()) break;
						if (!sit2.hasNext()) break;						
						
						pid1=sit1.next(); pid2=sit2.next();
					}
					else if (pid1.compareTo(pid2)<0) {
						while (sit1.hasNext()) {
							pid1=sit1.next();
							if (pid1.compareTo(pid2)>=0) break;
						}
						if (pid1.compareTo(pid2)<0) break;
					}
					else {
						while (sit2.hasNext()) {
							pid2=sit2.next();
							if (pid1.compareTo(pid2)<=0) break;
						}
						if (pid1.compareTo(pid2)>0) break;
					}
				}
				
				iit=argNum_newCnt.keySet().iterator();
				while (iit.hasNext()) {
					int an=iit.next();
					int c=argNum_newCnt.get(an);
					if (c>0) {
						score+=Utils.xlogx(c);
						score-=ParseParams.priorNumParam_;
					}
				}				
				iit=ac1.argNum_cnt_.keySet().iterator();
				while (iit.hasNext()) {
					int an=iit.next();
					int c=ac1.argNum_cnt_.get(an);
					if (c>0) {
						score-=Utils.xlogx(c);
						score+=ParseParams.priorNumParam_;
					}
				}
				iit=ac2.argNum_cnt_.keySet().iterator();
				while (iit.hasNext()) {
					int an=iit.next();
					int c=ac2.argNum_cnt_.get(an);
					if (c>0) {
						score-=Utils.xlogx(c);
						score+=ParseParams.priorNumParam_;
					}
				}
				
				// type
				Map<Integer,Integer> atc1=ac1.argTypeIdx_cnt_;
				Map<Integer,Integer> atc2=ac2.argTypeIdx_cnt_;
				if (atc1.size()<=atc2.size()) {
					iit=atc1.keySet().iterator();
					while (iit.hasNext()) {
						int ati1=iit.next();
						if (atc2.containsKey(ati1)) {
							int cx1=atc1.get(ati1);
							int cx2=atc2.get(ati1);
							score+=Utils.xlogx(cx1+cx2)-Utils.xlogx(cx1)-Utils.xlogx(cx2);
							score+=ParseParams.priorNumParam_;
						}
					}
				}
				else {
					iit=atc2.keySet().iterator();
					while (iit.hasNext()) {
						int ati2=iit.next();
						if (atc1.containsKey(ati2)) {
							int cx1=atc1.get(ati2);
							int cx2=atc2.get(ati2);
							score+=Utils.xlogx(cx1+cx2)-Utils.xlogx(cx1)-Utils.xlogx(cx2);
							score+=ParseParams.priorNumParam_;
						}
					}
				}
										
				
				// clust
				Map<Integer,Integer> ccc1=ac1.chdClustIdx_cnt_;
				Map<Integer,Integer> ccc2=ac2.chdClustIdx_cnt_;
				if (ccc1.size()<=ccc2.size()) {
					iit=ccc1.keySet().iterator();
					while (iit.hasNext()) {
						int cci1=iit.next();
						if (ccc2.containsKey(cci1)) {
							int cx1=ccc1.get(cci1);
							int cx2=ccc2.get(cci1);
							score+=Utils.xlogx(cx1+cx2)-Utils.xlogx(cx1)-Utils.xlogx(cx2);
							score+=ParseParams.priorNumParam_;
						}
					}
				}
				else {
					iit=ccc2.keySet().iterator();
					while (iit.hasNext()) {
						int cci2=iit.next();
						if (ccc1.containsKey(cci2)) {
							int cx1=ccc1.get(cci2);
							int cx2=ccc2.get(cci2);
							score+=Utils.xlogx(cx1+cx2)-Utils.xlogx(cx1)-Utils.xlogx(cx2);
							score+=ParseParams.priorNumParam_;
						}
					}
				}
				
				if (score>maxScore) {
					maxScore=score;
					maxMap=j;
					//ac1_ac2.put(j,i); // TO-DO: should map i to j
					aci2_aci1.put(ai2, ai1);
				}
			}			
			
			if (isMyDebug) {
				Utils.print("\t\tARG2:"+i+">");
				ArgClust ac=clx2.argClusts_.get(acIdxs2[i]);
				Iterator<Integer> itx=ac.argTypeIdx_cnt_.keySet().iterator();
				while (itx.hasNext()) {
					int ati=itx.next();
					int c=ac.argTypeIdx_cnt_.get(ati);
					Utils.print(" "+ArgType.getArgType(ati)+":"+c);
				}
				if (maxMap>0) {
					Utils.print(" ---> ARG1:"+maxMap+">");
					ac=clx1.argClusts_.get(acIdxs1[maxMap]);
					itx=ac.argTypeIdx_cnt_.keySet().iterator();
					while (itx.hasNext()) {
						int ati=itx.next();
						int c=ac.argTypeIdx_cnt_.get(ati);
						Utils.print(" "+ArgType.getArgType(ati)+":"+c);
					}
				}
				Utils.println();
			} // for acidx1						
			
			// assume independent merges; additive score increment
			finalScore+=maxScore-newBaseScore;
		}
		
		// greedy search = heuristic to find alignment
		// comp final score based on map

		finalScore+=deltaNoMergeArgClust;			
		return finalScore;
	} // scoreAlign

	// ----------------------------------------------------- //
	// - merge args in same clust
	// ----------------------------------------------------- //
	double scoreMergeArgs(Clust clust, int ai1, int ai2) {
		double score=0;
		
		// HP
		score-=ParseParams.priorMerge_;
		
		// same clust -> no change to ttlCnt; tac=1+2; ttlPartCnt,argNum_cnt may not -> need to loop thru parts
		/*
		 *  old 1,2: xlx(ttlCnt-ttlPartCnt)-xlx(ttlCnt)+SUM_i(xlx(argnum_cnt_i))
		 *  	+SUM_i(xlx(argtype_cnt_i))+SUM_i(xlx(chdcl_cnt_i))-xlx(tac)
		 *  new: xlx(ttlcnt-new_ttlptcnt)-xlx(ttlcnt)+SUM_i(xlx(new_argnum_cnt_i))
		 *  	+SUM_i(xlx(new_argtype_cnt_i))+SUM_i(xlx(new_chdcl_cnt_i))-xlx(tac1/2)
		 */

		ArgClust ac1=clust.argClusts_.get(ai1);
		ArgClust ac2=clust.argClusts_.get(ai2);
		int tpc=clust.ttlCnt_,tpc1=ac1.partRootTreeNodeIds_.size(),tpc2=ac2.partRootTreeNodeIds_.size();
		int tac1=ac1.ttlArgCnt_, tac2=ac2.ttlArgCnt_;
		score-=(Utils.xlogx(tpc-tpc1)+Utils.xlogx(tpc-tpc2));
		score+=Utils.xlogx(tpc);	// previously subtract two, now just one
		score-=2*(Utils.xlogx(tac1+tac2)-Utils.xlogx(tac1)-Utils.xlogx(tac2));	// argtype & chdcl

		// argNum_cnt
		Map<Integer,Integer> argNum_newCnt=new HashMap<Integer,Integer>();
		Iterator<Integer> iit=ac1.argNum_cnt_.keySet().iterator();
		while (iit.hasNext()) {
			int an=iit.next();
			int cnt=ac1.argNum_cnt_.get(an);
			if (cnt==0) {Utils.println("ERR: "+clust+": "+ac1+" argNum zerocnt "+an); System.exit(-1);}
			score-=Utils.xlogx(cnt);
			if (argNum_newCnt.get(an)==null) argNum_newCnt.put(an, cnt);
			else argNum_newCnt.put(an, argNum_newCnt.get(an)+cnt);
		}
		iit=ac2.argNum_cnt_.keySet().iterator();
		while (iit.hasNext()) {
			int an=iit.next();
			int cnt=ac2.argNum_cnt_.get(an);
			if (cnt==0) {Utils.println("ERR: "+clust+": "+ ac2+" argNum zerocnt "+an); System.exit(-1);}
			score-=Utils.xlogx(cnt);
			if (argNum_newCnt.get(an)==null) argNum_newCnt.put(an, cnt);
			else argNum_newCnt.put(an, argNum_newCnt.get(an)+cnt);
		}
		
		int tpc12=tpc1+tpc2;
		TreeSet<String> ptids1=ac1.partRootTreeNodeIds_;
		TreeSet<String> ptids2=ac2.partRootTreeNodeIds_;
		Iterator<String> sit1=ptids1.iterator();
		Iterator<String> sit2=ptids2.iterator();
		String pid1=sit1.next(), pid2=sit2.next();
		while (true) {
			if (pid1.equals(pid2)) {
				// new cnt
				int c1=Part.getPartByRootNodeId(pid1).argClustIdx_argIdxs_.get(ai1).size();
				int c2=Part.getPartByRootNodeId(pid2).argClustIdx_argIdxs_.get(ai2).size();
				int c0=c1+c2;
				tpc12--;
				if (argNum_newCnt.get(c0)==null) argNum_newCnt.put(c0,1);
				else argNum_newCnt.put(c0,argNum_newCnt.get(c0)+1);
				int c=argNum_newCnt.get(c1);
				if (c==1) argNum_newCnt.remove(c1);
				else argNum_newCnt.put(c1,c-1);
				c=argNum_newCnt.get(c2);
				if (c==1) argNum_newCnt.remove(c2);
				else argNum_newCnt.put(c2,c-1);
				
				if (!sit1.hasNext()) break;
				if (!sit2.hasNext()) break;						
				
				pid1=sit1.next(); pid2=sit2.next();
			}
			else if (pid1.compareTo(pid2)<0) {
				while (sit1.hasNext()) {
					pid1=sit1.next();
					if (pid1.compareTo(pid2)>=0) break;
				}
				if (pid1.compareTo(pid2)<0) break;
			}
			else {
				while (sit2.hasNext()) {
					pid2=sit2.next();
					if (pid1.compareTo(pid2)<=0) break;
				}
				if (pid1.compareTo(pid2)>0) break;
			}
		}
		score+=Utils.xlogx(tpc-tpc12);		
		iit=argNum_newCnt.keySet().iterator();
		while (iit.hasNext()) {
			int an=iit.next();
			int c=argNum_newCnt.get(an);
			score+=Utils.xlogx(c);					
		}
		score+=(ac1.argNum_cnt_.size()+ac2.argNum_cnt_.size()-argNum_newCnt.size())*ParseParams.priorNumParam_;	// argnum/absent params
		
		// type
		Map<Integer,Integer> atc1=ac1.argTypeIdx_cnt_;
		Map<Integer,Integer> atc2=ac2.argTypeIdx_cnt_;
		if (atc1.size()<=atc2.size()) {
			iit=atc1.keySet().iterator();
			while (iit.hasNext()) {
				int ati1=iit.next();
				if (atc2.containsKey(ati1)) {
					int cx1=atc1.get(ati1);
					int cx2=atc2.get(ati1);
					if (cx1==0 || cx2==0) {Utils.println("ERR: "+clust+": "+ac1+" "+ac2+" argType zerocnt"); System.exit(-1);}
					score+=Utils.xlogx(cx1+cx2)-Utils.xlogx(cx1)-Utils.xlogx(cx2);
					score+=ParseParams.priorNumParam_;
				}
			}
		}
		else {
			iit=atc2.keySet().iterator();
			while (iit.hasNext()) {
				int ati2=iit.next();
				if (atc1.containsKey(ati2)) {
					int cx1=atc1.get(ati2);
					int cx2=atc2.get(ati2);
					if (cx1==0 || cx2==0) {Utils.println("ERR: "+clust+": "+ac1+" "+ac2+" argType zerocnt"); System.exit(-1);}
					score+=Utils.xlogx(cx1+cx2)-Utils.xlogx(cx1)-Utils.xlogx(cx2);
					score+=ParseParams.priorNumParam_;
				}
			}
		}							
		
		// clust
		Map<Integer,Integer> ccc1=ac1.chdClustIdx_cnt_;
		Map<Integer,Integer> ccc2=ac2.chdClustIdx_cnt_;
		if (ccc1.size()<=ccc2.size()) {
			iit=ccc1.keySet().iterator();
			while (iit.hasNext()) {
				int cci1=iit.next();
				if (ccc2.containsKey(cci1)) {
					int cx1=ccc1.get(cci1);
					int cx2=ccc2.get(cci1);
					if (cx1==0 || cx2==0) {Utils.println("ERR: "+clust+": "+ac1+" "+ac2+" chdClust zerocnt"); System.exit(-1);}
					score+=Utils.xlogx(cx1+cx2)-Utils.xlogx(cx1)-Utils.xlogx(cx2);
					score+=ParseParams.priorNumParam_;
				}
			}
		}
		else {
			iit=ccc2.keySet().iterator();
			while (iit.hasNext()) {
				int cci2=iit.next();
				if (ccc1.containsKey(cci2)) {
					int cx1=ccc1.get(cci2);
					int cx2=ccc2.get(cci2);
					if (cx1==0 || cx2==0) {Utils.println("ERR: "+clust+": "+ac1+" "+ac2+" chdClust zerocnt"); System.exit(-1);}
					score+=Utils.xlogx(cx1+cx2)-Utils.xlogx(cx1)-Utils.xlogx(cx2);
					score+=ParseParams.priorNumParam_;
				}
			}
		}		
		
		return score;
	}
}
