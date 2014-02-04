package edu.arizona.biosemantics.micropie;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import edu.arizona.biosemantics.micropie.transform.ITextTransformer;
import edu.arizona.biosemantics.micropie.transform.MyTaxonCharacterMatrixCreator;
import edu.arizona.biosemantics.micropie.transform.MyTextSentenceTransformer;
import edu.arizona.biosemantics.micropie.transform.TextNormalizer;
import edu.arizona.biosemantics.micropie.transform.feature.IFilterDecorator;
import edu.arizona.biosemantics.micropie.transform.feature.MyFilterDecorator;

public class Main {

	private Map<Sentence, ClassifiedSentence> sentenceClassificationMap;
	private Map<Sentence, SentenceMetadata> sentenceMetadata;
	private Map<String, List<Sentence>> taxonSentencesMap;
	
	public void run() {
		sentenceClassificationMap = new HashMap<Sentence, ClassifiedSentence>();
		sentenceMetadata = new HashMap<Sentence, SentenceMetadata>();
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
	
	private List<Sentence> createTestSentences() throws Exception {
		log(LogLevel.INFO, "Reading test sentences...");
		List<Sentence> testSentences = new LinkedList<Sentence>();
		File inputFolder = new File("new-microbe-xml");
		CSVAbbreviationReader abbreviationReader = new CSVAbbreviationReader();
		abbreviationReader.setInputStream(new FileInputStream("abbrevlist.csv"));
		LinkedHashMap<String, String> abbreviations = abbreviationReader.read();
		ITextTransformer textNormalizer = new TextNormalizer(abbreviations);
		XMLTextReader textReader = new XMLTextReader();
		MyTextSentenceTransformer textSentenceTransformer = new MyTextSentenceTransformer();
		//TODO parallelize here
		for(File inputFile : inputFolder.listFiles()) {
			log(LogLevel.INFO, "Reading from " + inputFile.getName() + "...");
			textReader.setInputStream(new FileInputStream(inputFile));
			String taxon = textReader.getTaxon();
			log(LogLevel.INFO, "Taxon: " + taxon);
			String text = textReader.read();
			log(LogLevel.INFO, "Text: " + text);
			text = textNormalizer.transform(text);
			log(LogLevel.INFO, "Normalized text: " + text);
			List<Sentence> sentences = textSentenceTransformer.transform(textReader.read());
			for(int i=0; i<sentences.size(); i++) {
				Sentence sentence = sentences.get(i);
				SentenceMetadata metadata = new SentenceMetadata();
				metadata.setSourceFile(inputFile.getName());
				metadata.setSourceId(i);
				metadata.setTaxon(taxon);
				metadata.setCompoundSplitSentence(sentences.size() > 1);
				metadata.setParseResult(textSentenceTransformer.getCachedParseResult(sentence));
				sentenceMetadata.put(sentence, metadata);
				if(!taxonSentencesMap.containsKey(taxon))
					taxonSentencesMap.put(taxon, new LinkedList<Sentence>());
				taxonSentencesMap.get(taxon).add(sentence);
			}
			testSentences.addAll(sentences);
		}
		log(LogLevel.INFO, "Done reading test sentences...");
		return testSentences;
	}
	
	private List<Sentence> createTrainSentences() throws Exception {
		log(LogLevel.INFO, "Reading training data...");
		CSVSentenceReader reader = new CSVSentenceReader();
		// reader.setInputStream(new FileInputStream("131001-sampleCombinedSentencesList-csv-CB-manipulated-by-EW-131030-test-3.csv"));
		reader.setInputStream(new FileInputStream("131001-sampleCombinedSentencesList-csv-CB-manipulated-by-EW-131030-test-3-copy-2.csv"));
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
		String predictionsFile = "predictions.csv";
		log(LogLevel.INFO, "Writing prediciton results to " + predictionsFile + "...");
		CSVClassifiedSentenceWriter classifiedSentenceWriter = new CSVClassifiedSentenceWriter();
		classifiedSentenceWriter.setOutputStream(new FileOutputStream(predictionsFile));
		classifiedSentenceWriter.write(predictions);
		log(LogLevel.INFO, "Done writing prediciton results");
	}
		
	private TaxonCharacterMatrix createMatrix() {
		log(LogLevel.INFO, "Creating matrix...");
		MyTaxonCharacterMatrixCreator matrixCreator = new MyTaxonCharacterMatrixCreator();
		TaxonCharacterMatrix matrix = matrixCreator.create(taxonSentencesMap, sentenceMetadata, sentenceClassificationMap);
		log(LogLevel.INFO, "Done creating matrix");
		return matrix;
	}

	private void writeMatrix(TaxonCharacterMatrix matrix) throws Exception {
		String matrixFile = "matrix.csv";
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
