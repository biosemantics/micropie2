package edu.arizona.biosemantics.micropie.classify;

import java.io.File;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.io.WekaModelCaller;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.filters.MultiFilter;

/**
 * SVMClassifier wraps a support vector machine classifier of the weka toolkit
 * @author rodenhausen
 */
public class SVMClassifier extends WekaClassifierWrapper {

	private String libSVMOptions;

	/**
	 * @param filterDecorator
	 * @throws Exception 
	 */
	@Inject
	public SVMClassifier(List<ILabel> labels, @Named("MultiFilterOptions")String multiFilterOptions, @Named("LibSVMOptions")String libSVMOptions) {
		super(labels, multiFilterOptions);
		this.libSVMOptions = libSVMOptions;
	}

	@Override
	protected weka.classifiers.Classifier getWekaClassifier() throws Exception {
		LibSVM svm = new LibSVM();
		svm.setOptions(weka.core.Utils.splitOptions(libSVMOptions));
		
		//J48 svm = new J48();//C4.5算法的Java实现
		//svm.setBinarySplits(true);
		
		//NaiveBayes svm = new NaiveBayes();//
		//svm.setUseKernelEstimator(true);
		return svm;
	}
	
	
	/**
	 * add by maojin
	 */
	public FilteredClassifier getFilteredClassifier(){
		return super.filteredClassifier;
	}
	
	
	public Instances getInstances(){
		return this.instances; 
	}
	
}
