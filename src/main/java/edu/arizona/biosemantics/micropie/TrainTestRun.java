package edu.arizona.biosemantics.micropie;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.MultiSVMClassifier;
import edu.arizona.biosemantics.micropie.io.CSVClassifiedSentenceWriter;
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.io.CSVTaxonCharacterMatrixWriter;
import edu.arizona.biosemantics.micropie.io.XMLTextReader;
import edu.arizona.biosemantics.micropie.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.ClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.transform.CompoundSentenceSplitRun;
import edu.arizona.biosemantics.micropie.transform.ITextNormalizer;
import edu.arizona.biosemantics.micropie.transform.SentenceSplitRun;
import edu.arizona.biosemantics.micropie.transform.TaxonCharacterMatrixCreator;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.TokenizerFactory;

public class TrainTestRun implements IRun {

	private String trainingFile;
	private String testFolder;
	private String predictionsFile;
	private String matrixFile;

	private CSVSentenceReader sentenceReader;
	private MultiSVMClassifier classifier;
	private XMLTextReader textReader;
	private ITextNormalizer textNormalizer;
	private LexicalizedParser lexicalizedParser;
	private TokenizerFactory<CoreLabel> tokenizerFactory;
	private StanfordCoreNLP stanfordCoreNLP;
	private CSVClassifiedSentenceWriter classifiedSentenceWriter;
	private TaxonCharacterMatrixCreator matrixCreator;
	private CSVTaxonCharacterMatrixWriter matrixWriter;
	
	private boolean parallelProcessing;
	private int maxThreads;
	private ExecutorService executorService;
	
	private Map<Sentence, ClassifiedSentence> sentenceClassificationMap;
	private Map<Sentence, SentenceMetadata> sentenceMetadataMap;
	private Map<String, List<Sentence>> taxonSentencesMap;
	
	@Inject
	public TrainTestRun(@Named("trainingFile") String trainingFile,
			@Named("testFolder") String testFolder, 
			@Named("parallelProcessing") boolean parallelProcessing, 
			@Named("maxThreads") int maxThreads, 
			@Named("predictionsFile") String predictionsFile, 
			@Named("matrixFile") String matrixFile,
			@Named("SentenceClassificationMap")Map<Sentence, ClassifiedSentence> sentenceClassificationMap,
			@Named("SentenceMetadataMap")Map<Sentence, SentenceMetadata> sentenceMetadataMap,
			@Named("TaxonSentencesMap")Map<String, List<Sentence>> taxonSentencesMap,
			MultiSVMClassifier classifier,
			CSVSentenceReader sentenceReader,
			XMLTextReader textReader,
			ITextNormalizer textNormalizer,
			StanfordCoreNLP stanfordCoreNLP,
			LexicalizedParser lexicalizedParser,
			TokenizerFactory<CoreLabel> tokenizerFactory, 
			CSVClassifiedSentenceWriter classifiedSentenceWriter, 
			TaxonCharacterMatrixCreator matrixCreator, 
			CSVTaxonCharacterMatrixWriter matrixWriter
			){
		this.trainingFile = trainingFile;
		this.testFolder = testFolder;
		this.parallelProcessing = parallelProcessing;
		this.maxThreads = maxThreads;
		this.predictionsFile = predictionsFile;
		this.matrixFile = matrixFile;
		this.sentenceClassificationMap = sentenceClassificationMap;
		this.sentenceMetadataMap = sentenceMetadataMap;
		this.taxonSentencesMap = taxonSentencesMap;
		this.classifier = classifier;
		this.sentenceReader = sentenceReader;
		this.textReader = textReader;
		this.textNormalizer = textNormalizer;
		this.stanfordCoreNLP = stanfordCoreNLP;
		this.lexicalizedParser = lexicalizedParser;
		this.tokenizerFactory = tokenizerFactory;
		this.classifiedSentenceWriter = classifiedSentenceWriter;
		this.matrixCreator = matrixCreator;
		this.matrixWriter = matrixWriter;
		
		if(!this.parallelProcessing)
			executorService = Executors.newSingleThreadExecutor();
		if(this.parallelProcessing && this.maxThreads < Integer.MAX_VALUE)
			executorService = Executors.newFixedThreadPool(maxThreads);
		if(this.parallelProcessing && this.maxThreads == Integer.MAX_VALUE)
			executorService = Executors.newCachedThreadPool();
	}
	
