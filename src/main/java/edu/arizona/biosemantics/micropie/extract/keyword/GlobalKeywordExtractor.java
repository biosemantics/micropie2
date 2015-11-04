package edu.arizona.biosemantics.micropie.extract.keyword;

import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.Phrase;


/**
 * extract values according to global keyword termlist
 * 
 * Is this hypothesis true that one phrase can only have one character value at most?
 * 
 * 
 * @author maojin
 *
 */
public class GlobalKeywordExtractor {
	
	private Map<String, Set<ILabel>> globalTermCharacterMap;
	
	@Inject
	public GlobalKeywordExtractor(@Named("GlobalTermCharacterMap")Map<String, Set<ILabel>> termCharacterMap){
		this.globalTermCharacterMap = termCharacterMap;
	}
	
	/**
	 * If only one label is matched, then output it;
	 * if multiple labels are matched, then a score function is needed.
	 * @param phrase
	 * @return
	 */
	public CharacterValue uniqMatch(Phrase phrase){
		String core = phrase.getCore().toLowerCase().replace("-"," ");
		String phraseText = phrase.getText().toLowerCase().replace("-"," " );
		String modifier = phrase.getModifer().toLowerCase().replace("-"," " );
		
		//it can not handle the situation: long tail
		Set<ILabel> matchedLabels = globalTermCharacterMap.get(core);
		//to handle simple expressions like: long tail
		if(matchedLabels==null&&phraseText.split("[\\s]+").length<=3){
			matchedLabels = globalTermCharacterMap.get(core);
		}
		if(matchedLabels==null){
			String expression = modifier+" "+core;
			matchedLabels = globalTermCharacterMap.get(expression);
		}
		
		//List<CharacterValue> valueList = new ArrayList();
		CharacterValue cv = null;
		//If only one label is matched, then output it;
		if(matchedLabels!=null&&matchedLabels.size() == 1){
			ILabel label = matchedLabels.iterator().next();
			cv = phrase.convertValue(label);
		}
		
		return cv;
	}

}
