package edu.arizona.biosemantics.micropie.transform.regex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GcExtractor implements IContentExtractor {

	private String character = "%G+C";
	
	@Override
	public Set<String> getContent(String text) {
		// TODO Auto-generated constructor stub
		Set<String> output = new HashSet<String>(); // Output,
																	// format::List<String>

		// input: the original sentnece
		// output: String array?

		// log(LogLevel.INFO, "Original Sent : " + sent);
		text = text.substring(0, text.length() - 1); // remove the period at the
														// last position

		// String[] sentArray = sent.split(" ");
		// log(LogLevel.INFO, "sentArray.length :" + sentArray.length );

		// \s\d*\.\d*\s
		String patternStringGc = "Guanosine plus cytosine|guanine-plus-cytosine|\\sG\\s\\+\\sC\\s|\\(G+C\\s|G\\s*\\+\\s*C|\\s+G\\s*\\+\\s*C\\s+|\\s+g\\s*\\+\\s*c\\s+|\\s+GC\\s+|\\s+gc\\s+|%GC|%G+C";

		// String patternStringGc = "\\s+G\\+C\\s+";
		// String patternStringGc = "G\\s*\\+\\s*C";

		Pattern patternGc = Pattern.compile(patternStringGc);
		Matcher matcherGc = patternGc.matcher(text);

		List<String> gcStringList = new ArrayList<String>();
		List<Integer> gcPositionList = new ArrayList<Integer>();
		while (matcherGc.find()) {
			String matchWord = matcherGc.group();
			// log(LogLevel.INFO, "matchWord :: " + matchWord);

			text = text.replace(matchWord, " G+C ");
			// log(LogLevel.INFO, "sent :: " + sent);

			String[] sentArray = text.split(" ");
			for (int i = 0; i < sentArray.length; i++) {
				if (sentArray[i].equals("G+C")) {
					// if (sentArray[i].equals(matchWord.trim())) {
					boolean isIncluded = false;
					for (Integer itemInGcPositionList : gcPositionList) {
						if (itemInGcPositionList == i) {
							isIncluded = true;
						}
					}
					if (isIncluded == false) {
						// log(LogLevel.INFO, "Pos :" + i );
						gcPositionList.add(i);
						gcStringList.add(matchWord.trim());
					}
				}
			}
		}

		String[] sentArray = text.split(" ");
		for (int i = 0; i < gcPositionList.size(); i++) {
			int itemInGcPositionList = gcPositionList.get(i);
			// log(LogLevel.INFO, (matcherGc.group() + "\n");
			String subSent = "";
			int subSentStartFlag = itemInGcPositionList - 15;
			if (subSentStartFlag <= 0) {
				subSentStartFlag = 0;
			}
			int subSentEndFlag = itemInGcPositionList + 15;
			if (subSentEndFlag >= sentArray.length) {
				subSentEndFlag = sentArray.length;
			}

			for (int j = subSentStartFlag; j < subSentEndFlag; j++) {
				subSent += " " + sentArray[j];
			}

			// log(LogLevel.INFO, "subSent : " + subSent);

			String patternStringMolPercent = "\\s*MOL\\s*%\\s*|\\s*mol\\s*%\\s*|\\s?Mol\\s?%\\s?";
			Pattern patternMolPercent = Pattern
					.compile(patternStringMolPercent);
			Matcher matcherMolPercent = patternMolPercent.matcher(subSent);

			// \s\d*\.\d*\s
			String patternString = "(" + "\\s\\d+$|" + "\\s\\d+\\s|"
					+ "\\s\\d+\\.\\d*$|" + "\\s\\d+\\.\\d+\\s|" +

					"\\s\\d+\\.\\d+\\+\\/\\-\\d+\\.\\d+\\s|"
					+ "\\s\\d+\\+\\/\\-\\d+\\.\\d+\\s|"
					+ "\\s\\d+\\.\\d+\\+\\/\\-\\d+\\s|"
					+ "\\s\\d+\\+\\/\\-\\d+\\s|" +

					"\\s\\d+\\.\\d+\\-\\d+\\.\\d+\\s|"
					+ "\\s\\d+\\-\\d+\\.\\d+\\s|" + "\\s\\d+\\.\\d+\\-\\d+\\s|"
					+ "\\s\\d+\\-\\d+\\s|" +

					"\\s\\d+\\.\\d+\\–\\d+\\.\\d+\\s|"
					+ "\\s\\d+\\–\\d+\\.\\d+\\s|" + "\\s\\d+\\.\\d+\\–\\d+\\s|"
					+ "\\s\\d+\\–\\d+\\s" +

					")";
			// "\\s\\d*\\.*\\-\\s*\\d*\\.*\\d*\\.|" +
			// "\\s\\d*\\.\\d*\\-\\s*\\d*\\.\\d*\\s|" +
			// "\\s\\d*\\.*\\d*\\s\\+\\/\\-\\s\\d*\\s|" +
			// "\\s\\d*\\s|" +
			// "\\s\\d*\\.\\d*\\s|" +
			// "\\s\\d*\\-\\s*\\d*\\s|" +
			// "\\s\\d*\\.*\\d*\\.)";

			// patternString =
			// "(.*)(\\s\\d*\\s\\+\\/\\-\\s\\d*\\s|\\s\\d*\\s|\\s\\d*\\.\\d*\\s|\\s\\d*\\-\\s*\\d*\\s)(.*)";

			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(subSent);

			while (matcherMolPercent.find() && matcher.find()) {
				// log(LogLevel.INFO, (gcStringList.get(i) + "\n");
				// log(LogLevel.INFO, (matcherMolPercent.group() + "\n");
				// log(LogLevel.INFO, (matcher.group() + "\n");

				boolean isIncluded = false;
				for (String itemInOutputContentList : output) {
					if (itemInOutputContentList.equals(matcher.group().trim())) {
						isIncluded = true;
						// log(LogLevel.INFO, ("Has this :: " +
						// itemInGcContentList);
					}
				}
				if (isIncluded == false) {
					output.add(matcher.group().trim());
				}
			}
		}

		return output;
	}

	@Override
	public String getCharacter() {
		return character;
	}

}
