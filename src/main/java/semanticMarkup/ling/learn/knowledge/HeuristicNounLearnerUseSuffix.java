package semanticMarkup.ling.learn.knowledge;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import semanticMarkup.know.lib.WordNetPOSKnowledgeBase;
import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.WordPOSKey;
import semanticMarkup.ling.learn.dataholder.WordPOSValue;
import semanticMarkup.ling.learn.utility.LearnerUtility;

/**
 * Learn a set of seed nouns (singular and plural forms) by applying a number of
 * rules based on heuristics on the collection.
 * 
 * @author Dongye
 * 
 */
public class HeuristicNounLearnerUseSuffix implements IModule {
	private LearnerUtility myLearnerUtility;
	
	public HeuristicNounLearnerUseSuffix(LearnerUtility learnerUtility) {
		this.myLearnerUtility = learnerUtility;
	}	

	@Override
	public void run(DataHolder dataholderHandler) {
		this.posBySuffix(dataholderHandler);
	}
	
	/**
	 * for each unknown word in unknownwords table seperate root and suffix if
	 * root is a word in WN or in unknownwords table make the unknowword a "b"
	 * boundary
	 * 
	 * suffix: -fid(adj), -form (adj), -ish(adj), -less(adj), -like (adj)),
	 * -merous(adj), -most(adj), -shaped(adj), -ous(adj)
	 */
	public void posBySuffix(DataHolder dataholderHandler) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.posBySuffix");
		myLogger.trace("Enter posBySuffix");

		Iterator<Map.Entry<String, String>> iterator = dataholderHandler
				.getUnknownWordHolder().entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, String> unknownWordEntry = iterator.next();
			String unknownWord = unknownWordEntry.getKey();
			String unknownWordTag = unknownWordEntry.getValue();

			if (unknownWordTag.equals("unknown")) {
				// boolean flag1 =
				posBySuffixCase1Helper(dataholderHandler, unknownWord);
				// boolean flag2 =
				posBySuffixCase2Helper(dataholderHandler, unknownWord);
			}
		}

		myLogger.trace("Quite posBySuffix");
	}
	
	public boolean posBySuffixCase1Helper(DataHolder dataholderHandler, String unknownWord) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.posBySuffix");

		String pattern1 = "^[a-z_]+(" + Constant.SUFFIX + ")$";
		myLogger.debug("Pattern1: " + pattern1);

		if (unknownWord.matches(pattern1)) {
			Matcher matcher = Pattern
					.compile("(.*?)(" + Constant.SUFFIX + ")$").matcher(
							unknownWord);
			if ((unknownWord.matches("^[a-zA-Z0-9_-]+$")) && matcher.matches()) {
				myLogger.debug("posBySuffix - check word: " + unknownWord);
				String base = matcher.group(1);
				String suffix = matcher.group(2);
				if (this.containSuffix(dataholderHandler, unknownWord, base, suffix)) {
					myLogger.debug("Pass\n");
					dataholderHandler.updateDataHolder(unknownWord, "b", "*",
							"wordpos", 0);
					myLogger.debug("posBySuffix - set word: " + unknownWord);
					return true;
				} else {
					myLogger.debug("Not Pass\n");
				}
			}
		}
		return false;
	}

	public boolean posBySuffixCase2Helper(DataHolder dataholderHandler, String unknownWord) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.posBySuffix");

		String pattern2 = "^[._.][a-z]+"; // , _nerved
		myLogger.debug("Pattern2: " + pattern2);

		if (unknownWord.matches(pattern2)) {
			dataholderHandler.getWordPOSHolder().put(
					new WordPOSKey(unknownWord, "b"),
					new WordPOSValue("*", 0, 0, null, null));
			myLogger.debug("posbysuffix set " + unknownWord
					+ " a boundary word\n");
			return true;
		}

		return false;
	}

	/**
	 * return false or true depending on if the word contains the suffix as the
	 * suffix
	 * 
	 * @param word
	 * @param base
	 * @param suffix
	 * @return
	 */
	public boolean containSuffix(DataHolder dataholderHandler, String word, String base, String suffix) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.posBySuffix.containSuffix");
		myLogger.trace("Enter containSuffix");

		boolean flag = false; // return value
		boolean wordInWN = false; // if this word is in WordNet
		boolean baseInWN = false;
		WordNetPOSKnowledgeBase myWN = this.myLearnerUtility
				.getWordNetPOSKnowledgeBase();

		// check base
		if (base.length() == 0) {
			myLogger.trace("case 0");
			return true;
		}

		base.replaceAll("_", ""); // cup_shaped

		if (myWN.contains(word)) {
			myLogger.trace("case 1.1");
			wordInWN = true; // word is in WordNet
		} else {
			myLogger.trace("case 1.2");
			wordInWN = false;
		}

		if (myWN.contains(base)) {
			myLogger.trace("case 2.1");
			baseInWN = true;
		} else {
			myLogger.trace("case 2.2");
			baseInWN = false;
		}

		// if WN pos is adv, return 1: e.g. ly, or if $base is in
		// unknownwords table
		if (suffix.equals("ly")) {
			myLogger.trace("case 3.1");
			if (wordInWN) {
				if (myWN.isAdverb(word)) {
					return true;
				}
			}
			// if the word is in unknown word set, return true
			if (dataholderHandler.getUnknownWordHolder().containsKey(base)) {
				return true;
			}
		}

		// if WN recognize superlative, comparative adjs, return 1: e.g. er, est
		else if (suffix.equals("er") || suffix.equals("est")) {
			myLogger.trace("case 3.2");
			if (wordInWN) {
				boolean case1 = !myWN.isAdjective(word);
				boolean case2 = myWN.isAdjective(base);
				if (case1 && case2) {
					return true;
				} else {
					return false;
				}
			}
		}

		// if $base is in WN or unknownwords table, or if $word has sole pos
		// adj in WN, return 1: e.g. scalelike
		else {
			myLogger.trace("case 3.3");
			if (myWN.isSoleAdjective(word)) {
				return true;
			}
			if (baseInWN) {
				return true;
			}
			if (dataholderHandler.getUnknownWordHolder().containsKey(base)) {
				return true;
			}
		}

		return flag;
	}

}
