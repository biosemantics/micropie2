package edu.arizona.biosemantics.micropie.eval;

import edu.arizona.biosemantics.micropie.model.CharacterValue;

import java.util.List;
import java.util.Set;

/**
 * CharacterValue Comparetor
 * @author maojin
 *
 */
public interface IValueComparator {
	/**
	 * compare the character values, get the credits
	 * @param gstValues gold standard values
	 * @param tgValues tagert matrix values
	 * @return
	 */
	public double compare(List<CharacterValue> gstValues, List<CharacterValue> tgValues);
	
}
