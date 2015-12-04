package edu.arizona.biosemantics.micropie.model;

import edu.arizona.biosemantics.micropie.classify.ILabel;


/**
 * Numeric Character Value Type
 * 
 * @author maojin
 */
public class NumericCharacterValue extends CharacterValue{
	
	public NumericCharacterValue(ILabel label) {
		super(label);
	}
	
	public NumericCharacterValue(ILabel label, String charValue) {
		super(label,charValue);
	}
	
	public NumericCharacterValue(ILabel label, String charValue, String unit) {
		super(label,charValue);
		this.unit = unit;
	}
	
	
	
	private CharacterGroup characterGroup;
	private String subCharacter;//
	private ValueGroup valueGroup;
	
	public CharacterGroup getCharacterGroup() {
		return characterGroup;
	}
	public void setCharacterGroup(CharacterGroup characterGroup) {
		this.characterGroup = characterGroup;
	}
	public String getSubCharacter() {
		return subCharacter;
	}
	public void setSubCharacter(String subCharacter) {
		this.subCharacter = subCharacter;
	}
	public ValueGroup getValueGroup() {
		return valueGroup;
	}
	public void setValueGroup(ValueGroup valueGroup) {
		this.valueGroup = valueGroup;
	}
	
//	public String toString(){
//		return this.valueGroup+" "+this.characterGroup+" "+this.negation+" "+this.valueModifier==null?"":this.valueModifier+" "+this.value+" "+this.unit+" "+this.valueType;
//	}
	
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(this.character);
		sb.append(":");
		if(this.negation!=null&&!"".equals(this.negation)){
			sb.append(this.negation);
			sb.append("|");
			sb.append(this.valueModifier==null?"":this.valueModifier);
			sb.append("|");
		}else{
			if(this.valueModifier!=null&&!"".equals(this.valueModifier)){
				sb.append(this.valueModifier);
				sb.append("|");
			}
		}
		sb.append(this.value);
		
		if(this.unit!=null&&!"".equals(this.unit)){
			sb.append("|");
			sb.append(this.unit);
		}
		
		if(this.subCharacter!=null&&!"".equals(this.subCharacter)){
			if(this.unit==null||"".equals(this.unit)){
				sb.append("|");
			}
			sb.append("|");
			sb.append(this.subCharacter);
		}
		return sb.toString();
	}
}
