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
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.Sentence;

/**
 * Extract the character 6.1 Fermentation Products
 * Sample sentences:
 * 	1. Metabolism is fermentative, with glucose fermented to succinic and acetic acids, or respiratory, with glucose metabolized to CO2 and water by using oxygen as the terminal electron acceptor.  
 *	2. Major products of glucose fermentation are propionic, lactic and succinic acids.  
 *
 *	Method:
 *	1.	USP
 */
public class FermentationProductsExtractor extends AbstractCharacterValueExtractor {

	private String uspResultsDirectory;
	private String uspString;

	public FermentationProductsExtractor(ILabel label) {
		super(label, "Fermentation Products");
	}
	
	@Inject
	public FermentationProductsExtractor(@Named("FermentationProductsExtractor_Label")Label label, 
			@Named("FermentationProductsExtractor_Character")String character, 
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

		MicropieUSPExtractor micropieUSPExtractor = new MicropieUSPExtractor(uspResultsDirectory, uspString);
		try {
			output = micropieUSPExtractor.getObjectValue(text, "produces", "V", "dobj", "Dep");
			output.addAll(micropieUSPExtractor.getObjectValue(text, "produced", "V", "nsubjpass", "Dep"));
			output.addAll(micropieUSPExtractor.getObjectValue(text, "formed", "V", "nsubjpass", "Dep"));
			//System.out.println("Fermentation Products::" + output.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		charValueList = CharacterValueFactory.createList(this.getLabel(), output);
		return charValueList;
	}
}
