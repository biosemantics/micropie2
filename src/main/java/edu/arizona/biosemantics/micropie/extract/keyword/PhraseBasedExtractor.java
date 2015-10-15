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
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.nlptool.PhraseParser;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.stanford.nlp.ling.TaggedWord;


/**
 * 1, first generate phrases from the sentences
 * 2, then match 
 * @author maojin
 */
public class PhraseBasedExtractor extends KeywordBasedExtractor{
	//protected SentenceSpliter sentSplitter;
	protected PosTagger posTagger;
	
	private PhraseParser phraseParser = new PhraseParser();
	
	@Inject
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

	
//	public void setSentSplitter(SentenceSpliter sentSplitter) {
//		//this.sentSplitter = sentSplitter;
//	}

	public void setPosTagger(PosTagger posTagger) {
		this.posTagger = posTagger;
	}

	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		List<CharacterValue> charValueList = null;
		//System.out.println("sentence:"+sentence.getText());
		Set<String> returnCharacterStrings = new HashSet<String>();
		
		MultiClassifiedSentence sent = (MultiClassifiedSentence)sentence;
		this.posSentence(sent);//get sub sentences and their tagged words list
		List<List<TaggedWord>> taggedWordList = sent.getSubSentTaggedWords();
		
		if(taggedWordList.size()>=1){
			List<List<Phrase>> allPhraseList = sent.getPhraseList();
			boolean createPhraseList = false;
			if(allPhraseList==null){
				createPhraseList = true;
				allPhraseList = new ArrayList();
			}
			for(int subsent=0;subsent<taggedWordList.size();subsent++){
				List<Phrase> phraseList = null;
				if(createPhraseList){//if not exist, extract from scratch
					phraseList = phraseParser.extract(taggedWordList.get(subsent));
					allPhraseList.add(phraseList);
				}else{//if exist, get from the list 
					phraseList = allPhraseList.get(subsent);
				}
				
				for(Phrase pharse : phraseList){//deal with each pharse
					String text = pharse.getText().toLowerCase();
					if(text==null||"".equals(text)) continue; 
					
					//detect whether contain keywords
					for (String keywordString : keywords) {
						
						boolean isId = extract(keywordString, text);
						
						if(isId){
							returnCharacterStrings.add(text);
							continue;//if has found the value;
						}
						List<String> subKeywordList = subKeywords.get(keywordString);
						if(subKeywordList==null) continue;
						boolean isExist = false;
						for(String subKeyword : subKeywordList){
							
							isExist = extract(subKeyword, text);
							//System.out.println("subkeywords:"+subKeyword+"[ "+text+" ]"+isExist);
							if(isExist){
								returnCharacterStrings.add(text);
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
			charValueList = CharacterValueFactory.createList(this.getLabel(), returnCharacterStrings);
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
	
		String patternString = "^"+keywordString+"\\s|\\s"+keywordString+"\\s|\\s"+keywordString+"$|^"+keywordString+"$"; // regular expression pattern
		//System.out.println(patternString);
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
		List taggerwordsList = sentence.getSubSentTaggedWords();
		if(taggerwordsList==null){
			taggerwordsList = new LinkedList();
			sentence.setSubSentTaggedWords(taggerwordsList);
			String content = sentence.getText();
			List<TaggedWord> taggedWords  = posTagger.tagString(content);
			taggerwordsList.add(taggedWords);
		}
	}
}
