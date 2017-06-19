package edu.arizona.biosemantics.micropie.eval;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.classify.LabelPhraseValueType;
import edu.arizona.biosemantics.micropie.extract.StringValueFormatter;
import edu.arizona.biosemantics.micropie.io.CharacterReader;
import edu.arizona.biosemantics.micropie.io.XMLNewSchemaTextReader;
import edu.arizona.biosemantics.micropie.io.XMLTextReader;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;

public class Test {

	public static void main(String[] args) {
		String description = "methyl α-D-mannopyranoside";
		String keywordString = "methyl α-D-mannopyranoside";
		keywordString = keywordString.toLowerCase();
		String patternString = "^"+keywordString+"[,.?\\s]|[,.?\\s]"+keywordString+"[,.?\\s]|\\s"+keywordString+"$|^"+keywordString+"$"; // regular expression pattern
		
		
		description = description.replaceAll(patternString," <span style='background:#8DE0A2'>"+keywordString+"</span> ");
		
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(description.toLowerCase());			
		if (matcher.find() && keywordString.length() > 1) {
			String matchString = matcher.group().trim();
			System.out.println(matcher.start()+"-"+matcher.end());
			String repl = description.substring(matcher.start(),matcher.end());
			description = description.replaceAll(repl," <span style='background:#8DE0A2'>"+repl+"</span> ");
		}
		System.out.println(description);
	}
	
	
	public static String Stringinsert(String orgString,String addStringa,int indexa,String addStringb, int indexb){ 
		return orgString.substring(0,indexa)+addStringa+orgString.substring(indexa+1,indexb)+addStringb+orgString.substring(indexb+1,addStringb.length());
		} 

}
