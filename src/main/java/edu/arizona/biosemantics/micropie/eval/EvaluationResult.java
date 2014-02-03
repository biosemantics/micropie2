package edu.arizona.biosemantics.micropie.eval;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.arizona.biosemantics.micropie.classify.Label;

/**
 * EvaluationResult contains the {@link edu.arizona.sista.ista555.eval.LabelResult} for 
 * all {@link edu.arizona.sista.ista555.classify.Label} of an evaluation
 * @author rodenhausen
 */
public class EvaluationResult {

	private Map<Label, LabelResult> resultMap = new LinkedHashMap<Label, LabelResult>();
	private double overallAccuracy;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Overall accuracy: " + overallAccuracy + "\n");
		for(Label label : resultMap.keySet()) {
			LabelResult result = resultMap.get(label);
			builder.append(result.toString() + "\n");
		}
		return builder.toString();
	}

	/**
	 * @param label to set result for
	 * @param labelResult to set
	 */
	public void setLabelResult(Label label, LabelResult labelResult) {
		resultMap.put(label, labelResult);
	}
	
	/**
	 * @param label to get result for
	 * @return labelResult for label
	 */
	public LabelResult getLabelResult(Label label) {
		return resultMap.get(label);
	}

	public void setOverallAccuracy(double overallAccuracy) {
		this.overallAccuracy = overallAccuracy;
	}
	
}
