package edu.arizona.biosemantics.micropie.model;

/**
 * evaluation results
 * @author maojin
 *
 */
public class EvalResult {

	
	private float precisionValue;
	private float recallValue;
	private float fValue;
	
	public float getPrecisionValue() {
		return precisionValue;
	}
	
	public void setPrecisionValue(float precisionValue) {
		this.precisionValue = precisionValue;
	}

	public float getRecallValue() {
		return recallValue;
	}
	
	public void setRecallValue(float recallValue) {
		this.recallValue = recallValue;
	}
	
	public float getFValue() {
		return fValue;
	}
	
	public void setFValue(float fValue) {
		this.fValue = fValue;
	}
	
	
	
	public EvalResult() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
