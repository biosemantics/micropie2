package edu.arizona.biosemantics.micropie.extract.keyword;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Inject;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.nlptool.PhraseParser;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.stanford.nlp.ling.TaggedWord;


/**
 * 1, first generate phrases from the sentences
 * 2, then match 
 * @author maojin
 */
public class PhraseBasedExtractor extends KeywordBasedExtractor{
	//protected SentenceSpliter sentSplitter;
	protected PosTagger posTagger;
	
	protected PhraseParser phraseParser = null;
	
	/**
	 * should return the matched keywords or should return the whole phrase
	 */
	protected String matchMode = "P";//P: phrase; W: keyword
	
	public PhraseBasedExtractor(PosTagger posTagger,ILabel label, String character,
			Set<String> keywords, Map<String, List> subKeyword) {
		super(label, character, keywords, subKeyword);
		//this.sentSplitter = sentSplitter;SentenceSpliter sentSplitter,
		this.posTagger = posTagger;
		
	}
	
	public PhraseBasedExtractor(ILabel label, String character,
			Set<String> keywords, Map<String, List> subKeyword) {
		super(label, character, keywords, subKeyword);
	}
	
	public PhraseBasedExtractor(ILabel label, String character,
			Set<String> keywords, Map<String, List> subKeyword, String matchMode) {
		super(label, character, keywords, subKeyword);
		this.matchMode = matchMode;
	}
	
	
	public void setMatchMode(String matchMode){
		this.matchMode = matchMode;
	}

	
//	public void setSentSplitter(SentenceSpliter sentSplitter) {
//		//this.sentSplitter = sentSplitter;
//	}

	public void setPosTagger(PosTagger posTagger) {
		this.posTagger = posTagger;
	}

	public void setPhraseParser(PhraseParser phraseParser) {
		this.phraseParser = phraseParser;
	}

	
	public Set getKeywords(){
		return keywords;
	}
	
	@Override
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
					//System.out.println(text);
					if(text==null||"".equals(text)) continue; 
					//text = text.replace("-", " ").replace("_", " ");
					//detect whether contain keywords
					for (String keywordString : keywords) {
						
						boolean isId = extract(keywordString, text);
						
						if(isId){
							pharse.convertValue(this.getLabel());
							CharacterValue charVal = pharse.getCharValue();
							if("W".equals(this.matchMode)) charVal.setValue(keywordString);
							charValueList.add(charVal);
							//System.out.println("OUTER PHRASE HIT VALUE: ["+charVal+"]");
							////returnCharacterStrings.add(text);
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
	
	/**
	 * extract one character
	 * @param keywordString
	 * @param text
	 * @param returnCharacterStrings
	 */
	public boolean extract(String keywordString, String text){
		keywordString = keywordString.toLowerCase().trim();
		keywordString = keywordString.replace("+", "\\+");
		//keywordString = keywordString.replace("-", " ");
		//text = text.replace("-", " ");
		
		String patternString = "^"+keywordString+"\\s|\\s"+keywordString+"\\s|\\s"+keywordString+"$|^"+keywordString+"$"; // regular expression pattern
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text);			
		if (matcher.find() && keywordString.length() > 1) {
			String matchString = matcher.group().trim();
			if(matchString.substring(matchString.length()-1, matchString.length()).equals(",")) {
				matchString = matchString.substring(0, matchString.length()-1);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 1, do not separate subsetences
	 * 2, postag each subsentence
	 * 
	 * @param sentence
	 */
	public void posSentence(MultiClassifiedSentence sentence){
		List taggerwordsList =  new LinkedList();
		sentence.setSubSentTaggedWords(taggerwordsList);
		String content = sentence.getText();
		List<TaggedWord> taggedWords  = posTagger.tagString(content);
		taggerwordsList.add(taggedWords);
	}
}
