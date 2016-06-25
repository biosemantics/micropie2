package edu.arizona.biosemantics.micropie.extract;

import java.util.Comparator;

import edu.arizona.biosemantics.micropie.model.CharacterValue;

public class CharacterValueComparator implements Comparator<CharacterValue>{

	@Override
	public int compare(CharacterValue cv1, CharacterValue cv2) {
		// TODO Auto-generated method stub
		return cv1.getValue().compareTo(cv2.getValue());
	}

}
