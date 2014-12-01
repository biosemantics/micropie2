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

public class GrowthTempOptimumExtractor extends AbstractCharacterValueExtractor {
	
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
	
	public String getTargetPatternString() {
		return targetPatternString;
	}

	public GrowthTempOptimumExtractor(ILabel label) {
		super(label, "Temperature optimum");
	}
	
	@Inject
	public GrowthTempOptimumExtractor(@Named("GrowthTempOptimumExtractor_Label")Label label, 
			@Named("GrowthTempOptimumExtractor_Character")String character) {
		super(label, character);
	}
	
	@Override
	public Set<String> getCharacterValue(String text) {

		text = text.replaceAll("\\s?u C\\s?|\\s?°C\\s?|\\s?° C\\s?|\\s?˚C\\s?|\\s?◦C\\s?", " celsius_degree ");
		text = text.toLowerCase();
		// System.out.println("Modified sent::" + text);

		// input: the original sentnece
		// output: String array?
		Set<String> output = new HashSet<String>(); // Output, format::List<String>

		// Example: optimal temperature is 37°c; optimal temperature is 37˚c; optimum temperature is 37°c; optimum temperature is 37˚c;
		String patternString = "(.*)" + 
								"(" +
								// "\\(\\s(.*)(\\s?celsius_degree\\s?optimum\\s?)|" + // ??
								"(\\s?optimum,\\s?|\\s?optimal,\\s?)" + "(.*)" + "(\\s?celsius_degree\\s?)|" +
								"(\\s?optimum\\s?|\\s?optimal\\s?)" + "(.*)" + "(\\s?celsius_degree\\s?)|" +
								"(.*)(\\s?at\\s?)" + "(.*)" + "(\\s?celsius_degree\\s?with\\s?optimum\\s?)" + 
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
			
			// while (targetMatcher.find()) {
			if (targetMatcher.find()) { // Just choose the closest one (nearest one, first one)
				String matchPartString2 = targetMatcher.group(1);
				output.add(matchPartString2);
			}
		}			
		
		return output;
	}
	
	// Example: Growth occurs at 20–50 ˚C, with optimum growth at 37–45 ˚C.
	public static void main(String[] args) throws IOException {
		System.out.println("Start");
		GrowthTempOptimumExtractor growthTempOptimumExtractor = new GrowthTempOptimumExtractor(Label.c3);	
		
		CSVSentenceReader sourceSentenceReader = new CSVSentenceReader();
		// Read sentence list
		// 
		// sourceSentenceReader.setInputStream(new FileInputStream("split-additionalUSPInputs.csv"));
		
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
			
			// ˚C
			// if ( (sourceSentText.contains("celsius_degree") && sourceSentText.contains("optimal")) || 
			//		(sourceSentText.contains("celsius_degree") && sourceSentText.contains("optimum"))
			//		) {
			if ( (sourceSentText.matches("(.*)(\\bcelsius_degree\\b)(.*)") && sourceSentText.matches("(.*)(\\boptimal\\b)(.*)")) || 
					(sourceSentText.matches("(.*)(\\bcelsius_degree\\b)(.*)") && sourceSentText.matches("(.*)(\\boptimum\\b)(.*)"))
					) {			
				System.out.println("\n");
				System.out.println("sourceSentText::" + sourceSentText);
				Set<String> growTempOptimumResult = growthTempOptimumExtractor.getCharacterValue(sourceSentText);
				System.out.println("growTempOptimumResult::" + growTempOptimumResult.toString());
				if ( growTempOptimumResult.size() > 0 ) {
					extractedValueCounter +=1;
				}else {
					// System.out.println("\n");
					// System.out.println("sourceSentText::" + sourceSentText);
					// System.out.println("growTempOptimumResult::" + growTempOptimumResult.toString());
				}
				sampleSentCounter +=1;
			}
		
		}

		System.out.println("\n");
		System.out.println("sampleSentCounter::" + sampleSentCounter);
		System.out.println("extractedValueCounter::" + extractedValueCounter);
		
	}	
	
}



