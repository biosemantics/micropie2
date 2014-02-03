package edu.arizona.biosemantics.micropie.transform;

/**
 * Tokenizes given a seperator sting
 * @author rodenhausen
 */
public class SeperatorTokenizer implements ITokenizer {

	private String seperator;

	/**
	 * @param seperator
	 */
	public SeperatorTokenizer(String seperator) {
		this.seperator = seperator;
	}
	
	@Override
	public String[] tokenize(String text) {
		return text.split(seperator);
	}

}
