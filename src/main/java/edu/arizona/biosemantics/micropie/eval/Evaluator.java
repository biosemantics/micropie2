package edu.arizona.biosemantics.micropie.eval;

import java.util.List;

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
	public EvaluationResult evaluate(List<ClassifiedSentence> classifiedReviews) {
		int reviewCount = classifiedReviews.size();
		
		EvaluationResult result = new EvaluationResult();
		
		int sumTruePositives = 0;
		for(Label label : Label.values()) {
			int truePositives = getTruePositives(classifiedReviews, label);
			int falsePositives = getFalsePositives(classifiedReviews, label);
			int falseNegatives = getFalseNegatives(classifiedReviews, label);
			int trueNegatives = getTrueNegatives(classifiedReviews, label);
			
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
	
	private int getTruePositives(List<ClassifiedSentence> classifiedSentences, Label label) {
		int result = 0;
		for(ClassifiedSentence classifiedSentence : classifiedSentences) 
			if(classifiedSentence.getPredictions().equals(label) && 
					classifiedSentence.getSentence().getLabel().equals(label)) 
				result++;
		return result;
	}
	
	private int getFalsePositives(List<ClassifiedSentence> classifiedSentences, Label label) {
		int result = 0;
		for(ClassifiedSentence classifiedSentence : classifiedSentences) 
			if(classifiedSentence.getPredictions().equals(label) && 
					!classifiedSentence.getSentence().getLabel().equals(label)) 
				result++;
		return result;
	}
	
	private int getTrueNegatives(List<ClassifiedSentence> classifiedSentences, Label label) {
		int result = 0;
		for(ClassifiedSentence classifiedSentence : classifiedSentences) 
			if(!classifiedSentence.getPredictions().equals(label) && 
					!classifiedSentence.getSentence().getLabel().equals(label)) 
				result++;
		return result;
	}

	private int getFalseNegatives(List<ClassifiedSentence> classifiedSentences, Label label) {
		int result = 0;
		for(ClassifiedSentence classifiedSentence : classifiedSentences) 
			if(!classifiedSentence.getPredictions().equals(label) && 
					classifiedSentence.getSentence().getLabel().equals(label)) 
				result++;
		return result;
	}
}
