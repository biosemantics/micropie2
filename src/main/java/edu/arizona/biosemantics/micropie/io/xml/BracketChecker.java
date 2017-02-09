package edu.arizona.biosemantics.micropie.io.xml;

public class BracketChecker {
	
	/**
	 * Give toMatch and matcher already as quoted strings. Becuase there seems to be a bug in the Regex/replaceAll GWT implementation
	 * To many things are matched/replaced. Event if there is none of those characters in the string at all.
	 * @param text
	 * @param toMatch
	 * @param matcher
	 * @return
	 */
	public boolean hasUnmatchedCharacter(String text, String toMatch, String matcher) {
		int toMatchAmount = text.replaceAll("[^" + toMatch + "]", "").length();
		int matcherAmount = text.replaceAll("[^" + matcher + "]", "").length();
		
		if(toMatchAmount > matcherAmount) {
			return true;
		}
		
		return false;
	}

	//from OpenJDK6's Pattern.quote (not available for GWT)
	public String quote(String s) {
		int slashEIndex = s.indexOf("\\E");
		if (slashEIndex == -1)
			return "\\Q" + s + "\\E";

		StringBuilder sb = new StringBuilder(s.length() * 2);
		sb.append("\\Q");
		slashEIndex = 0;
		int current = 0;
		while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
			sb.append(s.substring(current, slashEIndex));
			current = slashEIndex + 2;
			sb.append("\\E\\\\E\\Q");
		}
		sb.append(s.substring(current, s.length()));
		sb.append("\\E");
		return sb.toString();
	}

	public String checkBrackets(String text, String descriptionType) {
		StringBuilder result = new StringBuilder();
		if(hasUnmatchedCharacter(text, "\\(", "\\)")) {
			result.append(descriptionType + " contains unclosed left brackets (\n");
		}
		if(hasUnmatchedCharacter(text, "\\)", "\\(")) {
			result.append(descriptionType + " contains unclosed right brackets )\n");
		}
		if(hasUnmatchedCharacter(text, "\\[", "\\]")) {
			result.append(descriptionType + " contains unclosed left brackets [\n");
		}
		if(hasUnmatchedCharacter(text, "\\]", "\\[")) {
			result.append(descriptionType + " contains unclosed right brackets ]\n");
		}
		if(hasUnmatchedCharacter(text, "\\{", "\\}")) {
			result.append(descriptionType + " contains unclosed left brackets {\n");
		}
		if(hasUnmatchedCharacter(text, "\\}", "\\{")) {
			result.append(descriptionType + " contains unclosed right brackets }\n");
		}
		return result.toString();
	}

}

