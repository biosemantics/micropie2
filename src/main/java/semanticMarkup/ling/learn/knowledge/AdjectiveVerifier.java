package semanticMarkup.ling.learn.knowledge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import semanticMarkup.ling.learn.Configuration;
import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.SentenceStructure;
import semanticMarkup.ling.learn.utility.LearnerUtility;
import semanticMarkup.ling.learn.utility.StringUtility;

/**
 * Correct markups that used an adj as a singular, e.g lateral, adult, juvenile
 * 
 * @author Dongye
 */
public class AdjectiveVerifier implements IModule {
	private LearnerUtility myLearnerUtility;

	public AdjectiveVerifier(LearnerUtility learnerUtility) {
		this.myLearnerUtility = learnerUtility;
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		this.adjectivesVerification(dataholderHandler);
	}
	
	/**
	 * correct markups that used an adj as an s, e.g lateral, adult, juvenile
	 */
	public void adjectivesVerification(DataHolder dataholderHandler) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.adjectivesVerification");

		String pattern = "^<N>([a-z]+)</N> ([^N,;.]+ <N>[a-z]+</N>)";
		Iterator<SentenceStructure> iter = dataholderHandler
				.getSentenceHolderIterator();
		while (iter.hasNext()) {
			SentenceStructure sentenceItem = iter.next();
			String sentence = sentenceItem.getSentence();
			
			if (sentence != null) {
				Pattern p = Pattern.compile(pattern);
				Matcher m = p.matcher(sentence);
				if (m.find()) {
					String part1 = m.group(1);
					String part2 = m.group(2);
					myLogger.trace(String.format("Sentence %s\n"
							+ "\tSentence: %s\n" + "\tPart1: %s\n"
							+ "\tPart2: %s", sentenceItem.getID(),
							sentenceItem.getSentence(), part1, part2));
					boolean condition1 = this.isSentenceTag(dataholderHandler,
							part2);
					boolean condition2 = StringUtils.equals(this
							.myLearnerUtility.getWordFormUtility()
							.getNumber(part1), "p");

					if (condition1 && condition2) {
						String wrongWord = part1;
						myLogger.trace("\tWrong: " + wrongWord);
						// if (StringUtility.isMatchedNullSafe(wrongWord,
						// "\\w")) {
						if (StringUtility.isMatchedNullSafe(wrongWord, "\\w")) {
							this.noun2Modifier(dataholderHandler, wrongWord);
							Set<String> words = dataholderHandler
									.getWordsFromUnknownWord(null, false,
											String.format("^%s$", wrongWord),
											true);
							for (String word : words) {
								this.noun2Modifier(dataholderHandler, word);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Check if a word is (part of) the tag of any sentence
	 * 
	 * @param dataholderHandler
	 *            DataHolder handler
	 * @param raw
	 *            word to check
	 * @return true if it is, false otherwise
	 */
	public boolean isSentenceTag(DataHolder dataholderHandler, String raw) {
		boolean result = false;
		result = dataholderHandler.isExistSentence(false,
				String.format("^%s.*$", raw));

		return result;
	}

	/**
	 * change the POS tag of a word from noun to modifier
	 * 
	 * @param dataholderHandler
	 *            dataholder handler
	 * 
	 * @param word
	 *            the word to change
	 * @return true if any updates has been made, false otherwise
	 */
	public boolean noun2Modifier(DataHolder dataholderHandler, String word) {
		boolean isUpdated = false;

		ArrayList<String> deletedPOSs = new ArrayList<String>();
		deletedPOSs.add("s");
		deletedPOSs.add("p");
		deletedPOSs.add("n");

		for (String POS : deletedPOSs) {
			dataholderHandler.deleteWordPOS(true, word, true, POS);
		}
		dataholderHandler.updateDataHolder(word, "m", "", "modifiers", 1);

		String oldPattern = String.format("(^%s$|^.* %s$)", word, word);
		dataholderHandler.updateSentenceTag(oldPattern, null);

		return isUpdated;
	}

}
