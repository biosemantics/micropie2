package semanticMarkup.ling.learn.knowledge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.SentenceStructure;
import semanticMarkup.ling.learn.dataholder.WordPOSKey;
import semanticMarkup.ling.learn.dataholder.WordPOSValue;
import semanticMarkup.ling.learn.utility.LearnerUtility;
import semanticMarkup.ling.learn.utility.StringUtility;


/**
 * Convert plural forms of annotations of tag (and modifier depending on the
 * learning mode) to singular form, to avoid count the same word of an organ
 * twice.
 * 
 * @author Dongye
 * 
 */
public class AnnotationNormalizer implements IModule {
	private String learningMode;
	Map<String, Boolean> checkedModifiers;
	private LearnerUtility myLearnerUtility;
	
	public AnnotationNormalizer(String lMode, Map<String, Boolean> cModifiers, LearnerUtility learnerUtility) {
		this.learningMode = lMode;
		this.checkedModifiers = cModifiers;
		this.myLearnerUtility = learnerUtility;
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		Logger myLogger = Logger.getLogger("Learn");
		if (StringUtils.equals(this.learningMode, "plain")) {
			myLogger.info("Normalize modifiers");
			this.normalizeModifiers(dataholderHandler);
		}
		
		myLogger.info("Final step: normalize tag and modifiers");
		this.normalizeTags(dataholderHandler);
		
	}
	
	/**
	 * Remove <b> from modifiers
	 * 
	 * @param dataholderHandler
	 */
	public void normalizeModifiers(DataHolder dataholderHandler) {
		Comparator<SentenceStructure> stringLengthComparator = new Comparator<SentenceStructure>() {
			@Override
			public int compare(SentenceStructure s1, SentenceStructure s2) {
				String m1 = s1.getModifier();
				String m2 = s2.getModifier();
				if (m1.length() == m2.length()) {
					return 0;
				} else {
					return m1.length() < m2.length() ? -1 : 1;
				}
			}
		};

		// Part 1
		// non- and/or/to/plus cases
		List<SentenceStructure> sentenceList = new ArrayList<SentenceStructure>();
		for (SentenceStructure sentenceItem : dataholderHandler
				.getSentenceHolder()) {
			String modifier = sentenceItem.getModifier();
			boolean c1 = !StringUtils.equals(modifier, "");
			boolean c2 = !StringUtility.isMatchedNullSafe(modifier,
					" (and|or|nor|plus|to) ");
			if (c1 && c2) {
				sentenceList.add(sentenceItem);
			}
		}

		Collections.sort(sentenceList, stringLengthComparator);
		Collections.reverse(sentenceList);

		for (SentenceStructure sentenceItem : sentenceList) {
			int sentenceID = sentenceItem.getID();
			String sentence = sentenceItem.getSentence();
			String tag = sentenceItem.getTag();
			String modifier = sentenceItem.getModifier();

			String mCopy = "" + modifier;
			modifier = finalizeModifier(dataholderHandler, modifier, tag, sentence);
			modifier = modifier.replaceAll("\\s*\\[.*?\\]\\s*", " ");
			modifier = StringUtility.trimString(modifier);

			if (!StringUtils.equals(mCopy, modifier)) {
				dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
						modifier, tag, "normalizemodifiers");
			}
		}

		// Part 2
		// deal with to: characterA to characterB organ (small to median shells)
		List<SentenceStructure> sentenceList2 = new ArrayList<SentenceStructure>();
		for (SentenceStructure sentenceItem : dataholderHandler
				.getSentenceHolder()) {
			String modifier = sentenceItem.getModifier();
			boolean c1 = StringUtility.isMatchedNullSafe(modifier, " to ");
			if (c1) {
				sentenceList2.add(sentenceItem);
			}
		}

