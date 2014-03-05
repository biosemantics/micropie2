package usp.semantic;

import java.util.*;

import usp.util.*;

public class Agenda {
	// debug
	boolean skipMC_=false;
	boolean skipCompose_=false;
	boolean isDebug_=true;
	
	Parse parse_;
	
	// first try brute-force: add to candidate if neigh are merged
	Map<SearchOp,Set<Integer>> mc_neighs_=new HashMap<SearchOp,Set<Integer>>(); // mc-op -> par/chd - clustIdx as neighbor	
	Map<SearchOp,Integer> compose_cnt_=new HashMap<SearchOp,Integer>();
	
	// agenda
	Set<SearchOp> agendaToScore_=new HashSet<SearchOp>();	
	Map<Integer,Set<SearchOp>> clustIdx_agenda_=new HashMap<Integer,Set<SearchOp>>();
	Map<SearchOp,Double> inactiveAgenda_score_=new HashMap<SearchOp,Double>();	// inactive
	Map<SearchOp,Double> activeAgenda_score_=new HashMap<SearchOp,Double>();	// active
	TreeSet<Pair<Double,SearchOp>> scoreActiveAgenda_=new TreeSet<Pair<Double,SearchOp>>();	// heap for active list	
	
	//
//	Map<Integer,Set<Pair<Double,SearchOp>>> clustIdx_scoreActiveAgenda_=new HashMap<Integer, Set<Pair<Double,SearchOp>>>();

	double minAbsCntObserved_=ParseParams.minAbsCnt_*(ParseParams.minAbsCnt_-1)/2;	// counts equate actual cnt chose 2	
	
	public Agenda(Parse parse) {
		parse_=parse;
	}
	
	public void createAgenda() {
//		util.Timer timer=new util.Timer();
		
		Utils.println("<INFO> Create initial agenda ... clust="+Clust.clusts_.size());
		Iterator<Integer> iit=Part.getClustPartRootNodeIds().keySet().iterator();
		while (iit.hasNext()) {
			int clustIdx=iit.next();
			Clust clust=Clust.getClust(clustIdx);
			
			// filter
			if (clust.getType()!='C') continue;	// non-content word doesn't count
			if (clust.isStop()) continue;
			addAgendaForNewClust(clustIdx);			
		}
	}
	
	public void procAgenda() {
		Utils.println("<ProcAgenda> ...");
		Utils.println("<INFO> Initial agenda MC="+mc_neighs_.size()+" ABS="+compose_cnt_.size());
		Utils.println("<INFO> Initial agenda to score="+agendaToScore_.size());

		usp.util.Timer timer = new usp.util.Timer();
		
		
		long time;
		int ttlAgendaScored=0, ttlExecMC=0, ttlExecAbs=0;
		long ttlScoreTime=0, ttlExecMCTime=0, ttlExecAbsTime=0;

		while (true) {
			timer.timerStart();
			int as=0;
			for (SearchOp op: agendaToScore_) {
				double score=parse_.scorer_.scoreOp(op);
				Utils.println("<Score> "+op+" score="+(score));
				as++;
				
				// TO-DO
				if (score<-200) continue;
				
				addAgenda(op,score);
			}
			agendaToScore_.clear();
			
			time=timer.getTimeElapsed();
//			Utils.println("<TIME> score "+as+" agenda "+((time/1000))+" s");											
			ttlAgendaScored+=as;
			ttlScoreTime+=time;
					
			if (scoreActiveAgenda_.isEmpty()) break;			

			// execute
			timer.timerStart();
			Pair<Double,SearchOp> so=scoreActiveAgenda_.last();
			double score=so.getFirst();
			SearchOp op=so.getSecond();
			Utils.println("<EXEC> "+op+" "+score);
			int newClustIdx=parse_.executor_.executeOp(op);
			updateAgendaAfterExec(op,newClustIdx);
			time=timer.getTimeElapsed();
			if (op.op_==SearchOp.OP_COMPOSE_) {
				ttlExecAbs++;
				ttlExecAbsTime+=time;
			}
			else if (op.op_==SearchOp.OP_MERGE_CLUST_) {
				ttlExecMC++;
				ttlExecMCTime+=time;
			}
//			Utils.println("<TIME> Exec "+op+" ("+time+"ms)");
		} //while true
		
		Utils.println("<INFO> Agenda scored="+ttlAgendaScored+" ("+((ttlScoreTime/1000))+"s)");
		Utils.println("<INFO> Agenda MC executed="+ttlExecMC+" ("+((ttlExecMCTime/1000))+"s)");
		Utils.println("<INFO> Agenda Compose executed="+ttlExecAbs+" ("+((ttlExecAbsTime/1000))+"s)");
	}//procagenda
	
