package edu.arizona.biosemantics.micropie.transform;

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
