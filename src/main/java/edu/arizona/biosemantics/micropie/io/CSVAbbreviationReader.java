package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;

import au.com.bytecode.opencsv.CSVReader;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.transform.ITokenizer;

public class CSVAbbreviationReader implements IAbbreviationReader {

	private InputStream inputStream;
	private ITokenizer tokenizer;

	/**
	 * @param tokenizer to use
	 */
	public CSVAbbreviationReader(ITokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}
	
	/**
	 * @param inputStream to read from
	 */
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	@Override
	public LinkedHashMap<String, String> read() throws IOException {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		// String line;
		// while ((line = bufferedReader.readLine()) != null) {
		//	String[] tokens = tokenizer.tokenize(line);
		//	result.put(tokens[0], tokens[1]);
		// }
		CSVReader reader = new CSVReader(bufferedReader);
	    String [] nextLine;
	    while ((nextLine = reader.readNext()) != null) {
	        // nextLine[] is an array of values from the line
	    	result.put(nextLine[0], nextLine[1]);
	    }
		return result;
	}
}
