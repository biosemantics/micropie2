package edu.arizona.biosemantics.micropie.extract.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RangePatternExtractor {

	// private String myNumberPattern = "(\\d+(\\.\\d+)?)"; 
	private String myNumberPattern = "(\\d+\\.?\\d+?)";
	
	public String getMyNumberPattern() {
		return myNumberPattern;
	}
	
	//
	// private String targetPatternString = "(" +
	//		"(between\\s?|from\\s?)*" +
	//		myNumberPattern + "(\\%|ph|celsius_degree)*(\\s)*(\\()*(±|-|–|and|to|or)*(\\s)*" + myNumberPattern + "*(\\))*" + 
	//		")";
	
	// The pH range for growth is 5.0-9.0, with an optimum between pH 6.0 and 7.0.
	// 
	// 
	// Add on March 04, 2015 Wednesday
	
	private String targetPatternString = "(" +
			"(between\\s*|from\\s*)?(pH)?" + myNumberPattern + "\\s*(±|-|–|and|to|)?\\s*" + myNumberPattern + "|" +
			myNumberPattern + "\\s*(±|-|–|and|to|)?\\s*" + myNumberPattern + 
			")";
	
	private String rangePatternMaxString;
	private String rangePatternMinString;
	
	
	public RangePatternExtractor(String rangePatternString, String symbol) {
		// TODO Auto-generated constructor stub
		
		System.out.println("Symbol::" + symbol);
		
		getRangePattern(rangePatternString, symbol);
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public String getRangePatternMaxString() {
		return rangePatternMaxString;
	}
	
	public String getRangePatternMinString() {
		return rangePatternMinString;
	}	
	
	public void getRangePattern(String rangePatternString, String symbol) {
		
		/*
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
		*/
		
		// patternString =
		// "(.*)(\\s\\d*\\s\\+\\/\\-\\s\\d*\\s|\\s\\d*\\s|\\s\\d*\\.\\d*\\s|\\s\\d*\\-\\s*\\d*\\s)(.*)";

		// Pattern patternRange = Pattern.compile(patternStringRange);
		Pattern patternRange = Pattern.compile("\\b" + targetPatternString + "\\b");
		
		Matcher matcherRange = patternRange.matcher(rangePatternString);			
		
		List<String> matchStringList = new ArrayList<String>();
		int matchCounter = 0;
		while (matcherRange.find()) {
			String matchRangeString = matcherRange.group();
			System.out.println("matchRangeString::" + matchRangeString);

			boolean isNextToRightSymbol = isNextToRightSymbol(rangePatternString, matchRangeString, symbol);
			if ( isNextToRightSymbol == true ) {
				System.out.println("Include::" + matchRangeString);
				matchStringList.add(matcherRange.group().trim());
				matchCounter++;	
			}
		}
		
		// if (matchCounter > 1 ) {
		//	// System.out.println(" ::" + matcherRange.group());
		//	outpputContentList.add("matchStringList.get(0).toString()::" + matchStringList.get(0).toString());
		// }else {
		//	outpputContentList.add("matchStringList.get(0).toString()::" + matchStringList.get(0).toString());
		// }
		// outpputContentList.add("matchStringList.get(0).toString()::" + matchStringList.get(0).toString());
		
		String rangePatternMinValue= "";
		String rangePatternMaxValue = "";			
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
		rangePatternMaxString = rangePatternMaxValue;
		rangePatternMinString = rangePatternMinValue;
	}
	
	public boolean isNextToRightSymbol(String matchPartString, String matchPartString2, String symbol) {
		
		boolean isNextToRightSymbol = false;
		
		matchPartString2 = matchPartString2.trim();
		String matchPartString2Condense = matchPartString2.replaceAll("\\s", "-");
		String matchPartStringCondense = matchPartString.replaceAll(Pattern.quote(matchPartString2), matchPartString2Condense);
		// System.out.println("matchPartStringCondense::" + matchPartStringCondense);
		
		matchPartString = matchPartStringCondense;
				
		matchPartString2 = matchPartString2Condense;
		
		String symbolArray[] = symbol.split(";");
		
		String matchPartStringArray[] = matchPartString.split(" ");
		// System.out.println("matchPartStringArray::" + Arrays.toString(matchPartStringArray));
		// System.out.println("matchPartString2::" + matchPartString2);
		// System.out.println("matchPartStringArray.length::" + matchPartStringArray.length);
		for ( int i = 0; i < matchPartStringArray.length; i++ ) {
			// System.out.println("matchPartStringArray[" + i + "]::" + matchPartStringArray[i]);
			
			if ( matchPartStringArray[i].contains(matchPartString2) ) {
				// System.out.println("matchPartStringArray[" + i + "]::" + matchPartStringArray[i]);
				for ( int j = 0; j < symbolArray.length; j++ ) {
					if (matchPartStringArray[i].contains(symbolArray[j])) {
						isNextToRightSymbol = true;
						// System.out.println("Include this!");
					}
				}
				
				
				if ( i > 0 ) {
					// System.out.println("matchPartStringArray[i-1]::" + matchPartStringArray[i-1]);
					for ( int j = 0; j < symbolArray.length; j++ ) {
						if (matchPartStringArray[i-1].contains(symbolArray[j])) {
							isNextToRightSymbol = true;
							// System.out.println("Include this!");
						}
					}
				}
				if ( i < matchPartStringArray.length -1 ) {
					// System.out.println("matchPartStringArray[i+1]::" + matchPartStringArray[i+1]);
					for ( int j = 0; j < symbolArray.length; j++ ) {
						if (matchPartStringArray[i+1].contains(symbolArray[j])) {
							isNextToRightSymbol = true;
							System.out.println("matchPartStringArray[i+1]::" + matchPartStringArray[i+1]);
							System.out.println("symbolArray[j]::" + symbolArray[j]);
							System.out.println("Include this!");
						}
					}					
				}
				
				if ( i > 5) {
					for ( int j = 0; j < 5; j++ ) {
						// System.out.println("matchPartStringArray[" + (i-j-1) + "]::" + matchPartStringArray[i-j-1]);
						if ( matchPartStringArray[i-j-1].contains("no") ||
								matchPartStringArray[i-j-1].contains("not") ||
								matchPartStringArray[i-j-1].contains("optimal") ||
								matchPartStringArray[i-j-1].contains("optimally") ||
								matchPartStringArray[i-j-1].contains("optimum")
								) {
							isNextToRightSymbol = false;
							System.out.println("Do not include this!");
						}
					}					
					
					
				}
				
				
				
			}
			
			
			/*
			if ( matchPartStringArray[i].contains(symbol) && matchPartStringArray[i].contains(matchPartString2) ) {
				// System.out.println("matchPartStringArray[i]::" + matchPartStringArray[i]);
				isNextToRightSymbolNeg = true;
			}
			
			if ( i > 0 && i < matchPartStringArray.length -1 ) {
				if ( matchPartStringArray[i+1].contains(symbol) && matchPartStringArray[i].contains(matchPartString2) ) {
					// System.out.println("matchPartStringArray[i+1]::" + matchPartStringArray[i+1]);
					isNextToRightSymbolNeg = true;
				}
				if ( matchPartStringArray[i-1].contains(symbol) && matchPartStringArray[i].contains(matchPartString2) ) {
					// System.out.println("matchPartStringArray[i-1]::" + matchPartStringArray[i-1]);
					isNextToRightSymbolNeg = true;
				}
			}
			*/
		}
		return isNextToRightSymbol;
	}	
	
}
