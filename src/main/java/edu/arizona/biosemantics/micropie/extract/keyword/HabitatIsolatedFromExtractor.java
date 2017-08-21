package edu.arizona.biosemantics.micropie.extract.keyword;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.extract.context.PhraseRelationGraph;
import edu.arizona.biosemantics.micropie.extract.context.RelationParser;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.model.PhraseRelation;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.arizona.biosemantics.micropie.nlptool.StanfordParserWrapper;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;


/**
 * extract habitat isolated from character,
 * 1, identify noun phrases and verb phrases
 * 2, find the right verb
 * 3, find the dependencies between verb phrases and noun phrases
 * 4, find the noun phrases that are connected via prep* dependencies 
 * @author maojin
 */
public class HabitatIsolatedFromExtractor extends PhraseBasedExtractor{
	private StanfordParserWrapper stanParser;
	private RelationParser relationParser;
	private SentenceSpliter sentSplitter;
	
	public HabitatIsolatedFromExtractor(PosTagger posTagger,ILabel label, String character,
			Set<String> keywords, Map<String, List> subKeyword) {
		super(posTagger,label, character, keywords, subKeyword);
	}
	
	public HabitatIsolatedFromExtractor(ILabel label, String character,
			Set<String> keywords, Map<String, List> subKeyword) {
		super(label, character, keywords, subKeyword);
	}
	
	
	//need to be initialized after created and before applied to extract values
	public void setStanParser(StanfordParserWrapper stanParser) {
		this.stanParser = stanParser;
	}

	public void setRelationParser(RelationParser relationParser) {
		this.relationParser = relationParser;
	}
	
	public void setSentSplitter(SentenceSpliter sentSplitter) {
		this.sentSplitter = sentSplitter;
	}

	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		List<CharacterValue> characterValueList = new ArrayList();
		//situation 1: Habitat: sea ice and seawater, algae, marine sediments, soil and fresh water.
		extractByComma(characterValueList, sentence.getText());
		if(characterValueList.size()>0) return characterValueList;
		
		//situation 2: The habitat is a marshy soil in the Sippenauer Moor close to Kelheim/Bavaria. 
		extractByExplicitStatement(characterValueList, sentence.getText());
		if(characterValueList.size()>0) return characterValueList;
		
		//noun phrases
		String cleanSent = sentence.getText();//sentSplitter.removeBrackets(sentence.getText());
		Tree phraseTree = stanParser.parsePhraseTree(cleanSent);
		List<Phrase> nounPhrases = phraseParser.extractNounPharse(phraseTree);
		
		//System.out.println("nounPhrases="+nounPhrases);
		List<Phrase> verbPhrases = phraseParser.extractVerbPharse(phraseTree);
		//System.out.println("verbPhrases="+verbPhrases);
		
		//filter verb phrases
		filterVerbPhraseList(verbPhrases);
		//System.out.println("filtered verbPhrases="+verbPhrases);
		GrammaticalStructure deptTree = stanParser.depParse(cleanSent);
		PhraseRelationGraph verbRelationGraph = relationParser.parseVerbDependencyRelation(verbPhrases, nounPhrases, deptTree);
		//System.out.println("verbRelationGraph edges ="+verbRelationGraph.edgeSet().size());
		//filter the edges by edge type constraints
		
		Set<PhraseRelation> edges = verbRelationGraph.edgeSet();
		Iterator<PhraseRelation> edgeIter = edges.iterator();
		while(edgeIter.hasNext()){
			PhraseRelation pr = edgeIter.next();
			
			String type = pr.getType();
			//System.out.println(pr+" the edge type is "+type);
			if(type.startsWith("prep")){//this is the right type
				Phrase source = pr.getSource();
				Phrase target = pr.getTarget();
				CharacterValue cv = null;
				if("N".equals(source.getType())){
					cv = source.convertValue(this.getLabel());
				}else{
					cv = target.convertValue(this.getLabel());
				}
				if(cv!=null){
					normalize(cv);
					characterValueList.add(cv);
					continue;
				}
				//System.out.println("HIT "+cv);
			}
			
			//the verb is within the noun phrase
			Phrase withinPhrase = verbIsInTheNoun(pr);
			if(withinPhrase!=null){
				CharacterValue cv = withinPhrase.convertValue(this.getLabel());
				normalize(cv);
				characterValueList.add(cv);
				//System.out.println("HIT "+cv);
			}
		}
		
		return characterValueList;
	}
 
	public void normalize(CharacterValue cv){
		String value = cv.getValue();
		value = value.replace("-LRB- ", "(").replace(" -RRB-", ")");
		cv.setValue(value);
	}

	//situation 1: Habitat: sea ice and seawater, algae, marine sediments, soil and fresh water.
	private void extractByComma(List<CharacterValue> characterValueList,
			String text) {
		String lowertext = text.toLowerCase().trim();
		if(lowertext.indexOf("habitat:")>-1||lowertext.indexOf("habitat :")>-1){
			text = text.substring(text.indexOf(":")+1,text.length()-1).trim();
			if(text.endsWith(".")) text = text.substring(0,text.length()-2);
			CharacterValue cv = new CharacterValue(this.getLabel(), text);
			characterValueList.add(cv);
		}else if((lowertext.indexOf("source:")>-1||lowertext.indexOf("source :")>-1)&&lowertext.indexOf("isolated")==-1){
			text = text.substring(text.indexOf(":")+1,text.length()-1).trim();
			if(text.endsWith(".")) text = text.substring(0,text.length()-2);
			CharacterValue cv = new CharacterValue(this.getLabel(), text);
			characterValueList.add(cv);
		}
		
	}
	
	//The habitat is the guinea pig vagina. 
	private void extractByExplicitStatement(List<CharacterValue> characterValueList,
			String text) {
		String lowertext = text.toLowerCase().trim();
		if(lowertext.indexOf("habitat is")>-1){
			text = text.substring(text.indexOf("habitat is")+10,text.length()-1).trim();
			if(text.endsWith(".")) text = text.substring(0,text.length()-2);
			CharacterValue cv = new CharacterValue(this.getLabel(), text);
			characterValueList.add(cv);
		}
		
	}
	
	
	
	/**
	 * 
	 * @param pr
	 * @return
	 */
	private Phrase verbIsInTheNoun(PhraseRelation pr) {
		Phrase source = pr.getSource();
		Phrase target = pr.getTarget();
		Phrase verb = null;
		Phrase noun = null;
		if("N".equals(source.getType())){
			noun = source;
			verb = target;
		}else{
			noun = target;
			verb = source;
		}
		
		String verbText = verb.getText();
		String nounText = noun.getText();
		
		if(nounText.indexOf(verbText)>-1){
			nounText = nounText.substring(nounText.indexOf(verbText)+verbText.length(),nounText.length());
			noun.setText(nounText);
			return noun;
		}
		return null;
	}

	/**
	 * 
	 * @param verbPhrases
	 */
	public void filterVerbPhraseList(List<Phrase> verbPhrases) {
		for(int i=0;i<verbPhrases.size();){
			Phrase verbPhrase = verbPhrases.get(i);
			String text = verbPhrase.getText();
			boolean isId = false;
			//detect whether contain keywords
			for (String keywordString : keywords) {
				isId = extract(keywordString, text.toLowerCase());
				if(isId) break;
			}
			
			if(isId){
				i++;
			}else{
				verbPhrases.remove(verbPhrase);
			}
		}
	}

}
