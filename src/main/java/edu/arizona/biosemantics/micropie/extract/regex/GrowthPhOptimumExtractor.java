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

public class GrowthPhOptimumExtractor extends AbstractCharacterValueExtractor {
	
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
		Set<String> output = new HashSet<String>(); // Output, format::List<String>
		
		// input: the original sentnece
		// output: String array?
		
		text = text.toLowerCase();
		// Example: 
		
		
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
		
		String patternString = "(.*)(\\s?optimum ph of\\s?|\\s?optimal ph of\\s?|\\s?optimal ph for growth is about\\s?|\\s?optimum growth at ph\\s?|\\s?optimum at ph\\s?|\\s?optimal growth at ph\\s?|\\s?the optimal ph was\\s?|\\s?optimal ph for growth is \\s?|\\s?optimum\\,\\s?ph\\s?|\\s?ph\\s?optimal\\s?|\\s?ph\\s?optimum\\s?|\\s?optimum\\s?ph\\s?|\\s?optimal\\s?ph\\s?)" +
				targetPatternString +
				"(.*)";


		
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text);

		while (matcher.find()) {
			// System.out.println("Whloe Sent::" + matcher.group());
			// System.out.println("Part 1::" + matcher.group(1));
			// System.out.println("Part 2::" + matcher.group(2));
			// System.out.println("Part 3::" + matcher.group(3));
			// System.out.println("Part 4::" + matcher.group(4));
			String targetPattern = matcher.group(3);
			output.add(targetPattern);
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
			// System.out.println("Whloe Sent::" + matcher2.group());
			// System.out.println("Part 1::" + matcher2.group(1));
			// System.out.println("Part 2::" + matcher2.group(2));
			// System.out.println("Part 3::" + matcher2.group(3));
			// System.out.println("Part 4::" + matcher2.group(4));
			// System.out.println("Part 5::" + matcher2.group(5));
			String targetPattern = matcher2.group(5);
			output.add(targetPattern);
		}		
		
		
		// Example: 
		// Growth is strictly aerobic and occurs between 4.0 and 36.0 ˚c (28.0–30.0 ˚c optimum), at ph 6.0–10.0 (ph 7.0–8.0 optimum) and at 1.0–6.0% nacl.

		
		String patternString3 = "(.*)(\\(\\s?ph\\s?)" +
				targetPatternString +
				"(\\s?optimum\\s?\\))(.*)";
		
		Pattern pattern3 = Pattern.compile(patternString3);
		Matcher matcher3 = pattern3.matcher(text);

		while (matcher3.find()) {
			// System.out.println("Whloe Sent::" + matcher3.group());
			// System.out.println("Part 1::" + matcher3.group(1));
			// System.out.println("Part 2::" + matcher3.group(2));
			// System.out.println("Part 3::" + matcher3.group(3));
			// System.out.println("Part 4::" + matcher3.group(4));
			// System.out.println("Part 5::" + matcher3.group(5));
			String targetPattern = matcher3.group(3);
			output.add(targetPattern);
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
			
			// pH
			if ( (sourceSentText.contains("ph") && sourceSentText.contains("optimal")) || 
					(sourceSentText.contains("ph") && sourceSentText.contains("optimum"))
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
		
		
	}



}



