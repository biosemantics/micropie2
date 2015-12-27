package edu.arizona.biosemantics.micropie.extract.regex;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Inject;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.keyword.PhraseBasedExtractor;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.stanford.nlp.ling.TaggedWord;

/**
 * Produces pigment with maximum absorption at 451.2 nm. Carotenoid pigments are
 * present with main absorption peaks at 480, 454 and 425 nm. Non-diffusible
 * carotenoid-type pigments with absorption peaks at 451 nm, 474 nm and 503 nm
 * are produced. Absorption spectral peaks of the pigments are observed at 450
 * and 475 nm.
 * 
 * @author maojin
 *
 */
public class PigmentCompoundAbsorptionExtractor extends PhraseBasedExtractor {

	public PigmentCompoundAbsorptionExtractor(PosTagger posTagger,
			ILabel label, String character, Set<String> keywords,
			Map<String, List> subKeyword) {
		super(label, character, keywords, subKeyword);
		this.posTagger = posTagger;
	}

	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		String text = sentence.getText();
		List cvList = new LinkedList();
		if (text.toLowerCase().indexOf("absorption") > -1) {
			String absorption = null;
			String figure = null;
			MultiClassifiedSentence sent = (MultiClassifiedSentence) sentence;
			List<TaggedWord> taggedWordList = posTagger.tagString(text);
			if (taggedWordList.size() >= 1) {
				List<Phrase> phraseList = null;
				phraseList = phraseParser.extract(taggedWordList);

				for (Phrase pharse : phraseList) {// deal with each phrase
					String ptext = pharse.getText().toLowerCase();
					// System.out.println("ptext="+ptext);
					if (ptext.toLowerCase().indexOf("absorption") > -1) {// absorption
						absorption = ptext;
						break;
					}
				}

				if (absorption != null
						&& (figure = containsTargetFigure(text)) != null) {
					CharacterValue cv = CharacterValueFactory.create(
							this.label,
							absorption.trim() + " at " + figure.trim());
					cvList.add(cv);
				}
			}
			// System.out.println("returnCharacterStrings="+returnCharacterStrings);
			// charValueList = CharacterValueFactory.createList(this.getLabel(),
			// returnCharacterStrings);
		}
		return cvList;

	}

	public String containsTargetFigure(String ptext) {
		String patternString = "[\\d\\s,.andm]+nm";

		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(ptext.toLowerCase());

		while (matcher.find()) {
			return matcher.group();
		}

		return null;
	}
}
