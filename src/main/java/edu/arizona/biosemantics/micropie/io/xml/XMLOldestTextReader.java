package edu.arizona.biosemantics.micropie.io.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.io.ITextReader;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;


/**
 * for handling old schema of XML files
 * @author maojin
 *
 */
public class XMLOldestTextReader implements ITextReader {

	private Element rootNode;
	private InputStream inputStream;
	/**
	 * @param inputStream to read from
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	public void setInputStream(InputStream inputStream) {		
		SAXBuilder builder = new SAXBuilder();
		Document xmlDocument;
		try {
			xmlDocument = (Document) builder.build(new InputStreamReader(inputStream, "UTF8"));
			rootNode = xmlDocument.getRootElement();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param inputStream to read from
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	public void setInputStream(String file) {	
		try {
			InputStream inputstream = new FileInputStream(file);
			this.setInputStream(inputstream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param inputStream to read from
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	public void setInputStream(File file) {	
		try {
			InputStream inputstream = new FileInputStream(file);
			this.setInputStream(inputstream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	

	// New Schema 2:: 141111
	@Override
	public String read(){
		
		String text = rootNode.getChildText("description");
		return text;
		//throw new Exception("Could not find a description");
		
		
	}
	
	
	public String getTaxon(){
		// String taxon = rootNode.getChildText("taxon_name");
		
		
		//<taxon_identification status="ACCEPTED">
		//  <taxon_name rank="genus">Leeuwenhoekiella</taxon_name>
		//  <strain_number equivalent_strain_numbers="ATCC 19326">LMG 1345</strain_number>
		//</taxon_identification>
		
		Element taxon_identification = rootNode.getChild("taxon_identification");
		
		String taxon = "";
		if(taxon_identification!=null){
			List<Element> taxon_nameListOfElement = taxon_identification.getChildren("taxon_name");
			
			
			for(Element taxon_nameElement : taxon_nameListOfElement) {
				String rank = taxon_nameElement.getAttributeValue("rank");
				
				
				if( rank.equals("genus")) {
					taxon += taxon_nameElement.getText();
				}
				
				if( rank.equals("species")&&taxon_nameElement.getText()!=null) {
					taxon += " " + taxon_nameElement.getText();
				}
				
			}
		
		}else{
			taxon = rootNode.getChildText("taxon_name");
		}
	
//		
//		if(taxon != null) {
//			System.out.println("taxon:" + taxon);
			return taxon;
//		}	
//		throw new Exception("Could not find a taxon name");
	}

	public TaxonTextFile readTaxonFile(File inputFile) {
		TaxonTextFile taxonFile = new TaxonTextFile();
		taxonFile.setText(this.read());
		return taxonFile;
	}
}
