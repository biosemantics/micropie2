package semanticMarkup.ling.learn.knowledge;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import semanticMarkup.ling.learn.Configuration;
import semanticMarkup.ling.learn.auxiliary.StringAndInt;
import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.SentenceStructure;
import semanticMarkup.ling.learn.utility.LearnerUtility;

/**
 * This module does rule based learning first for easy cases, then does instance
 * based learning for the remaining unsolved cases.
 * 
 * This module can run in two different modes: 1) start; 2) normal.
 * 
 * @author Dongye
 * 
 */
public class CoreBootstrappingLearner implements IModule {
	private LearnerUtility myLearnerUtility;
	private String status;
	private Configuration myConfiguration;
	
	public CoreBootstrappingLearner(LearnerUtility learnerUtility, Configuration configuration) {
		this.myLearnerUtility = learnerUtility;
		this.status = null;
		this.myConfiguration = configuration;
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		this.discover(dataholderHandler, this.status);

	}
	
	public void setStatus(String status){
		this.status = status;
	}
	
	/**
	 * 
	 * @param status
	 *            "start" or "normal"
	 * @return
	 */
	public int discover(DataHolder dataholderHandler, String status) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.discover");

		myLogger.trace("Enter Discover - Status: " + status);

		int newDisc = 0;

		// dataholderHandler.printHolder(DataHolder.SENTENCE);

		for (int i = 0; i < dataholderHandler.getSentenceHolder().size(); i++) {
			SentenceStructure sentEntry = dataholderHandler.getSentenceHolder()
					.get(i);
			// sentid
			String thisSentence = sentEntry.getSentence();
			String thisLead = sentEntry.getLead();
			String thisTag = sentEntry.getTag();
			String thisStatus = sentEntry.getStatus();
			// if (!(thisTag == null || !thisTag.equals("ignore")

			// myLogger.debug("Tag: "+thisTag);

			if ((!StringUtils.equals(thisTag, "ignore") || (thisTag == null))
					&& thisStatus.equals(status)) {

				myLogger.debug("Sentence #: " + i);
				myLogger.debug("Lead: " + thisLead);

				myLogger.debug("Tag: " + thisTag);

				myLogger.debug("Sentence: " + thisSentence);
				// tag is not null
				if (isMarked(dataholderHandler.getSentenceHolder().get(i))) {
					myLogger.debug("Not Pass");
					continue;
				}
				// tag is null
				else {
					myLogger.debug("Pass");
				}

				String[] startWords = thisLead.split("\\s+");
				myLogger.debug("startWords: " + startWords.toString());

				String pattern = buildPattern(dataholderHandler, startWords);

				if (pattern != null) {
					myLogger.debug("Build pattern [" + pattern
							+ "] from starting words [" + thisLead + "]");
					// IDs of untagged sentences that match the pattern
					Set<Integer> matched = matchPattern(dataholderHandler, pattern, status, false);
					int round = 0;
					int numNew = 0;

					do {
						numNew = ruleBasedLearn(dataholderHandler, matched);
						newDisc = newDisc + numNew;
						myLogger.trace("Round: " + round);
						round++;
					} while (numNew > 0);
				} else {
					myLogger.debug("Build no pattern from starting words ["
							+ thisLead + "]");
				}
			}
		}

