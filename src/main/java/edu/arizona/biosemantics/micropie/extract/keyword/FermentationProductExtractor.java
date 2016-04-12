package edu.arizona.biosemantics.micropie.extract.keyword;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import usp.eval.MicropieUSPExtractor;

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
 * Extract the character 6.1 Fermentation Products
 * Sample sentences:
 * 	1. Metabolism is fermentative, with glucose fermented to succinic and acetic acids, or respiratory, with glucose metabolized to CO2 and water by using oxygen as the terminal electron acceptor.  
 *	2. Major products of glucose fermentation are propionic, lactic and succinic acids.  
 *
 *	Method:
 *	1.	USP
 */
public class FermentationProductExtractor extends PhraseBasedExtractor {

	//public SemanticRoleLabeler semanticRoleLabeller = null;
	private StanfordParserWrapper stanParser;
	private SentenceSpliter sentSplitter;
	private RelationParser phraseRelationParser;
	private Label fermSubstrate = Label.c57;
	private Label fermProduct = Label.c41;
	//protected String matchMode = "W";
	private Set<String> inorganicWords;
	
	public FermentationProductExtractor(ILabel label, String character,
			Set<String> keywords, Map<String, List> subKeyword) {
		super(label, character, keywords, subKeyword);
		this.setMatchMode("W");
	}
	
	
	public FermentationProductExtractor(PosTagger postagger, Label label,
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
	
	public Set<String> getInorganicWords() {
		return inorganicWords;
	}

	public void setInorganicWords(Set<String> inorganicWords) {
		this.inorganicWords = inorganicWords;
	}

	public Set<String> fermemtSet = new HashSet();
	public Set<String> organicSet = new HashSet();
	
	{
		organicSet.add("hydroly");
		organicSet.add("assimilat");
		organicSet.add("organic");
		organicSet.add("utiliz");
		organicSet.add("degrad");
		organicSet.add("observe");
		organicSet.add("oxidize");
		organicSet.add("decompos");
		organicSet.add("positive");
		organicSet.add("negative");
		organicSet.add("attack");
		organicSet.add("respons");
		organicSet.add("activity");
		organicSet.add("dihydrolas");
	}
	
	{
		fermemtSet.add("ferment");
		fermemtSet.add("produc");
		fermemtSet.add("form");
	}
	
	
	@Override
	/**
	 * 1, identify phrases
	 * 2, identify the roles of the phrases: subject, verb_object, prep_object
	 * 3, filter out the values that are prep_object 
	 */
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		List<CharacterValue> charValueList =  new ArrayList();
		//System.out.println("sentence:"+sentence.getText());
		//Set<String> returnCharacterStrings = new HashSet<String>();
		//1, get rid of all the content in the brackets
		if(sentence==null) return null;
		if(((MultiClassifiedSentence)sentence).getPredictions()==null||
				((MultiClassifiedSentence)sentence).getPredictions().size()==0) return null;
		String cleanSent = sentSplitter.removeBrackets(sentence.getText());
		cleanSent = sentSplitter.removeSquBrackets(cleanSent);
		//System.out.println("1="+cleanSent);
		List<TaggedWord> tagList = posTagger.tagString(cleanSent);
		//somewhere a println
		List<Phrase> phraseList = phraseParser.extract(tagList);
		GrammaticalStructure deptTree = stanParser.depParse(cleanSent);
		
		//First, identify the coordinative relationships.
		List<List<Phrase>> coordTermLists = phraseRelationParser.getCoordList(phraseList,tagList);
		
		SemanticGraph semanticGraph = new SemanticGraph(deptTree.typedDependenciesCollapsedTree());
		//find the dependent relationship for all the verb phrases
		List<SemanticGraphEdge> edgeList = semanticGraph.edgeListSorted();
		List<Phrase> prepPhrases = new ArrayList();
		for(SemanticGraphEdge edge : edgeList){
			String edgeRelation = edge.getRelation().toString();
			//the first dependent on
			//if(dependent.tag().startsWith("N")&&sge.getRelation().toString().startsWith("prep")){
			//System.out.println(edge);
			if((edgeRelation.startsWith("prep_from")||edgeRelation.startsWith("prep_with"))
					&&((MultiClassifiedSentence)sentence).getPhraseList()!=null&&((MultiClassifiedSentence)sentence).getPhraseList().size()>0){
				IndexedWord dependent = edge.getDependent();
				//System.out.println(edge+" "+dependent);
				Phrase dependentPhrase = phraseRelationParser.findPhrase(dependent, phraseList);
				if(dependentPhrase==null) continue;
				prepPhrases.add(dependentPhrase);
				
				//default as a fermentation substrate
				charValueList.add(dependentPhrase.convertValue(fermSubstrate));
				
				phraseRelationParser.findConjPhrase(dependent,phraseList,semanticGraph,prepPhrases);
				//List coList = findCoordTerms(dependentPhrase,coordTermLists);
				//prepPhrases.addAll(coList);
				
			}
		}
		
		// add the from,  form ,product
		
		//detectFermentList(tagList, coordTermLists,prepPhrases);
		boolean isDefFerment  = detectFerment(tagList, phraseList,charValueList);
		//System.out.println("prepPhrases="+prepPhrases);
		
		
		//System.out.println("prepPhrases2="+prepPhrases);
		//System.out.println("charValueList="+charValueList);
		//fermentation products
		if(!isOrganicComp(cleanSent)){
			charValueList.addAll(extractFermentationProducts(phraseList, prepPhrases,cleanSent));
		}
		
		//fermentation substate
		charValueList.addAll(extractFermentationSubstrate(coordTermLists,tagList,prepPhrases,cleanSent,isDefFerment));
		//System.out.println("charValueList2="+charValueList);
		return charValueList;
	}
	
	
	
	

