package edu.arizona.biosemantics.micropie.extract.regex;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;

/**
 * Extract the character 3.10 Temperature maximum
 * Sample sentences:
 * 	1. Grows at 20–40  degree_celsius_1 (optimum 35  degree_celsius_1), at pH 5.5–9.0 (optimum pH 6.5) and with 0–1.2 M NaCl (optimum 
 * 	2. Grows between pH 7.0 and 10.0, with an optimum at pH 9, and between 30 and 46  degree_celsius_1, with an optimum at 37  
 *	
 *	Method:
 *	1.	Regular Expression
 */
public class GrowthTempMaxExtractor extends AbstractCharacterValueExtractor {
	
	// Add Map<String, String> on Feb 09, 2015 MON
	private Map<String, String> regexResultWithMappingCaseMap;

	public Map<String, String> getRegexResultWithMappingCaseMap() {
		return regexResultWithMappingCaseMap;
	}
	// Add Map<String, String> on Feb 09, 2015 MON
	
	
	private String celsius_degreeReplaceSourcePattern = "\\s?”C\\s?|\\s?u C\\s?|\\s?°C\\s?|\\s?° C\\s?|\\s?˚C\\s?|\\s?◦C\\s?";
	private String celsius_degreeReplaceTargetPattern = " celsius_degree ";
	
	public String getCelsius_degreeReplaceSourcePattern() {
		return celsius_degreeReplaceSourcePattern;
	}
	
	public String getCelsius_degreeReplaceTargetPattern() {
		return celsius_degreeReplaceTargetPattern;
	}
	
	public GrowthTempMaxExtractor(ILabel label) {
		super(label, "Temperature maximum");
	}
	
	@Inject
	public GrowthTempMaxExtractor(@Named("GrowthTempMaxExtractor_Label")Label label, 
			@Named("GrowthTempMaxExtractor_Character")String character) {
		super(label, character);
	}
	
	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {

		Set<String> output = new HashSet();
		List<CharacterValue> charValueList = null;
		
		String text = sentence.getText();

		text = text.replaceAll(celsius_degreeReplaceSourcePattern, celsius_degreeReplaceTargetPattern);
		text = text.toLowerCase();
		//System.out.println("Modified sent::" + text);
		
		// Add Map<String, String> on Feb 09, 2015 MON
		regexResultWithMappingCaseMap = new HashMap<String, String>();

		int caseNumber = 0;
		if ( text.matches("(.*)(temperature(.*)range|temperature range)(.*)")) {
			caseNumber = 1;
		} else if ( text.contains("growth") || 
				text.contains("grows")
				) {
		// } else if ( text.contains("growth occurs") || 
		//			text.contains("grows well") ||
		//			text.contains("grows at") ||
		//			text.contains("grows at temperatures")
		//			) {
			// if ( ! (text.contains("optimal") || text.contains("optimum")) ) {
				// System.out.println("case 2");
				caseNumber = 2;
			// }
		}
		switch(caseNumber) {
			case 1:
				// Example:  ... Temperature range 5-40˚C ..., The temperature range for growth is 18 to 37°C.
				// String patternString = "(.*)(\\s?temperature range\\s?)(.*)";
				String patternString = "(.*)(temperature range|temperature(.*)range for growth)(.*)";
				
				Pattern pattern = Pattern.compile(patternString);
				Matcher matcher = pattern.matcher(text);

				while (matcher.find()) {
					//System.out.println("Go to Case 1::");
					// System.out.println("Whloe Sent::" + matcher.group());
					// System.out.println("Part 1::" + matcher.group(1));
					// System.out.println("Part 2::" + matcher.group(2));
					// System.out.println("Part 3::" + matcher.group(3));
					// System.out.println("Part 4::" + matcher.group(4));
					String targetPattern = matcher.group(4);
					
					RangePatternExtractor rangePatternExtractor = new RangePatternExtractor(targetPattern, "celsius_degree");
					String growTempMax = rangePatternExtractor.getRangePatternMaxString();
					if ( ! growTempMax.equals("") ) {
						output.add(growTempMax);
						regexResultWithMappingCaseMap.put("Case 1", growTempMax);

					}
				}
				break;
			case 2:
				// Example: Growth occurs 42 ˚C with an optimum at 28 30 ˚C.
				// Example: Growth occurs between pH 5.0 and 10.0 (pH 7.0–9.0 optimum) and between 4.0 and 28.0 ˚C (12.0–20.0 ˚C optimum), but not at 36.0 ˚C or higher.
				// Example: °C
				
				// patternString = "(.*)(\\s?growth occurs\\s?)(.*)(˚c)";

				// patternString = "(.*)(\\s?growth\\s?|"
				//		+ "\\s?grows\\s?"
				//		+ ")(.*)";
				
				// patternString = "(.*)(\\s?growth occurs\\s?|"
				//		+ "\\s?grows well\\s?|"
				//		+ "\\s?grows at\\s?|"
				//		+ "\\s?grows at temperatures\\s?"
				//		+ ")(.*)";

				patternString = "(^growth\\s?|"
						+ "^grows\\s?"
						//+ "grows\\s?|"
						//+ "growth\\s?"
						+ ")(.*)";
				
				pattern = Pattern.compile(patternString);
				matcher = pattern.matcher(text);

				while (matcher.find()) {
					// System.out.println("Go to Case 2::");
					// System.out.println("Whloe Sent::" + matcher.group());
					// System.out.println("Part 1::" + matcher.group(1));
					// System.out.println("Part 2::" + matcher.group(2));
					// System.out.println("Part 3::" + matcher.group(3));
					
					String targetPattern = matcher.group(2);
					//System.out.println("targetPattern::" + targetPattern);
					RangePatternExtractor rangePatternExtractor = new RangePatternExtractor(targetPattern, "celsius_degree");
					output.add(rangePatternExtractor.getRangePatternMaxString());
					regexResultWithMappingCaseMap.put("Case 2", rangePatternExtractor.getRangePatternMaxString());

					
					/*
					int firsrCelsiusIdx = targetPattern.indexOf("celsius_degree");
					if (firsrCelsiusIdx > -1) {
						// System.out.println("firsrCelsiusIdx:" + firsrCelsiusIdx);
						String targetPattern_sub = targetPattern.substring(0, firsrCelsiusIdx);
						// System.out.println("targetPattern_sub::" + targetPattern_sub);
						
						RangePatternExtractor rangePatternExtractor = new RangePatternExtractor(targetPattern);
						String growTempMax = rangePatternExtractor.getRangePatternMax();
						
						if ( ! growTempMax.equals("") ) {
							output.add(growTempMax);
						}						
						
						// int lastAtIdx = targetPattern_sub.lastIndexOf("at");
						// if (lastAtIdx > -1) {
						//	String targetPattern_sub2 = targetPattern_sub.substring(lastAtIdx, targetPattern_sub.length());
						//	System.out.println("targetPattern_sub2::" + targetPattern_sub2);
						//	
						//	String growTempMax = rangePatternExtractor.getRangePatternMax(targetPattern_sub2);
						//	if ( ! growTempMax.equals("") ) {
						//		output.add(growTempMax);
						//	}								
						// }
								
					}
					*/
					
				}
				break;
			default:
		}

		charValueList = CharacterValueFactory.createList(this.getLabel(), output);
		return charValueList;
	}
	
}
