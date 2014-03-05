package usp.semantic;

import usp.syntax.ArgType;
import usp.util.Utils;
import java.io.*;

public class MLN {
	// formula/weights implicitly captured in Clust/Part probabilities
	
	public static void printModel(String filePrefix) throws Exception {
		PrintStream out=new PrintStream(filePrefix+".clustering");
		Utils.setOut(out);
		printClustering();
		out.close();
		
		out=new PrintStream(filePrefix+".mln");
		Utils.setOut(out);
		printMLN();
		out.close();
		
		out=new PrintStream(filePrefix+".parse");
		Utils.setOut(out);
		printParse();
		out.close();
	}
	
	static void printClustering() {
		Utils.println("\n\n===== CLUSTERING =====");
		for (Integer ci:Clust.clusts_.keySet()) {
			Clust cl=Clust.getClust(ci);
			if (cl.relTypeIdx_cnt_.size()>1) {
//				Utils.println(cl.clustIdx_+" "+cl.toString());
				Utils.println(""+cl);
			}
		}
	}

	static void printMLN() {
		for (Integer ci:Clust.clusts_.keySet()) {
			Clust cl=Clust.getClust(ci);
			Utils.println(cl.clustIdx_+"\t"+cl.toString());	
			for (Integer aci:cl.argClusts_.keySet()) {				
				ArgClust ac=cl.argClusts_.get(aci);
				Utils.print("\t"+aci+"");
				for (Integer an:ac.argNum_cnt_.keySet()) {
					int cnt=ac.argNum_cnt_.get(an);
					Utils.print("\t"+an+":"+cnt);
				}
				Utils.println();
				Utils.print("\t");
				for (Integer ati:ac.argTypeIdx_cnt_.keySet()) {
					int cnt=ac.argTypeIdx_cnt_.get(ati);
					ArgType at=ArgType.getArgType(ati);					
					Utils.print("\t"+ati+":"+at+":"+cnt);
				}
				Utils.println();
				Utils.print("\t");
				for (Integer cci:ac.chdClustIdx_cnt_.keySet()) {
					int cnt=ac.chdClustIdx_cnt_.get(cci);
					Clust cc=Clust.getClust(cci);					
					Utils.print("\t"+cci+":"+cc+":"+cnt);
				}
				Utils.println();
			}
		}
	}
	
	static void printParse() {
		for (String rnid:Part.rootNodeId_part_.keySet()) {
			Part pt=Part.rootNodeId_part_.get(rnid);
			Utils.println(rnid+"\t"+pt.relTreeRoot_.getTreeStr());
			Utils.println("\t"+pt.clustIdx_+"\t"+Clust.getClust(pt.clustIdx_));			
			if (pt.parPart_==null) {Utils.println("\t");Utils.println("\t");}
			else {
				Argument arg=pt.parPart_.getArgument(pt.parArgIdx_);
				Utils.println("\t"+pt.parPart_.relTreeRoot_.getId()+"\t"+pt.parPart_.clustIdx_+"\t"+Clust.getClust(pt.parPart_.clustIdx_)); // parpart; clust
				Utils.println("\t"+pt.parPart_.getArgClust(pt.parArgIdx_)+"\t"+arg.path_.getArgType()+"\t"+ArgType.getArgType(arg.path_.getArgType())); // arg				
			}
		}		
	}
}
