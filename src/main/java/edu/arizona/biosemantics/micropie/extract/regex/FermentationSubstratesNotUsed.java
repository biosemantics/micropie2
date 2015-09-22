package edu.arizona.biosemantics.micropie.extract.regex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.arizona.biosemantics.micropie.model.Sentence;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Extract the character 9.6 fermentation substrates not used 
 * Sample sentences:
 * 	1. Produces acid from glucosamine and ribose but not from L-or D-arabinose, xylose, rhamnose, cellobiose, melibiose, sucrose, raffinose, trehalose, glucose, lactose, maltose, fructose, galactose, mannose, inulin, mannitol, methyl a-D-glucoside, melezitose, methyl a-D-mannoside, malonate, adonitol, sodium gluconate, glycerol, salicin, dulcitol, inositol, sorbitol or xylitol after 1 week of incubation at optimum temperature and pH.  
 *	2. No production of acid in ammonium salt medium under aerobic conditions from adonitol, cellobiose, dulcitol, inositol, mannitol, raffinose, rhamnose, salicin, sorbitol, or sucrose.  
 *
 *	Method:
 *	1.	Regular Expression
 */
public class FermentationSubstratesNotUsed extends AbstractCharacterValueExtractor {

	public FermentationSubstratesNotUsed(@Named("FermentationSubstratesNotUsed_Label")ILabel label) {
		super(label, "fermentation substrates not used");
	}
	
	public FermentationSubstratesNotUsed(@Named("FermentationSubstratesNotUsed_Label")ILabel label, 
			@Named("FermentationSubstratesNotUsed_Character")String character) {
		super(label, character);
	}

	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {

		Set<String> output = new HashSet();
		List<CharacterValue> charValueList = null;
		
		String text = sentence.getText();

		String keywords = "Acidification|Gas|Acid|Acidified|pH lowered|pH drops";

		String[] keywordsArray = keywords.split("\\|");
		// System.out.println("keywordsArray.length::" + keywordsArray.length);
		
		// java string array set string - Google Search
		// collections - Java - easily convert array to set - Stack Overflow
		// http://stackoverflow.com/questions/3064423/java-easily-convert-array-to-set
		
		// java string[] set string - Google Search
		// arrays - Java/ How to convert String[] to List or Set - Stack Overflow
		// http://stackoverflow.com/questions/11986593/java-how-to-convert-string-to-list-or-set
		
		
		// Set<String> keywordList = new HashSet<String>(Arrays.asList("a", "b"));
		Set<String> keywordList = new HashSet<String>(Arrays.asList(keywordsArray)); 
		
		// System.out.println("Sent :" + sent);
		// text = text.substring(0, text.length()-1);
		// text = " " + text + " ";
		
		// String[] sentenceArray = text.split("\\.|\\band\\b");
		String[] sentenceArray = text.split("\\.");
		//System.out.println("sentenceArray.length :" + sentenceArray.length);
		
		for ( int i = 0; i < sentenceArray.length; i++ ) {
			
			String subText = sentenceArray[i] + ".";
			//System.out.println("subText :" + subText);

			int caseNumber = 0;

			if ( subText.matches("(.+)(\\bare not used|is not used\\b)(.+)") ) {
				caseNumber = 1;
				
			}
			
			
			switch(caseNumber) {
			case 1:
				//System.out.println("Case 1:");
				//System.out.println("subText :" + subText);
				subText = subText.substring(0, subText.length()-1);
				subText = " " + subText + " ";		

				Set<String> returnCharacterStrings = new HashSet<String>();

				for (String keywordString : keywordList) {
					keywordString = keywordString.toLowerCase();
					keywordString = keywordString.replace("+", "\\+");
					
					String patternString = "\\s"+keywordString+"\\,|\\s"+keywordString+"\\s|^"+keywordString+"\\s|^"+keywordString+"\\,"; // regular expression pattern
					// String patternString = "(.*)(\\b"+keywordString+"\\b)(.*)"; // regular expression pattern

					Pattern pattern = Pattern.compile(patternString);
					Matcher matcher = pattern.matcher(subText.toLowerCase());			
					if (matcher.find() && keywordString.length() > 1) {
						String matchString = matcher.group().trim();
						if(matchString.substring(matchString.length()-1, matchString.length()).equals(",")) {
							matchString = matchString.substring(0, matchString.length()-1);
						}
						returnCharacterStrings.add(matchString);
						//System.out.println(keywordString + "::" + matchString);
					}
				}
				output.addAll(returnCharacterStrings);
				
				break;
			case 2:

				break;
			default:
				// System.out.println("");
				// System.out.println("Go to Case 0::");
			}			
		}
		
		
		
		charValueList = CharacterValueFactory.createList(this.getLabel(), output);
		return charValueList;
	}

}