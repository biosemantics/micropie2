package semanticMarkup.ling.learn.knowledge;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.ModifierTableValue;
import semanticMarkup.ling.learn.dataholder.SentenceStructure;
import semanticMarkup.ling.learn.utility.StringUtility;

/**
 * sentences that are tagged with a commons substructure, such as blades,
 * margins need to be modified with its parent structure
 * 
 * @author Dongye
 * 
 */
public class CommonSubstructureAnnotator implements IModule {

	public CommonSubstructureAnnotator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		this.commonSubstructure(dataholderHandler);
	}
	
	// sentences that are tagged with a commons substructure, such as blades,
	// margins need to be modified with its parent structure
	public void commonSubstructure(DataHolder dataholderHandler) {
		Set<String> commonTags = this
				.getCommonStructures(dataholderHandler);

		String pattern = StringUtils.join(commonTags, "|");
		pattern = "\\\\[?(" + pattern + ")\\\\]?";

		for (SentenceStructure sentenceItem : dataholderHandler
				.getSentenceHolder()) {
			String tag = sentenceItem.getTag();
			boolean c1 = StringUtils.equals(tag, "ignore");
			boolean c2 = (tag == null);
			boolean c3 = (StringUtility.isMatchedNullSafe(tag, "^" + pattern
					+ "$"));

			if ((c1 || c2) && c3) {
				int sentenceID = sentenceItem.getID();
				String modifier = sentenceItem.getModifier();
				String sentence = sentenceItem.getSentence();

				if (!isModifierContainsStructure(dataholderHandler, modifier)
						&& !StringUtility.isMatchedNullSafe(tag, "\\[")) {
					// when the common substructure is not already modified by a
					// structure, and
					// when the tag is not already inferred from parent tag:
					// mid/[phyllaries]

					String parentStructure = dataholderHandler
							.getParentSentenceTag(sentenceID);

					String pTag = "" + parentStructure;
					parentStructure = parentStructure.replaceAll("([\\[\\]])",
							"");
					if (!StringUtils.equals(parentStructure, "[parenttag]")
							&& !StringUtility.isMatchedNullSafe(modifier,
									parentStructure)
							&& !StringUtility.isMatchedNullSafe(tag,
									parentStructure)) {
						// remove any overlapped words btw parentStructure and
						// tag
						pTag = pTag.replaceAll("\\b" + tag + "\\b", "");
						String modifierCopy = "" + modifier;
						modifier = StringUtility.trimString(modifier);
						pTag = StringUtility.trimString(pTag);
						pTag = pTag.replaceAll("\\s+", " ");
						if (isTypeModifier(dataholderHandler, modifier)) {
							// cauline/base => cauline [leaf] / base
							modifier = modifier + " " + pTag;
						} else {
							// main marginal/spine => [leaf blade] main
							// marginal/spine
							modifier = pTag + " " + modifier;
						}

						// tagsentwmt($sentid, $sentence, $modifier, $tag,
						// "commonsubstructure");
						dataholderHandler.tagSentenceWithMT(sentenceID,
								sentence, modifier, tag, "commonsubstructure");
					}
				}
			}
		}
	}
	
	public boolean isTypeModifier(DataHolder dataholderHandler, String modifier) {
		boolean res = false;

		String[] words = modifier.split("\\s+");
		String word = words[words.length - 1];

		if (dataholderHandler.getModifierHolder().containsKey(word)) {
			ModifierTableValue modifierItem = dataholderHandler
					.getModifierHolder().get(modifier);
			if (modifierItem.getIsTypeModifier()) {
				res = true;
			}
		}

		return res;
	}
	
	public boolean isModifierContainsStructure(DataHolder dataholderHandler,
			String modifier) {
		boolean res = false;

		String[] words = modifier.split("\\s+");

		for (String word : words) {
			Set<String> POSTags = new HashSet<String>();
			POSTags.add("p");
			POSTags.add("s");
			Set<String> PSWords = dataholderHandler
					.getWordsFromWordPOSByPOSs(POSTags);
			if (PSWords.contains(word)) {
				res = true;
				break;
			}
		}

		return res;
	}
	
	/**
	 * find tags with more than one different structure modifiers
	 * 
	 * @param dataholderHandler
	 * @return
	 */
	public Set<String> getCommonStructures(DataHolder dataholderHandler) {

		// Get structures.
		// Structures are just words from WordPOS holder that are P/S but not B
		Set<String> PSTags = new HashSet<String>(
				Arrays.asList("s p".split(" ")));
		Set<String> BTags = new HashSet<String>();
		BTags.add("b");
		Set<String> PSWords = dataholderHandler
				.getWordsFromWordPOSByPOSs(PSTags);
		Set<String> BWords = dataholderHandler.getWordsFromWordPOSByPOSs(BTags);

		Set<String> allStructures = StringUtility.setSubtraction(PSWords,
				BWords);

		Set<String> commonTags = new HashSet<String>();

		// Get a map maps tags to their structures
		Map<String, Set<String>> tagToModifiers = new HashMap<String, Set<String>>();
		for (SentenceStructure sentenceItem : dataholderHandler
				.getSentenceHolder()) {
			String tag = sentenceItem.getTag();
			String modifier = sentenceItem.getModifier();

			boolean c1 = StringUtils.equals(tag, "ignore");
			boolean c2 = (tag == null);
			boolean c3 = StringUtility.isMatchedNullSafe(tag, " ");
			boolean c4 = StringUtility.isMatchedNullSafe(tag, "\\[");
			if ((!c1 || c2) && !c3 && !c4) {
				if (allStructures.contains(modifier)) {
					if (tagToModifiers.containsKey(tag)) {
						tagToModifiers.get(tag).add(modifier);
					} else {
						HashSet<String> modifiers = new HashSet<String>();
						modifiers.add(modifier);
						tagToModifiers.put(tag, modifiers);
					}
				}
			}
		}

		// Added all tags with more than 1 structures into the common tags
		// collection
		Iterator<String> iter = tagToModifiers.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if (tagToModifiers.get(key).size() > 1) {
				String commonTag = new String(key);
				commonTag = commonTag.replaceAll("\\|+", "\\|");
				commonTag = commonTag.replaceAll("\\|+$", "");
				commonTags.add(key);
			}
		}

		return commonTags;
	}


}
