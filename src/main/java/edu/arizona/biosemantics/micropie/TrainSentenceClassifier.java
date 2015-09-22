package edu.arizona.biosemantics.micropie;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.classify.BinaryLabel;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.classify.MultiSVMClassifier;
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.io.CategoryReader;
import edu.arizona.biosemantics.micropie.io.WekaModelCaller;
import edu.arizona.biosemantics.micropie.model.RawSentence;

/**
 * Train the sample sentences that are annotated with the categories/characters, and store the trained models
 * Each class is modeled and labeled.
 * 
 * @author maojin
 *
 */
public class TrainSentenceClassifier{
	//private CSVSentenceReader trainingSentenceReader;
	private MultiSVMClassifier multiSVMClassifier;
	//private String trainedModelFolder;
	private String svmLabelAndCategoryMappingFile;
	
	@Inject
	public TrainSentenceClassifier(
			@Named("svmLabelAndCategoryMappingFile") String svmLabelAndCategoryMappingFile,
			@Named("trainedModelFile") String trainedModelFile,
			//CSVSentenceReader trainingSentenceReader,
			MultiSVMClassifier multiSVMClassifier){
		this.svmLabelAndCategoryMappingFile = svmLabelAndCategoryMappingFile;
		//this.trainedModelFolder = trainedModelFile;
		//this.trainingSentenceReader = trainingSentenceReader; 
		this.multiSVMClassifier = multiSVMClassifier;
	}

	/**
	 * train the given folders
	 * @param trainingFile
	 */
	public void train(String trainingFile, String trainedModelFolder, List<ILabel> labels) {
		long startTime = System.currentTimeMillis();
		try {
			CategoryReader cateReader = new CategoryReader();
			cateReader.setCategoryFile(svmLabelAndCategoryMappingFile);
			cateReader.read();
			Map categoryCodeLabelMap = cateReader.getCategoryCodeLabelMap();
			
			CSVSentenceReader trainingSentenceReader = new CSVSentenceReader();
			trainingSentenceReader.setCategoryCodeLabelMap(categoryCodeLabelMap);
			
			trainingSentenceReader.setInputStream(new FileInputStream(trainingFile));
			
			List<RawSentence> trainingSentences = trainingSentenceReader.readTwoColumnSentenceList();
			
			System.out.println("trainingSentences.size()::" + trainingSentences.size());
			
			multiSVMClassifier.setLabels(labels);
			multiSVMClassifier.train(trainingSentences);
			
			//save the trained files
			WekaModelCaller wmc = new WekaModelCaller();
			wmc.saveModel(multiSVMClassifier, trainedModelFolder);
		} catch (Exception e) {
			log(LogLevel.ERROR, "Could not run Main", e);
		}

		System.out.println("DONE: " + ((long) System.currentTimeMillis() - startTime) + " ms");
		
	}
	
	
	
	/**
	 * test the true positive measure of the model
	 */
	public void testTruePositive(String testFile, String trainedModelFolder, List<ILabel> labels){
		try {
			CategoryReader cateReader = new CategoryReader();
			cateReader.setCategoryFile(svmLabelAndCategoryMappingFile);
			cateReader.read();
			Map categoryCodeLabelMap = cateReader.getCategoryCodeLabelMap();
			
			CSVSentenceReader trainingSentenceReader = new CSVSentenceReader();
			trainingSentenceReader.setCategoryCodeLabelMap(categoryCodeLabelMap);
			trainingSentenceReader.setInputStream(new FileInputStream(testFile));
			
			List<RawSentence> trainingSentences = trainingSentenceReader.readTwoColumnSentenceList();
			System.out.println("trainingSentences.size()::" + trainingSentences.size());
			
			multiSVMClassifier.setLabels(labels);
			multiSVMClassifier.loadClassifier(trainedModelFolder);
			
			float[] positive = new float[59];
			float[] truePositive = new float[59];
			//float[] positive = new float[Label.values().length];
			//float[] truePositive = new float[Label.values().length];
			
			for(RawSentence sentence: trainingSentences){
				RawSentence result = (RawSentence)sentence.clone();
				ILabel markedLabel = result.getLabel();
				int labelInt = Integer.parseInt(markedLabel.getValue());
				positive[labelInt]++;
				//get the classifiers of the label
				ILabel prediction;
				try {
					prediction = multiSVMClassifier.getClassifiers().get(markedLabel).getClassification(result);
					if(prediction.equals(BinaryLabel.YES))
						truePositive[labelInt]++;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			
			for(int i=0;i<positive.length;i++){
				double precision = truePositive[i] / positive[i];
				System.out.println("label "+i+" "+precision);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * test the true positive measure of the model
	 */
	public void testTrueNegative(String testFile, String trainedModelFolder, List<ILabel> labels){
		try {
			//TODO: as the initialized inputs
			CategoryReader cateReader = new CategoryReader();
			cateReader.setCategoryFile(svmLabelAndCategoryMappingFile);
			cateReader.read();
			Map categoryCodeLabelMap = cateReader.getCategoryCodeLabelMap();
			
			CSVSentenceReader trainingSentenceReader = new CSVSentenceReader();
			trainingSentenceReader.setCategoryCodeLabelMap(categoryCodeLabelMap);
			trainingSentenceReader.setInputStream(new FileInputStream(testFile));
			
			
			List<RawSentence> trainingSentences = trainingSentenceReader.readTwoColumnSentenceList();
			System.out.println("trainingSentences.size()::" + trainingSentences.size());
			
			multiSVMClassifier.setLabels(labels);
			multiSVMClassifier.loadClassifier(trainedModelFolder);
			
			float[] negative = new float[59];
			float[] trueNegative = new float[59];
			//float[] positive = new float[Label.values().length];
			//float[] truePositive = new float[Label.values().length];
			
			for(ILabel label: labels){
				int labelInt = Integer.parseInt(label.getValue());
				for(RawSentence sentence: trainingSentences){
					RawSentence result = (RawSentence)sentence.clone();
					ILabel markedLabel = result.getLabel();
					if(markedLabel==label) continue;//positive samples will not be considered.
					
					negative[labelInt]++;
					//get the classifiers of the label
					try {
						ILabel prediction = multiSVMClassifier.getClassifiers().get(label).getClassification(result);
						if(prediction.equals(BinaryLabel.NO)) trueNegative[labelInt]++;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			
			for(int i=0;i<negative.length;i++){
				double tn = trueNegative[i] / negative[i];
				System.out.println("label "+i+" "+tn);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
