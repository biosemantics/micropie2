
package edu.arizona.biosemantics.micropie.extract.regex;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.NumericCharacterValue;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SubSentence;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.arizona.biosemantics.micropie.nlptool.StringUtil;
import edu.stanford.nlp.ling.TaggedWord;


/**
 * Extract the character 1.1 G+C
 * Sample sentences:
 * 	1.Mol % G+C ranges from 39-44.
 * 	2.DNA G+C content is 64.2–64.9 mol% (Tm method).
 *	
 *	Method:
 *	1.	Regular Expression
 */
public class GcFigureExtractor extends FigureExtractor {
	
	@Inject
	public GcFigureExtractor(SentenceSpliter sentSplitter,PosTagger posTagger, Label label, String characterName){
		super(label,characterName);
		this.sentSplitter = sentSplitter;
		this.posTagger = posTagger;
	}

	private String patternStringGc = "(" + 
										"\\bguanine plus cytosine|Guanosine plus cytosine|Guanosine plus cytosine|guanine-plus-cytosine\\b|" +
										"\\b\\(G\\s?\\+\\s?C\\b|" +
										"\\bG\\s?\\+\\s?C\\b|" + //zero or one
										"\\bG\\s*\\+\\s*C\\b|" + // zero or many // \\s+ => one or many
										// "\\bg\\s+\\+\\s+c\\b|" +
										// "\\s?\\(G+C\\s?|" + 
										// "\\s?G\\s*\\+\\s*C|" + 
										// "\\s+G\\s*\\+\\s*C\\s+|" + 
										// "\\s+g\\s*\\+\\s*c\\s+|" + 
										// "\\s+GC\\s+|" + 
										// "\\s+gc\\s+|" + 
										// "%GC|" + 
										// "%G+C" +
										"\\bGC\\b|" +
										"\\bguanine\\s*\\+\\s*cytosine\\b" +
										")";

	private String myNumberPattern = "(\\d+(\\.\\d+)?)";
	

	/*
	private String targetPatternString = "(" +
			
			"\\d+\\.\\d+±\\s?\\d+\\.\\d+|" + 
			"\\d+±\\s?\\d+\\.\\d+|" + 
			"\\d+\\.\\d+±\\s?\\d+|" + 
			"\\d+±\\s?\\d+|" + 
			
			"\\d+\\.\\d+\\s?-\\s?\\d+\\.\\d+\\s?|" +
			"\\d+\\s?-\\s?\\d+\\.\\d+\\s?|" +
			"\\d+\\.\\d+\\s?-\\s?\\d+\\s?|" +
			"\\d+\\s?-\\s?\\d+\\s?|" +
			
			"\\d+\\.\\d+\\s?–\\s?\\d+\\.\\d+\\s?|" +
			"\\d+\\s?–\\s?\\d+\\.\\d+\\s?|" +
			"\\d+\\.\\d+\\s?–\\s?\\d+\\s?|" +
			"\\d+\\s?–\\s?\\d+\\s?|" +
			
			"\\d+\\.\\d+\\s?and\\s?\\d+\\.\\d+\\s?|" +
			"\\d+\\s?and\\s?\\d+\\.\\d+\\s?|" +
			"\\d+\\.\\d+\\s?and\\s?\\d+\\s?|" +
			"\\d+\\s?and\\s?\\d+\\s?|" +
			
			"\\d+\\.\\d+\\s?\\d+\\.\\d+|" +
			"\\d+\\s?\\d+\\.\\d+|" +
			"\\d+\\.\\d+\\s?\\d+|" +
			"\\d+\\s?\\d+|" +
			
			"between\\s\\d+\\.\\d+\\sand\\s\\d+\\.\\d+|" +
			"between\\s\\d+\\.\\d+\\sand\\s\\d+|" +
			"between\\s\\d+\\sand\\s\\d+\\.\\d+|" +
			"between\\s\\d+\\sand\\s\\d+|" +
			
			"\\d+\\.\\d+|" +
			"\\d+|" +
			"\\d+\\s?" + 
			")";
	*/
	// private String targetPatternString = "(" +
	//		"(between\\s?|from\\s?)*" +
	//		myNumberPattern + "(\\s)*(\\()*(±|-|–|and|to|)*(\\s)*" + myNumberPattern + "*(\\))*" + 
	//		")";

