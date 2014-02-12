package edu.arizona.biosemantics.micropie.extract.regex;

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
import edu.arizona.biosemantics.micropie.log.LogLevel;

public class GrowthNaclOptimumExtractor extends AbstractCharacterValueExtractor {
	
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
		Set<String> output = new HashSet<String>(); // Output, format::List<String>
		
		// input: the original sentnece
		// output: String array?
		
		
		// Example: 
		String patternString = "(.*)(\\s?optimum\\s?|\\s?optimal\\s?)(.*)(nacl)";
		
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text.toLowerCase());

		while (matcher.find()) {
			// System.out.println("Whloe Sent::" + matcher.group());
			// System.out.println("Part 1::" + matcher.group(1));
			// System.out.println("Part 2::" + matcher.group(2));
			System.out.println("Part 3::" + matcher.group(3));
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
		
		return output;
	}
}



