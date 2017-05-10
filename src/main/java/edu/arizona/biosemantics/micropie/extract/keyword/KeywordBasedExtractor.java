package edu.arizona.biosemantics.micropie.extract.keyword;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.nlptool.INegationIdentifier;
import edu.arizona.biosemantics.micropie.nlptool.NegationIdentifier;


/**
 * Extractors for many keyword based character value
 * 
 */
public class KeywordBasedExtractor extends AbstractCharacterValueExtractor {
	
	protected INegationIdentifier negationIdentifier = new NegationIdentifier();
	protected Set<String> keywords;
	protected Map<String, List> subKeywords;
	
	public KeywordBasedExtractor(@Named("KeywordBasedExtractor_Label")ILabel label, 
			@Named("KeywordBasedExtractor_Character")String character, 
			@Named("KeywordBasedExtractor_Keywords")Set<String> keywords,
			Map<String, List> subKeyword) {
		super(label, character);
		this.keywords = keywords;
		this.subKeywords = subKeyword;
	}

	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {

		List<CharacterValue> charValueList = null;
		
		String text = sentence.getText();
		// TODO 
		// use keywords and regex to extract character values
		text = text.substring(0, text.length()-1);
		text = " " + text + " ";
		text = text.toLowerCase();
		
		Set<String> returnCharacterStrings = new HashSet<String>();
		for (String keywordString : keywords) {
			boolean isId = extract(keywordString, text, returnCharacterStrings);
			if(isId) continue;//if has found the value;
			List<String> subKeywordList = subKeywords.get(keywordString);
			if(subKeywordList==null) continue;
			for(String subKeyword : subKeywordList){
				boolean isExist = extract(subKeyword, text, returnCharacterStrings);
				if(isExist) break;
			}
		}
	   // System.out.println("returnCharacterStrings="+returnCharacterStrings);
		charValueList = CharacterValueFactory.createList(this.getLabel(), returnCharacterStrings);
		//System.out.println("returnCharacterStrings="+charValueList);
		return charValueList;
	}

	/**
	 * extract one character
	 * @param keywordString
	 * @param text
	 * @param returnCharacterStrings
	 */
	public boolean extract(String keywordString, String text, Set<String> returnCharacterStrings){
		keywordString = keywordString.toLowerCase();
		keywordString = keywordString.replace("+", "\\+");
	
		String patternString = "\\s"+keywordString+"[\\,\\.\\s\\?\\:]|\\s"+keywordString+"\\s|^"+keywordString+"\\s|\\s"+keywordString+"$"; // regular expression pattern
		//System.out.println(patternString);
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text);			
		if (matcher.find() && keywordString.length() > 1) {
			String matchString = matcher.group().trim();
			if(matchString.substring(matchString.length()-1, matchString.length()).equals(",")) {
				matchString = matchString.substring(0, matchString.length()-1);
			}
			returnCharacterStrings.add(matchString);
			return true;
		}
		return false;
	}
	
	/**
	 * read keywords from file
	 * @param keywordFile
	 */
	public Set<String> readKeywords(String keywordFile){
		try{
			keywords = new LinkedHashSet<String>();
			subKeywords = new LinkedHashMap<String, List>();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(keywordFile), "UTF8"));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				//jin 09-24-2015
				if(strLine.indexOf("|")>-1){
					if(strLine.startsWith("#")) continue;
					String[] fields = strLine.split("\\|");
					String keyword = fields[0].trim();
					keywords.add(keyword);
					subKeywords.put(keyword,new ArrayList());
					for(int i=0;i<fields.length;i++){
						subKeywords.get(keyword).add(fields[i].toString().trim());
					}
				}else{
					keywords.add(strLine.trim());
				}
				
			}
			br.close();
			return keywords;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * extract one character
	 * @param keywordString
	 * @param text
	 * @param returnCharacterStrings
	 */
	public boolean isExist(String keywordString, String text){
		keywordString = keywordString.toLowerCase().trim();
		keywordString = keywordString.replace("+", "\\+");
		//keywordString = keywordString.replace("-", " ");
		//text = text.replace("-", " ");
		String patternString = "^"+keywordString+"\\s|\\s"+keywordString+"\\s|\\s"+keywordString+"$|^"+keywordString+"$"; // regular expression pattern
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text);			
		if (matcher.find() && keywordString.length() > 1) {
			String matchString = matcher.group().trim();
			if(matchString.substring(matchString.length()-1, matchString.length()).equals(",")) {
				matchString = matchString.substring(0, matchString.length()-1);
			}
			return true;
		}
		return false;
	}
	
	public static void main(String[] args){
		String text = "The minimal Mg2+ concentration for growth and the Mg2+ concentration for optimal growth are 5 and 20 mM, respectively";
		String keywordString = "Mg2+";
		keywordString = keywordString.toLowerCase();
		keywordString = keywordString.replace("+", "\\+");
		
		String patternString = "\\s"+keywordString+"\\,|\\s"+keywordString+"\\s|^"+keywordString+"\\s|^"+keywordString+"\\,"; // regular expression pattern
		System.out.println(patternString);
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text.toLowerCase());			
		if (matcher.find() && keywordString.length() > 1) {
			String matchString = matcher.group().trim();
			System.out.println(matchString);
			if(matchString.substring(matchString.length()-1, matchString.length()).equals(",")) {
				matchString = matchString.substring(0, matchString.length()-1);
			}
			System.out.println(matchString);
		}
	}
}
