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
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.Sentence;

public class GrowthPhOptimumExtractor extends AbstractCharacterValueExtractor {

	private String myNumberPattern = "(\\d+(\\.\\d+)?)";

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
	
	
	public String getMyNumberPattern() {
		return myNumberPattern;
	}
	
	private String targetPatternString = "(" +
			"(between\\s?|from\\s?)*" +
			myNumberPattern + "(\\s)*(\\()*(±|-|–|and|to|)*(\\s)*" + myNumberPattern + "*(\\))*" + 
			")";	
	
	public GrowthPhOptimumExtractor(ILabel label) {
		super(label, "pH optimum");
	}
	
	@Inject
	public GrowthPhOptimumExtractor(@Named("GrowthPhOptimumExtractor_Label")Label label, 
			@Named("GrowthPhOptimumExtractor_Character")String character) {
		super(label, character);
	}
	
	@Override
	public Set<String> getCharacterValue(String text) {

		text = text.replaceAll("\\s?u C\\s?|\\s?°C\\s?|\\s?° C\\s?|\\s?˚C\\s?|\\s?◦C\\s?", " celsius_degree ");
		text = text.toLowerCase();
		// System.out.println("Modified sent::" + text);
		
		// Add Map<String, String> on Feb 09, 2015 MON
		regexResultWithMappingCaseMap = new HashMap<String, String>();

		
		// input: the original sentnece
		// output: String array?
		Set<String> output = new HashSet<String>(); // Output, format::List<String>

		// Example: 	
		/*
		String targetPatternString = "(" +
				"\\d+\\.\\d+\\s?-\\s?\\d+\\.\\d+\\s?|" +
				"\\d+\\s?-\\s?\\d+\\.\\d+\\s?|" +
				"\\d+\\.\\d+\\s?-\\s?\\d+\\s?|" +
				"\\d+\\s?-\\s?\\d+\\s?|" +
				
				"\\d+\\.\\d+\\s?–\\s?\\d+\\.\\d+\\s?|" +
				"\\d+\\s?–\\s?\\d+\\.\\d+\\s?|" +
				"\\d+\\.\\d+\\s?–\\s?\\d+\\s?|" +
				"\\d+\\s?–\\s?\\d+\\s?|" +
				
				"\\d+\\.\\d+\\s?and\\s?\\d+\\.\\d+\\s?|" +
				"\\d+\\s?and\\s?\\d+\\.\\d+\\s?|" +
				"\\d+\\.\\d+\\s?and\\s?\\d+\\s?|" +
				"\\d+\\s?and\\s?\\d+\\s?|" +
				
				"\\d+\\.\\d+\\s?\\d+\\.\\d+|" +
				"\\d+\\s?\\d+\\.\\d+|" +
				"\\d+\\.\\d+\\s?\\d+|" +
				"\\d+\\s?\\d+|" +
				
				"between\\s\\d+\\.\\d+\\sand\\s\\d+\\.\\d+|" +
				"between\\s\\d+\\.\\d+\\sand\\s\\d+|" +
				"between\\s\\d+\\sand\\s\\d+\\.\\d+|" +
				"between\\s\\d+\\sand\\s\\d+|" +
				
				"\\d+\\.\\d+|" +
				"\\d+|" +
				"\\d+\\s?" + 
				")";
		
		*/
		
		String patternString = "(.*)" + 
				"(" +
				// "(\\()*" + 
				//"(\\s|\\()*" + 
				//"(" + 
				//"optimum, around ph" +
				"optimum being ph|" +
				"optimum\\: ph|" +
				"optimum\\), at ph|" +
				"optimum\\s?ph|" + 
				"optimal\\s?ph|" +
				"optimum ph of|" + 
				"optimal ph of|" +
				"optimal ph for growth is about|" + 
				"optimum growth at ph|" + 
				"optimum at ph|" + 
				"optimal growth at ph|" + 
				"the optimal ph was|" + 
				"optimal ph for growth is |" + 

				"optimum\\,\\s?ph|" + 
				"ph\\s?optimal|" + 
				"ph\\s?optimum| " +
				"optimum, around ph|" +
				"optimum of ph" +
				//")" +
				//"(\\s|\\))*" + 
				//"(\\))*" +
				")" +
				// "\\b" +
				//targetPatternString +
				// "(\\))*\\b" +
				"(.*)";
		// System.out.println("patternString::" + patternString);

		
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text);

