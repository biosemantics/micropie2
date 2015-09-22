package edu.arizona.biosemantics.micropie.eval;

import java.util.List;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.model.PredictedSentence;

/**
 * Evaluator determines precision, recall, accuracy and f1 measures for all
 * {@link edu.arziona.sista.ista555.classify.Label}, given classified reviews
 * 
 * 
 */
public class ClassifyEvaluator {

	/**
	 * @param classifiedReviews
	 *            to evaluate
	 * @return the EvaluationResult
	 */
	public EvaluationResult evaluate(
			List<PredictedSentence> PredictedSentences, ILabel[] labels) {
		int reviewCount = PredictedSentences.size();

		EvaluationResult result = new EvaluationResult();

		int sumTruePositives = 0;
		for (ILabel label : labels) {
			int truePositives = getTruePositives(PredictedSentences, label);
			int falsePositives = getFalsePositives(PredictedSentences, label);
			int falseNegatives = getFalseNegatives(PredictedSentences, label);
			int trueNegatives = getTrueNegatives(PredictedSentences, label);

			double precision = truePositives
					/ ((double) (truePositives + falsePositives));
			double recall = truePositives
					/ ((double) (truePositives + falseNegatives));
			double accuracy = (truePositives + trueNegatives)
					/ (double) reviewCount;
			double f1 = 2 * precision * recall / (precision + recall);

			result.setLabelResult(label, new LabelResult(label, precision,
					recall, accuracy, f1));
			sumTruePositives += truePositives;
		}

		double overallAccuracy = sumTruePositives / (double) reviewCount;
		result.setOverallAccuracy(overallAccuracy);
		return result;
	}

	private int getTruePositives(List<PredictedSentence> PredictedSentences,
			ILabel label) {
		int result = 0;
		for (PredictedSentence sent : PredictedSentences)
			if (sent.getPrediction().equals(label)
					&& sent.getLabel().equals(label))
				result++;
		return result;
	}

	private int getFalsePositives(List<PredictedSentence> PredictedSentences,
			ILabel label) {
		int result = 0;
		for (PredictedSentence sent : PredictedSentences)
			if (sent.getPrediction().equals(label)
					&& !sent.getLabel().equals(label))
				result++;
		return result;
	}

	private int getTrueNegatives(List<PredictedSentence> PredictedSentences,
			ILabel label) {
		int result = 0;
		for (PredictedSentence sent : PredictedSentences)
			if (!sent.getPrediction().equals(label)
					&& !sent.getLabel().equals(label))
				result++;
		return result;
	}

	private int getFalseNegatives(List<PredictedSentence> PredictedSentences,
			ILabel label) {
		int result = 0;
		for (PredictedSentence sent : PredictedSentences)
			if (!sent.getPrediction().equals(label)
					&& sent.getLabel().equals(label))
				result++;
		return result;
	}
}