package edu.arizona.biosemantics.micropie.extract.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.Phrase;


/**
 * infer the value for the 
 * @author maojin
 *
 */
public class ContextInferrenceExtractor {
	
	private int phraseWindow = 2;

	/**
	 * The characters of the surrounding terms must be the same
	 * @param followPhrase 
	 * @param aheadPhrase 
	 * @param curPhrase 
	 * @return
	 */
	public CharacterValue strictStrategy1(Phrase curPhrase, PhraseRelationGraph coordGraph ){
		Set<Phrase> aheadPhraseSet = coordGraph.getAheadPhrase(curPhrase);
		Set<Phrase> followPhraseSet = coordGraph.getFollowPhrase(curPhrase);
		Phrase aheadPhrase = null;
		Phrase followPhrase = null;
		if(aheadPhraseSet!=null&&aheadPhraseSet.size()>0) aheadPhrase = aheadPhraseSet.iterator().next();
		if(followPhraseSet!=null&&followPhraseSet.size()>0) followPhrase = followPhraseSet.iterator().next();
		
		if(aheadPhrase==null||followPhrase == null) return null;
		CharacterValue aheadValue = aheadPhrase.getCharValue();
		CharacterValue followValue = followPhrase.getCharValue();
		
		if(aheadValue==null||followValue == null) return null;
		ILabel aheadLabel= aheadValue.getCharacter();
		ILabel followLabel= followValue.getCharacter();
		if(aheadLabel==null||followLabel == null) return null;
		
		if(aheadLabel == followLabel ){//The characters of the surrounding terms are the same
			CharacterValue cv = curPhrase.convertValue(aheadLabel);
			return cv;
		}
		return null;
	}
	
	/**
	 * using a decay function to 
	 * @param followPhrase 
	 * @param aheadPhrase 
	 * @param curPhrase 
	 * @return
	 */
	public CharacterValue strictStrategy2(Phrase curPhrase, PhraseRelationGraph coordGraph){
		Map<Phrase, Integer> aheadPhraseSet = coordGraph.expandSubgraph(curPhrase, this.phraseWindow);
		Map<ILabel, Double> catProbl = new HashMap();
		
		for(Entry<Phrase, Integer> e:aheadPhraseSet.entrySet()){
			Phrase p = e.getKey();
			CharacterValue cv = p.getCharValue();
			ILabel idlabel = cv==null?null:cv.getCharacter();
			int level = e.getValue();
			
			double weight = 1-level/(this.phraseWindow+1);
			
			Double orgWeight = catProbl.get(idlabel);
			if(orgWeight==null) orgWeight=0d;
			catProbl.put(idlabel, orgWeight+weight);
		}
		
		//find the largest one
		ILabel largest = null;
		double max=0;
		double maxNum = 0;
		for(Entry<ILabel, Double> lw:catProbl.entrySet()){
			ILabel el = lw.getKey();
			double value = lw.getValue();
			if(el!=null&&value>max){
				largest = el;
				max = value;
				maxNum = 1;
			}else if(el!=null&&value==max){
				maxNum = 2;
			}
		}
		
		if(maxNum==1){
			CharacterValue cv = curPhrase.convertValue(largest);
			return cv;
		}
		
		return null;
	}
}