package edu.arizona.biosemantics.micropie.io.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.arizona.biosemantics.micropie.io.FileReaderUtil;
import edu.arizona.biosemantics.micropie.model.XmlModelFile;

public class DescriptionOnlyToXML {
	
	private XmlFileConverter xmlCreator = new XmlFileConverter();

	/*
	 * example:
author: Fernald
year: 1950
title: Gray's Manual of Botany
genus name:  Rosaceae Linnaeus, 1735
species name:  Rosaceae Linnaeus, 1735
morphology: #Plants with regular flowers, numerous (rarely few) distinct stamens inserted on the calyx, and 1-many carpels, which are quite distinct, 
or (in the second tribe) united and combined with the calyx-tube.#
	 */
	public String completeETCInput(String description) {
		StringBuffer sb = new StringBuffer();
		sb.append("author: unknown\n");
		sb.append("year: 2018\n");
		sb.append("title: test\n");
		sb.append("genus name: G Test\n");
		sb.append("species name: S Test\n");
		sb.append("morphology: #");
		sb.append(description);
		sb.append("#");
		return sb.toString();
	}
	
	
	public void generateForFile(String file, String outputXML) {
		String content = FileReaderUtil.readFile(file);
		String etcInput = completeETCInput(content);
		XmlModelFile xmlFile = xmlCreator.createXmlModelFile(etcInput, "DiscpritionOnlyToXML");
		//System.out.println(xmlFile.getXML());
		
		FileWriter fw;
		try {
			fw = new FileWriter(outputXML);
			fw.write(xmlFile.getXML());
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void generateDataSet(String inputFolder, String outputFolder) {
		File inputFolderFile = new File(inputFolder);
		File[] allFiles = inputFolderFile.listFiles();
		for(File aFile:allFiles) {
			if(aFile.getName().endsWith("txt")) {
				String outputXMLFile = outputFolder+"/"+aFile.getName().replace(".txt", ".xml");
				generateForFile(aFile.getAbsolutePath(),outputXMLFile);
			}
		}
		
	}
	
	public static void main(String[] args) {
		DescriptionOnlyToXML dotXML = new DescriptionOnlyToXML();
		//String etcInput = dotXML.completeETCInput("content");
		//XmlModelFile xmlFile = dotXML.xmlCreator.createXmlModelFile(etcInput, "DiscpritionOnlyToXML");
		//System.out.println("xml:\n"+xmlFile.getXML());
		dotXML.generateDataSet("F:\\MicroPIE\\datasets\\vikas\\AllFirmicutes.Jul2017.formattedforETC", "F:\\\\MicroPIE\\\\datasets\\\\vikas\\AllFirmicutes.Jul2017.formattedforETCXML");
	}
}