		boolean isCase1= false;
		
		while (matcher.find()) {
			isCase1 = true;
			//System.out.println("Go to case 1.");
			
			// System.out.println("Whloe Sent::" + matcher.group());
			// System.out.println("Part 1::" + matcher.group(1));
			// System.out.println("Part 2::" + matcher.group(2));
			// System.out.println("Part 3::" + matcher.group(3));
			
			String matchPartString = matcher.group(3);
			
			Pattern targetPattern = Pattern.compile("\\b" + targetPatternString + "\\b");
			Matcher targetMatcher = targetPattern.matcher(matchPartString);
			
			// while (matcher2.find()) {
			if (targetMatcher.find()) { // Just choose the closest one (nearest one, first one)
				String matchPartString2 = targetMatcher.group(1);
				output.add(matchPartString2);
				regexResultWithMappingCaseMap.put("Case 1", matchPartString2);

			}
			
			
			// output.add(targetPattern);
		}	
		

		
		String patternString2 = "(.*)(\\s?grows optimally\\s?|" +
									"\\s?optimal\\s?growth\\s?|" + 
									"\\s?optimum\\s?growth\\s?)" +
									"(.*)(\\s?ph\\s?)" +
									targetPatternString +
									"(.*)";
		
		Pattern pattern2 = Pattern.compile(patternString2);
		Matcher matcher2 = pattern2.matcher(text);

		while (matcher2.find()) {
			//System.out.println("Go to case 2.");
			// System.out.println("Whloe Sent::" + matcher2.group());
			// System.out.println("Part 1::" + matcher2.group(1));
			// System.out.println("Part 2::" + matcher2.group(2));
			// System.out.println("Part 3::" + matcher2.group(3));
			// System.out.println("Part 4::" + matcher2.group(4));
			// System.out.println("Part 5::" + matcher2.group(5));
			String matchPartString = matcher2.group(5);
			output.add(matchPartString);
			regexResultWithMappingCaseMap.put("Case 2", matchPartString);

		}		
		
		
		// Example: 
		// Growth is strictly aerobic and occurs between 4.0 and 36.0 ˚c (28.0–30.0 ˚c optimum), at ph 6.0–10.0 (ph 7.0–8.0 optimum) and at 1.0–6.0% nacl.

		
		String patternString3 = "(.*)(\\(\\s?ph\\s?)" +
				targetPatternString +
				"(\\s?optimum\\s?\\))(.*)";
		
		Pattern pattern3 = Pattern.compile(patternString3);
		Matcher matcher3 = pattern3.matcher(text);

		while (matcher3.find()) {
			//System.out.println("Go to case 3.");
			// System.out.println("Whloe Sent::" + matcher3.group());
			// System.out.println("Part 1::" + matcher3.group(1));
			// System.out.println("Part 2::" + matcher3.group(2));
			// System.out.println("Part 3::" + matcher3.group(3));
			// System.out.println("Part 4::" + matcher3.group(4));
			// System.out.println("Part 5::" + matcher3.group(5));
			String matchPartString = matcher3.group(3);
			output.add(matchPartString);
			regexResultWithMappingCaseMap.put("Case 3", matchPartString);

		}
		
		
		
