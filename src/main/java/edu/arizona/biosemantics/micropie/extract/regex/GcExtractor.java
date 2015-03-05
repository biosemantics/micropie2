
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
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Sentence;

public class GcExtractor extends AbstractCharacterValueExtractor {

	private String patternStringGc = "(" + 
										"\\bguanine plus cytosine|Guanosine plus cytosine|Guanosine plus cytosine|guanine-plus-cytosine\\b|" +
										"\\b\\(G\\s?\\+\\s?C\\b|" +
										"\\bG\\s?\\+\\s?C\\b|" + //zero or one
										"\\bG\\s*\\+\\s*C\\b|" + // zero or many // \\s+ => one or many
										
										// "\\bg\\s+\\+\\s+c\\b|" +
										// "\\s?\\(G+C\\s?|" + 
										// "\\s?G\\s*\\+\\s*C|" + 
										// "\\s+G\\s*\\+\\s*C\\s+|" + 
										// "\\s+g\\s*\\+\\s*c\\s+|" + 
										// "\\s+GC\\s+|" + 
										// "\\s+gc\\s+|" + 
										// "%GC|" + 
										// "%G+C" +
										"\\bGC\\b|" +
										"\\bguanine\\s?\\+\\s?cytosine\\b" +
										")";

	private String myNumberPattern = "(\\d+(\\.\\d+)?)";
	

