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
 * Extract the character 2.2 Cell diameter
 * Sample sentences: 
 * 	1.The spherical heterocysts, 3-5 _ m in diameter, were usually located in the middle of the trichome, but also occasionally at the tip.
 *  2.Round cells, usually occurring in pairs, tetrads and clusters, about 1.0 µm in diameter.
 *
 *	Methods:
 *	1.Regular Expression
 *
 */
public class CellDiameterExtractor extends AbstractCharacterValueExtractor {

	public CellDiameterExtractor(ILabel label) {
		super(label, "Cell diameter");
	}
	
	@Inject
	public CellDiameterExtractor(@Named("CellDiameterExtractor_Label")Label label, 
			@Named("CellDiameterExtractor_Character")String character) {
		super(label, character);
	}
	
	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {

		Set<String> output = new HashSet();
		List<CharacterValue> charValueList = null;
		
		String text = sentence.getText();
		
		// Example:  Exponentially growing cells are 1-2-1.5 µm in diameter.
		String patternString = "(.*)(\\s?in\\sdiameter\\s?|\\s?diameter\\sof\\s?|\\s?diameters\\sof\\s?)(.*)";
		
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text.toLowerCase());

		while (matcher.find()) {
			String matchSubString;
			if (text.toLowerCase().contains("in diameter")) {
				matchSubString = matcher.group(1);
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
					// ex: 1-2 xxx xxx 3-4 μm in diameter
				}

				
				String unitString = "";
				String[] part1Array = matchSubString.split(" ");
				if (part1Array.length > 1 && !rangeString.equals(" ")) {
					int unitTokenPosition = 0;
					int rangeTokenPosition = 0;
					for( int i = 0; i < part1Array.length; i++) {							
						if (part1Array[i].toLowerCase().equals(rangeString)){
							rangeTokenPosition = i;
						}				
					}
					
					if ((part1Array.length-rangeTokenPosition) == 2) {
						unitTokenPosition = rangeTokenPosition + 1;
					}
					
					if (unitTokenPosition != 0) {
						unitString = part1Array[unitTokenPosition];
					}
				}

				output.add(rangeString + " " + unitString);				
			} else if (text.toLowerCase().contains("diameters of") || text.toLowerCase().contains("diameter of")) {
				matchSubString = matcher.group(3);
				// System.out.println("diameters of::" + matchSubString);
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
					rangeString = matchStringList.get(0).toString(); 
					// => find the closest range value region near the keyword
					// ex: diameter of xx-xx xxxx xx-xx μm
				}

				
				String unitString = "";
				String[] part1Array = matchSubString.split(" ");
				if (part1Array.length > 1 && !rangeString.equals(" ")) {
					int unitTokenPosition = 0;
					int rangeTokenPosition = 0;
					for( int i = 0; i < part1Array.length; i++) {							
						if (part1Array[i].toLowerCase().equals(rangeString)){
							rangeTokenPosition = i;
						}				
					}
					
					if ((part1Array.length-rangeTokenPosition) == 2) {
						unitTokenPosition = rangeTokenPosition + 1;
					}
					
					if (unitTokenPosition != 0) {
						unitString = part1Array[unitTokenPosition];
					}
				}

				output.add(rangeString + " " + unitString);				
				
			}
			
		}
		
		charValueList = CharacterValueFactory.createList(this.getLabel(), output);
		return charValueList;
	}
}