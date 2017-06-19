package edu.arizona.biosemantics.micropie.transform;

import java.util.List;

import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.stanford.nlp.ling.TaggedWord;

public class PosTaggerTest {
	public static void main(String[] args) {
		// wsj-0-18-bidirectional-nodistsim.tagger is accordant with
		// MaxentTagger
		
		PosTagger posTagger = new PosTagger();
		String sample = "This is a sample text. I am OK.";
		String sample2 = "���ڴ������״��������̥��Ը����������Ĺ�����ȫ�����£�";
		List<TaggedWord> tagged = posTagger.tagString(sample);

		// Output the result

		System.out.println(tagged);
		/*
		String[] words = { "The", "slimy", "slug", "crawled", "over", "the", "long", ",", "green", "grass", "." };
		
		long btime = System.currentTimeMillis();
		//List<TaggedWord> taggerWords = posTagger.tagWordList(tagger, words);
		List<TaggedWord> taggerWords = posTagger.tagFile("F:/MicroPIE/stanfordNLP/testNoStruct.txt");
		Map nounphrases = posTagger.filterNounPhrases(taggerWords);
		long etime = System.currentTimeMillis();
		
		System.out.println("time cost:"+(etime-btime));
		Iterator<String> npIter = nounphrases.keySet().iterator();
		while (npIter.hasNext()) {
			String np = npIter.next();
			System.out.println( np+ " " + nounphrases.get(np));
		}

		
		nounphrases = posTagger.filterNoun(taggerWords);
		npIter = nounphrases.keySet().iterator();
		while (npIter.hasNext()) {
			String np = npIter.next();
			System.out.println( np+ " " + nounphrases.get(np));
		}*/
	}
}
