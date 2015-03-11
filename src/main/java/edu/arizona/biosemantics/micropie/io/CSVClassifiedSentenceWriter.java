package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;

public class CSVClassifiedSentenceWriter implements IClassifiedSentenceWriter {

	private OutputStream outputStream;
	private InputStream inputStream;

	private Map<String, String> svmLabelAndCategoryMappingMap;

	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	/**
	 * @param inputStream to read SVMLabelSubcategoryMapping.txt
	 */	
	
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public Map<String, String> readSVMLabelAndCategoryMapping() throws IOException {
		CSVReader readerOfSVMLabelAndCategoryMapping = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
		List<String[]> linesOfSVMLabelAndCategoryMapping = readerOfSVMLabelAndCategoryMapping.readAll();
		
		svmLabelAndCategoryMappingMap = new HashMap<String, String>();
		for(String[] lineOfSVMLabelAndCategoryMapping : linesOfSVMLabelAndCategoryMapping) {
			if ( lineOfSVMLabelAndCategoryMapping.length > 2 ) {
				System.out.println("lineOfSVMLabelAndCategoryMapping.toString():" + lineOfSVMLabelAndCategoryMapping.toString());
				System.out.println("lineOfSVMLabelAndCategoryMapping[1]::" + lineOfSVMLabelAndCategoryMapping[0]);
				System.out.println("lineOfSVMLabelAndCategoryMapping[1]::" + lineOfSVMLabelAndCategoryMapping[1]);
				svmLabelAndCategoryMappingMap.put(lineOfSVMLabelAndCategoryMapping[0],lineOfSVMLabelAndCategoryMapping[1]);
			}	
		}
		return svmLabelAndCategoryMappingMap;
	}	
	
	@Override
	public void write(List<MultiClassifiedSentence> classifiedSentences) throws Exception {
		log(LogLevel.INFO, "Writing prediciton results...");
		
		
		
		CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF8")));
		List<String[]> lines = new LinkedList<String[]>();
		for(MultiClassifiedSentence classifiedSentence : classifiedSentences) {
			String categoryLabel = "";
			String predictions = getPredicitionsString(classifiedSentence.getPredictions());
			String[] predictionList = predictions.split(",");
			
			
			if ( predictionList.length == 1 ) {
				System.out.println("predictionsList.length::" + predictionList.length);
				System.out.println("predictions::" + predictions);
				for (Map.Entry<String, String> entry : svmLabelAndCategoryMappingMap.entrySet()) {
					// System.out.println("Key : " + entry.getKey() + " Value : "
					// 	+ entry.getValue());
					
					if ( predictions.equals(entry.getKey()) ) {
						System.out.println("svmLabelAndCategoryMappingMap.getKey()::" + entry.getKey());
						System.out.println("svmLabelAndCategoryMappingMap.getValue()::" + entry.getValue());
						// svmLabelAndCategoryMappingMap.getKey()::31
						// svmLabelAndCategoryMappingMap.getValue()::2.11
						categoryLabel = entry.getValue();
					}
					
				}				
			} else {
				for (String prediction : predictionList ) {
					System.out.println("predictionsList.length::" + predictionList.length);
					System.out.println("prediction::" + prediction);
					for (Map.Entry<String, String> entry : svmLabelAndCategoryMappingMap.entrySet()) {
						// System.out.println("Key : " + entry.getKey() + " Value : "
						// 	+ entry.getValue());
						
						if ( prediction.equals(entry.getKey()) ) {
							categoryLabel += entry.getValue() + ",";
						}	
					}				
				}
			}

			
			if ( categoryLabel.equals("") ) {
				categoryLabel = "0"; // no category
			} // else {
				// 1.1,2.3,3.7, => 1.1,2.3,3.7
				// will \"1.1,2.3,3.7 matters??? => not sure\"
				// categoryLabel = categoryLabel.substring(0, categoryLabel.length()-1);
			// }
			
			
			// lines.add(new String[] { getPredicitionsString(classifiedSentence.getPredictions()), 
			// 		classifiedSentence.getSentence().getText()});
			lines.add(new String[] { categoryLabel, 
					classifiedSentence.getSentence().getText()});
			
		}
			
		writer.writeAll(lines);
		writer.flush();
		writer.close();
		log(LogLevel.INFO, "Done writing prediciton results");
	}

	private String getPredicitionsString(Set<ILabel> predictions) {
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
