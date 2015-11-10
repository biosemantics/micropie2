package edu.arizona.biosemantics.micropie.extract.keyword;

import java.util.ArrayList;
import java.util.HashSet;
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
import edu.arizona.biosemantics.micropie.nlptool.VerbIdentifier;
import edu.stanford.nlp.ling.TaggedWord;


/**
 * Chemical Compound and keywords
 * 
 * 1, first identify the given chemical compounds and then identify the 
 * @author maojin
 *
 */
public class SalinityPreferenceExtractor extends KeywordBasedExtractor{
	
	private VerbIdentifier verbIdentifier = new VerbIdentifier();
	/**
	 * require pattern
	 */
	public Set<String> requirePatterns = new HashSet();
	{
		requirePatterns.add("require");
		requirePatterns.add("enhance");
		requirePatterns.add("support");
		requirePatterns.add("foster");
		requirePatterns.add("facilitat");
	}
	
	/**
	 * not require pattern
	 */
	public Set<String> notrequirePatterns = new HashSet();
	{
		notrequirePatterns.add("inhibit");
		notrequirePatterns.add("prohibit");
	}
	
	
	
	private int checkWindow = 5;

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
				//System.out.println("phrases:" +text);
				//detect whether contain keywords
				//System.out.println("charValueList:" +charValueList);
				for (String keywordString : keywords) {
					String substance = keywordString.substring(1,keywordString.length());
					//System.out.println("phrases:" +text+" to find "+isExist(substance.toLowerCase(), text.toLowerCase())+" in ["+keywordString+"]");
					
					//it's an adjective to describe the salinity preference
					//Situation 1: Adjective descriptions;
					//Sample sentences:
					//Halophilic, growing between 1.0 and 7.5 % (w/v) NaCl with optimum growth at 1–3 %.
					if(keywordString.indexOf("@")==-1&&isExist(keywordString.toLowerCase(), text.toLowerCase())){//adjectives
						//System.out.println("adjectives");
						phrase.convertValue(this.getLabel());
						phrase.setText(keywordString);
						CharacterValue charVal = phrase.getCharValue();
						charValueList.add(charVal);
						//System.out.println("Adjective words: ["+charVal+"]");
						break;//if has found the value;
						
					//Situation 2: Require some kinds of substances.
					//Sample sentences:
					//Halophilic, growing between 1.0 and 7.5 % (w/v) NaCl with optimum growth at 1–3 %.
					}else if(keywordString.indexOf("@")>-1&&isExist(substance.toLowerCase(), text.toLowerCase())){//compounds
						phrase.setText(substance);
						//System.out.println("found substances:" +substance+" "+phrase.getStart());
						//whether need require
						//Situation 2a: Explicitly express the requirement. 
						boolean foundNew = detectExplicitExpression(taggedwordsList, phrase, charValueList);
						if(foundNew){
							break;
						}else{
							boolean absentFoundInferred =detectAbsentExpression(sentStr, taggedwordsList, phrase, charValueList);
							if(absentFoundInferred){//either
								break;
							}
							boolean presentFoundInferred =detectPresentExpression(sentStr, taggedwordsList, phrase, charValueList);
							if(presentFoundInferred){//either
								break;
							}else{//default as requirement
								//find the nearest verb
								defaultExpression(taggedwordsList, phrase, charValueList);
							}
						}
						
					
						break;//if has found the value, do not find in this sentence anymore
				}//:~
			}//:~
		}//all phrase
		}
		
		return charValueList;
	}
	
	
	/**
	 * 
	 * @param taggedwordsList
	 * @param phrase
	 * @param charValueList
	 */
	public void defaultExpression(List<TaggedWord> taggedwordsList, Phrase phrase,
			List<CharacterValue> charValueList){
		TaggedWord verbWord = verbIdentifier.identifyNearestVerb(phrase, taggedwordsList);
		//find a nearby negation
		String negation = negationIdentifier.detectNeighbourNegation(taggedwordsList, verbWord);
		phrase.setNegation(negation);
		
		phrase.convertValue(this.getLabel());
		phrase.getCharValue().setValue("require "+phrase.getCharValue().getValue());
		CharacterValue charVal = phrase.getCharValue();
		//System.out.println("infer for present : ["+charVal+"]");
		charValueList.add(charVal);
	}
	
	
	/**
	 * infer the requirements from the sentences
	 * @param sentStr
	 * @param taggedwordsList
	 * @param phrase
	 * @param charValueList
	 * @return
	 */
	public boolean detectPresentExpression(String sentStr,
			List<TaggedWord> taggedwordsList, Phrase phrase,
			List<CharacterValue> charValueList) {
		//boolean foundNew = false;
		//Situation 2b: Explicitly express the requirement. 
		//Present State and the Verb
		//in the absence
		int phraseStartPosition = phrase.getStart();
		
		//in the presence
		int preIndex = sentStr.indexOf("in the presen");
		//System.out.println("infer for present : ");
		int preAbsIndex = sentStr.indexOf("in the absen");
		if(preIndex>-1&&preIndex<phraseStartPosition&&(preIndex>preAbsIndex||preAbsIndex>phraseStartPosition)){//it an present
			TaggedWord verbWord = verbIdentifier.identifyAheadVerb(preIndex, taggedwordsList);
			//find a nearby negation
			String negation = negationIdentifier.detectNeighbourNegation(taggedwordsList, verbWord);
			
			if(negation!=null&&!"".equals(negation.trim())){
				phrase.setNegation("negation");
			}
			
			negation = reverseNegation(verbWord, negation);
			phrase.setNegation(negation);
			
			phrase.convertValue(this.getLabel());
			phrase.getCharValue().setValue("require "+phrase.getCharValue().getValue());
			CharacterValue charVal = phrase.getCharValue();
			//System.out.println("infer for present : ["+charVal+"]");
			charValueList.add(charVal);
			return true;
		}
		
		
		//returnCharacterStrings.add(text);
		return false;
	}

	
	/**
	 * infer the requirements from the sentences
	 * @param sentStr
	 * @param taggedwordsList
	 * @param phrase
	 * @param charValueList
	 * @return
	 */
	public boolean detectAbsentExpression(String sentStr,
			List<TaggedWord> taggedwordsList, Phrase phrase,
			List<CharacterValue> charValueList) {
		//boolean foundNew = false;
		//Situation 2b: Explicitly express the requirement. 
		//Present State and the Verb
		//in the absence
		int phraseStartPosition = phrase.getStart();
		int preIndex = sentStr.indexOf("in the absen");
		int prePresIndex = sentStr.indexOf("in the presen");
		
		if(preIndex>-1&&preIndex<phraseStartPosition&&(preIndex>prePresIndex||prePresIndex>phraseStartPosition)){//it an absent
			TaggedWord verbWord = verbIdentifier.identifyAheadVerb(preIndex, taggedwordsList);
			//find a nearby negation
			String negation = negationIdentifier.detectNeighbourNegation(taggedwordsList, verbWord);
			
			
			negation = reverseNegation(verbWord, negation);
			//System.out.println("reversed negation: ["+negation+"] for "+verbWord);
			if(negation!=null&&!"".equals(negation.trim())){
				
			}else{
				phrase.setNegation("not");
			}
			
			
			//phrase.setNegation(negation);
			
			phrase.convertValue(this.getLabel());
			phrase.getCharValue().setValue("require "+phrase.getCharValue().getValue());
			CharacterValue charVal = phrase.getCharValue();
			//System.out.println("infer for absent : ["+charVal+"]");
			charValueList.add(charVal);
			
			return true;
		}
		
		
		//returnCharacterStrings.add(text);
		return false;
	}
	
	
	

	/**
	 * if the word is a netation verb, then reverse it
	 * @param verbWord
	 * @return
	 */
	private String reverseNegation(TaggedWord verbWord, String negation) {
		for(String notrequireStr: notrequirePatterns){
			if(verbWord.word().toLowerCase().startsWith(notrequireStr)){
				if(negation==null||"".equals(negation)){
					negation = "not";
				}else{
					negation = null;
				}
				return negation;
			}
		}
		return negation;
	}

	/**
	 * detect explicitly expressed requirements 
	 * @param taggedwordsList
	 * @return
	 */
	public boolean detectExplicitExpression(List<TaggedWord> taggedwordsList, Phrase phrase, List<CharacterValue> charValueList) {
		boolean foundNew = false;
		Boolean isRequired = null;
		int startIndex = phrase.getStartIndex();
		int endIndex = phrase.getEndIndex();
		int requireIndex = -1;
		//backward search
		for(int checkIndex = startIndex-1;checkIndex>=0;checkIndex--){
			String word = taggedwordsList.get(checkIndex).word().toLowerCase();
			for(String requireStr: requirePatterns){
				if(word.indexOf(requireStr)>-1){
					isRequired = true;
					requireIndex = checkIndex;
				}
				if(word.equals("but")){
					break;
				}
			}
		}
		
		//forward search
		for(int checkIndex = endIndex+1;checkIndex<taggedwordsList.size();checkIndex++){
			String word = taggedwordsList.get(checkIndex).word().toLowerCase();
			for(String requireStr: requirePatterns){
				if(word.indexOf(requireStr)>-1){
					isRequired = true;
					requireIndex = checkIndex;
					break;
				}
				if(word.equals("but")){
					break;
				}
			}
		}
		
		if(isRequired!=null&&isRequired){
			String negation = negationIdentifier.detectNeighbourNegation(taggedwordsList, requireIndex);
			
			//detect negation
			phrase.setNegation(negation);
			phrase.convertValue(this.getLabel());
			phrase.getCharValue().setValue("require "+phrase.getCharValue().getValue());
			CharacterValue charVal = phrase.getCharValue();
			//System.out.println("explicit requirements: ["+charVal+"]");
			charValueList.add(charVal);
			foundNew = true;
		}
		return foundNew;
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