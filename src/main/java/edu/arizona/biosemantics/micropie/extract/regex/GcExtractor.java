package edu.arizona.biosemantics.micropie.extract.regex;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.model.Sentence;

public class GcExtractor extends AbstractCharacterValueExtractor {

	private String patternStringGc = "(" + 
										"\\s?Guanosine plus cytosine|guanine-plus-cytosine\\s?|" +
										"\\s?G\\s?\\+\\s?C\\s?|" + 
										"\\s?\\(G\\s?\\+\\s?C\\s?|" + 
										// "\\s?\\(G+C\\s?|" + 
										// "\\s?G\\s*\\+\\s*C|" + 
										// "\\s+G\\s*\\+\\s*C\\s+|" + 
										// "\\s+g\\s*\\+\\s*c\\s+|" + 
										// "\\s+GC\\s+|" + 
										// "\\s+gc\\s+|" + 
										// "%GC|" + 
										// "%G+C" +
										"\\s?GC\\s?" +
										")";

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
	
	public String getPatternStringGc() {
		return patternStringGc;
	}
	
	// private String patternStringGc = "\\s+G\\+C\\s+";
	// private String patternStringGc = "G\\s*\\+\\s*C";
	
	public GcExtractor(ILabel label) {
		super(label, "%G+C");
	}
	
	@Inject
	public GcExtractor(@Named("GcExtractor_Label")ILabel label, 
			@Named("GcExtractor_Label")String character) {
		super(label, character);
	}

	@Override
	public Set<String> getCharacterValue(String text) {
		// TODO Auto-generated constructor stub
		
		
		Set<String> output = new HashSet<String>(); // Output,
		// format::List<String>

		// input: the original sentnece
		// output: String array?

		// log(LogLevel.INFO, "Original Sent : " + sent);
		text = text.substring(0, text.length() - 1); // remove the period at the
														// last position
		
		text = text.toLowerCase();
		patternStringGc = patternStringGc.toLowerCase();
		// String[] sentArray = sent.split(" ");
		// log(LogLevel.INFO, "sentArray.length :" + sentArray.length );

		// \s\d*\.\d*\s

		// Case 1:: The G+C contenc of DNA is 22.3 mol% (mol %).
		Pattern patternGc = Pattern.compile(patternStringGc + "(.*)(\\s)" + targetPatternString + "(\\s?\\s*mol\\s*\\%\\s*)");
		Matcher matcherGc = patternGc.matcher(text);

		while (matcherGc.find()) {
			// System.out.println("Whloe Sent::" + matcherGc.group());
			// System.out.println("Part 1::" + matcherGc.group(1));
			// System.out.println("Part 2::" + matcherGc.group(2));
			// System.out.println("Part 3::" + matcherGc.group(3));
			// System.out.println("Part 4::" + matcherGc.group(4));
			// System.out.println("Part 5::" + matcherGc.group(5));
			String targetPattern = matcherGc.group(4);
			output.add(targetPattern);
		}
		
		
		// Case 2: 
		Pattern patternGc2 = Pattern.compile("(.*)(\\s?\\s*mol\\s*\\%\\s*)(.*)" + patternStringGc + "(\\s?.*\\s?)" + targetPatternString + "(.*)");
		Matcher matcherGc2 = patternGc2.matcher(text);

		while (matcherGc2.find()) {
			// System.out.println("Case 2::");
			// System.out.println("Whloe Sent::" + matcherGc2.group());
			// System.out.println("Part 1::" + matcherGc2.group(1));
			// System.out.println("Part 2::" + matcherGc2.group(2));
			// System.out.println("Part 3::" + matcherGc2.group(3));
			// System.out.println("Part 4::" + matcherGc2.group(4));
			// System.out.println("Part 5::" + matcherGc2.group(5));
			// System.out.println("Part 6::" + matcherGc2.group(6));
			// System.out.println("Part 7::" + matcherGc2.group(7));
			String targetPattern = matcherGc2.group(6);
			output.add(targetPattern);
		}

		// Case 3::
		// Cannot handle this kind of example: 30.6 mol%
		// 30.6 mol% GC
		

		return output;
	}
	
	// Example: 
	public static void main(String[] args) throws IOException {		
		System.out.println("Start::");
		
		GcExtractor gcExtractor = new GcExtractor(Label.c1);	
		String patternStringGc = gcExtractor.getPatternStringGc();
		
		CSVSentenceReader sourceSentenceReader = new CSVSentenceReader();
		// Read sentence list
		// 
		sourceSentenceReader.setInputStream(new FileInputStream("split-additionalUSPInputs.csv"));
		List<Sentence> sourceSentenceList = sourceSentenceReader.readSentenceList();
		System.out.println("sourceSentenceList.size()::" + sourceSentenceList.size());
		
		int sampleSentCounter = 0;
		int extractedValueCounter = 0;
		
		for (Sentence sourceSentence : sourceSentenceList) {
			String sourceSentText = sourceSentence.getText();
			sourceSentText = sourceSentText.toLowerCase();
			patternStringGc = patternStringGc.toLowerCase();
			
			if ( sourceSentText.matches("(.*)" + patternStringGc + "(.*)")) {
				System.out.println("\n");
				System.out.println("sourceSentText::" + sourceSentText);
				Set<String> gcResult = gcExtractor.getCharacterValue(sourceSentText);
				System.out.println("gcResult::" + gcResult.toString());
				if ( gcResult.size() > 0 ) {
					extractedValueCounter +=1;
				}
				sampleSentCounter +=1;
			}
		
		}

		System.out.println("\n");
		System.out.println("sampleSentCounter::" + sampleSentCounter);
		System.out.println("extractedValueCounter::" + extractedValueCounter);
		
		
	}	
	
}
