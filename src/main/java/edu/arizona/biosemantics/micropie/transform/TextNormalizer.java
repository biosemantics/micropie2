package edu.arizona.biosemantics.micropie.transform;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

public class TextNormalizer implements ITextTransformer {
	
	private LinkedHashMap<String, String> abbreviations;
	
	public TextNormalizer(LinkedHashMap<String, String> abbreviations) throws IOException {
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

			// System.out.println("111::" + tokenString);
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
				System.out.println("(::"+isStartP+"::"+isEndP+"::"+isNested);
			
			} else if (tokenString.contains(")")) {
				if (isNested == true) {
					isEndP = false;
					isNested = false;
				}else {
					isEndP = true;
					isStartP = false;
				}
				System.out.println(")::"+isStartP+"::"+isEndP+"::"+isNested);									
			}			
			
			if (tokenString.length() > 2) {
				if (tokenString.substring(0,1).equals("(") && tokenString.substring(tokenString.length()-1,tokenString.length()).equals(")")
						|| tokenString.substring(0,1).equals("(") && tokenString.substring(tokenString.length()-2,tokenString.length()-1).equals(")")) {
				
					String finalCompoundTokenReplacement = tokenString.replaceAll("\\.", "_dot");
					System.out.println("finalCompoundTokenReplacement ::" + finalCompoundTokenReplacement);
					text = text.replace(tokenString, finalCompoundTokenReplacement);
				
					parenthesisReplacements.put(tokenString, finalCompoundTokenReplacement);

					System.out.println(tokenString);
						
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
				
				
				String finalCompoundTokenReplacement = finalCompoundToken.replaceAll("\\.", "_dot");
				System.out.println("finalCompoundTokenReplacement ::" + finalCompoundTokenReplacement);
				text = text.replace(finalCompoundToken, finalCompoundTokenReplacement);
				
				parenthesisReplacements.put(finalCompoundToken, finalCompoundTokenReplacement);
				System.out.println(finalCompoundToken);
				
				compoundToken = " ";
				isEndP = false;
			} else if (isStartP == false && isEndP == false && isNested == false) {
				// System.out.println(tokenString);
				newSent += tokenString + " ";
			}

			// if (tokenString.contains(".") &&
			// tokenString.subSequence(tokenString.length()-1,
			// tokenString.length()).equals(".")) {
			// System.out.println(tokenString);
			// }

			// if
			// (tokenString.matches("G\\s*\\+\\s*C|g\\s*\\+\\s*c||GC||gc"))
			// {
			// System.out.println(tokenString);
			// isTokenMatch = true;
			// }

		}

		// System.out.println("newSent::" + newSent);
		System.out.println(newSent);
		
		replace(text, this.abbreviations);
		replace(text, parenthesisReplacements);
		return text;
	}
	
	public String replace(String text, LinkedHashMap<String, String> replacements) {
		for (String original : replacements.keySet()) {
			text = text.replaceAll(original, replacements.get(original));
		}
		return text;
	}
	
}
