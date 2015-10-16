package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;


/**
 * CSVSentenceReader reads Sentences from a CSV-like InputStream
 * @author rodenhausen
 */
public class CSVSentenceReader implements ISentenceReader {
	
	private InputStream inputStream;
	private OutputStream outputStream;
	private Map<String, ILabel> categoryCodeLabelMap;
	
	/**
	 * @param inputStream to read from
	 */
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	
	/**
	 * @param inputStream to read from
	 */
	public void setInputStream(String inputFile) {
		try {
			this.inputStream = new FileInputStream(inputFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	public void setCategoryCodeLabelMap(Map<String, ILabel> categoryCodeLabelMap) {
		this.categoryCodeLabelMap = categoryCodeLabelMap;
	}

	@Override
	public List<RawSentence> read() throws IOException {
		log(LogLevel.INFO, "Reading sentences...");
		List<RawSentence> result = new LinkedList<RawSentence>();
		
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
	    List<String[]> lines = reader.readAll();
		for(String[] line : lines) {
			ILabel svmLabel = categoryCodeLabelMap.get(line[0]);
			//if(svmLabel==null) svmLabel = Label.c0;		
			
			result.add(new RawSentence(line[5], svmLabel));
			
		}
		reader.close();
		log(LogLevel.INFO, "Done reading sentences...");
		return result;
	}
	
	
	
	public List<RawSentence> readAdditionalUSPInputs() throws IOException {
		log(LogLevel.INFO, "Reading additional USP input sentences...");
		List<RawSentence> result = new LinkedList<RawSentence>();
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
	    List<String[]> lines = reader.readAll();
		for(String[] line : lines) {
			// System.out.println("line[0]::" + line[0]);
			// System.out.println("line[1]::" + line[1]);
			if (line[0].equals("")) {
				line[0] = "0";
			}
			result.add(new RawSentence(line[1], Label.getEnum(line[0])));

		}
		reader.close();
		log(LogLevel.INFO, "Done reading additional USP input sentences...");
		return result;
	}
	
	// readSentenceList
	public List<RawSentence> readSentenceList(){
		log(LogLevel.INFO, "Reading source sentences...");
		List<RawSentence> result = new LinkedList<RawSentence>();
		CSVReader reader;
		try {
			reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
			 List<String[]> lines = reader.readAll();
				for(String[] line : lines) {
					ILabel svmLabel = categoryCodeLabelMap.get(line[0]);
					//if(svmLabel==null) svmLabel = Label.c0;		
					
					result.add(new RawSentence(line[5], svmLabel));
				}
				reader.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	   
		log(LogLevel.INFO, "Done reading source sentences...");
		return result;
	}
	
	/**
	 * two column sentences:
	 *  col 1: category
	 *  col 2: sentence
	 * @return
	 * @throws IOException
	 */
	public List<RawSentence> readTwoColumnSentenceList() throws IOException {
		List<RawSentence> result = new LinkedList<RawSentence>();
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
	    List<String[]> lines = reader.readAll();
		for(String[] line : lines) {
			ILabel svmLabel = categoryCodeLabelMap.get(line[0]);
			//System.out.println(line[1]+" "+svmLabel);
			//if(svmLabel==null) svmLabel = Label.c0;		
			
			result.add(new RawSentence(line[1], svmLabel));
		}
		reader.close();
		return result;
	}
	
	
	/**
	 * One column sentences:
	 *  col 1: sentence
	 * @return
	 * @throws IOException
	 */
	public List<RawSentence> readOneColumnSentenceList(){
		List<RawSentence> result = new LinkedList<RawSentence>();
		CSVReader reader;
		try {
			reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
			List<String[]> lines = reader.readAll();
			for(String[] line : lines) {
				result.add(new RawSentence(line[0]));
			}
			reader.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	  
		return result;
	}
	
	
	public void readTaxonomicDescAndWriteToSingleTxt(String outputFileName) throws IOException {
		log(LogLevel.INFO, "Reading txonomic descriptions...");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
		
		StringBuilder filteredLineStringBuilder = new StringBuilder();
		String thisLine;
		
		String blockString = "";
		
		while ((thisLine = br.readLine()) != null) { // while loop begins here
			// System.out.println(thisLine);	
			
			if ( thisLine.length() > 0 ) {
				/*
				if(  thisLine.contains("Taxon name:") ) {
					System.out.println("Taxon Name::" + thisLine);
					blockString = "";
				}
				
				if ( ! thisLine.contains("Taxon name:") ) {
					blockString += thisLine + "\n";
				} else {
					System.out.println("blockString::" + blockString);
					
				}
				*/
				
				
				if (thisLine.contains("Description:")) {
					if (thisLine.substring(0, 12).equals("Description:")) {
						// System.out.println(thisLine.substring(13));
						filteredLineStringBuilder.append(thisLine.substring(13) + " ");
					}
				} else {
					if ( ! thisLine.contains("Taxon name:")) {
						filteredLineStringBuilder.append(thisLine + " ");
					}
				}

			}
		} // end while
		
		
		/*
		try (PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(outputFileName, true)))) {
			out.println(filteredLineStringBuilder);
		} catch (IOException e) {
			// exception handling left as an exercise for the reader
		}
		*/		
				
		br.close();

		try {
			Element treatment = new Element("treatment");
			
			Document doc = new Document(treatment);
			// or
			// Document doc = new Document();
			// doc.setRootElement(treatment);
			
			Element taxon_identification = new Element("taxon_identification");
			taxon_identification.setAttribute(new Attribute("status", "ACCEPTED"));
			taxon_identification.addContent(new Element("genus_name").setText("Aus"));
			taxon_identification.addContent(new Element("species_name").setText("bus"));
			
			doc.getRootElement().addContent(taxon_identification);
		 
			Element description = new Element("description");
			description.setAttribute(new Attribute("type", "morphology"));
			description.addContent(filteredLineStringBuilder.toString());

			doc.getRootElement().addContent(description);

		 
			// new XMLOutputter().output(doc, System.out);
			XMLOutputter xmlOutput = new XMLOutputter();
		 
			// display nice nice
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(doc, new FileWriter("123.xml"));
		 
			System.out.println("File Saved!");
		} catch (IOException io) {
			System.out.println(io.getMessage());
		}
		
		
		log(LogLevel.INFO, "Done reading taxonomic descriptions...");
	}
	
	
	public void categoryStat() throws IOException {
		log(LogLevel.INFO, "Calculating sentences labels...");

		Map<String, Integer> categoryStat = new HashMap<String,Integer>();
		
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
	    List<String[]> lines = reader.readAll();
		for (String[] line : lines) {
			// result.add(new Sentence(line[5], Label.getEnum(line[0])));
			String key = line[0];
			int value = 1;
			if(categoryStat.containsKey(key)){
				value +=  categoryStat.get(key);
			}
			categoryStat.put(key,value);
		}	
	    reader.close();
	    
	    Iterator it = categoryStat.entrySet().iterator();
	    while (it.hasNext()) {
	    	Map.Entry pairs = (Map.Entry)it.next();
	    	System.out.println(pairs.getKey() + " = " + pairs.getValue());
	    	it.remove(); // avoids a ConcurrentModificationException
	    }
	    
	    
	    log(LogLevel.INFO, "Done calculating sentences labels...");
	}
	


	public void splitCompoundCategory() throws IOException {
		log(LogLevel.INFO, "Spliting compound category...");

		
		
		CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF8")));		
		List<String[]> outputLines = new LinkedList<String[]>();
		
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
	    List<String[]> lines = reader.readAll();
		for (String[] line : lines) {
			String label = line[0];
			if (label.contains(",")) {
				String[] labelArray = label.split(",");
				for (int i = 0; i < labelArray.length; i++) {
					String[] tempLine = line.clone();
					// System.out.println("labelArray[i]::" + labelArray[i]);
					// System.out.println("line[0]::Before::" + line[0]);
					// System.out.println("tempLine[0]::Before::" + tempLine[0]);
					tempLine[0] = labelArray[i];
					// System.out.println("tempLine[0]::After::" + tempLine[0]);
					outputLines.add(tempLine);
					
				}	
			} else {
				outputLines.add(line);
			}
		}	
	    reader.close();
		
		//write
		writer.writeAll(outputLines);
		writer.flush();
		writer.close();	
	    
	    log(LogLevel.INFO, "Done spliting compound category...");
	}
	
	

	
	
	public void csvToXls(String outputFileName) throws IOException {
		log(LogLevel.INFO, "Transferring csv to xls format...");
		
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
	    List<String[]> lines = reader.readAll();
	    reader.close();

		try {
			FileOutputStream fileOut = new FileOutputStream(outputFileName);
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet worksheet = workbook.createSheet("sheet1");

			for (int i = 0; i < lines.size(); i++) {
				HSSFRow rowContent = worksheet.createRow(i); // create row content
				String[] line = lines.get(i);
				for (int j = 0; j < line.length; j++) {
					HSSFCell cellContent = rowContent.createCell(j);
					cellContent.setCellValue(line[j]);
				}
			}
			workbook.write(fileOut);
			fileOut.flush();
			fileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	    
		
		log(LogLevel.INFO, "Done transferrring csv to xml format...");
	}	
	

	
}
