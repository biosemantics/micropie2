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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;


/**
 * 1,correct the GSM number
 * 2,Copy manual scoring values
 * 
 * generate a standard file format for evaluation
 * @author maojin
 *
 */
public class ManualScoringResults {
	
	
	/**
	 * read the manual scoring file
	 * 
	 * 
	 * 0-5 WorkerId	SubmitTime	Input.Taxon	Input.XML_file	Input.Character Input.GSM_Value
	   6-10 Input.Extracted_Value	Input.GSM_NUM	Input.EXT_NUM	Input.HIT	Input.Relaxed_HIT
	   11-15 Input.Description	Answer.Character	Answer.Comment	Answer.GSM_corrected	Answer.HIT_corrected
	   16-17 Answer.Relaxed_HIT	Answer.gsm_right
	   
	 * @param scoreFile
	 * @return
	 */
	public List<String[]> readScoreFile(String scoreFile){
		List<String[]> allAssigns = new ArrayList();
		// 0 Taxon	XML_file	Genus	Species	Strain	Character	GSM Value	Extracted Value	GSM_NUM	EXT_NUM	HIT	Relaxed_HIT
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(scoreFile);
			CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
		    List<String[]> lines = reader.readAll();
		    for(int i=1;i<lines.size();i++){
		    	String[] line = lines.get(i);
		    	
		    	String[] newLine = new String[12];
		    	//Taxon,XML_file,Character
		    	newLine[0]=line[2];//taxon
		    	newLine[1]=line[3];//XML_file
		    	newLine[2]=null;//Genus
		    	newLine[3]=null;//Species
		    	newLine[4]=null;//Strain
		    	newLine[5]=line[4];//Character
		    	
		    	String gsmValue = line[14];//GSM Value
		    	if("{}".equals(gsmValue)){
		    		gsmValue="";
		    	}
		    	newLine[6]=gsmValue;
		    	
		    	newLine[7]=line[6];//Extracted Value
		    	
		    	if(gsmValue!=null&&!"".equals(gsmValue)){
		    		newLine[8]=line[14].split("#").length+"";//GSM_NUM
		    	}else{
		    		newLine[8]=0+"";
		    	}
		    	
		    	
		    	newLine[9]=line[8];//EXT_NUM
		    	newLine[10]=line[15];//HIT
		    	newLine[11]=line[16];//Relaxed_HIT
		    	allAssigns.add(newLine);
		    }
		    
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return allAssigns;
	}
	
