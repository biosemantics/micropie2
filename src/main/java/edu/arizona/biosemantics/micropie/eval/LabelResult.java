package edu.arizona.biosemantics.micropie.eval;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.log.ObjectStringifier;

/**
 * LabelResult stores the precision, recall, accuracy, f1 measures calculated for a specific label
 * @author rodenhausen
 */
public class LabelResult {

	private ILabel label;
	private double precision;
	private double recall;
	private double accuracy;
	private double f1;

	/**
	 * @param label
	 * @param precision
	 * @param recall
	 * @param accuracy
	 * @param f1
	 */
	public LabelResult(ILabel label, double precision, double recall,
			double accuracy, double f1) {
		this.label = label;
		this.precision = precision;
		this.recall = recall;
		this.accuracy = accuracy;
		this.f1 = f1;
	}
	
	/**
	 * @return precision
	 */
	public double getPrecision() {
		return precision;
	}

	/**
	 * @return recall
	 */
	public double getRecall() {
		return recall;
	}

	/**
	 * @return accuracy
	 */
	public double getAccuracy() {
		return accuracy;
	}

	/**
	 * @return f1
	 */
	public double getF1() {
		return f1;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(ObjectStringifier.getInstance().stringify(label) + "\n");
		builder.append("precision: " + precision + "\n");
		builder.append("recall: " + recall + "\n");
		builder.append("accuracy: " + accuracy + "\n");
		builder.append("f1: " + f1 + "\n");
		return builder.toString();
	}
	
}
