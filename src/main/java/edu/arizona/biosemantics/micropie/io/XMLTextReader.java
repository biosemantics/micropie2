package edu.arizona.biosemantics.micropie.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
	
	
}
