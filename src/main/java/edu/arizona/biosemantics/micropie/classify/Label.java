package edu.arizona.biosemantics.micropie.classify;

import java.util.LinkedList;
import java.util.List;

/**
 * The classification labels and their values used in the datasets
 * @author rodenhausen
 */
public enum Label implements ILabel {
	
	c0("0"),
	c1("1"), c11("11"), c12("12"), c13("13"),c14("14"), c15("15"), c16("16"), c17("17"), c18("18"),c19("19"), c110("110"), c111("111"), c112("112"), c113("113"), c114("114"), c115("115"),
	c2("2"), c21("21"), c22("22"), c23("23"),c24("24"), c25("25"), c26("26"), c27("27"), c28("28"),c29("29"), c210("210"), c211("211"), c212("212"), c213("213"), c214("214"), c215("215"),
	c3("3"), c31("31"), c32("32"), c33("33"),c34("34"), c35("35"), c36("36"), c37("37"), c38("38"),c39("39"), c310("310"), c311("311"), c312("312"), c313("313"), c314("314"), c315("315"),
	c4("4"), c41("41"), c42("42"), c43("43"),c44("44"), c45("45"), c46("46"), c47("47"), c48("48"),c49("49"), c410("410"), c411("411"), c412("412"), c413("413"), c414("414"), c415("415"),
	c5("5"), c51("51"), c52("52"), c53("53"),c54("54"), c55("55"), c56("56"), c57("57"), c58("58"),c59("59"), c510("510"), c511("511"), c512("512"), c513("513"), c514("514"), c515("515"),
	c6("6"), c61("61"), c62("62"), c63("63"),c64("64"), c65("65"), c66("66"), c67("67"), c68("68"),c69("69"), c610("610"), c611("611"), c612("612"), c613("613"), c614("614"), c615("615"),
	c7("7"), c71("71"), c72("72"), c73("73"),c74("74"), c75("75"), c76("76"), c77("77"), c78("78"),c79("79"), c710("710"), c711("711"), c712("712"), c713("713"), c714("714"), c715("715"),
	c8("8"), c81("81"), c82("82"), c83("83"),c84("84"), c85("85"), c86("86"), c87("87"), c88("88"),c89("89"), c810("810"), c811("811"), c812("812"), c813("813"), c814("814"), c815("815"),
	c9("9"), c91("91"), c92("92"), c93("93"),c94("94"), c95("95"), c96("96"), c97("97"), c98("98"),c99("99"), c910("910"), c911("911"), c912("912"), c913("913"), c914("914"), c915("915"),
	;
	/*
	c0("0"), c1("1"), c2("2"), c3("3"), c4("4"), c5("5"), 
	c6("6"), c7("7"), c8("8"), c9("9"), c10("10"), c11("11"), 
	c12("12"), c13("13"), c14("14"), c15("15"), c16("16"), c17("17"), 
	c18("18"), c19("19"), c20("20"), c21("21"), c22("22"), c23("23"), 
	c24("24"), c25("25"), c26("26"), c27("27"), c28("28"), c29("29"), 
	c30("30")
	;
	*/

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
		
		// System.out.println("value::" + value);
		
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