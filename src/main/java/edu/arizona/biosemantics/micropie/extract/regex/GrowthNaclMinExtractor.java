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
 * Extract the character 3.2 NaCl minimum
 * Sample sentences:
 * 	1. The NaCl concentration range was 0.1 to 35 g per liter and the optimum NaCl concentration was 0.5 g per liter.
 * 	2. Growth requires at least 1.7 M NaCl, optimally 3.0–4.3 M NaCl.
 *	
 *	Method:
 *	1.	Regular Expression
 */
public class GrowthNaclMinExtractor extends AbstractCharacterValueExtractor {
	
	private String celsius_degreeReplaceSourcePattern = "\\s?”C\\s?|\\s?u C\\s?|\\s?°C\\s?|\\s?° C\\s?|\\s?˚C\\s?|\\s?◦C\\s?";
	private String celsius_degreeReplaceTargetPattern = " celsius_degree ";

	private String myNumberPattern = "(\\d+(\\.\\d+)?)";
	
	// Add Map<String, String> on Feb 09, 2015 MON
	private Map<String, String> regexResultWithMappingCaseMap;

	public Map<String, String> getRegexResultWithMappingCaseMap() {
		return regexResultWithMappingCaseMap;
	}
	// Add Map<String, String> on Feb 09, 2015 MON

	
	public String getCelsius_degreeReplaceSourcePattern() {
		return celsius_degreeReplaceSourcePattern;
	}
	
	public String getCelsius_degreeReplaceTargetPattern() {
		return celsius_degreeReplaceTargetPattern;
	}
	
	public GrowthNaclMinExtractor(ILabel label) {
		super(label, "NaCl minimum");
	}
	
