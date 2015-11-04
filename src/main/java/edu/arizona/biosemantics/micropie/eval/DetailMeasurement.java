package edu.arizona.biosemantics.micropie.eval;

public class DetailMeasurement extends Measurement{
	
	public DetailMeasurement(String type, double value,String gstValue,String tgValue, double gstNum, double tgNum) {
		super(type, value);
		this.gstValue = gstValue;
		this.tgValue = tgValue;
		this.gstNum = gstNum;
		this.tgNum = tgNum;
	}
	
	private String gstValue;
	private String tgValue;
	private double gstNum;
	private double tgNum;
	
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
	public double getGstNum() {
		return gstNum;
	}
	public void setGstNum(double gstNum) {
		this.gstNum = gstNum;
	}
	public double getTgNum() {
		return tgNum;
	}
	public void setTgNum(double tgNum) {
		this.tgNum = tgNum;
	}
}
