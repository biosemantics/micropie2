package edu.arizona.biosemantics.micropie.extract.regex;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
 * Extract the character 3.3 NaCl optimum
 * Sample sentences:
 * 	1. Optimal salt concentration is 1% NaCl, optimal pH is 7.
 * 	2. Colonies growing on agar medium containing 4.0 M NaCl are small (1–2 mm in diameter), entire, smooth, round and orange.
 *	
 *	Method:
 *	1.	Regular Expression
 */
public class GrowthNaclOptimumExtractor extends AbstractCharacterValueExtractor {

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
	
	
	public GrowthNaclOptimumExtractor(ILabel label) {
		super(label, "NaCl optimum");
	}
	
	@Inject
	public GrowthNaclOptimumExtractor(@Named("GrowthNaclOptimumExtractor_Label")Label label, 
			@Named("GrowthNaclOptimumExtractor_Character")String character) {
		super(label, character);
	}
	
	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {

		Set<String> output = new HashSet();
		List<CharacterValue> charValueList = null;
		
		String text = sentence.getText();
		// Add Map<String, String> on Feb 09, 2015 MON
		regexResultWithMappingCaseMap = new HashMap<String, String>();
		
		// Example: optimal temperature is 37°c; optimal temperature is 37˚c; optimum temperature is 37°c; optimum temperature is 37˚c;
		// Another example: String patternString = "(.*)(\\s?optimum\\s?|\\s?optimal\\s?)(.*)(nacl)";
		String patternString = "(.*)" + 
								"(" +
								"(.*)(\\s?\\%\\s?optimum\\s?)|" + // ??
								"(\\s?optimum,\\s?|\\s?optimal,\\s?)" + "(.*)" + "(\\s?\\%\\s?)|" +
								"(\\s?optimum\\s?|\\s?optimal\\s?)" + "(.*)" + "(\\s?\\%\\s?)|" +
								"(\\s?optimum\\s?|\\s?optimal\\s?)" + "(.*)" + "(\\s?nacl\\s?)" +
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
			
			while (targetMatcher.find()) {
			// if (matcher2.find()) { // Just choose the closest one (nearest one, first one)
				String matchPartString2 = targetMatcher.group(1);
				
				// if ( isNextToRightSymbol(matchPartString, matchPartString2, "%") ) {
				//	output.add(matchPartString2);
				// }
				if ( isNextToRightSymbol(matchPartString, matchPartString2, "%") ) {
					output.add(matchPartString2 + " %");
					regexResultWithMappingCaseMap.put("Case 1", matchPartString2 + " %");
				}
				
				/*
				if ( isNextToRightSymbol(matchPartString, matchPartString2, "m") ) {
					output.add(matchPartString2 + " M");
				}
				if ( isNextToRightSymbol(matchPartString, matchPartString2, "% (w/v)") ) {
					output.add(matchPartString2 + " % (w/v)");
				}
				if ( isNextToRightSymbol(matchPartString, matchPartString2, "g/l") ) {
					output.add(matchPartString2 + " g/l");
				}				
				if ( isNextToRightSymbol(matchPartString, matchPartString2, "g per liter") ) {
					output.add(matchPartString2 + " g per liter");
				}
				*/
				
			}
			
		}			
		
		charValueList = CharacterValueFactory.createList(this.getLabel(), output);
		return charValueList;
	}

	public String getUnitString(String targetPattern) {	
		String returnUnitValue = "";
		
		// sourceSentText::the nacl range for growth is 0.3-5.0 % nacl (w/v), with the optimal nacl being 0.6-2.0 %.
		// can tolerate a wide range of salt concentration, from 0 to 30 g/l nacl.
		
		
		if (targetPattern.contains("% (w/v)")) {
			returnUnitValue = "% (w/v)";
		}

		if (targetPattern.contains("% nacl")) {
			if (targetPattern.contains("% nacl (w/v)")) {
				returnUnitValue = "% (w/v)";
			} else {
				returnUnitValue = "%";
			}
		}
		
		if (targetPattern.contains("m nacl")) {
			returnUnitValue = "M";
		}

		if (targetPattern.contains("g/l nacl")) {
			returnUnitValue = "g/l";
		}
		
		if (targetPattern.contains("g per liter")) {
			returnUnitValue = "g per liter";
		}
		
		
		return returnUnitValue;
	}	
	
	public boolean isNextToRightSymbol(String matchPartString, String matchPartString2, String symbol) {
		
		boolean isNextToRightSymbol = false;
		
		String matchPartStringArray[] = matchPartString.split(" ");
		// System.out.println("matchPartStringArray::" + Arrays.toString(matchPartStringArray));
		// System.out.println("matchPartString2::" + matchPartString2);
		for ( int i = 0; i < matchPartStringArray.length; i++ ) {
			if ( matchPartStringArray[i].contains(symbol) && matchPartStringArray[i].contains(matchPartString2) ) {
				// System.out.println("matchPartStringArray[i]::" + matchPartStringArray[i]);
				isNextToRightSymbol = true;
			}
			
			if ( i > 0 && i < matchPartStringArray.length -1 ) {
				if ( matchPartStringArray[i+1].contains(symbol) && matchPartStringArray[i].contains(matchPartString2) ) {
					// System.out.println("matchPartStringArray[i+1]::" + matchPartStringArray[i+1]);
					isNextToRightSymbol = true;
				}
				if ( matchPartStringArray[i-1].contains(symbol) && matchPartStringArray[i].contains(matchPartString2) ) {
					// System.out.println("matchPartStringArray[i-1]::" + matchPartStringArray[i-1]);
					isNextToRightSymbol = true;
				}
			}
		}
		return isNextToRightSymbol;
	}
}