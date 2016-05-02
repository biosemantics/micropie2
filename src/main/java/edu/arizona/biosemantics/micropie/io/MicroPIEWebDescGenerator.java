package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * Generate Descriptions that can be used by MicroPIEWeb
 * @author maojin
 *
 */
public class MicroPIEWebDescGenerator {
	
	public void splitFile(String inputFile, String outputFolder){
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
			String strLine;
			String genus = null;
			String species = null;
			String descriptions = null;
			FileWriter fw = null;
			int i=0;
			//fw = new FileWriter(outputFolder+"/"+taxonName+".txt");
			fw = new FileWriter(outputFolder+"/all-in-one.txt");
			while ((strLine = br.readLine()) != null) {
				strLine = strLine.trim();
				if(strLine.startsWith("Taxon name:")){//new file
					String taxonName = strLine.replace("Taxon name:", "").trim();
					System.out.println("taxonName="+taxonName);
					//fw = new FileWriter(outputFolder+"/"+taxonName+".txt");
					String[] fields = taxonName.split("[\\s]+");
					genus = fields[0];
					if(fields.length>1) {
						species = fields[1];
					}else{
						species = "";
					}
				}else if(strLine.startsWith("Description:")){
					descriptions = strLine.replace("Description:", "").trim();
					i++;
					fw.write("author: anonymous\n");
					fw.write("year: 2012\n");
					fw.write("title: anonymous\n");
					fw.write("genus name: "+genus+"\n");
					fw.write("species name: "+species+"\n");
					fw.write("morphology: "+descriptions+"\n");
					fw.write("\n");
					fw.flush();
					//fw.close();
					//fw = new FileWriter(outputFolder+"/"+i+".txt");
				}
			}
			fw.close();
			br.close();
			System.out.println("in total:"+i+" files");
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args){
		String inputFile = "F:\\MicroPIE\\datasets\\carrine_bacteria\\all 177 descriptions.txt";
		String outputFolder = "F:\\MicroPIE\\datasets\\carrine_bacteria\\";//micropieweb
		
		MicroPIEWebDescGenerator microPIEweb = new MicroPIEWebDescGenerator();
		microPIEweb.splitFile(inputFile, outputFolder);
	}

}
