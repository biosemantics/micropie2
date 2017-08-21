package edu.arizona.biosemantics.micropie.extract.crf;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.micropie.io.FileReaderUtil;


/**
 * Parse the geographical names
 * The gazetter is  from the site http://www.geonames.org/.
 * 
 * Indri stopwords list
 * http://www.lemurproject.org/stopwords/stoplist.dft
 */
public class GeoNamesTermList {

	
	public void parseUniqueTerms(String geoNamesFile, String uniqTermList){
		Set uniqTermSet = new HashSet();
		
		InputStream inputStream;
		List sb = new ArrayList();
		try {			
			inputStream = new FileInputStream(geoNamesFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
			String line = null;
			FileWriter fw = new FileWriter(uniqTermList);
			while((line=br.readLine())!=null){
				String[] fields = line.split("\t");
				String[] nameFields = fields[1].split("[\\s]+");
				for(String name:nameFields){
					if(!uniqTermSet.contains(name)){
						uniqTermSet.add(name);
						fw.write(name);
						fw.write("\n");
					}
				}
			}
			br.close();
			fw.flush();
			fw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void parseUniqueTermsHighFreq(String geoNamesFile, String uniqTermList, String stopwordFile,int freq){
		HashMap<String, Integer> uniqTermSet = new HashMap();
		
		InputStream inputStream;
		List sb = new ArrayList();
		try {			
			inputStream = new FileInputStream(geoNamesFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
			String line = null;
			Set<String> stopWords = this.readStopWords(stopwordFile);
			while((line=br.readLine())!=null){
				String[] fields = line.split("\t");
				String[] nameFields = fields[1].split("[\\s]+");
				for(String name:nameFields){
					if(!uniqTermSet.keySet().contains(name)){
						uniqTermSet.put(name, 1);
					}else{
						uniqTermSet.put(name, uniqTermSet.get(name)+1);
					}
				}
			}
			br.close();
			
			FileWriter fw = new FileWriter(uniqTermList);
			
			for(String name:uniqTermSet.keySet()){
				if(uniqTermSet.get(name)>=freq){
					name = name.replace("(", "").replace(")", "").replace(",", "");
					if(name.length()>1&&!stopWords.contains(name.toLowerCase())){
						fw.write(name);
						fw.write("\n");
					}
				
				}
			}
			fw.flush();
			fw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	public Set<String> readStopWords(String stopwordsFile){
		List<String> lineStr = FileReaderUtil.readFileLines(stopwordsFile);
		Set<String> stopWords = new HashSet();
		for(String line : lineStr){
			stopWords.add(line.trim());
		}
		return stopWords;
	}
	
	public static void main(String[] args){
		GeoNamesTermList gntlTool = new GeoNamesTermList();
		String geoNamesFile = "F:\\dataset\\gazetter\\allCountries.txt";//allCountries
		String uniqTermList ="F:\\dataset\\gazetter\\GeoUniqNames_allCountries_2.txt";
		String stopWordFile ="F:\\dataset\\gazetter\\stopwords.txt";
		//gntlTool.parseUniqueTerms(geoNamesFile, uniqTermList);
		gntlTool.parseUniqueTermsHighFreq(geoNamesFile, uniqTermList,stopWordFile,2);
	}
}
