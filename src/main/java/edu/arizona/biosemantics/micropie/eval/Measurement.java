package edu.arizona.biosemantics.micropie.eval;

/**
 * hold the measuretype and its value
 * @author maojin
 *
 */
public class Measurement {
	protected String type;
	protected double value;
	
	public Measurement(String type, double value) {
		this.type = type;
		if(!Double.isNaN(value)) this.value = value;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}
	
	public String toString(){
		return this.type+":"+this.value;
	}
}
