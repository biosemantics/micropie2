package edu.arizona.biosemantics.micropie.extract.regex;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;

import usp.eval.MicropieUSPExtractor;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.io.USPClusteringReader;
import edu.arizona.biosemantics.common.log.LogLevel;

public class USPBasedExtractor extends AbstractCharacterValueExtractor {
	
	private Set<USPRequest> uspRequests;
	private MicropieUSPExtractor micropieUSPExtractor;
	private String uspResultsDirectory;

	// public USPBasedExtractor(ILabel label) {
	//	super(label, "Antibiotic Sensitivity");
	// }
	
	@Inject
	public USPBasedExtractor(@Named("USPBasedExtractor_Label")Label label, 
			@Named("USPBasedExtractor_Character")String character,
			@Named("USPBasedExtractor_")Set<USPRequest> uspRequests,
			@Named("uspResultsDirectory")String uspResultsDirectory, 
			@Named("uspString") String uspString) {
		super(label, character);
		this.uspRequests = uspRequests;
		this.uspResultsDirectory = uspResultsDirectory;
		this.micropieUSPExtractor = new MicropieUSPExtractor(uspResultsDirectory, uspString);
	}
	
	@Override
	public Set<String> getCharacterValue(String text) {
		Set<String> returnCharacterStrings = new HashSet<String>();
		for (USPRequest uspRequest : uspRequests) {
			try {
				String originalString = uspRequest.getKeyword();
				USPClusteringReader uspClusteringReader = new USPClusteringReader();
				uspClusteringReader.setInputStream(new FileInputStream(uspResultsDirectory + File.separator + "usp.clustering"));
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
				log(LogLevel.ERROR, "Could not get object value from USP extractor for sentence: \"" + text + "\" with " + uspRequest);
			}
		}
		return returnCharacterStrings;
	}
}