		myLogger.trace("Return " + newDisc);
		myLogger.trace("Quite discover");
		return newDisc;
	}
	
	/**
	 * build a pattern based on existing checked word set, and the start words
	 * 
	 * @param startWords
	 * @return a pattern. If no pattern is generated, return null
	 */
	public String buildPattern(DataHolder dataholderHandler, String[] startWords) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.discover.buildPattern");

		myLogger.trace("Enter buildPattern");
		myLogger.trace("Start Words: " + startWords);

		Set<String> newWords = new HashSet<String>();
		String temp = "";
		String prefix = "\\w+\\s";
		String pattern = "";

		Set<String> checkedWords = dataholderHandler.checkedWordSet;
		myLogger.trace("checkedWords: " + checkedWords);

		for (int i = 0; i < startWords.length; i++) {
			String word = startWords[i];
			// This is not very sure, need to make sure - Dongye
			if ((!word.matches("[\\p{Punct}0-9]"))
					&& (!checkedWords.contains(word))) {
				temp = temp + word + "|";
				newWords.add(word);
			}
		}
		myLogger.trace("temp: " + temp);

		// no new words
		if (temp.length() == 0) {
			myLogger.trace("No new words");
			myLogger.trace("Return null");
			myLogger.trace("Quite buildPattern");
			myLogger.trace("\n");
			return null;
		} else {

			// remove the last char, which is a '|'
			temp = temp.substring(0, temp.length() - 1);
		}

		temp = "\\b(?:" + temp + ")\\b";
		pattern = "^" + temp + "|";

		for (int j = 0; j < this.myConfiguration.getNumLeadWords() - 1; j++) {
			temp = prefix + temp;
			pattern = pattern + "^" + temp + "|";
		}
		myLogger.trace("Pattern: " + pattern);

		pattern = pattern.substring(0, pattern.length() - 1);
		pattern = "(?:" + pattern + ").*$";
		checkedWords.addAll(newWords);
		dataholderHandler.checkedWordSet = checkedWords;

		myLogger.trace("Return Pattern: " + pattern);
		myLogger.trace("Quite buildPattern");
		myLogger.trace("\n");
		return pattern;
	}
	
	/**
	 * Find the IDs of the sentences that matches the pattern
	 * 
	 * @param pattern
	 * @param status
	 * @param hasTag
	 * @return a set of sentence IDs of the sentences that matches the pattern
	 */
	public Set<Integer> matchPattern(DataHolder dataholderHandler, String pattern, String status,
			boolean hasTag) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.discover.matchPattern");

		myLogger.trace("Enter matchPattern");
		myLogger.trace("Pattern: " + pattern);
		myLogger.trace("Status: " + status);
		myLogger.trace("HasTag: " + hasTag);

		Set<Integer> matchedIDs = new HashSet<Integer>();

		for (int i = 0; i < dataholderHandler.getSentenceHolder().size(); i++) {
			SentenceStructure sent = dataholderHandler.getSentenceHolder().get(
					i);
			String thisSentence = sent.getSentence();
			String thisStatus = sent.getStatus();
			String thisTag = sent.getTag();

			boolean a = hasTag;
			boolean b = (thisTag == null);

			if ((a ^ b) && (StringUtils.equals(status, thisStatus))) {
				Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(thisSentence);
				if (m.lookingAt()) {
					myLogger.debug("Push Sentence #" + i);
					myLogger.debug("Sentence: " + thisSentence);
					myLogger.debug("Status: " + thisStatus);
					myLogger.debug("Tag: " + thisTag);
					myLogger.debug("\n");

					matchedIDs.add(i);
				}
			}
		}

		myLogger.trace("Return IDs: " + matchedIDs);
		myLogger.trace("Quite matchPattern");
		myLogger.trace("\n");
		return matchedIDs;
	}

	/**
	 * return a positive number if anything new is learned from @source sentences
	 * by applying rules and clues to grow %NOUNS and %BDRY and to confirm tags
	 * create and maintain decision tables
	 * 
	 * @param matched
	 * @return
	 */
	public int ruleBasedLearn(DataHolder dataholderHandler, Set<Integer> matched) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.discover.ruleBasedLearn");

		myLogger.trace("Enter ruleBasedLearn");
		myLogger.trace("Matched IDs: " + matched);

		int sign = 0;

		Iterator<Integer> iter = matched.iterator();
		while (iter.hasNext()) {
			int sentID = iter.next().intValue();
			SentenceStructure sentence = dataholderHandler.getSentenceHolder()
					.get(sentID);
			if (!isMarked(sentence)) {
				StringAndInt tagAndNew = null;
				String tag = null;
				int numNew = 0;

				tagAndNew = this.myLearnerUtility.learnTerms(dataholderHandler, sentID);
				tag = tagAndNew.getString();
				numNew = tagAndNew.getInt();

				this.myLearnerUtility.tagSentence(dataholderHandler, this.myConfiguration.getMaxTagLength(), sentID, tag);
				sign = sign + numNew;
			}
		}

		myLogger.trace("Return: " + sign);
		myLogger.trace("Quit ruleBaseLearn");
		myLogger.trace("\n");

		return sign;
	}
	
	/**
	 * A helper of method discover(). Check if the tag of the i-th sentence is
	 * NOT null
	 * 
	 * @param sentence
	 *            the sentence to check
	 * @return if the tag of the i-th sentence is NOT null, returns true;
	 *         otherwise returns false
	 */
	public boolean isMarked(SentenceStructure sentence) {
		String thisTag = sentence.getTag();

		if (thisTag != null) {
			return true;
		} else {
			return false;
		}
	}
}
