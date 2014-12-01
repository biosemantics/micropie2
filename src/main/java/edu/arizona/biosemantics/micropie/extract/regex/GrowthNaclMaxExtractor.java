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
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.classify.Label;


public class GrowthNaclMaxExtractor extends AbstractCharacterValueExtractor {
	
	private String celsius_degreeReplaceSourcePattern = "\\s?”C\\s?|\\s?u C\\s?|\\s?°C\\s?|\\s?° C\\s?|\\s?˚C\\s?|\\s?◦C\\s?";
	private String celsius_degreeReplaceTargetPattern = " celsius_degree ";
	
	public String getCelsius_degreeReplaceSourcePattern() {
		return celsius_degreeReplaceSourcePattern;
	}
	
	public String getCelsius_degreeReplaceTargetPattern() {
		return celsius_degreeReplaceTargetPattern;
	}
	
	public GrowthNaclMaxExtractor(ILabel label) {
		super(label, "NaCl maximum");
	}
	
	@Inject
	public GrowthNaclMaxExtractor(@Named("GrowthNaclMaxExtractor_Label")Label label, 
			@Named("GrowthNaclMaxExtractor_Character")String character) {
		super(label, character);
	}
	
	@Override
	public Set<String> getCharacterValue(String text) {

		text = text.replaceAll(celsius_degreeReplaceSourcePattern, celsius_degreeReplaceTargetPattern);
		text = text.toLowerCase();
		System.out.println("Modified sent::" + text);
		
		// input: the original sentnece
		// output: String array?
		Set<String> output = new HashSet<String>(); // Output, format::List<String>
		
		int caseNumber = 0;
		if ( text.matches("(.*)(nacl(.*)range|nacl range)(.*)")) {
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
				// Example: 

				// String patternString = "(.*)(\\s?nacl range\\s?)(.*)";
				String patternString = "(.*)(nacl range|nacl(.*)range for growth|nacl occurs)(.*)";
				
				Pattern pattern = Pattern.compile(patternString);
				Matcher matcher = pattern.matcher(text);

				while (matcher.find()) {
					System.out.println("Go to Case 1::");
					// System.out.println("Whloe Sent::" + matcher.group());
					// System.out.println("Part 1::" + matcher.group(1));
					// System.out.println("Part 2::" + matcher.group(2));
					// System.out.println("Part 3::" + matcher.group(3));
					// System.out.println("Part 4::" + matcher.group(4));
					String targetPattern = matcher.group(4);
					
					RangePatternExtractor rangePatternExtractor = new RangePatternExtractor(targetPattern, "nacl;salinity;%");
					// String growNaclMax = rangePatternExtractor.getRangePatternMaxString();
					// if ( ! growNaclMax.equals("") ) {
					//	output.add(growNaclMax);
					// }
					if ( !targetPattern.contains("optimally") || !targetPattern.contains("optimal")) {
						String growNaclMax = rangePatternExtractor.getRangePatternMaxString();

						String unitString = getUnitString(targetPattern);
						// System.out.println("unitString::" + unitString);						
						if ( ! growNaclMax.equals("") ) {
							output.add(growNaclMax + " " + unitString);
						}			
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
					System.out.println("Go to Case 2::");
					// System.out.println("Whloe Sent::" + matcher.group());
					// System.out.println("Part 1::" + matcher.group(1));
					// System.out.println("Part 2::" + matcher.group(2));
					// System.out.println("Part 3::" + matcher.group(3));
					
					String targetPattern = matcher.group(2);
					System.out.println("targetPattern::" + targetPattern);
					
					RangePatternExtractor rangePatternExtractor = new RangePatternExtractor(targetPattern, "m;nacl;salinity;%");
					
					// output.add(rangePatternExtractor.getRangePatternMaxString());
					if ( !targetPattern.contains("optimally") || !targetPattern.contains("optimal")) {
						String unitString = getUnitString(targetPattern);
						// System.out.println("unitString::" + unitString);
						output.add(rangePatternExtractor.getRangePatternMaxString() + " " + unitString);					
					}
					
					
					
					
					
				}
				break;
			default:
				// System.out.println("");
				// System.out.println("Go to Case 0::");
				
		}
		return output;
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
	
	// Example: 
	public static void main(String[] args) throws IOException {
		System.out.println("Start::");
		
		GrowthNaclMaxExtractor growthNaclMaxExtractor = new GrowthNaclMaxExtractor(Label.c3);
		
		CSVSentenceReader sourceSentenceReader = new CSVSentenceReader();
		// Read sentence list
		// 
		
		// sourceSentenceReader.setInputStream(new FileInputStream("split-additionalUSPInputs.csv"));
		// List<Sentence> sourceSentenceList = sourceSentenceReader.readSentenceList();
		// System.out.println("sourceSentenceList.size()::" + sourceSentenceList.size());

		// sourceSentenceReader.setInputStream(new FileInputStream("predictions-140907-run-part-3.csv"));
		// line[0]::8,3
		sourceSentenceReader.setInputStream(new FileInputStream("split-predictions-140311-1.csv"));
		List<Sentence> sourceSentenceList = sourceSentenceReader.readSentenceList();
		// sourceSentenceReader.setInputStream(new FileInputStream("split-predictions-140528-3.csv"));
		// sourceSentenceList.addAll(sourceSentenceReader.readSentenceList());		
		

		
		
		
		int sampleSentCounter = 0;
		int extractedValueCounter = 0;
		
		for (Sentence sourceSentence : sourceSentenceList) {
			String sourceSentText = sourceSentence.getText();
			// System.out.println("sourceSentText::" + sourceSentText);
			
			sourceSentText = sourceSentText.replaceAll(growthNaclMaxExtractor.getCelsius_degreeReplaceSourcePattern(), growthNaclMaxExtractor.getCelsius_degreeReplaceTargetPattern());
			sourceSentText = sourceSentText.toLowerCase();
			// pH
			
			if (
					sourceSentText.matches("(.*)(\\bnacl\\b|\\bsalinity\\b)(.*)") 
				) {				
			
				System.out.println("\n");
				System.out.println("sourceSentText::" + sourceSentText);
				Set<String> growNaclMaxResult = growthNaclMaxExtractor.getCharacterValue(sourceSentText);
				System.out.println("growNaclMaxResult::" + growNaclMaxResult.toString());
				if ( growNaclMaxResult.size() > 0 ) {
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
