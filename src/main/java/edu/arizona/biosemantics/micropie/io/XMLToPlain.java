package edu.arizona.biosemantics.micropie.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.arizona.biosemantics.micropie.model.TaxonTextFile;

public class XMLToPlain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inputFolder = "F:/MicroPIE/datasets/GutsMicrobiomes";
		String outputFile = "F:/MicroPIE/datasets/GutMicro_MicroPIEWebFormat.txt";
		
		XMLToPlain xmlToPlain = new XMLToPlain();
		xmlToPlain.fromXMLToPlain(inputFolder, outputFile);
	}

	
	public void fromXMLToPlain(String inputFolder, String outputFile){
		
		
		try {
			FileWriter fw = new FileWriter(outputFile);
			
			
			File folder = new File(inputFolder);
			
			File[] files = folder.listFiles();
			
			for(File file:files){
				XMLTextReader textReader = new XMLNewSchemaTextReader();
				textReader.setInputStream(file);
				TaxonTextFile taxonFile = textReader.readFile();
				
				String text = textReader.read();
				
				fw.write("author: "+taxonFile.getAuthor());
				fw.write("\n");
				fw.write("year: "+taxonFile.getYear());
				fw.write("\n");
				fw.write("title: "+taxonFile.getTitle()+"-"+taxonFile.getStrain_number());
				fw.write("\n");
				fw.write("genus name:"+taxonFile.getGenus());
				fw.write("\n");
				fw.write("species name:"+taxonFile.getSpecies());
				fw.write("\n");
				fw.write("morphology:"+text);
				fw.write("\n");
				fw.write("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
