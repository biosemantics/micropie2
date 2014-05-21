package semanticMarkup.ling.learn.knowledge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.SentenceStructure;
import semanticMarkup.ling.learn.utility.LearnerUtility;
import semanticMarkup.ling.learn.utility.StringUtility;

/**
 * Learns nouns based on some heuristics.
 * 
 * @author Dongye
 * 
 */
public class HeuristicNounLearnerUseMorphology implements IModule {
	private LearnerUtility myLearnerUtility;

	public HeuristicNounLearnerUseMorphology(LearnerUtility learnerUtility) {
		this.myLearnerUtility = learnerUtility;
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.addHeuristicsNouns");

		myLogger.trace("Enter addHeuristicsNouns");

		Set<String> nouns = this.learnHeuristicsNouns(dataholderHandler);
		myLogger.debug("Nouns learned from heuristics:");
		myLogger.debug("\t" + nouns.toString());
		myLogger.debug("Total: " + nouns.size());

		List<Set<String>> results = this.characterHeuristics(dataholderHandler);
		Set<String> rnouns = results.get(0);
		Set<String> descriptors = results.get(1);
		addDescriptors(dataholderHandler, descriptors);
		addNouns(dataholderHandler, rnouns);

		// dataholderHandler.printHolder(DataHolder.SINGULAR_PLURAL);

		myLogger.debug("Total: " + nouns.size());
		Iterator<String> iter = nouns.iterator();
		myLogger.info("Learn singular-plural pair");
		while (iter.hasNext()) {
			String e = iter.next();
			myLogger.trace("Check Word: " + e);

			if ((e.matches("^.*\\w.*$"))
					&& (!StringUtility.isMatchedWords(e, "NUM|"
							+ this.myLearnerUtility.getConstant().NUMBER + "|"
							+ this.myLearnerUtility.getConstant().CLUSTERSTRING
							+ "|"
							+ this.myLearnerUtility.getConstant().CHARACTER
							+ "|"
							+ this.myLearnerUtility.getConstant().PROPERNOUN))) {
				myLogger.trace("Pass");

				// same word may have two different pos tags
				String[] nounArray = e.split("\\|");
				for (int i = 0; i < nounArray.length; i++) {
					String nounAndPOS = nounArray[i];
					Pattern p = Pattern.compile("(\\w+)\\[([spn])\\]");
					Matcher m = p.matcher(nounAndPOS);
					if (m.lookingAt()) {
						String word = m.group(1);
						String pos = m.group(2);
						dataholderHandler.updateDataHolder(word, pos, "*",
								"wordpos", 0);

						if (pos.equals("p")) {
							String plural = word;
							String singular = this.myLearnerUtility
									.getWordFormUtility().getSingular(plural);
							if (singular != null) {
								if (!singular.equals("")) {
									dataholderHandler.addSingularPluralPair(
											singular, plural);
								}
							}
						}

						if (pos.equals("s")) {
							String singular = word;
							List<String> pluralList = this.myLearnerUtility
									.getWordFormUtility().getPlural(singular);
							Iterator<String> pluralIter = pluralList.iterator();
							while (pluralIter.hasNext()) {
								String plural = pluralIter.next();
								if (plural != null) {
									if (!plural.equals("")) {
										dataholderHandler
												.addSingularPluralPair(
														singular, plural);
									}
								}
							}
						}
					}
				}
			}
		}

		myLogger.trace("Quite addHeuristicsNouns");
	}

	/**
	 * 
	 * @param descriptors
	 */
	public void addDescriptors(DataHolder dataholderHandler,
			Set<String> descriptors) {
		Iterator<String> iter = descriptors.iterator();
		while (iter.hasNext()) {
			String descriptor = iter.next();

			if (!StringUtility.isMatchedWords(descriptor,
					this.myLearnerUtility.getConstant().FORBIDDEN)) {
				dataholderHandler.updateDataHolder(descriptor, "b", "",
						"wordpos", 1);
			}
		}

	}

	/**
	 * 
	 * @param rnouns
	 */
	public void addNouns(DataHolder dataholderHandler, Set<String> rnouns) {
		Iterator<String> iter = rnouns.iterator();
		while (iter.hasNext()) {
			String noun = iter.next();
			if (!StringUtility.isMatchedWords(noun,
					this.myLearnerUtility.getConstant().FORBIDDEN)) {
				dataholderHandler.updateDataHolder(noun, "n", "", "wordpos", 1);
			}
		}
	}

