package edu.arizona.biosemantics.micropie.extract.keyword;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.nlptool.ClauseIdentifier;
import edu.arizona.biosemantics.micropie.nlptool.NegationIdentifier;
import edu.stanford.nlp.ling.TaggedWord;


/**
 * Physical Tests Extractors
 * 
 * TODO: 
 * 1, improve noun phrase detector
 * 2, divided into multiple sub-categories
 * 
 * @author maojin
 *
 */
public class TestExtractor extends PhraseBasedExtractor{

	private ClauseIdentifier clauseIdentifier = new ClauseIdentifier();
	private NegationIdentifier negIdentifier = new NegationIdentifier();
	
	private ILabel positive = Label.c45;
	private ILabel negative = Label.c46;
	public TestExtractor(ILabel label, String character, Set<String> keywords,
			Map<String, List> subKeyword) {
		super(label, character, keywords, subKeyword);
	}
	
	
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		List<CharacterValue> charValueList =  new ArrayList();
		//segment
		//System.out.println("sentence:"+sentence.getText());
		MultiClassifiedSentence sent = (MultiClassifiedSentence)sentence;
		String sentenceText = sent.getText().replace("-negative", " negative").replace("-positive", " positive");
		
		List<TaggedWord> sentTaggedWords  = posTagger.tagString(sentenceText);
		List<List<TaggedWord>> clauseTaggedWords = clauseIdentifier.segWithSeperator(sentTaggedWords);
		
		if(clauseTaggedWords.size()>=1){
			for(int subsent=0;subsent<clauseTaggedWords.size();subsent++){
				//System.out.println(clauseTaggedWords.get(subsent));
				List<Phrase> phraseList = phraseParser.extract(clauseTaggedWords.get(subsent));
				
				for(Phrase pharse : phraseList){//deal with each phrase
					String text = pharse.getText().toLowerCase();
					//System.out.println("phrase="+text);
					if(text==null||"".equals(text)) continue; 
					text = text.replace("-", " ").replace("_", " ");
					
					String negation = negIdentifier.detectFirstNegation(clauseTaggedWords.get(subsent));
					ILabel label = positive;
					if(negation !=null) label = negative;
					
					//detect whether contain keywords
					for (String keywordString : keywords) {
						
						boolean isId = extract(keywordString, text);
						
						if(isId){
							pharse.convertValue(label);
							CharacterValue charVal = pharse.getCharValue();
							if("W".equals(this.matchMode)) charVal.setValue(keywordString);
							charValueList.add(charVal);
							//System.out.println("OUTER PHRASE HIT VALUE: ["+charVal+"]");
							//returnCharacterStrings.add(text);
							break;//if has found the value;
						}
						List<String> subKeywordList = subKeywords.get(keywordString);
						if(subKeywordList==null) continue;
						boolean isExist = false;
						for(String subKeyword : subKeywordList){
							
							isExist = extract(subKeyword, text);
							//System.out.println("subkeywords:"+subKeyword+"[ "+text+" ]"+isExist);
							if(isExist){
								//CharacterValue charVal = CharacterValueFactory.create(this.getLabel(),text);
								//pharse.setCharValue(charVal);
								pharse.convertValue(label);
								CharacterValue charVal = pharse.getCharValue();
								if("W".equals(this.matchMode)) charVal.setValue(subKeyword);
								charValueList.add(charVal);
								//System.out.println("INNER PHRASE  HIT VALUE: ["+charVal+"]");
								break;
							}
						}
						if(isExist){
							break;
						}
					}
					
				}
			}
			//System.out.println("returnCharacterStrings="+returnCharacterStrings);
			//charValueList = CharacterValueFactory.createList(this.getLabel(), returnCharacterStrings);
		}
		return charValueList;
	}

	
}
