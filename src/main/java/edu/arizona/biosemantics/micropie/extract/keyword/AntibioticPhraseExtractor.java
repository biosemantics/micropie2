package edu.arizona.biosemantics.micropie.extract.keyword;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.extract.context.RelationParser;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.nlptool.PhraseParser;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.stanford.nlp.ling.TaggedWord;



/**
 * phrase based antibiotic extractor
 * First, identify the coordinative relationships.
 * Second, find the keyword and one substances that follows it.
 * Third, determine the phrases that have coordinative relationships with this keyword
 * 
 * @author maojin
 */
public class AntibioticPhraseExtractor extends AbstractCharacterValueExtractor{

	private PosTagger posTagger;
	private PhraseParser phraseParser = null;
	private RelationParser phraseRelationParser = null;
	private SentenceSpliter sentSplitter;
	
	
	//susceptibility
	//susceptible
	private Set<String> sensTypeKeywords = null;
	
	public AntibioticPhraseExtractor(ILabel label, String characterName) {
		super(label, characterName);
	}
	
	public AntibioticPhraseExtractor(ILabel label, String characterName,PosTagger posTagger,PhraseParser phraseParser,RelationParser phraseRelationParser, SentenceSpliter sentSplitter, Set<String> sensTypeKeywords) {
		super(label, characterName);
		this.posTagger = posTagger;
		this.phraseParser = phraseParser;
		this.sensTypeKeywords = sensTypeKeywords;
		this.sentSplitter = sentSplitter;
		this.phraseRelationParser = phraseRelationParser;
	}
	
	
	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		// TODO Auto-generated method stub
		String text = sentence.getText();
		
		//1, get rid of all the content in the brackets
		String cleanSent = sentSplitter.removeBrackets(text);
		
		List<TaggedWord> tagList = posTagger.tagString(cleanSent);
		List<Phrase> phraseList = phraseParser.extract(tagList);
		
		//First, identify the coordinative relationships.
		List<List<Phrase>> coordTermLists = phraseRelationParser.getCoordList(phraseList,tagList);
		
		
		//Second, find the keyword and one substances that follows it.
		//Third, determine the phrases that have coordinative relationships with this keyword
		List<Phrase> valueCandList = null;
		String negation = null;
		for(int w=0;w<tagList.size()-2;w++){
			TaggedWord tw = tagList.get(w);
			String word = tw.word();
			if(sensTypeKeywords.contains(word.toLowerCase())&&w<tagList.size()-2){//record the location
				//System.out.println("hit keyword:"+word.toLowerCase());
				TaggedWord nextTw = tagList.get(w+1);
				if(nextTw.word().equalsIgnoreCase("to")||nextTw.word().equalsIgnoreCase("with")||nextTw.word().equalsIgnoreCase("by")){//the next one should be the phrase
					//w+2 should be the start index, and the position difference should not be greater than 2
					//sensitive w to w+1 <w+2 w+3>
					valueCandList = sortByPosition(coordTermLists, w+2);
					
					for(int nindex = w-2;nindex>0&&nindex<w+2;nindex++){
						TaggedWord ntw = tagList.get(nindex);
						if(ntw.word().equals("not")){
							negation = ntw.word();
						}
					}
					
					//for a single word or phrase
					if(valueCandList==null){
						Phrase oneCandPhrase = sortASinglePhrase(phraseList, w+2);
						if(oneCandPhrase!=null){
							valueCandList = new ArrayList();
							valueCandList.add(oneCandPhrase);
						}
					}
					
					if(valueCandList!=null) break;//found values
				}
			}//end
		}
		
		List<CharacterValue> cvList = new ArrayList();
		if(valueCandList!=null){
			for(Phrase phrase:valueCandList){
				phrase.setNegation(negation);
				CharacterValue cv = phrase.convertValue(this.getLabel());
				cvList.add(cv);
			}
		}
		
		return cvList;
	}

	
	public Phrase sortASinglePhrase(List<Phrase> phraseList, int startPosition) {
		for(Phrase pr : phraseList){
			int startIndex = pr.getStartIndex();
			if(Math.abs(startIndex-startPosition)<=1){//just following the keyphrase
				return pr;
			}
		}
		return null;
	}

	/**
	 * find the candidate value list according to the position
	 * @param coordTermLists
	 * @param startPosition
	 * @return
	 */
	public List<Phrase> sortByPosition(List<List<Phrase>> coordTermLists, int startPosition) {
		for(List<Phrase> coordList : coordTermLists){
			for(Phrase pr : coordList){
				int startIndex = pr.getStartIndex();
				if(Math.abs(startIndex-startPosition)<=1){//just following the keyphrase
					return coordList;
				}
			}
		}
		
		return null;
	}
	
	
}
