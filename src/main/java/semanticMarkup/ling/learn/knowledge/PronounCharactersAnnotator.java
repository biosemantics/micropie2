package semanticMarkup.ling.learn.knowledge;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.SentenceStructure;
import semanticMarkup.ling.learn.utility.LearnerUtility;
import semanticMarkup.ling.learn.utility.StringUtility;

/**
 * Annotate any clause having a pronoun or a character instead of an organ name
 * as its subject by "ditto".
 * 
 * @author Dongye
 * 
 */
public class PronounCharactersAnnotator implements IModule {
	private LearnerUtility myLearnerUtility;
	
	public PronounCharactersAnnotator(LearnerUtility learnerUtility) {
		this.myLearnerUtility = learnerUtility;
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		// TODO Auto-generated method stub
		
	}
	
	public void pronounCharacterSubject(DataHolder dataholderHandler) {

		for (SentenceStructure sentenceItem : dataholderHandler
				.getSentenceHolder()) {

			int sentenceID = sentenceItem.getID();
			String lead = sentenceItem.getLead();
			String sentence = sentenceItem.getSentence();
			String modifier = sentenceItem.getModifier();
			String tag = sentenceItem.getTag();

			List<String> mt = pronounCharacterSubjectHelper(lead, sentence,
					modifier, tag);
			if (mt != null) {
				dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
						modifier, tag,
						"pronouncharactersubject[character subject]");
			}
		}

		// preposition cases
		String prepositionPattern = String
				.format("^(%s)", this.myLearnerUtility.getConstant().PREPOSITION);
		for (SentenceStructure sentenceItem : dataholderHandler
				.getSentenceHolder()) {
			int sentenceID = sentenceItem.getID();
			String lead = sentenceItem.getLead();
			String modifier = sentenceItem.getModifier();
			String tag = sentenceItem.getTag();
			String sentence = sentenceItem.getSentence();
			boolean case1 = (StringUtils.equals(tag, "ignore"));
			boolean case2 = (tag == null);
			boolean case3 = StringUtility.isMatchedNullSafe(tag,
					prepositionPattern + " ");
			if ((case1 || case2) && case3) {
				dataholderHandler.tagSentenceWithMT(sentenceID, sentence, "",
						"", "pronouncharactersubject[proposition subject]");
			}
		}

