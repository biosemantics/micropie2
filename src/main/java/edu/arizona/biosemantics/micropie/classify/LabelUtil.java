package edu.arizona.biosemantics.micropie.classify;

import java.util.HashMap;
import java.util.Map;

import edu.arizona.biosemantics.micropie.model.CharacterGroup;
import edu.arizona.biosemantics.micropie.model.NumericCharacterValue;
import edu.arizona.biosemantics.micropie.model.ValueGroup;



/**
 * tools about labels
 * @author maojin
 *
 */
public class LabelUtil {
	/**
	 * determine the character ILabel according to the character group and value group
	 * 
	 * @param curFd
	 */
	public static void determineLabel(NumericCharacterValue curFd) {
		ValueGroup valueGroup = curFd.getValueGroup();
		CharacterGroup characterGroup = curFd.getCharacterGroup();
		if(characterGroup==null) return;
		switch(characterGroup){
		case TEMP:
			if(valueGroup==null) {curFd.setCharacter(null);break;}
			switch(valueGroup){
					case MIN:curFd.setCharacter(Label.c24);break;
					case MAX:curFd.setCharacter(Label.c26);break;
					case OPT:curFd.setCharacter(Label.c25);break;
					case USP:;
					default:
					};break;
		case NACL:switch(valueGroup){
					case MIN:curFd.setCharacter(Label.c18);break;
					case MAX:curFd.setCharacter(Label.c20);break;
					case OPT:curFd.setCharacter(Label.c19);break;
					case USP:;
					};break;
		case PH:switch(valueGroup){
					case MIN:curFd.setCharacter(Label.c21);break;
					case MAX:curFd.setCharacter(Label.c23);break;
					case OPT:curFd.setCharacter(Label.c22);break;
					case USP:;
					};break;
		case CLENGTH:curFd.setCharacter(Label.c4);break;
		case CSIZE:curFd.setCharacter(null);break;
		case CWIDTH:curFd.setCharacter(Label.c5);break;
		//case CDIAM:curFd.setCharacter(Label.c3);break;
		case CDIAM:curFd.setCharacter(Label.c5);break;//merge cell width and cell diameter
		//case GC:curFd.setCharacter(Label.c1);break;//this will harm the performance
		default:
			curFd.setCharacter(null);
		}
	}
	
	public static Map<String, ILabel> stringToLabelMap = new HashMap();
	
	public static Map<ILabel, String> labelToStringMap = new HashMap();
	
