package edu.arizona.biosemantics.micropie;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.extract.TaxonCharacterMatrixCreator;
import edu.arizona.biosemantics.micropie.io.CSVTaxonCharacterMatrixWriter;
import edu.arizona.biosemantics.micropie.io.XMLTextReader;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.transform.CompoundSentenceSplitRun;
import edu.arizona.biosemantics.micropie.transform.ITextNormalizer;
import edu.arizona.biosemantics.micropie.transform.SentenceSplitRun;
import edu.arizona.biosemantics.micropie.transform.SentenceSpliter;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

public class MicroPIEProcessor{
	//System Resources
	private LexicalizedParser lexicalizedParser;
	private SentencePredictor sentencePredictor;
	private SentenceSpliter sentenceSpliter;
	private boolean parallelProcessing;
	private int maxThreads;
	
	//Class Members
	private ListeningExecutorService executorService;
	private TaxonCharacterMatrixCreator matrixCreator;
	private CSVTaxonCharacterMatrixWriter matrixWriter;
	private String matrixFile;
	
	//Running members?
	private Map<Sentence, SentenceMetadata> sentenceMetadataMap;
	private Map<TaxonTextFile, List<Sentence>> taxonSentencesMap;
	private Map<Sentence, MultiClassifiedSentence> sentenceClassificationMap;

