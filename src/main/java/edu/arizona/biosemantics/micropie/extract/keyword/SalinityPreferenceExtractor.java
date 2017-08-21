package edu.arizona.biosemantics.micropie.extract.keyword;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.FigureExtractUtil;
import edu.arizona.biosemantics.micropie.extract.regex.FigureExtractor;
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
	private FigureExtractUtil figureExtractor = new FigureExtractUtil();
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
		requirePatterns.add("occur");
		requirePatterns.add("grow");
		requirePatterns.add("tolerate");
		requirePatterns.add("supply");
		requirePatterns.add("grew");
		requirePatterns.add("observed");
	}
	
	/**
	 * not require pattern
	 */
	public Set<String> notrequirePatterns = new HashSet();
	{
		notrequirePatterns.add("inhibit");
		notrequirePatterns.add("prohibit");
	}
	
	public Set<String> verbPatterns = new HashSet();
	{
		verbPatterns.addAll(requirePatterns);
		verbPatterns.addAll(notrequirePatterns);
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
		//sentences
		if(sent.getSubSentTaggedWords()!=null&&sent.getSubSentTaggedWords().size()>0){
			for(List<TaggedWord> taggedwordsList : sent.getSubSentTaggedWords()){
				//System.out.println(taggedwordsList);
				List<Phrase> phList = phraseParser.extract(taggedwordsList);
				//System.out.println("phList="+phList);
				String existenceVerb = detectVerb(taggedwordsList);
				//System.out.println("existenceVerb=" +existenceVerb);
				boolean detectPresence = detectPresence(sentStr);
				//System.out.println("detectPresence=" +detectPresence);
				String negation = negationIdentifier.detectFirstNegation(taggedwordsList);
				int negationIndex = negationIdentifier.detectFirstNegationIndex(taggedwordsList);
				
				//verb+salt
				for(int cur=0;cur<phList.size();cur++){
					Phrase phrase = phList.get(cur);
					String text = phrase.getText();
					
					text = text.toLowerCase();
					//detect whether contain keywords
					for (String keywordString : keywords) {
						String substance = keywordString.substring(1,keywordString.length());
						//System.out.println(substance);
						//it's an adjective to describe the salinity preference
						//Situation 1: Adjective descriptions;
						//Sample sentences:
						//Halophilic, growing between 1.0 and 7.5 % (w/v) NaCl with optimum growth at 1–3 %.
						if(keywordString.indexOf("@")==-1&&isExist(keywordString.toLowerCase(), text.toLowerCase())){//adjectives
							//System.out.println("adjectives");
							CharacterValue newCV = new CharacterValue(this.getLabel());
							phrase.convertValue(this.getLabel());
							newCV.setValue(text);
							charValueList.add(newCV);
							break;//if has found the value;
							
						//Situation 2: Require some kinds of substances.
						//Sample sentences:
						//Require Na+ ions
						}else if(keywordString.indexOf("@")>-1&&isExist(substance.toLowerCase(), text.toLowerCase())){//compounds
	//						System.out.println("substances:"+keywordString);
							CharacterValue newCV = new CharacterValue(this.getLabel());
							phrase.convertValue(this.getLabel());
	//						if(substance.equals("Na+")){// ions
	//							if(sentStr.indexOf("Na\\+ ions")>-1) phrase.setText("Na+ ions");
	//						}else{
								if(findIons(cur, phList)){
									newCV.setValue(substance+" ions");
								}else{
									newCV.setValue(substance);
								}
	//						}
							//System.out.println("found substances:" +substance);
							//String verbWord = detectVerb(taggedwordsList); 
							newCV.setNegation(negation);
							newCV.setValueModifier(existenceVerb);
							//System.out.println("infer for present : ["+newCV+"]");
							charValueList.add(newCV);
						}
					}//:~
				}
				
				//situation 3
				List<NumericCharacterValue> nuChaValues = new ArrayList();
				if(detectPresence){//presence
					//segment sentences by "or",",","with"
					//System.out.println("subsentences:"+sub_sent.toString());
					nuChaValues.addAll(detectValuesInSent(taggedwordsList, negation));
				}
				
				//reasoningMinMax(nuChaValues);//keep only one as min max for every group of salinity, leave to PHTempNaclExtractor
				
				//reasoning Salt requirement,>0, require NaCl.
				removeConfict(charValueList, nuChaValues);
			}
		}//subsentences end
		
		Set<String> valueSet = new HashSet();
		for(int i=0;i<charValueList.size();){
			if(valueSet.contains(charValueList.get(i).toString())){
				charValueList.remove(i);
			}else{
				valueSet.add(charValueList.get(i).toString());
				i++;
			}
		}
		
		return charValueList;
	}
	
	//reasoning Salt requirement,>0, require NaCl.
	//if numerical values are found, all are required. Remove the negation
	private void removeConfict(List<CharacterValue> charValueList,
			List<NumericCharacterValue> nuChaValues) {
		if(nuChaValues.size()>0) 
			for(CharacterValue cv:charValueList) cv.setNegation(null);
	}

	
	//keep only one as min max for every group of salinity
	private void reasoningMinMax(List<NumericCharacterValue> nuChaValues) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * identify numerical values in the sub-sentence
	 * @param sub_sent
	 * @param negation
	 * @return
	 */
	private List<NumericCharacterValue> detectValuesInSent(
			List<TaggedWord> sub_sent, String negation) {
		List<NumericCharacterValue> values = figureExtractor.detectFigures(sub_sent);
		return values;
	}

	/**
	 * segment sentences by "but"
	 * @param taggedwordsList
	 * @return
	 */
	private List<List<TaggedWord>> segSentence(List<TaggedWord> taggedwordsList) {
		List<List<TaggedWord>> sub_sentences = new ArrayList();
		List<TaggedWord> tgList = new ArrayList();
		sub_sentences.add(tgList);
		for(TaggedWord tw:taggedwordsList){
			if("but".equals(tw.word())){
				tgList = new ArrayList();
				sub_sentences.add(tgList);
			}else{
				tgList.add(tw);
			}
		}
		return sub_sentences;
	}

	//whether "ions" follow this phrase
	private boolean findIons(int cur, List<Phrase> phList) {
		for(int j=cur; j>=0&&j>=cur-3;j--){
			if(phList.get(j).getText().contains("ions")) return true;
		}
		return false;
	}

	
	
	private boolean detectPresence(String sentStr) {
		return sentStr.indexOf("in the presen")>-1||sentStr.indexOf("in the absen")>-1;
	}

	private String detectVerb(List<TaggedWord> taggedwordsList) {
		String verb = null;
		for(TaggedWord tword:taggedwordsList){
			if(tword.tag().startsWith("VB")){
				Iterator<String> verbIter = this.verbPatterns.iterator();
				while(verbIter.hasNext()){
					String next = verbIter.next();
					if(tword.word().toLowerCase().contains(next)) verb = next;
				}
			}
		}
		return verb;
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
		taggerwordsList.addAll(segSentence(taggedWords));
	}
	
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