		Collections.sort(sentenceList2, stringLengthComparator);
		for (SentenceStructure sentenceItem : sentenceList2) {
			int sentenceID = sentenceItem.getID();
			String sentence = sentenceItem.getSentence();
			String tag = sentenceItem.getTag();
			String modifier = sentenceItem.getModifier();

			String mCopy = "" + modifier;
			modifier = modifier.replaceAll(".*? to ", "");
			List<String> mWords = new ArrayList<String>(Arrays.asList(modifier
					.split("\\s+")));
			Collections.reverse(mWords);

			String m = "";
			int count = dataholderHandler.getSentenceCount(true, m, true, tag);
			String modi = "" + m;
			for (String word : mWords) {
				m = word + " " + m;
				m = m.replaceAll("\\s+$", "");
				int c = dataholderHandler.getSentenceCount(true, m, true, tag);
				if (c > count) {
					count = c;
					modi = "" + m;
				}
			}
			// tagsentwmt($sentid, $sentence, $modi, $tag,
			// "normalizemodifiers");
			dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modi,
					tag, "normalizemodifiers");
		}

		// Part 3
		// modifier with and/or/plus
		List<SentenceStructure> sentenceList3 = new ArrayList<SentenceStructure>();
		for (SentenceStructure sentenceItem : dataholderHandler
				.getSentenceHolder()) {
			String modifier = sentenceItem.getModifier();
			boolean con = !StringUtility.isMatchedNullSafe(modifier,
					" (and|or|nor|plus|to) ");
			if (con) {
				sentenceList3.add(sentenceItem);
			}
		}

		Collections.sort(sentenceList3, stringLengthComparator);
		Collections.reverse(sentenceList3);

		for (SentenceStructure sentenceItem : sentenceList3) {
			int sentenceID = sentenceItem.getID();
			String sentence = sentenceItem.getSentence();
			String tag = sentenceItem.getTag();
			String modifier = sentenceItem.getModifier();

			String mCopy = "" + modifier;
			modifier = this.finalizeCompoundModifier(dataholderHandler,
					modifier, tag, sentence);

			modifier = modifier.replaceAll("\\s*\\[.*?\\]\\s*", " ");
			modifier = StringUtility.trimString(modifier);

			if (!StringUtils.equals(mCopy, modifier)) {
				// tagsentwmt($sentid, $sentence, $modifier, $tag,
				// "normalizemodifiers");
				dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
						modifier, tag, "normalizemodifiers");
			}
		}

		// Part 4
		// modifier with and/or/plus
		List<SentenceStructure> sentenceList4 = new ArrayList<SentenceStructure>();
		for (SentenceStructure sentenceItem : dataholderHandler
				.getSentenceHolder()) {
			String modifier = sentenceItem.getModifier();
			// ???
			boolean con = !StringUtility.isMatchedNullSafe(modifier,
					"[_ ](and|or|nor|plus|to)[ _]");
			if (con) {
				sentenceList4.add(sentenceItem);
			}
		}

		Collections.sort(sentenceList4, stringLengthComparator);
		Collections.reverse(sentenceList4);

		for (SentenceStructure sentenceItem : sentenceList4) {
			int sentenceID = sentenceItem.getID();
			String sentence = sentenceItem.getSentence();
			String tag = sentenceItem.getTag();
			String modifier = sentenceItem.getModifier();

			String mTag = "" + tag;
			tag = this.finalizeCompoundTag(tag, sentence);
			tag = tag.replaceAll("\\s*\\[.*?\\]\\s*", " ");
			tag = StringUtility.trimString(tag);

			if (!StringUtils.equals(mTag, tag)) {
				// tagsentwmt($sentid, $sentence, $modifier, $tag,
				// "normalizemodifiers");
				dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
						modifier, tag, "normalizemodifiers");
			}
		}
	}
	
	public String finalizeCompoundModifier(DataHolder dataholderHandler, String modifier, String tag,
			String sentence) {
		// case 1
		if (StringUtility.isMatchedNullSafe(modifier, "\\[")) {
			return modifier;
		}
		
		modifier = modifier.replaceAll("\\(.*?\\)", " ");
		modifier = modifier.replaceAll("\\(.*", "");
		modifier = modifier.replaceAll("\\W","");
		modifier = modifier.replaceAll("\\s+", " ");
		
		String mCopy = ""+modifier;
		String result = "";
		String m = "";
				String n = "";
				
		List<String> lastPart = new ArrayList(Arrays.asList(modifier.split("\\s+")));
		Collections.reverse(lastPart);
		int cut = 0;		
		for (String l : lastPart) {
			if (cut == 0 && StringUtility.isMatchedNullSafe(sentence, "<N>"+l)) {
				n = l + " " + n;
				n = StringUtility.trimString(n);
			}
			else {
				cut = 1;
				String tm = StringUtility.isMatchedNullSafe(n, "\\w") ? l + " "
						+ n : l;
				for (SentenceStructure sentenceItem : dataholderHandler
						.getSentenceHolder()) {
					if (StringUtils.equals(sentenceItem.getModifier(), tm)
							&& StringUtils.equals(sentenceItem.getTag(), tag)) {
						m = l + " " + m;
					}
				}
				break;
			}
		}
		
		m = StringUtility.trimString(m);
		n = StringUtility.trimString(n);
		modifier = modifier.replaceAll("\\s*"+n, "");
		
		// components
		List<String> parts = new ArrayList<String>();
		List<String> conj = new ArrayList<String>();
		conj.add("");
		if (modifier != null) {
			Matcher m1 = StringUtility.createMatcher(modifier, "(^.*?) (and|or|nor|plus) (.*)");
			while (m1.find()) {
				String g1 = m1.group(1);
				String g2 = m1.group(2);
				String g3 = m1.group(3);
				parts.add(g1);
				parts.add(g2);
				modifier = g3;
				m1 = StringUtility.createMatcher(modifier, "(^.*?) (and|or|nor|plus) (.*)");
			}
		}
		parts.add(modifier);
		
		// at least one m in a part
//		for (String part : parts) {
		for (int i = 0; i < parts.size(); i++) {
			String part = parts.get(i);
			String[] words = part.split("\\s+");
			boolean isFound = false;
			String r = "";
			
			for (String word : words) {
				if ((this.checkedModifiers.containsKey(word) && this.checkedModifiers.get(word)) || StringUtility.isMatchedNullSafe(sentence, "<N>"+word)) {
					isFound = true;
					r = r + " " + word;
				}
			}
			r = StringUtility.trimString(r);
			
			result = result + " " + conj.get(i)+ " "+r;
			String regex2 = "\\b(" + this.myLearnerUtility.getConstant().CHARACTER + "|" + this.myLearnerUtility.getConstant().STOP
					+ "|" + this.myLearnerUtility.getConstant().NUMBER + "|" + this.myLearnerUtility.getConstant().CLUSTERSTRING
					+ ")\\b";
			if (!StringUtility.isMatchedNullSafe(r, "\\w")
					|| StringUtility.isMatchedNullSafe(r, regex2)) {
				result = "";
				break;
			}
		}
		result = StringUtility.isMatchedNullSafe(result, "\\w") ? result
				+ " " + n : m + " " + n;
		result = StringUtility.trimString(result);
		
		return result;
	}

	// [bm]+n+&[bm]+n+
	public String finalizeCompoundTag(String tag, String sentence) {
		// avoid unmatched ( in regexp
		tag = tag.replaceAll("\\(.*?\\)", " ");
		tag = tag.replaceAll("\\(.*", "");
		tag = tag.replaceAll("\\s+", " ");
		
		String tCopy = "" + tag;
		String result = "";
		
		// components
		List<String> parts = new ArrayList<String>();
		List<String> conj = new ArrayList<String>();
		conj.add("");
		
		Matcher m1 = StringUtility.createMatcher(tag, "(^.*?)[_ ](and|or|nor|plus)[_ ](.*)");
		while (m1.find()) {
			String g1 = m1.group(1);
			String g2 = m1.group(2);
			String g3 = m1.group(3);
			parts.add(g1);
			conj.add(g2);
			tag = g3;
			m1 = StringUtility.createMatcher(tag, "(^.*?)[_ ](and|or|nor|plus)[_ ](.*)");
		}
		
		parts.add(tag);
		
		// at least one m in a part
//		for (String part : parts) {
		for (int i = 0; i < parts.size(); i++) {
			String part = parts.get(i);
			String[] words = part.split("\\s+");
			boolean isFoundM = false;
			String r = "";
			for (String word : words) {
				String escapedW = StringUtility.escapePerlRegex(word);
				if ((this.checkedModifiers.containsKey(word) && this.checkedModifiers
						.get(word))
						|| StringUtility.isMatchedNullSafe(sentence, "<N>"
								+ escapedW)) {
					isFoundM = true;
					r = r + " " + word;
				}
			}
			String regex = "\\b(" + this.myLearnerUtility.getConstant().CHARACTER + "|" + this.myLearnerUtility.getConstant().STOP
					+ "|" + this.myLearnerUtility.getConstant().NUMBER + "|" + this.myLearnerUtility.getConstant().CLUSTERSTRING
					+ ")\\b";
			r = r.replaceAll(regex, "");
			r = StringUtility.trimString(r);
			
			if (StringUtility.isMatchedNullSafe(r, "\\w")) {
				result = result + " " + conj.get(i) +" "+r;
			}
		}
		
		result = result.replaceAll("\\s+", " ");
		result = StringUtility.trimString(result);
		
		return result;
	}

	public String finalizeModifier(DataHolder dataholderHandler, String modifier, String tag, String sentence) {
		String fModifier = "";
		modifier = modifier.replaceAll("\\[.*?\\]", "");
		modifier = StringUtility.trimString(modifier);
		if (StringUtility.isMatchedNullSafe(modifier, "\\w")) {
			List<String> mWords = new ArrayList<String>(Arrays.asList(modifier.split("\\s+")));
			Collections.reverse(mWords);
			
			for (String mWord : mWords) {
				boolean isModifier = this.isModifier(dataholderHandler, mWord, modifier, tag);
				if (isModifier) {
					fModifier = mWord + " " + fModifier;
				}
				else {
					break;
				}
			}
			
			fModifier = fModifier.replaceAll("\\s+", "");
		}
		
		return fModifier;
	}
	

	public boolean isModifier(DataHolder dataholderHandler, String word, String modifier, String tag) {
		if (this.checkedModifiers.containsKey(word)) {
			if (this.checkedModifiers.get(word)) {
				return true;
			} else {
				return false;
			}
		}
		
		// if word is a "s", return 1
		Set<String> nouns = new HashSet<String>(Arrays.asList("s p n"
				.split(" ")));
		List<Entry<WordPOSKey, WordPOSValue>> entries = dataholderHandler
				.getWordPOSEntriesByWordPOS(word, nouns);
		if (entries.size() > 0) {

			this.checkedModifiers.put(word, true);
			return true;

		}
		
		// if word is a "b", and not a "m", return 0
		Set<String> bPOS = new HashSet<String>();
		bPOS.add("b");
		List<Entry<WordPOSKey, WordPOSValue>> boundaries = dataholderHandler
				.getWordPOSEntriesByWordPOS(word, bPOS);
		boolean c1 = (boundaries.size() > 0);		
		boolean c2 = dataholderHandler.getModifierHolder().containsKey(word);
		if (c1 && !c2) {
			// the word is a boundary word, but not a modifier
			this.checkedModifiers.put(word, false);
			return false;
		}
		
		if (!c1 && c2) {
			this.checkedModifiers.put(word, true);
			return true;
		}
		
		// when word has been used as "b" and "m" or neither "b" nor "m" and is not a "s"
		int mCount = this.getMCount(dataholderHandler, word);
		String wCopy = ""+word;
		if (StringUtility.isMatchedNullSafe(word, "_")) {
			wCopy = wCopy.replaceAll("_", " - ");
		}
		
		int tCount = 0;
		String pattern = "(^| )"+wCopy+" ";
		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
			String oSentence = sentenceItem.getOriginalSentence();
			if (StringUtility.isMatchedNullSafe(oSentence, pattern)) {
				tCount++;
			}
		}
		
		if (tCount == 0 || tCount > 0.25 * mCount) {
			this.checkedModifiers.put(word, false);
			return false;
		}
		else {
			this.checkedModifiers.put(word, true);
			return true;			
		}
	}
	
	
	public int getMCount(DataHolder dataholderHandler, String word) {
		int count = 0;
		String pattern = "(>| )"+word+"(</B></M>)? <N";
		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
			String sentence = sentenceItem.getSentence();
			if (StringUtility.isMatchedNullSafe(sentence, pattern)) {
				count++;
			}
		}
		
		return count;
	}
	
	
	/**
	 * Turn all tags and modifiers to singular form; Remove <NBM> tags from the
	 * sentences.
	 * 
	 * @param dataholderHandler
	 */
	public void normalizeTags(DataHolder dataholderHandler) {
		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
			int sentenceID = sentenceItem.getID();
			String modifier = sentenceItem.getModifier();
			String tag = sentenceItem.getTag();
			if (tag != null && StringUtils.equals(tag, "ignore")) {				
				tag = this.normalizeItem(tag);
				modifier = this.normalizeItem(modifier);
			}
			
			String sentence = sentenceItem.getSentence();
			sentence = sentence.replaceAll("</?[NBM]>", "");
			dataholderHandler.getSentence(sentenceID).setSentence(sentence);
			if (StringUtility.isMatchedNullSafe(tag, "\\w")) {
				dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modifier, tag, "normalizetags");
			}
			else {
				dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modifier, null, "normalizetags");
			}
		}
	}
	
	

	public String normalizeItem(String tag) {
		tag = tag.replaceAll("\\s*NUM\\s*", " ");
		tag = StringUtility.trimString(tag);

		if (StringUtility.isMatchedNullSafe(tag, "\\w")) {
			tag = tag.replaceAll("\\[", "[*");
			tag = tag.replaceAll("\\]", "*]");

			String[] twSegs = tag.split("[\\]\\[]");

			StringBuilder tagSB = new StringBuilder();

			for (int j = 0; j < twSegs.length; j++) {
				StringBuilder outSB = new StringBuilder();
				// case 1
				if (StringUtility.isMatchedNullSafe(twSegs[j], "\\*")) {
					twSegs[j] = twSegs[j].replaceAll("\\*", "");
					String[] tagWords = twSegs[j].split("\\s+");
					outSB.append('[');
					for (int i = 0; i < tagWords.length; i++) {
						tagWords[i] = this.myLearnerUtility
								.getWordFormUtility().getSingular(tagWords[i]);
						outSB.append(tagWords[i]);
						outSB.append(" ");
					}
					outSB.deleteCharAt(outSB.length() - 1);
					outSB.append(']');
				} 
				// case 2
				else if (StringUtility.isMatchedNullSafe(twSegs[j], "\\w")) {
					String[] tagWords = twSegs[j].split("\\s+");
					for (int i = 0; i < tagWords.length; i++) {
						tagWords[i] = this.myLearnerUtility
								.getWordFormUtility().getSingular(tagWords[i]);
						outSB.append(tagWords[i]);
						outSB.append(" ");
					}
					outSB.deleteCharAt(outSB.length() - 1);
				}
				String out = outSB.toString();
				if (StringUtility.isMatchedNullSafe(out, "\\w")) {
					tagSB.append(out.toString());
					tagSB.append(' ');
				}
			}

			tagSB.deleteCharAt(tagSB.length() - 1);
			tag = tagSB.toString();
			tag = tag.replaceAll("\\s+", " ");
		}

		return tag;
	}

}
