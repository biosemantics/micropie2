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

public class GrowthPhMinExtractor extends AbstractCharacterValueExtractor {
	
	public GrowthPhMinExtractor(ILabel label) {
		super(label, "pH minimum");
	}
	
	@Inject
	public GrowthPhMinExtractor(@Named("GrowthPhMinExtractor_Label")Label label, 
			@Named("GrowthPhMinExtractor_Character")String character) {
		super(label, character);
	}
	
	@Override
	public Set<String> getCharacterValue(String text) {
		Set<String> output = new HashSet<String>(); // Output, format::List<String>
		
		// input: the original sentnece
		// output: String array?
		
		// Example:  ... pH range 5-40˚C ..., The pH range for growth is 18 to 37°C.
		String patternString = "(.*)(\\s?ph range\\s?)(.*)";
		
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text.toLowerCase());

		while (matcher.find()) {
			// System.out.println("Whloe Sent::" + matcher.group());
			// System.out.println("Part 1::" + matcher.group(1));
			// System.out.println("Part 2::" + matcher.group(2));
			// System.out.println("pH range::" + matcher.group(3));
			String part3 = matcher.group(3);
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
			// patternString =
			// "(.*)(\\s\\d*\\s\\+\\/\\-\\s\\d*\\s|\\s\\d*\\s|\\s\\d*\\.\\d*\\s|\\s\\d*\\-\\s*\\d*\\s)(.*)";

			Pattern patternRange = Pattern.compile(patternStringRange);
			Matcher matcherRange = patternRange.matcher(part3);			
			
			List<String> matchStringList = new ArrayList<String>();
			int matchCounter = 0;
			while (matcherRange.find()) {
				matchStringList.add(matcherRange.group().trim());
				matchCounter++;
			}
			
			// if (matchCounter > 1 ) {
			//	// System.out.println(" ::" + matcherRange.group());
			//	outpputContentList.add("temperature range " + matchStringList.get(0).toString());
			// }else {
			//	outpputContentList.add("temperature range " + matchStringList.get(0).toString());
			// }
			
			// outpputContentList.add("temperature range " + matchStringList.get(0).toString());

			String growPhMin = "0";
			String growPhMax = "0";			
			if (matchStringList.size() > 0) {
				String rangeString = matchStringList.get(0).toString();
				if (rangeString.contains("to")){
					String[] rangeStringArray = rangeString.split("to");
					if (rangeStringArray.length > 1) {
						growPhMin = rangeStringArray[0].trim();
						growPhMax = rangeStringArray[1].trim();
					}		
				}
				if (rangeString.contains("-")){
					String[] rangeStringArray = rangeString.split("-");
					if (rangeStringArray.length > 1) {
						growPhMin = rangeStringArray[0].trim();
						growPhMax = rangeStringArray[1].trim();
					}		
				}
				if (rangeString.contains("–")){
					String[] rangeStringArray = rangeString.split("–");
					if (rangeStringArray.length > 1) {
						growPhMin = rangeStringArray[0].trim();
						growPhMax = rangeStringArray[1].trim();
					}		
				}			
				if (rangeString.contains("and")){
					String[] rangeStringArray = rangeString.split("and");
					if (rangeStringArray.length > 1) {
						growPhMin = rangeStringArray[0].trim();
						growPhMax = rangeStringArray[1].trim();
					}		
				}
			}	
				
				
			// output.add("pH range " + matchStringList.get(0).toString());
			// output.add("growPhMin " + growTempMin);
			// output.add("growPhMax " + growTempMax);
			output.add(growPhMin);
		}		
		
		return output;
	}
}



