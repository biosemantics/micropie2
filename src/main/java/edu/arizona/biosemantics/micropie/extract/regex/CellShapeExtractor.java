package edu.arizona.biosemantics.micropie.extract.regex;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import au.com.bytecode.opencsv.CSVReader;

public class CellShapeExtractor extends AbstractCharacterValueExtractor {

	public CellShapeExtractor(@Named("CellShapeExtractor_Label")ILabel label) {
		super(label, "Cell shape");
	}
	
	public CellShapeExtractor(@Named("CellShapeExtractor_Label")ILabel label, 
			@Named("CellShapeExtractor_Character")String character) {
		super(label, character);
	}

	@Override
	public Set<String> getCharacterValue(String text) {
		// TODO Auto-generated constructor stub
		Set<String> output = new HashSet<String>(); // Output,
		// format::List<String>

		List<String> keywordList = new ArrayList<String>();
		
		InputStream inputStream;
		try {
			inputStream = new FileInputStream("c2_shape.csv");
			CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
		    List<String[]> lines = reader.readAll();
			for(String[] line : lines)
				keywordList.add(line[0]);
		    reader.close();			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String returnString = "";	
		//System.out.println("Sent :" + sent);
		text = text.substring(0, text.length()-1);
		text = " " + text + " ";		

		String characterName = keywordList.get(0).toString();
		for (String keywordString : keywordList) {
			keywordString = keywordString.toLowerCase();
			String patternString = "\\s"+keywordString+"\\,|\\s"+keywordString+"\\s|^"+keywordString+"\\s|^"+keywordString+"\\,"; // !?!?
			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(text.toLowerCase());
				
			if (matcher.find()) {
				// System.out.print(matcher.group() + "\n");
				// System.out.println("Sent : " + text + " contains :" + characterName + "::" + keywordString);
				output.add(matcher.group().trim());			
			}
		}

		return output;
	}
}