	private String targetPatternString = "(" +
			"(between\\s*|from\\s*)?" + myNumberPattern + "\\s*(±|-|–|and|to|)?\\s*" + myNumberPattern + "|" +
			myNumberPattern + "\\s*(±|-|–|and|to|)?\\s*" + myNumberPattern +
			")";
	
	
	
	private Map<String, String> regexResultWithMappingCaseMap;

	public Map<String, String> getRegexResultWithMappingCaseMap() {
		return regexResultWithMappingCaseMap;
	}
	
	public String getTargetPatternString() {
		return targetPatternString;
	}
	
	public String getMyNumberPattern() {
		return myNumberPattern;
	}	
	
	public String getPatternStringGc() {
		return patternStringGc;
	}
	
	// private String patternStringGc = "\\s+G\\+C\\s+";
	// private String patternStringGc = "G\\s*\\+\\s*C";
	/*@Inject
	public GcExtractor(ILabel label) {
		super(label, "%G+C");
	}
	*/

	@Override
	
	/**
	 * questions:
	 * 	35-36 mol% (Tm)??
	 *  
	 * 
	 * 
	 */
	public List<CharacterValue> getCharacterValue(Sentence sent) {
		MultiClassifiedSentence sentence = (MultiClassifiedSentence)sent;
		this.posSentence(sentence);
		//System.out.println(sentence.getText());
		/**
		 * for each subsentences
		 * 	1, detect the figure
		 * 	2, find whether this figure is a range value
		 * 	3, find whether it's a gc value
		 */
		List<SubSentence> sents = sentence.getSubSentence();
		List<List<TaggedWord>> taggedWordList = sentence.getSubSentTaggedWords();
		//System.out.println(taggedWordList);
		int sentSize = taggedWordList.size();
		List sentValueList = new LinkedList();
		for(int sid=0;sid<sentSize;sid++){
			List<TaggedWord> taggedWords = taggedWordList.get(sid);
			
			//detect all the figures
			List valueList = detectFigures(taggedWords);
			
			//merge the figure ranges
			mergeFigureRange(valueList,taggedWords);
			sentValueList.addAll(valueList);
		}
		
		//filter values
		filterValues(sentValueList,sentence.getText());
		
		/*
		int fsize =  sentValueList.size();
		for(int i=0;i<fsize;i++){
			CharacterValue curFd = (CharacterValue)sentValueList.get(i);
			
			System.out.println(curFd.toString());
		}
		*/
		
		return sentValueList;
	}
	
	
	/**
	 * if more than 1 values:
	 * 	1. check the unit
	 *  2. check the confidence
	 * @param valueList
	 * @param text
	 */
	public void filterValues(List<NumericCharacterValue> valueList, String text) {
		System.out.println("is confident:"+this.confident(text));
		if(!this.confident(text)){
			valueList.clear();
		}
		int valueSize = valueList.size();
		if(valueSize==1){
			if(text.contains(" mol")||text.contains("%")){
				valueList.get(0).setUnit("mol%");
			}else{
				valueList.clear();
			}
			return;
		}
		for(int i=0;i<valueSize;){
			NumericCharacterValue curValue = valueList.get(i);
			if("%".equals(curValue.getUnit())&&text.indexOf("G")>-1&&text.indexOf("C")>-1){//find other information, add some features
				curValue.setUnit("mol%");
				i++;
			}else if("mol%".equals(curValue.getUnit())||"mol %".equals(curValue.getUnit())){
				i++;
			}else{//not % and not mol%
				valueList.remove(curValue);
				valueSize--;
			}
		}
	}
	
	
	public boolean confident(String text){
		String[] terms = text.split("[\\s\\+\\-\\.]+");
		int confidence = 0;
		if(StringUtil.contains(terms,"G")) confidence++;
		if(StringUtil.contains(terms,"C")) confidence++;
		if(StringUtil.contains(terms,"mol%")) confidence++;
		if(StringUtil.contains(terms,"mol")) confidence++;
		//guanine + cytosine
		if(StringUtil.contains(terms,"guanine")) confidence++;
		if(StringUtil.contains(terms,"cytosine")) confidence++;
		return confidence>0;
	}
	

