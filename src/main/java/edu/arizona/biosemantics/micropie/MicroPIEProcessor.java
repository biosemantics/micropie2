package edu.arizona.biosemantics.micropie;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
import edu.arizona.biosemantics.micropie.io.XMLNewSchemaTextReader;
import edu.arizona.biosemantics.micropie.io.XMLTextReader;
import edu.arizona.biosemantics.micropie.model.NewTaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
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
	public void processFolder(String inputFolder,String predictionFile,String outputMatrixFile){
		
		long start = System.currentTimeMillis();
		LinkedHashSet<ILabel> characterLabels = new LinkedHashSet();
		for(String characterName : characterNames){
			ILabel label = categoryNameLabelMap.get(characterName.trim().toLowerCase());
			characterLabels.add(label);
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
		
		File inputFolderFile = new File(inputFolder);
		File[] inputFiles = inputFolderFile.listFiles();
		Set taxonSet = new LinkedHashSet();
		for (File inputFile : inputFiles) {//process all the files
			TaxonTextFile taxonFile = processFile(inputFile,predictionFile, matrix);
			taxonSet.add(taxonFile);
		}
		matrix.setTaxonFiles(taxonSet);
		
		try {
			matrixWriter.setOutputStream(new FileOutputStream(outputMatrixFile, true));
			matrixWriter.write(matrix, labelCategoryNameMap,false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			try {
				matrixWriter.setOutputStream(new FileOutputStream(outputMatrixFile+new Random().nextInt(), true));
				matrixWriter.write(matrix, labelCategoryNameMap,false);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}catch (Exception e1) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("It cost "+(end-start)+" ms to process the folder");
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
		//STEP 2: predict the classifications of the sentences, i.e., the characters in each sentences
		
		for (MultiClassifiedSentence testSentence : sentences) {
			Set<ILabel> prediction = sentencePredictor.predict(testSentence);
			Set<ILabel> categories = categoryPredictor.predict(testSentence);
			testSentence.setPredictions(prediction);
			testSentence.setCategories(categories);
			
			//System.out.println("testSentence words="+testSentence.words());
			//testSentence.setPredictions(new HashSet());
			//System.out.println("prediction="+prediction+" categories="+categories);
			//System.out.println("prediction="+testSentence.getPredictions()+" categories="+testSentence.getCategories());
		}
		/*
		if(predictionFile!=null){
			classifiedSentenceWriter.write(sentences);
		}*/
		
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
		taxonFile.setInputFile(null);
		taxonFile.setText(text);
		taxonFile.setXmlFile(inputFile.getName());
		
		return taxonFile;
	}




}
