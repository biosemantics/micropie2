package edu.arizona.biosemantics.micropie.transform;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.common.log.LogLevel;

public class TermSpliter {
	
	/**
	 * split texts into terms by periods
	 * @param text
	 * @return
	 */
	public Set<String> getTermWithPeriod(String text) {
		Set<String> termWithPeriod = new HashSet();
		
		String textTokens[] = text.split(" ");
		
		for ( int i = 0; i < textTokens.length; i++ ) {
			String tokenString = textTokens[i];
			
			if ( tokenString.matches("(.*)([A-Z]+\\.)(.*)")) {
				//System.out.println("getTermWithPeriod::" + tokenString);
				termWithPeriod.add(tokenString);
			}
			
		}
		
		return termWithPeriod;
	}
	
	public List<String> getTransformedPeriod(List<String> sentences){
		log(LogLevel.INFO, "getTransformedPeriod:: replace \"·\" to \".\" ...");

		List<String> result = new LinkedList<String>();		
		
		for (String sentence : sentences) {
			log(LogLevel.INFO, "replacAll::2:: " + sentence);
			sentence = sentence.replaceAll("\\·", "."); // To avoid the error ClausIE spliter: the dash will disappear
			// https://d5gate.ag5.mpi-sb.mpg.de/ClausIEGate/ClausIEGate?inputtext=Optimal+temperature+and+pH+for+growth+are+25%E2%80%9330+%CB%9AC+and+pH+7%2C+respectively.&processCcAllVerbs=true&processCcNonVerbs=true&type=true&go=Extract
			sentence = sentence.replaceAll("\\s?\\·\\s?", "."); // To avoid the error ClausIE spliter: the dash will disappear
			result.add(sentence);
		}

		log(LogLevel.INFO, "done getTransformedPeriod:: replace \"·\" to \".\". Transformed " + result.size() + " sentences");
		return result;
	}
	
}
