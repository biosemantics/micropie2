package edu.arizona.biosemantics.micropie.extract;

import java.util.List;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.NumericLabels;
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
	private static NumericLabels numericLabels = new NumericLabels();
	
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
	
	//only return the main value
	public static String simpleFormat(List<CharacterValue> values){
		if(values==null) return "";
		StringBuffer valueStr = new StringBuffer();
		int size = values.size();
		for(int i=0; i<size;i++){
			CharacterValue cv = values.get(i);
			valueStr.append(cv.getValue());
			if(i!=size-1) valueStr.append("#");
		}
		return valueStr.toString();

	}
	
	
	public static String format(CharacterValue value){
		if(value instanceof NumericCharacterValue){
			return clean(nvFormatter.format(value));
		}else if(value instanceof CharacterValue){
			return clean(svFormatter.format(value));
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
		if(numericLabels.contains(label)){
			return nvFormatter.parse(label, valueStr);
		}else{
			return svFormatter.parse(label, valueStr);
		}
		
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
