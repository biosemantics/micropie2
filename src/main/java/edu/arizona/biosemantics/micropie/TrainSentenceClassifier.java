package edu.arizona.biosemantics.micropie;

import java.io.FileInputStream;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.classify.MultiSVMClassifier;
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.io.WekaModelCaller;
import edu.arizona.biosemantics.micropie.model.Sentence;

/**
 * Train the sample sentences that are annotated with the categories/characters, and store the trained models
 * Each class is modeled and labelled.
 * @author maojin
 *
 */
public class TrainSentenceClassifier implements IRun{
	private CSVSentenceReader trainingSentenceReader;
	private MultiSVMClassifier classifier;
	private String trainingFile;
	private String trainedModelFile;
	private String svmLabelAndCategoryMappingFile;
	
	@Inject
	public TrainSentenceClassifier(
			@Named("trainingFile") String trainingFile,
			@Named("svmLabelAndCategoryMappingFile") String svmLabelAndCategoryMappingFile,
			@Named("trainedModelFile") String trainedModelFile,
			CSVSentenceReader trainingSentenceReader,
			MultiSVMClassifier classifier){
		this.trainingFile = trainingFile;
		this.svmLabelAndCategoryMappingFile = svmLabelAndCategoryMappingFile;
		this.trainedModelFile = trainedModelFile;
		this.trainingSentenceReader = trainingSentenceReader; 
		this.classifier = classifier;
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		try {
			trainingSentenceReader.setInputStream(new FileInputStream(trainingFile));
			trainingSentenceReader.setInputStream2(new FileInputStream(svmLabelAndCategoryMappingFile));
			trainingSentenceReader.readSVMLabelAndCategoryMapping();
			List<Sentence> trainingSentences = trainingSentenceReader.read();
			
			System.out.println("trainingFile::" + trainingFile);
			System.out.println("svmLabelAndCategoryMappingFile::" + svmLabelAndCategoryMappingFile);
			System.out.println("trainingFile::" + trainingFile);
			System.out.println("trainingSentences.size()::" + trainingSentences.size());
			
			classifier.train(trainingSentences);
			
			//save the trained files
			WekaModelCaller wmc = new WekaModelCaller();
			wmc.saveModel(classifier, this.trainedModelFile);
		} catch (Exception e) {
			log(LogLevel.ERROR, "Could not run Main", e);
		}

		System.out.println("DONE: " + ((long) System.currentTimeMillis() - startTime) + " ms");
		
	}

}
