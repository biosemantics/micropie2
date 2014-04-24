package edu.arizona.biosemantics.micropie.transform;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.arizona.biosemantics.micropie.model.CollapseUSPSentIndexMapping;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class CollapseUSPSentByCategoryChar {

	public CollapseUSPSentByCategoryChar() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CollapseUSPSentByCategoryChar collapseUSPSentByCategoryChar = new CollapseUSPSentByCategoryChar();
		
		// String oriSent = "Acetoin, indole and H2S are not produced.";
		String oriSent = "Acetoin , indole and H2S are not produced .";
		
		System.out.println("oriSent::" + oriSent);
		
		Hashtable<String, String> kwdListByCategory = new Hashtable<String, String>();
		
		kwdListByCategory.put("c9-Other", "indole");
		kwdListByCategory.put("c7-Tests", "indole");
		
		String collapseUSPSentByCategoryCharString = collapseUSPSentByCategoryChar.tagWithCategoryList(oriSent, kwdListByCategory);
		
		System.out.println("collapseUSPSentByCategoryCharString::" + collapseUSPSentByCategoryCharString);
		
	}
	
	
	public String tagWithCategoryList(String oriSent, Hashtable<String, String> kwdListByCategory) {

		String tagWithCategoryListSent = oriSent;
		
		Iterator<Map.Entry<String, String>> kwdListByCategoryIterator = kwdListByCategory.entrySet().iterator();

		while (kwdListByCategoryIterator.hasNext()) {
			Map.Entry<String, String> entry = kwdListByCategoryIterator.next();
			// System.out.println("Category and Character Name is ::" + entry.getKey());
			// System.out.println("Keyword List is ::" + entry.getValue());

			String category_char_name = entry.getKey();
			String patternString = entry.getValue();
			patternString = patternString.replaceAll("\\+", "\\\\+");
			patternString = patternString.replaceAll("\\(", "\\\\(");
			patternString = patternString.replaceAll("\\)", "\\\\)");
			
			patternString = "\\b(" + patternString + ")\\b";

			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(oriSent);
			while (matcher.find()) {
				// System.out.println("Start index: " + matcher.start());
				// System.out.println(" End index: " + matcher.end() + " ");
				// System.out.println("matcher.group()::" + matcher.group());
				
				// System.out.println("matcher.group()::" + oriSent.substring(matcher.start(), matcher.end()));
				
				String matchPattern = matcher.group();
				matchPattern = matchPattern.replaceAll("\\+", "\\\\+");
				matchPattern = matchPattern.replaceAll("\\(", "\\\\(");
				matchPattern = matchPattern.replaceAll("\\)", "\\\\)");
				
				String[] matchPatternArray = matchPattern.split("\\s+");
				String replacePattern = "";
				if ( matchPatternArray.length > 1) {
					for ( int i = 0; i < matchPatternArray.length; i++ ) {
						replacePattern += matchPatternArray[i] + "#";
					}
				} else {
					replacePattern = matchPattern;
				}
				
				if (replacePattern.substring(replacePattern.length()-1, replacePattern.length()).equals("#")) {
					replacePattern = replacePattern.substring(0, replacePattern.length()-1);
				}
				
				
				tagWithCategoryListSent = tagWithCategoryListSent.replaceAll("\\b(" + matchPattern + ")\\b", replacePattern + "[" + category_char_name + "]");
				
				
				
			}
		}
		
		// System.out.println("111::" + tagWithCategoryListSent );
		return tagWithCategoryListSent;
		
	}
	
	
	
	public CollapseUSPSentIndexMapping addMapping(String category, String indices) {
		CollapseUSPSentIndexMapping collapseUSPSentIndexMapping = new CollapseUSPSentIndexMapping();
		collapseUSPSentIndexMapping.setToken(category);
		collapseUSPSentIndexMapping.setIndices(indices);
		return collapseUSPSentIndexMapping;
	}
	

	public int tokenNumber(List<String> words) {
		
		// System.out.println("words.toString()::" + words.toString());
		// ?? need to be refined!!
		// no need
		
		int count = 0;
		ArrayList<String> indices = new ArrayList<String>();
		
		for (String word : words) {				
			if (word.matches("//W+")) {
				count++;
			} else {
				word = removeNumAndCategory(word); //turn ‘DDD#EEE[Cat]’ to ‘DDD EEE’
				String[] parts = word.split(" ");
				for ( String part: parts ){
					count++;
				}
			}
		}
		// System.out.println("tokenNumber::" + count);
		return count;
	}
	
	public String removeNumAndCategory(String word) {
		String removedCategoryTag = "";
		if (word.contains("[") && word.contains("]")) {
			int startIdx = word.indexOf("[");
			int endIdx = word.indexOf("]");
			removedCategoryTag = word.substring(startIdx, endIdx+1);
		}
		word = word.replace("#", " ");
		word = word.replace(removedCategoryTag, "");
		
		return word;
	}
	
	public List<String> fetchSubseqTokensOfCat(int index, String category, String[] tokens) {
		List<String> fetchSubseqTokensOfCat = new ArrayList<String>();
		
		for (int i = 0; i < tokens.length; i++) {
			
			if ( i >= index ) {
				//if ( tokens[i].equals(",") ||  tokens[i].equals("and")) {
				if ( tokens[i].equals(",") ) {
					fetchSubseqTokensOfCat.add(tokens[i]);
				} else {
					if ( tokens[i].contains(category) ) {
						fetchSubseqTokensOfCat.add(tokens[i]);
					} else if ( ! tokens[i].contains(category) ) {
						break;
					}
				}
			}
		}
		
		return fetchSubseqTokensOfCat;
	}
	
	public String getCategoryName(String token, Hashtable<String, String> kwdListByCategory) {
		
		String categoryName = "";
		Iterator<Map.Entry<String, String>> kwdListByCategoryIterator = kwdListByCategory.entrySet().iterator();
		while (kwdListByCategoryIterator.hasNext()) {
			Map.Entry<String, String> entry = kwdListByCategoryIterator.next();
			String category_char_name = entry.getKey();
			
			// System.out.println("token::" + token);
			if (token.contains("[") && token.contains("]")) {
				int startIdx = token.indexOf("[");
				int endIdx = token.indexOf("]");

				// System.out.println("startIdx::" + startIdx);
				// System.out.println("endIdx::" + endIdx);
				String tokenCategoryName = token.substring(startIdx+1, endIdx);
				
				if (tokenCategoryName.equals(category_char_name)) {
					categoryName = category_char_name;
				}				
			}			
		}
		return categoryName;
	}	
		
	
	

}
