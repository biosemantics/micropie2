

package edu.arizona.biosemantics.micropie;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;


import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import usp.semantic.Parse;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.MultiSVMClassifier;
import edu.arizona.biosemantics.micropie.extract.TaxonCharacterMatrixCreator;
import edu.arizona.biosemantics.micropie.io.CSVClassifiedSentenceWriter;
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.io.CSVTaxonCharacterMatrixWriter;
import edu.arizona.biosemantics.micropie.io.XMLTextReader;
import edu.arizona.biosemantics.micropie.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.CollapseUSPSentByCategoryCharTokenizer;
import edu.arizona.biosemantics.micropie.model.CollapseUSPSentIndexMapping;
import edu.arizona.biosemantics.micropie.model.CollapsedSentenceAndIndex;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.model.USPTermIndex;
import edu.arizona.biosemantics.micropie.transform.CollapseUSPSentByCategoryChar;
import edu.arizona.biosemantics.micropie.transform.CompoundSentenceSplitRun;
import edu.arizona.biosemantics.micropie.transform.ITextNormalizer;
import edu.arizona.biosemantics.micropie.transform.SentenceSplitRun;
import edu.arizona.biosemantics.micropie.extract.ExtractorType;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.StringLabelFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreeReader;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;
import edu.stanford.nlp.util.CoreMap;

public class TrainTestRun implements IRun {

	private String trainingFile;
	private String testFolder;
	private String uspFolder;
	private String characterValueExtractorsFolder;
	private String predictionsFile;
	private String matrixFile;

	private CSVSentenceReader trainingSentenceReader;
	private CSVSentenceReader additionalUSPInputSentenceReader;
	
	private MultiSVMClassifier classifier;
	private XMLTextReader textReader;
	private ITextNormalizer textNormalizer;
	private LexicalizedParser lexicalizedParser;
	private StanfordCoreNLP tokenizeSSplit;
	private StanfordCoreNLP tokenizeSSplitPosParse;
	private CSVClassifiedSentenceWriter classifiedSentenceWriter;
	private TaxonCharacterMatrixCreator matrixCreator;
	private CSVTaxonCharacterMatrixWriter matrixWriter;

	private boolean parallelProcessing;
	private int maxThreads;
	private ListeningExecutorService executorService;

	private Map<Sentence, MultiClassifiedSentence> sentenceClassificationMap;
	private Map<Sentence, SentenceMetadata> sentenceMetadataMap;
	private Map<TaxonTextFile, List<Sentence>> taxonSentencesMap;

	private String celsius_degreeReplaceSourcePattern;
	
	
	
	@Inject
	public TrainTestRun(
			@Named("trainingFile") String trainingFile,
			@Named("testFolder") String testFolder,
			@Named("uspFolder") String uspFolder,
			@Named("characterValueExtractorsFolder") String characterValueExtractorsFolder,
			@Named("parallelProcessing") boolean parallelProcessing,
			@Named("maxThreads") int maxThreads,
			@Named("predictionsFile") String predictionsFile,
			@Named("matrixFile") String matrixFile,
			@Named("SentenceClassificationMap") Map<Sentence, MultiClassifiedSentence> sentenceClassificationMap,
			@Named("SentenceMetadataMap") Map<Sentence, SentenceMetadata> sentenceMetadataMap,
			@Named("TaxonSentencesMap") Map<TaxonTextFile, List<Sentence>> taxonSentencesMap,
			MultiSVMClassifier classifier,
			CSVSentenceReader trainingSentenceReader,
			XMLTextReader textReader,
			ITextNormalizer textNormalizer,
			@Named("TokenizeSSplit") StanfordCoreNLP tokenizeSSplit,
			@Named("TokenizeSSplitPosParse") StanfordCoreNLP tokenizeSSplitPosParse,
			LexicalizedParser lexicalizedParser,
			CSVClassifiedSentenceWriter classifiedSentenceWriter,
			TaxonCharacterMatrixCreator matrixCreator,
			CSVTaxonCharacterMatrixWriter matrixWriter,
			
			@Named("celsius_degreeReplaceSourcePattern") String celsius_degreeReplaceSourcePattern
			
			) {
		this.trainingFile = trainingFile;
		this.testFolder = testFolder;
		this.uspFolder = uspFolder;
		this.characterValueExtractorsFolder = characterValueExtractorsFolder;
		this.parallelProcessing = parallelProcessing;
		this.maxThreads = maxThreads;
		this.predictionsFile = predictionsFile;
		this.matrixFile = matrixFile;
		this.sentenceClassificationMap = sentenceClassificationMap;
		this.sentenceMetadataMap = sentenceMetadataMap;
		this.taxonSentencesMap = taxonSentencesMap;
		this.classifier = classifier;
		
		this.trainingSentenceReader = trainingSentenceReader;
		this.additionalUSPInputSentenceReader = additionalUSPInputSentenceReader;
		
		this.textReader = textReader;
		this.textNormalizer = textNormalizer;
		this.tokenizeSSplit = tokenizeSSplit;
		this.tokenizeSSplitPosParse = tokenizeSSplitPosParse;
		this.lexicalizedParser = lexicalizedParser;
		this.classifiedSentenceWriter = classifiedSentenceWriter;
		this.matrixCreator = matrixCreator;
		this.matrixWriter = matrixWriter;

		
		this.celsius_degreeReplaceSourcePattern = celsius_degreeReplaceSourcePattern;
		
		
		
		if (!this.parallelProcessing)
			executorService = MoreExecutors.listeningDecorator(Executors
					.newSingleThreadExecutor());
		if (this.parallelProcessing && this.maxThreads < Integer.MAX_VALUE)
			executorService = MoreExecutors.listeningDecorator(Executors
					.newFixedThreadPool(maxThreads));
		if (this.parallelProcessing && this.maxThreads == Integer.MAX_VALUE)
			executorService = MoreExecutors.listeningDecorator(Executors
					.newCachedThreadPool());
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		try {

			// NFolder Cross Validation
			// trainingSentenceReader.setInputStream(new FileInputStream(trainingFile));
			// List<Sentence> trainingSentences = trainingSentenceReader.read();
			// nFolderCrossValidation(10, trainingSentences);

			// stanfordCoreNLPTest();
			// stanfordCoreNLPTest2();
			
			
			// Create USP Inputs
			// List<Sentence> testSentences = createTestSentences();
			// createUSPInputs(testSentences);
			// Create USP Inputs

			
			
			// Run USP Parse
			// Parse uspParse = new Parse();
			// uspParse.runParse("usp", "usp_results");
			// Run USP Parse

			
			
			
			// Small tool 1:: Split compound category from training data set
			// trainingSentenceReader.setInputStream(new FileInputStream(trainingFile));
			// //trainingSentenceReader.setInputStream(new FileInputStream("training-base-140507.csv"));
			// trainingSentenceReader.setOutputStream(new FileOutputStream("split-" + trainingFile));
			// //trainingSentenceReader.setOutputStream(new FileOutputStream("split-training-base-140507.csv"));
			// trainingSentenceReader.splitCompoundCategory();
			
			// trainingSentenceReader.setInputStream(new FileInputStream("additionalUSPInputs.csv"));
			// trainingSentenceReader.setOutputStream(new FileOutputStream("split-additionalUSPInputs.csv"));
			// trainingSentenceReader.splitCompoundCategory();
			
			// trainingSentenceReader.setInputStream(new FileInputStream("predictions-140528-3.csv"));
			// trainingSentenceReader.setOutputStream(new FileOutputStream("split-predictions-140528-3.csv"));
			// trainingSentenceReader.splitCompoundCategory();

			// trainingSentenceReader.setInputStream(new FileInputStream("predictions-140311-1.csv"));
			// trainingSentenceReader.setOutputStream(new FileOutputStream("split-predictions-140311-1.csv"));
			// trainingSentenceReader.splitCompoundCategory();
			
			// Small tool 1:: Split compound category from training data set
			
			
			
			// Small tool 2:: Training data set statistics
			// trainingSentenceReader.setInputStream(new FileInputStream(trainingFile));
			////trainingSentenceReader.setInputStream(new FileInputStream("split-training-base-140507.csv"));
			// trainingSentenceReader.categoryStat();
			// 
			// Calculation on June 04, 2014 Wednesday
			// trainingSentenceReader.setInputStream(new FileInputStream("split-training-base-140603.csv"));
			// trainingSentenceReader.categoryStat();
			// Small tool 2:: Training data set statistics
			
			
			
			// readNewXml();
			// testAAA();
			
			// temporary use
			// trainingSentenceReader.setInputStream(new FileInputStream("new-inputs.txt"));
			// trainingSentenceReader.readTaxonomicDescAndWriteToSingleTxt("new-inputs-filtered.txt");
			
			
			
			/*
			// temporary use :: build the predictions for each sentence
			trainingSentenceReader.setInputStream(new FileInputStream(trainingFile));
			List<Sentence> trainingSentences = trainingSentenceReader.read();
			classifier.train(trainingSentences);
			
			List<Sentence> testSentences = createTestSentences();
			// Reed String testFolder from Config.java
			// private String testFolder = "new-microbe-xml-new-inputs-from-carrine";
			
			List<MultiClassifiedSentence> predictions = new LinkedList<MultiClassifiedSentence>(); // TODO possibly parallelize here
			for (Sentence testSentence : testSentences) {
				Set<ILabel> prediction = classifier.getClassification(testSentence);
				MultiClassifiedSentence classifiedSentence = new MultiClassifiedSentence( testSentence, prediction);
				sentenceClassificationMap.put(testSentence,classifiedSentence);
				predictions.add(classifiedSentence);
			}
			
			classifiedSentenceWriter.setOutputStream(new FileOutputStream(predictionsFile));
			classifiedSentenceWriter.write(predictions);
			// temporary use :: build the predictions for each sentence
			*/
			
			
			
			
			
			// Small tool 3:: Transformer:: CSV to Excel (2007 format)
			// trainingSentenceReader.setInputStream(new FileInputStream("split-training-base-140507.csv"));
			// trainingSentenceReader.csvToXls("split-training-base-140507.xls");
			
			
			// xls2csv.csv
			// trainingSentenceReader.setInputStream(new FileInputStream("xls2csv.csv"));
			// trainingSentenceReader.csvToXls("xls2csv.xls");
			// trainingSentenceReader.setInputStream(new FileInputStream("split-training-base-140603.csv"));
			// trainingSentenceReader.csvToXls("split-training-base-140603.xls");

			// Small tool 3:: Transformer:: CSV to Excel (2007 format)
			
			
			
			// USP example
			// createUSPInputs(testSentences);
			// Parse uspParse = new Parse();
			// uspParse.runParse("usp", "usp_results");
			// USP example
			
			// Small tool 4:: Pre-processing::Build USP inputs first from C
			// trainingSentenceReader.setInputStream(new FileInputStream("split-additionalUSPInputs.csv"));
			// trainingSentenceReader.setInputStream(new FileInputStream("split-additionalUSPInputs_short_for_testing.csv"));
			// List<Sentence> additionalUSPInputSentences = trainingSentenceReader.readAdditionalUSPInputs();
			// createUSPInputsFromListSentence(additionalUSPInputSentences);
			// Small tool 4:: Pre-processing::Build USP inputs first from C
			
			
			
			
			
			// formal MicroPIE process
			trainingSentenceReader.setInputStream(new FileInputStream(trainingFile));
			List<Sentence> trainingSentences = trainingSentenceReader.read();
			classifier.train(trainingSentences);
			
			List<Sentence> testSentences = createTestSentences();
			
			
			List<MultiClassifiedSentence> predictions = new LinkedList<MultiClassifiedSentence>(); // TODO possibly parallelize here
			for (Sentence testSentence : testSentences) {
				Set<ILabel> prediction = classifier.getClassification(testSentence);
				MultiClassifiedSentence classifiedSentence = new MultiClassifiedSentence( testSentence, prediction);
				sentenceClassificationMap.put(testSentence,classifiedSentence);
				predictions.add(classifiedSentence);
			}
						
			

			// 
			// USP
			createUSPInputs(predictions);
			
			Parse uspParse = new Parse();
			uspParse.runParse("usp", "usp_results");
			// USP

			
			
			classifiedSentenceWriter.setOutputStream(new FileOutputStream(predictionsFile));
			classifiedSentenceWriter.write(predictions);
			TaxonCharacterMatrix matrix = matrixCreator.create();
			matrixWriter.setOutputStream(new FileOutputStream(matrixFile));
			matrixWriter.write(matrix);
			
			// formal MicroPIE process
			
			
			// Using small tool 3:: Transformer:: CSV to Excel (2007 format)
			// trainingSentenceReader.setInputStream(new FileInputStream("140604-20more-sentences-to-be-determined.csv"));
			// trainingSentenceReader.csvToXls("140604-20more-sentences-to-be-determined.xls");
			
			
			trainingSentenceReader.setInputStream(new FileInputStream("matrix.csv"));
			trainingSentenceReader.csvToXls("matrix.xls");
			
			
			

		} catch (Exception e) {
			log(LogLevel.ERROR, "Could not run Main", e);
		}

		executorService.shutdown();
		System.out.println("DONE: "
				+ ((long) System.currentTimeMillis() - startTime) + " ms");
	}

