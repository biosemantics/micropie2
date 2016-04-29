package edu.arizona.biosemantics.micropie.extract.usp;

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
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.Sentence;


/**
 * Extract the character 4.1 Antibiotic sensitivity
 * Sample sentences:
 *   1. Sensitive to BACITRACIN and NOVOBIOCIN, but resistant to AMPICILLIN, CHLORAMPHENICOL, ERYTHROMYCIN, GENTAMICIN, NALIDIXIC ACID, 
 * 	 2. Susceptible to the following antimicrobial compounds: amoxicillin (25 µg), bacitracin (10 U), cephalothin (30 µg) and nitrofurantoin 
 *
 */
public class AntibioticSensitivityExtractor extends AbstractCharacterValueExtractor {

	private String uspResultsDirectory;
	private String uspString;

	public AntibioticSensitivityExtractor(ILabel label) {
		super(label, "Antibiotic Sensitivity");
	}
	
	@Inject
	public AntibioticSensitivityExtractor(@Named("AntibioticSensitivityExtractor_Label")Label label, 
			@Named("AntibioticSensitivityExtractor_Character")String character, 
			@Named("uspResultsDirectory")String uspResultsDirectory, @Named("uspString") String uspString) {
		super(label, character);
		this.uspResultsDirectory = uspResultsDirectory;
		this.uspString = uspString;
	}
	
	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {

		Set<String> output = new HashSet();
		List<CharacterValue> charValueList = null;
		
		String text = sentence.getText();
		// input: the original sentence
		// output: String array?
		
//		// Example:  ??
//		MicropieUSPExtractor micropieUSPExtractor = new MicropieUSPExtractor(uspResultsDirectory, uspString);
//		try {
//			output = micropieUSPExtractor.getObjectValue(text, "sensitive", "J", "prep_to", "Dep");
//			//System.out.println("Antibiotic Sensitivity::" + output.toString());
//			charValueList = CharacterValueFactory.createList(this.getLabel(), output);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		return charValueList;
	}
}
