package edu.arizona.biosemantics.micropie.extract.keyword;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	protected SentenceSpliter sentSplitter;
	protected PosTagger posTagger;
	
	private PhraseParser phraseParser = new PhraseParser();
	
	@Inject
	public PhraseBasedExtractor(SentenceSpliter sentSplitter,PosTagger posTagger,ILabel label, String character,
			Set<String> keywords, Map<String, List> subKeyword) {
		super(label, character, keywords, subKeyword);
		this.sentSplitter = sentSplitter;
		this.posTagger = posTagger;
	}
	
	public PhraseBasedExtractor(ILabel label, String character,
			Set<String> keywords, Map<String, List> subKeyword) {
		super(label, character, keywords, subKeyword);
	}

	
	public void setSentSplitter(SentenceSpliter sentSplitter) {
		this.sentSplitter = sentSplitter;
	}

	public void setPosTagger(PosTagger posTagger) {
		this.posTagger = posTagger;
	}
	
	
	
	

	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		List<CharacterValue> charValueList = null;
		
		Set<String> returnCharacterStrings = new HashSet<String>();
		
		MultiClassifiedSentence sent = (MultiClassifiedSentence)sentence;
		this.posSentence(sent);//get sub sentences and their tagged words list
		List<List<TaggedWord>> taggedWordList = sent.getSubSentTaggedWords();
		
		if(taggedWordList.size()>=1){
			List<Phrase> phraseList = phraseParser.extract(taggedWordList.get(0));
			
			for(Phrase pharse : phraseList){//deal with each pharse
				String text = pharse.getText();
				text = text.substring(0, text.length()-1);
				text = " " + text + " ";
				text = text.toLowerCase();
				
				//detect whether contain keywords
				Set<String> matchedString = new HashSet<String>();
				for (String keywordString : keywords) {
					boolean isId = extract(keywordString, text, matchedString);
					if(isId) continue;//if has found the value;
					List<String> subKeywordList = subKeywords.get(keywordString);
					if(subKeywordList==null) continue;
					for(String subKeyword : subKeywordList){
						boolean isExist = extract(subKeyword, text, returnCharacterStrings);
						if(isExist) break;
					}
				}
				
				if(matchedString.size()>0) returnCharacterStrings.add(text);
			}
			
			//System.out.println("returnCharacterStrings="+returnCharacterStrings);
			charValueList = CharacterValueFactory.createList(this.getLabel(), returnCharacterStrings);
		}
		return charValueList;
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
