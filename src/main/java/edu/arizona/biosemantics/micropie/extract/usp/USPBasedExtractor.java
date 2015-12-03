package edu.arizona.biosemantics.micropie.extract.usp;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import usp.eval.MicropieUSPExtractor;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.io.USPClusteringReader;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.common.log.LogLevel;



/**
 * Extractors for many keyword based character value
 *
 */
public class USPBasedExtractor extends AbstractCharacterValueExtractor {
	
	private Set<USPRequest> uspRequests;
	private MicropieUSPExtractor micropieUSPExtractor;
	private String uspResultsDirectory;
	private String uspString;
	// public USPBasedExtractor(ILabel label) {
	//	super(label, "Antibiotic Sensitivity");
	// }
	
	@Inject
	public USPBasedExtractor(@Named("USPBasedExtractor_Label")Label label, 
			@Named("USPBasedExtractor_Character")String character,
			@Named("USPBasedExtractor_")Set<USPRequest> uspRequests,
			@Named("uspResultsDirectory")String uspResultsDirectory, 
			@Named("uspString") String uspString,
			@Named("uspBaseString") String uspBaseString) {
		super(label, character);
		this.uspRequests = uspRequests;
		this.uspResultsDirectory = uspResultsDirectory;
		
		//Do not create here
		this.micropieUSPExtractor = new MicropieUSPExtractor(uspResultsDirectory, uspBaseString);
		this.uspString = uspString;
	}
	
	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {

		Set<String> output = new HashSet();
		List<CharacterValue> charValueList = null;
		
		String text = sentence.getText();
		Set<String> returnCharacterStrings = new HashSet<String>();
		for (USPRequest uspRequest : uspRequests) {
			try {
				String originalString = uspRequest.getKeyword();
				USPClusteringReader uspClusteringReader = new USPClusteringReader();
				uspClusteringReader.setInputStream(new FileInputStream(uspResultsDirectory + File.separator +uspString+ ".clustering"));
				Set<String> uspClusteringKeywords = uspClusteringReader.getRelatedKeywords(originalString);
				// System.out.println("uspClusteringKeywords::" + uspClusteringKeywords.toString());
				
				Set<String> tmpMicropieUSPExtractorResult = micropieUSPExtractor.getObjectValue(text, 
						uspRequest.getKeyword(), uspRequest.getKeywordType(), uspRequest.getKeywordObject(), uspRequest.getExtractionType());
				returnCharacterStrings.addAll(tmpMicropieUSPExtractorResult);
				
				if (tmpMicropieUSPExtractorResult.size() > 1) {
					System.out.println("\n");
					System.out.println("Text:" + text);
					System.out.println("kwd::" + uspRequest.getKeyword());
					System.out.println("type::" + uspRequest.getKeywordType());
					System.out.println("keywordObject::" + uspRequest.getKeywordObject());
					System.out.println("uspRequest.getKeyword():" + uspRequest.getKeyword());
					System.out.println("tmpMicropieUSPExtractorResult::" + tmpMicropieUSPExtractorResult);
					System.out.println("\n");
				}
				
				for (String uspClusteringKeyword : uspClusteringKeywords) {
					System.out.println("uspClusteringKeyword="+uspClusteringKeyword);
					tmpMicropieUSPExtractorResult = micropieUSPExtractor.getObjectValue(text, 
							uspClusteringKeyword, uspRequest.getKeywordType(), uspRequest.getKeywordObject(), uspRequest.getExtractionType());
					returnCharacterStrings.addAll(tmpMicropieUSPExtractorResult);
					
					if (tmpMicropieUSPExtractorResult.size() > 1) {
						System.out.println("\n");
						System.out.println("Text:" + text);
						System.out.println("kwd::" + uspClusteringKeyword);
						System.out.println("type::" + uspRequest.getKeywordType());
						System.out.println("keywordObject::" + uspRequest.getKeywordObject());
						System.out.println("uspRequest.getKeyword():" + uspRequest.getKeyword());
						System.out.println("tmpMicropieUSPExtractorResult::" + tmpMicropieUSPExtractorResult);
						System.out.println("\n");
					}
				}
				
				
			} catch(Exception e) {
				e.printStackTrace();
				log(LogLevel.ERROR, "Could not get object value from USP extractor for sentence: \"" + text + "\" with " + uspRequest);
			}
		}
		
		charValueList = CharacterValueFactory.createList(this.getLabel(), returnCharacterStrings);
		return charValueList;
	}
}
