package edu.arizona.biosemantics.micropie.transform;

/**
 * Transforms text
 * @author rodenhausen
 */
public interface ITextNormalizer {

	/**
	 * @param text
	 * @return transformed text
	 */
	public String transform(String text);
	
}
