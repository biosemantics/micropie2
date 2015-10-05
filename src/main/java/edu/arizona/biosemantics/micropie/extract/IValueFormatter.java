package edu.arizona.biosemantics.micropie.extract;

import java.util.List;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.model.CharacterValue;

public interface IValueFormatter {
	//format the values into a string
	public String format(List<CharacterValue> values);
	
	//format the values into a string
	public List<CharacterValue> parse(ILabel label, String valueStr);
}