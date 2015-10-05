package edu.arizona.biosemantics.micropie.nlptool;

/**
 * Tokenizes a text
 * @author rodenhausen
 */
public interface ITokenizer {

	/**
	 * @param text
	 * @return tokens
	 */
	public String[] tokenize(String text);
	
}
