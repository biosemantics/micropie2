package edu.arizona.biosemantics.micropie.classify;

import java.util.List;
import java.util.Random;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.common.log.ObjectStringifier;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.stemmers.SnowballStemmer;
import weka.core.stemmers.Stemmer;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * A wrapper for classifiers of the weka machine learning toolkit
 * @author rodenhausen
 */
public abstract class WekaClassifierWrapper implements IClassifier, ITrainableClassifier {
	private String nGramTokenizerOptions = "-delimiters ' ' -max 1 -min 1";
	private String stringToWordVectorOptions = "-W " + Integer.MAX_VALUE + " -T -L -M 1 -tokenizer weka.core.tokenizer.NGramTokenizer " + nGramTokenizerOptions + "";
	private String multiFilterOptions = "-D -F weka.filters.unsupervised.attribute.StringToWordVector " + stringToWordVectorOptions + "";
	
	
	protected FilteredClassifier filteredClassifier;
	protected boolean trained = false;
	protected Attribute labelAttribute;
	protected Attribute textAttribute;
	protected Instances instances;
	private MultiFilter filter;
	private List<ILabel> labels;
	
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
	public void train(List<RawSentence> trainingData) throws Exception {
		setupClassifier();
		instances = createInstances(trainingData);
		log(LogLevel.TRACE, "Training data in ARFF format: \n" + instances.toString());
		filter.setInputFormat(instances);
		

	   // attributeSelection.setInputFormat(instances); 
	   // instances = Filter.useFilter(instances, attributeSelection); 
		filteredClassifier.buildClassifier(instances);
		
		// evaluate classifier and print some statistics
		Instances newInstances = Filter.useFilter(instances, filter);
		weka.classifiers.Evaluation eval = new weka.classifiers.Evaluation(newInstances);
		eval.crossValidateModel(filteredClassifier.getClassifier(), newInstances, 10, new Random(1));
		System.out.println(eval.toSummaryString("\nResults\n======\n", true));
		 
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
	public void setupFilteredClassifier() {
		try {
		filter = new MultiFilter();
		//if(multiFilterOptions != null)
		//		filter.setOptions(weka.core.Utils.splitOptions(multiFilterOptions));
		
		//feature selection
	    //Filter StringToWordVector   
	    StringToWordVector STWfilter = new StringToWordVector(); 
	    STWfilter.setIDFTransform(true);//-I
	    STWfilter.setTFTransform(true);//T
	   // STWfilter.setStemmer(new SnowballStemmer());
	   // STWfilter.setStopwords(value);
	    
	    NGramTokenizer ngtokenizer = new NGramTokenizer();
	    ngtokenizer.setDelimiters(" ");
	    ngtokenizer.setNGramMinSize(1);
	    ngtokenizer.setNGramMaxSize(1);
	    STWfilter.setTokenizer(ngtokenizer);
	    
	    //Filter unbalanced instances
	    //SMOTE smoteFilter = new  SMOTE();
	    // smoteFilter.setNearestNeighbors(5);
	    // smoteFilter.setPercentage(200);//1000%
	    SpreadSubsample subsampleFilter = new SpreadSubsample();
	    subsampleFilter.setDistributionSpread(1.0);
	    
		//Filter Attribute selection
		int n = 100; //100， number of features to select 
	    AttributeSelection attributeSelection = new  AttributeSelection(); 
	    Ranker ranker = new Ranker(); 
	    ranker.setNumToSelect(n);
	    ranker.setThreshold(0.0);
	    
	    InfoGainAttributeEval infoGainAttributeEval = new InfoGainAttributeEval(); 
	    attributeSelection.setEvaluator(infoGainAttributeEval); 
	    attributeSelection.setSearch(ranker); 
	    
	    //filter.setFilters(new Filter[]{STWfilter,subsampleFilter,attributeSelection});
	    filter.setFilters(new Filter[]{STWfilter,subsampleFilter});
	    //filter.setFilters(new Filter[]{STWfilter});
	    
		filteredClassifier = new FilteredClassifier();
		weka.classifiers.Classifier wekaClassifier = getWekaClassifier();
		filteredClassifier.setFilter(filter);
		filteredClassifier.setClassifier(wekaClassifier);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	private Instances createInstances(List<RawSentence> trainingData) {
		FastVector attributes = new FastVector(0);
		attributes.addElement(labelAttribute);
		attributes.addElement(textAttribute);
				
		Instances instances = new Instances("dataset", attributes, trainingData.size());
		for(RawSentence sentence : trainingData) {
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
	private Instance createInstance(RawSentence sentence) {
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
	public ILabel getClassification(RawSentence sentence) throws Exception {
		if(!trained)
			throw new Exception("Classifier is not trained");
		Instance instance = createInstance(sentence.getText());
		log(LogLevel.TRACE, "Test data in ARFF format: \n" + instance.toString());
		double[] resultDistribution = filteredClassifier.distributionForInstance(instance);
		//find the most probably one
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
	
	
	public ILabel predictClassification(RawSentence sentence) throws Exception {
		if(!trained)
			throw new Exception("Classifier is not trained");
		
		
		
		Instance instance = createInstance(sentence.getText());
		/*
		Instances testinstances = new Instances(instances,0);
		testinstances.setClassIndex(0);
		testinstances.add(instance);
		
		long b = System.currentTimeMillis();
		filteredClassifier.getFilter().setInputFormat(testinstances);
		Filter.useFilter(testinstances, filteredClassifier.getFilter());
		long e = System.currentTimeMillis();
		System.out.println(e-b);*/
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