	@Inject
	public GrowthNaclMinExtractor(@Named("GrowthNaclMinExtractor_Label")Label label, 
			@Named("GrowthNaclMinExtractor_Character")String character) {
		super(label, character);
	}
	
	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {

		Set<String> output = new HashSet();
		List<CharacterValue> charValueList = null;
		
		String text = sentence.getText();

		text = text.replaceAll(celsius_degreeReplaceSourcePattern, celsius_degreeReplaceTargetPattern);
		text = text.toLowerCase();
		
		// Add Map<String, String> on Feb 09, 2015 MON
		regexResultWithMappingCaseMap = new HashMap<String, String>();
		
		// ??// sea salts concentration of 1-12%
		// with 1-7% sea salts
		
		// nacl concentrations of 4-23% (w/v)
		// nacl concentrations greater than 2.5% (w/v)
		// nacl concentrations in the range 0-3% (w/v)
		// with 0.5-8% nacl
		// growth occurs with 0.5-8% nacl, at 4-35 celsius_degree and at ph 5.5-10.0.
		// up to 5.0% nacl (w/v)
		// in the presence of 2% (w/v) nacl
		// concentrations > 9% (w/v) nacl
		
		// The pH and salinity ranges for growth are 6.0-9.0 and 0.25-10% (w/v), respectively.
		
		// no optimal
		
		
		
		
		int caseNumber = 0;
		
		if ( text.equals("nacl is required for growth.") ) {
			caseNumber = 1;
		} else if ( text.matches("(.+)(\\bnacl|sea salts|salinity\\b)(.+)") ) {
			caseNumber = 2;
		} /*else if ( text.matches("(.+)(sea salts)(.+)") ) {
			System.out.println("contain Sea Salts::" + text);	
		} else if ( text.matches("(.+)(salinity)(.+)") ) {
			System.out.println("contain Salinity::" + text);	
		}*/
		
		/* marked on March 04, 2015
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
		*/
		switch(caseNumber) {
			case 1:
				regexResultWithMappingCaseMap.put("Case 1","\"Unspecified\"");
				break;
			case 2:
				
				// String text = "";
				// text = "4-23% nacl";
				// text = "growth occurs with 0.5-8% nacl, at 4-35 celsius_degree and at ph 5.5-10.0.";
				// text = "a 11.1 b";
				// text = "11.1";
				// text = "11.1-23.23444";
				
				//text = "nacl concentrations of 4-23% (w/v)";
				// // text = "nacl concentrations greater than 2.5% (w/v)";
				//text = "nacl concentrations in the range 0-3% (w/v)";
				//text = "with 0.5-8% nacl";
				//text = "growth occurs with 0.5-8% nacl, at 4-35 celsius_degree and at ph 5.5-10.0.";
				// text = "up to 5.0% nacl (w/v)";
				
				// text = "with 1-7% sea salts";
				
				
				// text = "in the presence of 2% (w/v) nacl";
				// text = "concentrations > 9% (w/v) nacl";
				// text = "The pH and salinity ranges for growth are 6.0-9.0 and 0.25-10% (w/v), respectively.";
				
				//text = "colonies on agar plates containing 3.4 m nacl are red, elevated and round.";
				
				// text = "no growth occurs in the presence of >0.5% (w/v) nacl.";

				// Optimum growth occurs at 250 g nacl per liter with a high requirement of mgso4 (40 g per liter).
				
				// ??
				// text = "growth occurs with 1-15% (w/v) nacl (optimum 2% nacl), at ph 6-9 (optimum ph 7) and at 15-42 celsius_degree (optimum 20 celsius_degree ).";

				
				
				
				// String patternString = "(" + 
				//		// myNumberPattern + "\\-?" + myNumberPattern + "?\\s*\\%\\s*\\(w\\/v\\)|" + 
				//		"(\\<|\\≤|\\>|\\≥|up to|greater than|more than|less than|with)?\\s*" + myNumberPattern + "\\-?" + myNumberPattern + "?\\s*(\\%)?\\s*(g)?\\s*(m)?\\s*(nacl|sea salts|salinity)?\\s*(\\(w\\/v\\))?\\s*(per liter)?\\s*(m)?\\s*(nacl|sea salts|salinity)?" 
				//		+ ")";

				
				
				
				String patternString = "(" + 
						// OLD // myNumberPattern + "\\-?" + myNumberPattern + "?\\s*\\%\\s*\\(w\\/v\\)|" + 
						// OLD // "(\\<|\\≤|\\>|\\≥|up to|greater than|more than|less than)?\\s*" + myNumberPattern + "\\-?" + myNumberPattern + "?\\s*(\\%)?\\s*(g)?\\s*(m)?\\s*(nacl|sea salts|salinity)?\\s*(\\(w\\/v\\))?\\s*(per liter)?\\s*(m)?\\s*(nacl|sea salts|salinity)?" 
						"(\\b\\<|\\≤|\\>|\\≥|up to|greater than|more than|less than|at least|optimum|optimal|optimally|optimum of approximately|optimum growth in the presence of|optimally around|optimal growth at|with an optimum at\\b)?\\s*" + 
						myNumberPattern + "\\s*\\-?" + myNumberPattern + "?\\s*(\\%|\\‰|g|m)?\\s*(\\bnacl|sea\\ssalts|salinity\\b)?\\s*(\\(w\\/v\\))?\\s*(\\bper\\sliter\\b)?\\s*(\\bnacl|sea\\salts|salinity\\b)?" 
						+ ")";

				
				
				Pattern pattern = Pattern.compile(patternString);
				Matcher matcher = pattern.matcher(text);

				
				while (matcher.find()) {
					if (text.matches("(.*)(\\bno growth|cannot grow|can't grow|not\\b)(.*)")) {
						
					} else {
					
						// System.out.println("YES");
						// System.out.println("matcher.group()::" + matcher.group());
						// System.out.println("matcher.group(0)::" + matcher.group(0));
						// System.out.println("matcher.group(1)::" + matcher.group(1));
						// System.out.println("matcher.group(2)::" + matcher.group(2));
						// System.out.println("matcher.group(3)::" + matcher.group(3));
						// System.out.println("matcher.group(4)::" + matcher.group(4));
						// System.out.println("matcher.group(5)::" + matcher.group(5));
						// System.out.println("matcher.group(6)::" + matcher.group(6));
						// System.out.println("matcher.group(7)::" + matcher.group(7));
						// System.out.println("matcher.group(8)::" + matcher.group(8));
						// System.out.println("matcher.group(9)::" + matcher.group(9));
						// System.out.println("matcher.group(10)::" + matcher.group(10));
						// System.out.println("matcher.group(11)::" + matcher.group(11));
						// System.out.println("matcher.group(12)::" + matcher.group(12));
						// System.out.println("matcher.group(13)::" + matcher.group(13));
						// System.out.println("matcher.group(14)::" + matcher.group(14));
						
						
//						System.out.println("matcher.group(0)::" + matcher.group(0));
//						System.out.println("matcher.group(1)::" + matcher.group(1));
//						System.out.println("matcher.group(2)::" + matcher.group(2));
//						System.out.println("matcher.group(3)::" + matcher.group(3));
//						System.out.println("matcher.group(4)::" + matcher.group(4));
//						System.out.println("matcher.group(5)::" + matcher.group(5));
//						System.out.println("matcher.group(6)::" + matcher.group(6));
//						System.out.println("matcher.group(7)::" + matcher.group(7));
//						System.out.println("matcher.group(8)::" + matcher.group(8));
//						System.out.println("matcher.group(9)::" + matcher.group(9));
//						System.out.println("matcher.group(10)::" + matcher.group(10));
//						System.out.println("matcher.group(11)::" + matcher.group(11));
						// System.out.println("matcher.group(12)::" + matcher.group(12));
						// System.out.println("matcher.group(13)::" + matcher.group(13));
						// System.out.println("matcher.group(14)::" + matcher.group(14));
						
						String equatorString = "";
						if (  matcher.group(2) != null )
							equatorString = matcher.group(2);
						
						boolean isOptimumNaClDesc = false;
						if ( !equatorString.equals("") ) {
							if ( equatorString.matches("optimum|optimal|optimally|optimum of approximately|optimum growth in the presence of|optimally around|optimal growth at|with an optimum at") ) {
								isOptimumNaClDesc = true;
							}
						}
						
						
						
						String unitString = "";
						boolean isCorrectNaClUnit = false;
						for ( int i = 7; i < 12; i++ ) {
							if ( matcher.group(i) != null) {
								isCorrectNaClUnit = true;
								unitString += matcher.group(i) + " ";
							}
							
						}
						
						
						String stringNaClMin = "";
						
						if ( matcher.group(3) != null ) {
							stringNaClMin = matcher.group(3);
						} else {
							stringNaClMin = "did not capture";
						}

						
						
						
						if ( isCorrectNaClUnit == true && isOptimumNaClDesc == false ) {
							//System.out.println(equatorString + " " + stringNaClMin + unitString);
							output.add(stringNaClMin + "" + unitString);
							regexResultWithMappingCaseMap.put("Case 1", equatorString + " " + stringNaClMin + unitString);
						}


				
						
						

					}
				}
				
				/*
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
					// String growNaclMin = rangePatternExtractor.getRangePatternMaxString();
					// if ( ! growNaclMin.equals("") ) {
					//	output.add(growNaclMin);
					// }
					if ( !targetPattern.contains("optimally") || !targetPattern.contains("optimal")) {
						String growNaclMin = rangePatternExtractor.getRangePatternMaxString();

						String unitString = getUnitString(targetPattern);
						// System.out.println("unitString::" + unitString);						
						if ( ! growNaclMin.equals("") ) {
							output.add(growNaclMin + " " + unitString);
							regexResultWithMappingCaseMap.put("Case 1", growNaclMin + " " + unitString);
						}			
					}					
				}
				*/
				break;
			case 3:
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
					//System.out.println("Go to Case 2::");
					// System.out.println("Whloe Sent::" + matcher.group());
					// System.out.println("Part 1::" + matcher.group(1));
					// System.out.println("Part 2::" + matcher.group(2));
					// System.out.println("Part 3::" + matcher.group(3));
					
					String targetPattern = matcher.group(2);
					//System.out.println("targetPattern::" + targetPattern);
					
					RangePatternExtractor rangePatternExtractor = new RangePatternExtractor(targetPattern, "m;nacl;salinity;%");
					
					// output.add(rangePatternExtractor.getRangePatternMaxString());
					if ( !targetPattern.contains("optimally") || !targetPattern.contains("optimal")) {
						String unitString = getUnitString(targetPattern);
						// System.out.println("unitString::" + unitString);
						output.add(rangePatternExtractor.getRangePatternMaxString() + " " + unitString);
						regexResultWithMappingCaseMap.put("Case 2", rangePatternExtractor.getRangePatternMaxString() + " " + unitString);
					}
					
					
					
					
					
				}
				break;
			default:
				// System.out.println("");
				// System.out.println("Go to Case 0::");
				
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
}
