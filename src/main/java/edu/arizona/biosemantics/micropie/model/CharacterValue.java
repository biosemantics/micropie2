package edu.arizona.biosemantics.micropie.model;

import edu.arizona.biosemantics.micropie.classify.ILabel;

/**
 * the value of the character
 * @author maojin
 */
public class CharacterValue {
	protected ILabel character;//Tempature_optimal
	protected String negation; //
	protected String valueModifier;//the modifier of the value: near, close to, above, up to
	protected String value; //the value string
	//protected Integer valueFormat;//String, Numerical, Numerical Range
	protected String unit; //the unit
	protected ValueType valueType; //
	
	private Integer termBegIdx;//the start index in the tagged word list
	private Integer termEndIdx;//the end index in the tagged word list
	
	
	public CharacterValue(ILabel label) {
		this.character = label;
	}
	
	public CharacterValue(ILabel label, String charValue) {
		this.character = label;
		this.value = charValue;
	}

	public ILabel getCharacter() {
		return character;
	}
	public void setCharacter(ILabel character) {
		this.character = character;
	}
	public String getNegation() {
		return negation;
	}
	public void setNegation(String negation) {
		this.negation = negation;
	}
	public String getValueModifier() {
		return valueModifier;
	}
	public void setValueModifier(String valueModifier) {
		this.valueModifier = valueModifier;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
//	public Integer getValueFormat() {
//		return valueFormat;
//	}
//	public void setValueFormat(Integer valueFormat) {
//		this.valueFormat = valueFormat;
//	}
	
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	public Integer getTermBegIdx() {
		return termBegIdx;
	}

	public void setTermBegIdx(Integer termBegIdx) {
		this.termBegIdx = termBegIdx;
	}

	public Integer getTermEndIdx() {
		return termEndIdx;
	}

	public void setTermEndIdx(Integer termEndIdx) {
		this.termEndIdx = termEndIdx;
	}


	public ValueType getValueType() {
		return valueType;
	}

	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}
	
	
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
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj){
		//if (!(obj instanceof CharacterValue))
			//return false;	
		if (obj == this)
			return true;
		if(obj == null) return false;
		if(this.hashCode()==obj.hashCode()){
			//System.out.println(this+" "+ obj+" "+ this.hashCode()+" "+obj.hashCode());
			return true;
		}
		return false;
	}
	
	
	/**
	 * 	ILabel character;//Tempature_optimal
	 *  String negation; /
	 *	String valueModifier;
	 * String unit; //the unit
	 */
	@Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 17 + (character == null ? 0 : character.hashCode());
        hash = hash * 31 + (negation == null ? 0 : negation.hashCode());
        hash = hash * 13 + (valueModifier == null ? 0 : valueModifier.hashCode());
        hash = hash * 19 + (unit == null ? 0 : unit.hashCode());
        hash = hash * 13 + (value == null ? 0 : value.hashCode());
        return hash;
    }
}