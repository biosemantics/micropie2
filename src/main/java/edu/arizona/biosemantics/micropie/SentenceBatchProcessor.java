package edu.arizona.biosemantics.micropie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.ICharacterValueExtractor;
import edu.arizona.biosemantics.micropie.extract.ICharacterValueExtractorProvider;
import edu.arizona.biosemantics.micropie.extract.TaxonCharacterMatrixCreator;
import edu.arizona.biosemantics.micropie.io.CSVClassifiedSentenceWriter;
import edu.arizona.biosemantics.micropie.io.CSVExtValueWriter;
import edu.arizona.biosemantics.micropie.io.CSVTaxonCharacterMatrixWriter;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSplitRun;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

/**
 * Batch Process sentences in lines
 * Used in local experiments
 * 
 * @author maojin
 *
 */
public class SentenceBatchProcessor {
	private SentencePredictor sentencePredictor;
	private SentenceSpliter sentenceSpliter;
	private ListeningExecutorService executorService;
	private CSVClassifiedSentenceWriter classifiedSentenceWriter;
	private CSVTaxonCharacterMatrixWriter matrixWriter;
	private TaxonCharacterMatrixCreator matrixCreator;
	private ICharacterValueExtractorProvider contentExtractorProvider;// extractors
	
	private Map<ILabel, String> categoryLabelCodeMap;
	
	private Map sentenceMetadataMap;
	private Map<RawSentence, MultiClassifiedSentence> sentenceClassificationMap;
	
	
	@Inject
	public SentenceBatchProcessor(TaxonCharacterMatrixCreator matrixCreator,
			CSVTaxonCharacterMatrixWriter matrixWriter,
			@Named("parallelProcessing") boolean parallelProcessing,
			@Named("maxThreads") int maxThreads,
			@Named("labelCategoryCodeMap") Map<ILabel, String> categoryLabelCodeMap,
			@Named("SentenceMetadataMap") Map<RawSentence, SentenceMetadata> sentenceMetadataMap,
			//@Named("TaxonSentencesMap") Map<TaxonTextFile, List<RawSentence>> taxonSentencesMap,
			@Named("SentenceClassificationMap") Map<RawSentence, MultiClassifiedSentence> sentenceClassificationMap,
			SentencePredictor sentencePredictor,
			SentenceSpliter sentenceSpliter,
			CSVClassifiedSentenceWriter classifiedSentenceWriter,
			ICharacterValueExtractorProvider contentExtractorProvider) {
		this.matrixCreator = matrixCreator;
		this.matrixWriter = matrixWriter;
		this.sentencePredictor = sentencePredictor;
		this.sentenceSpliter = sentenceSpliter;
		this.classifiedSentenceWriter = classifiedSentenceWriter;
		this.contentExtractorProvider = contentExtractorProvider;
		this.categoryLabelCodeMap = categoryLabelCodeMap;
		
		this.sentenceMetadataMap = sentenceMetadataMap;
		//this.taxonSentencesMap = taxonSentencesMap;
		this.sentenceClassificationMap = sentenceClassificationMap;
		
		if (!parallelProcessing)
			executorService = MoreExecutors.listeningDecorator(Executors
					.newSingleThreadExecutor());
		if (parallelProcessing && maxThreads < Integer.MAX_VALUE)
			executorService = MoreExecutors.listeningDecorator(Executors
					.newFixedThreadPool(maxThreads));
		if (parallelProcessing && maxThreads == Integer.MAX_VALUE)
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
	public void processLineFile(String lineFile, String predictionsFile, String outputMatrixFile) {
		//STEP 1: split sentences
		List<RawSentence> testSentences = this.createSentencesByLine(lineFile);
		
		//STEP 2: predict the classifications of the sentences, i.e., the characters in each sentences
		List<MultiClassifiedSentence> predictions = new LinkedList<MultiClassifiedSentence>();
		
		Map<MultiClassifiedSentence, HashMap> chaMap = new LinkedHashMap();
		for (RawSentence testSentence : testSentences) {
			Set<ILabel> prediction = sentencePredictor.predict(testSentence);
			MultiClassifiedSentence classifiedSentence = new MultiClassifiedSentence(testSentence, prediction);
			sentenceClassificationMap.put(testSentence,classifiedSentence);
			predictions.add(classifiedSentence);
			
			chaMap.put(classifiedSentence, new HashMap());
			
			if (prediction.size() == 0 ) {//it can be any character
				Label[] labelList = Label.values();
				for ( int i = 0; i < labelList.length; i++ ) {
					prediction.add(labelList[i]);
				}
			}
			
			// Reference: 
			// get the character extractors for this sentence
			Set<ICharacterValueExtractor> extractors = new HashSet<ICharacterValueExtractor>();
			for(ILabel label : prediction) {//get all the extractors ready
				if(label instanceof Label) {
					extractors.addAll(contentExtractorProvider.getContentExtractor((Label)label));
				}
			}
			
			//call the extractors one by one
			for(ICharacterValueExtractor extractor : extractors) {
				String character = extractor.getCharacterName();
				
				List<CharacterValue> content = extractor.getCharacterValue(classifiedSentence);
				System.out.println("current doing:character "+character+" by ["+extractor.getClass().getName()+"]");
				chaMap.get(classifiedSentence).put(character, content);
			}
		}
		
		//out put the prediction results
		classifiedSentenceWriter.setCategoryLabelCodeMap(categoryLabelCodeMap);
		classifiedSentenceWriter.setPredictionFile(predictionsFile);
		classifiedSentenceWriter.write(predictions);
		
		CSVExtValueWriter extValueWriter = new CSVExtValueWriter();
		extValueWriter.write(chaMap, outputMatrixFile);
	}
	
	/**
	 * create sentences from the test folders
	 * @param folder
	 */
	public List<RawSentence> createSentencesByLine(String lineFile){
		
		//read and parse all the files in the folder
		File inputFile = new File(lineFile);
		List<String> lineStr = new ArrayList();
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
			String line = null;
			
			while((line = br.readLine())!=null) {
				String text = line.trim();
				lineStr.add(text);
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//split sentences by parallel processing， using Stanford Parser
		List<ListenableFuture<List<String>>> sentenceSplits = new ArrayList<ListenableFuture<List<String>>>(lineStr.size());
		CountDownLatch sentenceSplitLatch = new CountDownLatch(lineStr.size());
		for (String text : lineStr) {
			//build new thread
			SentenceSplitRun splitRun = new SentenceSplitRun(text, sentenceSplitLatch, sentenceSpliter);
			ListenableFuture<List<String>> futureResult = executorService.submit(splitRun);
			sentenceSplits.add(futureResult);
		}
		try {
			sentenceSplitLatch.await();
		} catch (InterruptedException e) {
			
		}
		//int numberOfSentences = getNumberOfSentences(sentenceSplits);
		List<RawSentence> result = new LinkedList<RawSentence>();
		for (int i = 0; i < lineStr.size(); i++) {
			List<String> sentences = null;//sentences for each file
			try {
				sentences = sentenceSplits.get(i).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			int j=0;
			for (String subsentence : sentences) {//subsentences of the sentence
				RawSentence sentence = new RawSentence(subsentence);
				result.add(sentence);
				SentenceMetadata metadata = new SentenceMetadata();
				metadata.setSourceId(j++);
				metadata.setTaxon(i+"");
				// metadata.setParseResult(textSentenceTransformer.getCachedParseResult(sentence));
				sentenceMetadataMap.put(sentence, metadata);
			}
		}
		
		//how to shutdown??
		//if(executorService.isTerminated()||executorService.){
			executorService.shutdown();
		//}
		return result;
	}
	
}
