package usp.semantic;

public class ParseParams {
	public static int minMCCnt_=10, minAbsCnt_=50;	
	public static double priorCutOff_=10;	// agenda cut off ~ cluster change
	
	public static double priorNumArgComb_=1;
	public static double priorMerge_=0;
	
	public static double priorNumParam_=5;
	public static double priorNumConj_=10;	// penalize merge score if the two appears in conjunction
	
}