	public Set<CharacterValue> getCharacterValueElvis(String text) {

		Set<String> output = new HashSet();
		Set<CharacterValue> charValueSet = null;
		regexResultWithMappingCaseMap = new HashMap<String, String>();
		
		text = text.substring(0, text.length() - 1); // remove the period at the last position
		text = text.toLowerCase();
		patternStringGc = patternStringGc.toLowerCase();

		// \s\d*\.\d*\s

		// Case 1:: The G+C content of DNA is 22.3 mol% (mol %).
		//Pattern patternGc = Pattern.compile(patternStringGc + "(.*)" + "(\\s*mol\\s*\\%\\s*|\\s*\\%\\s*|\\s*mol\\s*)");
		Pattern patternGc = Pattern.compile(patternStringGc + "(.*)" + "(\\s*mol\\s*\\%\\s*)");
		Matcher matcherGc = patternGc.matcher(text);

		while (matcherGc.find()) {
			String matchPartString = matcherGc.group(2);
			
			if ( ! matchPartString.equals("") ) {
				// adding sliding window? window size minus 5?
				//
				//
				Pattern targetPattern = Pattern.compile("\\b" + targetPatternString + "\\b");
				Matcher targetMatcher = targetPattern.matcher(matchPartString);
				while (targetMatcher.find()) {
					String matchPartString2 = targetMatcher.group(1);
					//System.out.println("matchPartString2::" + matchPartString2);
					if ( isAcceptValueRange(matchPartString2) == true) {
						//System.out.println("Add::" + matchPartString2);
						output.add(matchPartString2 + " mol%");
						regexResultWithMappingCaseMap.put("Case 1", matchPartString2 + " mol%");
					}
				}
			}
		}
		
		
		// Case 2: The mol% g+c of dna is 55–57.
		// Pattern patternGc2 = Pattern.compile("(.*)(\\s?\\s*mol\\s*\\%\\s*)(.*(?<!between))" + patternStringGc + "(\\s?.*\\s?)" + targetPatternString + "(.*)");
		Pattern patternGc2 = Pattern.compile("(\\s*mol\\s*\\%\\s*)" + "(.*)" + patternStringGc + "(.*)");
		Matcher matcherGc2 = patternGc2.matcher(text);

		while (matcherGc2.find()) {
			//System.out.println("Case 2::");
			// System.out.println("Whloe Sent::" + matcherGc2.group());
			// System.out.println("Part 1::" + matcherGc2.group(1));
			// System.out.println("Part 2::" + matcherGc2.group(2));
			// System.out.println("Part 3::" + matcherGc2.group(3));
			//System.out.println("Part 4::" + matcherGc2.group(4));
			// System.out.println("Part 5::" + matcherGc2.group(5 ));

			String matchPartString = matcherGc2.group(4);
			
			if ( ! matchPartString.equals("") ) {
				//System.out.println("targetPatternString::" + targetPatternString);
				Pattern targetPattern = Pattern.compile("\\b" + targetPatternString + "\\b");
				Matcher targetMatcher = targetPattern.matcher(matchPartString);
				while (targetMatcher.find()) {
					String matchPartString2 = targetMatcher.group(1);
					//System.out.println("matchPartString2::" + matchPartString2);
					if ( isAcceptValueRange(matchPartString2) == true) {
						//System.out.println("Add::" + matchPartString2);
						output.add(matchPartString2 + " mol%");
						regexResultWithMappingCaseMap.put("Case 2", matchPartString2 + " mol%");
					}
				}
			}			
		}

		// Case 3:: The base composition is 32.5 to 34 mol % g+c is three strains.
		// Cannot handle this kind of example: 30.6 mol%
		// Cannot handle this kind of example: 30.6 mol% GC
		
		//Pattern patternGc3 = Pattern.compile("(.*)(\\s*mol\\s*\\%\\s*)(" + patternStringGc + ")?");
		// => this one is too wider
		Pattern patternGc3 = Pattern.compile("(.*)(\\s*mol\\s*\\%\\s*)" + patternStringGc);

		Matcher matcherGc3 = patternGc3.matcher(text);

		while (matcherGc3.find()) {
			//System.out.println("Case 3::");
			// System.out.println("Whloe Sent::" + matcherGc3.group());
			//System.out.println("Part 1::" + matcherGc3.group(1));
			// System.out.println("Part 2::" + matcherGc3.group(2));
			// System.out.println("Part 3::" + matcherGc3.group(3));
			// System.out.println("Part 4::" + matcherGc3.group(4));

			String matchPartString = matcherGc3.group(1);
			
			if ( ! matchPartString.equals("") ) {
				Pattern targetPattern = Pattern.compile("\\b" + targetPatternString + "\\b");
				Matcher targetMatcher = targetPattern.matcher(matchPartString);
				while (targetMatcher.find()) {
					String matchPartString2 = targetMatcher.group(1);
					//System.out.println("matchPartString2::" + matchPartString2);
					if ( isAcceptValueRange(matchPartString2) == true) {
						//System.out.println("Add::" + matchPartString2);
						output.add(matchPartString2 + " mol%");
						regexResultWithMappingCaseMap.put("Case 3", matchPartString2 + " mol%");

					}				
				}
			}			
		}

		
		// Case 4:: dna base composition: 63.4 moles guanine + cytosine.
		Pattern patternGc4 = Pattern.compile("(.*)(\\s*moles\\s*\\s*)(" + patternStringGc + ")?");
		Matcher matcherGc4 = patternGc4.matcher(text);
		while (matcherGc4.find()) {
			//System.out.println("Case 4::");
			// System.out.println("Whloe Sent::" + matcherGc4.group());
			//System.out.println("Part 1::" + matcherGc4.group(1));
			// System.out.println("Part 2::" + matcherGc4.group(2));
			// System.out.println("Part 3::" + matcherGc4.group(3));
			// System.out.println("Part 4::" + matcherGc4.group(4));

			String matchPartString = matcherGc4.group(1);
			
			if ( ! matchPartString.equals("") ) {
				Pattern targetPattern = Pattern.compile("\\b" + targetPatternString + "\\b");
				Matcher targetMatcher = targetPattern.matcher(matchPartString);
				while (targetMatcher.find()) {
					String matchPartString2 = targetMatcher.group(1);
					//System.out.println("matchPartString2::" + matchPartString2);
					if ( isAcceptValueRange(matchPartString2) == true) {
						//System.out.println("Add::" + matchPartString2);
						output.add(matchPartString2 + " mol%");
						regexResultWithMappingCaseMap.put("Case 4", matchPartString2 + " mol%");

					}				
				}
			}			
		}
		
		// Case 5: dna g + c content (mol%): 31.7-35.7 (bd, tm).
		Pattern patternGc5 = Pattern.compile(patternStringGc + "(.*)" + "(\\(?\\s*mol\\s*\\%\\s*\\)?\\:?)" + "(.*)");
		Matcher matcherGc5 = patternGc5.matcher(text);
		while (matcherGc5.find()) {
			//System.out.println("Case 5::");
			// System.out.println("Whloe Sent::" + matcherGc5.group());
			// System.out.println("Part 1::" + matcherGc5.group(1));
			// System.out.println("Part 2::" + matcherGc5.group(2));
			// System.out.println("Part 3::" + matcherGc5.group(3));
			//System.out.println("Part 4::" + matcherGc5.group(4));

			String matchPartString = matcherGc5.group(4);
			
			if ( ! matchPartString.equals("") ) {
				Pattern targetPattern = Pattern.compile("\\b" + targetPatternString + "\\b");
				Matcher targetMatcher = targetPattern.matcher(matchPartString);
				while (targetMatcher.find()) {
					String matchPartString2 = targetMatcher.group(1);
					//System.out.println("matchPartString2::" + matchPartString2);
					if ( isAcceptValueRange(matchPartString2) == true) {
					//	System.out.println("Add::" + matchPartString2);
						output.add(matchPartString2 + " mol%");
						regexResultWithMappingCaseMap.put("Case 5", matchPartString2 + " mol%");

					}				
				}
			}			
		}		
		

		/*
		Pattern patternMol = Pattern.compile("(.*)(\\s*mol\\s*\\%\\s*)(.*)");
		Matcher matcherMol = patternMol.matcher(text);

		while (matcherMol.find()) {		
			String matchPart1String = matcherMol.group(1);
			String matchPart3String = matcherMol.group(3);
			
			if ( matchPart1String.matches("(.*)(" + patternStringGc + ")(.*)")) {
				System.out.println("Case 1");
				Pattern patternGc = Pattern.compile(patternStringGc + "(.*)");
				Matcher matcherGc = patternGc.matcher(matchPart1String);

				while (matcherGc.find()) {
					// System.out.println("Case 1::");
					// System.out.println("Whloe Sent::" + matcherGc.group());
					// System.out.println("Part 1::" + matcherGc.group(1));
					// System.out.println("Part 2::" + matcherGc.group(2));
					// System.out.println("Part 3::" + matcherGc.group(3));
					String matchPartString = matcherGc.group(2);
					
					if ( ! matchPartString.equals("") ) {
						// adding sliding window? window size minus 5?
						//
						//
						Pattern pattern2 = Pattern.compile("\\b" + targetPatternString + "\\b");
						Matcher matcher2 = pattern2.matcher(matchPartString);
						while (matcher2.find()) {
							String matchPartString2 = matcher2.group(1);
							output.add(matchPartString2);
						}
					}
				}			
			} else if ( matchPart3String.matches("(.*)(" + patternStringGc + ")(.*)") ) {
				System.out.println("Case 2 and case 3");
				
				Pattern patternGc = Pattern.compile("(.*)" + patternStringGc + "(.*)");
				Matcher matcherGc = patternGc.matcher(matchPart3String);
				while (matcherGc.find()) {
					String matchGcPart1String = matcherGc.group(1);
					String matchGcPart3String = matcherGc.group(3);
					System.out.println("matchGcPart1String::" + matchGcPart1String);
					System.out.println("matchGcPart3String::" + matchGcPart3String);
					
					if ( ! matchGcPart1String.equals("") ) {
						if (matchGcPart1String.matches("(.*)\\b" + targetPatternString + "\\b(.*)")) {
							System.out.println("Case 3-1");
							Pattern pattern2 = Pattern.compile("\\b" + targetPatternString + "\\b");
							Matcher matcher2 = pattern2.matcher(matchGcPart1String);
							while (matcher2.find()) {
								String matchPartString2 = matcher2.group(1);
								output.add(matchPartString2);
							}							
						}						
					}
					
					if ( ! matchGcPart3String.equals("") ) {
						if (matchGcPart3String.matches("(.*)\\b" + targetPatternString + "\\b(.*)")) {
							System.out.println("Case 3-2");
							Pattern pattern2 = Pattern.compile("\\b" + targetPatternString + "\\b");
							Matcher matcher2 = pattern2.matcher(matchGcPart3String);
							while (matcher2.find()) {
								String matchPartString2 = matcher2.group(1);
								output.add(matchPartString2);
							}							
						} else {
							if (matchPart1String.matches("(.*)\\b" + targetPatternString + "\\b(.*)")) {
								System.out.println("Case 2");
								Pattern pattern2 = Pattern.compile("\\b" + targetPatternString + "\\b");
								Matcher matcher2 = pattern2.matcher(matchPart1String);
								while (matcher2.find()) {
									String matchPartString2 = matcher2.group(1);
									output.add(matchPartString2);
								}								
							}
						}
					}
					
				}
			}
			
			
		}		
		*/

		System.out.println(output);
		charValueSet = CharacterValueFactory.createSet(this.getLabel(), output);
		return charValueSet;
	}
	
	
	
	
	
	public boolean isAcceptValueRange(String extractedValueText) {
		boolean isAccept = true;
		
		//System.out.println("extractedValueText::0::" + extractedValueText); 
		
		Pattern patternNumber = Pattern.compile(myNumberPattern);
		Matcher matcherNumber = patternNumber.matcher(extractedValueText);

		int matchCounter = 0;
		while (matcherNumber.find()) {
			
			// System.out.println("" + matcherNumber.group(1));
			//System.out.println("" + matcherNumber.group(2));
			
			matchCounter++;
		}
		
		if ( matchCounter == 1) {
			String decimalPattern = myNumberPattern;  
			boolean match = Pattern.matches(decimalPattern, extractedValueText);
			//System.out.println("extractedValueText::" + extractedValueText);
			//System.out.println("match::" + match); //if true then decimal else not  
			
			if ( match != true ) {
				isAccept = false;
			} else {
				float extractedValueInt = Float.parseFloat(extractedValueText);
				if ( extractedValueInt > 99.9 ) {
					isAccept = false;
				}				
			}
		}
		
		return isAccept;
	}
}
