package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.bytecode.opencsv.CSVWriter;
import edu.arizona.biosemantics.micropie.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;

public class CSVTaxonCharacterMatrixWriter implements ITaxonCharacterMatrixWriter {

	private OutputStream outputStream;
	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void write(TaxonCharacterMatrix matrix) throws Exception {
		log(LogLevel.INFO, "Writing matrix...");
		LinkedHashSet<String> characters = matrix.getCharacters();
		CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF8")));		
		List<String[]> lines = new LinkedList<String[]>();
		
		//create header
		String[] header = new String[characters.size() + 1];
		header[0] = "taxon";
		int i=1;
		for(String character : characters) 
			header[i++] = character;
		lines.add(header);

		//create matrix content
		Map<String, Map<String, Set<String>>> taxonCharacterMap = matrix.getTaxonCharacterMap();
		for(String taxon : matrix.getTaxa()) {
			String[] row = new String[characters.size() + 1];
			row[0] = taxon;
			i=1;
			for(String character : characters) 
				row[i++] = getValueString(taxonCharacterMap.get(taxon).get(character));
			lines.add(row);
		}
		
		//write
		writer.writeAll(lines);
		writer.flush();
		writer.close();
		log(LogLevel.INFO, "Done writing matrix");
	}

	/**
	 * Returns a single string representing the set of values (separated by comma)
	 * @param values
	 * @return
	 */
	private String getValueString(Set<String> values) {
		StringBuilder stringBuilder = new StringBuilder();
		for(String value : values) {
			stringBuilder.append(value + ",");
		}
		String result = stringBuilder.toString();
		if(result.isEmpty())
			return result;
		return result.substring(0, result.length() - 1);
	}

}
