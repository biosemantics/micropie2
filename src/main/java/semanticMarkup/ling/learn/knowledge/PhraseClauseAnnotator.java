package semanticMarkup.ling.learn.knowledge;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.SentenceStructure;
import semanticMarkup.ling.learn.utility.LearnerUtility;
import semanticMarkup.ling.learn.utility.StringUtility;

/**
 * Annotate directly the remaining clauses which are phrases with the head noun.
 * 
 * @author Dongye
 * 
 */
public class PhraseClauseAnnotator implements IModule {
	private LearnerUtility myLearnerUtility;

	public PhraseClauseAnnotator(LearnerUtility learnerUtility) {
		this.myLearnerUtility = learnerUtility;
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		this.phraseClause(dataholderHandler);
	}
	
	public void phraseClause(DataHolder dataholderHandler) {
		for (SentenceStructure sentenceItem : dataholderHandler
				.getSentenceHolder()) {
			if (sentenceItem.getTag() == null) {
				int sentenceID = sentenceItem.getID();
				String sentence = sentenceItem.getSentence();
				List<String> res = this.phraseClauseHelper(sentence);
				if (res != null && res.size() == 2) {
					String modifier = res.get(0);
					String tag = res.get(1);
					dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
							modifier, tag, "phraseclause");
				}
			}
		}
	}

	public List<String> phraseClauseHelper(String sentence) {
		if (sentence == null) {
			return null;
		}

		List<String> res = new ArrayList<String>(2);
		String pattern = "^(.*?)((?:<[A-Z]*M[A-Z]*>[^<]*?<\\/[A-Z]*M[A-Z]*>\\s*)*)((?:<[A-Z]*[NO]+[A-Z]*>[^<]*?<\\/[A-Z]*[NO]+[A-Z]*>\\s*)+)<B>[,:\\.;]<\\/B>\\s*$";
		String sentenceCopy = "" + sentence;
		sentenceCopy = sentenceCopy.replaceAll("></?", "");

		Matcher m = StringUtility.createMatcher(sentenceCopy, pattern);
		if (m.find()) {
			String head = m.group(1);
			String modifier = m.group(2);
			String tag = m.group(3);

			String prepositionPattern = String.format("\\b(%s)\\b",
					this.myLearnerUtility.getConstant().PREPOSITION);
			if (!StringUtility.isMatchedNullSafe(head, prepositionPattern)
					&& !StringUtility.isMatchedNullSafe(head, "<\\/N>")
					&& !StringUtility.isMatchedNullSafe(modifier,
							prepositionPattern)) {
				if (tag != null) {
					Matcher m2 = StringUtility.createMatcher(tag,
							"(.*?)<N>([^<]+)<\\/N>\\s*$");
					if (m2.find()) {
						modifier = modifier + m2.group(1);
						tag = m2.group(2);
					}
					tag = tag.replaceAll("<\\S+?>", "");
					modifier = modifier.replaceAll("<\\S+?>", "");
					tag = tag.replaceAll("(^\\s*|\\s*$)", "");
					modifier = modifier.replaceAll("(^\\s*|\\s*$)", "");
					res.add(modifier);
					res.add(tag);

					return res;
				}
			}
		}
		return res;
	}

}
