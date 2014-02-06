package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import au.com.bytecode.opencsv.CSVWriter;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;

public class CSVClassifiedSentenceWriter implements IClassifiedSentenceWriter {

	private OutputStream outputStream;
	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void write(List<MultiClassifiedSentence> classifiedSentences)
			throws Exception {
		log(LogLevel.INFO, "Writing prediciton results...");
		CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF8")));
		List<String[]> lines = new LinkedList<String[]>();
		for(MultiClassifiedSentence classifiedSentence : classifiedSentences) 
			lines.add(new String[] { getPredicitionsString(classifiedSentence.getPredictions()), 
					classifiedSentence.getSentence().getText()});
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