	@Override
	public void run() {		
		try {
			sentenceReader.setInputStream(new FileInputStream(trainingFile));
			List<Sentence> trainingSentences = sentenceReader.read();	
			classifier.train(trainingSentences);
			
			List<Sentence> testSentences = createTestSentences();
			List<ClassifiedSentence> predictions = new LinkedList<ClassifiedSentence>();	
			//TODO possibly parallelize here
			for(Sentence testSentence : testSentences) {
				Set<ILabel> prediction = classifier.getClassification(testSentence);
				ClassifiedSentence classifiedSentence = new ClassifiedSentence(testSentence, prediction);
				sentenceClassificationMap.put(testSentence, classifiedSentence);
				predictions.add(classifiedSentence);
			}
			
			classifiedSentenceWriter.setOutputStream(new FileOutputStream(predictionsFile));
			classifiedSentenceWriter.write(predictions);
			
			TaxonCharacterMatrix matrix = matrixCreator.create();
			matrixWriter.setOutputStream(new FileOutputStream(matrixFile));
			matrixWriter.write(matrix);
		} catch(Exception e) {
			log(LogLevel.ERROR, "Could not run Main", e);
		}
		
		executorService.shutdown();
	}
	
	private List<Sentence> createTestSentences() throws IOException, InterruptedException, ExecutionException {
		log(LogLevel.INFO, "Reading test sentences...");
		File inputFolder = new File(testFolder);
		File[] inputFiles = inputFolder.listFiles();		 
		List<TaxonTextFile> textFiles = new LinkedList<TaxonTextFile>();
		for(File inputFile : inputFiles) {
			log(LogLevel.INFO, "Reading from " + inputFile.getName() + "...");
			try {
				textReader.setInputStream(new FileInputStream(inputFile));
				String taxon = textReader.getTaxon();
				log(LogLevel.INFO, "Taxon: " + taxon);
				String text = textReader.read();
				log(LogLevel.INFO, "Text: " + text);
				textFiles.add(new TaxonTextFile(taxon, text, inputFile));
			} catch(Exception e) {
				log(LogLevel.ERROR, "Could not read test sentences from " + inputFile.getName(), e);
			}
		}
		
		List<Future<List<String>>> sentenceSplits = new ArrayList<Future<List<String>>>(textFiles.size());
		CountDownLatch sentenceSplitLatch = new CountDownLatch(textFiles.size());
		for(TaxonTextFile textFile : textFiles) {
			SentenceSplitRun splitRun = new SentenceSplitRun(textFile.getText(), textNormalizer, stanfordCoreNLP, 
					sentenceSplitLatch);
			Future<List<String>> futureResult = executorService.submit(splitRun);
			sentenceSplits.add(futureResult);
		}
		
		try {
			sentenceSplitLatch.await();
		} catch (InterruptedException e) {
			log(LogLevel.ERROR, "Problem with latch", e);
		}
		
		int numberOfSentences = getNumberOfSentences(sentenceSplits);
		List<List<Future<List<String>>>> subsentenceSplitsPerFile = new LinkedList<List<Future<List<String>>>>();
		CountDownLatch compoundSentenceSplitLatch = new CountDownLatch(numberOfSentences);
		for(int i=0; i<textFiles.size(); i++) {
			List<String> sentences = sentenceSplits.get(i).get();
			List<Future<List<String>>> subsentenceSplits = new LinkedList<Future<List<String>>>();
			for(String sentence : sentences) {
				CompoundSentenceSplitRun splitRun = new CompoundSentenceSplitRun(sentence, lexicalizedParser, 
						tokenizerFactory, compoundSentenceSplitLatch);
				Future<List<String>> futureResult = executorService.submit(splitRun);
				subsentenceSplits.add(futureResult);
			}
			subsentenceSplitsPerFile.add(subsentenceSplits);
		}
		
		try {
			compoundSentenceSplitLatch.await();
		} catch (InterruptedException e) {
			log(LogLevel.ERROR, "Problem with latch", e);
		}
		
		List<Sentence> result = new LinkedList<Sentence>();
		for(int i=0; i<textFiles.size(); i++) {
			List<String> sentences = sentenceSplits.get(i).get();
			for(int j=0; j<sentences.size(); j++) {
				List<String> subsentences = subsentenceSplitsPerFile.get(i).get(j).get();
				for(String subsentence : subsentences) {
					Sentence sentence = new Sentence(subsentence);
					result.add(sentence);
					SentenceMetadata metadata = new SentenceMetadata();
					metadata.setSourceId(j);
					metadata.setTaxonTextFile(textFiles.get(i));
					metadata.setCompoundSplitSentence(subsentences.size() > 1);
					//metadata.setParseResult(textSentenceTransformer.getCachedParseResult(sentence));
					sentenceMetadataMap.put(sentence, metadata);
					String taxon = textFiles.get(i).getTaxon();
					if(!taxonSentencesMap.containsKey(taxon))
						taxonSentencesMap.put(taxon, new LinkedList<Sentence>());
					taxonSentencesMap.get(taxon).add(sentence);
				}
			}
		}
		
		log(LogLevel.INFO, "Done reading test sentences...");
		return result;
	}
	
	private int getNumberOfSentences(List<Future<List<String>>> sentenceSplitsList) throws InterruptedException, ExecutionException {
		int i=0;
		for(Future<List<String>> sentenceSplits : sentenceSplitsList) {
			List<String> sentences = sentenceSplits.get();
			i += sentences.size();
		}
		return i;
	}


}
