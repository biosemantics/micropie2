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
public class StringValueFormatter implements IValueFormatter {
	private String separator;
	
	public StringValueFormatter(String separator){
		this.separator = separator;
	}
	
	public StringValueFormatter(){
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
			if(cv.getNegation()!=null) valueStr.append(cv.getNegation()).append(" ");
			valueStr.append(cv.getValue());
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
			if(value!=null&&!"".equals(value)){
				CharacterValue cv = CharacterValueFactory.create(label, value);
				if(!valueList.contains(cv)) valueList.add(cv);
			}
		}
		
		return valueList;
	}


}
