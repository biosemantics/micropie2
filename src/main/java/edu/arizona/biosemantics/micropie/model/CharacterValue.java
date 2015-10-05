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
		return this.value;
	}
}