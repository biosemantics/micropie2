package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVWriter;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.extract.ValueFormatterUtil;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;

public class CSVExtValueWriter {
	
	public void write(Map<MultiClassifiedSentence, HashMap> chaMap, String extValueFile){
		CSVWriter writer = null;
		try {
			writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(extValueFile,true), "UTF8")));
			
			List<String[]> lines = new LinkedList<String[]>();
			
			//output for each sentence
			for(MultiClassifiedSentence classifiedSentence : chaMap.keySet()) {
				
				String text = classifiedSentence.getText();
				
				
				HashMap<String, List> valueMap = chaMap.get(classifiedSentence);
				
				for(String character: valueMap.keySet()) {
					List charList = valueMap.get(character);
					String[] line  = new String[3];
					line[1] = character;
					line[0] = text;
					line[2] = ValueFormatterUtil.format(charList);
					lines.add(line);
				}
				
			}
			
			writer.writeAll(lines);
			writer.flush();
			writer.close();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
