package edu.arizona.biosemantics.micropie.nlptool;

import edu.stanford.nlp.ling.TaggedWord;


/**
 * 
 * a rule-based classifier
 * 
 * TO-DO: it can be developed into a complex classifier tool
 * predict whether a figure is a number or part of an entity
 * @author maojin
 *
 */
public class FigureClassifier {

	/**
	 * a simple rule is:
	 * if the former term is a noun and it contains uppercases, it's part of an entity
	 * @param figureWord
	 * @param aheadWord
	 * @return
	 */
	public boolean isEntity(TaggedWord figureWord, TaggedWord aheadWord){
		String ahTag = aheadWord.tag();
		String ahWord = aheadWord.word();
		//System.out.println(ahTag+" "+ahWord);
		if(!ahWord.equals(ahWord.toLowerCase())&&ahTag.startsWith("N")){
			return true;
		}
		return false;
	}
}
