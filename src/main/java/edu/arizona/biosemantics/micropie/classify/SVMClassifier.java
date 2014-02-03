package edu.arizona.biosemantics.micropie.classify;

import edu.arizona.biosemantics.micropie.transform.feature.IFilterDecorator;
import weka.classifiers.functions.LibSVM;

/**
 * SVMClassifier wraps a support vector machine classifier of the weka toolkit
 * @author rodenhausen
 */
public class SVMClassifier extends WekaClassifierWrapper {

	/**
	 * @param filterDecorator
	 */
	public SVMClassifier(ILabel[] labels, IFilterDecorator filterDecorator) {
		super(labels, filterDecorator);
	}

	@Override
	protected weka.classifiers.Classifier getWekaClassifier() {
		LibSVM svm = new LibSVM();
		return svm;
	}

}
