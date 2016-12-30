package edu.arizona.biosemantics.micropie;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.extract.NewTaxonCharacterMatrixCreator;
import edu.arizona.biosemantics.micropie.io.CSVClassifiedSentenceWriter;
import edu.arizona.biosemantics.micropie.io.CSVTaxonCharacterMatrixWriter;
import edu.arizona.biosemantics.micropie.io.xml.XMLNewSchemaTextReader;
import edu.arizona.biosemantics.micropie.io.xml.XMLTextReader;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.NewTaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.nlptool.CompoundSentenceSplitRun;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSplitRun;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;



public class SentenceMatrixGenerator {
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
		public SentenceMatrixGenerator(NewTaxonCharacterMatrixCreator matrixCreator,
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
		public void processFolder(String inputFolder,String predictionFile){
			String[] characterNames ="%G+C,Cell shape,Cell width,Cell length,Cell diameter,Motility,Pigment compounds,Gram stain type,Cell membrane & cell wall components,Cell relationships&aggregations,Cell division pattern & reproduction,External features,Internal features,Lysis Susceptibility,Biofilm formation,Filterability,Colony shape,Colony margin,Colony texture,Colony color,Film test result,Spot test result,Habitat isolated from,Geographic location,Aerophilicity,Pressure preference,pH minimum,pH optimum,pH maximum,Temperature minimum,Temperature optimum,Temperature maximum,NaCl minimum,NaCl optimum,NaCl maximum,Host,Symbiotic relationship,Pathogenic,Disease Caused,Pathogen Target Organ,Haemolytic&haemadsorption properties,Vitamins and Cofactors required for growth,Magnesium requirement for growth,organic compounds used or hydrolyzed,organic compounds NOT used or NOT hydrolyzed,inorganic substances used,inorganic substances NOT used,fermentation substrates used,fermentation substrates NOT used,Fermentation Products,Other metabolic product,Antibiotic sensitivity,Antibiotic resistant,Antibiotic production,tests positive,tests negative".split(",");
			
			List<ILabel> characterLabels = new ArrayList();
			for(String characterName : characterNames){
				ILabel label = categoryNameLabelMap.get(characterName.trim().toLowerCase());
				characterLabels.add(label);
			}
			
			//System.out.println("extractors="+characterLabels.size()+" "+characterNames.size());
			
			classifiedSentenceWriter.setCategoryLabelCodeMap(categoryLabelCodeMap);
			classifiedSentenceWriter.setPredictionFile(predictionFile);
			classifiedSentenceWriter.outputHeader(categoryNameLabelMap,characterNames);
			
			File inputFolderFile = new File(inputFolder);
			File[] inputFiles = inputFolderFile.listFiles();
			Set taxonSet = new LinkedHashSet();
			for (File inputFile : inputFiles) {//process all the files
				TaxonTextFile taxonFile = processFile(inputFile,predictionFile,characterLabels);
				taxonSet.add(taxonFile);
			}
			
		}
		
		/**
		 * 
		 * @param inputFile
		 * @param matrix
		 */
		public TaxonTextFile processFile(File inputFile, String predictionFile, List<ILabel> characterLabels) {
			
			//parse the taxon file information
			TaxonTextFile taxonFile = readTaxonFile(inputFile);
			//STEP 1: split sentences
			List<MultiClassifiedSentence> sentences = sentenceSpliter.createSentencesFromFile(taxonFile);
			
			Map<ILabel, List<String>> labelSentenceMap = new HashMap();
			//STEP 2: predict the classifications of the sentences, i.e., the characters in each sentences
			for (MultiClassifiedSentence testSentence : sentences) {
				Set<ILabel> prediction = sentencePredictor.predict(testSentence);
				testSentence.setPredictions(prediction);
				
				for(ILabel label:prediction){
					List sentList = labelSentenceMap.get(label);
					if(sentList==null){
						sentList = new ArrayList();
						labelSentenceMap.put(label, sentList);
					}
					sentList.add(testSentence.getText());
				}
				
				if(prediction==null||prediction.size()==0){
					ILabel label = null;
					List sentList = labelSentenceMap.get(label);
					if(sentList==null){
						sentList = new ArrayList();
						labelSentenceMap.put(label, sentList);
					}
					sentList.add(testSentence.getText());
				}
			}
			
			if(predictionFile!=null){
				classifiedSentenceWriter.writePredictMatrix(taxonFile, labelSentenceMap, characterLabels);
			}
			
			
			return taxonFile;
		}
		
		
		/**
		 * read a taxonfile
		 * @param inputFile
		 * @return
		 */
		private TaxonTextFile readTaxonFile(File inputFile) {
			
			XMLTextReader textReader = new XMLTextReader();
			textReader.setInputStream(inputFile);
			if(textReader.isNew()){
				textReader = new XMLNewSchemaTextReader();
				textReader.setInputStream(inputFile);
			}
			TaxonTextFile taxonFile = textReader.readFile();
			taxonFile.setTaxon(taxonFile.getGenus()+" "+taxonFile.getSpecies());
			
			
			String text = textReader.read();
			taxonFile.setInputFile(null);
			taxonFile.setText(text);
			taxonFile.setXmlFile(inputFile.getName());
			
			return taxonFile;
		}

		
		
		
		public static void main(String[] args){
			// TODO Auto-generated method stub
			Config config = new Config();
			String prjInputFolder = "F:/MicroPIE/micropieInput";
			String prjOutputFolder ="F:/MicroPIE/micropieInput/output";
			config.setInputDirectory(prjInputFolder);
			config.setOutputDirectory(prjOutputFolder);
			
			Injector injector = Guice.createInjector(config);
			
			//String inputFolder = "F:\\MicroPIE\\micropieInput\\input";
			String inputFolder = "F:\\MicroPIE\\datasets\\Part One 112";
			//String inputFolder ="F:\\MicroPIE\\ext\\sample1";
			//String svmLabelAndCategoryMappingFile = injector.getInstance(Key.get(String.class,  Names.named("svmLabelAndCategoryMappingFile")));
			String predictionsFile = "F:\\MicroPIE\\ext\\goldtest\\gold_matrix_Part_One_112_sentence.csv";
			
			//MicroPIEProcessorOld microPIEProcessor = injector.getInstance(MicroPIEProcessorOld.class);
			
			SentenceMatrixGenerator sentMatrixGenerator = injector.getInstance(SentenceMatrixGenerator.class);
			sentMatrixGenerator.processFolder(inputFolder,predictionsFile);
		}
}
