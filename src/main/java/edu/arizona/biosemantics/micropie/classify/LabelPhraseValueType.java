package edu.arizona.biosemantics.micropie.classify;

import java.util.HashSet;
import java.util.Set;

/**
 * Specify the value types for each label
 * @author maojin
 *
 */
public class LabelPhraseValueType {
	
	public Set<ILabel> nuCharSet = new HashSet();
	public Set<ILabel> jpCharSet = new HashSet();
	public Set<ILabel> npCharSet = new HashSet();
	public Set<ILabel> spCharSet = new HashSet();
	
}