	/*59 characters*/
	static{
		stringToLabelMap.put("0", null);
		stringToLabelMap.put("1.1", Label.c1);
		
		stringToLabelMap.put("2.1", Label.c2);
		stringToLabelMap.put("2.2", Label.c3);
		stringToLabelMap.put("2.3", Label.c4);
		stringToLabelMap.put("2.4", Label.c5);
		stringToLabelMap.put("2.5", Label.c6);
		stringToLabelMap.put("2.6", Label.c7);
		stringToLabelMap.put("2.7", Label.c8);
		stringToLabelMap.put("2.8", Label.c9);
		stringToLabelMap.put("2.9", Label.c10);
		stringToLabelMap.put("2.10", Label.c11);
		stringToLabelMap.put("2.1A", Label.c11);
		stringToLabelMap.put("2.11", Label.c12);
		stringToLabelMap.put("2.1B", Label.c12);
		stringToLabelMap.put("2.12", Label.c13);
		stringToLabelMap.put("2.1C", Label.c13);
		stringToLabelMap.put("2.13", Label.c14);
		stringToLabelMap.put("2.1D", Label.c14);
		stringToLabelMap.put("2.14", Label.c15);
		stringToLabelMap.put("2.1E", Label.c15);
		stringToLabelMap.put("2.15", Label.c16);
		stringToLabelMap.put("2.1F", Label.c16);
		
		stringToLabelMap.put("3.1", Label.c17);
		stringToLabelMap.put("3.2", Label.c18);
		stringToLabelMap.put("3.3", Label.c19);
		stringToLabelMap.put("3.4", Label.c20);
		stringToLabelMap.put("3.5", Label.c21);
		stringToLabelMap.put("3.6", Label.c22);
		stringToLabelMap.put("3.7", Label.c23);
		stringToLabelMap.put("3.8", Label.c24);
		stringToLabelMap.put("3.9", Label.c25);
		stringToLabelMap.put("3.10", Label.c26);
		stringToLabelMap.put("3.1A", Label.c26);
		stringToLabelMap.put("3.11", Label.c27);
		stringToLabelMap.put("3.1B", Label.c27);
		stringToLabelMap.put("3.12", Label.c28);
		stringToLabelMap.put("3.1C", Label.c28);
		//combine 3.1D and 3.1G
		stringToLabelMap.put("3.13", Label.c59);
		stringToLabelMap.put("3.1D", Label.c59);
		//stringToLabelMap.put("3.13", Label.c29);
		//stringToLabelMap.put("3.1D", Label.c29);
		stringToLabelMap.put("3.14", Label.c30);
		stringToLabelMap.put("3.1E", Label.c30);
		stringToLabelMap.put("3.15", Label.c31);
		stringToLabelMap.put("3.1F", Label.c31);
		
		stringToLabelMap.put("3.17", Label.c59);
		stringToLabelMap.put("3.1G", Label.c59);
		
		stringToLabelMap.put("4.1", Label.c32);
		stringToLabelMap.put("4.2", Label.c33);
		
		stringToLabelMap.put("5.1", Label.c35);
		stringToLabelMap.put("5.2", Label.c36);
		stringToLabelMap.put("5.3", Label.c37);
		stringToLabelMap.put("5.4", Label.c38);
		stringToLabelMap.put("5.5", Label.c39);
		stringToLabelMap.put("5.6", Label.c40);
		stringToLabelMap.put("6.1", Label.c41);
		stringToLabelMap.put("6.2", Label.c42);
		stringToLabelMap.put("6.3", Label.c43);
		stringToLabelMap.put("6.4", Label.c44);
		
		stringToLabelMap.put("7.1", Label.c45);
		stringToLabelMap.put("7.2", Label.c46);
		stringToLabelMap.put("8.1", Label.c47);
		stringToLabelMap.put("8.2", Label.c48);
		stringToLabelMap.put("8.3", Label.c49);
		stringToLabelMap.put("8.4", Label.c50);
		stringToLabelMap.put("8.5", Label.c51);
		stringToLabelMap.put("8.6", Label.c52);
		
		stringToLabelMap.put("9.1", Label.c53);
		stringToLabelMap.put("9.2", Label.c54);
		stringToLabelMap.put("9.3", Label.c55);
		stringToLabelMap.put("9.4", Label.c56);
		stringToLabelMap.put("9.5", Label.c57);
		stringToLabelMap.put("9.6", Label.c58);
		
		labelToStringMap.put(null, "0");
		labelToStringMap.put( Label.c1,"1.1");
		
		labelToStringMap.put( Label.c2,"2.1");
		labelToStringMap.put( Label.c3,"2.2");
		labelToStringMap.put( Label.c4,"2.3");
		labelToStringMap.put( Label.c5,"2.4");
		labelToStringMap.put( Label.c6,"2.5");
		labelToStringMap.put( Label.c7,"2.6");
		labelToStringMap.put( Label.c8,"2.7");
		labelToStringMap.put( Label.c9,"2.8");
		labelToStringMap.put( Label.c10,"2.9");
		//labelToStringMap.put( Label.c11,"2.10");
		labelToStringMap.put( Label.c11,"2.1A");
		//labelToStringMap.put( Label.c12,"2.11");
		labelToStringMap.put( Label.c12,"2.1B");
		//labelToStringMap.put( Label.c13,"2.12");
		labelToStringMap.put( Label.c13,"2.1C");
		//labelToStringMap.put( Label.c14,"2.13");
		labelToStringMap.put( Label.c14,"2.1D");
		//labelToStringMap.put( Label.c15,"2.14");
		labelToStringMap.put( Label.c15,"2.1E");
		//labelToStringMap.put( Label.c16,"2.15");
		labelToStringMap.put( Label.c16,"2.1F");
		
		labelToStringMap.put( Label.c17,"3.1");
		labelToStringMap.put( Label.c18,"3.2");
		labelToStringMap.put( Label.c19,"3.3");
		labelToStringMap.put( Label.c20,"3.4");
		labelToStringMap.put( Label.c21,"3.5");
		labelToStringMap.put( Label.c22,"3.6");
		labelToStringMap.put( Label.c23,"3.7");
		labelToStringMap.put( Label.c24,"3.8");
		labelToStringMap.put( Label.c25,"3.9");
		//labelToStringMap.put( Label.c26,"3.10");
		labelToStringMap.put( Label.c26,"3.1A");
		//labelToStringMap.put( Label.c27,"3.11");
		labelToStringMap.put( Label.c27,"3.1B");
		//labelToStringMap.put( Label.c28,"3.12");
		labelToStringMap.put( Label.c28,"3.1C");
		//labelToStringMap.put( Label.c29,"3.13");
		labelToStringMap.put( Label.c29,"3.1D");
		//labelToStringMap.put( Label.c30,"3.14");
		labelToStringMap.put( Label.c30,"3.1E");
		//labelToStringMap.put( Label.c31,"3.15");
		labelToStringMap.put( Label.c31,"3.1F");
		
		//labelToStringMap.put( Label.c59,"3.17");
		labelToStringMap.put( Label.c59,"3.1G");
		
		labelToStringMap.put(Label.c32,"4.1");
		labelToStringMap.put(Label.c33,"4.2");
		
		labelToStringMap.put(Label.c35,"5.1");
		labelToStringMap.put(Label.c36,"5.2");
		labelToStringMap.put(Label.c37,"5.3");
		labelToStringMap.put(Label.c38,"5.4");
		labelToStringMap.put(Label.c39,"5.5");
		labelToStringMap.put(Label.c40,"5.6");
		labelToStringMap.put(Label.c41,"6.1");
		labelToStringMap.put(Label.c42,"6.2");
		labelToStringMap.put(Label.c43,"6.3");
		labelToStringMap.put(Label.c44,"6.4");
		
		labelToStringMap.put(Label.c45,"7.1");
		labelToStringMap.put(Label.c46,"7.2");
		labelToStringMap.put(Label.c47,"8.1");
		labelToStringMap.put(Label.c48,"8.2");
		labelToStringMap.put(Label.c49,"8.3");
		labelToStringMap.put(Label.c50,"8.4");
		labelToStringMap.put(Label.c51,"8.5");
		labelToStringMap.put(Label.c52,"8.6");
		
		labelToStringMap.put(Label.c53,"9.1");
		labelToStringMap.put(Label.c54,"9.2");
		labelToStringMap.put(Label.c55,"9.3");
		labelToStringMap.put(Label.c56,"9.4");
		labelToStringMap.put(Label.c57,"9.5");
		labelToStringMap.put(Label.c58,"9.6");
	}
	
	
	public static ILabel stringToLabel(String categoryCode){
		return stringToLabelMap.get(categoryCode);
	}
	
	public static String labelToString(ILabel categorLabel){
		return labelToStringMap.get(categorLabel);
	}
	
	public static void main(String[] args){
		System.out.println(LabelUtil.stringToLabel("9.6"));
	}
}
