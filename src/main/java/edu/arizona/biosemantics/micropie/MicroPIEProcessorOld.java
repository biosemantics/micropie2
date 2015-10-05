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
import edu.arizona.biosemantics.micropie.io.CSVClassifiedSentenceWriter;
import edu.arizona.biosemantics.micropie.io.CSVTaxonCharacterMatrixWriter;
import edu.arizona.biosemantics.micropie.io.XMLTextReader;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.nlptool.CompoundSentenceSplitRun;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSplitRun;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;


/**
 * Run the major processes to extract characters from characters
 * @author maojin
 *
 */
public class MicroPIEProcessorOld{
	//System Resources
	private LexicalizedParser lexicalizedParser;
	private SentencePredictor sentencePredictor;
	private SentenceSpliter sentenceSpliter;
	private CSVClassifiedSentenceWriter classifiedSentenceWriter;
	private Map<ILabel, String> categoryLabelCodeMap;
	
	//System Parameters
	private boolean parallelProcessing;
	private int maxThreads;
	
	//Class Members
	private ListeningExecutorService executorService;
	private TaxonCharacterMatrixCreator matrixCreator;
	private CSVTaxonCharacterMatrixWriter matrixWriter;
	
	//Running members?
	//private Map<RawSentence, SentenceMetadata> sentenceMetadataMap;
	private Map<TaxonTextFile, List<MultiClassifiedSentence>> taxonSentencesMap;
	private Map<MultiClassifiedSentence, MultiClassifiedSentence> sentenceClassificationMap;

	@Inject
	public MicroPIEProcessorOld(TaxonCharacterMatrixCreator matrixCreator,
			CSVTaxonCharacterMatrixWriter matrixWriter,
			@Named("parallelProcessing") boolean parallelProcessing,
			@Named("maxThreads") int maxThreads,
			@Named("labelCategoryCodeMap") Map<ILabel, String> categoryLabelCodeMap,
			@Named("TaxonSentencesMap") Map<TaxonTextFile, List<MultiClassifiedSentence>> taxonSentencesMap,
			LexicalizedParser lexicalizedParser,
			SentencePredictor sentencePredictor,
			SentenceSpliter sentenceSpliter,
			CSVClassifiedSentenceWriter classifiedSentenceWriter) {
		this.matrixCreator = matrixCreator;
		this.matrixWriter = matrixWriter;
		this.lexicalizedParser = lexicalizedParser;
		this.sentencePredictor = sentencePredictor;
		this.sentenceSpliter = sentenceSpliter;
		this.classifiedSentenceWriter = classifiedSentenceWriter;
		this.categoryLabelCodeMap = categoryLabelCodeMap;
		
		this.parallelProcessing = parallelProcessing;
		this.maxThreads = maxThreads;
		
		this.taxonSentencesMap = taxonSentencesMap;
		
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

	
	/**
	 * 
	 * @param inputFolder: input folder, including files
	 * @param svmLabelAndCategoryMappingFile
	 * @param predictionsFile: records the predicted characters of the sentences
	 * @param outputMatrixFile: the output matrix file
	 */
	public void processFolder(String inputFolder, String svmLabelAndCategoryMappingFile, String predictionsFile, String outputMatrixFile) {
		try {
			//STEP 1: split sentences
			List<MultiClassifiedSentence> testSentences = this.createSentencesFromFolder(inputFolder);
			
			//STEP 2: predict the classifications of the sentences, i.e., the characters in each sentences
			for (MultiClassifiedSentence testSentence : testSentences) {
				Set<ILabel> prediction = sentencePredictor.predict(testSentence);
				testSentence.setPredictions(prediction);
			}
			
			//output the prediction results
			classifiedSentenceWriter.setCategoryLabelCodeMap(categoryLabelCodeMap);
			classifiedSentenceWriter.setPredictionFile(predictionsFile);
			classifiedSentenceWriter.write(testSentences);
			
			//b = System.currentTimeMillis();
			TaxonCharacterMatrix matrix = matrixCreator.create();
			matrixWriter.setOutputStream(new FileOutputStream(outputMatrixFile, true));
			matrixWriter.write(matrix);
			//e = System.currentTimeMillis();
			//System.out.println("extracting characters from the sentences costs "+(e-b)+" ms");
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
	private List<MultiClassifiedSentence> createSentencesFromFolder(String folder) throws IOException,
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
		
		//split sentences by parallel processing， using Stanford Parser
		List<ListenableFuture<List<String>>> sentenceSplits = new ArrayList<ListenableFuture<List<String>>>(textFiles.size());
		CountDownLatch sentenceSplitLatch = new CountDownLatch(textFiles.size());
		for (TaxonTextFile textFile : textFiles) {
			//build new thread
			SentenceSplitRun splitRun = new SentenceSplitRun(
					textFile.getText(), sentenceSplitLatch, sentenceSpliter);
			ListenableFuture<List<String>> futureResult = executorService.submit(splitRun);
			sentenceSplits.add(futureResult);
		}
		try {
			sentenceSplitLatch.await();
			//System.out.println(" threads run over! ");
		} catch (InterruptedException e) {
			log(LogLevel.ERROR, "Problem with latch", e);
		}

		//int numberOfSentences = getNumberOfSentences(sentenceSplits);
		List<List<ListenableFuture<List<String>>>> subsentenceSplitsPerFile = new LinkedList<List<ListenableFuture<List<String>>>>();

		
		//split each sentences further using ClausIE
		//TODO: fix this problem
		for (int i = 0; i < textFiles.size(); i++) {
			List<String> sentences = sentenceSplits.get(i).get();//sentences for each file
			List<ListenableFuture<List<String>>> subsentenceSplits = new LinkedList<ListenableFuture<List<String>>>();
			
			for (final String sentence : sentences) {
				/**
				 * what's this weird process?
				 * sentence length() <=1? it means that there is only one or zero character in the sentence?
				 * if so, why should it be devided further?
				 * 
				 */
				if (sentence.length() <= 1) {
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

		/*
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
	   */
		
		List<MultiClassifiedSentence> result = new LinkedList<MultiClassifiedSentence>();
		for (int i = 0; i < textFiles.size(); i++) {//each file
			List<ListenableFuture<List<String>>> fileFuture = subsentenceSplitsPerFile.get(i);
			for (int j = 0; j < fileFuture.size(); j++) {//sentences in the file, each file
				List<String> subsentences = fileFuture.get(j).get();// .get();
				for (String subsentence : subsentences) {//subsentences of the sentence
					MultiClassifiedSentence sentence = new MultiClassifiedSentence(subsentence);
					result.add(sentence);
					
					//SentenceMetadata metadata = new SentenceMetadata();
					//metadata.setSourceId(j);
					//metadata.setTaxonTextFile(textFiles.get(i));
					//metadata.setCompoundSplitSentence(subsentences.size() > 1);
					// metadata.setParseResult(textSentenceTransformer.getCachedParseResult(sentence));
					
					//TODO: if taxon file is the key, more memory is needed.
					TaxonTextFile taxon = textFiles.get(i);
					if (!taxonSentencesMap.containsKey(taxon))
						taxonSentencesMap
								.put(taxon, new LinkedList<MultiClassifiedSentence>());
					taxonSentencesMap.get(taxon).add(sentence);
				}
			}
		}
		
		this.executorService.shutdown();

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
