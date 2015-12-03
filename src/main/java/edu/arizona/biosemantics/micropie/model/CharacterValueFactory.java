package edu.arizona.biosemantics.micropie.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.NumericLabels;

/**
 * create characters
 * @author maojin
 */
public class CharacterValueFactory {
	
	private static NumericLabels numericLabels = new NumericLabels();
	
	
	/**
	 * create a single character value object
	 * @param character
	 * @return
	 */
	public static CharacterValue create(ILabel character){
		return new CharacterValue(character);
	}
	
	/**
	 * create a single character value object
	 * @param character
	 * @return
	 */
	public static CharacterValue createStringValue(ILabel character){
		return new CharacterValue(character);
	}
	
	
	/**
	 * create a single character value object
	 * @param character
	 * @return
	 */
	public static NumericCharacterValue createNumericValue(ILabel character,String value, String unit){
		return new NumericCharacterValue(character,value,unit);
	}
	
	/**
	 * create many character values
	 * @param character
	 * @param charValues
	 * @return
	 */
	public static Set<CharacterValue> createSet(ILabel character, Set<String> charValues){
		Set<CharacterValue> charSet = new HashSet<CharacterValue>();
		for(String charValue : charValues){
			charSet.add(new CharacterValue(character,charValue));
		}
		return charSet;
	}
	
	/**
	 * create many character values
	 * @param character
	 * @param charValues
	 * @return
	 */
	public static List<CharacterValue> createList(ILabel character, Collection<String> charValues){
		List<CharacterValue> charList = new LinkedList<CharacterValue>();
		for(String charValue : charValues){
			charList.add(new CharacterValue(character,charValue));
		}
		return charList;
	}

	/**
	 * create given character value
	 * @param label
	 * @param value
	 * @return
	 */
	public static CharacterValue create(ILabel character, String value) {
		if(numericLabels.contains(character)){
			return new NumericCharacterValue(character, value);
		}else{
			return new CharacterValue(character, value);
		}
		
	}
}
