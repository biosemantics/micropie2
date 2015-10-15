package edu.arizona.biosemantics.micropie.classify;

import java.util.LinkedList;
import java.util.List;

/**
 * The 9 chategory Label
 * 
 * @author maojin
 */
public enum CategoryLabel implements ILabel {
	
	
	/************************************************** 9 category version *********************************************************/
	c1("1"), c2("2"), c3("3"), c4("4"), c5("5"), c6("6"), c7("7"), c8("8"), c9("9");
	

	/****   MICROPIE SYSTEMS    *****/
	private final String value;

	private CategoryLabel(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	/**
	 * @param value
	 * @return the label associated with the value
	 */
	public static CategoryLabel getEnum(String value) {
		if(value == null)
			return null;
        for(CategoryLabel label : values())
            if(value.equals(label.value)) 
            	return label;
        return null;
    }

	@Override
	public String getValue() {
		return value;
	}	
	
	public static List<ILabel> valuesList() {
		List<ILabel> values = new LinkedList<ILabel>();
		for(ILabel value : values()) {
			values.add(value);
		}
		return values;
	}

}