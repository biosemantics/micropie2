package edu.arizona.biosemantics.micropie.transform;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.log.LogLevel;

public class TextNormalizer implements ITextNormalizer {
	
	private LinkedHashMap<String, String> abbreviations;
	
	@Inject
	public TextNormalizer(@Named("Abbreviations")LinkedHashMap<String, String> abbreviations) throws IOException {
		this.abbreviations = abbreviations;
	}

	@Override
	public String transform(String text) {
		boolean isTokenMatch = false;
		StringTokenizer textToken = new StringTokenizer(text, " ");

		String newSent = "";

		boolean isStartP = false;
		boolean isEndP = false;
		boolean isNested = false;

		LinkedHashMap<String, String> parenthesisReplacements = new LinkedHashMap<String, String>();
		
		String compoundToken = "";

		
		// String termWithPeriod = "";
		
		while (textToken.hasMoreTokens()) {
			String tokenString = textToken.nextToken();

			// log(LogLevel.INFO, "111::" + tokenString);
			if (tokenString.length() > 2) {
				if (tokenString.substring(0,1).equals("(") && tokenString.substring(tokenString.length()-1,tokenString.length()).equals(")")
						|| tokenString.substring(0,1).equals("(") && tokenString.substring(tokenString.length()-2,tokenString.length()-1).equals(")")) {
				}
			
			} else if (tokenString.contains("(")) {
			// if (tokenString.contains("(")) {
				if (isStartP == true) {
					isNested = true;
				}else {
					isStartP = true;
					isEndP = false;
				}
				log(LogLevel.INFO, "(::"+isStartP+"::"+isEndP+"::"+isNested);
			
			} else if (tokenString.contains(")")) {
				if (isNested == true) {
					isEndP = false;
					isNested = false;
				}else {
					isEndP = true;
					isStartP = false;
				}
				log(LogLevel.INFO, ")::"+isStartP+"::"+isEndP+"::"+isNested);									
			}			
			
			if (tokenString.length() > 2) {
				if (tokenString.substring(0,1).equals("(") && tokenString.substring(tokenString.length()-1,tokenString.length()).equals(")")
						|| tokenString.substring(0,1).equals("(") && tokenString.substring(tokenString.length()-2,tokenString.length()-1).equals(")")) {
				
					String finalCompoundTokenReplacement = tokenString.replaceAll("\\.", "pp_dot");
					log(LogLevel.INFO, "finalCompoundTokenReplacement ::" + finalCompoundTokenReplacement);
					
					// it seems we don't have to do this
					// text = text.replace(tokenString, finalCompoundTokenReplacement);
					// it seems we don't have to do this
				
					parenthesisReplacements.put(tokenString, finalCompoundTokenReplacement);

					log(LogLevel.INFO, tokenString);
						
					compoundToken = " ";
				}
			} else if (isStartP == true && isEndP == false) {
				compoundToken += tokenString + " ";
			} else if (isStartP == false && isEndP == true) {
				compoundToken += tokenString + " ";
				newSent += compoundToken.trim() + " ";
				
				String finalCompoundToken = compoundToken.trim();
				
				
				if(finalCompoundToken.substring(finalCompoundToken.length()-1, finalCompoundToken.length()).equals(".")) {
					finalCompoundToken = finalCompoundToken.substring(0, finalCompoundToken.length()-1);
				}
				
				
				String finalCompoundTokenReplacement = finalCompoundToken.replaceAll("\\.", "pp_dot");
				log(LogLevel.INFO, "finalCompoundTokenReplacement ::" + finalCompoundTokenReplacement);
				
				// it seems we don't have to do this
				// text = text.replace(finalCompoundToken, finalCompoundTokenReplacement);
				// it seems we don't have to do this
				
				parenthesisReplacements.put(finalCompoundToken, finalCompoundTokenReplacement);
				log(LogLevel.INFO, finalCompoundToken);
				
				compoundToken = " ";
				isEndP = false;
			} else if (isStartP == false && isEndP == false && isNested == false) {
				// log(LogLevel.INFO, tokenString);
				newSent += tokenString + " ";
			}

			// if (tokenString.contains(".") &&
			// tokenString.subSequence(tokenString.length()-1,
			// tokenString.length()).equals(".")) {
			// log(LogLevel.INFO, tokenString);
			// }

			// if
			// (tokenString.matches("G\\s*\\+\\s*C|g\\s*\\+\\s*c||GC||gc"))
			// {
			// log(LogLevel.INFO, tokenString);
			// isTokenMatch = true;
			// }

		}

		// log(LogLevel.INFO, "newSent::" + newSent);
		log(LogLevel.INFO, newSent);
		
		text = replace(text, this.abbreviations);
		
		// text = replace(text, parenthesisReplacements); // mark it first since this one doesn't work very well
		
		// "DNA base composition 45 mol% G + C.  Temperature optimum 30 to 37˚C."
		text = text.replaceAll("G\\s\\+\\sC", "G+C");
		
		// TODO
		// spaceM. => spaceM . ex: 11-23 M.
		// "DNA base composition 45 mol% G + C.  Temperature optimum 30 to 37˚C."
		// streptomycin and polymycin B.  The polar lip
		// μ m => μm
		// 
		
		
		return text;
	}
	
	public String replace(String text, LinkedHashMap<String, String> replacements) {
		for (String original : replacements.keySet()) {
			//or was this meant to work as regex replace? (.replace vs .replaceAll)
			text = text.replace(original, replacements.get(original));
		}
		return text;
	}

	
	
	public String transformBack(String sent) {
		
		sent = replaceBack(sent, this.abbreviations);
		
		
		return sent;
	}

	public String replaceBack(String sent, LinkedHashMap<String, String> replacements) {
		for (String original : replacements.keySet()) {
			//or was this meant to work as regex replace? (.replace vs .replaceAll)
			sent = sent.replace(replacements.get(original), original);
		}
		return sent;
	}	
	
	
}