	void addAgendaAfterMergeClust(Part p1, Part p2) {
		int clustIdx=p1.clustIdx_;
		assert clustIdx==p2.clustIdx_;
		
		// merge pars/absorb
		if (p1.getParPart()!=null && p2.getParPart()!=null) {
			Part pp1=p1.getParPart();
			int clustIdx1=pp1.clustIdx_;
			Part pp2=p2.getParPart();
			int clustIdx2=pp2.clustIdx_;
			if (clustIdx1!=clustIdx2) addAgendaMC(clustIdx1,clustIdx2,2*clustIdx+1);
			else addAgendaAbs(clustIdx1,clustIdx);
		}
		
		// merge chd/absorb?
		Map<Integer,Argument> chs1=p1.getArguments();
		Map<Integer,Argument> chs2=p2.getArguments();
		for (Integer i1:chs1.keySet()) { 
			Argument c1=chs1.get(i1);
			Part cp1=c1.argPart_;
			int clustIdx1=cp1.clustIdx_;
			for (Integer i2:chs2.keySet()) {
				Argument c2=chs2.get(i2);
				Part cp2=c2.argPart_;
				int clustIdx2=cp2.clustIdx_;
				if (clustIdx1!=clustIdx2) addAgendaMC(clustIdx1,clustIdx2,2*clustIdx);
				else addAgendaAbs(clustIdx,clustIdx1);
			}
		}
	}
	
	boolean moveAgendaToScore(SearchOp op) {		
		assert activeAgenda_score_.containsKey(op)||inactiveAgenda_score_.containsKey(op);	
		if (agendaToScore_.contains(op)) return true;
		
		if (activeAgenda_score_.containsKey(op)) {			
			Double score=activeAgenda_score_.get(op);
			Pair<Double,SearchOp> so=new Pair<Double,SearchOp>(score,op);
			scoreActiveAgenda_.remove(so);
			activeAgenda_score_.remove(op);
			agendaToScore_.add(op);
			return true;
		}
		else if (inactiveAgenda_score_.containsKey(op)) {			
			inactiveAgenda_score_.remove(op);
			agendaToScore_.add(op);
			return true;
		} 
		
		return false;
	}
	void addAgendaMC(int clustIdx1, int clustIdx2, int neighType) {
		if (skipMC_ || clustIdx1==clustIdx2) return;
		
		char type1=Clust.getClust(clustIdx1).getType();
		char type2=Clust.getClust(clustIdx2).getType();		
		if (type1!=type2) return;	// must match type to be merged (content vs func)
		
		// ignore non-content merge for now: subsume by arg alignment
		if (type1!='C') return;
				
		SearchOp op=new SearchOp();
		op.op_=SearchOp.OP_MERGE_CLUST_;
		if (clustIdx1<clustIdx2) {
			op.clustIdx1_=clustIdx1;
			op.clustIdx2_=clustIdx2;
		}
		else {
			op.clustIdx1_=clustIdx2;
			op.clustIdx2_=clustIdx1;
		}
		
		if (moveAgendaToScore(op)) return;	// already in agenda 
		
		Set<Integer> nts=mc_neighs_.get(op);
		if (nts==null) {
			nts=new HashSet<Integer>();
			mc_neighs_.put(op,nts);
		}
		if (nts.size()+1>=ParseParams.minMCCnt_) {
			agendaToScore_.add(op);
			mc_neighs_.remove(op);
		}
		else nts.add(neighType);
	}
	
	void addAgendaAbs(int parClustIdx, int chdClustIdx) {
		if (skipCompose_) return;
		
		SearchOp op=new SearchOp();
		op.op_=SearchOp.OP_COMPOSE_;
		op.parClustIdx_=parClustIdx;
		op.chdClustIdx_=chdClustIdx;

		if (moveAgendaToScore(op)) return;
		
		if (compose_cnt_.get(op)==null) compose_cnt_.put(op, 1);
		else if (compose_cnt_.get(op)+1 >= minAbsCntObserved_) {	// counted square
			compose_cnt_.remove(op);
			agendaToScore_.add(op);
		}
		else compose_cnt_.put(op, compose_cnt_.get(op)+1);
	}

