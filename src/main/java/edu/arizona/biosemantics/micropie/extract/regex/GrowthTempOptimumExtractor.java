package edu.arizona.biosemantics.micropie.extract.regex;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.arizona.biosemantics.micropie.model.Sentence;


/**
 * Extract the character 3.9 Temperature optimum
 * Sample sentences:
 * 	1. Cells are mesophilic (range 15 degree_celsius_1 to 45 degree_celsius_1) with an optimum at 40  degree_celsius_1.
 * 	2. Grows at 20–40  degree_celsius_1 (optimum 35  degree_celsius_1), at pH 5.5–9.0 (optimum pH 6.5) and with 0–1.2 M NaCl (optimum 0–0.68 M NaCl).
 *	
 *	Method:
 *	1.	Regular Expression
 */
public class GrowthTempOptimumExtractor extends AbstractCharacterValueExtractor {

	// Add Map<String, String> on Feb 09, 2015 MON
	private Map<String, String> regexResultWithMappingCaseMap;

	public Map<String, String> getRegexResultWithMappingCaseMap() {
		return regexResultWithMappingCaseMap;
	}

	private String celsius_degreeReplaceSourcePattern = "\\s?”C\\s?|\\s?u C\\s?|\\s?°C\\s?|\\s?° C\\s?|\\s?˚C\\s?|\\s?◦C\\s?";
	private String celsius_degreeReplaceTargetPattern = " celsius_degree ";
	
	public String getCelsius_degreeReplaceSourcePattern() {
		return celsius_degreeReplaceSourcePattern;
	}
	
	public String getCelsius_degreeReplaceTargetPattern() {
		return celsius_degreeReplaceTargetPattern;
	}	
	
	// Add Map<String, String> on Feb 09, 2015 MON
	
	private String myNumberPattern = "(\\d+(\\.\\d+)?)";

	public String getMyNumberPattern() {
		return myNumberPattern;
	}
	
	private String targetPatternString = "(" +
			"(between\\s?|from\\s?)*" +
			myNumberPattern + "(\\s)*(\\()*(±|-|–|and|to|)*(\\s)*" + myNumberPattern + "*(\\))*" + 
			")";

	// private String targetPatternString = "(" +
	//		"\\d+\\.\\d+\\s?-\\s?\\d+\\.\\d+\\s?|" +
	//		"\\d+\\s?-\\s?\\d+\\.\\d+\\s?|" +
	//		"\\d+\\.\\d+\\s?-\\s?\\d+\\s?|" +
	//		"\\d+\\s?-\\s?\\d+\\s?|" +
	//		
	//		"\\d+\\.\\d+\\s?–\\s?\\d+\\.\\d+\\s?|" +
	//		"\\d+\\s?–\\s?\\d+\\.\\d+\\s?|" +
	//		"\\d+\\.\\d+\\s?–\\s?\\d+\\s?|" +
	//		"\\d+\\s?–\\s?\\d+\\s?|" +
	//		
	//		"\\d+\\.\\d+\\s?and\\s?\\d+\\.\\d+\\s?|" +
	//		"\\d+\\s?and\\s?\\d+\\.\\d+\\s?|" +
	//		"\\d+\\.\\d+\\s?and\\s?\\d+\\s?|" +
	//		"\\d+\\s?and\\s?\\d+\\s?|" +
	//		
	//		"\\d+\\.\\d+\\s?\\d+\\.\\d+|" +
	//		"\\d+\\s?\\d+\\.\\d+|" +
	//		"\\d+\\.\\d+\\s?\\d+|" +
	//		"\\d+\\s?\\d+|" +
	//		
	//		"between\\s\\d+\\.\\d+\\sand\\s\\d+\\.\\d+|" +
	//		"between\\s\\d+\\.\\d+\\sand\\s\\d+|" +
	//		"between\\s\\d+\\sand\\s\\d+\\.\\d+|" +
	//		"between\\s\\d+\\sand\\s\\d+|" +
	//		
	//		"\\d+\\.\\d+|" +
	//		"\\d+|" +
	//		"\\d+\\s?" + 
	//		")";
	
	public String getTargetPatternString() {
		return targetPatternString;
	}

	public GrowthTempOptimumExtractor(ILabel label) {
		super(label, "Temperature optimum");
	}
	
	@Inject
	public GrowthTempOptimumExtractor(@Named("GrowthTempOptimumExtractor_Label")Label label, 
			@Named("GrowthTempOptimumExtractor_Character")String character) {
		super(label, character);
	}
	
	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {

		Set<String> output = new HashSet();
		List<CharacterValue> charValueList = null;
		
		String text = sentence.getText();

		text = text.replaceAll("\\s?u C\\s?|\\s?°C\\s?|\\s?° C\\s?|\\s?˚C\\s?|\\s?◦C\\s?", " celsius_degree ");
		text = text.toLowerCase();

		// Add Map<String, String> on Feb 09, 2015 MON
		regexResultWithMappingCaseMap = new HashMap<String, String>();
		
		// Example: optimal temperature is 37°c; optimal temperature is 37˚c; optimum temperature is 37°c; optimum temperature is 37˚c;
		String patternString = "(.*)" + 
								"(" +
								// "\\(\\s(.*)(\\s?celsius_degree\\s?optimum\\s?)|" + // ??
								"(\\s?optimum,\\s?|\\s?optimal,\\s?)" + "(.*)" + "(\\s?celsius_degree\\s?)|" +
								"(\\s?optimum\\s?|\\s?optimal\\s?)" + "(.*)" + "(\\s?celsius_degree\\s?)|" +
								"(.*)(\\s?at\\s?)" + "(.*)" + "(\\s?celsius_degree\\s?with\\s?optimum\\s?)" + 
								")" + 
								"(.*)";


		
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text);

		while (matcher.find()) {
			// System.out.println("Whloe Sent::" + matcher.group());
			// System.out.println("Part 1::" + matcher.group(1));
			//System.out.println("Part 2::" + matcher.group(2));
			// System.out.println("Part 3::" + matcher.group(3));
			String matchPartString = matcher.group(2);
			// System.out.println("targetPattern::" + targetPattern);
			// output.add(targetPattern);
			Pattern targetPattern = Pattern.compile("\\b" + targetPatternString + "\\b");
			Matcher targetMatcher = targetPattern.matcher(matchPartString);
			
			// while (targetMatcher.find()) {
			if (targetMatcher.find()) { // Just choose the closest one (nearest one, first one)
				String matchPartString2 = targetMatcher.group(1);
				output.add(matchPartString2);
				regexResultWithMappingCaseMap.put("Case 1", matchPartString2);

			}
		}			
		
		charValueList = CharacterValueFactory.createList(this.getLabel(), output);
		return charValueList;
	}
	
}



