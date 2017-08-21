package edu.arizona.biosemantics.micropie.seq;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import au.com.bytecode.opencsv.CSVWriter;
import edu.arizona.biosemantics.micropie.io.FileReaderUtil;


/**
 * generate training files from sentences
 * @author maojin
 *
 */
public class TrainFileGenerator {
	
	public static void main(String[] args){
		TrainFileGenerator tfg = new TrainFileGenerator();
		tfg.transform("C:\\micropie\\sentences\\geographical location.txt", "C:\\micropie\\geographic");
	}
	
	
	public void transform(String sentenceFile, String folder){
		List<String> sentences = FileReaderUtil.readFileLines(sentenceFile);
		int fileNumer = 1;
		Set<String> fileContents = new HashSet();
		
		for(String line: sentences){
			line = line.trim();
			if(!fileContents.contains(line)){
				fileContents.add(line);
				String numberAsString = String.valueOf(fileNumer);
				System.out.println(numberAsString);
				String fileName = "0000".substring(numberAsString.length()) + numberAsString+".txt";
				String fileName2 = "0000".substring(numberAsString.length()) + numberAsString+".ann";
				String file = folder+"/"+fileName;
				String file2 = folder+"/"+fileName2;
				try {
//					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true), "UTF8"));
//					writer.write(line);
//					writer.close();
//					
					BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file2,true), "UTF8"));
					writer2.write("");
					writer2.close();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				fileNumer++;
			}
		}
	}

}
