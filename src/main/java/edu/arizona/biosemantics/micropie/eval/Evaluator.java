package edu.arizona.biosemantics.micropie.eval;

import java.util.List;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.model.ClassifiedSentence;

/**
 * Evaluator determines precision, recall, accuracy and f1 measures for all {@link edu.arziona.sista.ista555.classify.Label}, given classified reviews
 * @author rodenhausen
 */
public class Evaluator {
	
	
	
	/**
	 * @param classifiedReviews to evaluate
	 * @return the EvaluationResult
	 */
	public EvaluationResult evaluate(List<ClassifiedSentence> classifiedSentences, ILabel[] labels) {
		int reviewCount = classifiedSentences.size();
		
		EvaluationResult result = new EvaluationResult();
		
		int sumTruePositives = 0;
		for(ILabel label : labels) {
			int truePositives = getTruePositives(classifiedSentences, label);
			int falsePositives = getFalsePositives(classifiedSentences, label);
			int falseNegatives = getFalseNegatives(classifiedSentences, label);
			int trueNegatives = getTrueNegatives(classifiedSentences, label);
			
			double precision = truePositives / ((double) (truePositives + falsePositives));
			double recall = truePositives / ((double) (truePositives + falseNegatives));
			double accuracy = (truePositives + trueNegatives) / (double) reviewCount;
			double f1 = 2 * precision * recall / (precision + recall);
			
			result.setLabelResult(label, new LabelResult(label, precision, 
					recall, accuracy, f1));
			sumTruePositives += truePositives;
		}
		
		double overallAccuracy = sumTruePositives / (double) reviewCount;
		result.setOverallAccuracy(overallAccuracy);
		return result;
	}
	
	private int getTruePositives(List<ClassifiedSentence> classifiedSentences, ILabel label) {
		int result = 0;
		for(ClassifiedSentence classifiedSentence : classifiedSentences) 
			if(classifiedSentence.getPrediction().equals(label) && 
					classifiedSentence.getSentence().getLabel().equals(label)) 
				result++;
		return result;
	}
	
	private int getFalsePositives(List<ClassifiedSentence> classifiedSentences, ILabel label) {
		int result = 0;
		for(ClassifiedSentence classifiedSentence : classifiedSentences) 
			if(classifiedSentence.getPrediction().equals(label) && 
					!classifiedSentence.getSentence().getLabel().equals(label)) 
				result++;
		return result;
	}
	
	private int getTrueNegatives(List<ClassifiedSentence> classifiedSentences, ILabel label) {
		int result = 0;
		for(ClassifiedSentence classifiedSentence : classifiedSentences) 
			if(!classifiedSentence.getPrediction().equals(label) && 
					!classifiedSentence.getSentence().getLabel().equals(label)) 
				result++;
		return result;
	}

	private int getFalseNegatives(List<ClassifiedSentence> classifiedSentences, ILabel label) {
		int result = 0;
		for(ClassifiedSentence classifiedSentence : classifiedSentences) 
			if(!classifiedSentence.getPrediction().equals(label) && 
					classifiedSentence.getSentence().getLabel().equals(label)) 
				result++;
		return result;
	}
}
