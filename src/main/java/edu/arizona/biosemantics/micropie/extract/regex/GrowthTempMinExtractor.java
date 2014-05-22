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
import edu.arizona.biosemantics.micropie.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.Sentence;

public class GrowthTempMinExtractor extends AbstractCharacterValueExtractor {
	
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

		text = text.replaceAll("\\s?u C\\s?|\\s?°C\\s?|\\s?° C\\s?|\\s?˚C\\s?", " celsius_degree ");
		text = text.toLowerCase();
		System.out.println("Modified sent::" + text);
		
		// input: the original sentnece
		// output: String array?
		Set<String> output = new HashSet<String>(); // Output, format::List<String>
		
		int caseNumber = 0;
		if ( text.contains("temperature range")) {
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
				String patternString = "(.*)(\\s?temperature range\\s?)(.*)";
				
				Pattern pattern = Pattern.compile(patternString);
				Matcher matcher = pattern.matcher(text);

				while (matcher.find()) {
					// System.out.println("Whloe Sent::" + matcher.group());
					// System.out.println("Part 1::" + matcher.group(1));
					// System.out.println("Part 2::" + matcher.group(2));
					// System.out.println("temperature range::" + matcher.group(3));
					String targetPattern = matcher.group(3);
					
					RangePatternExtractor rangePatternExtractor = new RangePatternExtractor(targetPattern);
					String growTempMin = rangePatternExtractor.getRangePatternMin();
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
				
				patternString = "(.*)(\\s?growth\\s?|"
						+ "\\s?grows\\s?"
						+ ")(.*)";
				// patternString = "(.*)(\\s?growth occurs\\s?|"
				//		+ "\\s?grows well\\s?|"
				//		+ "\\s?grows at\\s?|"
				//		+ "\\s?grows at temperatures\\s?"
				//		+ ")(.*)";
				
				pattern = Pattern.compile(patternString);
				matcher = pattern.matcher(text);

				while (matcher.find()) {
					// System.out.println("Whloe Sent::" + matcher.group());
					// System.out.println("Part 1::" + matcher.group(1));
					// System.out.println("Part 2::" + matcher.group(2));
					// System.out.println("Part 3::" + matcher.group(3));
					
					String targetPattern = matcher.group(3);
					System.out.println("targetPattern::" + targetPattern);
					
					int firsrCelsiusIdx = targetPattern.indexOf("celsius_degree");
					if (firsrCelsiusIdx > -1) {
						// System.out.println("firsrCelsiusIdx:" + firsrCelsiusIdx);
						String targetPattern_sub = targetPattern.substring(0, firsrCelsiusIdx);
						// System.out.println("targetPattern_sub::" + targetPattern_sub);
						
						RangePatternExtractor rangePatternExtractor = new RangePatternExtractor(targetPattern);
						String growTempMin = rangePatternExtractor.getRangePatternMin();
						
						if ( ! growTempMin.equals("") ) {
							output.add(growTempMin);
						}						
						/*
						int lastAtIdx = targetPattern_sub.lastIndexOf("at");
						if (lastAtIdx > -1) {
							String targetPattern_sub2 = targetPattern_sub.substring(lastAtIdx, targetPattern_sub.length());
							System.out.println("targetPattern_sub2::" + targetPattern_sub2);
							
							String growTempMin = rangePatternExtractor.getRangePatternMin(targetPattern_sub2);
							if ( ! growTempMin.equals("") ) {
								output.add(growTempMin);
							}								
						}
						*/		
					}					
				}
				break;
			default:
				System.out.println("");
				
		}
		return output;
	}
	
	
	// Example: Growth occurs at 20–50 ˚C, with optimum growth at 37–45 ˚C.
	public static void main(String[] args) throws IOException {
		GrowthTempMinExtractor growthTempMinExtractor = new GrowthTempMinExtractor(Label.c3);	
		
		CSVSentenceReader sourceSentenceReader = new CSVSentenceReader();
		// Read sentence list
		// 
		sourceSentenceReader.setInputStream(new FileInputStream("split-additionalUSPInputs.csv"));
		List<Sentence> sourceSentenceList = sourceSentenceReader.readSentenceList();
		System.out.println("sourceSentenceList.size()::" + sourceSentenceList.size());
		
		int sampleSentCounter = 0;
		int extractedValueCounter = 0;
		
		for (Sentence sourceSentence : sourceSentenceList) {
			String sourceSentText = sourceSentence.getText();
			
			// ˚C
			if (sourceSentText.contains("˚C") || sourceSentText.contains("temperature") ) {
			//if (sourceSentText.contains("˚C") || sourceSentText.contains("temperature") || sourceSentText.contains("pH") || sourceSentText.contains("NaCl")) {
				System.out.println("\n");
				System.out.println("sourceSentText::" + sourceSentText);
				Set<String> growTempMinResult = growthTempMinExtractor.getCharacterValue(sourceSentText);
				System.out.println("growTempMinResult::" + growTempMinResult.toString());
				if ( growTempMinResult.size() > 0 ) {
					extractedValueCounter +=1;
				}
				sampleSentCounter +=1;
			}
		
		}

		System.out.println("\n");
		System.out.println("sampleSentCounter::" + sampleSentCounter);
		System.out.println("extractedValueCounter::" + extractedValueCounter);
		
	}
}