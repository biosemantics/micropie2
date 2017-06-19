package edu.arizona.biosemantics.micropie.extract.keyword;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;





import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.extract.context.RelationParser;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SemanticRoleLabeler;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.arizona.biosemantics.micropie.nlptool.StanfordParserWrapper;
import edu.arizona.biosemantics.micropie.nlptool.TermRole;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalStructure;

/**
 * Extract the character 6.4 other metobolic product
 * Sample sentences:
 *
 */
public class OtherMetobolicProductExtractor extends PhraseBasedExtractor {

	private StanfordParserWrapper stanParser;
	private SentenceSpliter sentSplitter;
	private RelationParser phraseRelationParser;
	private Set<String> fermentationProducts = new HashSet();
	private Set<String> methProducts = new HashSet();
	
	public OtherMetobolicProductExtractor(ILabel label, String character,
			Set<String> keywords, Map<String, List> subKeyword) {
		super(label, character, keywords, subKeyword);
		this.setMatchMode("W");
	}
	
	
	public OtherMetobolicProductExtractor(PosTagger postagger, Label label,
			String character,Set<String> keywords, Map<String, List> subKeyword) {
		super(postagger, label, character, keywords, subKeyword);
	}

	//need to be initialized after created and before applied to extract values
	public void setStanParser(StanfordParserWrapper stanParser) {
		this.stanParser = stanParser;
	}

	public void setRelationParser(RelationParser relationParser) {
		this.phraseRelationParser = relationParser;
	}
	
	public void setSentSplitter(SentenceSpliter sentSplitter) {
		this.sentSplitter = sentSplitter;
	}
	

	public Set<String> fermemtSet = new HashSet();
	{
		fermemtSet.add("ferment");
		fermemtSet.add("produce");
		fermemtSet.add("forme");
		fermemtSet.add("forms");
	}
	
	public void setFermProductTerms(Set<String> productTerms){
		this.fermentationProducts.addAll(productTerms);
	}
	
	public void setMethProdcutTerm(Set<String> productTerms){
		this.methProducts.addAll(productTerms);
	}
	
	
	@Override
	/**
	 * 1, identify phrases
	 * 2, identify the roles of the phrases: subject, verb_object, prep_object
	 * 3, filter out the values that are prep_object 
	 */
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		List<CharacterValue> charValueList =  new ArrayList();
		//1, get rid of all the content in the brackets
		if(sentence==null) return null;
		if(((MultiClassifiedSentence)sentence).getPredictions()==null||
				((MultiClassifiedSentence)sentence).getPredictions().size()==0) return null;
		
