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
 * copy manual score into automatic score file for comparison
 * 
 * @author maojin
 *
 */
public class ManualScoreVSAutoScore {

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
	public Map<String, String[]> readScoreFile(String scoreFile){
		Map<String, String[]> allAssigns = new HashMap();
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
		    	allAssigns.put(line[2]+"_"+line[3]+"_"+newLine[5], line);
		    }
		    reader.close();
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
	 * read the manual scoring file
	 * 
	 * 
	 * 0 Taxon	1 XML_file	2 Character	3 GSM_Value	4 GSM_Value_Org	5 Extracted_Value	
	 * 6 GSM_NUM	7 EXT_NUM	8 HIT	9 Relaxed_HIT	10 Description
	 * @param scoreFile
	 * @return
	 */
	public List<String[]> readDiffFile(String diffFile){
		List<String[]> lines = null;
		// 0 Taxon	XML_file	Character	GSM_Value	GSM_Value_Org	Extracted_Value	GSM_NUM	EXT_NUM	HIT	Relaxed_HIT	Description
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(diffFile);
			CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
		     lines = reader.readAll();
		     reader.close();
		     System.out.println(diffFile+"  diffValues="+lines.size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
	
	
	/**
	 * copy the manual scoring to the diffFile
	 * @param manualScoreFile
	 * @param diffFile
	 * @param newComFile
	 */
	public void copy(String manualScoreFile, String diffFile, String newComFile){
		Map<String, String[]> manualScoreMap = this.readScoreFile(manualScoreFile);
		//List<String[]> diffValues = this.readDiffFile(diffFile);
		//System.out.println("diffValues="+diffValues.size());
		try{
			FileOutputStream fos = null;
		    fos = new FileOutputStream(new File(newComFile));
			    
			CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(fos, "UTF8")));	
			
			String[] header = new String[15];
			header[0] = "Taxon";
			header[1] = "XML_file";
			header[2] = "Character";
			header[3] = "GSM_Value";
			header[4] = "Extracted_Value";
			header[5] = "GSM_NUM";
			header[6] = "EXT_NUM";
			header[7] = "HIT";
			header[8] = "Relaxed_HIT";
			header[9] = "MAN_HIT";
			header[10] = "MAN_Relaxed_HIT";
			header[11] = "Final_HIT";
			header[12] = "Final_Relaxed_HIT";
			header[13] = "Comment";
			header[14] = "Description";
			writer.writeNext(header);
			//for(String[] oneCharacterLine: diffValues){
			InputStream inputStream = new FileInputStream(diffFile);
			CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
			String[] oneCharacterLine = null;
			while((oneCharacterLine=reader.readNext())!=null){
				// 0 Taxon	XML_file	Character	GSM_Value	GSM_Value_Org	Extracted_Value	
				//6 GSM_NUM	EXT_NUM	HIT	Relaxed_HIT	Description
				String[] newValueLine = new String[15];
				//System.out.println(oneCharacterLine[0]);
				
				
				String[] manScoreLine = manualScoreMap.get(oneCharacterLine[0]+"_"+oneCharacterLine[1]+"_"+oneCharacterLine[2]);
				
				newValueLine[0] = oneCharacterLine[0];//Taxon
				newValueLine[1] = oneCharacterLine[1];//XML_file
				newValueLine[2] = oneCharacterLine[2];//Character
				newValueLine[3] = oneCharacterLine[4];//GSM_Value_Org
				newValueLine[4] = oneCharacterLine[5];//Extracted_Value
				newValueLine[5] = oneCharacterLine[6];//GSM_NUM
				newValueLine[6] = oneCharacterLine[7];//EXT_NUM
				newValueLine[7] = oneCharacterLine[8];//HIT
				newValueLine[8] = oneCharacterLine[9];//Relaxed_HIT
				newValueLine[9] = manScoreLine!=null?manScoreLine[15]:null;//MAN_HIT
				newValueLine[10] = manScoreLine!=null?manScoreLine[16]:null;//MAN_Relaxed_HIT
				newValueLine[11] = null;//Final_HIT
				newValueLine[12] = null;//Final_Relaxed_HIT
				newValueLine[13] = manScoreLine!=null?manScoreLine[13]:null;//comment
				newValueLine[14] = oneCharacterLine[10];//description
				writer.writeNext(newValueLine);
				writer.flush();
			}
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	
	
	public static void main(String[] args){
		ManualScoreVSAutoScore msVSAS = new ManualScoreVSAutoScore();
		String manualScoreFile =  "F:\\MicroPIE\\manuscript\\results\\MicroPIE manual scoring turk v1-lisa all-final(with NOT SCORED).csv";
		String diffFile =  "F:\\MicroPIE\\manuscript\\results\\111_simplest_nosvm\\charAllDifEvalResult-correct.csv";
		String newComFile =  "F:\\MicroPIE\\manuscript\\results\\111_simplest_nosvm\\MicroPIE NO SVM For Manual Scoring.csv";
		msVSAS.copy(manualScoreFile, diffFile, newComFile);
	}
}
