package edu.arizona.biosemantics.micropie.model;

public class CollapseUSPSentByCategoryCharTokenizer {

	public CollapseUSPSentByCategoryCharTokenizer() {
		// TODO Auto-generated constructor stub
	}

	public String[] tokenize(String sentence) {
		return sentence.split("\\s+");
	}
	
}
