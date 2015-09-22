package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;


/**
 * store the predicted results into the CSV file
 * @author maojin
 * 
 */
public class CSVClassifiedSentenceWriter implements IClassifiedSentenceWriter {

	private OutputStream outputStream;
	private InputStream inputStream;
	private File labelMappingFile;

	/**
	 * in the file, the fields are:
	 * categoryNo, categoryLabel, categoryName
	 * "1","1.1","%G+C"
	 */
	private Map<String, String> svmLabelAndCategoryMappingMap;

	/**
	 * specify where to store the prediction file
	 * @param outputFile
	 */
	public void setOutputFile(String predictionFile) {
		try {
			this.outputStream = new FileOutputStream(predictionFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param inputStream to read SVMLabelSubcategoryMapping.txt
	 */	
	public void setLabelMappingFile(String labelMappingFilePath) {
		this.labelMappingFile = new File(labelMappingFilePath);
		try {
			this.inputStream = new FileInputStream(labelMappingFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * read the label category mapping pairs
	 * @return
	 * @throws IOException
	 */
	public Map<String, String> readSVMLabelAndCategoryMapping(){
		CSVReader readerOfSVMLabelAndCategoryMapping;
		try {
			readerOfSVMLabelAndCategoryMapping = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
			List<String[]> linesOfSVMLabelAndCategoryMapping = readerOfSVMLabelAndCategoryMapping.readAll();
			
			svmLabelAndCategoryMappingMap = new HashMap<String, String>();
			for(String[] lineOfSVMLabelAndCategoryMapping : linesOfSVMLabelAndCategoryMapping) {
				if ( lineOfSVMLabelAndCategoryMapping.length > 2 ) {
					String catNo = lineOfSVMLabelAndCategoryMapping[0];
					String catLabel = lineOfSVMLabelAndCategoryMapping[1];
					svmLabelAndCategoryMappingMap.put(catNo,catLabel);
				}	
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return svmLabelAndCategoryMappingMap;
	}	
	
	@Override
	/**
	 * output the results in a CSV file
	 * Format: categoryLabel,sentence
	 * 
	 */
	public void write(List<MultiClassifiedSentence> classifiedSentences){
		log(LogLevel.INFO, "Writing prediciton results...");
		
		if(svmLabelAndCategoryMappingMap==null){
			this.readSVMLabelAndCategoryMapping();
		}
		
		CSVWriter writer = null;
		try {
			writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF8")));
			List<String[]> lines = new LinkedList<String[]>();
			
			//output for each sentence
			for(MultiClassifiedSentence classifiedSentence : classifiedSentences) {
				
				Set<ILabel> predictions = classifiedSentence.getPredictions();
				StringBuffer multiCatLabelsb = new StringBuffer();
				for(ILabel label : predictions) {
					String labelNo = label.toString();
					String categoryLabel = svmLabelAndCategoryMappingMap.get(labelNo);
					categoryLabel = categoryLabel==null?"0":categoryLabel;
					multiCatLabelsb.append(categoryLabel).append(",");
				}
				
				String multiCatLabel = multiCatLabelsb.toString();
				if( multiCatLabel.length() == 0)
					multiCatLabel = "0";
				else
					multiCatLabel = multiCatLabel.substring(0, multiCatLabel.length() - 1);
				lines.add(new String[] { multiCatLabel, classifiedSentence.getText()});
			}
				
			writer.writeAll(lines);
			writer.flush();
			writer.close();
			
			log(LogLevel.INFO, "Done writing prediciton results");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getPredicitionsString(Set<ILabel> predictions) {
		StringBuilder stringBuilder = new StringBuilder();
		for(ILabel label : predictions) {
			stringBuilder.append(label + ",");
		}
		String result = stringBuilder.toString();
		if( result.length() == 0)
			return result; 
		else
			return result.substring(0, result.length() - 1);
	}

}
