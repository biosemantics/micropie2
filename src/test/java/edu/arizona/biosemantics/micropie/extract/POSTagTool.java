package edu.arizona.biosemantics.micropie.extract;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.util.concurrent.ListeningExecutorService;

import edu.arizona.biosemantics.micropie.SentenceBatchProcessor;
import edu.arizona.biosemantics.micropie.SentencePredictor;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.keyword.KeywordBasedExtractor;
import edu.arizona.biosemantics.micropie.io.CSVClassifiedSentenceWriter;
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.io.CSVTaxonCharacterMatrixWriter;
import edu.arizona.biosemantics.micropie.io.CharacterReader;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;

public class POSTagTool {
	
	public static void main(String[] args){
		String posTagModel = "edu/stanford/nlp/models/pos-tagger/english-bidirectional/english-bidirectional-distsim.tagger";
		PosTagger postagger = new PosTagger(posTagModel);
		String svmLabelAndCategoryMappingFile ="F:\\MicroPIE\\micropieInput\\svmlabelandcategorymapping_data\\categoryMapping_poster.txt";
		CharacterReader categoryReader = new CharacterReader();
		categoryReader.setCategoryFile(svmLabelAndCategoryMappingFile);
		categoryReader.read();
		Map categoryLabelCodeMap = categoryReader.getLabelCategoryCodeMap();
		
		
		
		String lineFile = "F:/MicroPIE/micropieInput/sentences/3.12 aerophilicity.txt";
		
		CSVSentenceReader sentReader = new CSVSentenceReader();
		sentReader.setInputStream(lineFile);
		
		List<RawSentence> testSentences = sentReader.readOneColumnSentenceList();
		
		for (RawSentence testSentence : testSentences) {
			String text = testSentence.getText();
			List tagWordList = postagger.tagString(text);
			System.out.println(text+"\n"+tagWordList);
		}
	}

}
