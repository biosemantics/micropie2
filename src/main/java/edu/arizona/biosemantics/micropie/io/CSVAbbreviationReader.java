package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

public class CSVAbbreviationReader implements IAbbreviationReader {

	private InputStream inputStream;
	
	/**
	 * @param inputStream to read from
	 */
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	@Override
	public LinkedHashMap<String, String> read() throws IOException {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();		
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
	    List<String[]> lines = reader.readAll();
		for(String[] line : lines)
			result.put(line[0], line[1]);
	    reader.close();
		return result;
	}
}