		// pronoun cases
		String pronounPattern = String.format("(%s)", this.myLearnerUtility.getConstant().PRONOUN);
		for (SentenceStructure sentenceItem : dataholderHandler
				.getSentenceHolder()) {
			int sentenceID = sentenceItem.getID();
			String lead = sentenceItem.getLead();
			String modifier = sentenceItem.getModifier();
			String tag = sentenceItem.getTag();
			String sentence = sentenceItem.getSentence();

			boolean case1 = StringUtility.isMatchedNullSafe(tag,
					String.format("(^| )%s( |\\$)", pronounPattern));
			boolean case2 = StringUtility.isMatchedNullSafe(modifier,
					String.format("(^| )%s( |\\$)", pronounPattern));
			if (case1 || case2) {
				modifier = modifier.replaceAll("\\b(" + this.myLearnerUtility.getConstant().PRONOUN
						+ ")\\b", "");
				tag = tag.replaceAll("\\b(" + this.myLearnerUtility.getConstant().PRONOUN + ")\\b", "");
				modifier = modifier.replaceAll("\\s+", " ");
				tag = tag.replaceAll("\\s+", " ");

				if (!StringUtility.isMatchedNullSafe(tag, "\\w")
						|| StringUtility.isMatchedNullSafe(tag, "ditto")) {
					tag = dataholderHandler.getParentSentenceTag(sentenceID);
				}

				modifier = modifier.replaceAll("(^\\s*|\\s*$)", "");
				tag = tag.replaceAll("(^\\s*|\\s*$)", "");

				List<String> mt = dataholderHandler.getMTFromParentTag(tag);
				String m = mt.get(0);
				tag = mt.get(1);

				if (StringUtility.isMatchedNullSafe(m, "\\w")) {
					modifier = modifier + m;
					dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
							modifier, tag,
							"pronouncharactersubject[pronoun subject]");
				}
			}
		}

		// correct to missed N
		for (SentenceStructure sentenceItem : dataholderHandler
				.getSentenceHolder()) {
			int sentenceID = sentenceItem.getID();
			String lead = sentenceItem.getLead();
			String modifier = sentenceItem.getModifier();
			String tag = sentenceItem.getTag();
			String sentence = sentenceItem.getSentence();

			List<String> mt = this.pronounCharacterSubjectHelper4(lead,
					sentence, modifier, tag);
			if (mt != null) {
				modifier = mt.get(0);
				tag = mt.get(1);
				dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
						modifier, tag,
						"pronouncharactersubject[correct to missed N]");
			}
		}
	}

	public List<String> pronounCharacterSubjectHelper4(String lead,
			String sentence, String modifier, String tag) {
		boolean case1 = (StringUtils.equals(tag, "ignore"));
		boolean case2 = (tag == null);
		boolean case3 = !StringUtility.isMatchedNullSafe(tag, " (and|nor|or) ");
		boolean case4 = !StringUtility.isMatchedNullSafe(sentence, "\\[");
		boolean case5 = false;
		if (sentence != null) {
			Pattern p = Pattern.compile("^[^N]*<N>" + tag);
			Matcher m = p.matcher(sentence);
			if (m.find()) {
				case5 = true;
			}
		}

		if ((case1 || case2) && case3 && case4 && case5) {
			if (sentence != null) {
				sentence = sentence.replaceAll("></?", "");
				Pattern p = Pattern
						.compile("^(\\S*) ?<N>([^<]+)<\\/N> <[MB]+>(\\S+)<\\/[MB]+> \\S*\\b"
								+ tag + "\\b\\S*");
				Matcher m2 = p.matcher(sentence);
				if (m2.find()) {
					modifier = m2.group(1);
					tag = m2.group(2);
					String g3 = m2.group(3);
					if (!StringUtility.isMatchedNullSafe(g3, "\\bof\\b")) {
						modifier = modifier.replaceAll("<\\S+?>", "");
						tag = tag.replaceAll("<\\S+?>", "");
						modifier = modifier.replaceAll("(^\\s*|\\s*$)", "");
						tag = tag.replaceAll("(^\\s*|\\s*$)", "");
						List<String> mt = new ArrayList<String>();
						mt.add(modifier);
						mt.add(tag);
						return mt;
					}
				}
			}
		}
		return null;

	}

	public List<String> pronounCharacterSubjectHelper(String lead,
			String sentence, String modifier, String tag) {
		String t = "(?:<\\/?[A-Z]+>)?";

		boolean b1 = !StringUtils.equals(tag, "ignore");
		boolean b2 = (tag == null);
		boolean b3 = StringUtility.isMatchedNullSafe(lead, "(^| )("
				+ this.myLearnerUtility.getConstant().CHARACTER + ")( |$)");
		boolean b4 = StringUtility.isMatchedNullSafe(tag, "(^| )("
				+ this.myLearnerUtility.getConstant().CHARACTER + ")( |$)");
		if (((b1 || b2) && b3) || b4) {
			sentence = sentence.replaceAll("></?", "");
			if (sentence != null) {
				String pattern1 = String
						.format("^.*?%s\\b(%s)\\b%s %s(?:of)%s (.*?)(<[NO]>([^<]*?)<\\/[NO]> ?)+ ",
								t, this.myLearnerUtility.getConstant().CHARACTER, t, t, t);
				Matcher m1 = StringUtility.createMatcher(sentence, pattern1);

				String pattern2 = String
						.format("^(.*?)((?:<\\/?[BM]+>\\w+?<\\/?[BM]+>\\s*)*)%s\\b(%s)\\b%s",
								t, this.myLearnerUtility.getConstant().CHARACTER, t);
				Matcher m2 = StringUtility.createMatcher(sentence, pattern2);

				// case 1.1
				if (m1.find()) {
					tag = m1.group(4);
					modifier = sentence.substring(m1.start(2), m1.start(4));
					String s2 = m1.group(2);
					String s3 = m1.group(3);

					if ((!StringUtility.isMatchedNullSafe(s2,
							String.format("\\b(%s)\\b", this.myLearnerUtility.getConstant().PREPOSITION)))
							&& (!StringUtility.isMatchedNullSafe(s3, String
									.format("\\b(%s|\\d)\\b", this.myLearnerUtility.getConstant().STOP)))) {
						modifier = modifier.replaceAll("<\\S+?>", "");
						modifier = modifier.replaceAll("(^\\s*|\\s*$)", "");
						tag = tag.replaceAll("<\\S+?>", "");
						tag = tag.replaceAll("(^\\s*|\\s*$)", "");
					} else {
						modifier = "";
						tag = "ditto";
					}
				}

				// case 1.2
				else if (m2.find()) {
					String text = m2.group(1);

					if ((!StringUtility.isMatchedNullSafe(text, "\\b("
							+ this.myLearnerUtility.getConstant().STOP + "|\\d+)\\b"))
							&& (StringUtility.isMatchedNullSafe(text, "\\w"))
							&& (!StringUtility
									.isMatchedNullSafe(text, "[,:;.]"))) {
						text = text.replaceAll("<\\S+?>", "");
						// $text =~ s#(^\s*|\s*$)##g;
						// $text =~ s#[[:punct:]]##g;
						text = text.replaceAll("(^\\s*|\\s*$)", "");
						text = text.replaceAll("\\p{Punct}", "");

						String[] textArray = text.split("\\s+");
						// List<String> textList = new LinkedList<String>();
						// textList.addAll(Arrays.asList(textArray));
						if (textArray.length >= 1) {
							tag = textArray[textArray.length - 1];
							String pattern = "<[NO]>" + tag + "</[NO]>";
							if (StringUtility.isMatchedNullSafe(sentence,
									pattern)) {
								// 1.2.1.1
								text = text.replaceAll(tag, "");
								modifier = text;
							} else {
								// 1.2.1.2
								modifier = "";
								tag = "ditto";
							}
						}
					} else {
						// 1.2.2
						modifier = "";
						tag = "ditto";
					}
				}

				// case 1.3
				else if (StringUtility.isMatchedNullSafe(sentence, "\\b("
						+ this.myLearnerUtility.getConstant().CHARACTER + ")\\b")) {
					modifier = "";
					tag = "ditto";
				}

			}
			List<String> mt = new ArrayList<String>(2);
			mt.add(modifier);
			mt.add(tag);
			return mt;
		} else {
			return null;
		}

	}

}
