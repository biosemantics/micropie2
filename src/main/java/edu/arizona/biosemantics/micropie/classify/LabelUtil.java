package edu.arizona.biosemantics.micropie.classify;

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
					//case MIN:curFd.setCharacter(Label.c24);break;
					//case MAX:curFd.setCharacter(Label.c26);break;
					//case OPT:curFd.setCharacter(Label.c25);break;
					case MIN:curFd.setCharacter(Label.c18);break;
					case MAX:curFd.setCharacter(Label.c20);break;
					case OPT:curFd.setCharacter(Label.c19);break;
					case USP:;
					default:
					};break;
		case NACL:switch(valueGroup){
//					case MIN:curFd.setCharacter(Label.c18);break;
//					case MAX:curFd.setCharacter(Label.c20);break;
//					case OPT:curFd.setCharacter(Label.c19);break;
					case MIN:curFd.setCharacter(Label.c12);break;
					case MAX:curFd.setCharacter(Label.c14);break;
					case OPT:curFd.setCharacter(Label.c13);break;
					case USP:;
					};break;
		case PH:switch(valueGroup){
//					case MIN:curFd.setCharacter(Label.c21);break;
//					case MAX:curFd.setCharacter(Label.c23);break;
//					case OPT:curFd.setCharacter(Label.c22);break;
					case MIN:curFd.setCharacter(Label.c15);break;
					case MAX:curFd.setCharacter(Label.c17);break;
					case OPT:curFd.setCharacter(Label.c16);break;
					case USP:;
					};break;
		case CLENGTH:curFd.setCharacter(Label.c4);break;
		case CSIZE:curFd.setCharacter(null);break;
		case CWIDTH:curFd.setCharacter(Label.c5);break;
		case CDIAM:curFd.setCharacter(Label.c3);break;
		default:
			curFd.setCharacter(null);
		}
	}
}
