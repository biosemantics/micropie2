package edu.arizona.biosemantics.micropie.io;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.classify.BinaryLabel;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.MultiSVMClassifier;
import edu.arizona.biosemantics.micropie.classify.SVMClassifier;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

/**
 * 
 * @author maojin
 *
 */
public class WekaModelCaller {

	/**
	 * save all the trained classifiers into a model file
	 */
	public static void saveModel(MultiSVMClassifier mutliClassifier, String modelPath) {
    	List<ILabel> labels  = mutliClassifier.getLabels();
    	Map<ILabel, SVMClassifier> classifiers = mutliClassifier.getClassifiers();
    	for(ILabel label : labels) {
			SVMClassifier classifier =  classifiers.get(label);
			saveModel(classifier, label, modelPath);
		}
	}
	
	/**
	 * save one model 
	 * @param classifier
	 * @param label
	 * @param modelPath
	 * @return 
	 */
	public static void saveModel(SVMClassifier classifier, ILabel label, String modelPath){
		ObjectOutputStream oos = null;
		try {
			//save the model
			oos = new ObjectOutputStream(new FileOutputStream(modelPath+File.separator+label.getValue()+".model"));
			oos.writeObject(classifier.getFilteredClassifier().getClassifier());
			oos.flush();
			oos.close();
			
			//save the arff
			saveArff(modelPath+File.separator+label+".arff", classifier.getInstances());
			
			//save the filter
			serilize(modelPath+File.separator+label+".filter",classifier.getFilteredClassifier().getFilter());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void serilize(String objPath,Object obj){
		try {
			weka.core.SerializationHelper.write(objPath, obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * read a classifier from the saved model file
	 */
	public static Object readModel(String modelPath) {
		Object cls = null;
		try {
			cls = (Object) weka.core.SerializationHelper.read(modelPath);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return cls;
	}
	
	
	/**
	 * save the instances into arff File
	 */
	public static void saveArff(String arrfFileName, Instances inst){
		File arffName = new File(arrfFileName);
		OutputStream writer;
		try {
			writer = new DataOutputStream(new FileOutputStream(arffName));
			ArffSaver saver = new ArffSaver();
			saver.setInstances(inst);
			saver.setDestination(writer);
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * load arff file
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static Instances loadArff(String fileName){
		Instances dataset = null;
		File file = new File(fileName);
		ArffLoader loder = new ArffLoader();
		try {
			loder.setFile(file);
			dataset = loder.getDataSet();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return dataset;	
	}
}
