package edu.arizona.biosemantics.micropie;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.classify.MultiSVMClassifier;
import edu.arizona.biosemantics.micropie.classify.SVMClassifier;
import edu.arizona.biosemantics.micropie.io.CSVAbbreviationReader;
import edu.arizona.biosemantics.micropie.io.CSVClassifiedSentenceWriter;
import edu.arizona.biosemantics.micropie.io.CSVTaxonCharacterMatrixWriter;
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.io.XMLTextReader;
import edu.arizona.biosemantics.micropie.model.ClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.ParseResult;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.transform.ITextSentenceTransformer;
import edu.arizona.biosemantics.micropie.transform.ITextTransformer;
import edu.arizona.biosemantics.micropie.transform.MyTaxonCharacterMatrixCreator;
import edu.arizona.biosemantics.micropie.transform.MyTextSentenceTransformer;
import edu.arizona.biosemantics.micropie.transform.SeperatorTokenizer;
import edu.arizona.biosemantics.micropie.transform.TextNormalizer;
import edu.arizona.biosemantics.micropie.transform.feature.IFilterDecorator;
import edu.arizona.biosemantics.micropie.transform.feature.MyFilterDecorator;

public class Main {

	public static void main(String[] args) throws Exception {		
		//setup classifier
		//TODO add "feature scaling"
		IFilterDecorator filterDecorator = new MyFilterDecorator(1, 1, 1);
		MultiSVMClassifier classifier = new MultiSVMClassifier(Label.values(), filterDecorator);
		
		//train classifier
		CSVSentenceReader reader = new CSVSentenceReader(new SeperatorTokenizer(","));
		// reader.setInputStream(new FileInputStream("131001-sampleCombinedSentencesList-csv-CB-manipulated-by-EW-131030-test-3.csv"));
		reader.setInputStream(new FileInputStream("131001-sampleCombinedSentencesList-csv-CB-manipulated-by-EW-131030-test-3-copy-2.csv"));

		List<Sentence> trainingSentences = reader.read();
				
		classifier.train(trainingSentences);
		
		//read test sentences		
		List<Sentence> testSentences = new LinkedList<Sentence>();
		Map<Sentence, SentenceMetadata> sentenceMetadata = new HashMap<Sentence, SentenceMetadata>();
		Map<String, List<Sentence>> taxonSentencesMap = new HashMap<String, List<Sentence>>();
		
		File inputFolder = new File("new-microbe-xml");
		CSVAbbreviationReader abbreviationReader = new CSVAbbreviationReader(new SeperatorTokenizer(","));
		abbreviationReader.setInputStream(new FileInputStream("abbrevlist.csv"));
		LinkedHashMap<String, String> abbreviations = abbreviationReader.read();
		ITextTransformer textNormalizer = new TextNormalizer(abbreviations);
		XMLTextReader textReader = new XMLTextReader();
		MyTextSentenceTransformer textSentenceTransformer = new MyTextSentenceTransformer();
		//TODO parallelize here
		for(File inputFile : inputFolder.listFiles()) {
			textReader.setInputStream(new FileInputStream(inputFile));
			String taxon = textReader.getTaxon();
			String text = textReader.read();
			text = textNormalizer.transform(text);
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
	
		//classify test sentences
		//TODO parallelize here
		Map<Sentence, ClassifiedSentence> sentenceClassificationMap = new HashMap<Sentence, ClassifiedSentence>();
		List<ClassifiedSentence> predictionResult = new LinkedList<ClassifiedSentence>();
		for(Sentence testSentence : testSentences) {
			Set<ILabel> predictions = classifier.getClassification(testSentence);
			ClassifiedSentence classifiedSentence = new ClassifiedSentence(testSentence, predictions);
			sentenceClassificationMap.put(testSentence, classifiedSentence);
			predictionResult.add(classifiedSentence);
		}
		
		//output resulting classified sentences
		CSVClassifiedSentenceWriter classifiedSentenceWriter = new CSVClassifiedSentenceWriter(",");
		classifiedSentenceWriter.setOutputStream(new FileOutputStream("predictions.csv"));
		classifiedSentenceWriter.write(predictionResult);
		
		//create matrix
		MyTaxonCharacterMatrixCreator matrixCreator = new MyTaxonCharacterMatrixCreator();
		TaxonCharacterMatrix matrix = matrixCreator.create(taxonSentencesMap, sentenceMetadata, sentenceClassificationMap);
		
		//output matrix
		CSVTaxonCharacterMatrixWriter matrixWriter = new CSVTaxonCharacterMatrixWriter(",");
		matrixWriter.setOutputStream(new FileOutputStream("matrix.csv"));
		matrixWriter.write(matrix);
	}

}
