package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.Sentence;
import au.com.bytecode.opencsv.CSVReader;


/**
 * CSVSentenceReader reads Sentences from a CSV-like InputStream
 * @author rodenhausen
 */
public class CSVSentenceReader implements ISentenceReader {
	
	private InputStream inputStream;
	
	/**
	 * @param inputStream to read from
	 */
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
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

}
