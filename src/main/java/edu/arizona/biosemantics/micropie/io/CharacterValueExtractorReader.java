package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.ExtractorType;
import edu.arizona.biosemantics.micropie.extract.regex.ICharacterValueExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.KeywordBasedExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.USPBasedExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.USPRequest;

public class CharacterValueExtractorReader implements
		ICharacterValueExtractorReader {

	@Override
	public ICharacterValueExtractor read(File file) throws Exception {
		String name = file.getName();
		int firstDotIndex = name.indexOf(".");
		
		
		int lastDotIndex = name.lastIndexOf(".");
		
		String labelName = name.substring(0, firstDotIndex);
		String character = name.substring(firstDotIndex + 1, lastDotIndex);
		String type = name.substring(lastDotIndex + 1, name.length());
		
		ExtractorType extractorType = ExtractorType.valueOf(type);
		switch(extractorType) {
		case key:
			return createKeywordBasedExtractor(file, labelName, character);
		case usp:
			return createUSPBasedExtractor(file, labelName, character);
		default:
			throw new Exception("Could not identify extractor type from file");
		}
	}

	private ICharacterValueExtractor createUSPBasedExtractor(File file,
			String labelName, String character) throws IOException {
		Set<USPRequest> uspRequests = new HashSet<USPRequest>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "UTF8"));
		String strLine;
		while ((strLine = br.readLine()) != null) {
			String[] requestParameters = strLine.split("\t");
			if(requestParameters.length != 4) 
				continue;
			
			// System.out.println("labelName:" + labelName + "::character::" + character);
			
			uspRequests.add(new USPRequest(requestParameters[0], requestParameters[1], requestParameters[2], requestParameters[3]));
		}
		br.close();
		return new USPBasedExtractor(Label.valueOf(labelName), character, uspRequests);
	}

	private ICharacterValueExtractor createKeywordBasedExtractor(File file, String labelName, String character) throws IOException {
		Set<String> keywords = new HashSet<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "UTF8"));
		String strLine;
		while ((strLine = br.readLine()) != null) {
			keywords.add(strLine);
		}
		br.close();
		return new KeywordBasedExtractor(Label.valueOf(labelName), character, keywords);
	}
}