	/**
	 * 
	 * @return nouns learned by heuristics
	 */
	public Set<String> learnHeuristicsNouns(DataHolder dataholderHandler) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger
				.getLogger("learn.addHeuristicsNouns.learnHeuristicsNouns");

		// Set of words
		Set<String> words = new HashSet<String>();

		// Set of nouns
		Set<String> nouns = new HashSet<String>();

		List<String> sentences = new LinkedList<String>();
		for (int i = 0; i < dataholderHandler.getSentenceHolder().size(); i++) {
			String originalSentence = dataholderHandler.getSentenceHolder()
					.get(i).getOriginalSentence();
			myLogger.trace("Original Sentence: " + originalSentence);
			sentences.add(StringUtility.strip(originalSentence));
		}

		// Now we have original sentences in sentences
		// Method addWords
		for (int i = 0; i < sentences.size(); i++) {
			String sentence = sentences.get(i);
			sentence = sentence.toLowerCase();
			String noun = this.getPresentAbsentNouns(sentence);
			if (!noun.equals("")) {
				nouns.add(noun);
			}

			// add words
			List<String> tokens = this.myLearnerUtility.tokenizeText(sentence,
					"all");
			for (String token : tokens) {
				if (StringUtility.isWord(token)) {
					words.add(token);
					myLogger.trace("Add a word into words: " + token);
				}
			}
		}

		// solve the problem: septa and septum are both s
		Iterator<String> nounsIterator = nouns.iterator();
		while (nounsIterator.hasNext()) {
			String oldNoun = nounsIterator.next();
			String newNoun = this.getHeuristicsNounsHelper(oldNoun, nouns);
			if (!newNoun.equals(oldNoun)) {
				nouns.remove(oldNoun);
				nouns.add(newNoun);
			}
		}

		// sort all words
		Map<String, Set<String>> wordMap = new HashMap<String, Set<String>>();
		Iterator<String> wordsIterator = words.iterator();
		while (wordsIterator.hasNext()) {
			String word = wordsIterator.next();
			String root = myLearnerUtility.getWordFormUtility().getRoot(word);
			if (wordMap.containsKey(root)) {
				Set<String> wordList = wordMap.get(root);
				wordList.add(word);
				// List<String> wordList2 = wordMap.get(root);
				// System.out.println(wordList2);
			} else {
				Set<String> wordList = new HashSet<String>();
				wordList.add(word);
				wordMap.put(root, wordList);
			}
		}

		// print out the wordMap
		myLogger.trace("WordMap:");
		Iterator<Map.Entry<String, Set<String>>> wordMapIter = wordMap
				.entrySet().iterator();
		while (wordMapIter.hasNext()) {
			Map.Entry<String, Set<String>> e = wordMapIter.next();
			myLogger.trace(e.toString());
		}

