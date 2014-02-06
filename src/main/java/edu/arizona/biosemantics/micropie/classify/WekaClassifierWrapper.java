package edu.arizona.biosemantics.micropie.classify;

import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.micropie.log.LogLevel;
import edu.arizona.biosemantics.micropie.log.ObjectStringifier;
import edu.arizona.biosemantics.micropie.model.Sentence;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.MultiFilter;

/**
 * A wrapper for classifiers of the weka machine learning toolkit
 * @author rodenhausen
 */
public abstract class WekaClassifierWrapper implements IClassifier, ITrainableClassifier {

	protected FilteredClassifier filteredClassifier;
	protected boolean trained = false;
	protected Attribute labelAttribute;
	protected Attribute textAttribute;
	protected Instances instances;
	private MultiFilter filter;
	private List<ILabel> labels;
	private String multiFilterOptions;
	
	/**
	 * Sets up attributes, filter and classifier of the weka toolkit
	 * @throws Exception 
	 */
	public WekaClassifierWrapper(List<ILabel> labels) {
		this.labels = labels;
		this.multiFilterOptions = "";
	}

	
	/**
	 * Sets up attributes, filter and classifier of the weka toolkit
	 * @throws Exception 
	 */
	public WekaClassifierWrapper(List<ILabel> labels, String multiFilterOptions) {
		this.labels = labels;
		this.multiFilterOptions = multiFilterOptions;
	}

	@Override
	public void train(List<Sentence> trainingData) throws Exception {
		setupClassifier();
		instances = createInstances(trainingData);
		log(LogLevel.TRACE, "Training data in ARFF format: \n" + instances.toString());
		filter.setInputFormat(instances);		
		filteredClassifier.buildClassifier(instances);
		trained = true;
	}
	
	protected void setupClassifier() throws Exception {
		setupAttributes();
		setupFilteredClassifier();
	}
	
	/**
	 * Uses a {@link edu.arizona.biosemantics.micropie.transform.feature.IFilterDecorator} to configure weka filter.
	 * Wraps the classifier selected from the weka toolkit in a FilteredClassifier of the weka toolkit.
	 * FilteredClassifier applies the filter on the input dataset before the wrapped classifier proceeds with training/classification
	 * @param options to use for filter configuration
	 * @throws Exception 
	 */
	public void setupFilteredClassifier() throws Exception {
		filter = new MultiFilter();
		if(multiFilterOptions != null)
			filter.setOptions(weka.core.Utils.splitOptions(multiFilterOptions));
		
		filteredClassifier = new FilteredClassifier();
		weka.classifiers.Classifier wekaClassifier = getWekaClassifier();
		filteredClassifier.setFilter(filter);
		filteredClassifier.setClassifier(wekaClassifier);
	}

	/**
	 * Setups up the features in the initially inputted dataset (label + text)
	 */
	private void setupAttributes() {
		// nominal attribute for label
		FastVector labelValues = new FastVector(3);
		for(ILabel label : labels)
			labelValues.addElement(label.toString());	
		labelAttribute = new Attribute("my_class_label", labelValues);
		
		// string attribute for text
		FastVector textValues = null;
		textAttribute = new Attribute("text", textValues);
	}
	
	/**
	 * @return the concrete classifier of the weka toolkit to use
	 * @throws Exception 
	 */
	protected abstract weka.classifiers.Classifier getWekaClassifier() throws Exception;

	
	/**
	 * @param trainingData
	 * @return corresponding Instances of the weka toolkit
	 */
	private Instances createInstances(List<Sentence> trainingData) {
		FastVector attributes = new FastVector(0);
		attributes.addElement(labelAttribute);
		attributes.addElement(textAttribute);
				
		Instances instances = new Instances("dataset", attributes, trainingData.size());
		for(Sentence sentence : trainingData) {
			Instance instance = createInstance(sentence);
			instances.add(instance);
		}
		instances.setClass(labelAttribute);
		return instances;
	}
	
	/**
	 * @param review
	 * @return corresponding Instance of the weka toolkit
	 */
	private Instance createInstance(Sentence sentence) {
		Instance instance = new Instance(2);
		instance.setValue(labelAttribute, sentence.getLabel().toString());
		instance.setValue(textAttribute, sentence.getText());
		return instance;
	}
	
	/**
	 * @param text
	 * @return corresponding Instance of the weka toolkit
	 * @throws Exception
	 */
	private Instance createInstance(String text) throws Exception {
		Instance instance = new Instance(2);
		instance.setValue(labelAttribute, "0");
		instance.setValue(textAttribute, text);
		instance.setDataset(instances);
		return instance;
	}

	@Override
	public ILabel getClassification(Sentence sentence) throws Exception {
		if(!trained)
			throw new Exception("Classifier is not trained");
		Instance instance = createInstance(sentence.getText());
		log(LogLevel.TRACE, "Test data in ARFF format: \n" + instance.toString());
		double[] resultDistribution = filteredClassifier.distributionForInstance(instance);
		int maxPropabilityIndex = 0;
		double maxPropability = 0.0;
		for(int i=0; i<resultDistribution.length; i++) {
			if(resultDistribution[i] > maxPropability) {
				maxPropability = resultDistribution[i];
				maxPropabilityIndex = i;
			}
		}
		ILabel result = labels.get(maxPropabilityIndex);
		log(LogLevel.INFO, "Prediction for " + sentence.toString() + "\n"
				+ " -> " + ObjectStringifier.getInstance().stringify(result));
		return result;
	}
		
}
