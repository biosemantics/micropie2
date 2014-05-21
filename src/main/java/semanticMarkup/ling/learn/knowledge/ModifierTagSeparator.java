package semanticMarkup.ling.learn.knowledge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.SentenceStructure;
import semanticMarkup.ling.learn.utility.LearnerUtility;
import semanticMarkup.ling.learn.utility.StringUtility;

/**
 * For those sentences whose tag has a space between words, separate modifier and update the tag for different cases
 * 
 * @author Dongye
 *
 */
public class ModifierTagSeparator implements IModule {
	private LearnerUtility myLearnerUtility;
	
	public ModifierTagSeparator(LearnerUtility learnerUtility) {
		this.myLearnerUtility = learnerUtility;
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		// TODO Auto-generated method stub

	}
	
	public void separateModifierTag(DataHolder dataholderHandler) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.separateModifierTag");

		List<SentenceStructure> sentences = dataholderHandler
				.getSentencesByTagPattern("^.* .*$");

		for (SentenceStructure sentenceItem : sentences) {
			int sentenceID = sentenceItem.getID();
			String sentence = sentenceItem.getSentence();
			String tag = sentenceItem.getTag();
			myLogger.trace("ID: " + sentenceID);
			myLogger.trace("Sentence: " + sentence);
			myLogger.trace("Tag: " + tag);

			// case 1
			String tagBackup = "" + tag;
			// if (StringUtility.isMatchedNullSafe("\\w+", tagBackup)) {
			if (StringUtility.isMatchedNullSafe(tagBackup, "\\w+")) {
				myLogger.trace("Case 1");
				if (!StringUtility.isMatchedNullSafe(tagBackup,
						String.format("\\b(%s)\\b", this.myLearnerUtility.getConstant().STOP))) {

					List<String> words = new LinkedList<String>();
					words.addAll(Arrays.asList(tagBackup.split("\\s+")));
					tag = words.get(words.size() - 1);

					String modifier = "";
					if (words.size() > 1) {
						modifier = StringUtils.join(
								StringUtility.stringArraySplice(words, 0,
										words.size() - 1), " ");
					}

					if (sentenceID == 22) {
						System.out.println();
					}
					if (StringUtility.isMatchedNullSafe(tag, "\\w")) {
						// case 1.1
						myLogger.trace("Case 1.1");
						dataholderHandler.tagSentenceWithMT(sentenceID,
								sentence, modifier, tag, "separatemodifiertag");
					} else {
						// case 1.2
						myLogger.trace("Case 1.2");
						myLogger.trace(sentenceID);
						dataholderHandler.tagSentenceWithMT(sentenceID,
								sentence, null, tag, "separatemodifiertag");
					}
				}

			}
			// case 2
			else {
				// treat them case by case
				// case 2: in some species, abaxially with =>NULL
				myLogger.trace("Case 2");
				if ((StringUtility.isMatchedNullSafe(tagBackup, "^in"))
						&& (StringUtility.isMatchedNullSafe(tagBackup,
								"\\b(with|without)\\b"))) {
					myLogger.trace("Case 2.1");
					dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
							"", null, "separtemodifiertag");
				} else {
					myLogger.trace("Case 2.2");
					String tagWithStopWordsReplaced = "" + tagBackup;
					if (tagWithStopWordsReplaced != null) {
						Pattern p = Pattern.compile("@ ([^@]+)$");
						Matcher m = p.matcher(tagWithStopWordsReplaced);
						if (m.find()) {
							String tg = m.group(1);
							ArrayList<String> tagWords = new ArrayList<String>();
							tagWords.addAll(Arrays.asList(tg.split("\\s+")));
							tag = tagWords.get(tagWords.size() - 1);
							String modifier = "";
							if (tagWords.size() > 1) {
								modifier = StringUtils.join(StringUtility
										.stringArraySplice(tagWords, 0,
												tagWords.size()), " ");
							}

							if (StringUtility.isMatchedNullSafe(tag, "\\w")) {
								myLogger.trace("Case 2.2.1");
								dataholderHandler.tagSentenceWithMT(sentenceID,
										sentence, modifier, tag,
										"separatemodifiertag");
							} else {
								myLogger.trace("Case 2.2.2");
								dataholderHandler.tagSentenceWithMT(sentenceID,
										sentence, "", null,
										"separatemodifiertag");
							}
						}
					}
				}
			}
		}
	}

}
