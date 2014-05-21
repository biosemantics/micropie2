package semanticMarkup.ling.learn.knowledge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import semanticMarkup.ling.learn.auxiliary.POSInfo;
import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.SentenceStructure;
import semanticMarkup.ling.learn.utility.LearnerUtility;
import semanticMarkup.ling.learn.utility.StringUtility;

/**
 * Find Modifier/Organ for the same Ox: M1 Ox, M2 Ox Example: inner phyllaries, middle phyllaries
#Find Mx/Oy where Ox != Oy Example: inner florets
# ==>inner/middle = type modifier
# Find TM C (character) patterns => TM = adjective nouns
# outer and middle => outer is adject noun
# outer and mid => mid is adject noun
#===> infer more boundary words/structure: outer [ligules], inner [fertile]
 * @author Dongye
 *
 */
public class AdjectiveSubjectBootstrappingLearner implements IModule {
	private LearnerUtility myLearnerUtility;
	private String learningMode;
	private int maxTagLength;
	
	public AdjectiveSubjectBootstrappingLearner(LearnerUtility learnerUtility, String learningMode, int maxTagLength) {
		this.myLearnerUtility = learnerUtility;
		this.learningMode = learningMode;
		this.maxTagLength = maxTagLength;
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		if (StringUtils.equals(this.learningMode, "adj")) {
//			myLogger.info("Bootstrapping on adjective subjects");
			 adjectiveSubjectBootstrapping(dataholderHandler, this.maxTagLength);
		} else {
			int v = 0;
			do {
				v = 0;
				this.handleAndOr(dataholderHandler);
			} while (v > 0);
		}
	}
	
	public void adjectiveSubjectBootstrapping(DataHolder dataholderHandler, int maxTagLength) {
		int flag = 0;
		int count = 0;
		
		do {
			// tag all sentences
			this.myLearnerUtility.tagAllSentences(dataholderHandler, "singletag", "sentence");
			
			// adjective subject markup: may discover new modifier, new boundary, and new nouns
			int res1 = this.adjectiveSubjects(dataholderHandler);
			flag += res1;
			
			// work on tag='andor' clauses, move to the main bootstrapping
			int res2 = discoverNewModifiers(dataholderHandler);
			flag += res2;
			
			int res3 = this.handleAndOr(dataholderHandler);
			flag += res3;			
			dataholderHandler.untagSentences();
			
			int res4 = this.myLearnerUtility.doItMarkup(dataholderHandler, maxTagLength);
			
		} while (flag > 0);
		
		// reset unsolvable andor to NULL
		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
			String tag = sentenceItem.getTag();
			if (StringUtils.equals(tag, "andor")) {
				sentenceItem.setTag(null);
			}
		}
		
