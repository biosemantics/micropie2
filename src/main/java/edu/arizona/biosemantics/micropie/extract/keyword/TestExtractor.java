package edu.arizona.biosemantics.micropie.extract.keyword;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.stanford.nlp.ling.TaggedWord;


/**
 * Physical Tests Extractors
 * @author maojin
 *
 */
public class TestExtractor extends PhraseBasedExtractor{

	public TestExtractor(ILabel label, String character, Set<String> keywords,
			Map<String, List> subKeyword) {
		super(label, character, keywords, subKeyword);
	}
	
	
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		List<CharacterValue> charValueList =  new ArrayList();
		//System.out.println("sentence:"+sentence.getText());
		//Set<String> returnCharacterStrings = new HashSet<String>();
		
		MultiClassifiedSentence sent = (MultiClassifiedSentence)sentence;
		this.posSentence(sent);//get sub sentences and their tagged words list
		List<List<TaggedWord>> taggedWordList = sent.getSubSentTaggedWords();
		
		if(taggedWordList.size()>=1){
			List<List<Phrase>> allPhraseList = sent.getPhraseList();
			boolean createPhraseList = false;
			if(allPhraseList==null){
				createPhraseList = true;
				allPhraseList = new ArrayList();
				sent.setPhraseList(allPhraseList);
			}
			for(int subsent=0;subsent<taggedWordList.size();subsent++){
				List<Phrase> phraseList = null;
				if(createPhraseList){//if not exist, extract from scratch
					phraseList = phraseParser.extract(taggedWordList.get(subsent));
					allPhraseList.add(phraseList);
				}else{//if exist, get from the list 
					phraseList = allPhraseList.get(subsent);
				}
				
				for(Phrase pharse : phraseList){//deal with each phrase
					String text = pharse.getText().toLowerCase();
					System.out.println(text);
					if(text==null||"".equals(text)) continue; 
					text = text.replace("-", " ").replace("_", " ");
					//detect whether contain keywords
					for (String keywordString : keywords) {
						
						boolean isId = extract(keywordString, text);
						
						if(isId){
							pharse.convertValue(this.getLabel());
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
								pharse.convertValue(this.getLabel());
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
