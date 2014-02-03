package edu.arizona.biosemantics.micropie.io;

import java.io.IOException;
import java.io.InputStream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class XMLTextReader implements ITextReader {

	private InputStream inputStream;
	private Element rootNode;

	/**
	 * @param inputStream to read from
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	public void setInputStream(InputStream inputStream) throws JDOMException, IOException {
		this.inputStream = inputStream;
		
		SAXBuilder builder = new SAXBuilder();
		Document xmlDocument = (Document) builder.build(inputStream);
		rootNode = xmlDocument.getRootElement();
	}
	
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
}
