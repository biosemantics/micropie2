package edu.arizona.biosemantics.micropie.classify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.log.LogLevel;
import edu.arizona.biosemantics.micropie.log.ObjectStringifier;
import edu.arizona.biosemantics.micropie.model.Sentence;

//TODO: Make weka log things such as filtered data (e.g. matrix actually passed into SVM)
//TODO: Evaluate this classifier to work correct on small example training/test data
public class MultiSVMClassifier implements IMultiClassifier, ITrainableClassifier {

	private List<ILabel> labels;
	private Map<ILabel, SVMClassifier> classifiers = new HashMap<ILabel, SVMClassifier>();
	private boolean trained = false;
	private String multiFilterOptions;
	private String libSVMOptions;

	@Inject
	public MultiSVMClassifier(@Named("MultiSVMClassifier_Labels") List<ILabel> labels, 
			@Named("MultiFilterOptions")String multiFilterOptions, @Named("LibSVMOptions")String libSVMOptions) {
		this.labels = labels;
		this.multiFilterOptions = multiFilterOptions;
		this.libSVMOptions = libSVMOptions;
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
		
		log(LogLevel.INFO, "Prediction for " + sentence.toString() + "\n"
				+ " -> " + ObjectStringifier.getInstance().stringify(result));
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

}
