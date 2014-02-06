package edu.arizona.biosemantics.micropie.classify;

import java.util.List;

import edu.arizona.biosemantics.micropie.transform.feature.IFilterDecorator;
import weka.classifiers.functions.LibSVM;
import weka.core.SelectedTag;

/**
 * SVMClassifier wraps a support vector machine classifier of the weka toolkit
 * @author rodenhausen
 */
public class SVMClassifier extends WekaClassifierWrapper {

	/**
	 * @param filterDecorator
	 */
	public SVMClassifier(List<ILabel> labels, IFilterDecorator filterDecorator) {
		super(labels, filterDecorator);
	}

	@Override
	protected weka.classifiers.Classifier getWekaClassifier() {
		LibSVM svm = new LibSVM();
		svm.setDegree(3);
		svm.setSVMType(new SelectedTag(LibSVM.SVMTYPE_C_SVC, LibSVM.TAGS_SVMTYPE));
		svm.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_LINEAR, LibSVM.TAGS_KERNELTYPE));
		svm.setDegree(3);
		svm.setGamma(0);
		svm.setCoef0(0);
		svm.setNu(0.5);
		svm.setCacheSize(100);
		svm.setEps(1e-3);
		svm.setShrinking(true);
		svm.setProbabilityEstimates(false);
		
		/*_param.C = 2048;
		_param.p = 0.1;
		_param.shrinking = 1;
		_param.probability = 0;
		_param.nr_weight = 0;
		_param.weight_label = new int[0];
		_param.weight = new double[0]; */
		return svm;
	}

}
