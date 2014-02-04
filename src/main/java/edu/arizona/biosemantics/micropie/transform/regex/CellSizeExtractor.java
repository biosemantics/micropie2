package edu.arizona.biosemantics.micropie.transform.regex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CellSizeExtractor implements IContentExtractor {

	private String character = "Cell size";
	
	@Override
	public Set<String> getContent(String text) {
		Set<String> output = new HashSet<String>(); // Output,
																	// format::List<String>

		// input: the original sentnece
		// output: String array?

		// log(LogLevel.INFO, "Original Sent : " + sent);
		text = text.substring(0, text.length() - 1); // remove the period at the
														// last position

		// String[] sentArray = sent.split(" ");
		// log(LogLevel.INFO, "sentArray.length :" + sentArray.length );

		//
		// Fail // String patternStringCellSize = "(.*) (µm in diameter) (.*)";
		String patternStringCellSize = "(.*)(\\s?µm\\s?in\\s?diameter\\s?)(.*)";
		// String patternStringCellSize = "\\s?µm\\s?in\\s?diameter\\s?";

		Pattern patternCellSize = Pattern.compile(patternStringCellSize);
		Matcher matcherCellSize = patternCellSize.matcher(text);

		while (matcherCellSize.find()) {
			// log(LogLevel.INFO, "Whloe Sent::" + matcherCellSize.group());
			// log(LogLevel.INFO, "Part 1::" + matcherCellSize.group(1));
			// log(LogLevel.INFO, "Part 2::" + matcherCellSize.group(2));
			// log(LogLevel.INFO, "Part 3::" + matcherCellSize.group(3));
			String part1 = matcherCellSize.group(1);
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
			Matcher matcher = pattern.matcher(part1);

			while (matcher.find()) {
				// log(LogLevel.INFO, "Size is ::" + matcher.group());
				output.add(matcher.group().trim()
						+ " µm in diameter");
			}
		}

		patternStringCellSize = "(.*)(\\s?diameter\\s?of|\\s?diameter\\s?)(.*)(\\s?µm)";
		// String patternStringCellSize = "\\s?µm\\s?in\\s?diameter\\s?";

		patternCellSize = Pattern.compile(patternStringCellSize);
		matcherCellSize = patternCellSize.matcher(text);

		while (matcherCellSize.find()) {
			// log(LogLevel.INFO, "Whloe Sent::" + matcherCellSize.group());
			// log(LogLevel.INFO, "Part 1::" + matcherCellSize.group(1));
			// log(LogLevel.INFO, "Part 2::" + matcherCellSize.group(2));
			// log(LogLevel.INFO, "Part 3::" + matcherCellSize.group(3));
			String part3 = matcherCellSize.group(3);
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
			Matcher matcher = pattern.matcher(part3);

			while (matcher.find()) {
				// log(LogLevel.INFO, "Size is ::" + matcher.group());
				output.add("diameter of " + matcher.group().trim()
						+ " µm");
			}

		}

		// ranging in diameter
		patternStringCellSize = "(.*)(\\s?ranging\\s?in\\s?diameter\\s?)(.*)";
		// String patternStringCellSize = "\\s?µm\\s?in\\s?diameter\\s?";

		patternCellSize = Pattern.compile(patternStringCellSize);
		matcherCellSize = patternCellSize.matcher(text);

		while (matcherCellSize.find()) {
			// log(LogLevel.INFO, "Whloe Sent::" + matcherCellSize.group());
			// log(LogLevel.INFO, "Part 1::" + matcherCellSize.group(1));
			// log(LogLevel.INFO, "Part 2::" + matcherCellSize.group(2));
			// log(LogLevel.INFO, "Part 3::" + matcherCellSize.group(3));
			String part2 = matcherCellSize.group(2);
			String part3 = matcherCellSize.group(3);

			output.add(part2 + " " + part3);

		}
		return output;
	}

	@Override
	public String getCharacter() {
		return character;
	}

}