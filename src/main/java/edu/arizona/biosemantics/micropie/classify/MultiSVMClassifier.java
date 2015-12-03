package edu.arizona.biosemantics.micropie.classify;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.common.log.ObjectStringifier;
import edu.arizona.biosemantics.micropie.io.WekaModelCaller;
import edu.arizona.biosemantics.micropie.model.RawSentence;

//TODO: Make weka log things such as filtered data (e.g. matrix actually passed into SVM)
//TODO: Evaluate this classifier to work correct on small example training/test data
public class MultiSVMClassifier implements IMultiClassifier, ITrainableClassifier {

	private List<ILabel> labels;
	private Map<ILabel, SVMClassifier> classifiers = new HashMap<ILabel, SVMClassifier>();
	protected boolean trained = false;
	private String multiFilterOptions;
	private String libSVMOptions;
	
	@Inject
	public MultiSVMClassifier(@Named("MultiSVMClassifier_Labels") List<ILabel> labels, 
			@Named("MultiFilterOptions")String multiFilterOptions, @Named("LibSVMOptions")String libSVMOptions
			//,
			//@Named("trainedModelFile") String trainedModelFile
			) {
		this.labels = labels;
		this.multiFilterOptions = multiFilterOptions;
		this.libSVMOptions = libSVMOptions;
		
		//this.trainedModelFile = trainedModelFile;
	}
	
	/*
	@Inject
	public MultiSVMClassifier(@Named("MultiFilterOptions")String multiFilterOptions, @Named("LibSVMOptions")String libSVMOptions) {
		this.multiFilterOptions = multiFilterOptions;
		this.libSVMOptions = libSVMOptions;
	}*/
	
	public void setLabels(List<ILabel> labels){
		this.labels = labels;
	}
	
	public List<ILabel> getLabels() {
		return labels;
	}

	
	
	public Map<ILabel, SVMClassifier> getClassifiers() {
		return classifiers;
	}

	public void setClassifiers(Map<ILabel, SVMClassifier> classifiers) {
		this.classifiers = classifiers;
	}

	public boolean isTrained() {
		return trained;
	}

	public void setTrained(boolean trained) {
		this.trained = trained;
	}

	public void setMultiFilterOptions(String multiFilterOptions) {
		this.multiFilterOptions = multiFilterOptions;
	}

	public void setLibSVMOptions(String libSVMOptions) {
		this.libSVMOptions = libSVMOptions;
	}

	@Override
	public Set<ILabel> predict(RawSentence sentence) throws Exception {
		if(!trained)
			throw new Exception("Classifier is not trained");
		if(labels==null)  throw new Exception("Classifier labels are not specified");
		Set<ILabel> result = new HashSet<ILabel>();
		for(ILabel label : labels) {
			SVMClassifier classifier = classifiers.get(label);
			if(classifier!=null){//some character do not have models
				RawSentence twoClassSentence = this.createTwoClassData(label, sentence);
				ILabel prediction = classifier.getClassification(twoClassSentence);
				if(prediction.equals(BinaryLabel.YES))
					result.add(label);
			}
		}
		
		log(LogLevel.INFO, "Prediction for " + sentence.getText() + "\n"
				+ " -> " + ObjectStringifier.getInstance().stringify(result));
		
		// System.out.println("Prediction for " + sentence.getText() + "\n"
		//		+ " -> " + ObjectStringifier.getInstance().stringify(result));
		return result;
	}
	
	
	
