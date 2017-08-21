package edu.arizona.biosemantics.micropie.nlptool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.ling.TaggedWord;


/**
 * some simple tools for clause identifier
 * each clause should contain a verb
 * 
 * separate different clauses
 * 1, identify and/or/but/while/however
 * 2, whether there are many verbs: VB, conj+VBP
 * 
 * multiple positive/negative
 * 
 * Test sentences: 
 * 1,Catalase positive, oxidase negative, glucose metabolized fermentatively. 
 * 2,Methyl red test is positive and Voges–Proskauer test is negative
 * 3,Catalase production is positive and oxidase production is negative. 
 * 
 * @author maojin
 *
 */
public class ClauseIdentifier {
	
	private static Set separatorSet = new HashSet();
	{
		//separatorSet.add("and"); and is not often used to seperate clauses in the development sentences
		//separatorSet.add("or");
		separatorSet.add("but");
		separatorSet.add("while");
		separatorSet.add("whereas");
		separatorSet.add("though");
		separatorSet.add("although");
		separatorSet.add("however");
		separatorSet.add("positive");
		separatorSet.add("negative");
	}
	
	
	/**
	 * create candidate clause segmented by the seperator
	 * @param taggedwordsList
	 * @return
	 */
	public List<List<TaggedWord>> segWithSeperator(List<TaggedWord> taggedwordsList) {
		List candidateClause = new ArrayList();
		
		List<TaggedWord> currentClause = new ArrayList();
		candidateClause.add(currentClause);
		//1, identify and/or/but/while/however
		for(int index=0;index<taggedwordsList.size();index++){
			TaggedWord tw = taggedwordsList.get(index);
			currentClause.add(tw);
			if(separatorSet.contains(tw.word().toLowerCase())){//or negation words contain
				currentClause = new ArrayList();
				candidateClause.add(currentClause);
			}
		}
		return candidateClause;
	}
	
	
	/**
	 * each clause should have verb or positive/negative, otherwise be combined into next
	 * 
	 * not apply this one
	 * 
	 * @return
	 */
	public List<List<TaggedWord>> combineClauses(List<List<TaggedWord>> clauses){
		boolean[] clauseTag = new boolean[clauses.size()];
		for(int i=0;i<clauses.size();i++){
			List<TaggedWord> clause = clauses.get(i);
			clauseTag[i] = isClause(clause);
		}
		
		for(int i=0,j=0;i<clauses.size();i++,j++){
			if(!clauseTag[i]){//merge the next one
				if(j+1<clauses.size()){
					clauses.get(j).addAll(clauses.get(j+1));
					//remove next
					clauses.remove(j+1);
					j--;//to keep in the right place
				}else{//the last one should be merge, merge to former one
					if(j>0){
						clauses.get(j-1).addAll(clauses.get(j));
						clauses.remove(j);
					}
					
				}
			}else{//not merge
				
			}
		}
		return clauses;
	}


	private boolean isClause(List<TaggedWord> clause) {
		for(TaggedWord tw:clause){
			if(tw.word().toLowerCase().equals("positive")
					||tw.word().toLowerCase().equals("negative")
					||tw.tag().equals("VBD")||tw.tag().equals("VBP")||tw.tag().equals("VBZ")){
				return true;
			}
		}
		return false;
	}
	
	
	
}