		// find nouns
		myLogger.info("Learn singular-plural pair");
		Iterator<Map.Entry<String, Set<String>>> iter = wordMap.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Set<String>> e = iter.next();
			Set<String> wordSet = e.getValue();
			Iterator<String> wordIterator = wordSet.iterator();
			while (wordIterator.hasNext()) {
				String word = wordIterator.next();

				// getnouns
				if (word.matches("^.*" + Constant.NENDINGS)) {
					nouns.add(word + "[s]");
					if (wordSet.contains(word + "s")) {
						nouns.add(word + "s" + "[p]");
						dataholderHandler.addSingularPluralPair(word, word
								+ "s");
					}
					if (wordSet.contains(word + "es")) {
						nouns.add(word + "es" + "[p]");
						dataholderHandler.addSingularPluralPair(word, word
								+ "es");
					}
				}
			}
		}

		// Iterator<LinkedList> wordMapIterator = wordMap.i
		Iterator<Map.Entry<String, Set<String>>> wordMapIterator = wordMap
				.entrySet().iterator();
		while (wordMapIterator.hasNext()) {
			Map.Entry<String, Set<String>> wordMapEntry = wordMapIterator
					.next();
			Set<String> wordSet = wordMapEntry.getValue();

			// check if there is a word with Vending
			boolean hasVending = false;
			// for (int i1 = 0; i1 < wordList.size(); i1++) {
			Iterator<String> wordIterator = wordSet.iterator();
			while (wordIterator.hasNext()) {
				String tempWord = wordIterator.next();
				if (tempWord.matches("^.*" + Constant.VENDINGS)) {
					hasVending = true;
					break;
				}
			}

			// at least two words without verb endings
			if ((!hasVending) && (wordSet.size() > 1)) {
				List<String> wordList = new LinkedList<String>(wordSet);
				for (int i = 0; i < wordList.size(); i++) {
					for (int j = i + 1; j < wordList.size(); j++) {
						String word1 = wordList.get(i);
						String word2 = wordList.get(j);
						List<String> pair = myLearnerUtility
								.getWordFormUtility().getSingularPluralPair(
										word1, word2);
						if (pair.size() == 2) {
							String singular = pair.get(0);
							String plural = pair.get(1);
							nouns.add(singular + "[s]");
							nouns.add(plural + "[p]");
							dataholderHandler.addSingularPluralPair(singular,
									plural);
						}
					}
				}
			}
		}

		// print out nouns
		myLogger.debug("Nouns: " + nouns);

		return nouns;
	}

	// ---------------addHeuristicsNouns Help Function----
	// #solve the problem: septa and septum are both s
	// septum - Singular
	// septa -Plural
	// septa[s] => septa[p]
	public String getHeuristicsNounsHelper(String oldNoun, Set<String> words) {
		String newNoun = oldNoun;

		if (oldNoun.matches("^.*a\\[s\\]$")) {
			String noun = oldNoun.replaceAll("\\[s\\]", "");
			if (words.contains(noun)) {
				newNoun = noun + "[p]";
			}
		}

		return newNoun;
	}

	/**
	 * any word preceeding "present"/"absent" would be a n
	 * 
	 * @param text
	 *            the content to learn from
	 * @return nouns learned
	 */
	public String getPresentAbsentNouns(String text) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger
				.getLogger("learn.addHeuristicsNouns.learnHeuristicsNouns.getPresentAbsentNouns");

		String pachecked = "and|or|to";

		// if (text.matches("(\\w+?)\\s+(present|absent)")) {
		// System.out.println(text);
		// }

		Matcher matcher = Pattern.compile("^.*?(\\w+?)\\s+(present|absent).*$")
				.matcher(text);
		if (matcher.lookingAt()) {
			String word = matcher.group(1);
			if ((!word.matches("\\b(" + pachecked + ")\\b"))
					&& (!word
							.matches("\\b("
									+ this.myLearnerUtility.getConstant().STOP
									+ ")\\b"))
					&& (!word
							.matches("\\b(always|often|seldom|sometimes|[a-z]+ly)\\b"))) {

				myLogger.trace("present/absent " + word);

				if (((word.matches("^.*" + Constant.PENDINGS))
						|| (word.matches("^.*[^s]s$")) || (word
							.matches("teeth")))
						&& (!word.matches(Constant.SENDINGS))) {
					return word + "[p]";
				} else {
					return word + "[s]";
				}
			}
		}

		return "";
	}

	/**
	 * Discover nouns and descriptors according to a set of rules
	 * 
	 * @return a linked list, whose first element is a set of nouns, and second
	 *         element is a set of descriptors
	 */
	public List<Set<String>> characterHeuristics(DataHolder dataholderHandler) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger
				.getLogger("learn.addHeuristicsNouns.characterHeuristics");

		Set<String> taxonNames = new HashSet<String>();
		Set<String> nouns = new HashSet<String>();
		Set<String> anouns = new HashSet<String>();
		Set<String> pnouns = new HashSet<String>();
		Set<String> descriptors = new HashSet<String>();
		Map<String, Boolean> descriptorMap = new HashMap<String, Boolean>();

		int sent_num = dataholderHandler.getSentenceHolder().size();
		for (int i = 0; i < sent_num; i++) {

			// taxon rule
			SentenceStructure sent = dataholderHandler.getSentenceHolder().get(
					i);
			String source = sent.getSource();
			String sentence = sent.getSentence();
			String originalSentence = sent.getOriginalSentence();

			myLogger.trace("Source: " + source);
			myLogger.trace("Sentence: " + sentence);
			myLogger.trace("Original Sentence: " + originalSentence);

			originalSentence = StringUtility.trimString(originalSentence);

			// noun rule 0: taxon names
			taxonNames = this.getTaxonNameNouns(originalSentence);

			// $sentence =~ s#<\s*/?\s*i\s*>##g;
			// $originalsent =~ s#<\s*/?\s*i\s*>##g;

			sentence = sentence.replaceAll("<\\s*/?\\s*i\\s*>", "");
			originalSentence = originalSentence.replaceAll("<\\s*/?\\s*i\\s*>",
					"");
			// Update getSentenceHolder()
			dataholderHandler.getSentenceHolder().get(i).setSentence(sentence);

			// noun rule 0.5: Meckle#s cartilage

			Set<String> nouns0 = this
					.getNounsMecklesCartilage(originalSentence);
			nouns.addAll(nouns0);
			sentence = sentence.replaceAll("#", "");
			// Update getSentenceHolder()
			dataholderHandler.getSentenceHolder().get(i).setSentence(sentence);

			// noun rule 2: end of sentence nouns
			// (a|an|the|some|any|this|that|those|these) noun$
			Set<String> nouns2 = this.getNounsRule2(originalSentence);
			nouns.addAll(nouns2);

			// noun rule 3: proper nouns and acronyms
			String copy = originalSentence;
			Set<String> nouns_temp = this.getNounsRule3Helper(copy);
			Iterator<String> iter = nouns_temp.iterator();
			while (iter.hasNext()) {
				String token = iter.next();
				if (token.matches("^.*[A-Z].+$")
						&& (!token.matches("^.*-\\w+ed$"))) {
					if (token.matches("^[A-Z0-9]+$")) {
						token = token.toLowerCase();
						anouns.add(token);
					} else {
						token = token.toLowerCase();
						pnouns.add(token);
					}
					nouns.add(token);
				}
			}

			// noun rule 1: sources with 1 _ are character statements, 2 _ are
			// descriptions
			Set<String> nouns1 = getNounsRule1(dataholderHandler, source,
					originalSentence, descriptorMap);
			nouns.addAll(nouns1);

			// noun rule 4: non-stop/prep followed by a number: epibranchial 4
			// descriptor heuristics
			Set<String> nouns4 = this.getNounsRule4(originalSentence);
			nouns.addAll(nouns4);

			// remove puncts for descriptor rules
			originalSentence = StringUtility.removePunctuation(
					originalSentence, "-");
			// System.out.println("oSent:");
			// System.out.println(originalSentence);

			// Descriptor rule 1: single term descriptions are descriptors
			descriptors.addAll(this.getDescriptorsRule1(source,
					originalSentence, nouns));

			// Descriptor rule 2: (is|are) red: isDescriptor
			descriptors.addAll(this.getDescriptorsRule2(dataholderHandler,
					originalSentence, descriptorMap));
		}

		nouns = this.filterOutDescriptors(nouns, descriptors);
		anouns = this.filterOutDescriptors(anouns, descriptors);
		pnouns = this.filterOutDescriptors(pnouns, descriptors);

		dataholderHandler.add2HeuristicNounTable(nouns, "organ");
		dataholderHandler.add2HeuristicNounTable(anouns, "acronyms");
		dataholderHandler.add2HeuristicNounTable(pnouns, "propernouns");
		dataholderHandler.add2HeuristicNounTable(taxonNames, "taxonnames");

		nouns.addAll(anouns);
		nouns.addAll(pnouns);
		nouns.addAll(taxonNames);

		List<Set<String>> results = new LinkedList<Set<String>>();
		results.add(nouns);
		results.add(descriptors);

		return results;
	}

	/**
	 * filter out descriptors from nouns, and return remaining nouns
	 * 
	 * @param rNouns
	 *            set of nouns
	 * @param rDescriptors
	 *            set of descriptors
	 * @return set of nouns that are not descriptors
	 */
	public Set<String> filterOutDescriptors(Set<String> rNouns,
			Set<String> rDescriptors) {
		Set<String> filtedNouns = new HashSet<String>();

		Iterator<String> iter = rNouns.iterator();
		while (iter.hasNext()) {
			String noun = iter.next();
			noun = noun.toLowerCase();

			Pattern p = Pattern.compile(
					"\\b(" + this.myLearnerUtility.getConstant().PREPOSITION
							+ "|" + this.myLearnerUtility.getConstant().STOP
							+ ")\\b", Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(noun);

			if ((!m.lookingAt()) && (!rDescriptors.contains(noun))) {
				filtedNouns.add(noun);
			}
		}
		return filtedNouns;
	}

	/**
	 * Nouns rule 0: get <i></i> enclosed taxon names
	 * 
	 * @param oSent
	 * @return
	 */
	public Set<String> getTaxonNameNouns(String oSent) {
		Set<String> taxonNames = new HashSet<String>();
		String regex = "(.*?)<\\s*i\\s*>\\s*([^<]*)\\s*<\\s*\\/\\s*i\\s*>(.*)";
		String copy = oSent;

		while (true) {
			Matcher matcher = Pattern.compile(regex).matcher(copy);
			if (matcher.lookingAt()) {
				String taxonName = matcher.group(2);
				if (taxonName.length() > 0) {
					taxonNames.add(taxonName);
					String[] taxonNameArray = taxonName.split("\\s+");
					for (int i = 0; i < taxonNameArray.length; i++) {
						taxonNames.add(taxonNameArray[i]);
					}
					copy = matcher.group(3);
				} else {
					break;
				}
			} else {
				break;
			}
		}

		return taxonNames;
	}

	/**
	 * Nouns rule 0.5: Meckle#s cartilage
	 * 
	 * @param oSent
	 * @return
	 */
	public Set<String> getNounsMecklesCartilage(String oSent) {
		Set<String> nouns = new HashSet<String>();
		String regex = "^.*\\b(\\w+#s)\\b.*$";
		Matcher m = Pattern.compile(regex).matcher(oSent);
		if (m.lookingAt()) {
			String noun = "";
			noun = m.group(1);

			noun = noun.toLowerCase();
			nouns.add(noun);

			noun = noun.replaceAll("#", "");
			nouns.add(noun);

			noun = noun.replaceAll("s$", "");
			nouns.add(noun);
		}

		return nouns;
	}

	/**
	 * 
	 * @param source
	 * @param originalSentence
	 * @param descriptorMap
	 * @return
	 */
	public Set<String> getNounsRule1(DataHolder dataholderHandler,
			String source, String originalSentence,
			Map<String, Boolean> descriptorMap) {
		Set<String> nouns = new HashSet<String>();

		if ((!(source.matches("^.*\\.xml_\\S+_.*$")))
				&& (!(originalSentence.matches("^.*\\s.*$")))) {
			if (!this.isDescriptor(dataholderHandler, originalSentence,
					descriptorMap)) {
				originalSentence = originalSentence.toLowerCase();
				nouns.add(originalSentence);
			}
		}

		return nouns;
	}

	/**
	 * 
	 * @param oSent
	 * @return
	 */
	public Set<String> getNounsRule2(String oSent) {
		String copy = oSent;
		String regex = "(.*?)\\b(a|an|the|some|any|this|second|third|fourth|fifth|sixth|seventh|eighth|ninth|tenth) +(\\w+)\\s*($|\\(|\\[|\\{|\\b"
				+ this.myLearnerUtility.getConstant().PREPOSITION + "\\b)(.*)";
		Set<String> nouns = new HashSet<String>();

		while (true) {
			if (copy == null) {
				break;
			}
			Matcher m = Pattern.compile(regex).matcher(copy);
			if (m.lookingAt()) {
				String t = m.group(3);
				String prep = m.group(4);
				copy = m.group(5);

				if (prep.matches("^.*\\w.*$")
						&& t.matches("^.*\\b(length|width|presence|\\w+tion)\\b.*$")) {
					continue;
				}
				t = t.toLowerCase();
				nouns.add(t);
			} else {
				break;
			}
		}

		return nouns;
	}

	/**
	 * 
	 * @param sentence
	 * @return
	 */
	public Set<String> getNounsRule3Helper(String sentence) {
		Set<String> nouns = new HashSet<String>();

		String[] segs = sentence.split("[()\\[\\]\\{\\}]");
		for (int i1 = 0; i1 < segs.length; i1++) {
			String seg = segs[i1];
			seg = StringUtility.removePunctuation(seg, "-");
			String[] tokens = seg.split("\\s+");

			// #ignore the first word in character statements--this is normally
			// capitalized
			for (int j = 1; j < tokens.length; j++) {
				String token = tokens[j];
				if (token.matches("^.*[A-Z].+$")
						&& (!token.matches("^.*-\\w+ed$"))) {
					nouns.add(token);
				}
			}
		}

		return nouns;
	}

	/**
	 * noun rule 4: non-stop/prep followed by a number: epibranchial 4
	 * descriptor heuristics
	 * 
	 * @param oSent
	 * @return a set of nouns
	 */
	public Set<String> getNounsRule4(String oSent) {
		Set<String> nouns = new HashSet<String>();

		String copy = oSent;
		String regex = "(.*?)\\s(\\w+)\\s+\\d+(.*)";

		while (true) {
			if (copy == null) {
				break;
			}
			Matcher m = Pattern.compile(regex).matcher(copy);
			if (m.lookingAt()) {
				String t = m.group(2);
				copy = m.group(3);
				String regex2 = "\\b("
						+ this.myLearnerUtility.getConstant().PREPOSITION + "|"
						+ this.myLearnerUtility.getConstant().STOP + ")\\b";
				if (!t.matches(regex2)) {
					t = t.toLowerCase();
					nouns.add(t);
				}
			} else {
				break;
			}
		}

		return nouns;
	}

	/**
	 * 
	 * @param source
	 * @param sentence
	 * @param nouns
	 * @return
	 */
	public Set<String> getDescriptorsRule1(String source, String sentence,
			Set<String> nouns) {
		Set<String> descriptors = new HashSet<String>();
		// single word
		if (source.matches("^.*\\.xml_\\S+_.*$")
				&& (!sentence.matches("^.*\\s.*$"))) {
			Iterator<String> iter = nouns.iterator();
			boolean isExist = false;
			while (iter.hasNext()) {
				String noun = iter.next();
				if (noun.equals(sentence)) {
					isExist = true;
					break;
				}
			}
			if (isExist == false) {
				sentence = sentence.toLowerCase();
				descriptors.add(sentence);
			}
		}

		return descriptors;
	}

	/**
	 * (is|are) red: isDescriptor
	 * 
	 * @param oSent
	 * @return
	 */
	public Set<String> getDescriptorsRule2(DataHolder dataholderHandler,
			String sentence, Map<String, Boolean> descriptorMap) {
		Set<String> descriptors = new HashSet<String>();

		String[] tokens = sentence.split("\\s+");

		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			token = token.toLowerCase();
			if (isDescriptor(dataholderHandler, token, descriptorMap)) {
				token = token.toLowerCase();
				descriptors.add(token);
			}
		}

		return descriptors;
	}

	/**
	 * Check if the term is a descriptor
	 * 
	 * @param term
	 * @param descriptorMap
	 *            descriptors have already learned
	 * @return a boolean value indicating whether the term is a descriptor. This
	 *         result will be stored in the descriptorMap for future use
	 */
	public boolean isDescriptor(DataHolder dataholderHandler, String term,
			Map<String, Boolean> descriptorMap) {
		if (descriptorMap.containsKey(term)) {
			if (descriptorMap.get(term).booleanValue()) {
				return true;
			} else {
				return false;
			}
		} else {
			for (int i = 0; i < dataholderHandler.getSentenceHolder().size(); i++) {
				String originalSentence = dataholderHandler.getSentenceHolder()
						.get(i).getOriginalSentence();
				if (isMatched(originalSentence, term, descriptorMap)) {
					return true;
				}
			}
			term = term.toLowerCase();
			descriptorMap.put(term, false);
			return false;
		}

	}

	/**
	 * Check if the term matches the sentence
	 * 
	 * @param sentence
	 * @param term
	 * @param descriptorMap
	 * @return a boolean value indicating whether the term matches the sentence
	 */
	public boolean isMatched(String sentence, String term,
			Map<String, Boolean> descriptorMap) {
		if (sentence.matches("^.*" + " (is|are|was|were|be|being) " + term
				+ ".*$")) {
			term = term.toLowerCase();
			descriptorMap.put(term, true);
			return true;
		} else {
			return false;
		}
	}

}
