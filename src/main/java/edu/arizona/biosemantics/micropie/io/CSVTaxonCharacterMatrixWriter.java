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
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.extract.NumericValueFormatter;
import edu.arizona.biosemantics.micropie.extract.StringValueFormatter;
import edu.arizona.biosemantics.micropie.extract.ValueFormatterUtil;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.NewTaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;

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
		String[] header = new String[characters.size() + 5];
		header[0] = "Taxon";
		header[1] = "XML file";
		header[2] = "Genus";
		header[3] = "Species";
		header[4] = "Strain";	
		
		int i=5;
		for(String character : characters) 
			header[i++] = character;
		lines.add(header);

		//create matrix content
		Map<TaxonTextFile, Map<String, Set<CharacterValue>>> taxonCharacterMap = matrix.getTaxonCharacterMap();
		for(TaxonTextFile taxonFile : matrix.getTaxonFiles()) {
			String[] row = new String[characters.size() + 5];
			row[0] = taxonFile.getTaxon();
			row[1] = taxonFile.getInputFile().getName();//row[1] = taxonFile.getFamily();
			row[2] = taxonFile.getGenus();
			row[3] = taxonFile.getSpecies();
			row[4] = taxonFile.getStrain_number();
			
			i=5;
			for(String character : characters) 
				row[i++] = getValueString(taxonCharacterMap.get(taxonFile).get(character));
			lines.add(row);
		}
		
		//write
		writer.writeAll(lines);
		writer.flush();
		writer.close();
		log(LogLevel.INFO, "Done writing matrix");
	}
	
	
	/**
	 * output
	 * @param matrix
	 * @throws Exception
	 */
	public void write(NewTaxonCharacterMatrix matrix, Map<ILabel, String> labelNameMap, boolean isFormat) throws Exception {
		log(LogLevel.INFO, "Writing matrix...");
		LinkedHashSet<ILabel> characterLabels = matrix.getCharacterLabels();
		LinkedHashSet<String> characterNames = matrix.getCharacterNames();
		
		//System.out.println("characterLabels="+characterLabels.size()+" "+characterNames.size());
		CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF8")));		
		List<String[]> lines = new LinkedList<String[]>();
		
		//create header
		String[] header = new String[characterLabels.size() + 5];
		header[0] = "Taxon";
		header[1] = "XML file";
		header[2] = "Genus";
		header[3] = "Species";
		header[4] = "Strain";	
		
		int i=5;
		for(ILabel character : characterLabels) {
			header[i++] = labelNameMap.get(character);
			//System.out.println(character+" "+header[i-1]);
		}
			
		lines.add(header);

		
		//StringValueFormatter svFormatter = new StringValueFormatter();
		//NumericValueFormatter nvFormatter = new NumericValueFormatter();
		ValueFormatterUtil formatter = new ValueFormatterUtil();
		//create matrix content
		Set<TaxonTextFile> textFiles = matrix.getTaxonFiles();
		for(TaxonTextFile taxonFile : textFiles) {
			String[] row = new String[characterNames.size() + 5];
			row[0] = taxonFile.getTaxon();
			row[1] = taxonFile.getXmlFile();//row[1] = taxonFile.getFamily();
			row[2] = taxonFile.getGenus();
			row[3] = taxonFile.getSpecies();
			row[4] = taxonFile.getStrain_number();
			
			Map<ILabel, List> taxonCharValues = matrix.getAllTaxonCharacterValues(taxonFile);
			i=5;
			for(ILabel character : characterLabels) {
				List values = taxonCharValues.get(character);
				row[i] = formatter.format(values);
				if(!isFormat&&row[i]!=null)  row[i] = row[i].replace("|", " ");
				i++;
			}
			lines.add(row);
		}
		
		//write
		writer.writeAll(lines);
		writer.flush();
		writer.close();
		log(LogLevel.INFO, "Done writing matrix");
	}
	
	
	/**
	 * output
	 * @param matrix
	 * @throws Exception
	 */
	public void writeWithTaxonName(NewTaxonCharacterMatrix matrix, Map<ILabel, String> labelNameMap) throws Exception {
		log(LogLevel.INFO, "Writing matrix...");
		LinkedHashSet<ILabel> characterLabels = matrix.getCharacterLabels();
		LinkedHashSet<String> characterNames = matrix.getCharacterNames();
		
		//System.out.println("characterLabels="+characterLabels.size()+" "+characterNames.size());
		CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF8")));		
		List<String[]> lines = new LinkedList<String[]>();
		
		//create header
		String[] header = new String[characterLabels.size() + 5];
		header[0] = "Taxon";
		header[1] = "XML file";
		header[2] = "Genus";
		header[3] = "Species";
		header[4] = "Strain";	
		
		int i=5;
		for(ILabel character : characterLabels) {
			header[i++] = labelNameMap.get(character);
			//System.out.println(character+" "+header[i-1]);
		}
			
		lines.add(header);

		
		//StringValueFormatter svFormatter = new StringValueFormatter();
		//NumericValueFormatter nvFormatter = new NumericValueFormatter();
		ValueFormatterUtil formatter = new ValueFormatterUtil();
		//create matrix content
		Set<String> textFiles = matrix.keySet();
		for(String taxonFile : textFiles) {
			String[] row = new String[characterNames.size() + 5];
			row[0] = taxonFile.substring(0,taxonFile.indexOf("_"));
			row[1] =  taxonFile.substring(taxonFile.indexOf("_")+1,taxonFile.length());
			
			Map<ILabel, List> taxonCharValues = matrix.getAllTaxonCharacterValues(taxonFile);
			i=5;
			for(ILabel character : characterLabels) {
				List values = taxonCharValues.get(character);
				row[i++] = formatter.format(values);
			}
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
	private String getValueString(Set<CharacterValue> values) {
		StringBuilder stringBuilder = new StringBuilder();
		for(CharacterValue value : values) {
			stringBuilder.append(value.getValue() + ",");
		}
		String result = stringBuilder.toString();
		if(result.isEmpty())
			return result;
		return result.substring(0, result.length() - 1);
	}
	
	

}