	@Inject
	public MicroPIEProcessor(TaxonCharacterMatrixCreator matrixCreator,
			CSVTaxonCharacterMatrixWriter matrixWriter,
			@Named("matrixFile") String matrixFile,
			@Named("parallelProcessing") boolean parallelProcessing,
			@Named("maxThreads") int maxThreads,
			@Named("SentenceMetadataMap") Map<Sentence, SentenceMetadata> sentenceMetadataMap,
			@Named("TaxonSentencesMap") Map<TaxonTextFile, List<Sentence>> taxonSentencesMap,
			@Named("SentenceClassificationMap") Map<Sentence, MultiClassifiedSentence> sentenceClassificationMap,
			LexicalizedParser lexicalizedParser,
			SentencePredictor sentencePredictor,
			SentenceSpliter sentenceSpliter) {
		this.matrixCreator = matrixCreator;
		this.matrixWriter = matrixWriter;
		this.matrixFile = matrixFile;
		this.parallelProcessing = parallelProcessing;
		this.maxThreads = maxThreads;		
		
		this.sentenceMetadataMap = sentenceMetadataMap;
		this.taxonSentencesMap = taxonSentencesMap;
		this.sentenceClassificationMap = sentenceClassificationMap;
		
		this.lexicalizedParser = lexicalizedParser;
		this.sentencePredictor = sentencePredictor;
		this.sentenceSpliter = sentenceSpliter;
		
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

	
	public void processFolder(String folder) {
		
		try {
			// splite sentences

			long b = System.currentTimeMillis();
			List<Sentence> testSentences = this.createSentencesFromFolder(folder);
			long e = System.currentTimeMillis();
			System.out.println("splitting the sentences costs "+(e-b)+" ms");
			b = System.currentTimeMillis();
			List<MultiClassifiedSentence> predictions = new LinkedList<MultiClassifiedSentence>(); // TODO possibly parallelize here
			for (Sentence testSentence : testSentences) {
				Set<ILabel> prediction = sentencePredictor.predict(testSentence);
				MultiClassifiedSentence classifiedSentence = new MultiClassifiedSentence( testSentence, prediction);
				sentenceClassificationMap.put(testSentence,classifiedSentence);
				predictions.add(classifiedSentence);
			}
			e = System.currentTimeMillis();
			System.out.println("predicting categories of the sentences costs "+(e-b)+" ms");
			
			/*
			for ( MultiClassifiedSentence prediction: predictions ) {
		    	String[] array =  prediction.getPredictions().toArray(new String[0]);
				// System.out.println("Prediction::" + prediction.getPredictions().toArray().toString());
				System.out.println("Prediction::" + Arrays.toString(array));
				System.out.println("Sentence::" + prediction.getSentence().toString());
			}
			*/

			/*
			classifiedSentenceWriter.setInputStream(new FileInputStream(svmLabelAndCategoryMappingFile));
			classifiedSentenceWriter.setOutputStream(new FileOutputStream(predictionsFile));
			classifiedSentenceWriter.readSVMLabelAndCategoryMapping();
			classifiedSentenceWriter.write(predictions);
			 */
			
			b = System.currentTimeMillis();
			TaxonCharacterMatrix matrix = matrixCreator.create();
			matrixWriter.setOutputStream(new FileOutputStream(matrixFile, true));
			matrixWriter.write(matrix);
			e = System.currentTimeMillis();
			System.out.println("extracting characters from the sentences costs "+(e-b)+" ms");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * create sentences from the test folders
	 * @param folder
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private List<Sentence> createSentencesFromFolder(String folder) throws IOException,
			InterruptedException, ExecutionException {
		log(LogLevel.INFO, "Reading test sentences...");
		
		//read and parse all the files in the folder
		File inputFolder = new File(folder);
		File[] inputFiles = inputFolder.listFiles();
		List<TaxonTextFile> textFiles = new LinkedList<TaxonTextFile>();
		XMLTextReader textReader = new XMLTextReader();
		for (File inputFile : inputFiles) {
			log(LogLevel.INFO, "Reading from " + inputFile.getName() + "...");
			try {
				textReader.setInputStream(new FileInputStream(inputFile));
				log(LogLevel.INFO, "XML file name: " + inputFile.getName());
				//System.out.println("XML file name: " + inputFile.getName());
				String taxon = textReader.getTaxon();
				log(LogLevel.INFO, "Taxon: " + taxon);

				String family = textReader.getFamily();
				log(LogLevel.INFO, "Family: " + family);

				String genus = textReader.getGenus();
				log(LogLevel.INFO, "Genus: " + genus);

				String species = textReader.getSpecies();
				log(LogLevel.INFO, "Species: " + species);

				String strain_number = textReader.getStrain_number();
				log(LogLevel.INFO, "Strain_number: " + strain_number);

				String the16SrRNAAccessionNumber = textReader
						.get16SrRNAAccessionNumber();
				log(LogLevel.INFO, "16S rRNA Accession Number: "
						+ the16SrRNAAccessionNumber);

				String text = textReader.read();
				log(LogLevel.INFO, "Text: " + text);

				textFiles.add(new TaxonTextFile(taxon, family, genus, species,
						strain_number, the16SrRNAAccessionNumber, text,
						inputFile));

			} catch (Exception e) {
				log(LogLevel.ERROR, "Could not read test sentences from "
						+ inputFile.getName(), e);
			}
		}
		
		//System.out.println(folder+" is read ! ");
		
		//split sentences by parallel processing
		List<ListenableFuture<List<String>>> sentenceSplits = new ArrayList<ListenableFuture<List<String>>>(
				textFiles.size());
		CountDownLatch sentenceSplitLatch = new CountDownLatch(textFiles.size());
		for(int i=0;i<10;i++){
		for (TaxonTextFile textFile : textFiles) {
			//build new thread
			SentenceSplitRun splitRun = new SentenceSplitRun(
					textFile.getText(), sentenceSplitLatch, sentenceSpliter);
			ListenableFuture<List<String>> futureResult = executorService.submit(splitRun);
			sentenceSplits.add(futureResult);
			//System.out.println(" threads added! ");
		}
		}
		try {
			sentenceSplitLatch.await();
			//System.out.println(" threads run over! ");
		} catch (InterruptedException e) {
			log(LogLevel.ERROR, "Problem with latch", e);
		}

		//int numberOfSentences = getNumberOfSentences(sentenceSplits);
		List<List<ListenableFuture<List<String>>>> subsentenceSplitsPerFile = new LinkedList<List<ListenableFuture<List<String>>>>();

		for (int i = 0; i < textFiles.size(); i++) {
			List<String> sentences = sentenceSplits.get(i).get();//sentences for each file
			List<ListenableFuture<List<String>>> subsentenceSplits = new LinkedList<ListenableFuture<List<String>>>();
			for (final String sentence : sentences) {

				// String[] tokens = sentence.split("\\s+");
				// System.out.println("length " + tokens.length);
				// int tokenSize = tokens.length;
				// overall += size;
				// if(size > maxSize) { maxSize = size; }

				if (sentence.length() <= 1) {
					// if (sentence.length() <= 50) {
					// if (sentence.length() <= 100) {
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
					//System.out.println("get");
					List<String> result = future.get();
				} catch (Exception e) {
					//System.out.println("something went wrong with this guy");
					e.printStackTrace();
				}
			}
		}

		List<Sentence> result = new LinkedList<Sentence>();
		for (int i = 0; i < textFiles.size(); i++) {//each file
			List<ListenableFuture<List<String>>> fileFuture = subsentenceSplitsPerFile.get(i);
			for (int j = 0; j < fileFuture.size(); j++) {//sentences in the file
				List<String> subsentences = fileFuture.get(j).get();// .get();
				for (String subsentence : subsentences) {//subsentences of the sentence
					Sentence sentence = new Sentence(subsentence);
					result.add(sentence);
					SentenceMetadata metadata = new SentenceMetadata();
					metadata.setSourceId(j);
					metadata.setTaxonTextFile(textFiles.get(i));
					metadata.setCompoundSplitSentence(subsentences.size() > 1);
					// metadata.setParseResult(textSentenceTransformer.getCachedParseResult(sentence));
					sentenceMetadataMap.put(sentence, metadata);
					
					//TODO: if taxon file is the key, more memory is needed.
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

	/**
	 * @param sentenceSplitsList
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
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
}
