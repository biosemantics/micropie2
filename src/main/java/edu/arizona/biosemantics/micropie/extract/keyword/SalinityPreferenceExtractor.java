package edu.arizona.biosemantics.micropie.extract.keyword;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.nlptool.PhraseParser;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.stanford.nlp.ling.TaggedWord;


/**
 * Chemical Compound and keywords
 * 
 * 1, first identify the given chemical compounds and then identify the 
 * @author maojin
 *
 */
public class SalinityPreferenceExtractor extends KeywordBasedExtractor{
	
	private int checkWindow = 3;

	//protected SentenceSpliter sentSplitter;
	protected PosTagger posTagger;
	private PhraseParser phraseParser;
	
	@Inject
	public SalinityPreferenceExtractor(PosTagger posTagger,ILabel label, String character,
			Set<String> keywords, Map<String, List> subKeyword,
			PhraseParser phraseParser) {
		super(label, character, keywords, subKeyword);
		this.posTagger = posTagger;
		this.phraseParser = phraseParser;
	}
	
	@Inject
	public SalinityPreferenceExtractor(ILabel label, String character,
			Set<String> keywords, Map<String, List> subKeyword) {
		super(label, character, keywords, subKeyword);
	}
	
	public PosTagger getPosTagger() {
		return posTagger;
	}

	public void setPosTagger(PosTagger posTagger) {
		this.posTagger = posTagger;
	}

	public PhraseParser getPhraseParser() {
		return phraseParser;
	}

	public void setPhraseParser(PhraseParser phraseParser) {
		this.phraseParser = phraseParser;
	}

	
	/**
	 * Qualitative statements:
	 * Requires sea salts for growth
	 * inhibited in the absence of NaCl#inhibited in presence of >8 % (w/v) NaCl
	 * 
	 */
	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		//pos tag
		MultiClassifiedSentence sent = (MultiClassifiedSentence)sentence;
		String sentStr = sent.getText();
		this.posSentence(sent);//get sub sentences and their tagged words list
		
		List<CharacterValue> charValueList =  new ArrayList();
		if(sent.getSubSentTaggedWords()!=null&&sent.getSubSentTaggedWords().size()>0){
			List<TaggedWord> taggedwordsList = (List<TaggedWord>)sent.getSubSentTaggedWords().get(0);
			//System.out.println(taggedwordsList);
			List<Phrase> phList = phraseParser.extract(taggedwordsList);
			//System.out.println(phList);
			
			
			for(int cur=0;cur<phList.size();cur++){
				Phrase phrase = phList.get(cur);
				String text = phrase.getText();
				//detect whether contain keywords
				for (String keywordString : keywords) {
					//it's an adjective to describe the salinity preference
					if(keywordString.indexOf("@")==-1&&isExist(keywordString, text)){//adjectives
						//System.out.println("adjectives");
						phrase.convertValue(this.getLabel());
						
						//whether need require
						int startIndex = phrase.getStartIndex();
						int endIndex = phrase.getEndIndex();
						
						for(int checkIndex = startIndex-checkWindow;checkIndex<=endIndex+checkWindow&&checkIndex<taggedwordsList.size();checkIndex++ ){
							if(checkIndex<=0) checkIndex=0;
							//System.out.println(taggedwordsList.get(checkIndex).word()+" "+taggedwordsList.get(checkIndex).word().toLowerCase().indexOf("requir"));
							if(taggedwordsList.get(checkIndex).word().toLowerCase().indexOf("requir")>-1){
								phrase.getCharValue().setValue("require "+phrase.getCharValue().getValue());
							}
						}
						
						CharacterValue charVal = phrase.getCharValue();
						charValueList.add(charVal);
						System.out.println("OUTER PHRASE HIT VALUE: ["+charVal+"]");
						//returnCharacterStrings.add(text);
						break;//if has found the value;
					}else if(keywordString.indexOf("@")>-1&&isExist(keywordString.substring(1,keywordString.length()), text)){{//compounds
						
						//System.out.println("substances:" +text);
						//whether need require
						int startIndex = phrase.getStartIndex();
						int endIndex = phrase.getEndIndex();
						
						Boolean isRequired = null;
						
						for(int checkIndex = startIndex-1;checkIndex>=0;checkIndex--){
							String word = taggedwordsList.get(checkIndex).word().toLowerCase();
							if(word.indexOf("requir")>-1){
								isRequired = true;
							}
							if(word.equals("but")){
								break;
							}
						}
						
						for(int checkIndex = endIndex+1;checkIndex<taggedwordsList.size();checkIndex++){
							String word = taggedwordsList.get(checkIndex).word().toLowerCase();
							if(word.indexOf("requir")>-1){
								isRequired = true;
							}
							if(word.equals("but")){
								break;
							}
						}
						
						if(isRequired!=null&&isRequired){
							phrase.convertValue(this.getLabel());
							phrase.getCharValue().setValue("require "+phrase.getCharValue().getValue());
							CharacterValue charVal = phrase.getCharValue();
							System.out.println("OUTER PHRASE HIT VALUE: ["+charVal+"]");
							charValueList.add(charVal);
						}
						
						//in the absence
						int preIndex = sentStr.indexOf("in the absen");
						if(preIndex>-1){//there is a 
							
						}
						
						//in the presence
						
						
						
						//returnCharacterStrings.add(text);
						break;//if has found the value;
					}
				}//:~
			}//:~
		}
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
		List taggerwordsList =  new LinkedList();
		sentence.setSubSentTaggedWords(taggerwordsList);
		String content = sentence.getText();
		List<TaggedWord> taggedWords  = posTagger.tagString(content);
		taggerwordsList.add(taggedWords);
	}

}