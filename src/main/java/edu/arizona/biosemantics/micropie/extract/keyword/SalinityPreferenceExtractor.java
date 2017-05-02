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
import edu.arizona.biosemantics.micropie.model.NumericCharacterValue;
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
			
			
			//in the presence
			int preIndex = sentStr.indexOf("in the presen");
			int preWordIndex = wordIndex("presence", taggedwordsList);
			//System.out.println("infer for present : ");
			int absIndex = sentStr.indexOf("in the absen");
			int absWordIndex = wordIndex("absence", taggedwordsList);
			String negation = negationIdentifier.detectFirstNegation(taggedwordsList);
			int negationIndex = negationIdentifier.detectFirstNegationIndex(taggedwordsList);
			
//			boolean absNegation = false;
//			boolean preNegation = false;
//			if(negationIndex>-1){
//				if(negationIndex-preWordIndex<0&&absWordIndex<0||
//						negationIndex-preWordIndex<0&&negationIndex-preWordIndex>negationIndex-absWordIndex){//present negation
//					preNegation = true;
//				}
//				if (negationIndex-absWordIndex<0&&preWordIndex<0||
//						negationIndex-absWordIndex<0&&negationIndex-absWordIndex>negationIndex-preWordIndex){//absent substance
//					absNegation = true;
//				}
//			}
			
			//System.out.println("sentence="+sentence.getText());
			//System.out.println("negation="+negation+" negationIndex="+negationIndex);
			//System.out.println("preIndex="+preIndex+" preWordIndex="+preWordIndex);
			//System.out.println("absIndex="+absIndex+" absWordIndex="+absWordIndex);
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
						phrase.setText(text);//phrase text
						
						CharacterValue charVal = phrase.getCharValue();
						charValueList.add(charVal);
						//System.out.println("Adjective words: ["+charVal+"]");
						break;//if has found the value;
						
					//Situation 2: Require some kinds of substances.
					//Sample sentences:
					//Halophilic, growing between 1.0 and 7.5 % (w/v) NaCl with optimum growth at 1–3 %.
					}else if(keywordString.indexOf("@")>-1&&isExist(substance.toLowerCase(), text.toLowerCase())){//compounds
						//System.out.println("substances");
						phrase.convertValue(this.getLabel());
						if(substance.equals("Na+")){// ions
							if(sentStr.indexOf("Na\\+ ions")>-1) phrase.setText("Na+ ions");
						}else{
							phrase.setText(substance);
						}
						System.out.println("found substances:" +substance+" "+phrase.getStart());
						//whether need require
						//Situation 2a: Explicitly express the requirement. 
						int subIndex = phrase.getStartIndex();
						if(preWordIndex>-1&&preWordIndex-subIndex<0&&absWordIndex<0||
								preWordIndex>-1&&preWordIndex-subIndex<0&&Math.abs(preWordIndex-subIndex)<Math.abs(absWordIndex-subIndex)||
								preWordIndex<subIndex&&subIndex<absWordIndex){//present substance
							//System.out.println(negation+" present "+phrase.getText());
							phrase.setNegation(negation);
							
							phrase.convertValue(this.getLabel());
							phrase.getCharValue().setValue("requires "+phrase.getCharValue().getValue());
							CharacterValue charVal = phrase.getCharValue();
							//System.out.println("infer for present : ["+charVal+"]");
							charValueList.add(charVal);
						}else if (absWordIndex>-1&&absWordIndex-subIndex<0&&preWordIndex<0||
								absWordIndex>-1&&absWordIndex-subIndex<0&&Math.abs(absWordIndex-subIndex)<Math.abs(preWordIndex-subIndex)){//absent substance
							//System.out.println(negation+" absent "+phrase.getText());
							
							CharacterValue charVal = phrase.getCharValue();
							phrase.convertValue(this.getLabel());
							if(negation==null){
								charVal.setNegation("not");
								//System.out.println("infer for absent : not ["+phrase.getCharValue().getValue()+"]");
								charVal.setValue("requires "+phrase.getCharValue().getValue());
							}else{
								charVal.setNegation(null);
								charVal.setValue("requires "+phrase.getCharValue().getValue());
							}
							
							//System.out.println("infer for absent : ["+charVal+"]");
							charValueList.add(charVal);
						}
					
						//break;//if has found the value, do not find in this phrase anymore
				}//:~
			}//:~
		}//all phrase
		}
		
		
		boolean containBoth = detectBoth(charValueList);
		if(containBoth){
			for(int i=0;i<charValueList.size();){
				CharacterValue cv = charValueList.get(i);
				if(cv.getNegation()!=null) charValueList.remove(cv);
				else i++;
			}
		}
		return charValueList;
	}
	
	
	private boolean detectBoth(List<CharacterValue> charValueList) {
		boolean yes = false;
		boolean not = false;
		for(CharacterValue cv : charValueList){
			if(cv.getValue().startsWith("requ")&&cv.getNegation()==null) yes = true;
		}
		for(CharacterValue cv : charValueList){
			if(cv.getValue().startsWith("requ")&&cv.getNegation()!=null) not = true;
		}
		return yes&&not;
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
		phrase.getCharValue().setValue("requires "+phrase.getCharValue().getValue());
		CharacterValue charVal = phrase.getCharValue();
		//System.out.println("infer for present : ["+charVal+"]");
		charValueList.add(charVal);
	}
	
	
	/**
	 * find the index of the word in the word list.
	 * @return
	 */
	public int wordIndex(String word, List<TaggedWord> taggedwordsList) {
		
		int wordIndex = -1;
		for(int i=0;i<taggedwordsList.size();i++){
			TaggedWord tword = taggedwordsList.get(i);
			if(tword.word().equals(word)){
				wordIndex = i;
			}
		}
		
		//int stringIndex = sentStr.indexOf(word);
		return wordIndex;//new WordIndexes(stringIndex, wordIndex);
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
			phrase.getCharValue().setValue("requires "+phrase.getCharValue().getValue());
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
			phrase.getCharValue().setValue("requires "+phrase.getCharValue().getValue());
			CharacterValue charVal = phrase.getCharValue();
			//System.out.println("infer for absent : ["+charVal+"]");
			charValueList.add(charVal);
			
			return true;
		}
		
		
		//returnCharacterStrings.add(text);
		return false;
	}
	
	
	

	/**
	 * if the word is a negation verb, then reverse it
	 * @param verbWord
	 * @return
	 */
	private String reverseNegation(TaggedWord verbWord, String negation) {
		for(String notrequireStr: notrequirePatterns){
			if(verbWord!=null&&verbWord.word()!=null&&verbWord.word().toLowerCase().startsWith(notrequireStr)){
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
			phrase.getCharValue().setValue("requires "+phrase.getCharValue().getValue());
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
	
	/**
	 * detect single figure and figure ranges
	 * 
	 * @param taggedWords
	 * @return
	
	public List<NumericCharacterValue> detectFigures(List<TaggedWord> taggedWords) {
		
		List<NumericCharacterValue> features = new ArrayList();
		for(int i = 0;i<taggedWords.size();){
			int termId= 0;
			TaggedWord word = (TaggedWord) taggedWords.get(i);
			String figure = null;
			
			if(word.tag().equals("CD")||(word.tag().equals("JJ")&&containNumber(word.word()))||(defIsNumber(word.word()))){
				//if(word.tag().equals("CD")||defNumber(word.word())){
				termId = i;
				NumericCharacterValue fd = new NumericCharacterValue(this.getLabel());
				String unit = "";
				
				figure = word.word();
				if(!containNumber(figure)){i++;continue;}
				
				//System.out.println("it is a figure:"+figure+" "+unit);
				
				
				//if(i+1<taggedWords.size()&&(taggedWords.get(i+1).tag().equals("CD")&&(containNumber(taggedWords.get(i+1).word())||"<".equalsIgnoreCase(taggedWords.get(i+1).word()))||defIsNumber(taggedWords.get(i+1).word()))){
				while(i+1<taggedWords.size()&&(taggedWords.get(i+1).tag().equals("CD")&&(containNumber(taggedWords.get(i+1).word())||containNumSign(taggedWords.get(i+1).word()))||defIsNumber(taggedWords.get(i+1).word()))){
					figure+=taggedWords.get(i+1).word();
					i++;
				}
				if(i+1<taggedWords.size()){
					if((taggedWords.get(i+1).word().equals("°")&&taggedWords.get(i+2).word().equals("C"))
							||taggedWords.get(i+1).word().equals("degree_celsius_1")
							||taggedWords.get(i+1).word().equals("degree_celsius_7")){
						unit = "˚C";
					}else if(taggedWords.get(i+1).word().equalsIgnoreCase("˚C")){
						unit = "˚C";
					}else if(taggedWords.get(i+1).word().equalsIgnoreCase("˚")||taggedWords.get(i+1).word().equalsIgnoreCase("°")){
						unit = "˚C";
					}else if(taggedWords.get(i+1).word().equalsIgnoreCase("%")){
						unit = "%";
					}else if(taggedWords.get(i+1).word().equalsIgnoreCase("m")||taggedWords.get(i+1).word().equalsIgnoreCase("m.")){
						unit = "m";
					}else if(taggedWords.get(i+1).word().equalsIgnoreCase("g")){
						unit = "g";
					}else if(taggedWords.get(i+1).word().equalsIgnoreCase("mm")){
						unit = "mm";
					}else if(taggedWords.get(i+1).word().equalsIgnoreCase("‰")){
						unit = "‰";
					}else if(taggedWords.get(i+1).word().equalsIgnoreCase("mol")&&taggedWords.get(i+2).word().equalsIgnoreCase("%")){
						unit = "mol%";
					}else if(taggedWords.get(i+1).word().startsWith("day")) {
						unit = "days";
					}
				}
				if(!defIsNumber(figure)){
					System.err.println("it is not a figure:"+figure+" "+unit);
				}else{
					if(termId-1>=0&&(taggedWords.get(termId-1).word().equals("-")||taggedWords.get(termId-1).word().equals("−")
							||taggedWords.get(termId-1).word().equals("+")||taggedWords.get(termId-1).word().equals("<")||taggedWords.get(termId-1).word().equals(">"))){
						figure = taggedWords.get(termId-1).word()+figure;
						termId = termId-1;
					}
					
					fd.setValue(figure);
					fd.setTermBegIdx(termId);
					fd.setTermEndIdx(i);
					fd.setUnit(unit);
					features.add(fd);
				}
				
			}
			
			i++;
		}//all words traversed
		return features;
	}
 */
}

class WordIndexes{
	
	private int stringIndex;
	private int wordIndex;
	//private String substance;
	//private String status;
	
	public WordIndexes(int stringIndex, int wordIndex){
		this.stringIndex = stringIndex;
		this.wordIndex = wordIndex;
		
		//, String substance, String status
		//this.substance = substance;
		//this.status = status;
	}
}