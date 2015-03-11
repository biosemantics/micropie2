package edu.arizona.biosemantics.micropie.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class XMLTextReader implements ITextReader {

	private Element rootNode;
	private InputStream inputStream;
	/**
	 * @param inputStream to read from
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	public void setInputStream(InputStream inputStream) throws JDOMException, IOException {		
		SAXBuilder builder = new SAXBuilder();
		Document xmlDocument = (Document) builder.build(new InputStreamReader(inputStream, "UTF8"));
		rootNode = xmlDocument.getRootElement();
	}
	

	// New Schema 2:: 141111
	@Override
	public String read() throws Exception {
		String returnText = "";

		Element meta = rootNode.getChild("meta");
		Element source = meta.getChild("source");
		// Element title = source.getChild("title");
		String titleText = source.getChildText("title");
		
		if ( titleText != null && ! titleText.equals("") ) {
			
			String lastCharOfTitleText = titleText.substring(titleText.length()-1, titleText.length());
			if ( ! lastCharOfTitleText.equals(".") ) {
				titleText += ".";
			}
			
			System.out.println("Adding title:" + titleText);
			returnText += titleText + " ";
		}
		
		String text = rootNode.getChildText("description");
		Element desc = rootNode.getChild("description");
		String descType = desc.getAttributeValue("type");
		
		// System.out.println("descType:" + descType);
		// System.out.println("text:" + text);
		if(text != null && descType.equals("morphology")) {  
			System.out.println("text:" + text);
			returnText += text;
			return returnText;
		}	
		throw new Exception("Could not find a description");
		
		
	}

	public String getTaxon() throws Exception {
		// String taxon = rootNode.getChildText("taxon_name");
		
		
		//<taxon_identification status="ACCEPTED">
		//  <taxon_name rank="genus">Leeuwenhoekiella</taxon_name>
		//  <strain_number equivalent_strain_numbers="ATCC 19326">LMG 1345</strain_number>
		//</taxon_identification>
		
		Element taxon_identification = rootNode.getChild("taxon_identification");
		
		List<Element> taxon_nameListOfElement = taxon_identification.getChildren("taxon_name");
		
		String taxon = "";
		for(Element taxon_nameElement : taxon_nameListOfElement) {
			String rank = taxon_nameElement.getAttributeValue("rank");
			
			
			if( rank.equals("genus")) {
				taxon += taxon_nameElement.getText();
			}
			
			if( rank.equals("species")) {
				taxon += " " + taxon_nameElement.getText();
			}
			
		}
		
	
		
		if(taxon != null) {
			System.out.println("taxon:" + taxon);
			return taxon;
		}	
		throw new Exception("Could not find a taxon name");
	}

	
	// add on March 07, 2015 Saturday
	// 16S rRNA accession #
	// Family
	// Genus
	// Species
	// Strain
	
	public String getFamily() throws Exception {
		Element taxon_identification = rootNode.getChild("taxon_identification");
		List<Element> taxon_nameListOfElement = taxon_identification.getChildren("taxon_name");
		String familyName = "";
		for(Element taxon_nameElement : taxon_nameListOfElement) {
			String rank = taxon_nameElement.getAttributeValue("rank");
			if( rank.equals("family")) {
				familyName = taxon_nameElement.getText();
			}
		}
		if(familyName != null) {
			System.out.println("familyName:" + familyName);
			return familyName;
		}	
		throw new Exception("Could not find a family name");
	}	
	
	public String getGenus() throws Exception {
		Element taxon_identification = rootNode.getChild("taxon_identification");
		List<Element> taxon_nameListOfElement = taxon_identification.getChildren("taxon_name");
		String genusName = "";
		for(Element taxon_nameElement : taxon_nameListOfElement) {
			String rank = taxon_nameElement.getAttributeValue("rank");
			if( rank.equals("genus")) {
				genusName = taxon_nameElement.getText();
			}
		}
		if(genusName != null) {
			System.out.println("genusName:" + genusName);
			return genusName;
		}	
		throw new Exception("Could not find a genus name");
	}		

	public String getSpecies() throws Exception {
		Element taxon_identification = rootNode.getChild("taxon_identification");
		List<Element> taxon_nameListOfElement = taxon_identification.getChildren("taxon_name");
		String speciesName = "";
		for(Element taxon_nameElement : taxon_nameListOfElement) {
			String rank = taxon_nameElement.getAttributeValue("rank");
			if( rank.equals("species")) {
				speciesName = taxon_nameElement.getText();
			}
		}
		if(speciesName != null) {
			System.out.println("speciesName:" + speciesName);
			return speciesName;
		}	
		throw new Exception("Could not find a species name");
	}	

	public String getStrain_number() throws Exception {
		Element taxon_identification = rootNode.getChild("taxon_identification");
		List<Element> strain_numberListOfElement = taxon_identification.getChildren("strain_number");
		String strain_number = "";
		for(Element strain_numberElement : strain_numberListOfElement) {
			strain_number = strain_numberElement.getText();
			
			// will we add "equivalent_strain_numbers"
			// String equivalent_strain_numbers = strain_numberElement.getAttributeValue("equivalent_strain_numbers");
			// strain_number += ";" + equivalent_strain_numbers;
			
		}
		if(strain_number != null) {
			System.out.println("strain_number:" + strain_number);
			return strain_number;
		}	
		throw new Exception("Could not find a strain number");
	}
	
	
	public String get16SrRNAAccessionNumber() throws Exception {
		Element taxon_identification = rootNode.getChild("taxon_identification");
		List<Element> strain_numberListOfElement = taxon_identification.getChildren("strain_number");
		String the16SrRNAAccessionNumber = "";
		for(Element strain_numberElement : strain_numberListOfElement) {
			the16SrRNAAccessionNumber = strain_numberElement.getAttributeValue("accession_number_16s_rrna");	
		}
		if(the16SrRNAAccessionNumber != null) {
			System.out.println("the16SrRNAAccessionNumber:" + the16SrRNAAccessionNumber);
			return the16SrRNAAccessionNumber;
		}	
		throw new Exception("Could not find a strain number");
	}	
	
	
	
	// New Schema 2:: 141111	
	
	/*
	// New schema
	@Override
	public String read() throws Exception {
		String returnText = "";

		Element meta = rootNode.getChild("meta");
		Element source = meta.getChild("source");
		// Element title = source.getChild("title");
		String titleText = source.getChildText("title");
		
		if ( titleText != null && ! titleText.equals("") ) {
			
			String lastCharOfTitleText = titleText.substring(titleText.length()-1, titleText.length());
			if ( ! lastCharOfTitleText.equals(".") ) {
				titleText += ".";
			}
			
			System.out.println("Adding title:" + titleText);
			returnText += titleText + " ";
		}
		
		String text = rootNode.getChildText("description");
		Element desc = rootNode.getChild("description");
		String descType = desc.getAttributeValue("type");
		
		// System.out.println("descType:" + descType);
		// System.out.println("text:" + text);
		if(text != null && descType.equals("morphology")) {  
			System.out.println("text:" + text);
			returnText += text;
			return returnText;
		}	
		throw new Exception("Could not find a description");
		
		
	}

	public String getTaxon() throws Exception {
		// String taxon = rootNode.getChildText("taxon_name");
		
		
		//<taxon_identification status="ACCEPTED">
		//<family_name>aaa</family_name>
		//<subfamily_name>bbb</subfamily_name>
		//<genus_name>ccc</genus_name>
		//<species_name>ddd</species_name>
		//<strain_name>Arc51T (=NBRC 100649T=DSM 18877T)</strain_name><strain_source>Arc51T (=NBRC 100649T=DSM 18877T)</strain_source>
		//</taxon_identification>
		
		Element taxon_identification = rootNode.getChild("taxon_identification");
		
		String taxon = "";
		taxon += taxon_identification.getChildText("genus_name");
		taxon += " ";
		taxon += taxon_identification.getChildText("species_name");
	
		
		if(taxon != null) {
			System.out.println("taxon:" + taxon);
			return taxon;
		}	
		throw new Exception("Could not find a taxon name");
	}
	// New Schema
	*/
	
	
	
	/*
	// Old Schema
	@Override
	public String read() throws Exception {
		String text = rootNode.getChildText("description");
		
		if(text != null) 
			return text;
		throw new Exception("Could not find a description");
	}

	public String getTaxon() throws Exception {
		String taxon = rootNode.getChildText("taxon_name");
		
		if(taxon != null) 
			return taxon;
		throw new Exception("Could not find a taxon name");
	}
	// Old Schema
	*/
	
}
