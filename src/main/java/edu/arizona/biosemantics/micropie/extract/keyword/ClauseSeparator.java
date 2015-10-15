package edu.arizona.biosemantics.micropie.extract.keyword;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.micropie.model.Clause;
import edu.stanford.nlp.ling.TaggedWord;

/**
 * a simple method to separate clauses based on linguistic rules
 * split the sentences into large level clause
 * the assumption for this system is that:
 * the sentences will not be very complex and conventionally at most 2-3 clauses will be contained.
 * 
 * @author maojin
 *
 */
public class ClauseSeparator {
	
	private Set<String> adConjSet = new HashSet();
	{
		adConjSet.add("but");
		adConjSet.add("however");
	}
	
	
	
	private Set<String> verbSet = new HashSet();
	{
		verbSet.add("VB");
		verbSet.add("VBD");
		//verbSet.add("VBG"); // gerund or present participle
		//verbSet.add("VBN"); //past participle for this case: a good boy loved by his mom
		verbSet.add("VBP");
		verbSet.add("VBZ");
	}
	
	
	
	public List detect(List<TaggedWord> sentTaggedWords){
		List<List> clauseList = new ArrayList();
		
		//adversative conjunction: but, however
		int start = 0;
		int end = 0;
		for(int i = 0;i<sentTaggedWords.size();i++){
			TaggedWord tw = sentTaggedWords.get(i);
			String word = tw.word();
			if(adConjSet.contains(word)){//seperate
				end = i;
				clauseList.add(sentTaggedWords.subList(start, end));
				start = i+1;
			}
		}
		end = sentTaggedWords.size();
		clauseList.add(sentTaggedWords.subList(start, end));
		
		//conjunction: and
		List<List> finalClauseList = new ArrayList();
		for(List<TaggedWord> oneClause : clauseList){
			int cstart = 0;
			int cend = 0;
			for(int i = 0;i<oneClause.size();i++){
				TaggedWord tw = oneClause.get(i);
				String word = tw.word();
				//System.out.println(word+" "+detectDoubleVerbs(oneClause,i));
				if("and".equalsIgnoreCase(word)&&detectDoubleVerbs(oneClause,i)){//found an "and", and it has two poles
					cend = i;
					finalClauseList.add(oneClause.subList(cstart, cend));
					cstart = i+1;
					break;//make a simple assumption that there is only one "and"
				}
			}
			cend = oneClause.size();
			finalClauseList.add(oneClause.subList(cstart, cend));
		}
		
		return finalClauseList;
	}



	/**
	 * to found whether there are two verb
	 * @param oneClause
	 * @return
	 */
	private boolean detectDoubleVerbs(List<TaggedWord> oneClause, int interval) {
		int verbCount = 0;
		for(int i=0;i<interval;i++){
			TaggedWord tw = oneClause.get(i);
			if(verbSet.contains(tw.tag())){
				verbCount++;
				break;
			}
		}
		
		for(int i=interval+1;i<oneClause.size();i++){
			TaggedWord tw = oneClause.get(i);
			if(verbSet.contains(tw.tag())){
				verbCount++;
				break;
			}
		}
		System.out.println(oneClause+" | "+verbCount);
		if(verbCount==2||verbCount==0) return true;
		return false;
	}

}
