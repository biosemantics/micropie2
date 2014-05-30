package edu.arizona.biosemantics.micropie.extract.regex;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

public class GrowthNaclOptimumExtractor extends AbstractCharacterValueExtractor {

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
	public Set<String> getCharacterValue(String text) {
		// input: the original sentnece
		// output: String array?
		Set<String> output = new HashSet<String>(); // Output, format::List<String>

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
			System.out.println("Part 2::" + matcher.group(2));
			// System.out.println("Part 3::" + matcher.group(3));
			String matchPartString = matcher.group(2);
			// System.out.println("targetPattern::" + targetPattern);
			// output.add(targetPattern);
			Pattern targetPattern = Pattern.compile("\\b" + targetPatternString + "\\b");
			Matcher targetMatcher = targetPattern.matcher(matchPartString);
			
			while (targetMatcher.find()) {
			// if (matcher2.find()) { // Just choose the closest one (nearest one, first one)
				String matchPartString2 = targetMatcher.group(1);
				
				if ( isNextToRightSymbol(matchPartString, matchPartString2, "%") ) {
					output.add(matchPartString2);
				}
				

			}
			
		}			
		
		return output;			
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
	
	// Example: Growth occurs with 1–9% (w/v) NaCl (optimum 2–3 %), at pH 5–8 (optimum pH 7) and at 10–40 ˚C (optimum 25– 35 ˚C).
	public static void main(String[] args) throws IOException {
		System.out.println("Start");
		GrowthNaclOptimumExtractor growthNaClOptimumExtractor = new GrowthNaclOptimumExtractor(Label.c3);	
		
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
			
			// NaCl (w/v) %
			if ( (sourceSentText.matches("(.*)(\\%)(.*)") && sourceSentText.matches("(.*)(\\boptimal\\b)(.*)")) || 
					(sourceSentText.matches("(.*)(\\%)(.*)") && sourceSentText.matches("(.*)(\\boptimum\\b)(.*)"))
					) {			
				System.out.println("\n");
				System.out.println("sourceSentText::" + sourceSentText);
				Set<String> growNaclOptimumResult = growthNaClOptimumExtractor.getCharacterValue(sourceSentText);
				System.out.println("growNaclOptimumResult::" + growNaclOptimumResult.toString());
				if ( growNaclOptimumResult.size() > 0 ) {
					extractedValueCounter +=1;
				}else {
					// System.out.println("\n");
					// System.out.println("sourceSentText::" + sourceSentText);
					// System.out.println("growNaclOptimumResult::" + growNaclOptimumResult.toString());
				}
				sampleSentCounter +=1;
			}
		
		}

		System.out.println("\n");
		System.out.println("sampleSentCounter::" + sampleSentCounter);
		System.out.println("extractedValueCounter::" + extractedValueCounter);
		
	}	
	
}



