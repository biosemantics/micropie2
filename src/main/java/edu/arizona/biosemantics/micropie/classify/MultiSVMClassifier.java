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
import edu.arizona.biosemantics.micropie.model.Sentence;

//TODO: Make weka log things such as filtered data (e.g. matrix actually passed into SVM)
//TODO: Evaluate this classifier to work correct on small example training/test data
public class MultiSVMClassifier implements IMultiClassifier, ITrainableClassifier {

	private List<ILabel> labels;
	private Map<ILabel, SVMClassifier> classifiers = new HashMap<ILabel, SVMClassifier>();
	protected boolean trained = false;
	private String multiFilterOptions;
	private String libSVMOptions;
	
	private String trainedModelFile;
	

	@Inject
	public MultiSVMClassifier(@Named("MultiSVMClassifier_Labels") List<ILabel> labels, 
			@Named("MultiFilterOptions")String multiFilterOptions, @Named("LibSVMOptions")String libSVMOptions,
			@Named("trainedModelFile") String trainedModelFile) {
		this.labels = labels;
		this.multiFilterOptions = multiFilterOptions;
		this.libSVMOptions = libSVMOptions;
		
		this.trainedModelFile = trainedModelFile;
	}
	
	@Override
	public Set<ILabel> getClassification(Sentence sentence) throws Exception {
		if(!trained)
			throw new Exception("Classifier is not trained");
				
		Set<ILabel> result = new HashSet<ILabel>();
		for(ILabel label : labels) {
			Sentence twoClassSentence = this.createTwoClassData(label, sentence);
			ILabel prediction = classifiers.get(label).getClassification(twoClassSentence);
			if(prediction.equals(BinaryLabel.YES))
				result.add(label);
		}
		
		log(LogLevel.INFO, "Prediction for " + sentence.getText() + "\n"
				+ " -> " + ObjectStringifier.getInstance().stringify(result));
		
		//System.out.println("Prediction for " + sentence.getText() + "\n"
		//		+ " -> " + ObjectStringifier.getInstance().stringify(result));
		

		
		return result;
	}
	
	@Override
	public void train(List<Sentence> trainingData) throws Exception {
		log(LogLevel.INFO, "Training classifier...");
		for(ILabel label : labels) {
			log(LogLevel.INFO, "Training SVM for label " + label.getValue());
			SVMClassifier classifier = new SVMClassifier(BinaryLabel.valuesList(), multiFilterOptions, libSVMOptions);
			classifiers.put(label, classifier);
			
			List<Sentence> twoClassData = createTwoClassData(label, trainingData);
			classifier.train(twoClassData);
		}
		trained = true;
		log(LogLevel.INFO, "Done training");
	}

	private List<Sentence> createTwoClassData(ILabel label, List<Sentence> sentences) {
		List<Sentence> result = new LinkedList<Sentence>();
		for(Sentence sentence : sentences) {
			result.add(this.createTwoClassData(label, sentence));
		}
		return result;
	}
	
	private Sentence createTwoClassData(ILabel label, Sentence sentence) {
		Sentence result = (Sentence)sentence.clone();
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
	public void loadClassifier(@Named("trainedModelFile")String trainedModelFile) throws Exception {
		log(LogLevel.INFO, "Loading classifier from files...");
		for(ILabel label : labels) {
			log(LogLevel.INFO, "Loading SVM classifier for label " + label.getValue());
			
			SVMClassifier svmClassifier = new SVMClassifier(BinaryLabel.valuesList(), multiFilterOptions, libSVMOptions);
			svmClassifier.setupFilteredClassifier();
			
			//recover the classifier
			Classifier wekaClfer =  (Classifier)WekaModelCaller.readModel(trainedModelFile+File.separator+label.getValue()+".model");
			svmClassifier.getFilteredClassifier().setClassifier(wekaClfer);
			
			//recover the filter
			Filter filter = (Filter)WekaModelCaller.readModel(trainedModelFile+File.separator+label.getValue()+".filter");
			svmClassifier.getFilteredClassifier().setFilter(filter);
			
			//recover instances
			svmClassifier.instances = WekaModelCaller.loadArff(trainedModelFile+File.separator+label.getValue()+".arff");
			svmClassifier.labelAttribute = svmClassifier.instances.attribute(0);
			svmClassifier.textAttribute  = svmClassifier.instances.attribute(1);
			svmClassifier.instances.setClassIndex(0);
			//svmClassifier.getFilteredClassifier().getFilter().setInputFormat(svmClassifier.instances);
			
			svmClassifier.trained = true;
			classifiers.put(label, svmClassifier);
			//break;
		}
		
		
		trained = true;
		
		System.out.println(this.trained);
		log(LogLevel.INFO, "Done training");
	}

	/**
	 * predict the categories for the sentence
	 * @param sentence
	 * @return
	 * @throws Exception
	 */
	public  Set<ILabel> predictClassification(Sentence sentence) throws Exception{
		Set<ILabel> result = new HashSet<ILabel>();
		for(ILabel label : labels) {
			Sentence twoClassSentence = this.createTwoClassData(label, sentence);
			ILabel prediction = classifiers.get(label).predictClassification(twoClassSentence);
			if(prediction.equals(BinaryLabel.YES))
				result.add(label);
			//break;
		}
		
		log(LogLevel.INFO, "Prediction for " + sentence.getText() + "\n"
				+ " -> " + ObjectStringifier.getInstance().stringify(result));
		
		System.out.println("Prediction for " + sentence.getText() + "\n"
				+ " -> " + ObjectStringifier.getInstance().stringify(result));
		

		
		return result;
		
	}
	
	public List<ILabel> getLabels() {
		return labels;
	}

	public Map<ILabel, SVMClassifier> getClassifiers() {
		return classifiers;
	}
	
	

}
