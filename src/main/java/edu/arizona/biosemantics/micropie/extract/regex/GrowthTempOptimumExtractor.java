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

public class GrowthTempOptimumExtractor extends AbstractCharacterValueExtractor {
	
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

		text = text.replaceAll("\\s?u C\\s?|\\s?°C\\s?|\\s?° C\\s?|\\s?˚C\\s?", " celsius_degree ");
		text = text.toLowerCase();
		// System.out.println("Modified sent::" + text);

		// input: the original sentnece
		// output: String array?
		Set<String> output = new HashSet<String>(); // Output, format::List<String>
		
		// Example: optimal temperature is 37°c; optimal temperature is 37˚c; optimum temperature is 37°c; optimum temperature is 37˚c;
		String patternString = "(.*)(\\s?optimum\\s?|\\s?optimal\\s?)(.*)(\\s?celsius_degree\\s?)|" +
								"(.*)(\\s?at\\s?)(.*)(\\s?celsius_degree\\s?with\\s?optimum\\s?)(.*)";
		
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text);

		while (matcher.find()) {
			// System.out.println("Whloe Sent::" + matcher.group());
			// System.out.println("Part 1::" + matcher.group(1));
			// System.out.println("Part 2::" + matcher.group(2));
			// System.out.println("Part 3::" + matcher.group(3));
			String targetPattern = matcher.group(3);
			System.out.println("targetPattern::" + targetPattern);
			targetPattern = " " + targetPattern + " ";
			String[] matchPart3Array = targetPattern.split(" ");				

			int subMatchPart3Length = 5;
			if (matchPart3Array.length < subMatchPart3Length) {
				subMatchPart3Length = matchPart3Array.length;
			}
			StringBuilder subMatchPart3 = new StringBuilder();
			for (int i = 0; i < subMatchPart3Length; i++) {
				subMatchPart3.append(" " + matchPart3Array[i]);
			}
			
			System.out.println("subMatchPart3::" + subMatchPart3);
			
			
			// matchPart3 should be "is 3.7", "3.7", "2.3-2.5"

			String patternStringRange = "(" + 
				"\\d+\\.\\d+\\sto\\s\\d+\\.\\d+|" +
				"\\d+\\sto\\s\\d+|" +

				"\\d+\\.\\d+\\s?-\\s?\\d+\\.\\d+|" +
				"\\d+\\s?-\\s?\\d+|" +
				
				"\\d+\\.\\d+\\s?–\\s?\\d+\\.\\d+|" +
				"\\d+\\s?–\\s?\\d+|" +

				"\\d+\\.\\d+\\s?\\d+\\.\\d+|" +
				"\\d+\\s?\\d+|" +
				
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
		
		return output;
	}
	
	// Example: Growth occurs at 20–50 ˚C, with optimum growth at 37–45 ˚C.
	public static void main(String[] args) throws IOException {
		GrowthTempOptimumExtractor growthTempOptimumExtractor = new GrowthTempOptimumExtractor(Label.c3);	
		
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
			sourceSentText = sourceSentText.replaceAll("\\s?u C\\s?|\\s?°C\\s?|\\s?° C\\s?|\\s?˚C\\s?", " celsius_degree ");
			
			// ˚C
			if ( (sourceSentText.contains("celsius_degree") && sourceSentText.contains("optimal")) || 
					(sourceSentText.contains("celsius_degree") && sourceSentText.contains("optimum"))
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



