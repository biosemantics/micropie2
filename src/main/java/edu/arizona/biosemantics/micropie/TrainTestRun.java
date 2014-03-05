package edu.arizona.biosemantics.micropie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

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
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.transform.CompoundSentenceSplitRun;
import edu.arizona.biosemantics.micropie.transform.ITextNormalizer;
import edu.arizona.biosemantics.micropie.transform.SentenceSplitRun;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

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


	@Inject
	public TrainTestRun(
			@Named("trainingFile") String trainingFile,
			@Named("testFolder") String testFolder,
			@Named("parallelProcessing") boolean parallelProcessing,
			@Named("maxThreads") int maxThreads,
			@Named("predictionsFile") String predictionsFile,
			@Named("matrixFile") String matrixFile,
			@Named("SentenceClassificationMap") Map<Sentence, MultiClassifiedSentence> sentenceClassificationMap,
			@Named("SentenceMetadataMap") Map<Sentence, SentenceMetadata> sentenceMetadataMap,
			@Named("TaxonSentencesMap") Map<TaxonTextFile, List<Sentence>> taxonSentencesMap,
			MultiSVMClassifier classifier, CSVSentenceReader sentenceReader,
			XMLTextReader textReader, ITextNormalizer textNormalizer,
			@Named("TokenizeSSplit") StanfordCoreNLP tokenizeSSplit,
			@Named("TokenizeSSplitPosParse") StanfordCoreNLP tokenizeSSplitPosParse,
			LexicalizedParser lexicalizedParser,
			CSVClassifiedSentenceWriter classifiedSentenceWriter,
			TaxonCharacterMatrixCreator matrixCreator,
			CSVTaxonCharacterMatrixWriter matrixWriter) {
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
		this.tokenizeSSplit = tokenizeSSplit;
		this.tokenizeSSplitPosParse = tokenizeSSplitPosParse;
		this.lexicalizedParser = lexicalizedParser;
		this.classifiedSentenceWriter = classifiedSentenceWriter;
		this.matrixCreator = matrixCreator;
		this.matrixWriter = matrixWriter;

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

			// Parse uspParse = new Parse();
			// uspParse.runParse("usp", "usp_results");
			
			
			
			// sentenceReader.setInputStream(new FileInputStream(trainingFile));
			// List<Sentence> trainingSentences = sentenceReader.read();
			// nFolderCrossValidation(10, trainingSentences);

			// List<Sentence> testSentences = createTestSentences();
			// createUSPInputs(testSentences);

			
			sentenceReader.setInputStream(new FileInputStream(trainingFile));
			List<Sentence> trainingSentences = sentenceReader.read();
			classifier.train(trainingSentences);

			List<Sentence> testSentences = createTestSentences();
			List<MultiClassifiedSentence> predictions = new LinkedList<MultiClassifiedSentence>();
			// TODO possibly parallelize here
			for (Sentence testSentence : testSentences) {
				Set<ILabel> prediction = classifier
						.getClassification(testSentence);
				MultiClassifiedSentence classifiedSentence = new MultiClassifiedSentence(
						testSentence, prediction);
				sentenceClassificationMap.put(testSentence, classifiedSentence);
				predictions.add(classifiedSentence);
			}

			classifiedSentenceWriter.setOutputStream(new FileOutputStream(
					predictionsFile));
			classifiedSentenceWriter.write(predictions);

			TaxonCharacterMatrix matrix = matrixCreator.create();
			matrixWriter.setOutputStream(new FileOutputStream(matrixFile));
			matrixWriter.write(matrix);
			
			
			
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
					sentenceSplitLatch);
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
				/*
				 * String[] tokens = sentence.split("\\s+");
				 * System.out.println("length " + tokens.length); int size =
				 * tokens.length; overall += size; if(size > maxSize) { maxSize
				 * = size; }
				 */
				if (sentence.length() <= 80) {

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

	private void createUSPInputs(List<Sentence> sentenceList)
			throws IOException, InterruptedException, ExecutionException {

		int counter = 1;
		for (Sentence sentence : sentenceList) {

			StringBuilder depStringBuilder = new StringBuilder();
			StringBuilder inputStringBuilder = new StringBuilder();
			StringBuilder morphStringBuilder = new StringBuilder();
			StringBuilder textStringBuilder = new StringBuilder();

			String text = sentence.getText(); // it is sentence based not text based anymore
			log(LogLevel.INFO,
					"build pos tagger and dependency as USP inputs using stanford corenlp pipeline...");

			Annotation annotation = new Annotation(text);
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

				// System.out.println("xml format::" +
				// dependencies.toString("xml"));
				String depStringXml = dependencies.toString("xml");

				String depStringPlain = "";
				SAXBuilder saxBuilder = new SAXBuilder();
				try {
					Document xmlDocument = saxBuilder.build(new StringReader(
							depStringXml));
					// String message = xmlDocument.getRootElement().getText();
					// System.out.println(message);
					Element rootNode = xmlDocument.getRootElement();
					// System.out.println(rootNode.getName()); //<dependencies
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
							depStringPlain += element2.getAttributeValue("idx");
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
							depStringPlain += element2.getAttributeValue("idx");
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

				textStringBuilder.append(sentence.getText());

			}

			new File("usp").mkdirs();
			new File("usp/dep").mkdirs();
			new File("usp/dep/0").mkdirs();
			new File("usp/morph").mkdirs();
			new File("usp/morph/0").mkdirs();
			new File("usp/text").mkdirs();
			new File("usp/text/0").mkdirs();

			try (PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter("usp/dep/0/" + counter + ".dep", false)))) {
				out.println(depStringBuilder);
			} catch (IOException e) {
				// exception handling left as an exercise for the reader
			}
			try (PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter("usp/morph/0/" + counter + ".input", false)))) {
				out.println(inputStringBuilder);
			} catch (IOException e) {
				// exception handling left as an exercise for the reader
			}
			try (PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter("usp/morph/0/" + counter + ".morph", false)))) {
				out.println(morphStringBuilder);
			} catch (IOException e) {
				// exception handling left as an exercise for the reader
			}
			try (PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter("usp/text/0/" + counter + ".txt", false)))) {
				out.println(textStringBuilder);
			} catch (IOException e) {
				// exception handling left as an exercise for the reader
			}

			counter++;
			log(LogLevel.INFO,
				"done building pos tagger and dependency as USP inputs using stanford corenlp pipeline...");

		}
	}

}
