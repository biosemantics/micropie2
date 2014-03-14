package edu.arizona.biosemantics.micropie.extract.regex;

import java.util.HashSet;
import java.util.Set;

import usp.eval.MicropieUSPExtractor;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.log.LogLevel;

public class USPBasedExtractor extends AbstractCharacterValueExtractor {
	
	private Set<USPRequest> uspRequests;
	private MicropieUSPExtractor micropieUSPExtractor = new MicropieUSPExtractor();

	// public USPBasedExtractor(ILabel label) {
	//	super(label, "Antibiotic Sensitivity");
	// }
	
	@Inject
	public USPBasedExtractor(@Named("USPBasedExtractor_Label")Label label, 
			@Named("USPBasedExtractor_Character")String character,
			@Named("USPBasedExtractor_")Set<USPRequest> uspRequests) {
		super(label, character);
		this.uspRequests = uspRequests;
	}
	
	@Override
	public Set<String> getCharacterValue(String text) {
		Set<String> returnCharacterStrings = new HashSet<String>();
		for (USPRequest uspRequest : uspRequests) {
			try {
				Set<String> tmpMicropieUSPExtractorResult = micropieUSPExtractor.getObjectValue(text, 
						uspRequest.getKeyword(), uspRequest.getKeywordType(), uspRequest.getKeywordObject(), uspRequest.getExtractionType());
				returnCharacterStrings.addAll(tmpMicropieUSPExtractorResult);
				
				// System.out.println("Text:" + text + "::kwd::" + uspRequest.getKeyword() + "::type::" + uspRequest.getKeywordType());
				// System.out.println("uspRequest.getKeyword():" + uspRequest.getKeyword() + "::" + tmpMicropieUSPExtractorResult);
				
			} catch(Exception e) {
				log(LogLevel.ERROR, "Could not get object value from USP extractor for sentence: \"" + text + "\" with " + uspRequest);
			}
		}
		return returnCharacterStrings;
	}
}
