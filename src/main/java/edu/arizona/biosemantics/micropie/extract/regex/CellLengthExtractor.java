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

public class CellLengthExtractor extends AbstractCharacterValueExtractor {

	public CellLengthExtractor(ILabel label) {
		super(label, "Cell length");
	}
	
	@Inject
	public CellLengthExtractor(@Named("CellLengthExtractor_Label")Label label, 
			@Named("CellLengthExtractor_Character")String character) {
		super(label, character);
	}
	
	@Override
	public Set<String> getCharacterValue(String text) {

		Set<String> output = new HashSet<String>(); // Output, format::List<String>
		
		// input: the original sentnece
		// output: String array?
		
		// Example: Cells are slender , cylindrical , sometimes crooked rods that are 0.35-0.5 µm wide and 2.5 µm long and occur singly or in pairs , or in longer chains.
		String patternString = "(.*)(\\s?µm\\slong\\s?)(.*)";
		
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text.toLowerCase());

		while (matcher.find()) {			
			String matchSubString = matcher.group(1);
			//System.out.println("matchSubString::" + matchSubString);
			String patternStringRange = "(" + 
					"\\d+\\.\\d+\\sto\\s\\d+\\.\\d+|" +
					"\\d+\\.\\d+\\sto\\s\\d+|" +
					"\\d+\\sto\\s\\d+\\.\\d+|" +
					"\\d+\\sto\\s\\d+|" +

					"\\d+\\.\\d+-\\d+\\.\\d+|" +
					"\\d+\\.\\d+-\\d+|" +
					"\\d+-\\d+\\.\\d+|" +
					"\\d+-\\d+|" +
					
					"\\d+\\.\\d+–\\d+\\.\\d+|" +
					"\\d+\\.\\d+–\\d+|" +
					"\\d+–\\d+\\.\\d+|" +						
					"\\d+–\\d+|" +

					"at least\\s\\d+\\.\\d+|" +
					"at least\\d+–\\d+|" +
					
					"between\\s\\d+\\.\\d+\\sand\\s\\d+\\.\\d+|" +
					"between\\s\\d+\\.\\d+\\sand\\s\\d+|" +
					"between\\s\\d+\\sand\\s\\d+\\.\\d+|" +
					"between\\s\\d+\\sand\\s\\d+|" +

					"\\d+\\.\\d+|" +
					"\\d+" +
					
					")";

			Pattern patternRange = Pattern.compile(patternStringRange);
			Matcher matcherRange = patternRange.matcher(matchSubString);			
			
			List<String> matchStringList = new ArrayList<String>();
			int matchCounter = 0;
			while (matcherRange.find()) {
				matchStringList.add(matcherRange.group().trim());
				matchCounter++;
			}
			
			String rangeString = "";

			if (matchStringList.size() > 0) {
				rangeString = matchStringList.get(matchStringList.size()-1).toString(); 
				// => find the closest range value region near the keyword
				// ex: 1-2 xxx xxx 3-4 μm long
			}
			
			rangeString += " µm";
		

			output.add(rangeString);	
			

		}
		
		return output;
	}
}