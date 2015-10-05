package edu.arizona.biosemantics.micropie.model;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;

/**
 * create characters
 * @author maojin
 */
public class CharacterValueFactory {
	
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
	public static List<CharacterValue> createList(ILabel character, Set<String> charValues){
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
		return new CharacterValue(character, value);
	}
}