		// cases releazed from andor[m&mn] may be marked by adjectivesubjects
		this.myLearnerUtility.tagAllSentences(dataholderHandler, "singletag", "sentence");
		this.adjectiveSubjects(dataholderHandler);
	}
	
	/**
	 * works on annotated sentences that starts with a M in all non-ignored
	 * sentences, find sentences that starts with a modifer <m> followed by a
	 * boundary word <b>. (note, if the <B> is a punct mark, this sentence
	 * should be tagged as ditto) Use the context to find the tag, use the
	 * modifier as the modifie (markup process, no new discovery). for
	 * "modifier unknown" pattern, check WNPOS of the "unknown" to decide if
	 * "unknown" is a structure name (if it is a pl) or a boundary word (may
	 * have new discoveries). Works on sentences, not leads
	 * 
	 * @param dataholderHandler
	 * @return # of updates
	 */
	public int adjectiveSubjects(DataHolder dataholderHandler) {
		Set<String> typeModifiers = new HashSet<String>();
		
		// Part 1: collect evidence for the usage of "modifier boundry":
		typeModifiers = adjectiveSubjectsPart1(dataholderHandler, typeModifiers);
		
		for (String typeModifier : typeModifiers) {
			if (dataholderHandler.getModifierHolder().containsKey(typeModifier)) {
				dataholderHandler.getModifierHolder().get(typeModifier)
						.setIsTypeModifier(true);
			}
		}
		
		// Part 2: process "typemodifier unknown" patterns
		int flag = adjectiveSubjectsPart2(dataholderHandler, typeModifiers);
		
		return flag;		
	}
	
	public Set<String> adjectiveSubjectsPart1(DataHolder dataholderHandler, Set<String> typeModifiers) {
		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
			String sentenceCopy = ""+sentenceItem.getSentence();
			String tag = sentenceItem.getTag();
			
			if (!StringUtils.equals(tag, "ignore") || tag == null) {
				Pattern p = Pattern.compile(".*?<M>(\\S+)</M> <B>[^,.]+</B> (.*)");
				Matcher m = p.matcher(sentenceCopy);
				while (m.find()) {
					sentenceCopy = m.group(2);
					String temp = m.group(1);
					temp = temp.replaceAll("<\\S+?>", "");
					if (!typeModifiers.contains(temp)) {
						typeModifiers.add(temp);
					}
				}
			}
					
		}
		
		return typeModifiers;

	}
	
	public int adjectiveSubjectsPart2(DataHolder dataholderHandler,
			Set<String> typeModifiers) {
		String pos = null;
		int flag = 0;
		
		
		for (SentenceStructure sentenceItem : dataholderHandler
				.getSentenceHolder()) {
			int sentenceID = sentenceItem.getID();
			String sentence = sentenceItem.getSentence();
			String tag = sentenceItem.getTag();
			String pattern = "<M>\\S*(" + StringUtils.join(typeModifiers, "|")
					+ ")\\S*</M> .*";
			int count = 0;
			
			if (((tag == null) || StringUtils.equals(tag, "") || StringUtils
					.equals(tag, "unknown"))
					&& adjectiveSubjectsPart2Helper1(sentence, typeModifiers)) {
				
				
				if (sentence != null) {
					String sentenceCopy = sentence + "";
					String regex = "(.*?)((?:(\\S+)\\s*(?:and|or|nor|and / or|or / and)\\s*)*(?:<M>\\S+</M>\\s*)+) (\\S+)\\s*(.*)";
					Pattern p = Pattern.compile(regex);
					Matcher m = p.matcher(sentenceCopy);
					while (m.find()) {
						int knownPOS = 0;
						String start = m.group(1);
						String modifier = m.group(2);
						String newModifier = m.group(3);
						String word = m.group(4);
						sentenceCopy = m.group(5);

						// case 1
						if (!this.myLearnerUtility.getConstant().forbiddenWords
								.contains(word)) {
							count++;
							continue;
						}

						// case 2
						if (StringUtility.isMatchedNullSafe(
								newModifier.toUpperCase(), "<N>")
								|| StringUtility.isMatchedNullSafe(
										start.toUpperCase(), "<N>")) {
							count++;
						continue;
						}
						
						// case 3
						boolean c3 = this.myLearnerUtility.getConstant().prepositionWords.contains(word);
						if (count == 0 
								&& ((StringUtility.isMatchedNullSafe(word, "[;,]") || c3) 
										|| (StringUtility.isMatchedNullSafe(word, "[.;,]") 
												&& !StringUtility.isMatchedNullSafe(sentence, "\\w")))) {
							// case 3.1
							// start with a <[BM]>, followed by a <[BM]>
							if ((StringUtility.isMatchedNullSafe(word,
									"\\b(with|without|of)\\b"))
									&& ((StringUtility.isMatchedNullSafe(modifier,
													"^(<M>)?<B>(<M>)?\\w+(</M)?</B>(</M>)? (?:and|or|nor|and / or|or / and)?\\s*(<[BM]>)+\\w+(</[BM]>)+\\s*$")) 
									|| (StringUtility.isMatchedNullSafe(modifier, "^(<[BM]>)+\\w+(</[BM]>)+$")))) { 
								dataholderHandler.tagSentenceWithMT(sentenceID,
										sentenceCopy, "", "ditto",
										"adjectivesubject[ditto]");
								count++;
								continue;
							} 
							// case 3.2
							// modifier={<M>outer</M> <M><B>pistillate</B></M>} word= <B>,</B> sentence= <N>corollas</N>....
							// make the last modifier b
							else {
								if (modifier != null) {
									Pattern p2 = Pattern
											.compile("^(.*) (\\S+)$");
									Matcher m2 = p2.matcher(modifier);
									if (m2.find()) {
										modifier = m2.group(1);
										String b = m2.group(2);
										String bCopy = "" + b;
										b = b.replaceAll("<\\S+?>", "");
										dataholderHandler.updateDataHolder(b,"b", "", "wordpos", 1);
										tag = dataholderHandler.getParentSentenceTag(sentenceID);
										List<String> modifierAndTag = 
												dataholderHandler.getMTFromParentTag(tag);
										String modifier2 = modifierAndTag.get(0);
										tag = modifierAndTag.get(1);
										modifier = modifier.replaceAll(
												"<\\S+?>", "");
										if (StringUtility.isMatchedNullSafe(modifier2, "\\w")) {
											modifier = modifier + " " + modifier2;
										}
										dataholderHandler.tagSentenceWithMT(
												sentenceID, sentence, modifier,
												tag, "adjectivesubject[M-B,]");
										count++;
										continue;
									}
								}
							}
						}
						
						// case 4
						// get new modifier from modifiers like
						// "mid and/or <m>distal</m>"
						if (!StringUtility.isMatchedNullSafe(newModifier,"<")
								&& StringUtility.isMatchedNullSafe(newModifier, "\\w")
								&& StringUtility.isMatchedNullSafe(start,",(?:</B>)?\\s*$")) {

						
							flag += dataholderHandler.updateDataHolder(newModifier, "m", "", "modifiers", 1);
//							print "find a modifier [E0]: $newm\n" if $debug;
						}
						
						// case 5
						// pos = "N"/"B"
						if (word != null) {
							Pattern p5 = Pattern.compile("([A-Z])>(<([A-Z])>)?(.*?)<");
							Matcher m5 = p5.matcher(word);
							if (m5.find()) {
								String g1 = m5.group(1);
								String g2 = m5.group(2);
								String g3 = m5.group(3);
								String g4 = m5.group(4);
								
								String t1 = g1;
								String t2 = g3;
								
								word = g4;
								pos = t1 + t2;
								
								// if <N><B>, decide on one tag
								if (pos.length() > 1) {
									if (StringUtility.isMatchedNullSafe(sentence, "^\\s*<B>[,;:]<\\/B>\\s*<N>")
											||StringUtility.isMatchedNullSafe(sentence, "^\\s*<B>\\.<\\/B>\\s*$")){
										pos = "B";
									}
									else {
										pos = "N";
									}
								}
								knownPOS = 1;
							}
							else {
								List<POSInfo> POSs = dataholderHandler.checkPOSInfo(word);
								pos = POSs.get(0).getPOS();
							}
						}
						
						pos = StringUtils.equals(pos, "?") ? this.myLearnerUtility.getWordFormUtility().getNumber(word) : pos;
						
						// part 6
						// markup sentid, update pos for word, new modifier
						if (StringUtils.equals(pos, "p") || StringUtils.equals(pos, "N")) {
							if (knownPOS != 0) {
								flag += dataholderHandler.updateDataHolder(word, "p", "-", "wordpos", 1);
//								/print "update [$word] pos: p\n" if (!$knownpos) && $debug;
							}
							
							if (count == 0 
									&& (StringUtility.isMatchedNullSafe(start, "^\\S+\\s?(?:and |or |and \\/ or |or \\/ and )?$")
											||start.length() == 0)) {
								modifier = start + modifier;
								modifier = modifier.replaceAll("<\\S+?>", "");
								word = word.replaceAll("<\\S+?>", "");
								dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modifier, tag, "adjectivesubject[M-N]");
								// new modifier
								start = start.replaceAll("\\s*(and |or |and \\/ or |or \\/ and )\\s*", "");
								start = start.replaceAll("<\\S+?>", "");
								
								while (StringUtility.isMatchedNullSafe(start, "^("+this.myLearnerUtility.getConstant().STOP+")\\b")) {
									start = start.replaceAll("^("+this.myLearnerUtility.getConstant().STOP+")\\b\\s*", "");
								}
								
								if (start.length() > 0) {
									flag += dataholderHandler.updateDataHolder(start, "m", "", "modifiers", 1);
									//print "find a modifier [E]: $start\n" if $debug;
											
								}
							}
						}
						// not p
						else {
							if (knownPOS != 0) {
								// update pos for word, markup sentid (get tag
								// from context), new modifier
								flag += dataholderHandler.updateDataHolder(word, "b", "", "wordpos", 1);
								// print "update [$word] pos: b\n" if $debug;
							}
							
							if (count == 0 
									&& (StringUtility.isMatchedNullSafe(start, "^\\S+\\s?(?:and |or |and \\/ or |or \\/ and )?$")
											||start.length() == 0)) {
								while (StringUtility.isMatchedNullSafe(start, "^("+this.myLearnerUtility.getConstant().STOP+"|"+this.myLearnerUtility.getConstant().FORBIDDEN+"|\\w+ly)\\b")) {
									start = start.replaceAll("^("+this.myLearnerUtility.getConstant().STOP+"|"+this.myLearnerUtility.getConstant().FORBIDDEN+"|\\w+ly)\\b\\s*", "");									
								}
								
								modifier = start + modifier;
								modifier = modifier.replaceAll("<\\S+?>", "");
								tag = dataholderHandler.getParentSentenceTag(sentenceID);
								List<String> modifierAndTag = dataholderHandler.getMTFromParentTag(tag);
								String newM = modifierAndTag.get(0);
								tag = modifierAndTag.get(1);
								if (StringUtility.isMatchedNullSafe(newM, "\\w")) {
									modifier = modifier + " " + newM;
								}
								dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modifier, tag, "adjectivesubject[M-B]");
								// new modifier
								start = start.replaceAll("\\s*(and |or |and \\/ or |or \\/ and )\\s*", "");
								start = start.replaceAll("<\\S+?>", "");
								if (start.length() > 0) {
									if (!StringUtility.isMatchedNullSafe(start, "ly\\s*$") 
											&& !StringUtility.isMatchedNullSafe(start, "\\b(" + this.myLearnerUtility.getConstant().STOP + "|" + this.myLearnerUtility.getConstant().FORBIDDEN + ")\\b")) {
										flag += dataholderHandler.updateDataHolder(word, "m", "", "modifiers", 1);
										// print "find a modifier [F]: $start\n" if $debug;
									}
								}	
							}
						}

						count++;
					}
				}
			}
		}
		
		return flag;
	}
	
	public boolean adjectiveSubjectsPart2Helper1(String sentence,
			Set<String> typeModifiers) {
		String pattern = "<M>\\S*(" + StringUtils.join(typeModifiers, "|")
				+ ")\\S*</M> .*";
		return StringUtility.isMatchedNullSafe(sentence, pattern);
	}
	
	/**
	 * Discover new modifiers using and/or pattern. 
	 * For "modifier and/or unknown boundary" pattern or
	 * "unknown and/or modifier boundary" pattern, make "unknown" a modifier
	 * 
	 * @param dataholderHandler
	 * @return
	 */
	public int discoverNewModifiers(DataHolder dataholderHandler) {
		int sign = 0;
		
		// "modifier and/or unknown boundary" pattern
		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
			String sentenceTag = sentenceItem.getTag();
			String sentence = sentenceItem.getSentence();	
			int sentenceID = sentenceItem.getID();
			if ((!StringUtility.isMatchedNullSafe(sentenceTag, "ignore") || sentenceTag == null) 
				&& StringUtility.isMatchedNullSafe(sentence, "<M>[^\\s]+</M> (or|and|and / or|or / and) .*")){
				String POS = "";
				// if "<m>xxx</m> (and|or) yyy (<b>|\d)" pattern appears at the
				// beginning or is right after the 1st word of the sentence,
				// mark up the sentence, add yyy as a modifier
				if (sentence != null) {
					Pattern p1 = Pattern.compile("^(?:\\w+\\s)?<M>(\\S+)<\\/M> (and|or|nor|and \\/ or|or \\/ and) ((?:<[^M]>)*[^<]+(?:<\\/[^M]>)*) <B>[^,;:\\.]");
					Matcher m1 = p1.matcher(sentence);
					if (m1.find()) {
						String g1 = m1.group(1);
						String g2 = m1.group(2);
						String g3 = m1.group(3);
						String modifier = g1 +" "+ g2+" "+ g3;
						String newM = g3;
						
						if (!StringUtility.isMatchedNullSafe(newM, "\\b("+this.myLearnerUtility.getConstant().STOP+")\\b")) {
							modifier = modifier.replaceAll("<\\S+?>", "");
							if (newM != null) {
								Pattern p11 = Pattern.compile("(.*?>)(\\w+)<\\/");
								Matcher m11 = p11.matcher(newM);
								if (m11.find()) {
									newM = m11.group(2);
									POS = m11.group(1);
								}
							}
							
							// update N to M: retag sentences tagged as $newm, remove [s] record from wordpos
							if (StringUtility.isMatchedNullSafe(POS, "<N>")) {
								sign += dataholderHandler.changePOS(newM, "s", "m", "", 1);
							}
							// B
							else {
								sign += dataholderHandler.updateDataHolder(newM, "m", "", "modifiers", 1);
							}
							// print "find a modifier [A]: $newm\n" if $debug;
							String tag = dataholderHandler.getParentSentenceTag(sentenceID);
							List<String> modifierAndTag = dataholderHandler.getMTFromParentTag(tag);
							String m = modifierAndTag.get(0);
							tag = modifierAndTag.get(1);
							if (StringUtility.isMatchedNullSafe(m, "\\w")) {
								modifier = modifier + " "+m;
							}
							dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modifier, tag, "discovernewmodifiers");							
						}
					}
					// if the pattern appear in the middle of the sentence, add yyy as modifier
					else {
						Pattern p2 = Pattern.compile("<M>(\\S+)<\\/M> (and|or|nor|and \\/ or|or \\/ and) (\\w+) <B>[^,;:\\.]");
						Matcher m2 = p2.matcher(sentence); 
						if (m2.find()) {
							String newM = m2.group(3);
							sign += dataholderHandler.updateDataHolder(newM, "m", "", "modifiers", 1);
							// print "find a modifier[B]: $newm\n" if $debug;
						}
						
					}
				}
			}
		}
		
		// "unknown and/or modifier boundary"
		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
			String sentence = sentenceItem.getSentence();
			String sentenceTag = sentenceItem.getTag();
			if ((!StringUtility.isMatchedNullSafe(sentenceTag, "ignore") || sentenceTag == null) 
					&& StringUtility.isMatchedNullSafe(sentence, "[^\\w]+ (and|or|nor|and / or|or / and) <M>[^\\w]+</M> .*")) {
				int sentenceID = sentenceItem.getID();
				
				String POS = "";
				// if "xxx (and|or|nor) <m>yyy</m> (<b>|\d)" pattern appear at the beginning or is right after the 1st word of the sentence, mark up the sentence, add yyy as a modifier
				if (sentence != null) {
					Pattern p3 = Pattern.compile("^(?:\\w+\\s)?((?:<[^M]>)*[^<]+(?:<\\/[^M]>)*) (and|or|nor|and \\/ or|or \\/ and) <M>(\\S+)<\\/M> <B>[^:;,\\.]");
					Matcher m3 = p3.matcher(sentence);
					if (m3.find()) {
						String g1 = m3.group(1);
						String g2 = m3.group(2);
						String g3 = m3.group(3);
						
						String modifier = g1 + " " + g2 + " " + g3; 
						String newM = g1;
						modifier = modifier.replaceAll("<\\S+?>", "");
						if (newM != null) {
							Pattern p31 = Pattern.compile("(.*?>)(\\w+)<\\/");							
							Matcher m31 = p31.matcher(newM);
							if (m31.find()) { // N or B
								newM = m31.group(2);
								POS = m31.group(1);
							}
						}
						
						if (StringUtility.isMatchedNullSafe(POS, "<N>")) { // update N to M
							sign += dataholderHandler.changePOS(newM, "s", "m", "", 1); // update $newm to m
						}
						else { // B
							sign += dataholderHandler.updateDataHolder(newM, "m", "", "modifiers", 1);
						}
						// print "find a modifier [C]: $newm\n" if $debug;
						String tag = dataholderHandler.getParentSentenceTag(sentenceID);
						List<String> modifierAndTag = dataholderHandler.getMTFromParentTag(tag);
						String m = modifierAndTag.get(0);
						tag = modifierAndTag.get(1);
						
						if (StringUtility.isMatchedNullSafe(m, "\\w")) {
							modifier = modifier +" "+m;
						}
						
						dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modifier, tag, "discovernewmodifiers");
					}
					else {
						Pattern p32 = Pattern.compile("(\\w+) (and|or|nor|and \\/ or|or \\/ and) <M>(\\S+)<\\/M> <B>[^,:;\\.]");
						Matcher m32 = p32.matcher(sentence);
						// if the pattern appear in the middle of the sentence, add yyy as modifier
						if (m32.find()) {
							String newM = m32.group(1);
							sign += dataholderHandler.updateDataHolder(newM, "m", "", "modifiers", 1);
						}
						//print "find a modifier [D]: $newm\n" if $debug;
					}
				}
			}
		}
		
		return sign;
	}
	
	public int handleAndOr(DataHolder dataholderHandler) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.handleAndOr");

		myLogger.info("to match pattern " + Constant.ANDORPTN);

		List<SentenceStructure> sentenceItems = dataholderHandler
				.getSentencesByTagPattern("^andor$");

		int sign = 0;
		for (SentenceStructure sentenceItem : sentenceItems) {
			int sentenceID = sentenceItem.getID();
			String sentence = sentenceItem.getSentence();
			// myLogger.trace(Constant.SEGANDORPTN);
			// myLogger.trace(Constant.ANDORPTN);
			int result = this.andOrTag(dataholderHandler, sentenceID, sentence,
					Constant.SEGANDORPTN, Constant.ANDORPTN);
			sign = sign + result;
		}
		
		return sign;
	}
	
	public int andOrTag(DataHolder dataholderHandler, int sentenceID,
			String sentence, String sPattern, String wPattern) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.andOrTag");
		myLogger.trace("Enter");

		int sign = 0;

		List<String> mPatterns = new ArrayList<String>();
		List<String> sPatterns = new ArrayList<String>();
		List<String> mSegments = new ArrayList<String>();
		List<String> sSegments = new ArrayList<String>();

		Set<String> token = new HashSet<String>();
		token.addAll(Arrays.asList("and or nor".split(" ")));
		token.add("\\");
		token.add("and / or");
		String strToken = "(" + StringUtils.join(token, " ") + ")";

		int limit = 80;
		List<String> words = new ArrayList<String>();
		words.addAll(Arrays.asList(sentence.split(" ")));
		String pattern = this.myLearnerUtility.getSentencePtn(
				dataholderHandler, token, limit, words);
		pattern = pattern.replaceAll("t", "m");

		myLogger.info(String.format("Andor pattern %s for %s", pattern,
				words.toString()));

		if (pattern == null) {
			return -1;
		}

		// Matcher m1 = StringUtility.createMatcher(pattern, wPattern);
		Matcher m2 = StringUtility.createMatcher(pattern, "^b+&b+[,:;.]");

		if (sentenceID == 163) {
			System.out.println();
		}

		List<List<String>> res = this.andOrTagCase1Helper(pattern, wPattern, words, token);
		if (res != null) {
			mPatterns = res.get(0);
			mSegments = res.get(1);
			sPatterns = res.get(2);
			sSegments = res.get(3);
			List<String> tagAndModifier1 = res.get(4);
			List<String> tagAndModifier2 = res.get(5);
			List<String> update1 = res.get(6);
			List<String> update2 = res.get(7);

			if (tagAndModifier1.size() > 0) {
				String modifier = tagAndModifier1.get(0);
				String tag = tagAndModifier1.get(1);
				dataholderHandler.tagSentenceWithMT(sentenceID, sentence, "",
						tag, "andor[n&n]");
				myLogger.trace("tagSentenceWithMT(" + sentenceID + ", "
						+ sentence + ", , " + tag + ", andor[n&n]");
			} else {
				myLogger.debug(String.format(
						"Andor can not determine a tag or modifier for %d: %s",
						sentenceID, sentence));
			}

			if (tagAndModifier2.size() > 0) {
				String modifier = tagAndModifier2.get(0);
				String tag = tagAndModifier2.get(1);
				dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
						modifier, tag, "andor[m&mn]");
				myLogger.trace("tagSentenceWithMT(" + sentenceID + ", "
						+ sentence + ", " + modifier + ", " + tag
						+ ", andor[m&mn]");
			} else {
				myLogger.debug(String.format(
						"Andor can not determine a tag or modifier for %d: %s",
						sentenceID, sentence));
			}

			if (update1.size() > 0) {
				String newBoundaryWord = update1.get(0);
				sign = sign
						+ dataholderHandler.updateDataHolder(newBoundaryWord,
								"b", "", "wordpos", 1);
			}

			if (update2.size() > 0) {
				for (String tempWord : update2) {
					sign = sign
							+ dataholderHandler.updateDataHolder(tempWord, "p",
									"-", "wordpos", 1);
				}
			}
		}

		else if (m2.find()) {
			myLogger.trace("Case 2");
			dataholderHandler.tagSentenceWithMT(sentenceID, sentence, "",
					"ditto", "andor");
		} else {
			myLogger.trace("Case 3");
			myLogger.trace("[andortag]Andor can not determine a tag or modifier for "
					+ sentenceID + ": " + sentence);
		}
		myLogger.trace("Return " + sign + "\n");
		return sign;
	}
	
	public List<List<String>> andOrTagCase1Helper(String pattern,
			String wPattern, List<String> words, Set<String> token) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.andOrTag");

		List<String> mPatterns = new ArrayList<String>();
		List<String> sPatterns = new ArrayList<String>();
		List<String> mSegments = new ArrayList<String>();
		List<String> sSegments = new ArrayList<String>();

		List<String> update1 = new ArrayList<String>();
		List<String> update2 = new ArrayList<String>();

		List<String> tagAndModifier1 = new ArrayList<String>();
		List<String> tagAndModifier2 = new ArrayList<String>();

		String strToken = "(" + StringUtils.join(token, " ") + ")";

		Matcher m1 = StringUtility.createMatcher(pattern, wPattern);

		if (m1.find()) {
			myLogger.trace("Case 1");
			if (pattern.equals("n&qqnbq")) {
				// System.out.println();
			}

			int start1 = m1.start(1);
			int end1 = m1.end(1);

			int start2 = m1.start(2);
			int end2 = m1.end(2);

			int start3 = m1.start(3);
			int end3 = m1.end(3);

			int start4 = m1.start(4);
			int end4 = m1.end(4);

			int start5 = m1.start(5);
			int end5 = m1.end(5);


			// System.out.println(pattern);
			// System.out.println(start1);
			// System.out.println();
			String earlyGroupsPattern = start1 == -1 ? "" : pattern.substring(
					0, start1);
			String[] patterns = earlyGroupsPattern.split("s*<B>,<\\/B>\\s*");
			String earlyGroupsWords = start1 == -1 ? "" : StringUtils.join(
					words.subList(0, start1), " ");
			String[] segments = earlyGroupsWords.split("\\s*<B>,<\\/B>s*");

			String secondLastModifierPattern = m1.group(1);
			String secondLastModifierWords = secondLastModifierPattern == null ? ""
					: StringUtils.join(words.subList(start1, end1), " ");

			String sencondLastStructurePattern = m1.group(2);
			String secondLastStructureWords = sencondLastStructurePattern == null ? ""
					: StringUtils.join(words.subList(start2, end2), " ");

			String lastModifierPattern = m1.group(3);
			String lastModifierWords = lastModifierPattern == null ? ""
					: StringUtils.join(words.subList(start3, end3), " ");

			String lastStructurePattern = m1.group(4);
			String lastStructureWords = lastStructurePattern == null ? ""
					: StringUtils.join(words.subList(start4, end4), " ");

			String endSegmentPattern = m1.group(5);
			String endSegmentWords = endSegmentPattern == null ? ""
					: StringUtils.join(words.subList(start5, end5), " ");

			int bIndex = start5;

			// matching pattern with original text
			if (!(patterns.length == 1 && StringUtils.equals(patterns[0], ""))) {
				for (int i = 0; i < patterns.length; i++) {
					Pattern p = Pattern.compile("sPattern");
					Matcher m10 = p.matcher(patterns[i]);
					if (m10.find()) {
						String g1 = m10.group(1);
						mPatterns.add(g1);
						String g2 = m10.group(2);
						sPatterns.add(g2);

						List<String> w = new ArrayList<String>(
								Arrays.asList(segments[i].split(" ")));
						String m = StringUtils.join(w.subList(0, m10.end(1)),
								" ");

						if (StringUtility.isMatchedNullSafe(m,
								"\\b(although|but|when|if|where)\\b")) {
							return null;
						}

						mSegments.add(m);
						sSegments.add(StringUtils.join(
								w.subList(m10.end(1), w.size()), " "));
					} else {
						myLogger.info("wrong segment: " + patterns[i] + "=>"
								+ segments[i] + "\n");
						return null;
					}
				}
			}

			if (secondLastModifierPattern != null)
				mPatterns.add(secondLastModifierPattern);
			if (!StringUtils.equals(secondLastModifierWords, ""))
				mSegments.add(secondLastModifierWords);
			if (sencondLastStructurePattern != null)
				sPatterns.add(sencondLastStructurePattern);
			if (!StringUtils.equals(secondLastStructureWords, ""))
				sSegments.add(secondLastStructureWords);

			if (lastModifierPattern != null)
				mPatterns.add(lastModifierPattern);
			if (!StringUtils.equals(lastModifierWords, ""))
				mSegments.add(lastModifierWords);
			if (lastStructurePattern != null)
				sPatterns.add(lastStructurePattern);
			if (!StringUtils.equals(lastStructureWords, ""))
				sSegments.add(lastStructureWords);

			// find the modifier and the tag for sentenceID
			// case 1.1
			if (this.countStructures(sPatterns) > 1) {
				// compound subject involving multiple structures: mn,mn,&mn =>
				// use all but bounary as the tag, modifier="";
				String tag = StringUtils.join(words.subList(0, bIndex), " ");
				String modifier = "";
				tag = tag.replaceAll("<\\S+?>", "");
				if (tag != null) {
					String regex11 = "\\b(" + StringUtils.join(token, "|")
							+ ")\\b";
					Matcher m11 = StringUtility.createMatcher(tag, regex11);

					if (m11.find()) {
						String conj = m11.group(1);

						tag = tag.replaceAll(",", " " + conj + " ");
						tag = tag.replaceAll("\\s+", " ");
						tag = tag.replaceAll("(" + conj + " )+", "$1");
						tag = tag.replaceAll("^\\s+", "");
						tag = tag.replaceAll("\\s+$", "");

						// dataholderHandler.tagSentenceWithMT(sentenceID,
						// sentence, "", tag, "andor[n&n]");
						tagAndModifier1.add("");
						tagAndModifier1.add(tag);
					}
					// else {
					// myLogger.debug(String.format("Andor can not determine a tag or modifier for %d: %s",
					// sentenceID, sentence));
					// }
				}
				// case 1.2
				else if (this.countStructures(sPatterns) == 1) {
					// m&mn => connect all modifiers as the modifier, and the n
					// as the tag
					int i = 0;
					for (i = 0; i < sPatterns.size(); i++) {
						if (StringUtility.isMatchedNullSafe(sPatterns.get(i),
								"\\w")) {
							break;
						}
					}

					tag = sSegments.get(i);
					tag = tag.replaceAll("<\\S+?>", "");
					modifier = StringUtils.join(mSegments, " ");
					modifier = modifier.replaceAll("<\\S+?>", "");

					tag = StringUtility.trimString(tag);
					modifier = StringUtility.trimString(modifier);

					String myStop = this.myLearnerUtility.getConstant().STOP;
					myStop = myStop.replaceAll(
							String.format("\\b%s\\b", token), "");
					myStop = myStop.replaceAll("\\s+$", "");

					if (StringUtility.isMatchedNullSafe(modifier, "\\b"
							+ strToken + "\\b")
							&& StringUtility.isEntireMatchedNullSafe(modifier,
									"\\b(" + myStop + "|to)\\b")) {
						// case 1.2.1
						List<String> wordsTemp = new ArrayList<String>();
						wordsTemp.addAll(Arrays.asList(tag.split("\\s+")));
						modifier = modifier
								+ " "
								+ StringUtils.join(wordsTemp.subList(0,
										wordsTemp.size() - 1), " ");
						tag = wordsTemp.get(wordsTemp.size() - 1);
						// dataholderHandler.tagSentenceWithMT(sentenceID,
						// sentence, modifier, tag, "andor[m&mn]");
						tagAndModifier2.add(modifier);
						tagAndModifier2.add(tag);

					}
					// else {
					// myLogger.debug(String.format("Andor can not determine a tag or modifier for %d: %s",
					// sentenceID, sentence));
					// }
				}
				// case 1.3
				else {
					myLogger.debug("Andor can not determine a tag or modifier");
				}

				int q = -1;
				if (endSegmentPattern != null) {
					Matcher m13 = StringUtility.createMatcher(
							endSegmentPattern, "q");
					if (m13.find()) {
						q = m13.start();
					}
				}

				if (q >= 0) {
					String newBoundaryWord = endSegmentWords.split(" ")[q];
					if (StringUtility.isMatchedNullSafe(newBoundaryWord, "\\w")) {
						update1.add(newBoundaryWord);
						// sign = sign +
						// dataholderHandler.updateDataHolder(newBoundaryWord,
						// "b", "", "wordpos", 1);
					}
				}

				// structure patterns and segments: $nptn =
				// "((?:[np],?)*&?[np])"; #grouped #must present, no q allowed
				// mark all ps "p"
				for (int i = 0; i < sPatterns.size(); i++) {
					String sPatternI = sPatterns.get(i);
					sPatternI = sPatternI.replaceAll("(.)", "$1 ");
					sPatternI = StringUtility.trimString(sPatternI);
					String[] ps = sPatternI.split(" ");
					String[] ts = sSegments.get(i).split("\\s+");

					for (int j = 0; j < ps.length; j++) {
						if (StringUtils.equals(ps[j], "p")) {
							ts[j] = StringUtility.trimString(ts[j]);
							update2.add(ts[j]);
							// sign = sign
							// + dataholderHandler.updateDataHolder(ts[j],
							// "p", "-", "wordpos", 1);
						}
					}

				}

			}

			List<List<String>> res = new ArrayList<List<String>>();
			res.add(mPatterns);
			res.add(mSegments);
			res.add(sPatterns);
			res.add(sSegments);
			res.add(tagAndModifier1);
			res.add(tagAndModifier2);
			res.add(update1);
			res.add(update2);

			return res;
		} else {
			return null;
		}
	}
	
	public int countStructures(List<String> patterns) {
		int count = 0;
		for (String pattern : patterns) {
			if (StringUtility.isMatchedNullSafe(pattern, "\\w")) {
				count++;
			}
		}

		return count;
	}

}
