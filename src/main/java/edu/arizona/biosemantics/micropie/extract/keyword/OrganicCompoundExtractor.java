package edu.arizona.biosemantics.micropie.extract.keyword;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.context.RelationParser;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.arizona.biosemantics.micropie.nlptool.StanfordParserWrapper;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.GrammaticalStructure;



/**
 * identify the list or the term, and them find the features in the sentence
 * @author maojin
 *
 */
public class OrganicCompoundExtractor extends PhraseBasedExtractor{
	
	public OrganicCompoundExtractor(ILabel label, String character,
			Set<String> keywords, Map<String, List> subKeyword) {
		super(label, character, keywords, subKeyword);
	}


	//public SemanticRoleLabeler semanticRoleLabeller = null;
	private StanfordParserWrapper stanParser;
	private SentenceSpliter sentSplitter;
	private RelationParser phraseRelationParser;
	
	public void setStanParser(StanfordParserWrapper stanParser) {
		this.stanParser = stanParser;
	}

	public void setSentSplitter(SentenceSpliter sentSplitter) {
		this.sentSplitter = sentSplitter;
	}

	public void setPhraseRelationParser(RelationParser phraseRelationParser) {
		this.phraseRelationParser = phraseRelationParser;
	}

	public Set<String> featureSet = new HashSet();
	{
		featureSet.add("hydroly");
		featureSet.add("use");
		featureSet.add("assimilat");
		featureSet.add("organic");
		featureSet.add("utiliz");
		featureSet.add("degrad");
		featureSet.add("observe");
		featureSet.add("oxidize");
		featureSet.add("decompos");
		featureSet.add("grow");
		featureSet.add("positive");
		featureSet.add("negative");
		featureSet.add("attack");
		featureSet.add("respons");
		featureSet.add("grew");
		featureSet.add("activity");
		featureSet.add("dihydrolas");
	}
	
	
	public Set<String> excluSet = new HashSet();
	{
		excluSet.add("ferment");
		excluSet.add("produc");
		excluSet.add("form");
	}
	
	@Override
	/**
	 * 1, identify phrases
	 * 2, identify the roles of the phrases: subject, verb_object, prep_object
	 * 3, filter out the values that are prep_object 
	 */
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		List<CharacterValue> charValueList =  new ArrayList();
		//System.out.println("keywords:"+keywords.size());
		//System.out.println("sentence:"+sentence.getText());
		//Set<String> returnCharacterStrings = new HashSet<String>();
		//1, get rid of all the content in the brackets
		if(sentence==null) return null;
		String cleanSent = sentSplitter.removeBrackets(sentence.getText());
		cleanSent = sentSplitter.removeSquBrackets(cleanSent);
		//System.out.println("1="+cleanSent);
		List<TaggedWord> tagList = posTagger.tagString(cleanSent);
		//somewhere a println
		List<Phrase> phraseList = phraseParser.extract(tagList);
		phraseParser.applyNegative(tagList, phraseList);
		
		//First, identify the coordinative relationships.
		List<List<Phrase>> coordTermLists = phraseRelationParser.getCoordList(phraseList,tagList);
		//System.out.println("coordTermLists:"+coordTermLists);
		List<CharacterValue> valueList = new ArrayList();
		//find features 
		//System.out.println("sentence:"+cleanSent);
		//System.out.println("useOrHydFeature(cleanSent):"+useOrHydFeature(cleanSent));
		//System.out.println("useFermentation(cleanSent):"+useFermentation(cleanSent));
		if(useOrHydFeature(cleanSent.toLowerCase())&&!useFermentation(cleanSent.toLowerCase())){
			//phrase List
			for(List<Phrase> phList : coordTermLists){
				//System.out.println("phList:"+phList);
				boolean isInList = false;
				for(Phrase p:phList){
					//detect whether contain keywords
					for (String keywordString : keywords) {
						isInList = extract(keywordString, p.getText());
						if(isInList) break;
					}
				}
				//System.out.println("isInList:"+isInList);
				if(isInList){
					for(Phrase p:phList){
						Label label = p.getNegation()!=null?Label.c54:Label.c53;
						valueList.add(p.convertValue(label));
						phraseList.remove(p);//remove from phrase List
					}
				}
			}
			
			//System.out.println(phraseList.size());
			//not in phraseList
			for(Phrase p:phraseList){
				String text = p.getText().toLowerCase();
				//detect whether contain keywords
				for (String keywordString : keywords) {
					if(extract(keywordString.toLowerCase(), text)){
						Label label = p.getNegation()!=null?Label.c54:Label.c53;
						valueList.add(p.convertValue(label));
					}
					
					List<String> subKeywordList = subKeywords.get(keywordString);
					if(subKeywordList==null) continue;
					for(String subKeyword : subKeywordList){
						if(extract(subKeyword.toLowerCase(), text)){
							Label label = p.getNegation()!=null?Label.c54:Label.c53;
							valueList.add(p.convertValue(label));
						}
					}
				}
				
			}
		}
		//System.out.println(valueList);
		return valueList;
	}

	private boolean useFermentation(String cleanSent) {
		for(String feature:excluSet){
			if(cleanSent.indexOf(feature)>-1) return true;
		}
		return false;
	}

	/**
	 * hydroly,used,assimilat,organic,utiliz,degrad,observe
	 * @param cleanSent
	 * @return
	 */
	public boolean useOrHydFeature(String cleanSent) {
		for(String feature:featureSet){
			if(cleanSent.indexOf(feature)>-1) return true;
		}
		return false;
	}
	
	
	
}
