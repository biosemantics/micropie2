package edu.arizona.biosemantics.micropie.classify;

import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import weka.classifiers.functions.LibSVM;

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
		return svm;
	}
}
