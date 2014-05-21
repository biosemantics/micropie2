package semanticMarkup.ling.learn.knowledge;

import java.util.regex.Matcher;

import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.SentenceStructure;
import semanticMarkup.ling.learn.utility.LearnerUtility;
import semanticMarkup.ling.learn.utility.StringUtility;

/**
 * Annotate any clause which share its subject with an previous clause by
 * "ditto".
 * 
 * @author Dongye
 * 
 */
public class DittoAnnotator implements IModule {
	private LearnerUtility myLearnerUtility;
	
	public DittoAnnotator(LearnerUtility learnerUtility) {
		this.myLearnerUtility = learnerUtility;
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		this.ditto(dataholderHandler);
	}
	
	public void ditto(DataHolder dataholderHandler) {
		String nPhrasePattern = "(?:<[A-Z]*[NO]+[A-Z]*>[^<]+?<\\/[A-Z]*[NO]+[A-Z]*>\\s*)+";
		String mPhrasePattern = "(?:<[A-Z]*M[A-Z]*>[^<]+?<\\/[A-Z]*M[A-Z]*>\\s*)+";

		for (SentenceStructure sentenceItem : dataholderHandler
				.getSentenceHolder()) {
			if (sentenceItem.getTag() == null) {
				int sentenceID = sentenceItem.getID();
				String sentence = sentenceItem.getSentence();
				this.dittoHelper(dataholderHandler, sentenceID, sentence,
						nPhrasePattern, mPhrasePattern);
			}
		}
	}

	public int dittoHelper(DataHolder dataholderHandler, int sentenceID,
			String sentence, String nPhrasePattern, String mPhrasePattern) {
		int res = 0;
		String sentenceCopy = "" + sentence;
		sentenceCopy = sentenceCopy.replaceAll("></?", "");
		String modifier = "";

		Matcher m2 = StringUtility.createMatcher(sentenceCopy, "(.*?)"
				+ nPhrasePattern);

		if (!StringUtility.isMatchedNullSafe(sentence, "<[NO]>")) {
			String tag = "ditto";
			dataholderHandler.tagSentenceWithMT(sentenceID, sentence, "", tag,
					"ditto-no-N");
			res = 1;
		} else if (m2.find()) {
			String head = m2.group(1);
			String pattern21 = String
					.format("\\b(%s)\\b", this.myLearnerUtility.getConstant().PREPOSITION);
			if (StringUtility.isMatchedNullSafe(head, pattern21)) {
				String tag = "ditto";
				dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
						modifier, tag, "ditto-proposition");
				res = 21;
			} else if (StringUtility.isMatchedNullSafe(head, ",<\\/B>\\s*$")) {
				String tag = "ditto";
				dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
						modifier, tag, "ditto-,-N");
				res = 22;
			}
		}

		return res;
	}

}
