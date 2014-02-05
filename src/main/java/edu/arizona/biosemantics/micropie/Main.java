package edu.arizona.biosemantics.micropie;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.classify.MultiSVMClassifier;
import edu.arizona.biosemantics.micropie.io.CSVAbbreviationReader;
import edu.arizona.biosemantics.micropie.io.CSVClassifiedSentenceWriter;
import edu.arizona.biosemantics.micropie.io.CSVTaxonCharacterMatrixWriter;
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.io.XMLTextReader;
import edu.arizona.biosemantics.micropie.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.ClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.transform.ITextTransformer;
import edu.arizona.biosemantics.micropie.transform.MyTaxonCharacterMatrixCreator;
import edu.arizona.biosemantics.micropie.transform.TextNormalizer;
import edu.arizona.biosemantics.micropie.transform.feature.IFilterDecorator;
import edu.arizona.biosemantics.micropie.transform.feature.MyFilterDecorator;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;

public class Main {

	// User Input
	private String trainingFile = "131001-sampleCombinedSentencesList-csv-CB-manipulated-by-EW-131030-test-3-copy-2.csv";
	private String abbreviationFile = "abbrevlist.csv";
	private String testFolder = "test";
	private boolean parallelProcessing = true;
	private int testSentenceExtractorMax = 5;
	private String predictionsFile = "predictions.csv";
	private String matrixFile = "matrix.csv";
	
	// Non-User Input
	private ExecutorService executorService;
	private Map<Sentence, ClassifiedSentence> sentenceClassificationMap;
	private Map<Sentence, SentenceMetadata> sentenceMetadataMap;
	private Map<String, List<Sentence>> taxonSentencesMap;
	
	public Main(){
		if(!this.parallelProcessing)
			executorService = Executors.newSingleThreadExecutor();
		if(this.parallelProcessing && this.testSentenceExtractorMax < Integer.MAX_VALUE)
			executorService = Executors.newFixedThreadPool(testSentenceExtractorMax);
		if(this.parallelProcessing && this.testSentenceExtractorMax == Integer.MAX_VALUE)
			executorService = Executors.newCachedThreadPool();
	}
	
	public void run() {
		sentenceClassificationMap = new HashMap<Sentence, ClassifiedSentence>();
		sentenceMetadataMap = new HashMap<Sentence, SentenceMetadata>();
		taxonSentencesMap = new HashMap<String, List<Sentence>>();
		
		try {
			MultiSVMClassifier classifier = setupClassifier();
			List<Sentence> trainingSentences = createTrainSentences();
			trainClassifier(classifier, trainingSentences);
			List<Sentence> testSentences = createTestSentences();
			List<ClassifiedSentence> predictions = classifySentences(classifier, testSentences);
			writePredictionResults(predictions);
			TaxonCharacterMatrix matrix = createMatrix();
			writeMatrix(matrix);
		} catch(Exception e) {
			log(LogLevel.ERROR, "Could not run Main", e);
		}
		
		executorService.shutdown();
	}
	
	private MultiSVMClassifier setupClassifier() {
		log(LogLevel.INFO, "Setup classifier...");
		IFilterDecorator filterDecorator = new MyFilterDecorator(1, 1, 1);
		MultiSVMClassifier classifier = new MultiSVMClassifier(Label.values(), filterDecorator);
		log(LogLevel.INFO, "Done setting up classifier");
		return classifier;
	}
	
	private void trainClassifier(MultiSVMClassifier classifier, List<Sentence> trainingSentences) throws Exception {
		log(LogLevel.INFO, "Training classifier...");
		classifier.train(trainingSentences);
		log(LogLevel.INFO, "Done training");
	}
	
