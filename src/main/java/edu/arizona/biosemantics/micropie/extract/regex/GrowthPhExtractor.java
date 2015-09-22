package edu.arizona.biosemantics.micropie.extract.regex;

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
import edu.arizona.biosemantics.common.log.LogLevel;
/**
 * Extract the character 3.??
 * Sample sentences:
 * 	1. 
 * 	2. 
 *	
 *	Method:
 *	1.	Regular Expression
 */
public class GrowthPhExtractor extends AbstractCharacterValueExtractor {
	
	public GrowthPhExtractor(ILabel label) {
		super(label, "Growth PH");
	}
	
	@Inject
	public GrowthPhExtractor(@Named("GrowthPhExtractor_Label")Label label, 
			@Named("GrowthPhExtractor_Character")String character) {
		super(label, character);
	}
	
	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {

		Set<String> output = new HashSet();
		List<CharacterValue> charValueList = null;
		
		String text = sentence.getText();
		
		// log(LogLevel.INFO, "Original Sent : " + sent);	
		text = text.substring(0, text.length()-1); // remove the period at the last position
		
		// String[] sentArray = sent.split(" ");
		// log(LogLevel.INFO, "sentArray.length :"  + sentArray.length );
		
		// Example: pH range for growth is 6.0–9.5
		String patternString = "(.*)(\\s?ph\\s?range\\s?for\\s?growth\\s?)(.*)";
			
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text);

		while (matcher.find()) {
			// log(LogLevel.INFO, "Whloe Sent::" + matcher.group());
			// log(LogLevel.INFO, "Part 1::" + matcher.group(1));
			// log(LogLevel.INFO, "Part 2::" + matcher.group(2));
			log(LogLevel.INFO, "Part 3::" + matcher.group(3));
			String part3 = matcher.group(3);
			String patternStringRange = "(" + 
					"\\s\\d+$|" +
					"\\s\\d+\\s|" +
					"\\s\\d+\\.\\d*$|" +
					"\\s\\d+\\.\\d+\\s|" +
					
					"\\s\\d+\\.\\d+\\+\\/\\-\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\+\\/\\-\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\.\\d+\\+\\/\\-\\d+\\s|" + 
					"\\s\\d+\\+\\/\\-\\d+\\s|" + 

					"\\s\\d+\\.\\d+\\-\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\-\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\.\\d+\\-\\d+\\s|" + 
					"\\s\\d+\\-\\d+\\s|" + 

					"\\s\\d+\\.\\d+\\–\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\–\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\.\\d+\\–\\d+\\s|" + 
					"\\s\\d+\\–\\d+\\s" + 
					
					")";
					//"\\s\\d*\\.*\\-\\s*\\d*\\.*\\d*\\.|" + 
					//"\\s\\d*\\.\\d*\\-\\s*\\d*\\.\\d*\\s|" + 
					//"\\s\\d*\\.*\\d*\\s\\+\\/\\-\\s\\d*\\s|" + 
					//"\\s\\d*\\s|" + 
					//"\\s\\d*\\.\\d*\\s|" + 
					//"\\s\\d*\\-\\s*\\d*\\s|" + 
					//"\\s\\d*\\.*\\d*\\.)";
	
	
			// patternString =
			// "(.*)(\\s\\d*\\s\\+\\/\\-\\s\\d*\\s|\\s\\d*\\s|\\s\\d*\\.\\d*\\s|\\s\\d*\\-\\s*\\d*\\s)(.*)";

			Pattern patternRange = Pattern.compile(patternStringRange);
			Matcher matcherRange = patternRange.matcher(part3);			
	
			while (matcherRange.find()) {
				// log(LogLevel.INFO, " ::" + matcherRange.group());
				output.add("ph range for growth is " + matcherRange.group().trim());
			}
		}

		// Example: Cells grow at pH 1.7–6.5, and optimal growth occurs at pH 3.5.
		patternString = "(.*)(\\s?Cells\\s?grow\\s?at\\s?pH\\s?|at\\s?pH)(.*)";
		
		pattern = Pattern.compile(patternString);
		matcher = pattern.matcher(text);