	/**
	 * determine for multiple term list
	 * @param tagList
	 * @param coordList
	 * @param prepPhrases
	 */
	private void detectFermentList(List<TaggedWord> tagList,
			List<List<Phrase>> coordList, List prepPhrases) {
		for(List<Phrase> phraseList:coordList){
			for(Phrase phrase : phraseList){
				int phraseIndex = phrase.getStartIndex();
				for(int index = phraseIndex-1;index>=0&&index>=phraseIndex-2;index--){
					TaggedWord tw = tagList.get(index);
					if(tw.word().equalsIgnoreCase("ferment")||tw.word().equalsIgnoreCase("ferments")) prepPhrases.add(phrase);
				}
			}
			
			
		}
		
	}
	
	
	/**
	 * determine for single words
	 * @param tagList
	 * @param phraseList
	 * @param charList
	 */
	private boolean detectFerment(List<TaggedWord> tagList,
			List<Phrase> phraseList, List charList) {
		boolean isDefFerment = false;
		for(Phrase phrase : phraseList){
			//System.out.println(phrase);
			int phraseIndex = phrase.getStartIndex();
			for(int index = phraseIndex-1;index>=0&&index>=phraseIndex-2;index--){
				TaggedWord tw = tagList.get(index);
				//if(tw.word().equalsIgnoreCase("ferment")||tw.word().equalsIgnoreCase("Ferments")||tw.word().equalsIgnoreCase("from")){
				if(tw.word().toLowerCase().startsWith("ferment")||tw.word().toLowerCase().startsWith("produc")||tw.word().toLowerCase().startsWith("from")
						//||tw.word().toLowerCase().startsWith("positive")||tw.word().toLowerCase().startsWith("negative")
						//||tw.word().toLowerCase().startsWith("utili")||tw.word().toLowerCase().startsWith("use")||tw.word().toLowerCase().startsWith("test")
						){	
					charList.add(phrase.convertValue(fermSubstrate));
					if(!isDefFerment) isDefFerment = true;
				}
			}
		}
		return isDefFerment;
	}