	private List<Sentence> createTestSentences() throws IOException,
			InterruptedException, ExecutionException {
		log(LogLevel.INFO, "Reading test sentences...");
		File inputFolder = new File(testFolder);
		File[] inputFiles = inputFolder.listFiles();
		List<TaxonTextFile> textFiles = new LinkedList<TaxonTextFile>();
		for (File inputFile : inputFiles) {
			log(LogLevel.INFO, "Reading from " + inputFile.getName() + "...");
			try {
				textReader.setInputStream(new FileInputStream(inputFile));
				String taxon = textReader.getTaxon();
				log(LogLevel.INFO, "Taxon: " + taxon);
				String text = textReader.read();
				log(LogLevel.INFO, "Text: " + text);
				textFiles.add(new TaxonTextFile(taxon, text, inputFile));
			} catch (Exception e) {
				log(LogLevel.ERROR, "Could not read test sentences from "
						+ inputFile.getName(), e);
			}
		}

		List<ListenableFuture<List<String>>> sentenceSplits = new ArrayList<ListenableFuture<List<String>>>(
				textFiles.size());
		CountDownLatch sentenceSplitLatch = new CountDownLatch(textFiles.size());
		for (TaxonTextFile textFile : textFiles) {
			SentenceSplitRun splitRun = new SentenceSplitRun(
					textFile.getText(), textNormalizer, tokenizeSSplit,
					sentenceSplitLatch,
					celsius_degreeReplaceSourcePattern
					
					);
			ListenableFuture<List<String>> futureResult = executorService
					.submit(splitRun);
			sentenceSplits.add(futureResult);
		}

		try {
			sentenceSplitLatch.await();
		} catch (InterruptedException e) {
			log(LogLevel.ERROR, "Problem with latch", e);
		}

		// TODO Parallel processing doesn't work right, gives
		// NullPointerException in getBestParse() from LexicalizedParser and
		// NoSuchParseException also
		// They should
		// - not occur if enough memory is used
		// - or something is not set up right with threads competing for the use
		// of them? However Stanford CoreNLP claims to be thread safe? Is
		// ClausIE not?
		// - these exceptions, if not treated right, and a countdownlatch is
		// used, can cause the latch to not count to zero and thus make main
		// thread continue
		// - Try to cut down on sentence length for the sentences passed to
		// ClausIE to reduce computation time and memory necessary to get best
		// parse (exponential complexity?)
		// - Try to pre-pos tag things known
		// --e.g. replace phrases such as L-arabinose, fructose, sucrose,
		// L-sorbitol, glucose-1-phosphate, glucose-6-phosphate, maltose, ... by
		// ENUMERATION to allow easy parse?
		// --then whatever outcome parse has use it on all the elements?

		// http://stackoverflow.com/questions/19243260/stanford-corenlp-failing-only-on-windows
		// http://stackoverflow.com/questions/12305667/how-is-exception-handling-done-in-a-callable
		// http://nlp.stanford.edu/downloads/parser-faq.shtml#n
		// http://nlp.stanford.edu/downloads/parser-faq.shtml#k
		// -> sort sentences according to length buckets and use number of
		// threads according to
		// approx memory usage for the buckets, e.g. can do 4 of max length 20
		// at once if ~1024M Heap.
		// http://nlp.stanford.edu/downloads/corenlp-faq.shtml#memory
		// http://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/parser/lexparser/LexicalizedParser.html

		int numberOfSentences = getNumberOfSentences(sentenceSplits);
		List<List<ListenableFuture<List<String>>>> subsentenceSplitsPerFile = new LinkedList<List<ListenableFuture<List<String>>>>();
		// final CountDownLatch compoundSentenceSplitLatch = new
		// CountDownLatch(numberOfSentences);
		// final CountDownLatch compoundSentenceSplitLatchDummy = new
		// CountDownLatch(numberOfSentences);
		// int overall = 0;
		// int maxSize = 0;
		for (int i = 0; i < textFiles.size(); i++) {
			List<String> sentences = sentenceSplits.get(i).get();
			List<ListenableFuture<List<String>>> subsentenceSplits = new LinkedList<ListenableFuture<List<String>>>();
			for (final String sentence : sentences) {

				// String[] tokens = sentence.split("\\s+");
				// System.out.println("length " + tokens.length);
				//int tokenSize = tokens.length;
				// overall += size;
				// if(size > maxSize) { maxSize = size; }
				
				// if (sentence.length() <= 50) {
				if (sentence.length() <= 100) {
				// if (sentence.length() <= 200) {
				// if (tokenSize <= 30) {

					CompoundSentenceSplitRun splitRun = new CompoundSentenceSplitRun(
							sentence, lexicalizedParser, PTBTokenizer.factory(
									new CoreLabelTokenFactory(), ""));
					ListenableFuture<List<String>> futureResult = executorService
							.submit(splitRun);
					/*
					 * futureResult.addListener(new Runnable() {
					 * 
					 * @Override public void run() { System.out.println("done");
					 * // compoundSentenceSplitLatch.countDown(); //
					 * System.out.println
					 * (compoundSentenceSplitLatch.getCount()); } },
					 * this.executorService);
					 */
					subsentenceSplits.add(futureResult);
				} else {
					ListenableFuture<List<String>> futureResult = executorService
							.submit(new Callable<List<String>>() {
								@Override
								public List<String> call() throws Exception {
									List<String> result = new LinkedList<String>();
									result.add(sentence);
									return result;
								}
							});
					subsentenceSplits.add(futureResult);
				}
			}
			subsentenceSplitsPerFile.add(subsentenceSplits);
		}
		// double avgLength = (double)overall / numberOfSentences;
		// System.out.println("avg: " + avgLength);
		// System.out.println("maxSize: " + maxSize);

		for (List<ListenableFuture<List<String>>> fileFutures : subsentenceSplitsPerFile) {
			for (ListenableFuture<List<String>> future : fileFutures) {
				try {
					System.out.println("get");
					List<String> result = future.get();
				} catch (Exception e) {
					System.out.println("something went wrong with this guy");
					e.printStackTrace();
				}
			}
		}

		/*
		 * try { compoundSentenceSplitLatch.await(); } catch
		 * (InterruptedException e) { log(LogLevel.ERROR, "Problem with latch",
		 * e); }
		 */

		// non-threaded
		/*
		 * int numberOfSentences = getNumberOfSentences(sentenceSplits);
		 * List<List<List<String>>> subsentenceSplitsPerFile = new
		 * LinkedList<List<List<String>>>(); for(int i=0; i<textFiles.size();
		 * i++) { List<String> sentences = sentenceSplits.get(i).get();
		 * List<List<String>> subsentenceSplits = new
		 * LinkedList<List<String>>(); for(String sentence : sentences) {
		 * CompoundSentenceSplitRun splitRun = new
		 * CompoundSentenceSplitRun(sentence, lexicalizedParser,
		 * tokenizerFactory); try { List<String> result = splitRun.call();
		 * subsentenceSplits.add(result);
		 * System.out.println(numberOfSentences--); } catch(Exception e) {
		 * e.printStackTrace(); } }
		 * subsentenceSplitsPerFile.add(subsentenceSplits); }
		 */
		List<Sentence> result = new LinkedList<Sentence>();
		for (int i = 0; i < textFiles.size(); i++) {
			List<ListenableFuture<List<String>>> fileFuture = subsentenceSplitsPerFile
					.get(i);
			for (int j = 0; j < fileFuture.size(); j++) {
				List<String> subsentences = fileFuture.get(j).get();// .get();
				for (String subsentence : subsentences) {
					Sentence sentence = new Sentence(subsentence);
					result.add(sentence);
					SentenceMetadata metadata = new SentenceMetadata();
					metadata.setSourceId(j);
					metadata.setTaxonTextFile(textFiles.get(i));
					metadata.setCompoundSplitSentence(subsentences.size() > 1);
					// metadata.setParseResult(textSentenceTransformer.getCachedParseResult(sentence));
					sentenceMetadataMap.put(sentence, metadata);
					TaxonTextFile taxon = textFiles.get(i);
					if (!taxonSentencesMap.containsKey(taxon))
						taxonSentencesMap
								.put(taxon, new LinkedList<Sentence>());
					taxonSentencesMap.get(taxon).add(sentence);
				}
			}
		}

		log(LogLevel.INFO, "Done reading test sentences...");
		return result;
	}
	
	private List<Sentence> createTestSentences2() throws IOException,
	InterruptedException, ExecutionException {
		List<Sentence> result = new LinkedList<Sentence>();

		
		log(LogLevel.INFO, "Reading test sentences...");
		
		
		
		// Sentence sentence = new Sentence(subsentence);

		
		
		
		
		log(LogLevel.INFO, "Done reading test sentences...");
		return result;
	}
	

	private int getNumberOfSentences(
			List<ListenableFuture<List<String>>> sentenceSplitsList)
			throws InterruptedException, ExecutionException {
		int i = 0;
		for (ListenableFuture<List<String>> sentenceSplits : sentenceSplitsList) {
			List<String> sentences = sentenceSplits.get();
			i += sentences.size();
		}
		return i;
	}

