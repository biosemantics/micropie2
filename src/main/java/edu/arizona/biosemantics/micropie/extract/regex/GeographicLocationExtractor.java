package edu.arizona.biosemantics.micropie.extract.regex;

import java.util.List;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.nlptool.StanfordParserWrapper;


/**
 * geographic location identification
 * first found the ner
 * @author maojin
 *
 */
public class GeographicLocationExtractor extends AbstractCharacterValueExtractor{
	
	public GeographicLocationExtractor(ILabel label, String characterName) {
		super(label, characterName);
	}

	private StanfordParserWrapper stanParser;

	public void setStanParser(StanfordParserWrapper stanParser) {
		this.stanParser = stanParser;
	}

	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		List<String> nerList = stanParser.getLocationNER(sentence.getText());//ORGANIZATION
		//TODO:need verb features?
		List charValueList = CharacterValueFactory.createList(this.getLabel(), nerList);
		return charValueList;
	}
	
}