package edu.arizona.biosemantics.micropie.extract.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RangePatternExtractor {

	private String rangePatternMax;
	private String rangePatternMin;
	
	public RangePatternExtractor(String rangePatternString) {
		// TODO Auto-generated constructor stub
		getRangePatternMax(rangePatternString);
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public String getRangePatternMax() {
		return rangePatternMax;
	}
	
	public String getRangePatternMin() {
		return rangePatternMin;
	}	
	
	public void getRangePatternMax(String rangePatternString) {
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

				"\\s\\d+\\.\\d+\\sand\\s\\d+\\.\\d+|" +
				"\\s\\d+\\.\\d+\\sand\\s\\d+|" +
				"\\s\\d+\\sand\\s\\d+\\.\\d+|" +
				"\\s\\d+\\sand\\s\\d+|" +
				
				"\\s\\d+\\.\\d+\\s\\d+\\.\\d+|" +
				"\\s\\d+\\.\\d+\\s\\d+|" +
				"\\s\\d+\\s\\d+\\.\\d+|" +
				"\\s\\d+\\s\\d+" +				
				
				")";
		// patternString =
		// "(.*)(\\s\\d*\\s\\+\\/\\-\\s\\d*\\s|\\s\\d*\\s|\\s\\d*\\.\\d*\\s|\\s\\d*\\-\\s*\\d*\\s)(.*)";

		Pattern patternRange = Pattern.compile(patternStringRange);
		Matcher matcherRange = patternRange.matcher(rangePatternString);			
		
		List<String> matchStringList = new ArrayList<String>();
		int matchCounter = 0;
		while (matcherRange.find()) {
			matchStringList.add(matcherRange.group().trim());
			matchCounter++;
		}
		
		// if (matchCounter > 1 ) {
		//	// System.out.println(" ::" + matcherRange.group());
		//	outpputContentList.add("matchStringList.get(0).toString()::" + matchStringList.get(0).toString());
		// }else {
		//	outpputContentList.add("matchStringList.get(0).toString()::" + matchStringList.get(0).toString());
		// }
		// outpputContentList.add("matchStringList.get(0).toString()::" + matchStringList.get(0).toString());
		
		String rangePatternMinValue= "0";
		String rangePatternMaxValue = "0";			
		if (matchStringList.size() > 0) {
			String rangeString = matchStringList.get(0).toString();
			if (rangeString.contains(" ")){
				String[] rangeStringArray = rangeString.split(" ");
				if (rangeStringArray.length > 1) {
					rangePatternMinValue = rangeStringArray[0].trim();
					rangePatternMaxValue = rangeStringArray[1].trim();
				}		
			}
			
			if (rangeString.contains("to")){
				String[] rangeStringArray = rangeString.split("to");
				if (rangeStringArray.length > 1) {
					rangePatternMinValue = rangeStringArray[0].trim();
					rangePatternMaxValue = rangeStringArray[1].trim();
				}		
			}
			if (rangeString.contains("-")){
				String[] rangeStringArray = rangeString.split("-");
				if (rangeStringArray.length > 1) {
					rangePatternMinValue = rangeStringArray[0].trim();
					rangePatternMaxValue = rangeStringArray[1].trim();
				}		
			}
			if (rangeString.contains("–")){
				String[] rangeStringArray = rangeString.split("–");
				if (rangeStringArray.length > 1) {
					rangePatternMinValue = rangeStringArray[0].trim();
					rangePatternMaxValue = rangeStringArray[1].trim();
				}		
			}			
			if (rangeString.contains("and")){
				String[] rangeStringArray = rangeString.split("and");
				if (rangeStringArray.length > 1) {
					rangePatternMinValue = rangeStringArray[0].replace("between", "");
					rangePatternMinValue = rangePatternMinValue.trim();
					rangePatternMaxValue = rangeStringArray[1].trim();
				}		
			}
			if (rangeString.contains("at least")){
				String[] rangeStringArray = rangeString.split("at least");
				if (rangeStringArray.length > 1) {
					rangePatternMinValue = rangeStringArray[1].trim();
					rangePatternMaxValue = "-";
				}		
			}
		
		}
		rangePatternMax = rangePatternMaxValue;
		rangePatternMin = rangePatternMinValue;
	}
}
