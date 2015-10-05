package edu.arizona.biosemantics.micropie.eval;

public class DetailMeasurement extends Measurement{
	
	public DetailMeasurement(String type, double value,String gstValue,String tgValue) {
		super(type, value);
		this.gstValue = gstValue;
		this.tgValue = tgValue;
	}
	
	private String gstValue;
	private String tgValue;
	
	public String getGstValue() {
		return gstValue;
	}
	public void setGstValue(String gstValue) {
		this.gstValue = gstValue;
	}
	public String getTgValue() {
		return tgValue;
	}
	public void setTgValue(String tgValue) {
		this.tgValue = tgValue;
	}
}
