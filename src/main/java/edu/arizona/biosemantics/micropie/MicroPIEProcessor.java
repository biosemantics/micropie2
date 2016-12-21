package edu.arizona.biosemantics.micropie;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import com.google.inject.name.Names;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.extract.NewTaxonCharacterMatrixCreator;
import edu.arizona.biosemantics.micropie.extract.TaxonCharacterMatrixCreator;
import edu.arizona.biosemantics.micropie.io.CSVClassifiedSentenceWriter;
import edu.arizona.biosemantics.micropie.io.CSVTaxonCharacterMatrixWriter;
import edu.arizona.biosemantics.micropie.io.MarkupXMLWriter;
import edu.arizona.biosemantics.micropie.io.XMLNewSchemaTextReader;
import edu.arizona.biosemantics.micropie.io.XMLTextReader;
import edu.arizona.biosemantics.micropie.model.NewTaxonCharacterMatrix;
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
public class MicroPIEProcessor{
	//System Resources
	private LexicalizedParser lexicalizedParser;
	private SentencePredictor sentencePredictor;
	private CategoryPredictor categoryPredictor;
	private SentenceSpliter sentenceSpliter;
	private CSVClassifiedSentenceWriter classifiedSentenceWriter;
	
	private Map<ILabel, String> categoryLabelCodeMap;
	private Map<ILabel, String> labelCategoryNameMap;
	private Map<String, ILabel> categoryNameLabelMap;
	
	//System Parameters
	private boolean parallelProcessing;
	private int maxThreads;
	private LinkedHashSet<String> characterNames;
	private LinkedHashSet<String> outputCharacterNames;
	//Class Members
	private ListeningExecutorService executorService;
	private NewTaxonCharacterMatrixCreator matrixCreator;
	private CSVTaxonCharacterMatrixWriter matrixWriter;
	
	//Running members?
	private Map<TaxonTextFile, List<MultiClassifiedSentence>> taxonSentencesMap;