	private void nFolderCrossValidation(int numberOfFold,
			List<Sentence> trainingSentences) throws Exception {

		StringBuilder outputStringBuilder = new StringBuilder();
		outputStringBuilder
				.append("Category,NumberOfRecord,TruePositive,FalsePositive,TrueNegative,FalseNegative,Precision,Recall,Accuracy\n");

		// int numberOfFold = 10;

		int foldInterval = (trainingSentences.size() / numberOfFold) + 1;

		System.out.println("Size :: " + trainingSentences.size());
		System.out.println("foldInterval :: " + foldInterval);

		String[][] labelPredictionPairLit = new String[trainingSentences.size()][2];

		int rowIndex = 0;

		for (int i = 0; i < numberOfFold; i++) {
			// List<List<Double>> testingStatTotal = new
			// ArrayList<List<Double>>();
			System.out.println("\nfold :: " + (i + 1));
			int startFlag = (foldInterval * i);
			int endFlag;
			if (i == (numberOfFold - 1)) {
				endFlag = (trainingSentences.size() - 1);
			} else {
				endFlag = (foldInterval * (i + 1) - 1);
			}
			System.out.println("testing data is :: " + startFlag + " to :: "
					+ endFlag);

			List<Sentence> subTrainingSentences = new LinkedList<Sentence>();
			List<Sentence> subTestingSentences = new LinkedList<Sentence>();
			subTrainingSentences.addAll(trainingSentences);

			for (int j = startFlag; j <= endFlag; j++) {
				subTrainingSentences.remove(trainingSentences.get(j));
				subTestingSentences.add(trainingSentences.get(j));
			}
			// System.out.println("trainingSentences.size()::" +
			// trainingSentences.size());
			// System.out.println("subTrainingSentences.size()::" +
			// subTrainingSentences.size());
			// System.out.println("subTestingSentences.size()::" +
			// subTestingSentences.size());

			classifier.train(subTrainingSentences);

			List<Sentence> testSentences = subTestingSentences;

			// TODO possibly parallelize here
			for (Sentence testSentence : testSentences) {
				Set<ILabel> prediction = classifier
						.getClassification(testSentence);
				String predictionString = "";

				Iterator<ILabel> iterator = prediction.iterator();
				while (iterator.hasNext()) {
					predictionString = iterator.next().toString();
				}

				// System.out.println(testSentence.getLabel() + " versus " +
				// predictionString);
				labelPredictionPairLit[rowIndex][0] = testSentence.getLabel()
						.toString();
				labelPredictionPairLit[rowIndex][1] = predictionString;

				rowIndex++;

			}

		}

		for (int i = 1; i <= 9; i++) {
			double truePositive = 0.0;
			double falsePositive = 0.0;
			double trueNegative = 0.0;
			double falseNegative = 0.0;
			String labelString = String.valueOf(i);

			int labelSize = 0;
			for (int j = 0; j < labelPredictionPairLit.length; j++) {
				String label = labelPredictionPairLit[j][0];
				String prediction = labelPredictionPairLit[j][1];
				// if (prediction.equals(labelString)) {
				// }
				if (prediction.equals(labelString) && label.equals(labelString))
					truePositive += 1;

				if (prediction.equals(labelString)
						&& !label.equals(labelString))
					falsePositive += 1;

				if (!prediction.equals(labelString)
						&& label.equals(labelString))
					falseNegative += 1;

				if (!prediction.equals(labelString)
						&& !label.equals(labelString)
						&& prediction.equals(label))
					trueNegative += 1;

				labelSize++;

				// Reference
				// http://dearalex0321.wordpress.com/2008/08/20/precision-recall/
				// v => Prediction
				// _prob.y[j] => actual label

				// if ( v == _prob.y[j] && v == labelInteger ) {
				// correct++;
				// }

				// if ( v == labelInteger && _prob.y[j] == labelInteger ) {
				// truePositive++;
				// }
				// if ( v != labelInteger && _prob.y[j] == labelInteger ) {
				// falseNegative++;
				// }
				// if ( v == labelInteger && _prob.y[j] != labelInteger ) {
				// falsePositive++;
				// }
				// if ( v != labelInteger && _prob.y[j] != labelInteger && v ==
				// _prob.y[j] ) {
				// //if ( v != labelInteger && _prob.y[j] != labelInteger ) {
				// trueNegative++;
				// }
			}
			double precision;
			if (truePositive == 0 && falsePositive == 0)
				precision = 0;
			else
				precision = truePositive / (truePositive + falsePositive);

			double recall;
			if (truePositive == 0 && falseNegative == 0)
				recall = 0;
			else
				recall = truePositive / (truePositive + falseNegative);

			double accuracy = (truePositive + trueNegative)
					/ labelPredictionPairLit.length;

			// System.out.println("precision:"+ precision);
			// System.out.println("recall:"+ recall);
			// System.out.println("accuracy:"+ accuracy);

			Date date = new Date();

			outputStringBuilder.append("Category " + labelString + ",");
			outputStringBuilder.append(labelSize + ",");
			outputStringBuilder.append(truePositive + ",");
			outputStringBuilder.append(falsePositive + ",");
			outputStringBuilder.append(trueNegative + ",");
			outputStringBuilder.append(falseNegative + ",");
			outputStringBuilder.append(precision + ",");
			outputStringBuilder.append(recall + ",");
			outputStringBuilder.append(accuracy + "\n");
			// outputStringBuilder.append(date.toString());

		}

		try (PrintWriter out = new PrintWriter(
				new BufferedWriter(new FileWriter(
						"WekaLibSVM-n-folder-cross-validation.csv", true)))) {
			out.println(outputStringBuilder);
		} catch (IOException e) {
			// exception handling left as an exercise for the reader
		}

		StringBuilder lpPairStringBuilder = new StringBuilder();

		for (int j = 0; j < labelPredictionPairLit.length; j++) {
			String label = labelPredictionPairLit[j][0];
			String prediction = labelPredictionPairLit[j][1];
			lpPairStringBuilder.append(label + "," + prediction + "\n");
		}
		try (PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(
						"WekaLibSVM-n-folder-cross-validation-lp-Pair.csv",
						true)))) {
			out.println(lpPairStringBuilder);
		} catch (IOException e) {
			// exception handling left as an exercise for the reader
		}
	}

	// createUSPInputsFromListSentence
	private void createUSPInputsFromListSentence(List<Sentence> listSentence)
			throws IOException, InterruptedException, ExecutionException {		

		// STEP 1: Read Abbreviation (Keyword) List First and But HashTable<String, String>
		
		// if the folder "usp" exists, delete it
		FileUtils.deleteDirectory(new File("usp"));
		
		// Construct abbreviation list
		Hashtable<String, String> kwdListByCategory = new Hashtable<String, String>();
		// Example {category1_character1,XXX|YYY|ZZZ}
		
		File inputDir = new File(characterValueExtractorsFolder);
		if(inputDir.exists() && !inputDir.isFile()) {
			for(File file : inputDir.listFiles()) {
				try {
					String name = file.getName();
					
					// System.out.println("file name is ::" + name);
					
					int firstDotIndex = name.indexOf(".");

					int lastDotIndex = name.lastIndexOf(".");
					
					String labelName = name.substring(0, firstDotIndex);
					
					String character = name.substring(firstDotIndex + 1, lastDotIndex);
					
					// character = character.replaceAll("\\s", "-");
					// character = character.replaceAll("\\(", "-");
					// character = character.replaceAll("\\)", "-");
					character = character.replaceAll("\\s", "");
					character = character.replaceAll("\\(", "");
					character = character.replaceAll("\\)", "");
					character = character.replaceAll("\\&", "");
					
					// character = character.substring(0,10);
					
					String type = name.substring(lastDotIndex + 1, name.length());
					
					// System.out.println("type is ::" + type);
					
					ExtractorType extractorType = ExtractorType.valueOf(type);
					
					String keywords = "";
					switch(extractorType) {
					case key:
						BufferedReader br = new BufferedReader(new InputStreamReader(
								new FileInputStream(file), "UTF8"));
						
						String strLine;
						while ((strLine = br.readLine()) != null) {
							
							// System.out.println("strLine is ::" + strLine);
							// if (name.contains("c7.")) {
							//	System.out.println("c7 exists!");
							//	System.out.println("strLine is ::" + strLine);
							// }
							if (strLine.length() > 1) {
								
								// remove space
								// 
								String keyword = strLine.trim().toLowerCase();
								
								keywords += keyword + "|";
							}
						}
						br.close();
						
						if( keywords.length() > 1) {
							if( keywords.substring(keywords.length()-1, keywords.length()).equals("|")) {
								keywords = keywords.substring(0, keywords.length()-1);
							}
							kwdListByCategory.put(labelName+"-"+character , keywords);

						}
						

					case usp:
						// do nothing
					default:
						// throw new Exception("Could not identify extractor type from file");
					}
					
				} catch(Exception e) {
					log(LogLevel.ERROR, "Could not load extractor in file: " + file.getAbsolutePath() + "\nIt will be skipped", e);
				}
			}
		}
		
		

		
		
		int counter = 1;
		
		for (Sentence sentence : listSentence) {			
			
			StringBuilder depStringBuilder = new StringBuilder(); // Stanford Dependency
			StringBuilder inputStringBuilder = new StringBuilder();
			StringBuilder morphStringBuilder = new StringBuilder();
			StringBuilder parseStringBuilder = new StringBuilder(); // Parse Tree
			StringBuilder textStringBuilder = new StringBuilder();

			String sentText0 = sentence.getText();
			
			
			String sentText = sentence.getText();; // it is sentence based not text
			ILabel sentLabel = sentence.getLabel(); // labels
			// based anymore ??
			
			
			log(LogLevel.INFO,
					"build pos tagger and dependency as USP inputs using stanford corenlp pipeline...");

			
			
			String depStringPlain = ""; // Dependency String
			
			StringTokenizer textToken = new StringTokenizer(sentText, " ");
			if (textToken.countTokens() < 40) {
				
				// STEP 2: Build Original Sentence USP inputs
				/*
				Annotation annotation = new Annotation(sentText);
				this.tokenizeSSplitPosParse.annotate(annotation);
				List<CoreMap> sentenceAnnotations = annotation
						.get(SentencesAnnotation.class);
				for (CoreMap sentenceAnnotation : sentenceAnnotations) {
					// result.add(sentenceAnnotation.toString());
					for (CoreLabel token : sentenceAnnotation
							.get(TokensAnnotation.class)) {

						String pos = token.get(PartOfSpeechAnnotation.class);
						// System.out.println(token + "_" + pos);

						if (token.toString().equals("_")) {
							inputStringBuilder.append("dash_" + pos + "\n");
							morphStringBuilder.append("dash\n");
						} else {
							inputStringBuilder.append(token + "_" + pos + "\n");
							morphStringBuilder.append(token.toString()
									.toLowerCase() + "\n");
						}

					}
					
					Tree tree = sentenceAnnotation.get(TreeAnnotation.class);

					//System.out.println("The first sentence parsed is:");
					// tree.pennPrint(outputStream);
					String treeString = tree.pennString();
					//System.out.println(treeString);
					parseStringBuilder.append(treeString);
					
					SemanticGraph dependencies = sentenceAnnotation
							.get(CollapsedCCProcessedDependenciesAnnotation.class);
					// the same as above
					// SemanticGraph dependencies =
					// sentenceAnnotation.get(CollapsedDependenciesAnnotation.class);

					// Deprecated
					// String depString = dependencies.toString("plain");
					// depString = depString.replaceAll(",", ", ");
					// System.out.println("Dependency String::" + depString);
					// depStringBuilder.append(dependencies.toString("plain"));
					// Deprecated

					// System.out.println("xml format::" + dependencies.toString("xml"));
					String depStringXml = dependencies.toString("xml");

					
					SAXBuilder saxBuilder = new SAXBuilder();
					try {
						Document xmlDocument = saxBuilder
								.build(new StringReader(depStringXml));
						// String message =
						// xmlDocument.getRootElement().getText();
						// System.out.println(message);
						Element rootNode = xmlDocument.getRootElement();
						// System.out.println(rootNode.getName());
						// //<dependencies
						// style="typed"> => dependencies
						// System.out.println(rootNode.getAttributeValue("style"));
						// // style="typed" => typed

						List depList = rootNode.getChildren("dep");
						for (int i = 0; i <= depList.size() - 1; i++) {
							Element element = (Element) depList.get(i);
							// System.out.println("dep type : "+
							// element.getAttributeValue("type"));
							depStringPlain += element.getAttributeValue("type")
									+ "(";

							// System.out.println("governor : "+
							// element.getChildText("governor"));
							depStringPlain += element.getChildText("governor")
									+ "-";
							List<Element> childrenList = element
									.getChildren("governor");
							for (int j = 0; j <= childrenList.size() - 1; j++) {
								Element element2 = childrenList.get(j);
								// System.out.println("idx : "+
								// element2.getAttributeValue("idx"));
								depStringPlain += element2
										.getAttributeValue("idx");
							}
							depStringPlain += ", ";

							// System.out.println("dependent : "+
							// element.getChildText("dependent"));
							depStringPlain += element.getChildText("dependent")
									+ "-";
							List<Element> childrenList2 = element
									.getChildren("dependent");
							for (int j = 0; j <= childrenList2.size() - 1; j++) {
								Element element2 = childrenList2.get(j);
								// System.out.println("idx : "+
								// element2.getAttributeValue("idx"));
								depStringPlain += element2
										.getAttributeValue("idx");
							}
							depStringPlain += ")\n";

						}
						depStringBuilder.append(depStringPlain);
						// System.out.println("dependencies::" + dependencies);
						// System.out.println("depStringPlain:: " + depStringPlain);

					} catch (JDOMException e) {
						// handle JDOMException
					} catch (IOException e) {
						// handle IOException
					}

					// textStringBuilder.append(sentence.getText());

				}

				textStringBuilder.append(sentText);

				
				new File("usp").mkdirs();
				new File("usp/dep_o").mkdirs();
				new File("usp/dep_o/0").mkdirs();
				new File("usp/morph_o").mkdirs();
				new File("usp/morph_o/0").mkdirs();
				new File("usp/text_o").mkdirs();
				new File("usp/text_o/0").mkdirs();
				new File("usp/parse_o").mkdirs();
				new File("usp/parse_o/0").mkdirs();

				try (PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("usp/dep_o/0/" + counter + ".dep", false)))) {
					out.println(depStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("usp/morph_o/0/" + counter + ".input",
								false)))) {
					out.println(inputStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("usp/morph_o/0/" + counter + ".morph",
								false)))) {
					out.println(morphStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(
						new BufferedWriter(new FileWriter("usp/text_o/0/"
								+ counter + ".txt", false)))) {
					out.println(textStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(
						new BufferedWriter(new FileWriter("usp/parse_o/0/"
								+ counter + ".parse", false)))) {
					out.println(parseStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				*/
				
				
				

				
				// STEP 3: Build sentence list with abbreviation terms
				
				// and build the collapseUSPSent Index
				// build collase USP sentence
				
				
				
				
				// System.out.println("ORI_SENT::"+ sentText);

				
				CollapseUSPSentByCategoryChar collapseUSPSentByCateogryChar = new CollapseUSPSentByCategoryChar();
				
				// System.out.println("ORI_SENT::"+ sentText);
				
				sentText = stanfordTokenizerTransformation(sentText);
				
				// System.out.println("sentText::2::" + sentText);
				
				String tagSentText = collapseUSPSentByCateogryChar.tagWithCategoryList(sentText, kwdListByCategory);
				
				// System.out.println("tagTestSent::" + tagSentText);
				
				
				List<CollapseUSPSentIndexMapping> collapseUSPSentIndexMappingList = new ArrayList<CollapseUSPSentIndexMapping>();
				
				
				
				CollapseUSPSentByCategoryCharTokenizer collapseUSPSentByCategoryCharTokenizer = new CollapseUSPSentByCategoryCharTokenizer();
				
				String[] tokens = collapseUSPSentByCategoryCharTokenizer.tokenize(sentText);
				
				for (int i = 0; i < tokens.length; i++) {
					morphStringBuilder.append(tokens[i].toString()
						.toLowerCase() + "\n");
				}
				
				textStringBuilder.append(sentText0);
				
				

				
				
				
				String[] tagTokens = collapseUSPSentByCategoryCharTokenizer.tokenize(tagSentText);
				// System.out.println(Arrays.toString(tokens));
				
				
				int oriIndex = 0;
				for (int index  = 0; index < tagTokens.length;) {
					
					// System.out.println("\n::oriIndex::" + oriIndex);

					String tagToken = tagTokens[index];
					
					// System.out.println("index::" + index);
					// System.out.println("tagToken::" + tagToken);
					
					List<String> words = new ArrayList<String>();
					words.add(tagToken);
					String category = collapseUSPSentByCateogryChar.getCategoryName(tagToken, kwdListByCategory);
					
					if ( !category.equals("") ) {
						// System.out.println("category::" + category + "::");
						words = collapseUSPSentByCateogryChar.fetchSubseqTokensOfCat(index, category, tagTokens);
						// System.out.println("fetchSubseqTokensOfCat::" + words.toString());
						
						int count = 0;
						ArrayList<String> indices = new ArrayList<String>();
						
						for (String word : words) {
							
							// System.out.println("word::" + word);
							if (word.matches("\\W+")) {
								count++;
								
								// indices.add(String.valueOf(index+count));
							} else {
								word = collapseUSPSentByCateogryChar.removeNumAndCategory(word); //turn ‘DDD#EEE[Cat]’ to ‘DDD EEE’
								String[] parts = word.split(" ");
								
								String multiIndex = ""; 
								for ( String part: parts ){
									multiIndex += String.valueOf(oriIndex+count) + "-";
									// indices.add(String.valueOf(oriIndex+count));
									count++;
								}
								
								if (multiIndex.substring(multiIndex.length()-1, multiIndex.length()).equals("-")) {
								 	multiIndex = multiIndex.substring(0, multiIndex.length()-1);
								}
								indices.add(multiIndex);
							}
						}
						

						// System.out.println("indices::" + indices.toString());
						// System.out.println("oriIndex::" + oriIndex);
						// System.out.println("index::" + index);
						
						
						// transfer it into indiciesString
						String indicesString = "";
						int itemInIndicesCounter = 0;
						for (String itemInIndices : indices) {
							if (itemInIndicesCounter == (indices.size()-1)) {
								indicesString += itemInIndices;
							} else {
								indicesString += itemInIndices + ",";
							}
							itemInIndicesCounter++;
						}
						
						collapseUSPSentIndexMappingList.add(collapseUSPSentByCateogryChar.addMapping(category, indicesString)); //add a list of index	
						
						// System.out.println("tokenNumber(words)::" + tokenNumber(words) + "::words.size()::" + words.size());
						
						// System.out.println("old index::" + index);
						index = index + words.size();
						// System.out.println("new index::" + index);
						oriIndex = oriIndex + collapseUSPSentByCateogryChar.tokenNumber(words);
						// System.out.println("new oriIndex::" + oriIndex);
						
					} else {
						
						// System.out.println("tagToken::" + tagToken);
						// System.out.println("oriIndex::" + oriIndex);
						// System.out.println("index::" + index);
						
						collapseUSPSentIndexMappingList.add(collapseUSPSentByCateogryChar.addMapping(tagToken, String.valueOf(oriIndex))); //add single index
						
						index +=1;
						// System.out.println("Update the index to " + index);
						
						oriIndex +=1;
						// System.out.println("Update the oriIndex to " + oriIndex);
						
					}
					// System.out.println("\n");
				}
				
				
				
				
				// STEP 4: Collapse the sentence and build index txt file
				
			
				String collapsedSentString = "";
				String collapsedSentIndexString = "";
				
				
				int lastIndexInPreviousItem = -1;
				int firstIndexInCurrentItem = -1;
				int itemInCollapseUSPSentIndexMappingListCounter = 0;
				for (CollapseUSPSentIndexMapping itemInCollapseUSPSentIndexMappingList : collapseUSPSentIndexMappingList) {
					// System.out.println("itemInCollapseUSPSentIndexMappingList.getToken()::" + itemInCollapseUSPSentIndexMappingList.getToken());
					// System.out.println("itemInCollapseUSPSentIndexMappingList.getIndices()::" + itemInCollapseUSPSentIndexMappingList.getIndices());
					String curToken = itemInCollapseUSPSentIndexMappingList.getToken();
					String curIndices = itemInCollapseUSPSentIndexMappingList.getIndices();
					
					String[] curIndicesArray = curIndices.split(",");
					String[] subFirstIndexArray = curIndicesArray[0].split("-");
					String[] subLastIndexArray = curIndicesArray[curIndicesArray.length-1].split("-");
					
					if (itemInCollapseUSPSentIndexMappingListCounter == 0) {
						firstIndexInCurrentItem = Integer.parseInt(subFirstIndexArray[0]);
					}
					
					if (itemInCollapseUSPSentIndexMappingListCounter > 0) {
						firstIndexInCurrentItem = Integer.parseInt(subFirstIndexArray[0]);
 					}
					
					
					// System.out.println("lastIndexInPreviousItem::" + lastIndexInPreviousItem);					
					// System.out.println("firstIndexInCurrentItem::" + firstIndexInCurrentItem);
					
					
					
					if ((lastIndexInPreviousItem+1) < firstIndexInCurrentItem) {
						collapsedSentString += " , " + itemInCollapseUSPSentIndexMappingList.getToken();
						collapsedSentIndexString +=  ",\t\n";
						collapsedSentIndexString += itemInCollapseUSPSentIndexMappingList.getToken() + "\t" + itemInCollapseUSPSentIndexMappingList.getIndices() + "\n";
						
					} else {
						collapsedSentString += " " + itemInCollapseUSPSentIndexMappingList.getToken();
						collapsedSentIndexString += itemInCollapseUSPSentIndexMappingList.getToken() + "\t" + itemInCollapseUSPSentIndexMappingList.getIndices() + "\n";
					}
					
					lastIndexInPreviousItem = Integer.parseInt(subLastIndexArray[subLastIndexArray.length-1]);
 					
					
					itemInCollapseUSPSentIndexMappingListCounter++;
					
				}
				
				// System.out.println("collapsedSent::" + collapsedSentString);
				// System.out.println("collapsedSentIndexString::" + collapsedSentIndexString);

				
				
				/*
				if ( isCollapsedSentence(sentStanfordTokenizedStringTokens) == true) {
					CollapsedSentenceAndIndex generatedCollapsedSentence = generateCollapsedSentenceAndIndex(sentStanfordTokenizedStringTokens);
					// System.out.println("generatedCollapsedSentence::" + generatedCollapsedSentence.getCollapsedSentence() + "\n");
					
					collapsedSentString = generatedCollapsedSentence.getCollapsedSentence();
					collapsedSentIndexString = generatedCollapsedSentence.getCollapsedSentenceIndex();	

					
					
				} else {
					// System.out.println("sentReplacedByCategoryKwd::" + sentReplacedByCategoryKwd + "\n");
					
					collapsedSentString = sentReplacedByCategoryKwd; // non-collapsed sentence

					
					// reduce this
					
					// CollapsedSentenceAndIndex generatedCollapsedSentence = generateCollapsedSentenceAndIndex(sentStanfordTokenizedStringTokens);
					// collapsedSentIndexString = generatedCollapsedSentence.getCollapsedSentenceIndex();	

					String generatedNonCollapsedSentenceIndexString = generateNonCollapsedSentenceIndexString(sentStanfordTokenizedStringTokens);
					
					collapsedSentIndexString = generatedNonCollapsedSentenceIndexString;
					
					
					// System.out.println("non-collapsed::collapsedSentIndexString::\n" + collapsedSentIndexString + "\n");
					

					
					
				}
				*/
				
				
				

				
				// STEP 5: Build USP_COLLAPSED inputs
				// build collapsed USP inputs

				
				StringBuilder collapsedDepStringBuilder = new StringBuilder(); // Stanford Dependency
				StringBuilder collapsedInputStringBuilder = new StringBuilder();
				StringBuilder collapsedMorphStringBuilder = new StringBuilder();
				StringBuilder collapsedParseStringBuilder = new StringBuilder(); // Parse Tree
				StringBuilder collapsedTextStringBuilder = new StringBuilder();
				StringBuilder collapsedSentIndexStringBuilder = new StringBuilder();
				
				String collapsedDepStringPlain = "";
				
				// StringTokenizer collapsedSentStringToken = new StringTokenizer(collapsedSentString, " ");
				// if (collapsedSentStringToken.countTokens() < 100) {
				// }
				
				
				Annotation annotationCollapsedSent = new Annotation(collapsedSentString);
				this.tokenizeSSplitPosParse.annotate(annotationCollapsedSent);
				List<CoreMap> sentenceAnnotationsCollapsedSent = annotationCollapsedSent
						.get(SentencesAnnotation.class);
				for (CoreMap sentenceAnnotationCollapsedSent : sentenceAnnotationsCollapsedSent) {
					// result.add(sentenceAnnotationCollapsedSent.toString());
					for (CoreLabel token : sentenceAnnotationCollapsedSent
							.get(TokensAnnotation.class)) {

						String pos = token.get(PartOfSpeechAnnotation.class);
						// System.out.println(token + "_" + pos);

						if (token.toString().equals("_")) {
							collapsedInputStringBuilder.append("dash_" + pos + "\n");
							collapsedMorphStringBuilder.append("dash\n");
						} else {
							collapsedInputStringBuilder.append(token + "_" + pos + "\n");
							collapsedMorphStringBuilder.append(token.toString()
									.toLowerCase() + "\n");
						}

					}
					
					Tree tree = sentenceAnnotationCollapsedSent.get(TreeAnnotation.class);

					//System.out.println("The first sentence parsed is:");
					// tree.pennPrint(outputStream);
					String treeString = tree.pennString();
					//System.out.println(treeString);
					collapsedParseStringBuilder.append(treeString);
					
					SemanticGraph dependencies = sentenceAnnotationCollapsedSent
							.get(CollapsedCCProcessedDependenciesAnnotation.class);

					String depStringXml = dependencies.toString("xml");
					
					if (depStringXml.length() == 0) {
						System.out.println("No Dependency!");	
					}
					
					
					SAXBuilder saxBuilder = new SAXBuilder();
					try {
						Document xmlDocument = saxBuilder
								.build(new StringReader(depStringXml));
						// String message =
						// xmlDocument.getRootElement().getText();
						// System.out.println(message);
						Element rootNode = xmlDocument.getRootElement();
						// System.out.println(rootNode.getName());
						// //<dependencies
						// style="typed"> => dependencies
						// System.out.println(rootNode.getAttributeValue("style"));
						// // style="typed" => typed

						List depList = rootNode.getChildren("dep");
						for (int i = 0; i <= depList.size() - 1; i++) {
							Element element = (Element) depList.get(i);
							// System.out.println("dep type : "+
							// element.getAttributeValue("type"));
							collapsedDepStringPlain += element.getAttributeValue("type")
									+ "(";

							// System.out.println("governor : "+
							// element.getChildText("governor"));
							collapsedDepStringPlain += element.getChildText("governor")
									+ "-";
							List<Element> childrenList = element
									.getChildren("governor");
							for (int j = 0; j <= childrenList.size() - 1; j++) {
								Element element2 = childrenList.get(j);
								// System.out.println("idx : "+
								// element2.getAttributeValue("idx"));
								collapsedDepStringPlain += element2
										.getAttributeValue("idx");
							}
							collapsedDepStringPlain += ", ";

							// System.out.println("dependent : "+
							// element.getChildText("dependent"));
							collapsedDepStringPlain += element.getChildText("dependent")
									+ "-";
							List<Element> childrenList2 = element
									.getChildren("dependent");
							for (int j = 0; j <= childrenList2.size() - 1; j++) {
								Element element2 = childrenList2.get(j);
								// System.out.println("idx : "+
								// element2.getAttributeValue("idx"));
								collapsedDepStringPlain += element2
										.getAttributeValue("idx");
							}
							collapsedDepStringPlain += ")\n";

						}
						collapsedDepStringBuilder.append(collapsedDepStringPlain);
						// System.out.println("dependencies::" + dependencies);
						// System.out.println("depStringPlain:: " + depStringPlain);

					} catch (JDOMException e) {
						// handle JDOMException
					} catch (IOException e) {
						// handle IOException
					}

					// collapsedTextStringBuilder.append(sentence.getText());

				}

				// System.out.println("collapsedDepStringPlain ::\n" + collapsedDepStringPlain);					
				
				collapsedTextStringBuilder.append(collapsedSentString);
				collapsedSentIndexStringBuilder.append(collapsedSentIndexString);				
				
				
				
				if (collapsedDepStringBuilder.toString().length() == 0) continue;
					// System.out.println("Elvis::No Dependency!");	
				
				
				
				// write string into txt files

				
				
				new File("usp").mkdirs();
				new File("usp/morph_o").mkdirs();
				new File("usp/morph_o/0").mkdirs();
				new File("usp/text_o").mkdirs();
				new File("usp/text_o/0").mkdirs();
				
				try (PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("usp/morph_o/0/" + counter + ".morph",
								false)))) {
					out.println(morphStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(
						new BufferedWriter(new FileWriter("usp/text_o/0/"
								+ counter + ".txt", false)))) {
					out.println(textStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				
				
				
				new File("usp").mkdirs();
				new File("usp/dep").mkdirs();
				new File("usp/dep/0").mkdirs();
				new File("usp/morph").mkdirs();
				new File("usp/morph/0").mkdirs();
				new File("usp/text").mkdirs();
				new File("usp/text/0").mkdirs();
				new File("usp/parse").mkdirs();
				new File("usp/parse/0").mkdirs();
				new File("usp/index").mkdirs();
				new File("usp/index/0").mkdirs();

				
				
				
				
				
				try (PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("usp/index/0/" + counter + ".index", false)))) {
					out.println(collapsedSentIndexStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				
				try (PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("usp/dep/0/" + counter + ".dep", false)))) {
					out.println(collapsedDepStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("usp/morph/0/" + counter + ".input",
								false)))) {
					out.println(collapsedInputStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("usp/morph/0/" + counter + ".morph",
								false)))) {
					out.println(collapsedMorphStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(
						new BufferedWriter(new FileWriter("usp/text/0/"
								+ counter + ".txt", false)))) {
					out.println(collapsedTextStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(
						new BufferedWriter(new FileWriter("usp/parse/0/"
								+ counter + ".parse", false)))) {
					out.println(collapsedParseStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				
				
				
				
				
				counter++;
				
				
			}
			
			// counter++;
						
			log(LogLevel.INFO,
					"done building pos tagger and dependency as USP inputs using stanford corenlp pipeline...");
		}
	}
	
	private void createUSPInputs(List<MultiClassifiedSentence> predictions)
			throws IOException, InterruptedException, ExecutionException {		

		// STEP 1: Read Abbreviation (Keyword) List First and But HashTable<String, String>
		
		// if the folder "usp" exists, delete it
		// FileUtils.deleteDirectory(new File("usp"));
		
		// Construct abbreviation list
		Hashtable<String, String> kwdListByCategory = new Hashtable<String, String>();
		// Example {category1_character1,XXX|YYY|ZZZ}
		
		File inputDir = new File(characterValueExtractorsFolder);
		if(inputDir.exists() && !inputDir.isFile()) {
			for(File file : inputDir.listFiles()) {
				try {
					String name = file.getName();
					
					// System.out.println("file name is ::" + name);
					
					int firstDotIndex = name.indexOf(".");

					int lastDotIndex = name.lastIndexOf(".");
					
					String labelName = name.substring(0, firstDotIndex);
					
					String character = name.substring(firstDotIndex + 1, lastDotIndex);
					
					// character = character.replaceAll("\\s", "-");
					// character = character.replaceAll("\\(", "-");
					// character = character.replaceAll("\\)", "-");
					character = character.replaceAll("\\s", "");
					character = character.replaceAll("\\(", "");
					character = character.replaceAll("\\)", "");
					character = character.replaceAll("\\&", "");
					
					// character = character.substring(0,10);
					
					String type = name.substring(lastDotIndex + 1, name.length());
					
					// System.out.println("type is ::" + type);
					
					ExtractorType extractorType = ExtractorType.valueOf(type);
					
					String keywords = "";
					switch(extractorType) {
					case key:
						BufferedReader br = new BufferedReader(new InputStreamReader(
								new FileInputStream(file), "UTF8"));
						
						String strLine;
						while ((strLine = br.readLine()) != null) {
							
							// System.out.println("strLine is ::" + strLine);
							// if (name.contains("c7.")) {
							//	System.out.println("c7 exists!");
							//	System.out.println("strLine is ::" + strLine);
							// }
							if (strLine.length() > 1) {
								
								// remove space
								// 
								String keyword = strLine.trim().toLowerCase();
								
								keywords += keyword + "|";
							}
						}
						br.close();
						
						if( keywords.length() > 1) {
							if( keywords.substring(keywords.length()-1, keywords.length()).equals("|")) {
								keywords = keywords.substring(0, keywords.length()-1);
							}
							kwdListByCategory.put(labelName+"-"+character , keywords);

						}
						

					case usp:
						// do nothing
					default:
						// throw new Exception("Could not identify extractor type from file");
					}
					
				} catch(Exception e) {
					log(LogLevel.ERROR, "Could not load extractor in file: " + file.getAbsolutePath() + "\nIt will be skipped", e);
				}
			}
		}
		
		

		
		
		// TODO:: Change counter number from existing USP input folder
		// DONE
		
		int maxUspInputFileId = 0;
		File uspInputsFolder = new File(uspFolder);
		if(uspInputsFolder.exists() && !uspInputsFolder.isFile()) {
			File[] uspInputsFiles = uspInputsFolder.listFiles();
			// System.out.println("uspInputsFiles.length is ::" + uspInputsFiles.length);
			for (File uspInputsFile : uspInputsFiles) {
				log(LogLevel.INFO, "Reading from " + uspInputsFile.getName() + "...");
				try {
					String uspInputsFileName = uspInputsFile.getName();
					int firstDotIndex = uspInputsFileName.indexOf(".");
					String uspInputFileId = uspInputsFileName.substring(0, firstDotIndex);
					log(LogLevel.INFO, "Usp input file id: " + uspInputFileId);
					
					int curentUspInputFileId = Integer.parseInt(uspInputFileId);
					if ( curentUspInputFileId > maxUspInputFileId ) {
						maxUspInputFileId = curentUspInputFileId;
					}
					
					
				} catch (Exception e) {
					log(LogLevel.ERROR, "Could not read usp input from "
							+ uspInputsFile.getName(), e);
					log(LogLevel.ERROR, "Could not read usp input in file: " + uspInputsFile.getAbsolutePath() + "\nIt will be skipped", e);

				}
			}
		}
		
		
		// System.out.println("maxUspInputFileId is ::" + maxUspInputFileId);
		
		
		int counter = maxUspInputFileId + 1;
		
		for (MultiClassifiedSentence multiClassifiedSentence : predictions) {			
			
			
			
			StringBuilder depStringBuilder = new StringBuilder(); // Stanford Dependency
			StringBuilder inputStringBuilder = new StringBuilder();
			StringBuilder morphStringBuilder = new StringBuilder();
			StringBuilder parseStringBuilder = new StringBuilder(); // Parse Tree
			StringBuilder textStringBuilder = new StringBuilder();

			String sentText0 = multiClassifiedSentence.getSentence().getText();
			
			
			String sentText = multiClassifiedSentence.getSentence().getText(); // it is sentence based not text
			Set<ILabel> sentLabels = multiClassifiedSentence.getPredictions(); // labels
			// based anymore ??
			
			
			log(LogLevel.INFO,
					"build pos tagger and dependency as USP inputs using stanford corenlp pipeline...");

			
			
			String depStringPlain = ""; // Dependency String
			
			StringTokenizer textToken = new StringTokenizer(sentText, " ");
			if (textToken.countTokens() < 40) {
				
				// STEP 2: Build Original Sentence USP inputs
				/*
				Annotation annotation = new Annotation(sentText);
				this.tokenizeSSplitPosParse.annotate(annotation);
				List<CoreMap> sentenceAnnotations = annotation
						.get(SentencesAnnotation.class);
				for (CoreMap sentenceAnnotation : sentenceAnnotations) {
					// result.add(sentenceAnnotation.toString());
					for (CoreLabel token : sentenceAnnotation
							.get(TokensAnnotation.class)) {

						String pos = token.get(PartOfSpeechAnnotation.class);
						// System.out.println(token + "_" + pos);

						if (token.toString().equals("_")) {
							inputStringBuilder.append("dash_" + pos + "\n");
							morphStringBuilder.append("dash\n");
						} else {
							inputStringBuilder.append(token + "_" + pos + "\n");
							morphStringBuilder.append(token.toString()
									.toLowerCase() + "\n");
						}

					}
					
					Tree tree = sentenceAnnotation.get(TreeAnnotation.class);

					//System.out.println("The first sentence parsed is:");
					// tree.pennPrint(outputStream);
					String treeString = tree.pennString();
					//System.out.println(treeString);
					parseStringBuilder.append(treeString);
					
					SemanticGraph dependencies = sentenceAnnotation
							.get(CollapsedCCProcessedDependenciesAnnotation.class);
					// the same as above
					// SemanticGraph dependencies =
					// sentenceAnnotation.get(CollapsedDependenciesAnnotation.class);

					// Deprecated
					// String depString = dependencies.toString("plain");
					// depString = depString.replaceAll(",", ", ");
					// System.out.println("Dependency String::" + depString);
					// depStringBuilder.append(dependencies.toString("plain"));
					// Deprecated

					// System.out.println("xml format::" + dependencies.toString("xml"));
					String depStringXml = dependencies.toString("xml");

					
					SAXBuilder saxBuilder = new SAXBuilder();
					try {
						Document xmlDocument = saxBuilder
								.build(new StringReader(depStringXml));
						// String message =
						// xmlDocument.getRootElement().getText();
						// System.out.println(message);
						Element rootNode = xmlDocument.getRootElement();
						// System.out.println(rootNode.getName());
						// //<dependencies
						// style="typed"> => dependencies
						// System.out.println(rootNode.getAttributeValue("style"));
						// // style="typed" => typed

						List depList = rootNode.getChildren("dep");
						for (int i = 0; i <= depList.size() - 1; i++) {
							Element element = (Element) depList.get(i);
							// System.out.println("dep type : "+
							// element.getAttributeValue("type"));
							depStringPlain += element.getAttributeValue("type")
									+ "(";

							// System.out.println("governor : "+
							// element.getChildText("governor"));
							depStringPlain += element.getChildText("governor")
									+ "-";
							List<Element> childrenList = element
									.getChildren("governor");
							for (int j = 0; j <= childrenList.size() - 1; j++) {
								Element element2 = childrenList.get(j);
								// System.out.println("idx : "+
								// element2.getAttributeValue("idx"));
								depStringPlain += element2
										.getAttributeValue("idx");
							}
							depStringPlain += ", ";

							// System.out.println("dependent : "+
							// element.getChildText("dependent"));
							depStringPlain += element.getChildText("dependent")
									+ "-";
							List<Element> childrenList2 = element
									.getChildren("dependent");
							for (int j = 0; j <= childrenList2.size() - 1; j++) {
								Element element2 = childrenList2.get(j);
								// System.out.println("idx : "+
								// element2.getAttributeValue("idx"));
								depStringPlain += element2
										.getAttributeValue("idx");
							}
							depStringPlain += ")\n";

						}
						depStringBuilder.append(depStringPlain);
						// System.out.println("dependencies::" + dependencies);
						// System.out.println("depStringPlain:: " + depStringPlain);

					} catch (JDOMException e) {
						// handle JDOMException
					} catch (IOException e) {
						// handle IOException
					}

					// textStringBuilder.append(sentence.getText());

				}

				textStringBuilder.append(sentText);

				
				new File("usp").mkdirs();
				new File("usp/dep_o").mkdirs();
				new File("usp/dep_o/0").mkdirs();
				new File("usp/morph_o").mkdirs();
				new File("usp/morph_o/0").mkdirs();
				new File("usp/text_o").mkdirs();
				new File("usp/text_o/0").mkdirs();
				new File("usp/parse_o").mkdirs();
				new File("usp/parse_o/0").mkdirs();

				try (PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("usp/dep_o/0/" + counter + ".dep", false)))) {
					out.println(depStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("usp/morph_o/0/" + counter + ".input",
								false)))) {
					out.println(inputStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("usp/morph_o/0/" + counter + ".morph",
								false)))) {
					out.println(morphStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(
						new BufferedWriter(new FileWriter("usp/text_o/0/"
								+ counter + ".txt", false)))) {
					out.println(textStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(
						new BufferedWriter(new FileWriter("usp/parse_o/0/"
								+ counter + ".parse", false)))) {
					out.println(parseStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				*/
				
				
				

				
				// STEP 3: Build sentence list with abbreviation terms
				
				// and build the collapseUSPSent Index
				// build collase USP sentence
				
				
				
				
				// System.out.println("ORI_SENT::"+ sentText);

				
				CollapseUSPSentByCategoryChar collapseUSPSentByCateogryChar = new CollapseUSPSentByCategoryChar();
				
				// System.out.println("ORI_SENT::"+ sentText);
				
				sentText = stanfordTokenizerTransformation(sentText);
				
				// System.out.println("sentText::2::" + sentText);
				
				String tagSentText = collapseUSPSentByCateogryChar.tagWithCategoryList(sentText, kwdListByCategory);
				
				// System.out.println("tagTestSent::" + tagSentText);
				
				
				List<CollapseUSPSentIndexMapping> collapseUSPSentIndexMappingList = new ArrayList<CollapseUSPSentIndexMapping>();
				
				
				
				CollapseUSPSentByCategoryCharTokenizer collapseUSPSentByCategoryCharTokenizer = new CollapseUSPSentByCategoryCharTokenizer();
				
				String[] tokens = collapseUSPSentByCategoryCharTokenizer.tokenize(sentText);
				
				for (int i = 0; i < tokens.length; i++) {
					morphStringBuilder.append(tokens[i].toString()
						.toLowerCase() + "\n");
				}
				
				textStringBuilder.append(sentText0);
				
				

				
				
				
				String[] tagTokens = collapseUSPSentByCategoryCharTokenizer.tokenize(tagSentText);
				// System.out.println(Arrays.toString(tokens));
				
				
				int oriIndex = 0;
				for (int index  = 0; index < tagTokens.length;) {
					
					// System.out.println("\n::oriIndex::" + oriIndex);

					String tagToken = tagTokens[index];
					
					// System.out.println("index::" + index);
					// System.out.println("tagToken::" + tagToken);
					
					List<String> words = new ArrayList<String>();
					words.add(tagToken);
					String category = collapseUSPSentByCateogryChar.getCategoryName(tagToken, kwdListByCategory);
					
					if ( !category.equals("") ) {
						// System.out.println("category::" + category + "::");
						words = collapseUSPSentByCateogryChar.fetchSubseqTokensOfCat(index, category, tagTokens);
						// System.out.println("fetchSubseqTokensOfCat::" + words.toString());
						
						int count = 0;
						ArrayList<String> indices = new ArrayList<String>();
						
						for (String word : words) {
							
							// System.out.println("word::" + word);
							if (word.matches("\\W+")) {
								count++;
								
								// indices.add(String.valueOf(index+count));
							} else {
								word = collapseUSPSentByCateogryChar.removeNumAndCategory(word); //turn ‘DDD#EEE[Cat]’ to ‘DDD EEE’
								String[] parts = word.split(" ");
								
								String multiIndex = ""; 
								for ( String part: parts ){
									multiIndex += String.valueOf(oriIndex+count) + "-";
									// indices.add(String.valueOf(oriIndex+count));
									count++;
								}
								
								if (multiIndex.substring(multiIndex.length()-1, multiIndex.length()).equals("-")) {
								 	multiIndex = multiIndex.substring(0, multiIndex.length()-1);
								}
								indices.add(multiIndex);
							}
						}
						

						// System.out.println("indices::" + indices.toString());
						// System.out.println("oriIndex::" + oriIndex);
						// System.out.println("index::" + index);
						
						
						// transfer it into indiciesString
						String indicesString = "";
						int itemInIndicesCounter = 0;
						for (String itemInIndices : indices) {
							if (itemInIndicesCounter == (indices.size()-1)) {
								indicesString += itemInIndices;
							} else {
								indicesString += itemInIndices + ",";
							}
							itemInIndicesCounter++;
						}
						
						collapseUSPSentIndexMappingList.add(collapseUSPSentByCateogryChar.addMapping(category, indicesString)); //add a list of index	
						
						// System.out.println("tokenNumber(words)::" + tokenNumber(words) + "::words.size()::" + words.size());
						
						// System.out.println("old index::" + index);
						index = index + words.size();
						// System.out.println("new index::" + index);
						oriIndex = oriIndex + collapseUSPSentByCateogryChar.tokenNumber(words);
						// System.out.println("new oriIndex::" + oriIndex);
						
					} else {
						
						// System.out.println("tagToken::" + tagToken);
						// System.out.println("oriIndex::" + oriIndex);
						// System.out.println("index::" + index);
						
						collapseUSPSentIndexMappingList.add(collapseUSPSentByCateogryChar.addMapping(tagToken, String.valueOf(oriIndex))); //add single index
						
						index +=1;
						// System.out.println("Update the index to " + index);
						
						oriIndex +=1;
						// System.out.println("Update the oriIndex to " + oriIndex);
						
					}
					// System.out.println("\n");
				}
				
				
				
				
				// STEP 4: Collapse the sentence and build index txt file
				
			
				String collapsedSentString = "";
				String collapsedSentIndexString = "";
				
				
				int lastIndexInPreviousItem = -1;
				int firstIndexInCurrentItem = -1;
				int itemInCollapseUSPSentIndexMappingListCounter = 0;
				for (CollapseUSPSentIndexMapping itemInCollapseUSPSentIndexMappingList : collapseUSPSentIndexMappingList) {
					// System.out.println("itemInCollapseUSPSentIndexMappingList.getToken()::" + itemInCollapseUSPSentIndexMappingList.getToken());
					// System.out.println("itemInCollapseUSPSentIndexMappingList.getIndices()::" + itemInCollapseUSPSentIndexMappingList.getIndices());
					String curToken = itemInCollapseUSPSentIndexMappingList.getToken();
					String curIndices = itemInCollapseUSPSentIndexMappingList.getIndices();
					
					String[] curIndicesArray = curIndices.split(",");
					String[] subFirstIndexArray = curIndicesArray[0].split("-");
					String[] subLastIndexArray = curIndicesArray[curIndicesArray.length-1].split("-");
					
					if (itemInCollapseUSPSentIndexMappingListCounter == 0) {
						firstIndexInCurrentItem = Integer.parseInt(subFirstIndexArray[0]);
					}
					
					if (itemInCollapseUSPSentIndexMappingListCounter > 0) {
						firstIndexInCurrentItem = Integer.parseInt(subFirstIndexArray[0]);
 					}
					
					
					// System.out.println("lastIndexInPreviousItem::" + lastIndexInPreviousItem);					
					// System.out.println("firstIndexInCurrentItem::" + firstIndexInCurrentItem);
					
					
					
					if ((lastIndexInPreviousItem+1) < firstIndexInCurrentItem) {
						collapsedSentString += " , " + itemInCollapseUSPSentIndexMappingList.getToken();
						collapsedSentIndexString +=  ",\t\n";
						collapsedSentIndexString += itemInCollapseUSPSentIndexMappingList.getToken() + "\t" + itemInCollapseUSPSentIndexMappingList.getIndices() + "\n";
						
					} else {
						collapsedSentString += " " + itemInCollapseUSPSentIndexMappingList.getToken();
						collapsedSentIndexString += itemInCollapseUSPSentIndexMappingList.getToken() + "\t" + itemInCollapseUSPSentIndexMappingList.getIndices() + "\n";
					}
					
					lastIndexInPreviousItem = Integer.parseInt(subLastIndexArray[subLastIndexArray.length-1]);
 					
					
					itemInCollapseUSPSentIndexMappingListCounter++;
					
				}
				
				// System.out.println("collapsedSent::" + collapsedSentString);
				// System.out.println("collapsedSentIndexString::" + collapsedSentIndexString);

				
				
				/*
				if ( isCollapsedSentence(sentStanfordTokenizedStringTokens) == true) {
					CollapsedSentenceAndIndex generatedCollapsedSentence = generateCollapsedSentenceAndIndex(sentStanfordTokenizedStringTokens);
					// System.out.println("generatedCollapsedSentence::" + generatedCollapsedSentence.getCollapsedSentence() + "\n");
					
					collapsedSentString = generatedCollapsedSentence.getCollapsedSentence();
					collapsedSentIndexString = generatedCollapsedSentence.getCollapsedSentenceIndex();	

					
					
				} else {
					// System.out.println("sentReplacedByCategoryKwd::" + sentReplacedByCategoryKwd + "\n");
					
					collapsedSentString = sentReplacedByCategoryKwd; // non-collapsed sentence

					
					// reduce this
					
					// CollapsedSentenceAndIndex generatedCollapsedSentence = generateCollapsedSentenceAndIndex(sentStanfordTokenizedStringTokens);
					// collapsedSentIndexString = generatedCollapsedSentence.getCollapsedSentenceIndex();	

					String generatedNonCollapsedSentenceIndexString = generateNonCollapsedSentenceIndexString(sentStanfordTokenizedStringTokens);
					
					collapsedSentIndexString = generatedNonCollapsedSentenceIndexString;
					
					
					// System.out.println("non-collapsed::collapsedSentIndexString::\n" + collapsedSentIndexString + "\n");
					

					
					
				}
				*/
				
				
				

				
				// STEP 5: Build USP_COLLAPSED inputs
				// build collapsed USP inputs

				
				StringBuilder collapsedDepStringBuilder = new StringBuilder(); // Stanford Dependency
				StringBuilder collapsedInputStringBuilder = new StringBuilder();
				StringBuilder collapsedMorphStringBuilder = new StringBuilder();
				StringBuilder collapsedParseStringBuilder = new StringBuilder(); // Parse Tree
				StringBuilder collapsedTextStringBuilder = new StringBuilder();
				StringBuilder collapsedSentIndexStringBuilder = new StringBuilder();
				
				String collapsedDepStringPlain = "";
				
				// StringTokenizer collapsedSentStringToken = new StringTokenizer(collapsedSentString, " ");
				// if (collapsedSentStringToken.countTokens() < 100) {
				// }
				
				
				Annotation annotationCollapsedSent = new Annotation(collapsedSentString);
				this.tokenizeSSplitPosParse.annotate(annotationCollapsedSent);
				List<CoreMap> sentenceAnnotationsCollapsedSent = annotationCollapsedSent
						.get(SentencesAnnotation.class);
				for (CoreMap sentenceAnnotationCollapsedSent : sentenceAnnotationsCollapsedSent) {
					// result.add(sentenceAnnotationCollapsedSent.toString());
					for (CoreLabel token : sentenceAnnotationCollapsedSent
							.get(TokensAnnotation.class)) {

						String pos = token.get(PartOfSpeechAnnotation.class);
						// System.out.println(token + "_" + pos);

						if (token.toString().equals("_")) {
							collapsedInputStringBuilder.append("dash_" + pos + "\n");
							collapsedMorphStringBuilder.append("dash\n");
						} else {
							collapsedInputStringBuilder.append(token + "_" + pos + "\n");
							collapsedMorphStringBuilder.append(token.toString()
									.toLowerCase() + "\n");
						}

					}
					
					Tree tree = sentenceAnnotationCollapsedSent.get(TreeAnnotation.class);

					//System.out.println("The first sentence parsed is:");
					// tree.pennPrint(outputStream);
					String treeString = tree.pennString();
					//System.out.println(treeString);
					collapsedParseStringBuilder.append(treeString);
					
					SemanticGraph dependencies = sentenceAnnotationCollapsedSent
							.get(CollapsedCCProcessedDependenciesAnnotation.class);

					String depStringXml = dependencies.toString("xml");
					
					if (depStringXml.length() == 0) {
						System.out.println("No Dependency!");	
					}
					
					
					SAXBuilder saxBuilder = new SAXBuilder();
					try {
						Document xmlDocument = saxBuilder
								.build(new StringReader(depStringXml));
						// String message =
						// xmlDocument.getRootElement().getText();
						// System.out.println(message);
						Element rootNode = xmlDocument.getRootElement();
						// System.out.println(rootNode.getName());
						// //<dependencies
						// style="typed"> => dependencies
						// System.out.println(rootNode.getAttributeValue("style"));
						// // style="typed" => typed

						List depList = rootNode.getChildren("dep");
						for (int i = 0; i <= depList.size() - 1; i++) {
							Element element = (Element) depList.get(i);
							// System.out.println("dep type : "+
							// element.getAttributeValue("type"));
							collapsedDepStringPlain += element.getAttributeValue("type")
									+ "(";

							// System.out.println("governor : "+
							// element.getChildText("governor"));
							collapsedDepStringPlain += element.getChildText("governor")
									+ "-";
							List<Element> childrenList = element
									.getChildren("governor");
							for (int j = 0; j <= childrenList.size() - 1; j++) {
								Element element2 = childrenList.get(j);
								// System.out.println("idx : "+
								// element2.getAttributeValue("idx"));
								collapsedDepStringPlain += element2
										.getAttributeValue("idx");
							}
							collapsedDepStringPlain += ", ";

							// System.out.println("dependent : "+
							// element.getChildText("dependent"));
							collapsedDepStringPlain += element.getChildText("dependent")
									+ "-";
							List<Element> childrenList2 = element
									.getChildren("dependent");
							for (int j = 0; j <= childrenList2.size() - 1; j++) {
								Element element2 = childrenList2.get(j);
								// System.out.println("idx : "+
								// element2.getAttributeValue("idx"));
								collapsedDepStringPlain += element2
										.getAttributeValue("idx");
							}
							collapsedDepStringPlain += ")\n";

						}
						collapsedDepStringBuilder.append(collapsedDepStringPlain);
						// System.out.println("dependencies::" + dependencies);
						// System.out.println("depStringPlain:: " + depStringPlain);

					} catch (JDOMException e) {
						// handle JDOMException
					} catch (IOException e) {
						// handle IOException
					}

					// collapsedTextStringBuilder.append(sentence.getText());

				}

				// System.out.println("collapsedDepStringPlain ::\n" + collapsedDepStringPlain);					
				
				collapsedTextStringBuilder.append(collapsedSentString);
				collapsedSentIndexStringBuilder.append(collapsedSentIndexString);				
				
				if (collapsedDepStringBuilder.toString().length() == 0) continue;
				// System.out.println("Elvis::No Dependency!");	
				
				
				
				// write string into txt files
				new File("usp").mkdirs();
				new File("usp/morph_o").mkdirs();
				new File("usp/morph_o/0").mkdirs();
				new File("usp/text_o").mkdirs();
				new File("usp/text_o/0").mkdirs();
				
				
				new File("usp").mkdirs();
				new File("usp/dep").mkdirs();
				new File("usp/dep/0").mkdirs();
				new File("usp/morph").mkdirs();
				new File("usp/morph/0").mkdirs();
				new File("usp/text").mkdirs();
				new File("usp/text/0").mkdirs();
				new File("usp/parse").mkdirs();
				new File("usp/parse/0").mkdirs();
				new File("usp/index").mkdirs();
				new File("usp/index/0").mkdirs();


				try (PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("usp/morph_o/0/" + counter + ".morph",
								false)))) {
					out.println(morphStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(
						new BufferedWriter(new FileWriter("usp/text_o/0/"
								+ counter + ".txt", false)))) {
					out.println(textStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				
				
				try (PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("usp/index/0/" + counter + ".index", false)))) {
					out.println(collapsedSentIndexStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				
				try (PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("usp/dep/0/" + counter + ".dep", false)))) {
					out.println(collapsedDepStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("usp/morph/0/" + counter + ".input",
								false)))) {
					out.println(collapsedInputStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("usp/morph/0/" + counter + ".morph",
								false)))) {
					out.println(collapsedMorphStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(
						new BufferedWriter(new FileWriter("usp/text/0/"
								+ counter + ".txt", false)))) {
					out.println(collapsedTextStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				try (PrintWriter out = new PrintWriter(
						new BufferedWriter(new FileWriter("usp/parse/0/"
								+ counter + ".parse", false)))) {
					out.println(collapsedParseStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				
				counter++;
				
			}
			
			// counter++;
						
			log(LogLevel.INFO,
					"done building pos tagger and dependency as USP inputs using stanford corenlp pipeline...");
		}
	}	
	
	/*
	private String getCategoryName(String token, Hashtable<String, String> kwdListByCategory) {
		
		String categoryName = "";
		Iterator<Map.Entry<String, String>> kwdListByCategoryIterator = kwdListByCategory.entrySet().iterator();
		while (kwdListByCategoryIterator.hasNext()) {
			Map.Entry<String, String> entry = kwdListByCategoryIterator.next();
			String category_char_name = entry.getKey();
			String patternString = entry.getValue();
			
			int startIdx = token.indexOf("[");
			int endIdx = token.indexOf("]");
			String tokenCategoryName = token.substring(startIdx, endIdx);
			
			if (tokenCategoryName.equals(category_char_name)) {
				categoryName = category_char_name;
			}
			
		}
		
		return categoryName;
	}
	*/

	private String getPhrase(int indexStartNum, int indexEndNum, String[] depStringPlainArray) {
		String phraseString = "";

		for (int h = indexStartNum; h <= indexEndNum; h++) {
			if( depStringPlainArray.length > 1 ) {
				for ( int i = 0; i < depStringPlainArray.length; i++) {
					
					String rel = depStringPlainArray[i].substring(0,depStringPlainArray[i].indexOf("("));
					int i1 = depStringPlainArray[i].indexOf("(")+1, i3 = depStringPlainArray[i].lastIndexOf(")"), i2 = depStringPlainArray[i].indexOf(", ");
					while (i1 == i2 || (!Character.isDigit(depStringPlainArray[i].charAt(i2-1)) && depStringPlainArray[i].charAt(i2-1)!='\'')) {
						i2 = depStringPlainArray[i].indexOf(",",i2+1);
					}; 
					
					//	Utils.println("s="+s+" i1="+i1+" i2="+i2+" i3="+i3);
					
					String gov = depStringPlainArray[i].substring(i1,i2).trim();			
					String dep = depStringPlainArray[i].substring(i2+1,i3).trim();
					
					//	Utils.println("gov="+gov+" dep="+dep);
					
					// all-info
					if (gov.charAt(gov.length()-1)=='\'') gov=gov.substring(0, gov.length()-1);
					if (dep.charAt(dep.length()-1)=='\'') dep=dep.substring(0, dep.length()-1);			
					int govId=Integer.parseInt(gov.substring(gov.lastIndexOf("-")+1));
					int depId=Integer.parseInt(dep.substring(dep.lastIndexOf("-")+1));
					
					gov = gov.substring(0, gov.lastIndexOf("-")); 
					dep = dep.substring(0, dep.lastIndexOf("-"));
					
					//Chl d.
					// =>
					//Chl d
					if (dep.substring(dep.length()-1, dep.length()).equals(".")) {
						dep = dep.substring(0, dep.length()-1);
					}
					
												
					if (depId == h){
						if (!phraseString.contains(dep)) phraseString += dep + " ";
					}

				}						
			}
		}		
		
		
		return phraseString;
	}

	private String getTerm(int indexStartNum, String[] depStringPlainArray) {
		String termString = "";

		if( depStringPlainArray.length > 1 ) {
			for ( int i = 0; i < depStringPlainArray.length; i++) {
				
				String rel = depStringPlainArray[i].substring(0,depStringPlainArray[i].indexOf("("));
				int i1 = depStringPlainArray[i].indexOf("(")+1, i3 = depStringPlainArray[i].lastIndexOf(")"), i2 = depStringPlainArray[i].indexOf(", ");
				while (i1 == i2 || (!Character.isDigit(depStringPlainArray[i].charAt(i2-1)) && depStringPlainArray[i].charAt(i2-1)!='\'')) {
					i2 = depStringPlainArray[i].indexOf(",",i2+1);
				}; 
				
				//	Utils.println("s="+s+" i1="+i1+" i2="+i2+" i3="+i3);
				
				String gov = depStringPlainArray[i].substring(i1,i2).trim();			
				String dep = depStringPlainArray[i].substring(i2+1,i3).trim();
				
				//	Utils.println("gov="+gov+" dep="+dep);
				
				// all-info
				if (gov.charAt(gov.length()-1)=='\'') gov=gov.substring(0, gov.length()-1);
				if (dep.charAt(dep.length()-1)=='\'') dep=dep.substring(0, dep.length()-1);			
				int govId=Integer.parseInt(gov.substring(gov.lastIndexOf("-")+1));
				int depId=Integer.parseInt(dep.substring(dep.lastIndexOf("-")+1));
				
				gov = gov.substring(0, gov.lastIndexOf("-")); 
				dep = dep.substring(0, dep.lastIndexOf("-"));
				
				//Chl d.
				// =>
				//Chl d
				if (dep.substring(dep.length()-1, dep.length()).equals(".")) {
					dep = dep.substring(0, dep.length()-1);
				}
				
											
				if (depId == indexStartNum){
					termString = dep;
					
				}

			}						
		}
			
	
		
		
		return termString;
	}
	
	private int getCollapsedDepStringPlainDepId(String collapsedTerm, int depStartId, String[] collapsedDepStringPlainArray) {
		int returnDepId = 0;

		
		
		if( collapsedDepStringPlainArray.length > 1 ) {
			for ( int i = 0; i < collapsedDepStringPlainArray.length; i++) {
				
				String rel = collapsedDepStringPlainArray[i].substring(0,collapsedDepStringPlainArray[i].indexOf("("));
				int i1 = collapsedDepStringPlainArray[i].indexOf("(")+1, i3 = collapsedDepStringPlainArray[i].lastIndexOf(")"), i2 = collapsedDepStringPlainArray[i].indexOf(", ");
				while (i1 == i2 || (!Character.isDigit(collapsedDepStringPlainArray[i].charAt(i2-1)) && collapsedDepStringPlainArray[i].charAt(i2-1)!='\'')) {
					i2 = collapsedDepStringPlainArray[i].indexOf(",",i2+1);
				}; 
				
				//	Utils.println("s="+s+" i1="+i1+" i2="+i2+" i3="+i3);
				
				String gov = collapsedDepStringPlainArray[i].substring(i1,i2).trim();			
				String dep = collapsedDepStringPlainArray[i].substring(i2+1,i3).trim();
				
				//	Utils.println("gov="+gov+" dep="+dep);
				
				// all-info
				if (gov.charAt(gov.length()-1)=='\'') gov=gov.substring(0, gov.length()-1);
				if (dep.charAt(dep.length()-1)=='\'') dep=dep.substring(0, dep.length()-1);			
				int govId=Integer.parseInt(gov.substring(gov.lastIndexOf("-")+1));
				int depId=Integer.parseInt(dep.substring(dep.lastIndexOf("-")+1));
				
				gov = gov.substring(0, gov.lastIndexOf("-")); 
				dep = dep.substring(0, dep.lastIndexOf("-"));
				

				
				if (dep.equals(collapsedTerm) && depId > depStartId) {
					returnDepId = depId;
					break;
					// System.out.println("dep::"+ dep);
					// System.out.println("collapsedTerm::" + collapsedTerm);
					// System.out.println("depId::" + depId);
					// System.out.println("depStartId::" + depStartId);
					
				}

			}						
		}
		
		
		return returnDepId;
	}
	
	private void stanfordCoreNLPTest() throws IOException {

		Annotation annotation = new Annotation(
				"This bacteria is isolated from sea salt in Taiwan.");
		
		// This bacteria is isolated from sea salt in Taiwan.
		// Growth is most rapid at 34 to 37°C, and very slow or no growth occurs at temperatures below 12°C and above 40°C.  Type strain DMS1 was isolated from a slurry prepared from a eutrophic pond sediment (Campus of Dekkerswald Institute, Nijmegen, The Netherlands).
		// The type strain is MG62T (=JCM 10825T=DSM 13459T), which was isolated from a paddy field soil in Chikugo, Fukuoka, Japan.
		// The type strain, SH-6T (=CECT 7173T=CGMCC 1.6379T=JCM 14033T), was isolated from Shangmatala salt lake, Inner Mongolia, China.

		
		this.tokenizeSSplitPosParse.annotate(annotation);

		List<CoreMap> sentenceAnnotations = annotation
				.get(SentencesAnnotation.class);

		String treeString = "";
		if (sentenceAnnotations != null && sentenceAnnotations.size() > 0) {
			CoreMap sentence = sentenceAnnotations.get(0);
			Tree tree = sentence.get(TreeAnnotation.class);

			System.out.println("The first sentence parsed is:");
			// tree.pennPrint(outputStream);
			treeString = tree.pennString();
			System.out.println(treeString);
		}
		// http://stackoverflow.com/questions/11832490/stanford-core-nlp-java-output

		// traverse the parse tree
		// http://stackoverflow.com/questions/10474827/get-certain-nodes-out-of-a-parse-tree
		// [java-nlp-user] Traversing the Parse Tree
		// https://mailman.stanford.edu/pipermail/java-nlp-user/2011-April/000906.html

		// http://stackoverflow.com/questions/6624479/extracting-sub-trees-using-stanford-tregex
		// -->
		// -->
		// TreeReader r = new PennTreeReader(new
		// StringReader("(VP (VP (VBZ Try) (NP (NP (DT this) (NN wine)) (CC and) (NP (DT these) (NNS snails)))) (PUNCT .))"),
		// new LabeledScoredTreeFactory(new StringLabelFactory()));

		// http://tides.umiacs.umd.edu/webtrec/stanfordParser/javadoc/edu/stanford/nlp/trees/PennTreeReader.html
		// TreeReader tr = new PennTreeReader(new BufferedReader(new
		// InputStreamReader(new FileInputStream(file),"UTF-8")),
		// myTreeFactory);
		// Tree t = tr.readTree();

		TreeReader tr = new PennTreeReader(new StringReader(treeString),
				new LabeledScoredTreeFactory(new StringLabelFactory()));
		Tree tree2 = tr.readTree();
		String tree2String = tree2.pennString();
		System.out.println(tree2String);

		TregexPattern tgrepPattern = TregexPattern.compile("PP <1 (IN << from)");

		// upper level
		//TregexPattern tgrepPattern = TregexPattern
		//		.compile("VP <1 (VBN << " + "isolated" + ")");
		
		// NP <1 (NP << Bank)
		// http://stackoverflow.com/questions/3693323/how-do-i-manipulate-parse-trees

		TregexMatcher m = tgrepPattern.matcher(tree2);
		while (m.find()) {
			Tree subtree = m.getMatch();
			System.out.println(subtree.toString());

			
			final StringBuilder sb = new StringBuilder();

			for ( final Tree t : subtree.getLeaves() ) {
			     sb.append(t.toString()).append(" ");
			}
			System.out.println("sb::" + sb);
			// http://stackoverflow.com/questions/11148890/how-to-print-the-parse-tree-of-stanford-javanlp
			// 
			
			
			
			
			// Tree[] aaa = subtree.children();
			// for ( int i = 0; i < aaa.length; i++ ) {
			//	System.out.println("i::" + aaa[i].toString());
			// }
			
			// System.out.println(subtree.pennString()); // equal => subtree.pennPrint();

		}

		// Tree t = Tree
		// 		.valueOf("(ROOT (S (NP (NP (NNP Bank)) (PP (IN of) (NP (NNP America)))) (VP (VBD used) (S (VP (TO to) (VP (VB be) (VP (VBN called) (NP (NP (NNP Bank)) (PP (IN of) (NP (NNP Italy)))))))))))");
		// TregexPattern pat = TregexPattern
		//		.compile("NP <1 (NP << Bank) <2 PP=remove");
		// TsurgeonPattern surgery = Tsurgeon
		//		.parseOperation("excise remove remove");
		// Tsurgeon.processPattern(pat, surgery, t).pennPrint();

	}

	
	private void stanfordCoreNLPTest2() throws IOException {
		
		
		Annotation annotation = new Annotation(
				"Utilizes acetate, lactate, malic acid, fumaric acid, sucrose, L-glutamic acid, glucose, fructose, succinate, lactose, DL-aspartic acid, pyruvate, glycine, galactose, sorbitol, glycerol, starch, L-histidine, trehalose, DL-norleucine, D-glucuronic acid, DL-phenylalanine, aesculin and salicin, but not L-arginine, L-alanine, sodium citrate, xylose, mannitol, L-threonine, dulcitol, dextrin, L-methionine, 3,3-dimethylglutaric acid or L-tyrosine.");
		
		// Utilizes acetate, lactate, malic acid, fumaric acid, sucrose, L-glutamic acid, glucose, fructose, succinate, lactose, DL-aspartic acid, pyruvate, glycine, galactose, sorbitol, glycerol, starch, L-histidine, trehalose, DL-norleucine, D-glucuronic acid, DL-phenylalanine, aesculin and salicin, but not L-arginine, L-alanine, sodium citrate, xylose, mannitol, L-threonine, dulcitol, dextrin, L-methionine, 3,3-dimethylglutaric acid or L-tyrosine.
		// Utilizes H2/CO2, formate, 2-propanol/CO2 and 2-butanol/ CO2 for growth and/or methane production.
		// Utilizes glucose, fructose, glycerol, maltose, trehalose, starch, propionate, fumarate, acetate, threonine, asparagine and lysine as single carbon and energy sources for growth."
		// Christopher Manning owns club barcelona?

		
		this.tokenizeSSplitPosParse.annotate(annotation);

		List<CoreMap> sentenceAnnotations = annotation
				.get(SentencesAnnotation.class);

		String treeString = "";
		if (sentenceAnnotations != null && sentenceAnnotations.size() > 0) {
			CoreMap sentence = sentenceAnnotations.get(0);
			Tree tree = sentence.get(TreeAnnotation.class);

			System.out.println("The first sentence parsed is:");
			// tree.pennPrint(outputStream);
			treeString = tree.pennString();
			System.out.println(treeString);
		}
		
		
		
		TreeReader tr = new PennTreeReader(new StringReader(treeString),
				new LabeledScoredTreeFactory(new StringLabelFactory()));
		Tree tree = tr.readTree();
		// System.out.println(tree.pennString());
		
		
		TregexPattern NPpattern = TregexPattern.compile("@NP !<< @NP");
		TregexMatcher matcher = NPpattern.matcher(tree);
		
		int counter = 0;
		while (matcher.findNextMatchingNode()) {
			Tree match = matcher.getMatch();
			System.out.println(counter + "::" + match.yield());
			
			for (int i = 0; i < match.yield().size(); i++) {
				System.out.println("J::" + i + "::" +match.yield().get(i));
			}
				
			counter+=1;
			
		}
			
		
	}
	
	
	private void readNewXml() {
		log(LogLevel.INFO, "Reading test sentences...");
		File inputFolder = new File(testFolder);
		File[] inputFiles = inputFolder.listFiles();
		List<TaxonTextFile> textFiles = new LinkedList<TaxonTextFile>();
		for (File inputFile : inputFiles) {
			log(LogLevel.INFO, "Reading from " + inputFile.getName() + "...");
			try {
				textReader.setInputStream(new FileInputStream(inputFile));
				String taxon = textReader.getTaxon();
				log(LogLevel.INFO, "Taxon: " + taxon);
				
				String text = textReader.read();
				log(LogLevel.INFO, "Text: " + text);
				textFiles.add(new TaxonTextFile(taxon, text, inputFile));
			} catch (Exception e) {
				log(LogLevel.ERROR, "Could not read test sentences from "
						+ inputFile.getName(), e);
			}
		}
	}
	
	
	private String stanfordTokenizerTransformation(String oriSent) {
		String returnString = "";
				
		Annotation annotation = new Annotation(oriSent);
		this.tokenizeSSplit.annotate(annotation);
		List<CoreMap> sentenceAnnotations = annotation
				.get(SentencesAnnotation.class);
		
		if (sentenceAnnotations.size() > 0) {
			for (CoreMap sentenceAnnotation : sentenceAnnotations) {
				// result.add(sentenceAnnotation.toString());
				for (CoreLabel token : sentenceAnnotation
						.get(TokensAnnotation.class)) {
					returnString += " " + token;
				}
			}
			returnString = returnString.substring(1);	
		} else {
			returnString = oriSent;
		}
		
		return returnString;
	}
	
	private List<String> ssplit2(String text) {
		log(LogLevel.INFO, "ssplit2:: split text to sentences using stanford corenlp pipeline...");
		List<String> result = new LinkedList<String>();
		
		Annotation annotation = new Annotation(text);
		this.tokenizeSSplit.annotate(annotation);
		List<CoreMap> sentenceAnnotations = annotation
				.get(SentencesAnnotation.class);
		
		for (CoreMap sentenceAnnotation : sentenceAnnotations) {
			result.add(sentenceAnnotation.toString());
		}			
		
		log(LogLevel.INFO, "done ssplit2:: splitting text to sentences using stanford corenlp pipeline. Created " + result.size() + " sentences");
		
		return result;
	}	
	
	
	public static String[] tokenize(String sentence) {
		String[] returnStringArray = sentence.split("\\s+");
		
		// for (int i = 0; i < returnStringArray.length; i++ ) {
		// 	returnStringArray[i]
		// }
		
		return returnStringArray;
	}
	

	
	private static CollapsedSentenceAndIndex generateCollapsedSentenceAndIndex(String[] tokenStrings) {
		
		
		int tokenListCounter = 0;
		String swapTargetString = "";
		
		// String grpTermIndexList = "";
		
	
		List<String> grpTermAndIndex = new ArrayList<String>();
		List<List<String>> grpTermAndIndexList = new ArrayList<List<String>>();
		
		for (String itemInTokenList : tokenStrings) {

			if (!itemInTokenList.equals(",")) {				
				if ( !swapTargetString.equals(itemInTokenList)) {
					grpTermAndIndex = new ArrayList<String>();
		
					swapTargetString = itemInTokenList;

					// grpTermIndexList += "," + itemInTokenList + "-" + tokenListCounter;					
					grpTermAndIndex.add(itemInTokenList);
					grpTermAndIndex.add(String.valueOf(tokenListCounter));
					grpTermAndIndexList.add(grpTermAndIndex);
					
				}else if (swapTargetString.equals(itemInTokenList)) {
					// grpTermIndexList += "-" + tokenListCounter;
					grpTermAndIndex.add(String.valueOf(tokenListCounter));

				}				
			}

			tokenListCounter++;
		}

		// System.out.println("grpTermIndexList::" + grpTermIndexList);

		// if (grpTermIndexList.substring(0, 1).equals(",")) {
		//	grpTermIndexList = grpTermIndexList.substring(1);
		// }
		
		// System.out.println("grpTermIndexList::2::" + grpTermIndexList);
		// System.out.println("grpTermAndIndexList ::" + grpTermAndIndexList.toString());
		
		
		StringBuilder collapsedSent = new StringBuilder();
		
		StringBuilder collapsedSentIndex = new StringBuilder();
		
		for (int i = 0; i < grpTermAndIndexList.size(); i++) {

			if ( i < grpTermAndIndexList.size() -1 ) {
				List<String> grpTermIndexListArrayItemArray = grpTermAndIndexList.get(i);
				List<String> grpTermIndexListArrayItemArray2 = grpTermAndIndexList.get(i+1);
				
				int grpTermIndexListArrayItemArray2FirstIndex = Integer.parseInt(grpTermIndexListArrayItemArray2.get(1));
				int grpTermIndexListArrayItemArrayLastIndex = Integer.valueOf(grpTermIndexListArrayItemArray.get(grpTermIndexListArrayItemArray.size()-1));
				
				// System.out.println("grpTermIndexListArrayItemArray2FirstIndex::" + grpTermIndexListArrayItemArray2FirstIndex);
				// System.out.println("grpTermIndexListArrayItemArrayLastIndex::" + grpTermIndexListArrayItemArrayLastIndex);

				String indexListString = "";
				for ( int j = 1; j < grpTermIndexListArrayItemArray.size(); j++ ) {
					indexListString += grpTermIndexListArrayItemArray.get(j) + ",";
				}
				
				if ( indexListString.substring(indexListString.length()-1, indexListString.length()).equals(",") ) {
					indexListString = indexListString.substring(0, indexListString.length()-1);
				}
				
				if ( grpTermIndexListArrayItemArray2FirstIndex == (grpTermIndexListArrayItemArrayLastIndex+1) ) {
					collapsedSent.append(grpTermIndexListArrayItemArray.get(0) + " ");
					
					collapsedSentIndex.append(grpTermIndexListArrayItemArray.get(0) + "\t" + indexListString + "\n");

				} else if ( grpTermIndexListArrayItemArray2FirstIndex > (grpTermIndexListArrayItemArrayLastIndex+1) ) {
					collapsedSent.append(grpTermIndexListArrayItemArray.get(0) + ", ");
					
					collapsedSentIndex.append(grpTermIndexListArrayItemArray.get(0) + "\t" + indexListString + "\n");
					collapsedSentIndex.append(",\t\n");

				}
			} else {
				
				String lastTermString = grpTermAndIndexList.get(i).get(0).toString();
				String lastTermIndexListString = "";
				
				for ( int j = 1; j < grpTermAndIndexList.get(i).size(); j++ ) {
					lastTermIndexListString += grpTermAndIndexList.get(i).get(j) + ",";
				}
				
				if ( lastTermIndexListString.substring(lastTermIndexListString.length()-1, lastTermIndexListString.length()).equals(",") ) {
					lastTermIndexListString = lastTermIndexListString.substring(0, lastTermIndexListString.length()-1);
				}
				
				
				collapsedSent.append(lastTermString);

				
				collapsedSentIndex.append(lastTermString + "\t" + lastTermIndexListString + "\n");
				
				
			}			
		}
		
		// System.out.println("collapsedSent::" + collapsedSent.toString());
		// System.out.println("collapsedSentIndex::\n" + collapsedSentIndex.toString());
		
		
		String returnCollapsedSentString = collapsedSent.toString();
		String returnCollapsedSentIndexString = collapsedSentIndex.toString();
		
		CollapsedSentenceAndIndex returnCollapsedSentenceAndIndex = new CollapsedSentenceAndIndex();
		returnCollapsedSentenceAndIndex.setCollapsedSentence(returnCollapsedSentString);
		returnCollapsedSentenceAndIndex.setCollapsedSentenceIndex(returnCollapsedSentIndexString);
		
		return returnCollapsedSentenceAndIndex;
	}	
	

	
	
	
	private static String generateNonCollapsedSentenceIndexString(String[] tokenStrings) {
		
		String returnNonCollapsedSentIndexString = "";
		
		int tokenListCounter = 0;
		String swapTargetString = "";
				
	
		List<String> grpTermAndIndex = new ArrayList<String>();
		List<List<String>> grpTermAndIndexList = new ArrayList<List<String>>();
		
		for (String itemInTokenList : tokenStrings) {

			if (!itemInTokenList.equals(",")) {				
				if ( !swapTargetString.equals(itemInTokenList)) {
					grpTermAndIndex = new ArrayList<String>();
		
					swapTargetString = itemInTokenList;

					grpTermAndIndex.add(itemInTokenList);
					grpTermAndIndex.add(String.valueOf(tokenListCounter));
					grpTermAndIndexList.add(grpTermAndIndex);
					
				}else if (swapTargetString.equals(itemInTokenList)) {
					grpTermAndIndex.add(String.valueOf(tokenListCounter));

				}				
			}

			tokenListCounter++;
		}

		
		StringBuilder collapsedSentIndex = new StringBuilder();
		
		for (int i = 0; i < grpTermAndIndexList.size(); i++) {

			if ( i < grpTermAndIndexList.size() -1 ) {
				List<String> grpTermIndexListArrayItemArray = grpTermAndIndexList.get(i);
				List<String> grpTermIndexListArrayItemArray2 = grpTermAndIndexList.get(i+1);
				
				int grpTermIndexListArrayItemArray2FirstIndex = Integer.parseInt(grpTermIndexListArrayItemArray2.get(1));
				int grpTermIndexListArrayItemArrayLastIndex = Integer.valueOf(grpTermIndexListArrayItemArray.get(grpTermIndexListArrayItemArray.size()-1));
				
				// System.out.println("grpTermIndexListArrayItemArray2FirstIndex::" + grpTermIndexListArrayItemArray2FirstIndex);
				// System.out.println("grpTermIndexListArrayItemArrayLastIndex::" + grpTermIndexListArrayItemArrayLastIndex);

				String indexListString = "";
				for ( int j = 1; j < grpTermIndexListArrayItemArray.size(); j++ ) {
					indexListString += grpTermIndexListArrayItemArray.get(j) + ",";
				}
				
				if ( indexListString.substring(indexListString.length()-1, indexListString.length()).equals(",") ) {
					indexListString = indexListString.substring(0, indexListString.length()-1);
				}
				
				if ( grpTermIndexListArrayItemArray2FirstIndex == (grpTermIndexListArrayItemArrayLastIndex+1) ) {
					
					collapsedSentIndex.append(grpTermIndexListArrayItemArray.get(0) + "\t" + indexListString + "\n");

				} else if ( grpTermIndexListArrayItemArray2FirstIndex > (grpTermIndexListArrayItemArrayLastIndex+1) ) {
					
					collapsedSentIndex.append(grpTermIndexListArrayItemArray.get(0) + "\t" + indexListString + "\n");
					collapsedSentIndex.append(",\t\n");

				}
			} else {
				
				String lastTermString = grpTermAndIndexList.get(i).get(0).toString();
				String lastTermIndexListString = "";
				
				for ( int j = 1; j < grpTermAndIndexList.get(i).size(); j++ ) {
					lastTermIndexListString += grpTermAndIndexList.get(i).get(j) + ",";
				}
				
				if ( lastTermIndexListString.substring(lastTermIndexListString.length()-1, lastTermIndexListString.length()).equals(",") ) {
					lastTermIndexListString = lastTermIndexListString.substring(0, lastTermIndexListString.length()-1);
				}
				
				
		
				collapsedSentIndex.append(lastTermString + "\t" + lastTermIndexListString + "\n");
				
				
			}			
		}
		
		returnNonCollapsedSentIndexString = collapsedSentIndex.toString();
		
		return returnNonCollapsedSentIndexString;
	}	
	
	
	private static boolean isCollapsedSentence(String[] tokenStrings) {
		boolean returnIsCollapsedSent = false;
		
		int tokenListCounter = 0;
		String swapTargetString = "";
		
		for (String itemInTokenList : tokenStrings) {
			if (!itemInTokenList.equals(",")) {				
				if ( !swapTargetString.equals(itemInTokenList)) {
		
					swapTargetString = itemInTokenList;
					
				}else if (swapTargetString.equals(itemInTokenList)) {
					returnIsCollapsedSent = true;
					
				}				
			}
			tokenListCounter++;
		}
		return returnIsCollapsedSent;
	}	
	
	private void testAAA() {
		String sent = "Sensitive to c4-Antibiotics and c4-Antibiotics , but not to c4-Antibiotics or c4-Antibiotics .";
		// String sent = "Glycerol and starch not utilized.";
		// String sent = "Sensitive to AAA and BBB but not to CCC and DDD.";
		
		Tree parseTree = lexicalizedParser.parse(sent);
		// System.out.println(parseTree);		

		// TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
	    // tp.printTree(parseTree);

		TreePrint tp = new TreePrint("penn");
		tp.printTree(parseTree);
		
		TreePrint tp2 = new TreePrint("typedDependenciesCollapsed");
		tp2.printTree(parseTree);
		
		
	}	
}