	/*
	private String targetPatternString = "(" +
			
			"\\d+\\.\\d+±\\s?\\d+\\.\\d+|" + 
			"\\d+±\\s?\\d+\\.\\d+|" + 
			"\\d+\\.\\d+±\\s?\\d+|" + 
			"\\d+±\\s?\\d+|" + 
			
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
	// private String targetPatternString = "(" +
	//		"(between\\s?|from\\s?)*" +
	//		myNumberPattern + "(\\s)*(\\()*(±|-|–|and|to|)*(\\s)*" + myNumberPattern + "*(\\))*" + 
	//		")";

	private String targetPatternString = "(" +
			"(between\\s*|from\\s*)?" + myNumberPattern + "\\s*(±|-|–|and|to|)?\\s*" + myNumberPattern + "|" +
			myNumberPattern + "\\s*(±|-|–|and|to|)?\\s*" + myNumberPattern +
			")";
	
	
	
	private Map<String, String> regexResultWithMappingCaseMap;

	public Map<String, String> getRegexResultWithMappingCaseMap() {
		return regexResultWithMappingCaseMap;
	}
	
	public String getTargetPatternString() {
		return targetPatternString;
	}
	
	public String getMyNumberPattern() {
		return myNumberPattern;
	}	
	
	public String getPatternStringGc() {
		return patternStringGc;
	}
	
	// private String patternStringGc = "\\s+G\\+C\\s+";
	// private String patternStringGc = "G\\s*\\+\\s*C";
	
	public GcExtractor(ILabel label) {
		super(label, "%G+C");
	}
	
	@Inject
	public GcExtractor(@Named("GcExtractor_Label")ILabel label, 
			@Named("GcExtractor_Label")String character) {
		super(label, character);
	}

	@Override
	public Set<String> getCharacterValue(String text) {
		// TODO Auto-generated constructor stub
		
		// Add Map<String, String> on Feb 04, 2015 WED
		regexResultWithMappingCaseMap = new HashMap<String, String>();
		
		Set<String> output = new HashSet<String>(); // Output,
		// format::List<String>

		// input: the original sentnece
		// output: String array?

		// log(LogLevel.INFO, "Original Sent : " + sent);
		text = text.substring(0, text.length() - 1); // remove the period at the
														// last position
		
		text = text.toLowerCase();
		patternStringGc = patternStringGc.toLowerCase();
		// String[] sentArray = sent.split(" ");
		// log(LogLevel.INFO, "sentArray.length :" + sentArray.length );

		// \s\d*\.\d*\s


		// Case 1:: The G+C contenc of DNA is 22.3 mol% (mol %).
		//Pattern patternGc = Pattern.compile(patternStringGc + "(.*)" + "(\\s*mol\\s*\\%\\s*|\\s*\\%\\s*|\\s*mol\\s*)");
		Pattern patternGc = Pattern.compile(patternStringGc + "(.*)" + "(\\s*mol\\s*\\%\\s*)");
		Matcher matcherGc = patternGc.matcher(text);

		while (matcherGc.find()) {
			System.out.println("Case 1::");
			// System.out.println("Whloe Sent::" + matcherGc.group());
			// System.out.println("Part 1::" + matcherGc.group(1));
			System.out.println("Part 2::" + matcherGc.group(2));
			// System.out.println("Part 3::" + matcherGc.group(3));
			String matchPartString = matcherGc.group(2);
			
			if ( ! matchPartString.equals("") ) {
				// adding sliding window? window size minus 5?
				//
				//
				Pattern targetPattern = Pattern.compile("\\b" + targetPatternString + "\\b");
				Matcher targetMatcher = targetPattern.matcher(matchPartString);
				while (targetMatcher.find()) {
					String matchPartString2 = targetMatcher.group(1);
					System.out.println("matchPartString2::" + matchPartString2);
					if ( isAcceptValueRange(matchPartString2) == true) {
						System.out.println("Add::" + matchPartString2);
						output.add(matchPartString2 + " mol%");
						regexResultWithMappingCaseMap.put("Case 1", matchPartString2 + " mol%");
					}
				}
			}
		}
		
		
		// Case 2: The mol% g+c of dna is 55–57.
		// Pattern patternGc2 = Pattern.compile("(.*)(\\s?\\s*mol\\s*\\%\\s*)(.*(?<!between))" + patternStringGc + "(\\s?.*\\s?)" + targetPatternString + "(.*)");
		Pattern patternGc2 = Pattern.compile("(\\s*mol\\s*\\%\\s*)" + "(.*)" + patternStringGc + "(.*)");
		Matcher matcherGc2 = patternGc2.matcher(text);

		while (matcherGc2.find()) {
			System.out.println("Case 2::");
			// System.out.println("Whloe Sent::" + matcherGc2.group());
			// System.out.println("Part 1::" + matcherGc2.group(1));
			// System.out.println("Part 2::" + matcherGc2.group(2));
			// System.out.println("Part 3::" + matcherGc2.group(3));
			System.out.println("Part 4::" + matcherGc2.group(4));
			// System.out.println("Part 5::" + matcherGc2.group(5 ));

			String matchPartString = matcherGc2.group(4);
			
			if ( ! matchPartString.equals("") ) {
				System.out.println("targetPatternString::" + targetPatternString);
				Pattern targetPattern = Pattern.compile("\\b" + targetPatternString + "\\b");
				Matcher targetMatcher = targetPattern.matcher(matchPartString);
				while (targetMatcher.find()) {
					String matchPartString2 = targetMatcher.group(1);
					System.out.println("matchPartString2::" + matchPartString2);
					if ( isAcceptValueRange(matchPartString2) == true) {
						System.out.println("Add::" + matchPartString2);
						output.add(matchPartString2 + " mol%");
						regexResultWithMappingCaseMap.put("Case 2", matchPartString2 + " mol%");
					}
				}
			}			
		}

		// Case 3:: The base composition is 32.5 to 34 mol % g+c is three strains.
		// Cannot handle this kind of example: 30.6 mol%
		// Cannot handle this kind of example: 30.6 mol% GC
		
		//Pattern patternGc3 = Pattern.compile("(.*)(\\s*mol\\s*\\%\\s*)(" + patternStringGc + ")?");
		// => this one is too wider
		Pattern patternGc3 = Pattern.compile("(.*)(\\s*mol\\s*\\%\\s*)" + patternStringGc);

		Matcher matcherGc3 = patternGc3.matcher(text);

		while (matcherGc3.find()) {
			System.out.println("Case 3::");
			// System.out.println("Whloe Sent::" + matcherGc3.group());
			System.out.println("Part 1::" + matcherGc3.group(1));
			// System.out.println("Part 2::" + matcherGc3.group(2));
			// System.out.println("Part 3::" + matcherGc3.group(3));
			// System.out.println("Part 4::" + matcherGc3.group(4));

			String matchPartString = matcherGc3.group(1);
			
			if ( ! matchPartString.equals("") ) {
				Pattern targetPattern = Pattern.compile("\\b" + targetPatternString + "\\b");
				Matcher targetMatcher = targetPattern.matcher(matchPartString);
				while (targetMatcher.find()) {
					String matchPartString2 = targetMatcher.group(1);
					System.out.println("matchPartString2::" + matchPartString2);
					if ( isAcceptValueRange(matchPartString2) == true) {
						System.out.println("Add::" + matchPartString2);
						output.add(matchPartString2 + " mol%");
						regexResultWithMappingCaseMap.put("Case 3", matchPartString2 + " mol%");

					}				
				}
			}			
		}

		
		// Case 4:: dna base composition: 63.4 moles guanine + cytosine.
		Pattern patternGc4 = Pattern.compile("(.*)(\\s*moles\\s*\\s*)(" + patternStringGc + ")?");
		Matcher matcherGc4 = patternGc4.matcher(text);
		while (matcherGc4.find()) {
			System.out.println("Case 4::");
			// System.out.println("Whloe Sent::" + matcherGc4.group());
			System.out.println("Part 1::" + matcherGc4.group(1));
			// System.out.println("Part 2::" + matcherGc4.group(2));
			// System.out.println("Part 3::" + matcherGc4.group(3));
			// System.out.println("Part 4::" + matcherGc4.group(4));

			String matchPartString = matcherGc4.group(1);
			
			if ( ! matchPartString.equals("") ) {
				Pattern targetPattern = Pattern.compile("\\b" + targetPatternString + "\\b");
				Matcher targetMatcher = targetPattern.matcher(matchPartString);
				while (targetMatcher.find()) {
					String matchPartString2 = targetMatcher.group(1);
					System.out.println("matchPartString2::" + matchPartString2);
					if ( isAcceptValueRange(matchPartString2) == true) {
						System.out.println("Add::" + matchPartString2);
						output.add(matchPartString2 + " mol%");
						regexResultWithMappingCaseMap.put("Case 4", matchPartString2 + " mol%");

					}				
				}
			}			
		}
		
		// Case 5: dna g + c content (mol%): 31.7-35.7 (bd, tm).
		Pattern patternGc5 = Pattern.compile(patternStringGc + "(.*)" + "(\\(?\\s*mol\\s*\\%\\s*\\)?\\:?)" + "(.*)");
		Matcher matcherGc5 = patternGc5.matcher(text);
		while (matcherGc5.find()) {
			System.out.println("Case 5::");
			// System.out.println("Whloe Sent::" + matcherGc5.group());
			// System.out.println("Part 1::" + matcherGc5.group(1));
			// System.out.println("Part 2::" + matcherGc5.group(2));
			// System.out.println("Part 3::" + matcherGc5.group(3));
			System.out.println("Part 4::" + matcherGc5.group(4));

			String matchPartString = matcherGc5.group(4);
			
			if ( ! matchPartString.equals("") ) {
				Pattern targetPattern = Pattern.compile("\\b" + targetPatternString + "\\b");
				Matcher targetMatcher = targetPattern.matcher(matchPartString);
				while (targetMatcher.find()) {
					String matchPartString2 = targetMatcher.group(1);
					System.out.println("matchPartString2::" + matchPartString2);
					if ( isAcceptValueRange(matchPartString2) == true) {
						System.out.println("Add::" + matchPartString2);
						output.add(matchPartString2 + " mol%");
						regexResultWithMappingCaseMap.put("Case 5", matchPartString2 + " mol%");

					}				
				}
			}			
		}		
		

		/*
		Pattern patternMol = Pattern.compile("(.*)(\\s*mol\\s*\\%\\s*)(.*)");
		Matcher matcherMol = patternMol.matcher(text);

		while (matcherMol.find()) {		
			String matchPart1String = matcherMol.group(1);
			String matchPart3String = matcherMol.group(3);
			
			if ( matchPart1String.matches("(.*)(" + patternStringGc + ")(.*)")) {
				System.out.println("Case 1");
				Pattern patternGc = Pattern.compile(patternStringGc + "(.*)");
				Matcher matcherGc = patternGc.matcher(matchPart1String);

				while (matcherGc.find()) {
					// System.out.println("Case 1::");
					// System.out.println("Whloe Sent::" + matcherGc.group());
					// System.out.println("Part 1::" + matcherGc.group(1));
					// System.out.println("Part 2::" + matcherGc.group(2));
					// System.out.println("Part 3::" + matcherGc.group(3));
					String matchPartString = matcherGc.group(2);
					
					if ( ! matchPartString.equals("") ) {
						// adding sliding window? window size minus 5?
						//
						//
						Pattern pattern2 = Pattern.compile("\\b" + targetPatternString + "\\b");
						Matcher matcher2 = pattern2.matcher(matchPartString);
						while (matcher2.find()) {
							String matchPartString2 = matcher2.group(1);
							output.add(matchPartString2);
						}
					}
				}			
			} else if ( matchPart3String.matches("(.*)(" + patternStringGc + ")(.*)") ) {
				System.out.println("Case 2 and case 3");
				
				Pattern patternGc = Pattern.compile("(.*)" + patternStringGc + "(.*)");
				Matcher matcherGc = patternGc.matcher(matchPart3String);
				while (matcherGc.find()) {
					String matchGcPart1String = matcherGc.group(1);
					String matchGcPart3String = matcherGc.group(3);
					System.out.println("matchGcPart1String::" + matchGcPart1String);
					System.out.println("matchGcPart3String::" + matchGcPart3String);
					
					if ( ! matchGcPart1String.equals("") ) {
						if (matchGcPart1String.matches("(.*)\\b" + targetPatternString + "\\b(.*)")) {
							System.out.println("Case 3-1");
							Pattern pattern2 = Pattern.compile("\\b" + targetPatternString + "\\b");
							Matcher matcher2 = pattern2.matcher(matchGcPart1String);
							while (matcher2.find()) {
								String matchPartString2 = matcher2.group(1);
								output.add(matchPartString2);
							}							
						}						
					}
					
					if ( ! matchGcPart3String.equals("") ) {
						if (matchGcPart3String.matches("(.*)\\b" + targetPatternString + "\\b(.*)")) {
							System.out.println("Case 3-2");
							Pattern pattern2 = Pattern.compile("\\b" + targetPatternString + "\\b");
							Matcher matcher2 = pattern2.matcher(matchGcPart3String);
							while (matcher2.find()) {
								String matchPartString2 = matcher2.group(1);
								output.add(matchPartString2);
							}							
						} else {
							if (matchPart1String.matches("(.*)\\b" + targetPatternString + "\\b(.*)")) {
								System.out.println("Case 2");
								Pattern pattern2 = Pattern.compile("\\b" + targetPatternString + "\\b");
								Matcher matcher2 = pattern2.matcher(matchPart1String);
								while (matcher2.find()) {
									String matchPartString2 = matcher2.group(1);
									output.add(matchPartString2);
								}								
							}
						}
					}
					
				}
			}
			
			
		}		
		*/

		
		return output;
	}
	
	public boolean isAcceptValueRange(String extractedValueText) {
		boolean isAccept = true;
		
		System.out.println("extractedValueText::0::" + extractedValueText); 
		
		Pattern patternNumber = Pattern.compile(myNumberPattern);
		Matcher matcherNumber = patternNumber.matcher(extractedValueText);

		int matchCounter = 0;
		while (matcherNumber.find()) {
			
			// System.out.println("" + matcherNumber.group(1));
			//System.out.println("" + matcherNumber.group(2));
			
			matchCounter++;
		}
		
		if ( matchCounter == 1) {
			String decimalPattern = myNumberPattern;  
			boolean match = Pattern.matches(decimalPattern, extractedValueText);
			System.out.println("extractedValueText::" + extractedValueText);
			System.out.println("match::" + match); //if true then decimal else not  
			
			if ( match != true ) {
				isAccept = false;
			} else {
				float extractedValueInt = Float.parseFloat(extractedValueText);
				if ( extractedValueInt > 99.9 ) {
					isAccept = false;
				}				
			}
		}
		
		return isAccept;
	}
	
	// Example: 
	public static void main(String[] args) throws IOException {		
		
		
		System.out.println("Start::");
		
		GcExtractor gcExtractor = new GcExtractor(Label.c1);	
		String patternStringGc = gcExtractor.getPatternStringGc();
		
		/*
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
			sourceSentText = sourceSentText.toLowerCase();
			patternStringGc = patternStringGc.toLowerCase();
			
			if ( sourceSentText.matches("(.*)" + patternStringGc + "(.*)")) {
				System.out.println("\n");
				System.out.println("sourceSentText::" + sourceSentText);
				Set<String> gcResult = gcExtractor.getCharacterValue(sourceSentText);
				System.out.println("gcResult::" + gcResult.toString());
				if ( gcResult.size() > 0 ) {
					extractedValueCounter +=1;
				}
				sampleSentCounter +=1;
			}
		
		}

		System.out.println("\n");
		System.out.println("sampleSentCounter::" + sampleSentCounter);
		System.out.println("extractedValueCounter::" + extractedValueCounter);
		*/
		
		// Test on June 10, 2014 Wednesday
		/*
		String testSent = "G +C content range is 3 1-33 mol%.";
		testSent = testSent.toLowerCase();
		patternStringGc = patternStringGc.toLowerCase();
		
		if ( testSent.matches("(.*)" + patternStringGc + "(.*)")) {
			System.out.println("\n");
			System.out.println("sourceSentText::" + testSent);
			Set<String> gcResult = gcExtractor.getCharacterValue(testSent);
			System.out.println("gcResult::" + gcResult.toString());
		}
		*/
		
		
		
		
		/*
		// Testing on :: myNumberPattern
		// String exText = "the range is 12.33.";
		String exText = "The dna g+c content of representative strains varied between 35 and 36 mol %.";
		String myNumberPattern = gcExtractor.getMyNumberPattern();
		String targetPatternString = gcExtractor.getTargetPatternString();
		//Pattern pattern = Pattern.compile(myNumberPattern);
		Pattern pattern = Pattern.compile(targetPatternString);
		Matcher matcher = pattern.matcher(exText);

		System.out.println("myNumberPattern::" + myNumberPattern);
		
		while (matcher.find()) {
			System.out.println("Whloe Sent::" + matcher.group());
			System.out.println("Part 1::" + matcher.group(1));
			// System.out.println("Part 2::" + matcherGc.group(2));
			// System.out.println("Part 3::" + matcherGc.group(3));
			// System.out.println("Part 4::" + matcherGc.group(4));
			// System.out.println("Part 5::" + matcherGc.group(5));
			String matchResult = matcher.group(1);
			System.out.println("matchResult::" + matchResult);
		}
		*/
		
		/*
		String newString = "";
		String testString = "AAA is bbb C. BBB is ccc D. The strain number is 123.";
		String testStringArray[] = testString.split("\\s+");
		String targetString = "";
		for ( int i = 0; i < testStringArray.length; i++ ) {
			String itemString = testStringArray[i];
			if ( itemString.matches("[A-Z]\\.") ) {
				System.out.println("111::222::itemString::" + itemString);
				targetString = itemString;
				for ( int j = 0; j < itemString.length(); j++ ) {
					newString += itemString.substring(j, j+1) + " ";
				}
			}
		}
		System.out.println("333::444::newString::" + newString);
		
		testString = testString.replaceAll(targetString, newString);
		
		System.out.println("555::666::testString::" + testString);
		*/
	
		/*
		String testString = "AAA is bbb C. BBB is ccc D. The strain number is 123.";
		System.out.println("testString::Before::" + testString);
		
		String targetPatternString = "(\\s[A-Z]\\.\\s)";
		Pattern pattern = Pattern.compile(targetPatternString);
		Matcher matcher = pattern.matcher(testString);
		
		while (matcher.find()) {
			System.out.println("Whloe Sent::" + matcher.group());
			System.out.println("Part 1::" + matcher.group(1));
			// System.out.println("Part 2::" + matcher.group(2));
			// System.out.println("Part 3::" + matcher.group(3));
			
			String matchString = matcher.group(1);
			
			String newMatchString = "";
			for ( int j = 0; j < matchString.length(); j++ ) {
				newMatchString += matchString.substring(j, j+1) + " ";
			}
			
			
			testString = testString.replaceAll(matcher.group(1), newMatchString);
			
			// String matchResult = matcher.group(1);
			// System.out.println("matchResult::" + matchResult);
		}
		System.out.println("testString::After::" + testString);
		*/
		
		// Test on February 04, 2015 Wed
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

		
		String outputFile = "micropieInput_zip_output/GC_Regex-150304.csv";
		OutputStream outputStream = new FileOutputStream(outputFile);
		CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF8")));
		List<String[]> lines = new LinkedList<String[]>();
		
		
		int sampleSentCounter = 0;
		int extractedValueCounter = 0;
		
		for (Sentence sourceSentence : sourceSentenceList) {
			String sourceSentText = sourceSentence.getText();
			sourceSentText = sourceSentText.toLowerCase();
			patternStringGc = patternStringGc.toLowerCase();
			
			if ( sourceSentText.matches("(.*)" + patternStringGc + "(.*)")) {
				System.out.println("\n");
				System.out.println("sourceSentText::" + sourceSentText);
				
				
				
				Set<String> gcResult = gcExtractor.getCharacterValue(sourceSentText);
				
				System.out.println("gcExtractor.getRegexResultWithMappingCaseMap()::" + gcExtractor.getRegexResultWithMappingCaseMap().toString());
				
				String regexResultWithMappingCaseMapString = "";
				
				for (Map.Entry<String, String> entry : gcExtractor.getRegexResultWithMappingCaseMap().entrySet()) {
					System.out.println("Key : " + entry.getKey() + " Value : "
					 	+ entry.getValue());
				
					regexResultWithMappingCaseMapString += entry.getKey() + ":" + entry.getValue() + ", ";
					
				}
				
				System.out.println("gcResult::" + gcResult.toString());
				if ( gcResult.size() > 0 ) {
					extractedValueCounter +=1;
				}
				sampleSentCounter +=1;
				
				System.out.println("regexResultWithMappingCaseMapString::" + regexResultWithMappingCaseMapString);

				
				lines.add(new String[] { sourceSentText,
						regexResultWithMappingCaseMapString
						} );
				
			} else {
				String sentLabel = sourceSentence.getLabel().getValue();
				
				if ( sentLabel.equals("1") ) {
					System.out.println("sentLabel::" + sentLabel);
					System.out.println("sourceSentText::" + sourceSentText);
					System.out.println("no case");
					lines.add(new String[] { sourceSentText,
							"No Case"
							} );
				}
				
				
				

			}
			
		
		
		} 

		System.out.println("\n");
		System.out.println("sampleSentCounter::" + sampleSentCounter);
		System.out.println("extractedValueCounter::" + extractedValueCounter);

		
		writer.writeAll(lines);
		writer.flush();
		writer.close();			
		
		
	}	
	
	
	
}
