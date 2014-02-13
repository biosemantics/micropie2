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

public class GrowthNaclMinExtractor extends AbstractCharacterValueExtractor {
	
	public GrowthNaclMinExtractor(ILabel label) {
		super(label, "NaCl minimum");
	}
	
	@Inject
	public GrowthNaclMinExtractor(@Named("GrowthNaclMinExtractor_Label")Label label, 
			@Named("GrowthNaclMinExtractor_Character")String character) {
		super(label, character);
	}
	
	@Override
	public Set<String> getCharacterValue(String text) {
		Set<String> output = new HashSet<String>(); // Output, format::List<String>
		
		// input: the original sentnece
		// output: String array?
		
		// Example: The NaCl range for growth is 2.2 M to saturation , with an optimum of 3.4 M NaCl.
		// Example: Growth requires at least 1.7 M NaCl , optimally 2.6–4.3 M NaCl.
		// Example: Requires 15 to 30% NaCl for growth; optimum , 20% ( 3.5 M NaCl ).
		
		// Regular expression logic expression
		// first, find the sentence containing "NaCl"
		// in general, this sentence will contain two chunks
		// check if it contains "," or ";"
		// if yes, divide it into two parts
		// choose the part without "optimal" or "optimum"
		// 
		
		int naclCount = 0;
		String patternString = "(nacl|\\s?nacl\\s?)";
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text.toLowerCase());

		while (matcher.find()) {
			naclCount+=1;
		}
		
		String targetString = "";
		String[] textArray;
		if (naclCount > 1) {

			// detect "," or ";"
			int caseNumber = 0;
			if (text.contains(",")) {
				caseNumber = 1;
			} else if (text.contains(";")) {
				caseNumber = 2;
			} else if (text.contains("\\(")) {
				caseNumber = 3;
			}
							

			switch (caseNumber) {
			case 1:				
				textArray = text.split(",");
				if (textArray.length > 1) {
					targetString = textArray[0];
				} 
			case 2:
				textArray = text.split(";");
				if (textArray.length > 1) {
					targetString = textArray[0];
				}
			case 3:
				textArray = text.split("\\(");
				if (textArray.length > 1) {
					targetString = textArray[0];
				}
			default:
				// return null;
			}
		} else {
			targetString = text;
		}
		
		
		targetString = " " + targetString + " ";
		
		if ( !targetString.contains("optimal") && !targetString.contains("optimum")) {
			// System.out.println("targetString::" + targetString);

			patternString = "(.*)(m\\s?nacl\\s?|%\\s?nacl\\s?)";
			pattern = Pattern.compile(patternString);
			matcher = pattern.matcher(targetString.toLowerCase());			
			
			while (matcher.find()) {
				String naclRangeDesc = matcher.group(1);
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
						"between\\s\\d+\\sand\\s\\d+" +

						")";

				Pattern patternRange = Pattern.compile(patternStringRange);
				Matcher matcherRange = patternRange.matcher(naclRangeDesc);			
				
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
				String growNaclMin = "0";
				String growNaclMax = "0";			
				if (matchStringList.size() > 0) {
					String rangeString = matchStringList.get(matchStringList.size()-1).toString();

					if (rangeString.contains("to")){
						String[] rangeStringArray = rangeString.split("to");
						if (rangeStringArray.length > 1) {
							growNaclMin = rangeStringArray[0].trim();
							growNaclMax = rangeStringArray[1].trim();
						}		
					}
					if (rangeString.contains("-")){
						String[] rangeStringArray = rangeString.split("-");
						if (rangeStringArray.length > 1) {
							growNaclMin = rangeStringArray[0].trim();
							growNaclMax = rangeStringArray[1].trim();
						}		
					}
					if (rangeString.contains("–")){
						String[] rangeStringArray = rangeString.split("–");
						if (rangeStringArray.length > 1) {
							growNaclMin = rangeStringArray[0].trim();
							growNaclMax = rangeStringArray[1].trim();
						}		
					}			
					if (rangeString.contains("and")){
						String[] rangeStringArray = rangeString.split("and");
						if (rangeStringArray.length > 1) {
							growNaclMin = rangeStringArray[0].replace("between", "");
							growNaclMin = growNaclMin.trim();
							growNaclMax = rangeStringArray[1].trim();
						}		
					}
					if (rangeString.contains("at least")){
						String[] rangeStringArray = rangeString.split("at least");
						if (rangeStringArray.length > 1) {
							growNaclMin = rangeStringArray[1].trim();
							growNaclMax = "-";
						}		
					}
				}
				
				// System.out.println("growNaclMin" + growNaclMin);
				// System.out.println("growNaclMax" + growNaclMax);
				if (patternString.toLowerCase().contains("m nacl")) {
					growNaclMin += " M";
					growNaclMax += " M";
				} else if (patternString.toLowerCase().contains("% nacl")) {
					growNaclMin += " %";
					growNaclMax += " %";
				}
				// (\\s?nacl\\s?)(.*)(%\\s?)(.*)|(\\s?nacl\\s?)(.*)(\\sm\\s)(.*)

				
				
				// output.add("NaCl range " + matchStringList.get(matchStringList.size()-1).toString());
				// output.add("growNaclMin " + growNaclMin);
				// output.add("growNaclMax " + growNaclMax);
				output.add(growNaclMin);				
			}

		}		
		
		return output;
	}
}