	/**
	 * if the phrase in prepPhrases are in the prepPhrases, then the phraseList should be the value
	 * it should not be in the inorganic list
	 * @param coordTermLists
	 * @param tagList
	 * @param prepPhrases
	 * @return
	 */
	public  List<CharacterValue> extractFermentationSubstrate(
			List<List<Phrase>> coordTermLists, List<TaggedWord> tagList,
			List<Phrase> prepPhrases,String text,boolean isDefFerment) {
		List<CharacterValue> charList = new ArrayList();
		for(List<Phrase> pList:coordTermLists){
			//has a keyword phrase
			for(Phrase pPhrase: prepPhrases){
				if(pList.size()>0&&pList.contains(pPhrase)){//if the phrase is in the prepPhrases
					for(Phrase p: pList){
						//System.out.println(p.getText()+"   "+isInorganic(p.getText()));
						if(!isInorganic(p.getText())){
							prepPhrases.add(p);
							charList.add(p.convertValue(fermSubstrate));
						}
					}
					break;
				}
			}
			
			//System.out.println("judge this:"+pList);
			//feature two: has produce, ferment and so on
			if(isDefFerment||determineFermentList2(pList,tagList,text)){
				//System.out.println("add this:"+pList);
				for(Phrase p: pList){
					//System.out.println(p.getText()+"   "+isInorganic(p.getText()));
					//not in the inorganic list
					if(!isInorganic(p.getText())){
						prepPhrases.add(p);
						charList.add(p.convertValue(fermSubstrate));
					}
				}
			}else{//not a term list at all
				break;
			}

		}
		return charList;
	}


