package edu.arizona.biosemantics.micropie.classify;

import java.util.LinkedList;
import java.util.List;

/**
 * The classification labels and their values used in the datasets
 * @author rodenhausen
 */
public enum Label implements ILabel {
	
	
	/*
	c0("0"),
	c1("1"), c11("11"), c12("12"), c13("13"),c14("14"), c15("15"), c16("16"), c17("17"), c18("18"),c19("19"), c110("110"), c111("111"), c112("112"), c113("113"), c114("114"), c115("115"),
	c2("2"), c21("21"), c22("22"), c23("23"),c24("24"), c25("25"), c26("26"), c27("27"), c28("28"),c29("29"), c210("210"), c211("211"), c212("212"), c213("213"), c214("214"), c215("215"),
	c3("3"), c31("31"), c32("32"), c33("33"),c34("34"), c35("35"), c36("36"), c37("37"), c38("38"),c39("39"), c310("310"), c311("311"), c312("312"), c313("313"), c314("314"), c315("315"),
	c4("4"), c41("41"), c42("42"), c43("43"),c44("44"), c45("45"), c46("46"), c47("47"), c48("48"),c49("49"), c410("410"), c411("411"), c412("412"), c413("413"), c414("414"), c415("415"),
	c5("5"), c51("51"), c52("52"), c53("53"),c54("54"), c55("55"), c56("56"), c57("57"), c58("58"),c59("59"), c510("510"), c511("511"), c512("512"), c513("513"), c514("514"), c515("515"),
	c6("6"), c61("61"), c62("62"), c63("63"),c64("64"), c65("65"), c66("66"), c67("67"), c68("68"),c69("69"), c610("610"), c611("611"), c612("612"), c613("613"), c614("614"), c615("615"),
	c7("7"), c71("71"), c72("72"), c73("73"),c74("74"), c75("75"), c76("76"), c77("77"), c78("78"),c79("79"), c710("710"), c711("711"), c712("712"), c713("713"), c714("714"), c715("715"),
	c8("8"), c81("81"), c82("82"), c83("83"),c84("84"), c85("85"), c86("86"), c87("87"), c88("88"),c89("89"), c810("810"), c811("811"), c812("812"), c813("813"), c814("814"), c815("815"),
	c9("9"), c91("91"), c92("92"), c93("93"),c94("94"), c95("95"), c96("96"), c97("97"), c98("98"),c99("99"), c910("910"), c911("911"), c912("912"), c913("913"), c914("914"), c915("915")
	;
	*/
	/*
	c0("0"), c1("1"), c2("2"), c3("3"), c4("4"), c5("5"), c6("6"), c7("7"), c8("8"), c9("9"), c10("10"), 
	c11("11"), c12("12"), c13("13"), c14("14"), c15("15"), c16("16"), c17("17"), c18("18"), c19("19"), c20("20"), 
	c21("21"), c22("22"), c23("23"), c24("24"), c25("25"), c26("26"), c27("27"), c28("28"), c29("29"), c30("30"),
	c31("31"), c32("32"), c33("33"), c34("34"), c35("35"), c36("36"), c37("37"), c38("38"), c39("39"), c40("40"),
	c41("41"), c42("42"), c43("43"), c44("44"), c45("45"), c46("46"), c47("47"), c48("48"), c49("49"), c50("50"),
	c51("51"), c52("52"), c53("53"), c54("54"), c55("55"), c56("56"), c57("57"), c58("58"), c59("59"), c60("60"),
	c61("61"), c62("62"), c63("63"), c64("64"), c65("65"), c66("66"), c67("67"), c68("68"), c69("69"), c70("70"),
	c71("71"), c72("72"), c73("73"), c74("74"), c75("75"), c76("76"), c77("77"), c78("78"), c79("79"), c80("80"),
	c81("81"), c82("82"), c83("83"), c84("84"), c85("85"), c86("86"), c87("87"), c88("88"), c89("89"), c90("90"),
	c91("91"), c92("92"), c93("93"), c94("94"), c95("95"), c96("96"), c97("97"), c98("98"), c99("99"), c1000("100"),
	c101("101"), c102("102"), c103("103"), c104("104"), c105("105"), c106("106"), c107("107"), c108("108"), c109("109"), c110("110"),
	c111("111"), c112("112"), c113("113"), c114("114"), c115("115"), c116("116"), c117("117"), c118("118"), c119("119"), c120("120"),
	c121("121"), c122("122"), c123("123"), c124("124"), c125("125"), c126("126"), c127("127"), c128("128"), c129("129"), c130("130"),
	c131("131"), c132("132"), c133("133"), c134("134"), c135("135"), c136("136"), c137("137"), c138("138"), c139("139"), c140("140"),
	c141("141"), c142("142"), c143("143"), c144("144"), c145("145"), c146("146"), c147("147"), c148("148"), c149("149"), c150("150"),
	c151("151"), c152("152"), c153("153"), c154("154"), c155("155"), c156("156"), c157("157"), c158("158"), c159("159"), c160("160"),
	c161("161"), c162("162"), c163("163"), c164("164"), c165("165"), c166("166"), c167("167"), c168("168"), c169("169"), c170("170")
	;
	*/
	
	/*
	c0("0"),c1("1");
	*/
	
	c0("0"), c1("1"), c2("2"), c3("3"), c4("4"), c5("5"), c6("6"), c7("7"), c8("8"), c9("9"), c10("10"), 
	c11("11"), c12("12"), c13("13"), c14("14"), c15("15"), c16("16"), c17("17"), c18("18"), c19("19"), c20("20"), 
	c21("21"), c22("22"), c23("23"), c24("24"), c25("25"), c26("26"), c27("27"), c28("28"), c29("29"), c30("30"),
	c31("31"), c32("32"), c33("33"), c34("34"), c35("35"), c36("36"), c37("37"), c38("38"), c39("39"), c40("40"),
	c41("41"), c42("42"), c43("43"), c44("44"), c45("45"), c46("46"), c47("47"), c48("48"), c49("49"), c50("50"),
	c51("51"), c52("52"), c53("53"), c54("54"), c55("55"), c56("56"), c57("57"), c58("58");
	
	/****   MICROPIE SYSTEMS    *****/
	/*
	c0("0"),
	c1("1"), 
	c21("2.1"), c22("2.2"), c23("2.3"), c24("2.4"), c25("2.5"), c26("2.6"), c27("2.7"), c28("2.8"), c29("2.9"),c210("2.1A"), c211("2.11"), c212("2.12"), c213("2.13"), c214("2.14"), 
	c11("11"), c12("12"), c13("13"), c14("14"), c15("15"), c16("16"), c17("17"), c18("18"), c19("19"), c20("20"), 
	c21("21"), c22("22"), c23("23"), c24("24"), c25("25"), c26("26"), c27("27"), c28("28"), c29("29"), c30("30"),
	c31("31"), c32("32"), c33("33"), c34("34"), c35("35"), c36("36"), c37("37"), c38("38"), c39("39"), c40("40"),
	c41("41"), c42("42"), c43("43"), c44("44"), c45("45"), c46("46"), c47("47"), c48("48"), c49("49"), c50("50"),
	c51("51"), c52("52"), c53("53"), c54("54"), c55("55"), c56("56"), c57("57"), c58("58");
	*/
	//, c59("59"), c60("60")
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
           // throw new IllegalArgumentException();
			return null;
        for(Label label : values())
            if(value.equals(label.value)) 
            	return label;
        return null;
        //throw new IllegalArgumentException();
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