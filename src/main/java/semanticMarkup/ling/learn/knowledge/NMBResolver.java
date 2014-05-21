package semanticMarkup.ling.learn.knowledge;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.SentenceStructure;
import semanticMarkup.ling.learn.dataholder.WordPOSKey;
import semanticMarkup.ling.learn.dataholder.WordPOSValue;
import semanticMarkup.ling.learn.utility.StringUtility;

/**
 * Deal with words that plays N, and B roles
 * 
 * @author Dongye
 * 
 */
public class NMBResolver implements IModule {

	public NMBResolver() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		// TODO Auto-generated method stub

	}
	
	public void resolveNMB(DataHolder dataholderHandler) {
		Set<String> tags = dataholderHandler.getSentenceTags();
		Iterator<Entry<WordPOSKey, WordPOSValue>> wordPOSIter = dataholderHandler
				.getWordPOSHolderIterator();

		// get words
		Set<String> words = new HashSet<String>();
		while (wordPOSIter.hasNext()) {
			Entry<WordPOSKey, WordPOSValue> wordPOSEntry = wordPOSIter.next();
			if (StringUtils.equals(wordPOSEntry.getKey().getPOS(), "b")) {
				String word = wordPOSEntry.getKey().getWord();
				boolean case1 = dataholderHandler.getWordPOSHolder()
						.containsKey(new WordPOSKey(word, "s"));
				boolean case2 = tags.contains(word);
				if (case1 || case2) {
					words.add(word);
				}
			}
		}

		// update wordPOS holder and / or sentence holder
		Iterator<String> wordIter = words.iterator();
		while (wordIter.hasNext()) {
			String word = wordIter.next();

			if (dataholderHandler.getModifierHolder().containsKey(word)) {
				// remove N role
				dataholderHandler.getWordPOSHolder().remove(
						new WordPOSKey(word, "s"));

				// reset sentence tags
				Iterator<SentenceStructure> sentenceIter = dataholderHandler
						.getSentenceHolderIterator();
				while (sentenceIter.hasNext()) {
					SentenceStructure sentenceItem = sentenceIter.next();
					String tag = sentenceItem.getSentence();
					boolean case1 = StringUtils.equals(tag, word);
					boolean case2 = StringUtility.isMatchedNullSafe(tag, " "
							+ word);
					if (case1 || case2) {
						sentenceItem.setModifier("");
						sentenceItem.setTag(null);
					}
				}

				dataholderHandler.getBMSWords().add(word);
			}
		}

		// retag clauses with <N><M><B> tags
		Iterator<SentenceStructure> sentenceIter = dataholderHandler
				.getSentenceHolderIterator();
		while (sentenceIter.hasNext()) {
			SentenceStructure sentenceItem = sentenceIter.next();
			String sentence = sentenceItem.getSentence();
			sentence = sentence.replaceAll("<[ON]><M><B>", "<M><B>");
			sentence = sentence.replaceAll("</B></M></[ON]>", "</B></M>");
			sentenceItem.setSentence(sentence);
		}

	}

}