	/**
	 * update new score file
	 * @param newScoreFile
	 * @param lines
	 */
	public void writeUpdatedScoreFile(String newScoreFile, List<String[]> lines){
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(newScoreFile));
			CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(fos, "UTF8")));	
			String[] header = new String[12];
			header[0] = "Taxon";
			header[1] = "XML file";
			header[2] = "Genus";
			header[3] = "Species";
			header[4] = "Strain";
			header[5] = "Character";
			header[6] = "GSM Value";
			header[7] = "Extracted Value";
			header[8] = "GSM_NUM";
			header[9] = "EXT_NUM";
	    	header[10] = "HIT";
	    	header[11] = "Relaxed_HIT";
			
			//write
	    	writer.writeNext(header);
	    	System.out.println(lines.size());
			writer.writeAll(lines);
			writer.flush();
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * calculate the final score
	 * @param scoreFile
	 * @return
	 */
	public void calculateFinalResult(String finalScoreFile, String resultsFile){
		InputStream inputStream;
		Map<String, Integer> gsmNumMap = new HashMap();
		Map<String, Integer> extNumMap = new HashMap();
		Map<String, Double> hitMap = new HashMap();
		Map<String, Double> relaxedHitMap = new HashMap();
		
		try {
			inputStream = new FileInputStream(finalScoreFile);
			
			CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
		    List<String[]> lines = reader.readAll();
		    for(int i=1;i<lines.size();i++){
		    	String[] newLine = lines.get(i);
		    	String character = newLine[5];//Character
		    	String gsmNumStr = newLine[8];//GSM_NUM
		    	int gsmNum = new Integer(gsmNumStr);
		    	Integer chaGsmNum = gsmNumMap.get(character);
		    	if(chaGsmNum==null){
		    		gsmNumMap.put(character, gsmNum);
		    	}else{
		    		gsmNumMap.put(character, chaGsmNum+gsmNum);
		    	}
		    	
		    	String extNumStr = newLine[9];//EXT_NUM
		    	int extNum = new Integer(extNumStr);
		    	Integer chaExtNum = extNumMap.get(character);
		    	if(chaExtNum==null){
		    		extNumMap.put(character, extNum);
		    	}else{
		    		extNumMap.put(character, chaExtNum+extNum);
		    	}
		    	
		    	
		    	String hitStr = newLine[10];//HIT
		    	double hit = new Double(hitStr);
		    	Double chaHit = hitMap.get(character);
		    	if(chaHit==null){
		    		hitMap.put(character, hit);
		    	}else{
		    		hitMap.put(character, chaHit+hit);
		    	}
		    	
		    	String relaxedHitStr=newLine[11];//Relaxed_HIT
		    	double relaxedHit = new Double(relaxedHitStr);
		    	Double chaRelaxedHit = relaxedHitMap.get(character);
		    	if(chaHit==null){
		    		relaxedHitMap.put(character, relaxedHit);
		    	}else{
		    		relaxedHitMap.put(character, relaxedHit+chaRelaxedHit);
		    	}
		    }
		    
		    
		    FileOutputStream fos = null;
		    fos = new FileOutputStream(new File(resultsFile));
		    
			CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(fos, "UTF8")));	
			String[] header = new String[11];
			header[0] = "character";
			header[1] = "GSM_NUM";
			header[2] = "EXT_NUM";
			header[3] = "HIT";
			header[4] = "RELAXED_HIT";
			header[5] = "P";
			header[6] = "R";
			header[7] = "F1";
			header[8] = "Relaxed_P";
			header[9] = "Relaxed_R";
			header[10] = "Relaxed_F1";
			
		    
			writer.writeNext(header);
		    //calculate P, R, F1 for each character
		    for(String character:gsmNumMap.keySet()){
		    	Integer gsmNum = gsmNumMap.get(character);
		    	Integer extNum = extNumMap.get(character);
		    	Double hit = hitMap.get(character);
		    	Double relaxedHit = relaxedHitMap.get(character);
		    	
		    	double p = hit/extNum;
		    	double r = hit/gsmNum;
		    	double f1 = 2*p*r/(p+r);
		    	
		    	double rp = relaxedHit/extNum;
		    	double rr = relaxedHit/gsmNum;
		    	double rf1 = 2*rp*rr/(rp+rr);
		    	
		    	String[] characterValues = new String[11];
		    	characterValues[0] = character;
		    	characterValues[1] = gsmNum+"";
		    	characterValues[2] = extNum+"";
		    	characterValues[3] = hit+"";
		    	characterValues[4] = relaxedHit+"";
		    	characterValues[5] = p+"";
		    	characterValues[6] = r+"";
		    	characterValues[7] = f1+"";
		    	characterValues[8] = rp+"";
		    	characterValues[9] = rr+"";
		    	characterValues[10] = rf1+"";
		    	
		    	writer.writeNext(characterValues);
		    	//System.out.println(character+" "+p+" "+r+" "+f1+" "+rp+" "+rr+" "+rf1);
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
	
	
	
	public static void main(String[] args){
		//manual score file downloaded from GOOGLE DRIVE
		String mannualScoreFile = "F:\\MicroPIE\\manuscript\\results\\MicroPIE manual scoring turk v1-lisa all-final(without NOT SCORED).csv";
		//manual score file in the format of final assessment
		String mannualScoreFileToCompute = "F:\\MicroPIE\\manuscript\\results\\MicroPIE manual scoring turk v1-lisa all-final for assessment(without NOT SCORED).csv";
		//combine manual and automatic scoring
		//String finalCombinedScoreFile = "F:\\MicroPIE\\manuscript\\results\\simplest final\\MicroPIE manual scoring and automatic scoring 021216.csv";
		//String resultsFile = "F:\\MicroPIE\\manuscript\\results\\MicroPIE 021216 result.csv";
		
		String finalCombinedScoreFile = "F:\\MicroPIE\\manuscript\\results\\studentexp\\MicroPIE manual scoring and automatic scoring + same descriptions.csv";
		String resultsFile = "F:\\MicroPIE\\manuscript\\results\\studentexp\\micropie 46 result.csv";
		ManualScoringResults msr = new ManualScoringResults();
		//List<String[]> lines = msr.readScoreFile(mannualScoreFile);
		//msr.writeUpdatedScoreFile(mannualScoreFileToCompute, lines);
		msr.calculateFinalResult(finalCombinedScoreFile,resultsFile);
	}
}
