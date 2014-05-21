package semanticMarkup.ling.learn.knowledge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
 * Set and/or tags
 * 
 * @author Dongye
 * 
 */
public class AndOrTagSetter implements IModule {
	private LearnerUtility myLearnerUtility;

	public AndOrTagSetter(LearnerUtility learnerUtility) {
		this.myLearnerUtility = learnerUtility;
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		// TODO Auto-generated method stub

	}

	public void setAndOr(DataHolder dataholderHandler) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.separateModifierTag");
		myLogger.debug("Tag and/or sentences andor");

		String ptn1 = "^(?:[mbq,]{0,10}[onp]+(?:,|(?=&)))+&(?:[mbq,]{0,10}[onp]+)"; // n,n,n&n
		String ptn2 = "^(?:[mbq,]{0,10}(?:,|(?=&)))+&(?:[mbq,]{0,10})[onp]+"; // m,m,&mn

		Iterator<SentenceStructure> sentenceIter = dataholderHandler
				.getSentenceHolderIterator();
		while (sentenceIter.hasNext()) {
			SentenceStructure sentenceItem = sentenceIter.next();
			int sentenceID = sentenceItem.getID();
			String sentence = sentenceItem.getSentence();
			String lead = sentenceItem.getLead();
			if (isIsAndOrSentence(dataholderHandler, sentenceID, sentence,
					lead, ptn1, ptn2)) {
				sentenceItem.setTag("andor");
			}
		}
	}

	public boolean isIsAndOrSentence(DataHolder dataholderHandler,
			int sentenceID, String sentence, String lead, String ptn1,
			String ptn2) {

		Set<String> token = new HashSet<String>();
		token.addAll(Arrays.asList("and or nor".split(" ")));
		token.add("\\");
		token.add("and / or");

		int limit = 80;

		List<String> words = new ArrayList<String>();
		words.addAll(Arrays.asList(sentence.split(" ")));

		String sentencePtn = this.myLearnerUtility.getSentencePtn(
				dataholderHandler, token, limit, words);

		if (sentencePtn == null) {
			return false;
		}

		boolean result = isIsAndOrSentenceHelper(words, sentencePtn, ptn1, ptn2);

		return result;
	}

	public boolean isIsAndOrSentenceHelper(List<String> words,
			String sentencePtn, String ptn1, String ptn2) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.isIsAndOrSentence");

		sentencePtn = sentencePtn.toLowerCase();
		// ignore the distinction between type modifiers and modifiers
		sentencePtn = sentencePtn.replaceAll("t", "m");

		Pattern p1 = Pattern.compile(ptn1);
		Matcher m1 = p1.matcher(sentencePtn);

		Pattern p2 = Pattern.compile(ptn2);
		Matcher m2 = p2.matcher(sentencePtn);

		int end = -1;
		boolean case1 = false;
		boolean case2 = false;

		if (m1.find()) {
			end = m1.end();
			case1 = true;
		}

		if (m2.find()) {
			end = m2.end();
			case2 = true;
		}

		if (case1 || case2) {
			String matchedWords = StringUtils.join(words.subList(0, end), " ");
			String regex = String.format("\\b(%s)\\b",
					this.myLearnerUtility.getConstant().PREPOSITION);
			if (StringUtility.isMatchedNullSafe(matchedWords, regex)) {
				myLogger.trace("Case 1");
				return false;
			}
			myLogger.trace("Case 2");
			return true;
		}
		myLogger.trace("Case 3");
		return false;
	}

}
