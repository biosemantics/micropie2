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
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;

import edu.arizona.biosemantics.micropie.io.xml.XMLTextReader;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.nlptool.StringUtil;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;


/**
 * find the taxonFile for the matrix
 * @author maojin
 *
 */
public class MatrixComplementor {
	
	
	/**
	 * 
	 * @param matrixFile
	 * @param datasetFolder
	 * @param newMatrixFile
	 */
	public void complement(String matrixFile, String datasetFolder, String newMatrixFile){
		InputStream inputStream;
		try {			
			inputStream = new FileInputStream(matrixFile);
			
			//init all the file map
			Map<String, String> fileMapping = readAllFileMap(datasetFolder);
			
			CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF8")));
			CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newMatrixFile), "UTF8")));
			List<String[]> lineData = reader.readAll();
			String[] allFields = lineData.get(0);
			writer.writeNext((String[])ArrayUtils.addAll(new String[]{"XML file"},allFields));
			for(int row=1;row<lineData.size();row++){
				String[] taxonInfo = lineData.get(row);
				String fileKey = "";
				//Genus|Species|Strain
				for(int i=0;i<7;i++){
					if("Genus".equals(allFields[i])||"Species".equals(allFields[i])||"Strain".equals(allFields[i])){
						fileKey+=taxonInfo[i].trim();
					}
				}
				fileKey = UUID.fromString(fileKey).toString();
				
				String xmlFile = fileMapping.get(fileKey);
				writer.writeNext((String[])ArrayUtils.addAll(new String[]{xmlFile},taxonInfo));
				writer.flush();
			}
			reader.close();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	
	/**
	 * 
	 * @param datasetFolder
	 * @return
	 */
	private Map<String, String> readAllFileMap(String datasetFolder) {
		File datasetFile = new File(datasetFolder);
		File[] allFiles = datasetFile.listFiles();
		XMLTextReader xmlTextReader = new XMLTextReader();
		Map fileMap = new HashMap();
		for(File file:allFiles){
			try {
				xmlTextReader.setInputStream(new FileInputStream(file));
				String fileKey =  xmlTextReader.getGenus().trim();
				fileKey+=xmlTextReader.getSpecies();
				fileKey+=xmlTextReader.getStrain_number();
				
				fileMap.put( UUID.fromString(fileKey).toString(), file.getName());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return fileMap;
	}

}
