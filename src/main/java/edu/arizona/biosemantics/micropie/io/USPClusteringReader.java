package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.keyword.KeywordBasedExtractor;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.RawSentence;


/**
 * USPClusteringReader reads lines from a USP.clustering
 * @author elvis
 */

public class USPClusteringReader implements IUSPClusteringReader{

	private InputStream inputStream;
	
	public USPClusteringReader() {
		// TODO Auto-generated constructor stub
	}
	
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	@Override
	public Set<String> read() throws IOException {
		log(LogLevel.INFO, "Reading USP.clustering...");
		Set<String> keywords = new HashSet<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
		String strLine;
		while ((strLine = br.readLine()) != null) {
			keywords.add(strLine);
		}
		br.close();
		log(LogLevel.INFO, "Done reading USP.clustering...");
		return keywords;
	}
	
	public Set<String> getRelatedKeywords(String rootKeyword) throws IOException {
		log(LogLevel.INFO, "Reading USP.clustering...");
		log(LogLevel.INFO, "Retrieving related keywords...");
		Set<String> keywords = new HashSet<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
		
		String strLine;
		while ((strLine = br.readLine()) != null) {
			// System.out.println("strLine::" + strLine);
			Map<String,String> tempMap = readClustrCoreFormToOutputMap(strLine);
			// if (strLine.contains(rootKeyword)) {	
			// }
			boolean isContainingRootKeyword = false;
			for (Map.Entry<String, String> entry : tempMap.entrySet()) {
				// System.out.println("Key : " + entry.getKey() + " Value : "
				//	+ entry.getValue());
				if (entry.getKey().equals(rootKeyword)) {
					// System.out.println("Yes, it contains \"rootKeyword\"!");
					isContainingRootKeyword = true;
				}
			}
			if ( isContainingRootKeyword == true ) {
				for (Map.Entry<String, String> entry : tempMap.entrySet()) {
					// System.out.println("Key : " + entry.getKey() + " Value : "
					//	+ entry.getValue());
					if ( !entry.getKey().equals(rootKeyword)) {
						keywords.add(entry.getKey());
					}
				}
			}
		}
		
		br.close();
		log(LogLevel.INFO, "Done retrieving related keywords...");
		log(LogLevel.INFO, "Done reading USP.clustering...");
		return keywords;
	}

	private Map<String,String> readClustrCoreFormToOutputMap(String clusterCoreForms) {
		Map<String,String> outputMap = new HashMap<String,String>();
			
		// Original code
		// int i=clusterCoreForms.indexOf('\t');
		// String cs=clusterCoreForms.substring(i+1);//[(N:diffusion):1]
		// i=cs.indexOf('(');
		// Original code
		
		// Elvis modified code
		int i = 0;
		String cs = clusterCoreForms;
		i=cs.indexOf('(');
		// Elvis modified code
		while (i>=0) {
			int j=cs.indexOf(':',i);
			String pos=cs.substring(i+1,j);
			int k=cs.indexOf("):",j);
			i=cs.indexOf('(',k);
			String rt=cs.substring(j+1,k);
			// System.out.println("1. pos :: " + pos);
			// System.out.println("2. rt :: " + rt);
			outputMap.put(rt, pos);
			
		}
		return outputMap;
	}	
	
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		USPClusteringReader uspClusteringReader = new USPClusteringReader();
		uspClusteringReader.setInputStream(new FileInputStream("usp_results/usp.clustering"));
		// Set<String> uspClusteringKeywords = uspClusteringReader.read();
		Set<String> uspClusteringKeywords = uspClusteringReader.getRelatedKeywords("hydrolysed");
		System.out.println("uspClusteringKeywords::" + uspClusteringKeywords.toString());
		
	}

}
