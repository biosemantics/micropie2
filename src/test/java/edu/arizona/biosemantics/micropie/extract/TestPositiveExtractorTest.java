package edu.arizona.biosemantics.micropie.extract;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
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
import edu.arizona.biosemantics.micropie.extract.keyword.KeywordBasedExtractor;
import edu.arizona.biosemantics.micropie.extract.keyword.PhraseBasedExtractor;
import edu.arizona.biosemantics.micropie.extract.keyword.TestExtractor;
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

public class TestPositiveExtractorTest {
	
	
	public static void main(String[] args){
		/*Config config = new Config();
		String prjInputFolder = "F:/MicroPIE/micropieInput";
		String prjOutputFolder ="F:/MicroPIE/micropieInput/output";
		config.setInputDirectory(prjInputFolder);
		config.setOutputDirectory(prjOutputFolder);
		
		Injector injector = Guice.createInjector(config);
		String svmLabelAndCategoryMappingFile = injector.getInstance(Key.get(String.class,  Names.named("svmLabelAndCategoryMappingFile")));
		String predictionsFile = "F:/MicroPIE/micropieInput/sentences/1.1prediction.csv";
		String outputMatrixFile = "F:\\MicroPIE\\micropieInput\\output\\matrix.csv";
		
		SentenceBatchProcessor sentBatPIEProcessor = injector.getInstance(SentenceBatchProcessor.class);
		String lineFile = "F:/MicroPIE/micropieInput/sentences/1.1 G+C small.csv";
		//STEP 1: split sentences
		List<RawSentence> testSentences = sentBatPIEProcessor.createSentencesByLine(lineFile);
				
		//STEP 2: predict the classifications of the sentences, i.e., the characters in each sentences
		List<MultiClassifiedSentence> predictions = new LinkedList<MultiClassifiedSentence>();
		ICharacterValueExtractorProvider contentExtractorProvider = injector.getInstance(ICharacterValueExtractorProvider.class);
		for (RawSentence testSentence : testSentences) {
			Set prediction = new HashSet();
			prediction.add(Label.c1);
			
			// c1==>%g+c
			Set<ICharacterValueExtractor> extractors = contentExtractorProvider.getContentExtractor(Label.c1);
			
			
			MultiClassifiedSentence classifiedSentence = new MultiClassifiedSentence(testSentence, prediction);
			for(ICharacterValueExtractor extractor : extractors) {
				String character = extractor.getCharacterName();
				System.out.println("currently is doing:character");
				Set<CharacterValue> content = extractor.getCharacterValue(classifiedSentence);
			}
		}
		*/
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
		stanfordCoreProperties.put("annotators", "tokenize, ssplit, parse");// ,parse, pos, lemma, ner, , dcoref
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
		
		
		String svmLabelAndCategoryMappingFile ="F:\\MicroPIE\\micropieInput\\svmlabelandcategorymapping\\categoryMapping_poster.txt";
		CharacterReader categoryReader = new CharacterReader();
		categoryReader.setCategoryFile(svmLabelAndCategoryMappingFile);
		categoryReader.read();
		Map categoryLabelCodeMap = categoryReader.getLabelCategoryCodeMap();
		
		
		SentenceBatchProcessor sentBatPIEProcessor =  new SentenceBatchProcessor(matrixCreator, matrixWriter, true, 3, categoryLabelCodeMap, sentenceMetadataMap, sentenceClassificationMap, sentencePredictor, sentSplitter, classifiedSentenceWriter, contentExtractorProvider);
		String lineFile = "F:/MicroPIE/micropieInput/sentences/7.2 Test negative small.txt";
		//STEP 1: split sentences
		List<RawSentence> testSentences = sentBatPIEProcessor.createSentencesByLine(lineFile);
				
		//STEP 2: predict the classifications of the sentences, i.e., the characters in each sentences
		//List<MultiClassifiedSentence> predictions = new LinkedList<MultiClassifiedSentence>();
		PhraseParser phraseParser = new PhraseParser();
		TestExtractor extractor = new TestExtractor(Label.c46, "Test Positive", null, null);
		extractor.setPhraseParser(phraseParser);
		extractor.readKeywords("F:\\MicroPIE\\micropieInput\\CharacterValueExtractors\\c46.Tests negative.key");
		extractor.setPosTagger(postagger);
		PostProcessor postProcessor = new PostProcessor();
		for (RawSentence testSentence : testSentences) {
			Set prediction = new HashSet();
			prediction.add(Label.c46);
			
			MultiClassifiedSentence classifiedSentence = new MultiClassifiedSentence(testSentence, prediction);
			String character = extractor.getCharacterName();
			//System.out.println(classifiedSentence.getText());
			//System.out.println("current doing:character "+character+" by ["+extractor.getClass().getName()+"]");
			List<CharacterValue> content = extractor.getCharacterValue(classifiedSentence);
			postProcessor.postProcessor(content, new ArrayList(), new HashMap());
			System.out.println(classifiedSentence.getText()+" "+content+"\n");
		}
		
	}
}
