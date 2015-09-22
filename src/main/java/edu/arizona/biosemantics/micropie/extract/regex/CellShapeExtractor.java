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
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.Sentence;
import au.com.bytecode.opencsv.CSVReader;

/**
 * Extract the character 2.1 Cell Shape
 * Sample sentences:
 * 	1. On blood agar, this organism usually appears as an oval bacillus, 0.7 to 2.5 microns long.
 * 	2. On blood agar, the bacilli are single, 1 to 2.5 microns long, about 0.5 microns thick.
 * 
 *	Method:
 *	1.	Keyword
 *	2.  USP
 */
public class CellShapeExtractor extends AbstractCharacterValueExtractor {

	public CellShapeExtractor(@Named("CellShapeExtractor_Label")ILabel label) {
		super(label, "Cell shape");
	}
	
	public CellShapeExtractor(@Named("CellShapeExtractor_Label")ILabel label, 
			@Named("CellShapeExtractor_Character")String character) {
		super(label, character);
	}

	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {

		Set<String> output = new HashSet();
		List<CharacterValue> charValueList = null;
		
		String text = sentence.getText();

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

		charValueList = CharacterValueFactory.createList(this.getLabel(), output);
		return charValueList;
	}
}