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
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;

/**
 * Predict the categories of the sentence
 * @author maojin
 *
 */
public class SentencePredictor{

	private MultiSVMClassifier msvmClassifier;

	@Inject
	public SentencePredictor(MultiSVMClassifier classifier,
			@Named("trainedModelFile") String trainedModelFile) {
		System.out.println("trainedModelFile="+trainedModelFile);
		this.msvmClassifier = classifier;
		try {
			msvmClassifier.loadClassifier(trainedModelFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Predict the categories for the text
	 * @param text
	 * @return
	 */
	public Set<ILabel> predict(String text) {
		
		//Habitat is not known.
		//Colonies are 0.2 to 0.3 mm in diameter on blood-enriched Columbia agar and Brain Heart Infusion (BHI) agar.
		
		RawSentence testSentence = new RawSentence(text);

		Set<ILabel> prediction = null;
		try {
			prediction = msvmClassifier.predict(testSentence);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prediction;
	}
	
	/**
	 * Predict the categories for the sentence
	 * @param sent
	 * @return
	 */
	public Set<ILabel> predict(RawSentence sent) {
		Set<ILabel> prediction = null;
		try {
			prediction = msvmClassifier.predict(sent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prediction;
	}
	
	
	/**
	 * Predict the categories for the sentence
	 * @param sent
	 * @return
	 */
	public Set<ILabel> predict(MultiClassifiedSentence sent) {
		Set<ILabel> prediction = null;
		try {
			prediction = this.predict(sent.getText());
			sent.setPredictions(prediction);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prediction;
	}
	
}
