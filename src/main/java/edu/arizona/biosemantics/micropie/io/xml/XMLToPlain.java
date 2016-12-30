package edu.arizona.biosemantics.micropie.io.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.arizona.biosemantics.micropie.io.FileReaderUtil;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.model.XmlModelFile;

public class XMLToPlain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inputFolder = "F:/MicroPIE/datasets/GutsMicrobiomes";
		String outputFile = "F:/MicroPIE/datasets/GutMicro_MicroPIEWebFormat.txt";
		
		//XMLToPlain xmlToPlain = new XMLToPlain();
		//xmlToPlain.fromXMLToPlain(inputFolder, outputFile);
		
		
		String plainInputFile = "F:/MicroPIE/datasets/2017/Firmicutes_NoGenomes_descriptions_122316.txt";
		String xmlFolder = "F:/MicroPIE/datasets/2017/Firmicutes_NoGenomes_descriptions_122316";
		XmlFileConverter xmlFileConverter = new XmlFileConverter();
		
		List<String> lines = FileReaderUtil.readFileLines(plainInputFile);
		//create the text xml files
		List<String> treatments = xmlFileConverter.getTreatmentTexts(lines);
		
		int i=0;
		for(String treatment:treatments){
			XmlModelFile xmlModelFile = xmlFileConverter.createXmlModelFile(treatment, "MicroPIE");
			
			try {
				FileWriter fw = new FileWriter(xmlFolder+"/"+i+++".xml");
				fw.write(xmlModelFile.getXML());
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
