package edu.arizona.biosemantics.micropie.extract;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.micropie.Config;
import edu.arizona.biosemantics.micropie.SentenceBatchProcessor;
import edu.arizona.biosemantics.micropie.SentencePredictor;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.ICharacterValueExtractorProvider;
import edu.arizona.biosemantics.micropie.extract.TaxonCharacterMatrixCreator;
import edu.arizona.biosemantics.micropie.extract.context.RelationParser;
import edu.arizona.biosemantics.micropie.extract.keyword.AntibioticPhraseExtractor;
import edu.arizona.biosemantics.micropie.extract.keyword.KeywordBasedExtractor;
import edu.arizona.biosemantics.micropie.extract.keyword.PhraseBasedExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.AntibioticSyntacticExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GcExtractor;
import edu.arizona.biosemantics.micropie.io.CSVAbbreviationReader;
import edu.arizona.biosemantics.micropie.io.CSVClassifiedSentenceWriter;
import edu.arizona.biosemantics.micropie.io.CSVTaxonCharacterMatrixWriter;
import edu.arizona.biosemantics.micropie.io.CharacterReader;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.arizona.biosemantics.micropie.nlptool.ITextNormalizer;
import edu.arizona.biosemantics.micropie.nlptool.PhraseParser;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.arizona.biosemantics.micropie.nlptool.StanfordParserWrapper;
import edu.arizona.biosemantics.micropie.nlptool.TextNormalizer;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class AntibioticExtractorTest {
	
	
	public static void main(String[] args){
		String inputDirectory = "F:/MicroPIE/micropieInput";
		String abbreviationFile = inputDirectory + File.separator + "abbrevlist/abbrevlist.csv";
		LinkedHashMap<String, String> abbreviations = null;
		CSVAbbreviationReader abbreviationReader = new CSVAbbreviationReader();
		try {
			abbreviationReader.setInputStream(new FileInputStream(abbreviationFile));
			abbreviations =  abbreviationReader.read();
		} catch (Exception e) {
			e.printStackTrace();
		}
		 String celsius_degreeReplaceSourcePattern = "(" +
					"\\s?˚C\\s?|" +
					"\\s?˚ C\\s?|" +
					"\\s?\"C\\s?|" +
					"\\s?\" C\\s?|" +
					"\\s?◦C\\s?|" +
					"\\s?◦ C\\s?|" +
					"\\s?°C\\s?|" +
					"\\s?° C\\s?|" +
					"\\s?\\”C\\s?|" +
					"\\s?\\” C\\s?|" +
					"\\s?u C\\s?" +
					")";
		ITextNormalizer textNormalizer = new TextNormalizer(abbreviations, celsius_degreeReplaceSourcePattern);
		
		
		Properties stanfordCoreProperties = new Properties();
		stanfordCoreProperties.put("annotators", "tokenize, ssplit, parse");
		StanfordCoreNLP sfCoreNLP = new StanfordCoreNLP(stanfordCoreProperties);
		
		LexicalizedParser lexParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		 
		String serializedClassifierModel = "F:\\MicroPIE\\micropieInput\\nlpmodel/english.all.3class.distsim.crf.ser.gz";
		AbstractSequenceClassifier nerClassifier=CRFClassifier.getClassifierNoExceptions(serializedClassifierModel);
		
		StanfordParserWrapper stanfordWrapper = new StanfordParserWrapper(sfCoreNLP, lexParser,nerClassifier);
		String posTagModel = "edu/stanford/nlp/models/pos-tagger/english-bidirectional/english-bidirectional-distsim.tagger";
		PosTagger postagger = new PosTagger(posTagModel);
		
		SentenceSpliter sentSplitter = new SentenceSpliter(textNormalizer, stanfordWrapper);
		
		
		
		SentencePredictor sentencePredictor = null;
		CSVClassifiedSentenceWriter classifiedSentenceWriter = null;
		CSVTaxonCharacterMatrixWriter matrixWriter =null;
		TaxonCharacterMatrixCreator matrixCreator =null;
		ICharacterValueExtractorProvider contentExtractorProvider = null;// extractors
		
		Map sentenceMetadataMap = new HashMap();
		Map<RawSentence, MultiClassifiedSentence> sentenceClassificationMap = new HashMap();
		
		
		String svmLabelAndCategoryMappingFile ="F:\\MicroPIE\\micropieInput\\svmlabelandcategorymapping\\categoryMapping_micropie1.5.txt";
		CharacterReader categoryReader = new CharacterReader();
		categoryReader.setCategoryFile(svmLabelAndCategoryMappingFile);
		categoryReader.read();
		Map categoryLabelCodeMap = categoryReader.getLabelCategoryCodeMap();
		
		
		SentenceBatchProcessor sentBatPIEProcessor =  new SentenceBatchProcessor(matrixCreator, matrixWriter, true, 3, categoryLabelCodeMap, sentenceMetadataMap, sentenceClassificationMap, sentencePredictor, sentSplitter, classifiedSentenceWriter, contentExtractorProvider);
		String lineFile = "F:/MicroPIE/micropieInput/sentences/4.1 Antibiotic sensitivity small.txt";
		//STEP 1: split sentences
		List<RawSentence> testSentences = sentBatPIEProcessor.createSentencesByLine(lineFile);
				
		PhraseParser phraseParser = new PhraseParser();
		RelationParser phraseRelationParser = new RelationParser();
		//STEP 2: predict the classifications of the sentences, i.e., the characters in each sentences
		//List<MultiClassifiedSentence> predictions = new LinkedList<MultiClassifiedSentence>();
		HashSet patterns = new HashSet();
		patterns.add("[Ss]usceptible|susceptible");//susceptible 
		patterns.add("[Ss]ensitive|[Ss]ensitivity");
		patterns.add("[Ii]nhibited|[Ii]nhibiting");
		AntibioticSyntacticExtractor extractor1 = new AntibioticSyntacticExtractor(Label.c32, "Antibiotic sensitivity",patterns,sentSplitter,stanfordWrapper);
		
		
		HashSet keywords = new HashSet();
		keywords.add("susceptible");//susceptible 
		keywords.add("sensitive");
		keywords.add("sensitivity");
		keywords.add("inhibited");
		keywords.add("susceptibility");
		
		//resistant,insensitive
		//
		
		
		AntibioticPhraseExtractor extractor2 = new AntibioticPhraseExtractor(Label.c32, "Antibiotic sensitivity", null, null);
		extractor2.readKeywords("F:\\MicroPIE\\micropieInput\\CharacterValueExtractors\\c32.Antibiotic sensitivity.key");
		extractor2.setPhraseParser(phraseParser);
		extractor2.setPhraseRelationParser(phraseRelationParser);
		extractor2.setPosTagger(postagger);
		extractor2.setSentSplitter(sentSplitter);
		extractor2.setSensTypeKeywords(keywords);
		
		PostProcessor postProcessor = new PostProcessor(); 
		
		for (RawSentence testSentence : testSentences) {
			Set prediction = new HashSet();
			prediction.add(Label.c32);
			
			MultiClassifiedSentence classifiedSentence = new MultiClassifiedSentence(testSentence, prediction);
			String character = extractor1.getCharacterName();
			//System.out.println("current doing:character "+character+" by ["+extractor.getClass().getName()+"]");
			List<CharacterValue> content1 = extractor1.getCharacterValue(classifiedSentence);
			List<CharacterValue> content2 = extractor2.getCharacterValue(classifiedSentence);
			System.out.println("Final Results 1--  "+content1);
			System.out.println("Final Results 2-- "+content2);
			for(CharacterValue cv : content2){
				if(!content1.contains(cv)) content1.add(cv);
			}
			//System.out.println("Final Results--"+classifiedSentence.getText()+" "+content1);
			//postProcessor.postProcessor(content1);
			//System.out.println("Final Results--"+classifiedSentence.getText()+" "+content1);
		}
		
	}
}