	@Inject
	public MicroPIEProcessor(NewTaxonCharacterMatrixCreator matrixCreator,
			CSVTaxonCharacterMatrixWriter matrixWriter,
			@Named("parallelProcessing") boolean parallelProcessing,
			@Named("maxThreads") int maxThreads,
			@Named("Characters") LinkedHashSet<String> characterNames,
			@Named("OutputCharacters") LinkedHashSet<String> outputCharacterNames,//OutputCharacters
			@Named("categoryNameLabelMap") Map<String, ILabel> categoryNameLabelMap,
			@Named("labelCategoryCodeMap") Map<ILabel, String> categoryLabelCodeMap,
			@Named("labelCategoryNameMap")  Map<ILabel, String> labelCategoryNameMap,
			@Named("TaxonSentencesMap") Map<TaxonTextFile, List<MultiClassifiedSentence>> taxonSentencesMap,
			LexicalizedParser lexicalizedParser,
			SentencePredictor sentencePredictor,
			CategoryPredictor categoryPredictor,
			SentenceSpliter sentenceSpliter,
			CSVClassifiedSentenceWriter classifiedSentenceWriter) {
		this.matrixCreator = matrixCreator;
		this.matrixWriter = matrixWriter;
		this.lexicalizedParser = lexicalizedParser;
		this.sentencePredictor = sentencePredictor;
		this.categoryPredictor = categoryPredictor;
		this.sentenceSpliter = sentenceSpliter;
		this.classifiedSentenceWriter = classifiedSentenceWriter;
		
		this.parallelProcessing = parallelProcessing;
		this.maxThreads = maxThreads;
		this.characterNames = characterNames;
		this.outputCharacterNames = outputCharacterNames;
		this.categoryNameLabelMap = categoryNameLabelMap;
		this.categoryLabelCodeMap = categoryLabelCodeMap;
		this.labelCategoryNameMap = labelCategoryNameMap;
		
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
	 * @param inputFolder
	 * @param predictionFile if it's null, donot printout
	 * @param outputMatrixFile
	 */
	public void processFolder(String inputFolder,String predictionFile,String outputMatrixFile, int outputFormat, boolean isValueInference){
		LinkedHashSet<ILabel> characterLabels = new LinkedHashSet();
		for(String characterName : characterNames){
			ILabel label = categoryNameLabelMap.get(characterName.trim().toLowerCase());
			characterLabels.add(label);
			//System.out.println(characterName.trim().toLowerCase()+" "+label);
		}
		
		LinkedHashSet<ILabel> outputCharacterLabels = new LinkedHashSet();
		for(String characterName : outputCharacterNames){
			ILabel label = categoryNameLabelMap.get(characterName.trim().toLowerCase());
			outputCharacterLabels.add(label);
			//System.out.println(characterName.trim().toLowerCase()+" "+label);
		}
		
		//System.out.println("extractors="+characterLabels.size()+" "+characterNames.size());
		
		// <Taxon, <Character, List<Value>>>
		NewTaxonCharacterMatrix matrix = new NewTaxonCharacterMatrix();
		matrix.setCharacterLabels(characterLabels);
		matrix.setCharacterNames(characterNames);
		
		if(predictionFile!=null){
			classifiedSentenceWriter.setCategoryLabelCodeMap(categoryLabelCodeMap);
			classifiedSentenceWriter.setPredictionFile(predictionFile);
		}
		
		matrixCreator.setCharacterLabels(characterLabels);
		matrixCreator.setCharacterNames(characterNames);
		matrixCreator.setJudgeUSPForPTN(isValueInference);
		
		File inputFolderFile = new File(inputFolder);
		File[] inputFiles = inputFolderFile.listFiles();
		Set taxonSet = new LinkedHashSet();
		for (File inputFile : inputFiles) {//process all the files
			TaxonTextFile taxonFile = processFile(inputFile,predictionFile, matrix);
			taxonSet.add(taxonFile);
		}
		matrix.setTaxonFiles(taxonSet);
		
		try {
			
			matrix.propagateGenus();
			switch(outputFormat){
				case 1://output the format of MatrixConverter csv
					matrixWriter.setOutputStream(new FileOutputStream(outputMatrixFile, true));
					matrixWriter.writeMatrixConverter(matrix, labelCategoryNameMap,outputCharacterLabels,false);break;
				case 2://output Marked up XML files
					String outputFolder = outputMatrixFile;
					Set taxonFiles = matrix.getTaxonFiles();
					//taxonFile.setInputFile(inputFile);
					MarkupXMLWriter markupXMLWriter = new MarkupXMLWriter();
					markupXMLWriter.setInputFolder(inputFolder);
					String xmloutputFolder = outputMatrixFile.replace("matrix.csv","");
					markupXMLWriter.setOutputFolder(xmloutputFolder);
					markupXMLWriter.setTaxonFiles(taxonFiles);
					markupXMLWriter.setLabelCategoryNameMap(labelCategoryNameMap);
					markupXMLWriter.generateOutputXML();
					break;
				default://output default csv
					matrixWriter.setOutputStream(new FileOutputStream(outputMatrixFile, true));
					matrixWriter.write(matrix, labelCategoryNameMap,outputCharacterLabels,false);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			try {
				matrixWriter.setOutputStream(new FileOutputStream(outputMatrixFile+new Random().nextInt(), true));
				matrix.propagateGenus();
				switch(outputFormat){
				case 1: matrixWriter.writeMatrixConverter(matrix, labelCategoryNameMap,outputCharacterLabels,false);break;
				default:
					matrixWriter.write(matrix, labelCategoryNameMap,outputCharacterLabels,false);
			}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}catch (Exception e1) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param inputFile
	 * @param matrix
	 */
	public TaxonTextFile processFile(File inputFile, String predictionFile, NewTaxonCharacterMatrix matrix) {
		
		//parse the taxon file information
		//System.out.println("read from taxon file");
		TaxonTextFile taxonFile = readTaxonFile(inputFile);
		//STEP 1: split sentences
		List<MultiClassifiedSentence> sentences = sentenceSpliter.createSentencesFromFile(taxonFile);
		taxonFile.setSentences(sentences);
		taxonFile.setSentCharacterValues(new HashMap());
		
		//STEP 2: predict the classifications of the sentences, i.e., the characters in each sentences
		
		for (MultiClassifiedSentence testSentence : sentences) {
			Set<ILabel> prediction = sentencePredictor.predict(testSentence);
			Set<ILabel> categories = categoryPredictor.predict(testSentence);
			testSentence.setPredictions(prediction);
			testSentence.setCategories(categories);
			//testSentence.setPredictions(new HashSet());
			//System.out.println("prediction="+prediction+" categories="+categories);
			//System.out.println("prediction="+testSentence.getPredictions()+" categories="+testSentence.getCategories());
		}
		/* */
		if(predictionFile!=null){
			classifiedSentenceWriter.write(sentences);
		}
		
		//System.out.println("extract values and create matrix");
		matrixCreator.create(matrix,taxonFile,sentences);
		
		return taxonFile;
	}
	
	
	/**
	 * read a taxonfile
	 * @param inputFile
	 * @return
	 */
	private TaxonTextFile readTaxonFile(File inputFile) {

		XMLTextReader textReader = new XMLTextReader();
		//System.out.println(inputFile);
		textReader.setInputStream(inputFile);
		if(textReader.isNew()){
			textReader = new XMLNewSchemaTextReader();
			textReader.setInputStream(inputFile);
		}
		TaxonTextFile taxonFile = textReader.readFile();
		if(taxonFile.getSpecies()==null){
			taxonFile.setTaxon(taxonFile.getGenus());
		}else{
			taxonFile.setTaxon(taxonFile.getGenus()+" "+taxonFile.getSpecies());
		}
		
		
		
		String text = textReader.read();
		taxonFile.setInputFile(inputFile);
		taxonFile.setText(text);
		taxonFile.setXmlFile(inputFile.getName());
		
		return taxonFile;
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
					
					SentenceMetadata metadata = new SentenceMetadata();
					metadata.setSourceId(j);
					metadata.setTaxonTextFile(textFiles.get(i));
					metadata.setCompoundSplitSentence(subsentences.size() > 1);
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
		
		executorService.shutdown();

		log(LogLevel.INFO, "Done reading test sentences...");
		return result;
	}
}
