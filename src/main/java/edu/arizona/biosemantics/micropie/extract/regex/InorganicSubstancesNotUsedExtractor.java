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
 * Extract the character 9.4 inorganic substances not used
 * Sample sentences:
 * 	1.Forms sulfides from thiosulfate, but not sulfur.
 *	2.Nitrate reduction to nitrite and gas formation from nitrate are not observed.
 *
 *	Methods:
 *	1.USP
 *
 */
public class InorganicSubstancesNotUsedExtractor extends AbstractCharacterValueExtractor {

	public InorganicSubstancesNotUsedExtractor(@Named("InorganicSubstancesNotUsedExtractor_Label")ILabel label) {
		super(label, "inorganic substances not used");
	}
	
	public InorganicSubstancesNotUsedExtractor(@Named("InorganicSubstancesNotUsedExtractor_Label")ILabel label, 
			@Named("InorganicSubstancesNotUsedExtractor_Character")String character) {
		super(label, character);
	}

	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {

		Set<String> output = new HashSet();
		List<CharacterValue> charValueList = null;
		
		String text = sentence.getText();

		String keywords = "ACC|Acetamide|Ammonia|Ammonium|Ammonium nitrate|Ammonium sulfate|Arsenate|Arsenite|Arsenopyrite|Bentonite|Carbon dioxide|CH4|chalcopyrite|Chromate|Chromite|Chromium|Carbon monoxide|CO|CO2|Covellite|CuFeS2|CuS|Dihydrogen|Dimenthylamine|Dimethylamine|Dimethylamines|Dimethylselenide|Dinitrogen|Dinitrogen oxide|Dioxygen|Disodium selenate|DMA|Elemental selenium|Elemental sulfur|Elemental sulphur|Fe(II)|Fe(II)SO4|Fe(III)|Fe2O3|Fe3O4|FeCl3|FeCO3|Ferric iron|Ferrihydrite|Ferrous carbonate|Ferrous iron|FeS|FeS2|FeSO4|Formamide|greigite|H2|H2/CO2|H2/S|H2S|H2SO4|H2SO4|hematite|Hydrogen|Hydrogen gas|Hydrogen sulfide|Hydrogen sulphide|Iron oxidation|iron oxide|Iron-reducing|K2S4O6|Kaolin|KCN|KH2PO4|Magnetite|Manganese(IV)|Methane|Methylamine|Methylamines|Methylated amines|MMA|Mn(II)|Mn(IV)|Mn2+|Mn4+|MnSO4|Molecular nitrogen|Molybdate|Monomethylamine|N2|Na2S2O3|Na2S2O3|NH3|NH4|NH4Cl|Nitrate|Nitrate|nitric oxide|Nitrite|Nitrite|Nitrogen|Nitrogen dioxide|Nitrogen oxide|Nitrogen-Carbon dioxide|Nitrous oxide|NO2|NO3|O2|Oxygen|Poly(sulfide)|poly(sulphide)|Polyphosphate|Polysulfide|polysulfides|Polysulphide|polysulphides|potassium cyanate |Potassium nitrate|potassium thiocyanate|pyrite|Pyrrhotite|Realgar|S|S2O3|S2O3|Selenate|Selenite|Selenium|Senenite|Siderite|SO3|SO4|Sodium bicarbonate|Sodium bisulfite|Sodium bisulphate|Sodium nitrate|Sodium selenate|Sphalerite|Sulfate|Sulfide|Sulfidic ores|Sulfite|Sulfur|Sulfuric acid|Sulphate|Sulphite|Sulphur|Sulphur-reducing|Sulfur-reducing|Tetramethylammonium|Tetrathionate|Thiosulfate|Thiosulfite|Thiosulphate|TMA|TMAO|Trimethylamine|Trimethylamine N-oxide|Trimethylamines|Tungstate|Tungsten|Tungstite|ZnS";
		
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