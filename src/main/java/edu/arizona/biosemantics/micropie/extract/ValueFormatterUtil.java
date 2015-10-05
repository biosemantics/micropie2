package edu.arizona.biosemantics.micropie.extract;

import java.util.List;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.NumericCharacterValue;

/**
 * value formatter tool
 * @author maojin
 *
 */
public class ValueFormatterUtil {
	private static StringValueFormatter svFormatter = new StringValueFormatter();
	private static NumericValueFormatter nvFormatter = new NumericValueFormatter();
	
	public static String format(List values){
		if(values==null||values.size()==0) return "";
		Object valueSample = values.get(0);
		if(valueSample instanceof NumericCharacterValue){
			return clean(nvFormatter.format(values));
		}else if(valueSample instanceof CharacterValue){
			return clean(svFormatter.format(values));
		}
		return "";

	}
	
	/**
	 * parse the value
	 * @param label
	 * @param valueStr
	 * @return
	 */
	public static List parse(ILabel label, String valueStr){
		return svFormatter.parse(label, valueStr);
	}
	
	
	/**
	 * simple clean
	 * @param value
	 * @return
	 */
	public static String clean(String value){
		value = value.trim().replace("[\\s]+", " ")
				.replace("–", "-")//
				.replace("[\\s-\\s]", "-")//
				.replace("·", ".")//decimal number
				;
		return value;
	}
}
