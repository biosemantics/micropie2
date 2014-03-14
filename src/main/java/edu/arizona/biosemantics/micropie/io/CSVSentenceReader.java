package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;


/**
 * CSVSentenceReader reads Sentences from a CSV-like InputStream
 * @author rodenhausen
 */
public class CSVSentenceReader implements ISentenceReader {
	
	private InputStream inputStream;
	private OutputStream outputStream;
	
	/**
	 * @param inputStream to read from
	 */
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}	
	
	@Override
	public List<Sentence> read() throws IOException {
		log(LogLevel.INFO, "Reading sentences...");
		List<Sentence> result = new LinkedList<Sentence>();
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
	    List<String[]> lines = reader.readAll();
		for(String[] line : lines)
			result.add(new Sentence(line[5], Label.getEnum(line[0])));
	    reader.close();
		log(LogLevel.INFO, "Done reading sentences...");
		return result;
	}
	
	
	public void categoryStat() throws IOException {
		log(LogLevel.INFO, "Calculating sentences labels...");

		Map<String, Integer> categoryStat = new HashMap<String,Integer>();
		
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
	    List<String[]> lines = reader.readAll();
		for (String[] line : lines) {
			// result.add(new Sentence(line[5], Label.getEnum(line[0])));
			String key = line[0];
			int value = 1;
			if(categoryStat.containsKey(key)){
				value +=  categoryStat.get(key);
			}
			categoryStat.put(key,value);
		}	
	    reader.close();
	    
	    Iterator it = categoryStat.entrySet().iterator();
	    while (it.hasNext()) {
	    	Map.Entry pairs = (Map.Entry)it.next();
	    	System.out.println(pairs.getKey() + " = " + pairs.getValue());
	    	it.remove(); // avoids a ConcurrentModificationException
	    }
	    
	    
	    log(LogLevel.INFO, "Done calculating sentences labels...");
	}
	


	public void splitCompoundCategory() throws IOException {
		log(LogLevel.INFO, "Spliting compound category...");

		
		
		CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF8")));		
		List<String[]> outputLines = new LinkedList<String[]>();
		
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
	    List<String[]> lines = reader.readAll();
		for (String[] line : lines) {
			String label = line[0];
			if (label.contains(",")) {
				String[] labelArray = label.split(",");
				for (int i = 0; i < labelArray.length; i++) {
					String[] tempLine = line.clone();
					// System.out.println("labelArray[i]::" + labelArray[i]);
					// System.out.println("line[0]::Before::" + line[0]);
					// System.out.println("tempLine[0]::Before::" + tempLine[0]);
					tempLine[0] = labelArray[i];
					// System.out.println("tempLine[0]::After::" + tempLine[0]);
					outputLines.add(tempLine);
					
				}	
			} else {
				outputLines.add(line);
			}
		}	
	    reader.close();
		
		//write
		writer.writeAll(outputLines);
		writer.flush();
		writer.close();	
	    
	    log(LogLevel.INFO, "Done spliting compound category...");
	}
	
	
	
	
}