		if ( isCase1 == false ) {
			// Case 4::
			// sourceSentText::grows between 10 and 45˚c (optimum at 37˚c) and ph 5–9 (optimum at 7.5).
			// ph 5–9 (optimum at 7.5)
			
			// String patternString4 = "(.*)(ph\\s?)" + myNumberPattern + "(\\s?\\(\\s?optimum at\\s?)" + 
			//						myNumberPattern + "(\\))(.*)";
			
			// String patternString4 = "(.*)(ph\\s?)(.*)(\\s?\\(\\s?optimum\\s?)(.*)(\\))(.*)";
			// String patternString4 = "(.*)(ph\\s?)"+myNumberPattern+"(\\s?\\(\\s?optimum\\s?)(.*)(\\))(.*)";
			
			
			String patternString4 = "(ph\\s?)" + targetPatternString + "(\\s?\\()(.*)(\\s?\\))";
			// System.out.println("patternString4::" + patternString4);
			
			Pattern pattern4 = Pattern.compile(patternString4);
			Matcher matcher4 = pattern4.matcher(text);

			while (matcher4.find()) {
				//System.out.println("Go to case 4.");
				// System.out.println("Whloe Sent::" + matcher4.group());
				// System.out.println("Part 1::" + matcher4.group(1));
				// System.out.println("Part 2::" + matcher4.group(2));
				// System.out.println("Part 3::" + matcher4.group(3));
				// System.out.println("Part 4::" + matcher4.group(4));
				// System.out.println("Part 5::" + matcher4.group(5));
				// System.out.println("Part 6::" + matcher4.group(6));
				// System.out.println("Part 7::" + matcher4.group(7));
				// System.out.println("Part 8::" + matcher4.group(8));
				// System.out.println("Part 9::" + matcher4.group(9));
				// System.out.println("Part 10::" + matcher4.group(10));
				// System.out.println("Part 11::" + matcher4.group(11));
				// System.out.println("Part 12::" + matcher4.group(12));
				// System.out.println("Part 13::" + matcher4.group(13));
				//System.out.println("Part 14::" + matcher4.group(14));
				// System.out.println("Part 15::" + matcher4.group(15));
				
				String matchPartString = matcher4.group(14);
				
				Pattern targetPattern = Pattern.compile("\\b" + targetPatternString + "\\b");
				Matcher targetMatcher = targetPattern.matcher(matchPartString);
				
				while (targetMatcher.find()) {
				// if (targetMatcher.find()) { // Just choose the closest one (nearest one, first one)
					String matchPartString2 = targetMatcher.group(1);
					
					// System.out.println("matchPartString2::" + matchPartString2);

					if ( isNextToRightSymbolNeg(matchPartString, matchPartString2, "%;celsius_degree") == true ) {
						output.add(matchPartString2);
						regexResultWithMappingCaseMap.put("Case 4", matchPartString2);

					}
					// } else {
					//	System.out.println("Do not include this!");
					// }
				}			
				
				//output.add(targetPattern);
			}			
		}
		

		
		/*
		// Example: optimal ph is 6.2; optimum pH is 3.3; optimum pH 3.7
		String patternString = "(.*)(\\s?ph\\s?optimal\\s?|\\s?ph\\s?optimum\\s?|\\s?optimum\\s?ph\\s?|\\s?optimal\\s?ph\\s?)(.*)";
			
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text.toLowerCase());

		while (matcher.find()) {
			// System.out.println("Whloe Sent::" + matcher.group());
			// System.out.println("Part 1::" + matcher.group(1));
			// System.out.println("Part 2::" + matcher.group(2));
			// System.out.println("Part 3::" + matcher.group(3));
			String matchPart3 = matcher.group(3);
			matchPart3 = " " + matchPart3 + " ";
			String[] matchPart3Array = matchPart3.split(" ");				

			int subMatchPart3Length = 3;
			if (matchPart3Array.length < subMatchPart3Length) {
				subMatchPart3Length = matchPart3Array.length;
			}
			StringBuilder subMatchPart3 = new StringBuilder();
			for (int i = 0; i < subMatchPart3Length; i++) {
				subMatchPart3.append(" " + matchPart3Array[i]);
			}
			
			
			
			
			// matchPart3 should be "is 3.7", "3.7", "2.3-2.5"

			String patternStringRange = "(" + 
				"\\d+\\.\\d+\\sto\\s\\d+\\.\\d+|" +
				"\\d+\\sto\\s\\d+|" +

				"\\d+\\.\\d+-\\d+\\.\\d+|" +
				"\\d+-\\d+|" +
				
				"\\d+\\.\\d+–\\d+\\.\\d+|" +
				"\\d+–\\d+|" +

				"between\\s\\d+\\.\\d+\\sand\\s\\d+\\.\\d+|" +
				"between\\s\\d+\\sand\\s\\d+" +

				")";			


			Pattern patternRange = Pattern.compile(patternStringRange);
			Matcher matcherRange = patternRange.matcher(subMatchPart3);			
			
			List<String> matchStringList = new ArrayList<String>();

			int matchCounter = 0;
			while (matcherRange.find()) {
				matchStringList.add(matcherRange.group().trim());
				matchCounter++;
			}	
			
			if (matchCounter > 0) {
				String rangeString = matchStringList.get(0).toString();
				output.add(rangeString);	
			} else {
				if (matchPart3Array.length > 1) {
					List<String> matchStringList2 = new ArrayList<String>();
					String patternString2 = "(" +
							"\\d+\\.\\d+-\\d+\\.\\d+|" +
							"\\d+\\-\\d+|" +
							"\\d+\\.\\d+|" +
							"\\d+" +
							")";
					int loopLength = 6;
					if (matchPart3Array.length < loopLength){
						loopLength = matchPart3Array.length;
					}					
					
					for (int i = 0; i < loopLength; i++) {
						Pattern pattern2 = Pattern.compile(patternString2);
						Matcher matcher2 = pattern2.matcher(matchPart3Array[i]);
						while (matcher2.find()) {
							matchStringList2.add(matcher2.group().trim());
						}
					}
					output.addAll(matchStringList2);
				}
			}	
		}			
		*/
		
