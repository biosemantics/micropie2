package edu.arizona.biosemantics.micropie.eval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;


/**
 * find what values are missed during manual scoring
 * @author maojin
 *
 */
public class FindMissedAssignments {

	public void findDifferences(){
		String allAssignFile = "F:\\MicroPIE\\manuscript\\results\\simplest final turk hight\\charAllDifEvalResult  google2.csv";
		String finishedAssignFile =  "F:\\MicroPIE\\manuscript\\results\\MicroPIE manual scoring turk v1 lisa.csv";
		Map<String, String[]> allAssignedTaxon = readAllAssign(allAssignFile);
		Map<String, String[]> finishedAssignTaxon = readFinishedAssign(finishedAssignFile);
		
		String missedFile =  "F:\\MicroPIE\\manuscript\\results\\MicroPIE manual scoring turk v1 lisa 3 missed.csv";
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(new File(missedFile));
			CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(fos, "UTF8")));		
			List<String[]> lines = new LinkedList<String[]>();
			for(String key:allAssignedTaxon.keySet()){
				if(!finishedAssignTaxon.containsKey(key)){
					writer.writeNext(allAssignedTaxon.get(key));
				}
			}
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	/**
	 * F:\MicroPIE\manuscript\results\simplest final turk hight\charAllDifEvalResult-org.csv
	 * @param allAssignsFile
	 * @return
	 */
	public Map<String, String[]> readAllAssign(String allAssignsFile){
		Map<String, String[]> allAssigns = new HashMap();
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(allAssignsFile);
			CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
		    List<String[]> lines = reader.readAll();
		    for(int i=1;i<lines.size();i++){
		    	String[] line = lines.get(i);
		    	//Taxon,XML_file,Character
			    String keyStr = new String(line[0].toLowerCase().trim()+line[1].toLowerCase().trim()+line[2].toLowerCase().trim());
			    allAssigns.put(keyStr, line);
			    System.out.println(keyStr);
		    }
		    
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allAssigns;
	}
	
	/**
	 * MicroPIE manual scoring turk v1 lisa.csv
	 * @param finishedFile
	 * @return
	 */
	public Map<String, String[]> readFinishedAssign(String finishedFile){
		Map<String, String[]> allAssigns = new HashMap();
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(finishedFile);
			CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
		    List<String[]> lines = reader.readAll();
		    for(int i=1;i<lines.size();i++){
		    	String[] line = lines.get(i);
		    	//Taxon,XML_file,Character
		    	String keyStr = new String(line[2].toLowerCase().trim()+line[3].toLowerCase().trim()+line[4].toLowerCase().trim());
			    System.out.println("finished="+keyStr);
		    	allAssigns.put(keyStr, line);
		    }
		    
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allAssigns;
	}
	
	
	
	public static void main(String[] args){
		FindMissedAssignments fma = new FindMissedAssignments();
		fma.findDifferences();
	}
	
	
}