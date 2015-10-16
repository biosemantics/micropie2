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

	private String predictionFile;

	/**
	 * in the file, the fields are:
	 * categoryNo, categoryLabel, categoryCode
	 * "1","1.1","%G+C"
	 */
	private Map<ILabel, String> categoryLabelCodeMap;

	
	public void setCategoryLabelCodeMap(Map<ILabel, String> categoryLabelCodeMap) {
		this.categoryLabelCodeMap = categoryLabelCodeMap;
	}
	
	
	/**
	 * specify where to store the prediction file
	 * @param outputFile
	 */
	public void setPredictionFile(String predictionFile) {
		this.predictionFile = predictionFile;
	}
	

	
	
	@Override
	/**
	 * output the results in a CSV file
	 * Format: categoryCode,sentence
	 * 
	 */
	public void write(List<MultiClassifiedSentence> classifiedSentences){
		log(LogLevel.INFO, "Writing prediciton results...");
 
		CSVWriter writer = null;
		try {
			writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(predictionFile,true), "UTF8")));
			List<String[]> lines = new LinkedList<String[]>();
			
			//output for each sentence
			for(MultiClassifiedSentence classifiedSentence : classifiedSentences) {
				
				//Characterlabel
				Set<ILabel> predictions = classifiedSentence.getPredictions();				
				Set<ILabel> categories = classifiedSentence.getCategories();
				
				lines.add(new String[] {categories.toString(),
						predictions.toString(),
						classifiedSentence.getText()});
				/*
				StringBuffer multiCharachterLabelsb = new StringBuffer();
				for(ILabel label : predictions) {
					//String labelNo = label.toString();
					//Label labelNo = label.toString();
					//String categoryLabel = svmLabelAndCategoryMappingMap.get(labelNo);
					String characterCode = categoryLabelCodeMap.get(label);
					characterCode = characterCode==null?"0":characterCode;
					multiCharachterLabelsb.append(characterCode).append(",");
				}
				
				String multiCatLabel = multiCharachterLabelsb.toString();
				if( multiCatLabel.length() == 0)
					multiCatLabel = "0";
				else
					multiCatLabel = multiCatLabel.substring(0, multiCatLabel.length() - 1);
				
				//category label
				//Characterlabel
				
				StringBuffer multiCategoryLabelsb = new StringBuffer();
				for(ILabel label : categories) {
					multiCharachterLabelsb.append(label).append(",");
				}
				if( multiCategoryLabelsb.length() == 0) multiCategoryLabelsb.append("0,");
				
				lines.add(new String[] {multiCategoryLabelsb.substring(0, multiCategoryLabelsb.length() - 1),
						multiCatLabel,
						classifiedSentence.getText()});
						*/
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
	
	/*
	public void close(){
		try {
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

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
