package edu.arizona.biosemantics.micropie.extract;

import java.util.ArrayList;
import java.util.List;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.eval.IValueComparator;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;

/**
 * Format String Value
 * @author maojin
 *
 */
public class NumericValueFormatter implements IValueFormatter {
	private String separator;
	
	public NumericValueFormatter(String separator){
		this.separator = separator;
	}
	
	public NumericValueFormatter(){
		this.separator = "#";
	}
	
	@Override
	/**
	 * separate multiple values with #
	 * 
	 */
	public String format(List<CharacterValue> values) {
		if(values==null) return "";
		StringBuffer valueStr = new StringBuffer();
		int size = values.size();
		for(int i=0; i<size;i++){
			CharacterValue cv = values.get(i);
			valueStr.append(cv.getValue());
			valueStr.append("|");
			if(i!=size-1) valueStr.append(separator);
		}
		return valueStr.toString();
	}

	@Override
	/**
	 * parse the string into multiple CharacterValues
	 */
	public List<CharacterValue> parse(ILabel label, String valueStr) {
		String[] values = valueStr.split(separator);
		
		List<CharacterValue> valueList = new ArrayList();
		for(String value:values){
			CharacterValue cv = CharacterValueFactory.create(null, value);
			
			valueList.add(cv);
		}
		
		return valueList;
	}

}