	void updateAgendaAfterExec(SearchOp op,int newClustIdx) {		
		if (isDebug_) Utils.println("update after exec: "+op+" newci="+newClustIdx+" "+Clust.getClust(newClustIdx));
		
		// remove op from agenda
		removeAgenda(op);		
		
		if (newClustIdx<0) return;	

		// update affected agenda
		if (op.op_==SearchOp.OP_MERGE_CLUST_) updateAgendaAfterExecMC(op,newClustIdx);			
		else if (op.op_==SearchOp.OP_COMPOSE_) updateAgendaAfterExecAbs(op,newClustIdx);	
	}
	
	void addAgendaToScore(SearchOp op) {
		if (isDebug_) Utils.println("<Add agenda to score> "+op);
		agendaToScore_.add(op);
	}
	
	void updateAgendaAfterExecMC(SearchOp op,int newClustIdx) {	
		assert (op.op_==SearchOp.OP_MERGE_CLUST_);
//		if (isDebug_) Utils.println("updateAgendaAfterExecMC: "+op+" newclustidx="+newClustIdx+" "+Clust.getClust(newClustIdx));		
		
		// update affected agenda		
		int oldClustIdx=op.clustIdx2_;
		if (oldClustIdx==newClustIdx) oldClustIdx=op.clustIdx1_;

		while (!clustIdx_agenda_.get(oldClustIdx).isEmpty()) {
			SearchOp oop=clustIdx_agenda_.get(oldClustIdx).iterator().next();
			removeAgenda(oop);
			if (oop.op_==SearchOp.OP_MERGE_CLUST_) {				
				int ci1=oop.clustIdx1_, ci2=oop.clustIdx2_;
				if (ci1==oldClustIdx) ci1=newClustIdx;
				if (ci2==oldClustIdx) ci2=newClustIdx;
				if (ci1!=ci2) {
					SearchOp nop=oop;
					if (ci1<ci2) {
						nop.clustIdx1_=ci1;
						nop.clustIdx2_=ci2;
					}
					else {
						nop.clustIdx1_=ci2;
						nop.clustIdx2_=ci1;
					}
					nop.genString();
					addAgendaToScore(nop);
				}
			}
			else if (oop.op_==SearchOp.OP_COMPOSE_) {				
				int ci1=oop.parClustIdx_, ci2=oop.chdClustIdx_;
				if (ci1==oldClustIdx) ci1=newClustIdx;
				if (ci2==oldClustIdx) ci2=newClustIdx;
				SearchOp nop=oop;
				nop.parClustIdx_=ci1;
				nop.chdClustIdx_=ci2;
				nop.genString();
				addAgendaToScore(nop);
			} 
		}
		clustIdx_agenda_.remove(oldClustIdx);
		
		// add new agenda enabled by merge
		//  -> already did new-new, old-old at createAgenda; so only did new/old
		for (String prnid:Part.getClustPartRootNodeIds().get(newClustIdx)) {
			Part p=Part.getPartByRootNodeId(prnid);				
			for (String prnid2:Part.getClustPartRootNodeIds().get(oldClustIdx)) {
				Part p2=Part.getPartByRootNodeId(prnid2);
				addAgendaAfterMergeClust(p, p2);
			}
		}

	}
	void updateAgendaAfterExecAbs(SearchOp op,int newClustIdx) {	
		assert (op.op_==SearchOp.OP_COMPOSE_);

		// handle affected agenda: any op involving par/chd should be re-evaluated
		int parClustIdx=op.parClustIdx_, chdClustIdx=op.chdClustIdx_;
		
		clustIdx_agenda_.get(parClustIdx).remove(op);
		clustIdx_agenda_.get(chdClustIdx).remove(op);
		while (!clustIdx_agenda_.get(parClustIdx).isEmpty()) {
			SearchOp oop=clustIdx_agenda_.get(parClustIdx).iterator().next();
			removeAgenda(oop);
			oop.genString();
			addAgendaToScore(oop);
		}
		while (!clustIdx_agenda_.get(chdClustIdx).isEmpty()) {
			SearchOp oop=clustIdx_agenda_.get(chdClustIdx).iterator().next();
			removeAgenda(oop);
			oop.genString();
			addAgendaToScore(oop);
		}
		
		// add new agenda enabled by new clust
		addAgendaForNewClust(newClustIdx);
	}
	
