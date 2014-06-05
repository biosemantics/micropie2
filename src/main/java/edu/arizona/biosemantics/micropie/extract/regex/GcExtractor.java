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
import edu.arizona.biosemantics.micropie.model.Sentence;

public class GcExtractor extends AbstractCharacterValueExtractor {

	private String patternStringGc = "(" + 
										"\\bGuanosine plus cytosine|guanine-plus-cytosine\\b|" +
										"\\bG\\s?\\+\\s?C\\b|" + 
										"\\b\\(G\\s?\\+\\s?C\\b|" + 
										// "\\s?\\(G+C\\s?|" + 
										// "\\s?G\\s*\\+\\s*C|" + 
										// "\\s+G\\s*\\+\\s*C\\s+|" + 
										// "\\s+g\\s*\\+\\s*c\\s+|" + 
										// "\\s+GC\\s+|" + 
										// "\\s+gc\\s+|" + 
										// "%GC|" + 
										// "%G+C" +
										"\\bGC\\b" +
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
	private String targetPatternString = "(" +
			"(between\\s?|from\\s?)*" +
			myNumberPattern + "(\\s)*(\\()*(±|-|–|and|to|)*(\\s)*" + myNumberPattern + "*(\\))*" + 
			")";

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
			// System.out.println("Part 2::" + matcherGc.group(2));
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
					if ( isAcceptValueRange(matchPartString2) == true) {
						output.add(matchPartString2);
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
			// System.out.println("Part 4::" + matcherGc2.group(4));

			String matchPartString = matcherGc2.group(4);
			
			if ( ! matchPartString.equals("") ) {
				Pattern targetPattern = Pattern.compile("\\b" + targetPatternString + "\\b");
				Matcher targetMatcher = targetPattern.matcher(matchPartString);
				while (targetMatcher.find()) {
					String matchPartString2 = targetMatcher.group(1);
					if ( isAcceptValueRange(matchPartString2) == true) {
						output.add(matchPartString2);
					}				}
			}			
		}

		// Case 3:: The base composition is 32.5 to 34 mol % g+c is three strains.
		// Cannot handle this kind of example: 30.6 mol%
		// 30.6 mol% GC
		Pattern patternGc3 = Pattern.compile("(.*)(\\s*mol\\s*\\%\\s*)(" + patternStringGc + ")?");
		Matcher matcherGc3 = patternGc3.matcher(text);

		while (matcherGc3.find()) {
			System.out.println("Case 3::");
			// System.out.println("Whloe Sent::" + matcherGc3.group());
			// System.out.println("Part 1::" + matcherGc3.group(1));
			// System.out.println("Part 2::" + matcherGc3.group(2));
			// System.out.println("Part 3::" + matcherGc3.group(3));
			// System.out.println("Part 4::" + matcherGc3.group(4));

			String matchPartString = matcherGc3.group(1);
			
			if ( ! matchPartString.equals("") ) {
				Pattern targetPattern = Pattern.compile("\\b" + targetPatternString + "\\b");
				Matcher targetMatcher = targetPattern.matcher(matchPartString);
				while (targetMatcher.find()) {
					String matchPartString2 = targetMatcher.group(1);
					if ( isAcceptValueRange(matchPartString2) == true) {
						output.add(matchPartString2);
					}				}
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
			// System.out.println(match); //if true then decimal else not  
			
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
		
	}	
	
}
