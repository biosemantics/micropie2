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
			valueStr.append(format(cv));
			if(i!=size-1) valueStr.append(separator);
		}
		return valueStr.toString();
	}
	
	
	/**
	 * format as:  negation|modifier|mainValue
	 * 
	 * @param value
	 * @return
	 */
	public String format(CharacterValue value) {
		StringBuffer valueStr = new StringBuffer();
		if(value.getNegation()!=null){
			valueStr.append(value.getNegation()).append("|");
		}
		if(value.getNegation()!=null&&(value.getValueModifier()==null||"".equals(value.getValueModifier()))){
			valueStr.append("|");
		}
		if(value.getValueModifier()!=null&&!"".equals(value.getValueModifier())) {
			valueStr.append(value.getValueModifier());
			valueStr.append("|");
		}
		
		valueStr.append(value.getValue());
		return valueStr.toString();
	}
	

	@Override
	/**
	 * parse the string into multiple CharacterValues
	 * 
	 * negation|modifier|mainValue
	 * if only one field is available, it is the mainValue
	 * if three fields are there, it should be sperated.
	 */
	public List<CharacterValue> parse(ILabel label, String valueStr) {
		//separate multiple values
		String[] values = valueStr.split(separator);
		
		List<CharacterValue> valueList = new ArrayList();
		for(String value:values){//this is a value
			
			if(value!=null&&!"".equals(value)){
				
				String[] fields = value.trim().split("\\|");
				if(fields.length==1){
					CharacterValue cv = CharacterValueFactory.create(label, fields[0]);
					if(!valueList.contains(cv)) valueList.add(cv);
				}else if(fields.length==3){
					CharacterValue cv = CharacterValueFactory.create(label);
					cv.setNegation(fields[0]);
					cv.setValueModifier(fields[1]);
					cv.setValue(fields[2]);
					if(!valueList.contains(cv)) valueList.add(cv);
				}
				
				
			}
		}
		
		return valueList;
	}

	/**
	 * parse the string into multiple CharacterValues
	 * 
	 * 
	 */
	public List<CharacterValue> parseSimple(ILabel label, String valueStr) {
		//separate multiple values
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
