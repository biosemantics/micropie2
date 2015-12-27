package edu.arizona.biosemantics.micropie.classify;

import java.util.HashSet;

/**
 * diffent kinds of label groups
 * 
 * @author maojin
 *
 */
public class LabelGroups {
	
	public HashSet<ILabel> minValueGroup = new HashSet();
	{
		minValueGroup.add(Label.c18);
		minValueGroup.add(Label.c21);
		minValueGroup.add(Label.c24);
	}
	
	public HashSet<ILabel> maxValueGroup = new HashSet();
	{
		maxValueGroup.add(Label.c20);
		maxValueGroup.add(Label.c23);
		maxValueGroup.add(Label.c26);
	}
}