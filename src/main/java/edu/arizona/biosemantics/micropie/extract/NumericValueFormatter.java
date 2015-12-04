package edu.arizona.biosemantics.micropie.extract;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.eval.IValueComparator;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.NumericCharacterValue;

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
			valueStr.append(format(cv));
			if(i!=size-1) valueStr.append(separator);
		}
		return valueStr.toString();
	}
	
	
	/**
	 * format as:  Negation | Modifier | Main Value | Units |Subtypes
	 * 
	 * @param value
	 * @return
	 */
	public String format(CharacterValue cv) {
		
		NumericCharacterValue value = (NumericCharacterValue)cv;
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
		if(value.getUnit()!=null&&!"".equals(value.getUnit())) {
			valueStr.append("|");
			valueStr.append(replaceNull(value.getUnit()));
		}
		if(value.getSubCharacter()!=null&&!"".equals(value.getSubCharacter())) {
			if(value.getUnit()==null||"".equals(value.getUnit())){
				valueStr.append("|");
			}
			valueStr.append("|");
			valueStr.append(replaceNull(value.getSubCharacter()));
		}
		
		return valueStr.toString();
	}
	
	public String replaceNull(String str){
		return str==null?"":str;
	}

	@Override
	/**
	 * parse the string into multiple CharacterValues
	 * 
	 * 
	 * Negation | Modifier | Main Value | Units |Subtypes
	 * 
	 * Format 1: 44.4|mol%, 1.3-3.7|μm
	 * Format 2: 0.5|%|NaCl
	 * Format 3: 6.0
	 * Format 4: <|40|°C
	 * Format 5: <|40|°C
	 * Format 6: not||0.5|%|NaCl
	 * 
	 * First identify the main value field
	 */
	public List<CharacterValue> parse(ILabel label, String valueStr) {
		String[] values = valueStr.split(separator);
		
		List<CharacterValue> valueList = new ArrayList();
		for(String value:values){
			NumericCharacterValue cv =  null;
			String[] fields = value.split("\\|",-1);
			
			if(fields.length==1){
				cv = CharacterValueFactory.createNumericValue(label, value, null);
			}else{
				cv = CharacterValueFactory.createNumericValue(label, value, null);
				int mainIndex = -1;
				for(int i=0;i<fields.length;i++){
					if(containNumber(fields[i])){
						mainIndex =i;
						cv.setValue(fields[i]);
					}
				}
				
				//forward search
				if(mainIndex-1>=0&&!"".equals(fields[mainIndex-1].trim())){//modifier
					cv.setValueModifier(fields[mainIndex-1].trim());
				}
				if(mainIndex-2>=0&&!"".equals(fields[mainIndex-2].trim())){//negation
					cv.setNegation(fields[mainIndex-2].trim());
				}
				
				//backward search
				if(mainIndex+1<fields.length&&!"".equals(fields[mainIndex+1].trim())){//unit
					cv.setUnit(fields[mainIndex+1].trim());
				}
				if(mainIndex+2<fields.length&&!"".equals(fields[mainIndex+2].trim())){//subcharacter
					cv.setSubCharacter(fields[mainIndex+2].trim());
				}
				
			}
			
			if(!valueList.contains(cv)) valueList.add(cv);
		}
		
		return valueList;
	}

	
	/**
	 * whether a string is a numeric value
	 * 
	 * @param field
	 * @return
	 */
	public boolean isNumeric(String field){
		//System.out.println(field);
		if(field.indexOf("-")>-1){//It is a range
			String[] pairs = field.split("\\-");
			return isNumeric(pairs[0]);
		}else{
			try{
				Double d = new Double(field);
				return true;
			}catch(Exception e){
				return false;
			}
		}
	}
	
	/**
	 * only contain a number
	 * @param word
	 * @return
	 */
	public boolean containNumber(String word) {
		//System.out.println(word.matches("[+-]?[1-9]+[0-9]*(\\.[0-9]+)?[-]?[1-9]+[0-9]*(\\.[0-9]+)?"));
		if(word.length()==1){
			return word.matches("[0-9]+");
		}else{
			Matcher m = Pattern.compile(".*\\d+.*").matcher(word);// Pattern.compile(".*\\d+.*").matcher(word);
			if (m.matches()){
				return true;
			}
			//return word.matches("[+-.0-9]+"); 
		}
		
		return false;
	}
}
