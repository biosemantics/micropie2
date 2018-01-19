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
import edu.arizona.biosemantics.micropie.extract.regex.PHTempNaClExtractor;
import edu.arizona.biosemantics.micropie.io.CSVAbbreviationReader;
import edu.arizona.biosemantics.micropie.io.CSVClassifiedSentenceWriter;
import edu.arizona.biosemantics.micropie.io.CSVTaxonCharacterMatrixWriter;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.arizona.biosemantics.micropie.nlptool.ITextNormalizer;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.arizona.biosemantics.micropie.nlptool.StanfordParserWrapper;
import edu.arizona.biosemantics.micropie.nlptool.TextNormalizer;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class PHTempNaClExtractorTest {
	
	
	public static void main(String[] args){
		String inputDirectory = "F:/MicroPIE/micropie0.2_model";
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
		stanfordCoreProperties.put("annotators", "tokenize, ssplit");
		StanfordCoreNLP sfCoreNLP = new StanfordCoreNLP(stanfordCoreProperties);
		LexicalizedParser lexParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		String serializedClassifierModel = "F:/MicroPIE/micropie0.2_model\\nlpmodel/english.all.3class.distsim.crf.ser.gz";
		AbstractSequenceClassifier nerClassifier=CRFClassifier.getClassifierNoExceptions(serializedClassifierModel);
		
		StanfordParserWrapper stanfordWrapper = new StanfordParserWrapper(sfCoreNLP, lexParser,nerClassifier);
		
		String posTagModel = "edu/stanford/nlp/models/pos-tagger/english-bidirectional/english-bidirectional-distsim.tagger";
		PosTagger postagger = new PosTagger(posTagModel);
		SentenceSpliter sentSplitter = new SentenceSpliter(textNormalizer, stanfordWrapper);
		
		
		
		SentencePredictor sentencePredictor = null;
		ListeningExecutorService executorService = null;
		CSVClassifiedSentenceWriter classifiedSentenceWriter = null;
		CSVTaxonCharacterMatrixWriter matrixWriter =null;
		TaxonCharacterMatrixCreator matrixCreator =null;
		ICharacterValueExtractorProvider contentExtractorProvider = null;// extractors
		
		Map sentenceMetadataMap = new HashMap();
		Map<RawSentence, MultiClassifiedSentence> sentenceClassificationMap = new HashMap();
		
		SentenceBatchProcessor sentBatPIEProcessor =  new SentenceBatchProcessor(matrixCreator, matrixWriter, true, 3, sentenceMetadataMap, sentenceMetadataMap, sentenceClassificationMap, sentencePredictor, sentSplitter, classifiedSentenceWriter, contentExtractorProvider);
		//String lineFile = "F:/MicroPIE/micropieInput/sentences/3.2 Nacl Min small2.csv";//3.2 Nacl Min small.csv
		String lineFile = "F:/MicroPIE/micropieInput/sentences/3.1G salinity preference simple2.txt";
		//phsamples.txt
		//3.2 Nacl Min small.csv
		//STEP 1: split sentences
		List<RawSentence> testSentences = sentBatPIEProcessor.createSentencesByLine(lineFile);
				
		//STEP 2: predict the classifications of the sentences, i.e., the characters in each sentences
		List<MultiClassifiedSentence> predictions = new LinkedList<MultiClassifiedSentence>();
		PHTempNaClExtractor extractor = new PHTempNaClExtractor(sentSplitter, postagger, null, null);
		PostProcessor pp = new PostProcessor();
		for (RawSentence testSentence : testSentences) {
			Set prediction = new HashSet();
			prediction.add(Label.c17);
			
			MultiClassifiedSentence classifiedSentence = new MultiClassifiedSentence(testSentence, prediction);
			String character = extractor.getCharacterName();
			
			List<CharacterValue> content = extractor.getCharacterValue(classifiedSentence);
			System.out.println("\n before USP values: "+content);
			
			List<CharacterValue> noLabelValueList = new ArrayList();
			//pp.seperateLabelAndUnlabelList(content,noLabelValueList);
			pp.dealUSP(noLabelValueList, new HashMap());
			//pp.postProcessor(content,new ArrayList());
			System.out.println(testSentence.getText()+"\n after USP values: "+content+" "+noLabelValueList);
		}
		
	}
}
