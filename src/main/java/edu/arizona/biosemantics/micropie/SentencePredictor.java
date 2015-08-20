package edu.arizona.biosemantics.micropie;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.MultiSVMClassifier;
import edu.arizona.biosemantics.micropie.io.XMLTextReader;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;

/**
 * Predict the categories of the sentence
 * @author maojin
 *
 */
public class SentencePredictor{

	private XMLTextReader textReader;
	private MultiSVMClassifier msvmClassifier;
	private String trainedModelFile;

	@Inject
	public SentencePredictor(XMLTextReader textReader,
			MultiSVMClassifier classifier,
			@Named("trainedModelFile") String trainedModelFile) {
		this.textReader = textReader;
		this.msvmClassifier = classifier;
		this.trainedModelFile = trainedModelFile;
		try {
			msvmClassifier.loadClassifier(trainedModelFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public Set<ILabel> predict(String text) {
		
		//Habitat is not known.
		//Colonies are 0.2 to 0.3 mm in diameter on blood-enriched Columbia agar and Brain Heart Infusion (BHI) agar.
		
		Sentence testSentence = new Sentence(text);

		Set<ILabel> prediction = null;
		try {
			prediction = msvmClassifier.getClassification(testSentence);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(testSentence + "====" + prediction);
		return prediction;
	}

	
	
	
}
