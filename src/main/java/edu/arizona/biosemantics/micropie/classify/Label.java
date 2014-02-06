package edu.arizona.biosemantics.micropie.classify;

import java.util.LinkedList;
import java.util.List;

/**
 * The classification labels and their values used in the datasets
 * @author rodenhausen
 */
public enum Label implements ILabel {
	c0("0"), c1("1"), c2("2"), c3("3"), c4("4"), c5("5"), 
	c6("6"), c7("7"), c8("8"), c9("9"), c10("10"), c11("11"), 
	c12("12"), c13("13"), c14("14"), c15("15"), c16("16"), c17("17"), 
	c18("18"), c19("19"), c20("20"), c21("21"), c22("22"), c23("23"), 
	c24("24"), c25("25"), c26("26"), c27("27"), c28("28"), c29("29"), 
	c30("30")
	;

	private final String value;

	private Label(String value) {
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
	public static Label getEnum(String value) {
		if(value == null)
            throw new IllegalArgumentException();
        for(Label label : values())
            if(value.equals(label.value)) 
            	return label;
        throw new IllegalArgumentException();
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