	/**
	 * nearby has produce, ferment
	 * 
	 * ferment something
	 * ... feremented
	 * 
	 * @param pList
	 * @param tagList
	 * @return
	 */
	private boolean determineFermentList(List<Phrase> pList,
			List<TaggedWord> tagList, String text) {
		//////add a rule to differentiate from organic and inorganic
		if(text.indexOf("inorganic")>-1){
			return false;
		}
		
		for(int i=0;i<pList.size();i++){
			int phraseIndex = pList.get(i).getStartIndex();
			//ahead features
			for(int index = phraseIndex-3;index>=0&&index<=phraseIndex;index++){
				TaggedWord tw = tagList.get(index);
				if(tw.word().equalsIgnoreCase("ferment")||tw.word().equalsIgnoreCase("ferments")
						||tw.word().equalsIgnoreCase("produce")||tw.word().equalsIgnoreCase("produces")||tw.word().equalsIgnoreCase("from")
						//||tw.word().toLowerCase().startsWith("positive")||tw.word().toLowerCase().startsWith("negative")
						//||tw.word().equalsIgnoreCase("utilizes")||tw.word().equalsIgnoreCase("utilizing")||tw.word().equalsIgnoreCase("utilize")||tw.word().equalsIgnoreCase("utilized")||
						//tw.word().equalsIgnoreCase("use")
						){
					return true;
				}
			}
			
			//ahead features
			for(int index = phraseIndex+1;index<tagList.size()&&index<=phraseIndex+3;index++){
				TaggedWord tw = tagList.get(index);
				if(tw.word().equalsIgnoreCase("fermented")||tw.word().equalsIgnoreCase("produced")){//||tw.word().equalsIgnoreCase("used")
					return true;
				}
			}
		}
		return false;
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
	private boolean determineFermentList2(List<Phrase> pList, List<TaggedWord> tagList, String text) {
		boolean isTrue = false;
		for(String feature:fermemtSet){
			if(text.indexOf(feature)>-1){//no inorganic words
				isTrue =  true;
				break;
			}
		}
		if(isTrue){
			if(isInorganic(text)){//no inorganic words
				isTrue =  false;
			}
		}
		if(isTrue){
			if(isOrganicComp(text)){//not definite isOrganicComp words
				isTrue =  false;
			}
		}
		
		return isTrue;
	}
	
	

	/**
	 * 
	 * @param phraseList the phrase list of the sentence
	 * @param prepPhrases the prep-phrase list of the sentence, which should not be the fermentations
	 * @return
	 */
	public List<CharacterValue> extractFermentationProducts(List<Phrase> phraseList, List<Phrase> prepPhrases, String sentText){
		List<CharacterValue> charValueList = new ArrayList();
		
		if(isInorganic(sentText)||isOrganicComp(sentText)){
			return charValueList;
		}
		
		for(int index = 0; index< phraseList.size();index++){//deal with each phrase
			Phrase pharse = phraseList.get(index);
			//it should not be in the list of fermentation substrate
			if(prepPhrases.contains(pharse)) continue;
			String text = pharse.getText().toLowerCase();
			if(text.indexOf(" and ")>-1) {
				String[] field = text.split(" and ");
				text = field[0];
				for(int i=1;i<field.length;i++){
					Phrase p = new Phrase();
					p.setText(field[i]);
					phraseList.add(p);
				}
			}else if(text.indexOf(":")>-1) {
				String[] field = text.split(":");
				text = field[0];
				for(int i=1;i<field.length;i++){
					Phrase p = new Phrase();
					p.setText(field[i]);
					phraseList.add(p);
				}
			}
			//System.out.println(pharse+" "+prepPhrases.contains(pharse));
			if(text==null||"".equals(text)||prepPhrases.contains(pharse)) continue; 
			//detect whether contain keywords
			for (String keywordString : keywords) {
				
				boolean isId = extract(keywordString, text);
				
				if(isId){
					pharse.convertValue(fermProduct);
					CharacterValue charVal = pharse.getCharValue();
					if("W".equals(this.matchMode)){
						charVal.setValue(keywordString);//use the keywordString rather than subkeyword
					}else{
						charVal.setValue(text);
					}
					if(charVal.getNegation()==null) charValueList.add(charVal);
					//System.out.println("OUTER PHRASE HIT VALUE: ["+charVal+"]");
					//returnCharacterStrings.add(text);
					break;//if has found the value;
				}
				List<String> subKeywordList = subKeywords.get(keywordString);
				if(subKeywordList==null) continue;
				boolean isExist = false;
				for(String subKeyword : subKeywordList){
					
					isExist = extract(subKeyword, text);
					//System.out.println("subkeywords:"+subKeyword+"[ "+text+" ]"+keywordString+" "+isExist);
					if(isExist){
						//CharacterValue charVal = CharacterValueFactory.create(this.getLabel(),text);
						//pharse.setCharValue(charVal);
						pharse.convertValue(fermProduct);
						CharacterValue charVal = null;
						if("W".equals(this.matchMode)&&!"acid".equals(subKeyword)&&!"acids".equals(subKeyword)){
							charVal = pharse.getCharValue();
							charVal.setValue(keywordString);//use the keywordString rather than subkeyword
							charValueList.add(charVal);
							//System.out.println("INNER PHRASE  HIT VALUE1: ["+charVal+"]");
						}else if("acid".equalsIgnoreCase(text)||"acids".equalsIgnoreCase(text)){
							charVal = pharse.getCharValue();
							charVal.setValue(text);
							charValueList.add(charVal);
							//System.out.println("INNER PHRASE  HIT VALUE2: ["+charVal+"]");
						}
						//if(charVal!=null&&charVal.getNegation()!=null) //fermentation products should not be a negation expression
						//System.out.println("INNER PHRASE  HIT VALUE: ["+charVal+"]");
						break;
					}
				}
				if(isExist){
					break;
				}
			}
				
		}
		
		return charValueList;
	}//::::
	
	
	
	
	/**
	 * determine whether this phrase in 
	 * @param text
	 * @return
	 */
	public boolean isInorganic(String text){
		for (String keywordString : inorganicWords) {
			if(extract(keywordString, text)) return true;
		}
		return false;
	}
	
	/**
	 * determine whether this phrase in OrganicComp
	 * @param text
	 * @return
	 */
	public boolean isOrganicComp(String text){
		for (String keywordString : organicSet) {
			if(text.indexOf(keywordString)>-1){
				//System.out.println("keywordString="+keywordString);
				return true;
			}
		}
		return false;
	}
	
}
