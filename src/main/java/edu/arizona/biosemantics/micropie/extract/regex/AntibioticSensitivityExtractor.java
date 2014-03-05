package edu.arizona.biosemantics.micropie.extract.regex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import usp.eval.MicropieUSPExtractor;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;

public class AntibioticSensitivityExtractor extends AbstractCharacterValueExtractor {

	public AntibioticSensitivityExtractor(ILabel label) {
		super(label, "Antibiotic Sensitivity");
	}
	
	@Inject
	public AntibioticSensitivityExtractor(@Named("AntibioticSensitivityExtractor_Label")Label label, 
			@Named("AntibioticSensitivityExtractor_Character")String character) {
		super(label, character);
	}
	
	@Override
	public Set<String> getCharacterValue(String text) {

		Set<String> output = new HashSet<String>(); // Output, format::List<String>
		
		// input: the original sentnece
		// output: String array?
		
		// Example:  ??
		MicropieUSPExtractor micropieUSPExtractor = new MicropieUSPExtractor();
		try {
			output = micropieUSPExtractor.getObjectValue(text, "sensitive", "J", "prep_to");
			System.out.println("Antibiotic Sensitivity::" + output.toString());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return output;
	}
}
