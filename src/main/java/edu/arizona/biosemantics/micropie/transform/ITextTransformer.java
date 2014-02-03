package edu.arizona.biosemantics.micropie.transform;

/**
 * Transforms text
 * @author rodenhausen
 */
public interface ITextTransformer {

	/**
	 * @param text
	 * @return transformed text
	 */
	public String transform(String text);
	
}
