package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.bytecode.opencsv.CSVWriter;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.NumericValueFormatter;
import edu.arizona.biosemantics.micropie.extract.StringValueFormatter;
import edu.arizona.biosemantics.micropie.extract.ValueFormatterUtil;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
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
	 * @param outputCharacterLabels 
	 * @throws Exception
	 */
	public void write(NewTaxonCharacterMatrix matrix, Map<ILabel, String> labelNameMap, LinkedHashSet<ILabel> outputCharacterLabels, boolean isFormat) throws Exception {
		log(LogLevel.INFO, "Writing matrix...");
		LinkedHashSet<ILabel> characterLabels = matrix.getCharacterLabels();
		LinkedHashSet<String> characterNames = matrix.getCharacterNames();
		
		//System.out.println("characterLabels="+characterLabels.size()+" "+characterNames.size());
		CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF8")));	
		
		outputStream.write(239);
		outputStream.write(187);
		outputStream.write(191);
		List<String[]> lines = new LinkedList<String[]>();
		
		
		//create header
		String[] header = new String[characterLabels.size() + 6];
		header[0] = "Taxon";
		header[1] = "XML file";
		header[2] = "Genus";
		header[3] = "Species";
		header[4] = "Strain";	
		
		int i=5;
		for(ILabel character : characterLabels) {
			if(outputCharacterLabels==null||outputCharacterLabels.contains(character)) header[i++] = labelNameMap.get(character);
			//System.out.println(character+" "+header[i-1]);
		}
		header[i++] = "Character not determined";
		lines.add(header);

		
		//StringValueFormatter svFormatter = new StringValueFormatter();
		//NumericValueFormatter nvFormatter = new NumericValueFormatter();
		ValueFormatterUtil formatter = new ValueFormatterUtil();
		//create matrix content
		Set<TaxonTextFile> textFiles = matrix.getTaxonFiles();
		for(TaxonTextFile taxonFile : textFiles) {
			String[] row = new String[characterNames.size() + 6];
			row[0] = taxonFile.getTaxon();
			row[1] = taxonFile.getXmlFile();//row[1] = taxonFile.getFamily();
			row[2] = taxonFile.getGenus();
			row[3] = taxonFile.getSpecies();
			row[4] = taxonFile.getStrain_number();
			
			Map<ILabel, List> taxonCharValues = matrix.getAllTaxonCharacterValues(taxonFile);
			i=5;
			for(ILabel character : characterLabels) {
				if(outputCharacterLabels==null||outputCharacterLabels.contains(character)){
					List values = taxonCharValues.get(character);
					row[i] = formatter.format(values);
					if(!isFormat&&row[i]!=null)  row[i] = row[i].replace("|", " ");
					i++;
				}
			}
			
			//character not identified
			List values = taxonCharValues.get(Label.USP);
			row[i] = formatter.format(values);
			if(!isFormat&&row[i]!=null)  row[i] = row[i].replace("|", " ");
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
	 * @param outputCharacterLabels 
	 * @throws Exception
	 */
	public void writeMatrixConverter(NewTaxonCharacterMatrix matrix, Map<ILabel, String> labelNameMap, LinkedHashSet<ILabel> outputCharacterLabels, boolean isFormat) throws Exception {
		log(LogLevel.INFO, "Writing matrix...");
		LinkedHashSet<ILabel> characterLabels = matrix.getCharacterLabels();
		LinkedHashSet<String> characterNames = matrix.getCharacterNames();
		
		//System.out.println("characterLabels="+characterLabels.size()+" "+characterNames.size());
		CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF8")), ',',CSVWriter.NO_QUOTE_CHARACTER);	
		
		outputStream.write(239);
		outputStream.write(187);
		outputStream.write(191);
		List<String[]> lines = new LinkedList<String[]>();
		
		
		//create header
		String[] header = new String[characterLabels.size() + 6];
		header[0] = "Taxon";
		header[1] = "XML file";
		header[2] = "Genus";
		header[3] = "Species";
		header[4] = "Strain";	
		
		int i=5;
		for(ILabel character : characterLabels) {
			if(outputCharacterLabels==null||outputCharacterLabels.contains(character)) header[i++] = labelNameMap.get(character);
			//System.out.println(character+" "+header[i-1]);
		}
		header[i++] = "Character not determined";
		lines.add(header);

		
		//StringValueFormatter svFormatter = new StringValueFormatter();
		//NumericValueFormatter nvFormatter = new NumericValueFormatter();
		ValueFormatterUtil formatter = new ValueFormatterUtil();
		//create matrix content
		Set<TaxonTextFile> textFiles = matrix.getTaxonFiles();
		for(TaxonTextFile taxonFile : textFiles) {
			String[] row = new String[characterNames.size() + 5];
			row[0] = taxonFile.getTaxon().replace(",", " ");
			row[1] = taxonFile.getXmlFile().replace(",", " ");//row[1] = taxonFile.getFamily();
			row[2] = taxonFile.getGenus().replace(",", " ");
			row[3] = taxonFile.getSpecies().replace(",", " ");
			row[4] = taxonFile.getStrain_number().replace(",", " ");
			
			Map<ILabel, List> taxonCharValues = matrix.getAllTaxonCharacterValues(taxonFile);
			i=5;
			for(ILabel character : characterLabels) {
				if(outputCharacterLabels==null||outputCharacterLabels.contains(character)){
					//character value
					List values = taxonCharValues.get(character);
					row[i] = formatter.format(values);
					if(!isFormat&&row[i]!=null){
						row[i] = row[i].replace("|", " ").replace("#", "|").replace(",", " ");
					}
					i++;
					
				}
			}
			
			//character not identified
			List values = taxonCharValues.get(Label.USP);
			row[i] = formatter.format(values);
			if(!isFormat&&row[i]!=null){
				row[i] = row[i].replace("|", " ").replace("#", "|").replace(",", " ");
			}
			
			
			lines.add(row);
		}
		
		//write
		writer.writeAll(lines);
		writer.flush();
		writer.close();
		log(LogLevel.INFO, "Done writing matrix");
	}
	
	
	public Map<ILabel, List> convertLabelSentMap(List<MultiClassifiedSentence> classifiedSentences) {
		Map<ILabel, List> labelSentMap = new HashMap();
		for(MultiClassifiedSentence sent:classifiedSentences){
			Set<ILabel> labels = sent.getPredictions();
			String text = sent.getText();
			Iterator<ILabel> labelIter = labels.iterator();
			while(labelIter.hasNext()){
				ILabel label = labelIter.next();
				List sentList = labelSentMap.get(label);
				if(sentList==null){
					sentList = new ArrayList();
					labelSentMap.put(label, sentList);
				}
				sentList.add(text);
				System.out.println(label+"\t"+text);
			}
			
			//if no class
			if(labels==null||labels.size()==0){
				List sentList = labelSentMap.get(null);
				if(sentList==null){
					sentList = new ArrayList();
					labelSentMap.put(null, sentList);
				}
				sentList.add(text);
			}
		}
		return labelSentMap;
	}

	/**
	 * output character and sent matrix
	 * if outputCharacterLabels is null , output all characters.
	 * 
	 * @param matrix
	 * @param outputCharacterLabels 
	 * @throws Exception
	 */
	public void writeCharSentMatrix(NewTaxonCharacterMatrix matrix, Map<ILabel, String> labelNameMap, LinkedHashSet<ILabel> outputCharacterLabels, boolean isFormat) throws Exception {
		log(LogLevel.INFO, "Writing matrix...");
		LinkedHashSet<ILabel> characterLabels = matrix.getCharacterLabels();
		LinkedHashSet<String> characterNames = matrix.getCharacterNames();
		
		//System.out.println("characterLabels="+characterLabels.size()+" "+characterNames.size());
		CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF8")));	
		
		outputStream.write(239);
		outputStream.write(187);
		outputStream.write(191);
		List<String[]> lines = new LinkedList<String[]>();
		
		
		//create header
		String[] header = new String[characterLabels.size()*2 + 7];
		header[0] = "Taxon";
		header[1] = "XML file";
		header[2] = "Genus";
		header[3] = "Species";
		header[4] = "Strain";	
		
		int i=5;
		for(ILabel character : characterLabels) {
			if(outputCharacterLabels==null||outputCharacterLabels.contains(character)){
				header[i++] = labelNameMap.get(character);//character
				header[i++] = labelNameMap.get(character)+" [sentence]";//character sentence
			}
			//System.out.println(character+" "+header[i-1]);
		}
		header[i++] = "Character not determined";
		header[i++] = "no characters in these sentences [sentence]";
		lines.add(header);

		
		//StringValueFormatter svFormatter = new StringValueFormatter();
		//NumericValueFormatter nvFormatter = new NumericValueFormatter();
		ValueFormatterUtil formatter = new ValueFormatterUtil();
		//create matrix content
		Set<TaxonTextFile> textFiles = matrix.getTaxonFiles();
		for(TaxonTextFile taxonFile : textFiles) {
			String[] row = new String[characterNames.size()*2 + 7];
			row[0] = taxonFile.getTaxon();
			row[1] = taxonFile.getXmlFile();//row[1] = taxonFile.getFamily();
			row[2] = taxonFile.getGenus();
			row[3] = taxonFile.getSpecies();
			row[4] = taxonFile.getStrain_number();
			
		
			Map<ILabel, List> taxonCharValues = matrix.getAllTaxonCharacterValues(taxonFile);
			List<MultiClassifiedSentence> classifiedSentences = taxonFile.getSentences();
			Map<ILabel, List> labelSents = convertLabelSentMap(classifiedSentences);
			i=5;
			for(ILabel character : characterLabels) {
				if(outputCharacterLabels==null||outputCharacterLabels.contains(character)){
					//character value
					List values = taxonCharValues.get(character);
					row[i] = formatter.format(values);
					if(!isFormat&&row[i]!=null){
						row[i] = row[i].replace("|", " ").replace("#", "|").replace(",", " ");
					}
					i++;
					
					//character sentence
					List<String> sentTexts = labelSents.get(character);
					StringBuffer charSentSb = new StringBuffer();
					if(sentTexts!=null){
						for(String sent:sentTexts){
							charSentSb.append("[").append(sent).append("] ");
						}
					}
					row[i] = charSentSb.toString();
					i++;
				}
			}//labels over
			
			//character not identified
			List values = taxonCharValues.get(Label.USP);
			row[i] = formatter.format(values);
			if(!isFormat&&row[i]!=null){
				row[i] = row[i].replace("|", " ").replace("#", "|").replace(",", " ");
			}
			i++;
			
			//no characters in these sentences [sentence]
			//character sentence
			List<String> sentTexts = labelSents.get(null);
			StringBuffer charSentSb = new StringBuffer();
			if(sentTexts!=null){
				for(String sent:sentTexts){
					charSentSb.append("[").append(sent).append("] ");
				}
			}
			row[i] = charSentSb.toString();
			
			//add to the CSV file
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
