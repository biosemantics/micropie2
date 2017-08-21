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
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.Sentence;

/**
 * Extract the character 2.4 Cell Width
 * Sample sentences:
 * 	1. Cells are strictly aerobic, non-motile straight rods, approximately 1.5-2.0 µm in length and 0.5 µm in width, and form cream to light pink circular colonies with regular edges on TSA and 10-fold diluted LB agar.
 * 	2. Cells are approximately 0.3-0.4 x 2.5-6.3 µm.
 *	
 *	Method:
 *	1.	Regular Expression
 */
public class CellWidthExtractor extends AbstractCharacterValueExtractor {

	public CellWidthExtractor(ILabel label) {
		super(label, "Cell width");
	}
	
	@Inject
	public CellWidthExtractor(@Named("CellWidthExtractor_Label")Label label, 
			@Named("CellWidthExtractor_Character")String character) {
		super(label, character);
	}
	
	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		Set<String> output = new HashSet();
		List<CharacterValue> charValueList = null;
		String text = sentence.getText();
		//System.out.println(text);
		// Example: Cells are slender , cylindrical , sometimes crooked rods that are 0.35-0.5 µm wide and 2.5 µm long and occur singly or in pairs , or in longer chains.
		String patternString = "(.*)(\\s?µm\\swide\\s?)(.*)";
		
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
		//System.out.println(output);
		charValueList = CharacterValueFactory.createList(this.getLabel(), output);
		return charValueList;
	}
}