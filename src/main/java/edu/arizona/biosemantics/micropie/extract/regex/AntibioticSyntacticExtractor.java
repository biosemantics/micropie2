package edu.arizona.biosemantics.micropie.extract.regex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.arizona.biosemantics.micropie.nlptool.StanfordParserWrapper;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

/**
 * used to extract Antibiotic sensitivity and Antibiotic resistant
 * 
 * insensitive = resistant 
 * 
 * TODO: Need to add a phrase filter to filter out the identified phrases
 * 
 * @author maojin
 *
 */
public class AntibioticSyntacticExtractor extends AbstractCharacterValueExtractor{
	private  SentenceSpliter sentSplitter;
	private  StanfordParserWrapper stfParser;
	
	private Set sensTypeSet = null;
	
	public AntibioticSyntacticExtractor(ILabel label, String characterName) {
		super(label, characterName);
	}
	
	public AntibioticSyntacticExtractor(ILabel label, String characterName, Set sensTypeSet,
			SentenceSpliter sentSplitter, 
			StanfordParserWrapper stfParser) {
		super(label, characterName);
		this.sensTypeSet = sensTypeSet;
		this.sentSplitter = sentSplitter;
		this.stfParser = stfParser;
	}
	
	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		//1, get rid of all the content in the brackets
		String cleanSent = sentSplitter.removeBrackets(sentence.getText());
		
		//2, get the dependency tree
		GrammaticalStructure deptTree = stfParser.depParse(cleanSent);
		
		//3, parse
		List<CharacterValue> valueList = parseValueFromTree(deptTree);
		
		return valueList;
	}
	
	
	/**
	 * 1, identify the given 
	 * @param deptTree
	 * @return
	 */
	public List<CharacterValue> parseValueFromTree(GrammaticalStructure deptTree) {
		//System.out.println(deptTree.typedDependenciesCollapsed());
		SemanticGraph graph = new SemanticGraph(deptTree.typedDependenciesCollapsedTree());
		
		List<CharacterValue> cvList = new ArrayList();
		
		Iterator<String> keywordPatterns = this.sensTypeSet.iterator();
		while(keywordPatterns.hasNext()){
			String pattern = keywordPatterns.next();
			IndexedWord sentJJWord = graph.getNodeByWordPattern(pattern);
			//IndexedWord sentJJWord = graph.getNodeByWordPattern("[Ss]usceptible");"[Ss]ensitive|Susceptible"
			Iterator<SemanticGraphEdge> sentJJWordOutIter = graph.outgoingEdgeIterator(sentJJWord);
			while(sentJJWordOutIter.hasNext()){
				SemanticGraphEdge sge = sentJJWordOutIter.next();
				IndexedWord dependent = sge.getDependent();
				//the first dependent on
				//if(dependent.tag().startsWith("N")&&sge.getRelation().toString().startsWith("prep")){
				if(sge.getRelation().toString().equals("prep_to")||sge.getRelation().toString().equals("prep_by")){
					//System.out.println(dependent);//this is the first one
					//System.out.println(sge.getGovernor()+" "+sge.getDependent()+" "+sge.getRelation());
					
					cvList.add(CharacterValueFactory.create(this.getLabel(), this.complementPhrase(dependent, graph)));
					
					Iterator<SemanticGraphEdge> similarWordsIter = graph.outgoingEdgeIterator(dependent);
					//similar words like the first one
					while(similarWordsIter.hasNext()){
						SemanticGraphEdge simword = similarWordsIter.next();
						//System.out.println(simword.getGovernor()+" "+simword.getDependent()+" "+simword.getRelation());
						IndexedWord simWependent = simword.getDependent();
						if(simword.getRelation().toString().startsWith("conj")){//simWependent.tag().startsWith("N")&
							
							
							if(simword.getRelation().toString().equals("conj_but")){//there should be a negation
								boolean negation = true;
								//System.out.println("a similar word found: not "+simword.getDependent()+" "+simword.getRelation());
								
								CharacterValue cv = CharacterValueFactory.create(this.getLabel(), this.complementPhrase(simWependent, graph));
								cv.setNegation("not");
								cvList.add(cv);
								
								Iterator<SemanticGraphEdge> ngsimilarWordsIter = graph.outgoingEdgeIterator(simword.getDependent());
								//similar words like the first one
								while(ngsimilarWordsIter.hasNext()){
									SemanticGraphEdge ngsimword = ngsimilarWordsIter.next();
									//System.out.println(simword.getGovernor()+" "+simword.getDependent()+" "+simword.getRelation());
									IndexedWord ngsimWependent = simword.getDependent();
									if(ngsimword.getRelation().toString().startsWith("conj")){
										CharacterValue cv1 = CharacterValueFactory.create(this.getLabel(), this.complementPhrase(ngsimWependent, graph));
										cv1.setNegation("not");
										cvList.add(cv1);
										//System.out.println("a ng similar word found: not "+ngsimword.getDependent()+" "+ngsimword.getRelation());
									}
								}
								
							}else{//not a 
								//System.out.println("a similar word found:  "+simword.getDependent()+" "+simword.getRelation());
								cvList.add( CharacterValueFactory.create(this.getLabel(), this.complementPhrase(simWependent, graph)));
							}
						}
					}
					
				}
				/*else if(dependent.word().equals("to")){
					//find those dependent upon to
					
					Iterator<SemanticGraphEdge> toWordsIter = graph.outgoingEdgeIterator(dependent);
					while(toWordsIter.hasNext()){
						SemanticGraphEdge toword = toWordsIter.next();
						System.out.println(dependent.word()+" out going:"+toword.getGovernor()+" "+toword.getDependent()+" "+toword.getRelation());
					}
				}
				*/
			}
		}
		
		
		System.out.println(cvList);
		return cvList;
	}
	
	
	
	/**
	 * complement the phrase
	 * @param coreWord
	 * @param graph
	 * @return
	 */
	public String complementPhrase(IndexedWord coreWord,SemanticGraph graph){
		List<SemanticGraphEdge> prefixDep = graph.outgoingEdgeList(coreWord);
		//similar words like the first one
		String phrase = coreWord.word();
		for(int i= prefixDep.size()-1; i>=0;i--){//
			SemanticGraphEdge simword = prefixDep.get(i);
			if(simword.getRelation().toString().equalsIgnoreCase("nn")||simword.getRelation().toString().equals("amod")){//simWependent.tag().startsWith("N")&
				phrase = simword.getDependent().word()+" "+phrase;
			}
		}
		
		return phrase;
	}
	

}