		while (matcher.find()) {
			// log(LogLevel.INFO, "Whloe Sent::" + matcher.group());
			// log(LogLevel.INFO, "Part 1::" + matcher.group(1));
			// log(LogLevel.INFO, "Part 2::" + matcher.group(2));
			log(LogLevel.INFO, "Part 3::" + matcher.group(3));
			String part3 = matcher.group(3);
			String patternStringRange = "(" + 
					"\\s\\d+$|" +
					"\\s\\d+\\s|" +
					"\\s\\d+\\.\\d*$|" +
					"\\s\\d+\\.\\d+\\s|" +
					
					"\\s\\d+\\.\\d+\\+\\/\\-\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\+\\/\\-\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\.\\d+\\+\\/\\-\\d+\\s|" + 
					"\\s\\d+\\+\\/\\-\\d+\\s|" + 

					"\\s\\d+\\.\\d+\\-\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\-\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\.\\d+\\-\\d+\\s|" + 
					"\\s\\d+\\-\\d+\\s|" + 

					"\\s\\d+\\.\\d+\\–\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\–\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\.\\d+\\–\\d+\\s|" + 
					"\\s\\d+\\–\\d+\\s" + 
					
					")";
					//"\\s\\d*\\.*\\-\\s*\\d*\\.*\\d*\\.|" + 
					//"\\s\\d*\\.\\d*\\-\\s*\\d*\\.\\d*\\s|" + 
					//"\\s\\d*\\.*\\d*\\s\\+\\/\\-\\s\\d*\\s|" + 
					//"\\s\\d*\\s|" + 
					//"\\s\\d*\\.\\d*\\s|" + 
					//"\\s\\d*\\-\\s*\\d*\\s|" + 
					//"\\s\\d*\\.*\\d*\\.)";
	
	
			// patternString =
			// "(.*)(\\s\\d*\\s\\+\\/\\-\\s\\d*\\s|\\s\\d*\\s|\\s\\d*\\.\\d*\\s|\\s\\d*\\-\\s*\\d*\\s)(.*)";

			Pattern patternRange = Pattern.compile(patternStringRange);
			Matcher matcherRange = patternRange.matcher(part3);			
	
			while (matcherRange.find()) {
				log(LogLevel.INFO, " ::" + matcherRange.group());
				output.add("cells grow at pH " + matcherRange.group().trim());
			}
		}	

		// Example: Cells grow at pH 1.7–6.5, and optimal growth occurs at pH 3.5.
		patternString = "(.*)(\\s?Cells\\s?grow\\s?at\\s?pH\\s?|at\\s?pH)(.*)";
		
		pattern = Pattern.compile(patternString);
		matcher = pattern.matcher(text);

		while (matcher.find()) {
			// log(LogLevel.INFO, "Whloe Sent::" + matcher.group());
			// log(LogLevel.INFO, "Part 1::" + matcher.group(1));
			// log(LogLevel.INFO, "Part 2::" + matcher.group(2));
			log(LogLevel.INFO, "Part 3::" + matcher.group(3));
			String part3 = matcher.group(3);
			String patternStringRange = "(" + 
					"\\s\\d+$|" +
					"\\s\\d+\\s|" +
					"\\s\\d+\\.\\d*$|" +
					"\\s\\d+\\.\\d+\\s|" +
					
					"\\s\\d+\\.\\d+\\+\\/\\-\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\+\\/\\-\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\.\\d+\\+\\/\\-\\d+\\s|" + 
					"\\s\\d+\\+\\/\\-\\d+\\s|" + 

					"\\s\\d+\\.\\d+\\-\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\-\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\.\\d+\\-\\d+\\s|" + 
					"\\s\\d+\\-\\d+\\s|" + 

					"\\s\\d+\\.\\d+\\–\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\–\\d+\\.\\d+\\s|" + 
					"\\s\\d+\\.\\d+\\–\\d+\\s|" + 
					"\\s\\d+\\–\\d+\\s" + 
					
					")";
					//"\\s\\d*\\.*\\-\\s*\\d*\\.*\\d*\\.|" + 
					//"\\s\\d*\\.\\d*\\-\\s*\\d*\\.\\d*\\s|" + 
					//"\\s\\d*\\.*\\d*\\s\\+\\/\\-\\s\\d*\\s|" + 
					//"\\s\\d*\\s|" + 
					//"\\s\\d*\\.\\d*\\s|" + 
					//"\\s\\d*\\-\\s*\\d*\\s|" + 
					//"\\s\\d*\\.*\\d*\\.)";
	
	
			// patternString =
			// "(.*)(\\s\\d*\\s\\+\\/\\-\\s\\d*\\s|\\s\\d*\\s|\\s\\d*\\.\\d*\\s|\\s\\d*\\-\\s*\\d*\\s)(.*)";

			Pattern patternRange = Pattern.compile(patternStringRange);
			Matcher matcherRange = patternRange.matcher(part3);			
	
			while (matcherRange.find()) {
				log(LogLevel.INFO, " ::" + matcherRange.group());
				output.add("grow at pH " + matcherRange.group().trim());
			}
		}
		
		
		charValueList = CharacterValueFactory.createList(this.getLabel(), output);
		return charValueList;
	}
}
