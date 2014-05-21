package semanticMarkup.ling.learn.knowledge;

import java.util.List;
import java.util.regex.Matcher;

import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.SentenceStructure;
import semanticMarkup.ling.learn.utility.StringUtility;

public class IgnoredFinalizer implements IModule {

	public IgnoredFinalizer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		List<SentenceStructure> sentences = dataholderHandler
				.getSentencesByTagPattern("^ignore$");

		for (SentenceStructure sentenceItem : sentences) {
			String sentence = sentenceItem.getSentence();
			if (sentence != null) {
				Matcher m = StringUtility.createMatcher(sentence,
						Constant.IGNORE_PATTERN);
				if (m.find()) {
					String g1 = m.group(1);
					if (StringUtility.isMatchedNullSafe(g1, "<N>")) {
						int sentenceID = sentenceItem.getID();
						SentenceStructure sentenceItemX = dataholderHandler
								.getSentence(sentenceID);
						sentenceItemX.setTag(null);
					}
				}
			}
		}
	}
}
