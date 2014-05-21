package semanticMarkup.ling.learn.knowledge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.SentenceStructure;
import semanticMarkup.ling.learn.utility.LearnerUtility;
import semanticMarkup.ling.learn.utility.StringUtility;

/**
 * Identify and annotate clauses in which a comma is used to mean "and" by the
 * compound subject
 * 
 * @author Dongye
 * 
 */
public class CommaAsAndAnnotator implements IModule {
	private LearnerUtility myLearnerUtility;
	
	public CommaAsAndAnnotator(LearnerUtility learnerUtility) {
		this.myLearnerUtility = learnerUtility;
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * comma used for 'and': seen in TreatiseH, using comma for 'and' as in
	 * "adductor , diductor scars clearly differentiated ;", which is the same
	 * as "adductor and diductor scars clearly differentiated ;". ^m*n+,m*n+ or
	 * m*n+,m*n+;$, or m,mn. Clauses dealt in commaand do not contain "and/or".
	 * andortag() deals with clauses that do.
	 * 
	 * @param dataholderHandler
	 */
	public void commaAnd(DataHolder dataholderHandler) {
		// cover m,mn

		// last + =>*
		// "(?:<[A-Z]*[NO]+[A-Z]*>[^<]+?<\/[A-Z]*[NO]+[A-Z]*>\\s*)+"
		String nPhrasePattern = "(?:<[A-Z]*[NO]+[A-Z]*>[^<]+?<\\/[A-Z]*[NO]+[A-Z]*>\\s*)+";

		// add last \\s*
		// "(?:<[A-Z]*M[A-Z]*>[^<]+?<\/[A-Z]*M[A-Z]*>\\s*)"
		String mPhrasePattern = "(?:<[A-Z]*M[A-Z]*>[^<]+?<\\/[A-Z]*M[A-Z]*>\\s*)";

		// "(?:<[A-Z]*B[A-Z]*>[,:\.;<]<\/[A-Z]*B[A-Z]*>)"
		String bPattern = "(?:<[A-Z]*B[A-Z]*>[,:.;<]<\\/[A-Z]*B[A-Z]*>)";

		String commaPattern = "<B>,</B>";

		String phrasePattern = mPhrasePattern + "\\s*" + nPhrasePattern;
		String pattern = phrasePattern + "\\s+" + commaPattern + "\\s+(?:"
				+ phrasePattern + "| |" + commaPattern + ")+";
		String pattern1 = "^(" + pattern + ")";
		String pattern2 = "(.*?)(" + pattern + ")\\s*" + bPattern + "\\$";
		// changed last * to +
		String pattern3 = "^((?:" + mPhrasePattern + "\\s+)+" + commaPattern
				+ "\\s+(?:" + mPhrasePattern + "|\\s*|" + commaPattern + ")+"
				+ mPhrasePattern + "+\\s*" + nPhrasePattern + ")";

		for (SentenceStructure sentenceItem : dataholderHandler
				.getSentenceHolder()) {
			int sentenceID = sentenceItem.getID();
			String sentence = sentenceItem.getSentence();

			String sentenceCopy = "" + sentence;
			sentenceCopy = sentenceCopy.replaceAll("></?", "");

			Matcher m1 = StringUtility.createMatcher(sentenceCopy, pattern1);
			Matcher m2 = StringUtility.createMatcher(sentenceCopy, pattern2);
			Matcher m3 = StringUtility.createMatcher(sentenceCopy, pattern3);

			// case 1
			if (m1.find()) {
				String tag = m1.group(1);
				tag = tag.replaceAll(",", "and");
				tag = tag.replaceAll("</?\\S+?>", "");
				tag = StringUtility.trimString(tag);
				// case 1.1
				if (!StringUtility.isMatchedNullSafe(tag, " and$")) {
					dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
							"", tag, "commaand[CA1]");
				}
			}
			// case 2
			else if (m2.find()) {
				String g1 = m2.group(1);
				String tag = m2.group(2);
				if (!StringUtility.isMatchedNullSafe(g1, "\\b("
						+ this.myLearnerUtility.getConstant().PREPOSITION + ")\\b")
						&& !StringUtility.isMatchedNullSafe(g1, "<N>")) {
					tag = tag.replaceAll(",", "and");
					tag = tag.replaceAll("</?\\S+?>", "");
					tag = StringUtility.trimString(tag);
					// case 2.1.1
					if (!StringUtility.isMatchedNullSafe(tag, " and$")) {
						dataholderHandler.tagSentenceWithMT(sentenceID,
								sentence, "", tag, "commaand[CA2]");
					}

				}
			}
			// case 3
			else if (m3.find()) {
				String tag = m3.group(1);
				String g1 = m3.group(1);
				// case 3.1
				if (!StringUtility.isMatchedNullSafe(g1, "\\b("
						+ this.myLearnerUtility.getConstant().PREPOSITION + ")\\b")) {
					tag = tag.replaceAll(",", "and");
					tag = tag.replaceAll("</?\\S+?>", "");
					tag = StringUtility.trimString(tag);
					// case 3.1.1
					if (!StringUtility.isMatchedNullSafe(tag, " and$")) {
						String[] tagWords = tag.split("\\s+");
						List<String> tagWordsList = new ArrayList<String>(
								Arrays.asList(tagWords));
						tag = tagWordsList.get(tagWordsList.size() - 1);
						String modifier = StringUtils.join(tagWordsList
								.subList(0, tagWordsList.size() - 1), " ");
						dataholderHandler.tagSentenceWithMT(sentenceID,
								sentence, modifier, tag, "commaand[CA3]");
					}
				}
			}
		}
	}

}