	private List<Sentence> createTestSentences() throws IOException, InterruptedException, ExecutionException {
		log(LogLevel.INFO, "Reading test sentences...");
		File inputFolder = new File(testFolder);
		XMLTextReader textReader = new XMLTextReader();
		
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
		
		CSVAbbreviationReader abbreviationReader = new CSVAbbreviationReader();
		abbreviationReader.setInputStream(new FileInputStream(abbreviationFile));
		LinkedHashMap<String, String> abbreviations = abbreviationReader.read();
		ITextTransformer textNormalizer = new TextNormalizer(abbreviations);
		Properties stanfordCoreProperties = new Properties();
		stanfordCoreProperties.put("annotators", "tokenize, ssplit");
		StanfordCoreNLP stanfordCoreNLP = new StanfordCoreNLP(stanfordCoreProperties);
		
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
		
		LexicalizedParser lexicalizedParser = LexicalizedParser
				.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
		
		int numberOfSentences = getNumberOfSentences(sentenceSplits);
		System.out.println(numberOfSentences);
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
		
		System.out.println("waiting");
		try {
			compoundSentenceSplitLatch.await();
		} catch (InterruptedException e) {
			log(LogLevel.ERROR, "Problem with latch", e);
		}
		System.out.println("done waiting");
		
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

	private List<Sentence> createTrainSentences() throws Exception {
		log(LogLevel.INFO, "Reading training data...");
		CSVSentenceReader reader = new CSVSentenceReader();
		// reader.setInputStream(new FileInputStream("131001-sampleCombinedSentencesList-csv-CB-manipulated-by-EW-131030-test-3.csv"));
		reader.setInputStream(new FileInputStream(trainingFile));
		List<Sentence> trainingSentences = reader.read();	
		log(LogLevel.INFO, "Done reading training sentences...");
		return trainingSentences;
	}
	
	private List<ClassifiedSentence> classifySentences(MultiSVMClassifier classifier,
			List<Sentence> testSentences) throws Exception {
		//TODO parallelize here
		List<ClassifiedSentence> predictionResult = new LinkedList<ClassifiedSentence>();
		log(LogLevel.INFO, "Predicting classes for test sentences...");
		for(Sentence testSentence : testSentences) {
			Set<ILabel> predictions = classifier.getClassification(testSentence);
			ClassifiedSentence classifiedSentence = new ClassifiedSentence(testSentence, predictions);
			sentenceClassificationMap.put(testSentence, classifiedSentence);
			predictionResult.add(classifiedSentence);
		}
		log(LogLevel.INFO, "Done predicting classes for test sentences");
		return predictionResult;
	}

	private void writePredictionResults(List<ClassifiedSentence> predictions) throws Exception {
		log(LogLevel.INFO, "Writing prediciton results to " + predictionsFile + "...");
		CSVClassifiedSentenceWriter classifiedSentenceWriter = new CSVClassifiedSentenceWriter();
		classifiedSentenceWriter.setOutputStream(new FileOutputStream(predictionsFile));
		classifiedSentenceWriter.write(predictions);
		log(LogLevel.INFO, "Done writing prediciton results");
	}
		
	private TaxonCharacterMatrix createMatrix() {
		log(LogLevel.INFO, "Creating matrix...");
		MyTaxonCharacterMatrixCreator matrixCreator = new MyTaxonCharacterMatrixCreator();
		TaxonCharacterMatrix matrix = matrixCreator.create(taxonSentencesMap, sentenceMetadataMap, sentenceClassificationMap);
		log(LogLevel.INFO, "Done creating matrix");
		return matrix;
	}

	private void writeMatrix(TaxonCharacterMatrix matrix) throws Exception {
		log(LogLevel.INFO, "Writing matrix to " + matrixFile + "...");
		CSVTaxonCharacterMatrixWriter matrixWriter = new CSVTaxonCharacterMatrixWriter();
		matrixWriter.setOutputStream(new FileOutputStream(matrixFile));
		matrixWriter.write(matrix);
		log(LogLevel.INFO, "Done writing prediciton results");
	}

	public static void main(String[] args) {		
		Main main = new Main();
		main.run();
	}

}
