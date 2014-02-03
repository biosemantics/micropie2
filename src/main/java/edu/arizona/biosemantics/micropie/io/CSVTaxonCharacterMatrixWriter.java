package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.model.ClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;

public class CSVTaxonCharacterMatrixWriter implements IMatrixWriter {

	private OutputStream outputStream;
	private String seperator;

	public CSVTaxonCharacterMatrixWriter(String seperator) {
		this.seperator = seperator;
	}
	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void write(TaxonCharacterMatrix matrix) throws Exception {
		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
				
		LinkedHashSet<String> characters = matrix.getCharacters();
		bufferedWriter.write("taxon" + seperator);
		bufferedWriter.write(getValueString(characters) + "\n");
		
		Map<String, Map<String, Set<String>>> taxonCharacterMap = matrix.getTaxonCharacterMap();
		for(String taxon : matrix.getTaxa()) {
			bufferedWriter.write(taxon + seperator);
			for(String character : characters) {
				bufferedWriter.write(getValueString(taxonCharacterMap.get(taxon).get(character)) + "\n");
			}
		}
		
		bufferedWriter.flush();
		bufferedWriter.close();
	}

	private String getValueString(Set<String> values) {
		StringBuilder stringBuilder = new StringBuilder();
		for(String value : values) {
			stringBuilder.append(value + ",");
		}
		String result = stringBuilder.toString();
		return result.substring(0, result.length() - 1);
	}

}