	void updateAgendaAfterExecAbs(SearchOp op, int newClustIdx, SearchOp oop) {
		int c1=-1,c2=-1;
		if (oop.op_==SearchOp.OP_MERGE_CLUST_) {
			c1=oop.clustIdx1_;
			c2=oop.clustIdx2_;
		}
		else if (oop.op_==SearchOp.OP_COMPOSE_) {
			c1=oop.parClustIdx_;
			c2=oop.chdClustIdx_;
		}
		if (c1==op.parClustIdx_ || c1==op.chdClustIdx_) c1=newClustIdx;
		if (c2==op.parClustIdx_ || c2==op.chdClustIdx_) c2=newClustIdx;
		if (oop.op_==SearchOp.OP_MERGE_CLUST_) {
			if (c1==c2) return;
			SearchOp nop=new SearchOp();	
			if (c1<c2) {
				nop.clustIdx1_=c1;
				nop.clustIdx2_=c2;
			}
			else {
				nop.clustIdx1_=c2;
				nop.clustIdx2_=c1;
			}
			nop.op_=oop.op_;
			addAgendaToScore(nop);
			return;
		}
		else if (oop.op_==SearchOp.OP_COMPOSE_) {
			SearchOp nop=new SearchOp();			
			nop.parClustIdx_=c1;
			nop.chdClustIdx_=c2;
			nop.op_=oop.op_;
			addAgendaToScore(nop);
		}
	}	

	// add new agenda enabled by parts in the new clust
	void addAgendaForNewClust(int newClustIdx) {
		//if (isDebug_) Utils.println("add");
		TreeSet<String> prnids=Part.getClustPartRootNodeIds().get(newClustIdx);
		if (prnids.size()>1) {
//			if (isDebug_) Utils.println("\tClust "+newClustIdx+" "+Clust.getClust(newClustIdx)+": "+prnids.size());
			for (String prnid:prnids) {
				Part p=Part.getPartByRootNodeId(prnid);				
				for (String prnid2:prnids) {
					if (prnid.compareTo(prnid2)<=0) break;					
					Part p2=Part.getPartByRootNodeId(prnid2);
					addAgendaAfterMergeClust(p, p2);
				}
			}
		}
	}

	void removeAgenda(SearchOp op) {
		if (isDebug_) Utils.println("<Remove Agenda> "+op);
		assert activeAgenda_score_.containsKey(op) || inactiveAgenda_score_.containsKey(op);
		if (activeAgenda_score_.containsKey(op)) {
			Double score=activeAgenda_score_.get(op);
			Pair<Double,SearchOp> sa=new Pair<Double,SearchOp>(score,op);
			scoreActiveAgenda_.remove(sa);
			activeAgenda_score_.remove(op);
		}
		else if (inactiveAgenda_score_.containsKey(op)) inactiveAgenda_score_.remove(op);

		if (op.op_==SearchOp.OP_MERGE_CLUST_) {
			int ci1=op.clustIdx1_, ci2=op.clustIdx2_;
			clustIdx_agenda_.get(ci1).remove(op);
			clustIdx_agenda_.get(ci2).remove(op);
		}
		else if (op.op_==SearchOp.OP_COMPOSE_) {
			int ci1=op.parClustIdx_, ci2=op.chdClustIdx_;
			clustIdx_agenda_.get(ci1).remove(op);
			clustIdx_agenda_.get(ci2).remove(op);
		} 
	}
	void addAgenda(SearchOp op, Double score) {		
		if (isDebug_) Utils.println("<Add Agenda> "+op+" "+score);		
		
		int ci1=-1,ci2=-1;
		
		if (op.op_==SearchOp.OP_MERGE_CLUST_) {
			ci1=op.clustIdx1_; ci2=op.clustIdx2_;
		}
		else if (op.op_==SearchOp.OP_COMPOSE_) {
			ci1=op.parClustIdx_; ci2=op.chdClustIdx_;
		}
		
		Set<SearchOp> ops=clustIdx_agenda_.get(ci1);
		if (ops==null) {
			ops=new HashSet<SearchOp>();
			clustIdx_agenda_.put(ci1, ops);
		}
		ops.add(op);
		ops=clustIdx_agenda_.get(ci2);
		if (ops==null) {
			ops=new HashSet<SearchOp>();
			clustIdx_agenda_.put(ci2, ops);
		}
		ops.add(op);
		
		if (score<ParseParams.priorCutOff_) inactiveAgenda_score_.put(op, score);
		else {
			activeAgenda_score_.put(op, score);
			scoreActiveAgenda_.add(new Pair<Double,SearchOp>(score,op));
		}
	}
}
