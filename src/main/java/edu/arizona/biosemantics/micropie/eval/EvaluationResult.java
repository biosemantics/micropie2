package edu.arizona.biosemantics.micropie.eval;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.arizona.biosemantics.micropie.classify.ILabel;

/**
 * EvaluationResult contains the {@link edu.arizona.sista.ista555.eval.LabelResult} for 
 * all {@link edu.arizona.sista.ista555.classify.Label} of an evaluation
 * @author rodenhausen
 */
public class EvaluationResult {

	private Map<ILabel, LabelResult> resultMap = new LinkedHashMap<ILabel, LabelResult>();
	private double overallAccuracy;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Overall accuracy: " + overallAccuracy + "\n");
		for(ILabel label : resultMap.keySet()) {
			LabelResult result = resultMap.get(label);
			builder.append(result.toString() + "\n");
		}
		return builder.toString();
	}

	/**
	 * @param label to set result for
	 * @param labelResult to set
	 */
	public void setLabelResult(ILabel label, LabelResult labelResult) {
		resultMap.put(label, labelResult);
	}
	
	/**
	 * @param label to get result for
	 * @return labelResult for label
	 */
	public LabelResult getLabelResult(ILabel label) {
		return resultMap.get(label);
	}

	public void setOverallAccuracy(double overallAccuracy) {
		this.overallAccuracy = overallAccuracy;
	}
	
}
