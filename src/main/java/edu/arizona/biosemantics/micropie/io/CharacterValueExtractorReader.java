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

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.ExtractorType;
import edu.arizona.biosemantics.micropie.extract.ICharacterValueExtractor;
import edu.arizona.biosemantics.micropie.extract.keyword.KeywordBasedExtractor;
import edu.arizona.biosemantics.micropie.extract.usp.USPBasedExtractor;
import edu.arizona.biosemantics.micropie.extract.usp.USPRequest;


/**
 * create character value extractors according to the configuration files
 * 
 */
public class CharacterValueExtractorReader implements ICharacterValueExtractorReader {

	private String uspResultsDirectory;
	private String uspString;
	
	@Inject
	public CharacterValueExtractorReader(@Named("uspResultsDirectory") String uspResultsDirectory, @Named("uspString") String uspString) {
		this.uspResultsDirectory = uspResultsDirectory;
		this.uspString = uspString;
	}
	
	@Override
	/**
	 * create ValueExtractor from the file name, either USP or Keyword-based
	 * 
	 * Example : 
	 * 	file name: c2.Cell wall.key
	 * 	labelName: c2
	 * 	character: Cell wall
	 *  type:key
	 */
	public ICharacterValueExtractor read(File file) throws Exception {
		String name = file.getName();
		int firstDotIndex = name.indexOf(".");
		
		// Example: file name: c2.Cell wall.key
		
		int lastDotIndex = name.lastIndexOf(".");
		
		String labelName = name.substring(0, firstDotIndex);
		String characterName = name.substring(firstDotIndex + 1, lastDotIndex);
		String type = name.substring(lastDotIndex + 1, name.length());
		
		ExtractorType extractorType = ExtractorType.valueOf(type);
		switch(extractorType) {
		case key:
			return createKeywordBasedExtractor(file, labelName, characterName);
		case usp:
			return createUSPBasedExtractor(file, labelName, characterName);
		default:
			throw new Exception("Could not identify extractor type from file");
		}
	}

	
	/**
	 * Create  value extractor which extracts values by USP
	 * @param file : contains the initial USP request
	 * @param labelName
	 * @param character
	 * @return
	 * @throws IOException
	 */
	private ICharacterValueExtractor createUSPBasedExtractor(File file,
			String labelName, String characterName) throws IOException {
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
		return new USPBasedExtractor(Label.valueOf(labelName), characterName, uspRequests, uspResultsDirectory, uspString);
	}

	
	/**
	 * Create keyword based extractor
	 * @param file: contains the initial keywords
	 * @param labelName
	 * @param character
	 * @return
	 * @throws IOException
	 */
	private ICharacterValueExtractor createKeywordBasedExtractor(File file, String labelName, String characterName) throws IOException {
		Set<String> keywords = new HashSet<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "UTF8"));
		String strLine;
		while ((strLine = br.readLine()) != null) {
			keywords.add(strLine);
		}
		br.close();
		return new KeywordBasedExtractor(Label.valueOf(labelName), characterName, keywords);
	}
}
