package edu.arizona.biosemantics.micropie.extract.regex;

import java.io.FileInputStream;
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
import edu.arizona.biosemantics.micropie.model.Sentence;

public class GrowthTempMinExtractor extends AbstractCharacterValueExtractor {

	private String celsius_degreeReplaceSourcePattern = "\\s?”C\\s?|\\s?u C\\s?|\\s?°C\\s?|\\s?° C\\s?|\\s?˚C\\s?|\\s?◦C\\s?";
	private String celsius_degreeReplaceTargetPattern = " celsius_degree ";
	
	public String getCelsius_degreeReplaceSourcePattern() {
		return celsius_degreeReplaceSourcePattern;
	}
	
	public String getCelsius_degreeReplaceTargetPattern() {
		return celsius_degreeReplaceTargetPattern;
	}
	
	public GrowthTempMinExtractor(ILabel label) {
		super(label, "Temperature minimum");
	}
	
	@Inject
	public GrowthTempMinExtractor(@Named("GrowthTempMinExtractor_Label")Label label, 
			@Named("GrowthTempMinExtractor_Character")String character) {
		super(label, character);
	}
	
	
	
	@Override
	public Set<String> getCharacterValue(String text) {

		text = text.replaceAll(celsius_degreeReplaceSourcePattern, celsius_degreeReplaceTargetPattern);
		text = text.toLowerCase();
		//System.out.println("Modified sent::" + text);
		
		// input: the original sentnece
		// output: String array?
		Set<String> output = new HashSet<String>(); // Output, format::List<String>
		
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
//					System.out.println("Go to Case 1::");
//					System.out.println("Whloe Sent::" + matcher.group());
//					System.out.println("Part 1::" + matcher.group(1));
//					System.out.println("Part 2::" + matcher.group(2));
//					System.out.println("Part 3::" + matcher.group(3));
//					System.out.println("Part 4::" + matcher.group(4));
					String targetPattern = matcher.group(4);
					
					RangePatternExtractor rangePatternExtractor = new RangePatternExtractor(targetPattern, "celsius_degree");
					String growTempMin = rangePatternExtractor.getRangePatternMinString();
					if ( ! growTempMin.equals("") ) {
						output.add(growTempMin);
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
					output.add(rangePatternExtractor.getRangePatternMinString());
					
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
				// System.out.println("");
				// System.out.println("Go to Case 0::");
				
		}
		return output;
	}
	
	// Example: Growth occurs at 20–50 ˚C, with optimum growth at 37–45 ˚C.
	public static void main(String[] args) throws IOException {
		GrowthTempMinExtractor growthTempMinExtractor = new GrowthTempMinExtractor(Label.c3);	
		
		CSVSentenceReader sourceSentenceReader = new CSVSentenceReader();
		// Read sentence list
		// 
		
		// sourceSentenceReader.setInputStream(new FileInputStream("split-additionalUSPInputs.csv"));
		// List<Sentence> sourceSentenceList = sourceSentenceReader.readSentenceList();
		// System.out.println("sourceSentenceList.size()::" + sourceSentenceList.size());
		
		sourceSentenceReader.setInputStream(new FileInputStream("split-predictions-140311-1.csv"));
		List<Sentence> sourceSentenceList = sourceSentenceReader.readSentenceList();
		sourceSentenceReader.setInputStream(new FileInputStream("split-predictions-140528-3.csv"));
		sourceSentenceList.addAll(sourceSentenceReader.readSentenceList());
		
		int sampleSentCounter = 0;
		int extractedValueCounter = 0;
		
		for (Sentence sourceSentence : sourceSentenceList) {
			String sourceSentText = sourceSentence.getText();
			
			sourceSentText = sourceSentText.replaceAll(growthTempMinExtractor.getCelsius_degreeReplaceSourcePattern(), growthTempMinExtractor.getCelsius_degreeReplaceTargetPattern());

			
			// ˚C
			//if (sourceSentText.contains("celsius_degree") || sourceSentText.contains("temperature") ) {
			//if (sourceSentText.contains("˚C") || sourceSentText.contains("temperature") || sourceSentText.contains("pH") || sourceSentText.contains("NaCl")) {
			
			if (
					( 
					sourceSentText.matches("(.*)(\\bcelsius_degree\\b)(.*)") || 
					sourceSentText.matches("(.*)(\\btemperature\\b)(.*)") 
					)
				) {	
				
				//System.out.println("\n");
				//System.out.println("sourceSentText::" + sourceSentText);
				Set<String> growTempMinResult = growthTempMinExtractor.getCharacterValue(sourceSentText);
				//System.out.println("growTempMinResult::" + growTempMinResult.toString());
				if ( growTempMinResult.size() > 0 ) {
					extractedValueCounter +=1;
				}
				sampleSentCounter +=1;
			}
		
		}

		//System.out.println("\n");
		//System.out.println("sampleSentCounter::" + sampleSentCounter);
		//System.out.println("extractedValueCounter::" + extractedValueCounter);
		//
	}
}