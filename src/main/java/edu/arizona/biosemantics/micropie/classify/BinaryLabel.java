package edu.arizona.biosemantics.micropie.classify;

import java.util.LinkedList;
import java.util.List;

public enum BinaryLabel implements ILabel {
	
	NO("0"), YES("1");

	private final String value;

	private BinaryLabel(String value) {
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
	public static BinaryLabel getEnum(String value) {
        if(value == null)
            throw new IllegalArgumentException();
        for(BinaryLabel label : values())
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