		return output;
	}

	public boolean isNextToRightSymbolNeg(String matchPartString, String matchPartString2, String negSymbol) {
		
		boolean isNextToRightSymbolNeg = true;
		
		matchPartString2 = matchPartString2.trim();
		String negSymbolArray[] = negSymbol.split(";");
		
		String matchPartStringArray[] = matchPartString.split(" ");
		// System.out.println("matchPartStringArray::" + Arrays.toString(matchPartStringArray));
		// System.out.println("matchPartString2::" + matchPartString2);
		// System.out.println("matchPartStringArray.length::" + matchPartStringArray.length);
		for ( int i = 0; i < matchPartStringArray.length; i++ ) {
			// System.out.println("matchPartStringArray[" + i + "]::" + matchPartStringArray[i]);
			
			if ( matchPartStringArray[i].contains(matchPartString2) ) {
				// System.out.println("matchPartStringArray[" + i + "]::" + matchPartStringArray[i]);
				if ( i > 0 ) {
					// System.out.println("matchPartStringArray[i-1]::" + matchPartStringArray[i-1]);
					for ( int j = 0; j < negSymbolArray.length; j++ ) {
						if (matchPartStringArray[i-1].contains(negSymbolArray[j])) {
							isNextToRightSymbolNeg = false;
							// System.out.println("Do not include this!");
						}
					}
				}
				if ( i < matchPartStringArray.length -1 ) {
					// System.out.println("matchPartStringArray[i+1]::" + matchPartStringArray[i+1]);
					for ( int j = 0; j < negSymbolArray.length; j++ ) {
						if (matchPartStringArray[i+1].contains(negSymbolArray[j])) {
							isNextToRightSymbolNeg = false;
							// System.out.println("Do not include this!");
						}
					}					
				}
			}
			
			
			/*
			if ( matchPartStringArray[i].contains(symbol) && matchPartStringArray[i].contains(matchPartString2) ) {
				// System.out.println("matchPartStringArray[i]::" + matchPartStringArray[i]);
				isNextToRightSymbolNeg = true;
			}
			
			if ( i > 0 && i < matchPartStringArray.length -1 ) {
				if ( matchPartStringArray[i+1].contains(symbol) && matchPartStringArray[i].contains(matchPartString2) ) {
					// System.out.println("matchPartStringArray[i+1]::" + matchPartStringArray[i+1]);
					isNextToRightSymbolNeg = true;
				}
				if ( matchPartStringArray[i-1].contains(symbol) && matchPartStringArray[i].contains(matchPartString2) ) {
					// System.out.println("matchPartStringArray[i-1]::" + matchPartStringArray[i-1]);
					isNextToRightSymbolNeg = true;
				}
			}
			*/
		}
		return isNextToRightSymbolNeg;
	}	
	
	
	// Example: Grows at 15–37 celsius_degree (optimum 30 celsius_degree ) and pH 5–8 (optimum pH 7).
	public static void main(String[] args) throws IOException {
		/*
		String exampleSent = "Grows at 15–37 celsius_degree (optimum 30 celsius_degree ) and pH 5–8 (optimum pH 7).";
		String patternString = "(.*)(\\s?optimum pH\\s?)" +
								"(" +
								"\\d+|" +
								"\\d+\\s?|" + 
								"\\d+\\s?-\\s?\\d+\\s?|" +
								"\\d+\\s?–\\s?\\d+\\s?|" +
								"\\d+\\.\\d+\\s?-\\s?\\d+\\.\\d+\\s?|" +
								"\\d+\\.\\d+\\s?–\\s?\\d+\\.\\d+\\s?" +
								")" +
								"(.*)";
		
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(exampleSent);

		while (matcher.find()) {
			System.out.println("Whloe Sent::" + matcher.group());
			System.out.println("Part 1::" + matcher.group(1));
			System.out.println("Part 2::" + matcher.group(2));
			System.out.println("Part 3::" + matcher.group(3));
			System.out.println("Part 4::" + matcher.group(4));
		}
		*/
		
		System.out.println("Start::");
		
		GrowthPhOptimumExtractor growthPhOptimumExtractor = new GrowthPhOptimumExtractor(Label.c3);	
		
		
		/*
		CSVSentenceReader sourceSentenceReader = new CSVSentenceReader();
		// Read sentence list
		// 
		sourceSentenceReader.setInputStream(new FileInputStream("split-predictions-140311-1.csv"));
		List<Sentence> sourceSentenceList = sourceSentenceReader.readSentenceList();
		sourceSentenceReader.setInputStream(new FileInputStream("split-predictions-140528-3.csv"));
		sourceSentenceList.addAll(sourceSentenceReader.readSentenceList());
		
		System.out.println("sourceSentenceList.size()::" + sourceSentenceList.size());
		
		int sampleSentCounter = 0;
		int extractedValueCounter = 0;
		
		for (Sentence sourceSentence : sourceSentenceList) {
			String sourceSentText = sourceSentence.getText();
			sourceSentText = sourceSentText.replaceAll("\\s?u C\\s?|\\s?°C\\s?|\\s?° C\\s?|\\s?˚C\\s?|\\s?◦C\\s?", " celsius_degree ");
			sourceSentText = sourceSentText.toLowerCase();
			
			// pH
			if ( (sourceSentText.matches("(.*)(\\bph\\b)(.*)") && sourceSentText.matches("(.*)(\\boptimal\\b)(.*)")) || 
					(sourceSentText.matches("(.*)(\\bph\\b)(.*)") && sourceSentText.matches("(.*)(\\boptimum\\b)(.*)"))
					) {
				System.out.println("\n");
				System.out.println("sourceSentText::" + sourceSentText);
				Set<String> growPhOptimumResult = growthPhOptimumExtractor.getCharacterValue(sourceSentText);
				System.out.println("growPhOptimumResult::" + growPhOptimumResult.toString());
				if ( growPhOptimumResult.size() > 0 ) {
					extractedValueCounter +=1;
				}
				sampleSentCounter +=1;
			}
		
		}

		System.out.println("\n");
		System.out.println("sampleSentCounter::" + sampleSentCounter);
		System.out.println("extractedValueCounter::" + extractedValueCounter);
		
		*/

		// Test on February 09, 2015 Mon
		CSVSentenceReader sourceSentenceReader = new CSVSentenceReader();
		// Read sentence list
		// 
		String sourceFile = "micropieInput_zip/training_data/150130-Training-Sentences-new.csv";
		String svmLabelAndCategoryMappingFile = "micropieInput_zip/svmlabelandcategorymapping_data/SVMLabelAndCategoryMapping.txt";
		sourceSentenceReader.setInputStream(new FileInputStream(sourceFile));
		sourceSentenceReader.setInputStream2(new FileInputStream(svmLabelAndCategoryMappingFile));
		sourceSentenceReader.readSVMLabelAndCategoryMapping();
		List<Sentence> sourceSentenceList = sourceSentenceReader.readSentenceList();
		System.out.println("sourceSentenceList.size()::" + sourceSentenceList.size());

		
		String outputFile = "micropieInput_zip_output/GrowthPhOptimum_Regex.csv";
		OutputStream outputStream = new FileOutputStream(outputFile);
		CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF8")));
		List<String[]> lines = new LinkedList<String[]>();
		
		
		int sampleSentCounter = 0;
		int extractedValueCounter = 0;
		
		for (Sentence sourceSentence : sourceSentenceList) {
			String sourceSentText = sourceSentence.getText();
			sourceSentText = sourceSentText.replaceAll(growthPhOptimumExtractor.getCelsius_degreeReplaceSourcePattern(), growthPhOptimumExtractor.getCelsius_degreeReplaceTargetPattern());
			sourceSentText = sourceSentText.toLowerCase();
			// NaCl Optimum
			// NaCl (w/v) %
			
			
			if ( (sourceSentText.matches("(.*)(\\bph\\b)(.*)") && sourceSentText.matches("(.*)(\\boptimal\\b)(.*)")) || 
					(sourceSentText.matches("(.*)(\\bph\\b)(.*)") && sourceSentText.matches("(.*)(\\boptimum\\b)(.*)"))
					) {	
				System.out.println("\n");
				System.out.println("sourceSentText::" + sourceSentText);
				
				
				
				Set<String> growthPhOptimumResult = growthPhOptimumExtractor.getCharacterValue(sourceSentText);
				
				System.out.println("growthNaClOptimumExtractor.getRegexResultWithMappingCaseMap()::" + growthPhOptimumExtractor.getRegexResultWithMappingCaseMap().toString());
				
				String regexResultWithMappingCaseMapString = "";
				
				for (Map.Entry<String, String> entry : growthPhOptimumExtractor.getRegexResultWithMappingCaseMap().entrySet()) {
					System.out.println("Key : " + entry.getKey() + " Value : "
					 	+ entry.getValue());
				
					regexResultWithMappingCaseMapString += entry.getKey() + ":" + entry.getValue() + ", ";
					
				}
				
				System.out.println("growthPhOptimumResult::" + growthPhOptimumResult.toString());
				if ( growthPhOptimumResult.size() > 0 ) {
					extractedValueCounter +=1;
				}
				sampleSentCounter +=1;
				
				System.out.println("regexResultWithMappingCaseMapString::" + regexResultWithMappingCaseMapString);

				
				lines.add(new String[] { sourceSentText,
						regexResultWithMappingCaseMapString
						} );
				
			} /*else {
				String sentLabel = sourceSentence.getLabel().getValue();
				
				if ( sentLabel.equals("1") ) {
					System.out.println("sentLabel::" + sentLabel);
					System.out.println("sourceSentText::" + sourceSentText);
					System.out.println("no case");
					lines.add(new String[] { sourceSentText,
							"No Case"
							} );
				}
				
				
				

			}*/
			
		
		
		} 

		System.out.println("\n");
		System.out.println("sampleSentCounter::" + sampleSentCounter);
		System.out.println("extractedValueCounter::" + extractedValueCounter);

		
		writer.writeAll(lines);
		writer.flush();
		writer.close();		
		
		
	}



}



