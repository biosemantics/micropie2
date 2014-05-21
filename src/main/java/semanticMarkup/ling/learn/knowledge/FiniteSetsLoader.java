package semanticMarkup.ling.learn.knowledge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.utility.LearnerUtility;

public class FiniteSetsLoader implements IModule {
	private LearnerUtility myLearnerUtility;

	public FiniteSetsLoader(LearnerUtility learnerUtility) {
		this.myLearnerUtility = learnerUtility;
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		this.addStopWords(dataholderHandler);
		this.addCharacters(dataholderHandler);
		this.addNumbers(dataholderHandler);
		this.addClusterStrings(dataholderHandler);
		this.addProperNouns(dataholderHandler);
		
	}
	
	public void addStopWords(DataHolder dataholderHandler) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.addStopWords");
		myLogger.trace("Add stop words");

		List<String> stops = new ArrayList<String>();
		stops.addAll(Arrays.asList(this.myLearnerUtility.getConstant().STOP.split("\\|")));
		stops.addAll(Arrays.asList(new String[] { "NUM", "(", "[", "{", ")",
				"]", "}", "d+" }));

		myLogger.trace("Stop Words: " + stops);
		for (int i = 0; i < stops.size(); i++) {
			String word = stops.get(i);
			if (word.matches("\\b(" + this.myLearnerUtility.getConstant().FORBIDDEN + ")\\b")) {
				continue;
			}
			dataholderHandler.updateDataHolder(word, "b", "*", "wordpos", 0);
			myLogger.trace(String.format(
					"(\"%s\", \"b\", \"*\", \"wordpos\", 0) added\n", word));
			// this.getWordPOSHolder().put(new WordPOSKey(word, "b"), new
			// WordPOSValue("*", 0, 0, null, null));
			// System.out.println("Add Stop Word: " + word+"\n");
		}
		myLogger.trace("Quite\n");
	}

	public void addCharacters(DataHolder dataholderHandler) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.addCharacters");
		myLogger.trace("Add characters");

		List<String> chars = new ArrayList<String>();
		chars.addAll(Arrays.asList(this.myLearnerUtility.getConstant().CHARACTER.split("\\|")));
		//
		// System.out.println(chars);
		// System.out.println(this.myLearnerUtility.getConstant().CHARACTER);

		for (int i = 0; i < chars.size(); i++) {
			String word = chars.get(i);
			// String reg="\\b("+this.myLearnerUtility.getConstant().FORBIDDEN+")\\b";
			// boolean f = word.matches(reg);
			if (word.matches("\\b(" + this.myLearnerUtility.getConstant().FORBIDDEN + ")\\b")) {
				continue;
			}
			dataholderHandler.updateDataHolder(word, "b", "*", "wordpos", 0);
			// this.getWordPOSHolder().put(new WordPOSKey(word, "b"), new
			// WordPOSValue("", 0, 0, null, null));
			// System.out.println("addCharacter word: " + word);
		}
	}

	public void addNumbers(DataHolder dataholderHandler) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.addNumbers");
		myLogger.trace("Add numbers");

		List<String> nums = new ArrayList<String>();
		nums.addAll(Arrays.asList(this.myLearnerUtility.getConstant().NUMBER.split("\\|")));

		// System.out.println(nums);
		// System.out.println(this.myLearnerUtility.getConstant().NUMBER);

		for (int i = 0; i < nums.size(); i++) {
			String word = nums.get(i);
			// String reg="\\b("+this.myLearnerUtility.getConstant().FORBIDDEN+")\\b";
			// boolean f = word.matches(reg);
			if (word.matches("\\b(" + this.myLearnerUtility.getConstant().FORBIDDEN + ")\\b")) {
				continue;
			}
			dataholderHandler.updateDataHolder(word, "b", "*", "wordpos", 0);
			// this.getWordPOSHolder().put(new WordPOSKey(word, "b"), new
			// WordPOSValue("*", 0, 0, null, null));
			// System.out.println("add Number: " + word);
		}
		dataholderHandler.updateDataHolder("NUM", "b", "*", "wordpos", 0);
		// this.getWordPOSHolder().put(new WordPOSKey("NUM", "b"), new
		// WordPOSValue("*",0, 0, null, null));
	}

	public void addClusterStrings(DataHolder dataholderHandler) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.addClusterstrings");
		myLogger.trace("Add clusterstrings");

		List<String> cltstrs = new ArrayList<String>();
		cltstrs.addAll(Arrays.asList(this.myLearnerUtility.getConstant().CLUSTERSTRING.split("\\|")));

		// System.out.println(cltstrs);
		// System.out.println(this.myLearnerUtility.getConstant().CLUSTERSTRING);

		for (int i = 0; i < cltstrs.size(); i++) {
			String word = cltstrs.get(i);
			if (word.matches("\\b(" + this.myLearnerUtility.getConstant().FORBIDDEN + ")\\b")) {
				continue;
			}
			dataholderHandler.updateDataHolder(word, "b", "*", "wordpos", 0);
			// this.getWordPOSHolder().put(new WordPOSKey(word, "b"), new
			// WordPOSValue("*", 1, 1, null, null));
			// System.out.println("addClusterString: " + word);
		}
	}

	public void addProperNouns(DataHolder dataholderHandler) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.addProperNouns");
		myLogger.trace("Add proper nouns");

		List<String> ppnouns = new ArrayList<String>();
		ppnouns.addAll(Arrays.asList(Constant.PROPERNOUN.split("\\|")));

		for (int i = 0; i < ppnouns.size(); i++) {
			String word = ppnouns.get(i);
			if (word.matches("\\b(" + this.myLearnerUtility.getConstant().FORBIDDEN + ")\\b")) {
				continue;
			}
			dataholderHandler.updateDataHolder(word, "b", "*", "wordpos", 0);
			// this.getWordPOSHolder().put(new WordPOSKey(word, "z"), new
			// WordPOSValue("*", 0, 0, null, null));
			// System.out.println("Add ProperNoun: " + word);
		}
	}
}
