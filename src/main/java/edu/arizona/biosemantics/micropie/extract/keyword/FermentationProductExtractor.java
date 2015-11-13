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
	private Label fermProSubstrate = Label.c41;
	//protected String matchMode = "W";
	
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
		String cleanSent = sentSplitter.removeBrackets(sentence.getText());
		List<TaggedWord> tagList = posTagger.tagString(cleanSent);
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
			if(edgeRelation.startsWith("prep")){
				IndexedWord dependent = edge.getDependent();
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
		
		//fermentation products
		charValueList.addAll(extractFermentationProducts(phraseList, prepPhrases));
		
		//fermentation substate
		charValueList.addAll(extractFermentationSubstrate(coordTermLists,tagList,prepPhrases));
		
		return charValueList;
	}

	
	/**
	 * if the phrase in prepPhrases are in the prepPhrases, then the phraseList should be the value
	 * @param coordTermLists
	 * @param tagList
	 * @param prepPhrases
	 * @return
	 */
	public  List<CharacterValue> extractFermentationSubstrate(
			List<List<Phrase>> coordTermLists, List<TaggedWord> tagList,
			List<Phrase> prepPhrases) {
		List<CharacterValue> charList = new ArrayList();
		for(List<Phrase> pList:coordTermLists){
			if(pList.size()>0&&prepPhrases.contains(pList.get(0))){//if the phrase is in the prepPhrases
				for(Phrase p: pList){
					charList.add(p.convertValue(fermSubstrate));
				}
			}
		}
		return charList;
	}


	/**
	 * 
	 * @param phraseList the phrase list of the sentence
	 * @param prepPhrases the prep-phrase list of the sentence, which should not be the fermentations
	 * @return
	 */
	public List<CharacterValue> extractFermentationProducts(List<Phrase> phraseList, List<Phrase> prepPhrases){
		List<CharacterValue> charValueList = new ArrayList();
		for(int index = 0; index< phraseList.size();index++){//deal with each phrase
			Phrase pharse = phraseList.get(index);
			String text = pharse.getText().toLowerCase();
			//System.out.println(pharse+" "+prepPhrases.contains(pharse));
			if(text==null||"".equals(text)||prepPhrases.contains(pharse)) continue; 
			//detect whether contain keywords
			for (String keywordString : keywords) {
				
				boolean isId = extract(keywordString, text);
				
				if(isId){
					pharse.convertValue(fermProSubstrate);
					CharacterValue charVal = pharse.getCharValue();
					if("W".equals(this.matchMode)) charVal.setValue(keywordString);
					charValueList.add(charVal);
					//System.out.println("OUTER PHRASE HIT VALUE: ["+charVal+"]");
					//returnCharacterStrings.add(text);
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
						pharse.convertValue(fermProSubstrate);
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
		
		return charValueList;
	}
	
	
}
