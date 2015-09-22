package edu.arizona.biosemantics.micropie.extract.regex;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * Extract the character 3.5 PH Minimum
 * Sample sentences:
 * 	1. Grows between pH 7.0 and 10.0, with an optimum at pH 9, and between 30 and 46  degree_celsius_1, with an optimum at 37  degree_celsius_1.
 * 	2. Cells were able to grow and produce methane within a temperature range of 23–35 degree_celsius_1 (lower temperatures not tested) and a pH range of 6.5–8.3 on methanol or trimethylamine.
 *	
 *	Method:
 *	1.	Regular Expression
 */
public class GrowthPhMinExtractor extends AbstractCharacterValueExtractor {
	
	private String celsius_degreeReplaceSourcePattern = "\\s?”C\\s?|\\s?u C\\s?|\\s?°C\\s?|\\s?° C\\s?|\\s?˚C\\s?|\\s?◦C\\s?";
	private String celsius_degreeReplaceTargetPattern = " celsius_degree ";
	
	public String getCelsius_degreeReplaceSourcePattern() {
		return celsius_degreeReplaceSourcePattern;
	}
	
	public String getCelsius_degreeReplaceTargetPattern() {
		return celsius_degreeReplaceTargetPattern;
	}
	
	public GrowthPhMinExtractor(ILabel label) {
		super(label, "pH minimum");
	}
	
	@Inject
	public GrowthPhMinExtractor(@Named("GrowthPhMExtractor_Label")Label label, 
			@Named("GrowthPhMinExtractor_Character")String character) {
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
		
		int caseNumber = 0;
		if ( text.matches("(.*)(ph(.*)range|ph range)(.*)")) {
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
				// Example: Temperature and pH ranges for growth are 15–65 celsius_degree (optimum 42–45 celsius_degree ) and pH 0–4 (optimum pH 1.4–1.6).
				// ... Temperature range 5-40˚C ...
				// The temperature range for growth is 18 to 37°C.
				// String patternString = "(.*)(\\s?ph range\\s?)(.*)";
				String patternString = "(.*)(ph range|ph(.*)range for growth)(.*)";
				
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
					
					RangePatternExtractor rangePatternExtractor = new RangePatternExtractor(targetPattern, "ph");
					String growPhMin = rangePatternExtractor.getRangePatternMinString();
					if ( ! growPhMin.equals("") ) {
						output.add(growPhMin);
					}
				}
				break;
			case 2:
				// Example: Growth occurs between 70 and 100 celsius_degree (optimum, 90 to 95 celsius_degree ), at pH 5 to 9 (optimum, pH 7.0), and at 1.8 to 7.0% salinity (optimum, 3.5% salinity).
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
					RangePatternExtractor rangePatternExtractor = new RangePatternExtractor(targetPattern, "ph");
					output.add(rangePatternExtractor.getRangePatternMinString());
					
					
				}
				break;
			default:
				// System.out.println("");
				// System.out.println("Go to Case 0::");
				
		}
		charValueList = CharacterValueFactory.createList(this.getLabel(), output);
		return charValueList;
	}
	
}