		//1, detect fermentation trigger words
		if(containsTriggerWords(sentence.getText().toLowerCase())){
			String cleanSent = sentSplitter.removeBrackets(sentence.getText());
			cleanSent = sentSplitter.removeSquBrackets(cleanSent);
			//System.out.println("1="+cleanSent);
			List<TaggedWord> tagList = posTagger.tagString(cleanSent);
			List<Phrase> phraseList = phraseParser.extract(tagList);
			//System.out.println("case 1:"+tagList);
			
			String negation = negationIdentifier.detectFirstNegation(tagList);
			int negationIndex = negationIdentifier.detectFirstNegationIndex(tagList);
			//System.out.println("negation:"+negation);
			//First, identify the coordinative relationships.
			List<List<Phrase>> coordTermLists = phraseRelationParser.getCoordList(phraseList,tagList);
			
			int triggerWordIndex = triggerWordIndex(tagList);
			if(triggerWordIndex>-1){
				TaggedWord triggerWord = tagList.get(triggerWordIndex);
				List<CharacterValue> valueList = null;
				if("VBN".equals(triggerWord.tag())){// is produced from .... find from previous Noun phrase
					//System.out.println("case VBN: triggerword:"+triggerWordIndex+"-"+triggerWord.word());
					valueList = parsePastParticiple(phraseList, triggerWordIndex, coordTermLists);
				}else{//VBD,VBG, VBP, VBZ
					//find from following NN, but before PP
					//System.out.println("case other:triggerword:"+triggerWordIndex+"-"+triggerWord.word());
					valueList = parsePresent(phraseList, triggerWordIndex, coordTermLists);
				}
				if(negation!=null&&!"".equals(negation)&&(negationIndex<triggerWordIndex||negationIndex-triggerWordIndex<3)){
					for(CharacterValue cv:valueList){
						cv.setNegation(negation);
					}
				}
				
				charValueList.addAll(valueList);
			}else{
				extractedByMatching((MultiClassifiedSentence)sentence,charValueList);
			}
		}else{//2, using keyword matching for normal keywords
			extractedByMatching((MultiClassifiedSentence)sentence,charValueList);
		}
		//System.out.println("before filter:"+charValueList);
		filterValues(charValueList);
		return charValueList;
	}
	

	private void filterValues(List<CharacterValue> charValueList) {
		for(int i=0;i<charValueList.size();i++){
			CharacterValue cv  = charValueList.get(i);
			for(String fterm:fermentationProducts){
				if(cv.getValue().equalsIgnoreCase(fterm)){
					cv.setCharacter(Label.c41);
				}
			}
			for(String fterm:methProducts){
				if(cv.getValue().equalsIgnoreCase(fterm)){
					cv.setCharacter(Label.c43);
				}
			}
		}
	}


	/**
	 * the phrase list should  be ahead of the triggerWord directly
	 * @param phraseList
	 * @param triggerWordIndex
	 * @param charValueList
	 */
	public List  parsePastParticiple(List<Phrase> phraseList, int triggerWordIndex, 
			List<List<Phrase>> coordTermLists) {
		List<CharacterValue> charValueList =  new ArrayList();
		int nearest = 100;
		Phrase np = null;
		for(int pindex=0;pindex<phraseList.size();pindex++){
			Phrase phrase = phraseList.get(pindex);
			//System.out.println(phrase.getText()+" " +phrase.getStartIndex()+"-"+phrase.getEndIndex());
			int distance = triggerWordIndex - phrase.getEndIndex();
			if(distance>0&&nearest>distance){
				np = phrase;
				nearest = distance;
			}
		}
		
		if(np!=null){//at least it should be a candidate
			//TO-DO: further filter
			//System.out.println("find phrase:"+np);
			List<Phrase> valueList = null;
			if(coordTermLists!=null&&coordTermLists.size()>0) valueList = findFromCoordTermList(coordTermLists, np);
			if(valueList!=null){
				//System.out.println("find coord term list:"+valueList);
				for(Phrase p:valueList){
					charValueList.add(p.convertValue(this.getLabel()));
				}
			}else{
				charValueList.add(np.convertValue(this.getLabel()));
			}
			
		}
		return charValueList;
	}

	/**
	 * find whether any coordinate term list contains this phrase
	 * @param coordTermLists
	 * @param np
	 * @return
	 */
	public List findFromCoordTermList(List<List<Phrase>> coordTermLists,
			Phrase np) {
		for(List<Phrase> phraseList : coordTermLists){
			for(Phrase p:phraseList) if(np.equals(p)) return phraseList;
		}
		return null;
	}


	public List parsePresent(List<Phrase> phraseList, int triggerWordIndex, 
			List<List<Phrase>> coordTermLists) {
		List<CharacterValue> charValueList = new ArrayList();
		int nearest = -100;
		Phrase np = null;
		for(int pindex=0;pindex<phraseList.size();pindex++){
			Phrase phrase = phraseList.get(pindex);
			//System.out.println(phrase.getText()+" " +phrase.getStartIndex()+"-"+phrase.getEndIndex());
			int distance = triggerWordIndex - phrase.getEndIndex();
			if(distance<0&&nearest<distance){
				np = phrase;
				nearest = distance;
			}
		}
		
		if(np!=null){//at least it should be a candidate
			//TO-DO: further filter
			List<Phrase> valueList = null;
			//System.out.println("nearest is "+np);
			if(coordTermLists!=null&&coordTermLists.size()>0) valueList = findFromCoordTermList(coordTermLists, np);
			if(valueList!=null){
				for(Phrase p:valueList){
					charValueList.add(p.convertValue(this.getLabel()));
				}
			}else{
				charValueList.add(np.convertValue(this.getLabel()));
			}
		}
		return charValueList;
	}
	
	
	/**
	 * using keyword matching method
	 * @param sent
	 * @param charValueList
	 */
	public void extractedByMatching(MultiClassifiedSentence sent, List<CharacterValue> charValueList){
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
		}
	}

	
	/**
	 * determine for multiple term list
	 * @param tagList
	 * @param coordList
	 * @param prepPhrases
	 */
	private int triggerWordIndex(List<TaggedWord> tagList) {
		for(int index = 0;index < tagList.size(); index++){
			TaggedWord tw = tagList.get(index);
			if(this.containsTriggerWords(tw.word().toLowerCase()))
				return index;
		}
		return -1;
	}
	
	/**
	 * nearby has produce, ferment
	 * JUST USE simple clues
	 * 
	 * ferment something
	 * ... feremented
	 * 
	 * @param pList
	 * @param tagList
	 * @return
	 */
	private boolean containsTriggerWords(String text) {
		boolean isTrue = false;
		for(String feature:fermemtSet){
			if(text.indexOf(feature)>-1){//no inorganic words
				isTrue =  true;
				break;
			}
		}
		
		return isTrue;
	}
	
	
}
