package edu.arizona.biosemantics.micropie.nlptool;

import java.util.List;

/**
 * Transforms text
 * @author rodenhausen
 */
public interface ITextNormalizer {

	/**
	 * @param text
	 * @return transformed text
	 */
	public String transform(String text); // text based
	public String transformBack(String sent); // sentence based
	
	/**
	 * For a list
	 * replace celsius degree  °C => celsius_degree
	 * @param sentences
	 * @return
	 */
	public List<String> transformCelsiusDegree(List<String> sentences);
	
	/**
	 * For a sentence
	 * replace celsius degree  °C => celsius_degree
	 * @param sentences
	 * @return
	 */
	public String transformCelsiusDegree(String sentences);
	
	
	/**
	 * For a List
	 * replace \"–\" to \"-\" ..."
	 * @param sentence
	 * @return
	 */
	public List<String> transformDash(List<String> sentences);
	
	/**
	 * For a sentence
	 * replace \"–\" to \"-\" ..."
	 * @param sentence
	 * @return
	 */
	public String transformDash(String sentence);
	
	/**
	 * To avoid the error ClausIE spliter: the dash will disappear
	 * for sentence List
	 * replace \"·\" to \".\" ..."
	 * @param sentences
	 * @return
	 */
	public List<String> transformPeriod(List<String> sentences);
	
	
	/**
	 * To avoid the error ClausIE spliter: the dash will disappear
	 * for sentence
	 * replace \"·\" to \".\" ..."
	 * @param sentences
	 * @return
	 */
	public String transformPeriod(String sentence);
	
	
	/**
	 * 
	 * @param sentence
	 * @return
	 */
	public String transformEntity(String sentence);
	
	
	/**
	 * replace various types of special characters
	 * @param text
	 * @return
	 */
	public String transformSpchar(String text);
}
