package edu.arizona.biosemantics.micropie.nlptool;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;


/**
 * Stanford NLP POS Tagger
 * 
 * @author maojin
 *
 */
public class PosTagger {
	private PorterStemmer stemmer = new PorterStemmer();
	private MaxentTagger tagger = null;
	
	/*
	public PosTagger(){
		tagger = new MaxentTagger("F:/科研工具/Stanford POS Tagger/stanford-postagger-2013-06-20/models/wsj-0-18-bidirectional-nodistsim.tagger");
	}*/
	
	@Inject
	public PosTagger(@Named("pos_model_file") String maxenttaggerFile){
		tagger = new MaxentTagger(maxenttaggerFile);
	}
	
	public MaxentTagger getTagger(){
		return this.tagger;
	}
	
	
	/**
	 * tag words list
	 * 
	 * @param tagger
	 * @param words
	 * @return
	 */
	public List<TaggedWord> tagString(String str) {
		List<List<HasWord>> sentences =  tagger.tokenizeText(new StringReader(str));
		// The tagged string
		List taggedWords = new ArrayList();
		for (List<HasWord> sentence : sentences) {
			List<TaggedWord> tSentence = tagger.tagSentence(sentence);
			taggedWords.addAll(tSentence);
		}
		return taggedWords;
	}
	
	/**
	 * tag words list
	 * 
	 * @param tagger
	 * @param words
	 * @return
	 */
	public List<TaggedWord> tagWordList(String[] words) {
		List sentence = Sentence.toWordList(words);
		// The tagged string
		List<TaggedWord> taggerWords = tagger.apply(sentence);
		return taggerWords;
	}

	/**
	 * tag a file
	 * @param tagger
	 * @param filePath
	 * @return
	 */
	public List<TaggedWord> tagFile(String filePath) {
		List<List<HasWord>> sentences;
		List taggedWords = new ArrayList();
		try {
			sentences = tagger.tokenizeText(new BufferedReader(new FileReader(filePath)));
			for (List<HasWord> sentence : sentences) {
				List<TaggedWord> tSentence = tagger.tagSentence(sentence);
				taggedWords.addAll(tSentence);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return taggedWords;
	}
	
	/**
	 * filter noun phrases by treating continuous nouns as phrase
	 * @param wordList
	 * @return
	 */
	public Map filterNounPhrases(List<TaggedWord> wordList){
		HashMap<String, Integer> nounMap = new HashMap();
		StringBuffer nounStr = new StringBuffer();
		for(int i=0;i<wordList.size();i++){
			TaggedWord word = wordList.get(i);
			if(!word.tag().startsWith("N")&&i!=0){
				//add the noun
				String np = nounStr.toString().trim();
				Integer freq = nounMap.get(np);
				freq = freq==null?0:freq;
				nounMap.put(np, freq+1);
				nounStr.delete(0, nounStr.length());
			}else{// combine continuous nous
				nounStr.append(stemmer.stem(word.word()));
				//nounStr.append(word.word());
				nounStr.append(" ");
			}
		}
		
		return nounMap;		
	}
	
	/**
	 * filter noun
	 * @param wordList
	 * @return
	 */
	public Map filterNoun(List<TaggedWord> wordList){
		HashMap<String, Integer> nounMap = new HashMap();
		
		for(int i=0;i<wordList.size();){
			TaggedWord word = wordList.get(i);
			if(!word.tag().startsWith("N")&&i!=0){
				//add the noun
				wordList.remove(i);
			}else{// combine continuous nous
				String noun = stemmer.stem(word.word());
				Integer freq = nounMap.get(noun);
				freq = freq==null?0:freq;
				nounMap.put(noun, freq+1);
				
				i++;
			}
		}
		
		return nounMap;		
	}
	
	
}
