package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.transform.ITokenizer;

import au.com.bytecode.opencsv.CSVReader;


/**
 * CSVSentenceReader reads Sentences from a CSV-like InputStream
 * @author rodenhausen
 */
public class CSVSentenceReader implements ISentenceReader {
	
	private InputStream inputStream;
	private ITokenizer tokenizer;

	/**
	 * @param tokenizer to use
	 */
	public CSVSentenceReader(ITokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}
	
	/**
	 * @param inputStream to read from
	 */
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	@Override
	public List<Sentence> read() throws IOException {
		List<Sentence> result = new LinkedList<Sentence>();
		//TODO do the xml reading here
		
		//TODO do the compound sentence splitting already here.. that makes sense
		
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		
		CSVReader reader = new CSVReader(bufferedReader);
	    String [] nextLine;
	    while ((nextLine = reader.readNext()) != null) {
	        // nextLine[] is an array of values from the line
	    	result.add(new Sentence(nextLine[7], Label.getEnum(nextLine[0])));
	    }
	    // String line;
		// while ((line = bufferedReader.readLine()) != null) {
		// 	String[] tokens = tokenizer.tokenize(line);
		//	result.add(new Sentence(tokens[7], Label.getEnum(tokens[0])));
		// }
		return result;
	}

}