	@Override
	/**
	 * train the data
	 * Alert: label list should be specified
	 */
	public void train(List<RawSentence> trainingData) throws Exception {
		log(LogLevel.INFO, "Training classifier...");
		if(labels==null)  throw new Exception("Classifier labels are not specified");
		for(ILabel label : labels) {
			log(LogLevel.INFO, "Training SVM for label " + label.getValue());
			
			SVMClassifier classifier = new SVMClassifier(BinaryLabel.valuesList(), multiFilterOptions, libSVMOptions);
			classifiers.put(label, classifier);
			
			//divide into two classes
			List<RawSentence> twoClassData = createTwoClassData(label, trainingData);
			//System.out.println("Training SVM for label " + label.getValue()+" training sentences:"+twoClassData.size());
			classifier.train(twoClassData);
			//
			
		}
		trained = true;
		log(LogLevel.INFO, "Done training");
	}

	
	/**
	 * train the data
	 * Alert: label list should be specified
	 */
	public void trainAndSave(List<RawSentence> trainingData, String modelPath) throws Exception {
		log(LogLevel.INFO, "Training classifier...");
		if(labels==null)  throw new Exception("Classifier labels are not specified");
		for(ILabel label : labels) {
			log(LogLevel.INFO, "Training SVM for label " + label.getValue());
			System.out.println("Training SVM for label " + label.getValue());
			SVMClassifier svmClassifier = new SVMClassifier(BinaryLabel.valuesList(), multiFilterOptions, libSVMOptions);
			classifiers.put(label, svmClassifier);
			
			//divide into two classes
			List<RawSentence> twoClassData = createTwoClassData(label, trainingData);
			svmClassifier.train(twoClassData);
			//
			//save the trained files
			//WekaModelCaller wmc = new WekaModelCaller();
			WekaModelCaller.saveModel(svmClassifier, label, modelPath);
		}
		trained = true;
		log(LogLevel.INFO, "Done training");
	}
	
	
	private List<RawSentence> createTwoClassData(ILabel label, List<RawSentence> sentences) {
		List<RawSentence> result = new LinkedList<RawSentence>();
		for(RawSentence sentence : sentences) {
			result.add(this.createTwoClassData(label, sentence));
		}
		return result;
	}
	
	private RawSentence createTwoClassData(ILabel label, RawSentence sentence) {
		RawSentence result = (RawSentence)sentence.clone();
		//if(result.getLabel().equals(label)) System.out.println("sentence label ="+ result.getLabel()+" "+label);
		if(result.getLabel() != null)
			if(result.getLabel().equals(label))
				result.setLabel(BinaryLabel.YES);
			else
				result.setLabel(BinaryLabel.NO);
		return result;
	}
	
	/**
	 * add by maojin
	 * Load the trained classifiers
	 */
	public void loadClassifier(String trainedModelFileFolder){
		log(LogLevel.INFO, "Loading classifier from files...");
		for(ILabel label : labels) {
			log(LogLevel.INFO, "Loading SVM classifier for label " + label.getValue());
			File modelFile = new File(trainedModelFileFolder+File.separator+label.getValue()+".model");
			if(modelFile.exists()){
				SVMClassifier svmClassifier = new SVMClassifier(BinaryLabel.valuesList(), multiFilterOptions, libSVMOptions);
				svmClassifier.setupFilteredClassifier();
				
				//recover the classifier
				Classifier wekaClfer =  (Classifier)WekaModelCaller.readModel(trainedModelFileFolder+File.separator+label.getValue()+".model");
				svmClassifier.getFilteredClassifier().setClassifier(wekaClfer);
				
				//recover the filter
				Filter filter = (Filter)WekaModelCaller.readModel(trainedModelFileFolder+File.separator+label.getValue()+".filter");
				svmClassifier.getFilteredClassifier().setFilter(filter);
				
				//recover instances
				svmClassifier.instances = WekaModelCaller.loadArff(trainedModelFileFolder+File.separator+label.getValue()+".arff");
				svmClassifier.labelAttribute = svmClassifier.instances.attribute(0);
				svmClassifier.textAttribute  = svmClassifier.instances.attribute(1);
				svmClassifier.instances.setClassIndex(0);
				//svmClassifier.getFilteredClassifier().getFilter().setInputFormat(svmClassifier.instances);
				
				svmClassifier.trained = true;
				classifiers.put(label, svmClassifier);
				//break;
			}
		}
		
		
		trained = true;
		
		System.out.println(this.trained);
		log(LogLevel.INFO, "Done training");
	}

}
