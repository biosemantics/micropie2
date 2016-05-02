package edu.arizona.biosemantics.micropie.io;

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
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;


/**
 * for handling old schema of XML files
 * @author maojin
 *
 */
public class XMLTextReader implements ITextReader {

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
			xmlDocument = (Document) builder.build(new InputStreamReader(inputStream, "UTF-8"));
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
		String returnText = "";

		Element meta = rootNode.getChild("meta");
		Element source = meta.getChild("source");
		// Element title = source.getChild("title");
		String titleText = source.getChildText("title");
		
		/*
		if ( titleText != null && ! titleText.equals("") ) {
			
			String lastCharOfTitleText = titleText.substring(titleText.length()-1, titleText.length());
			if ( ! lastCharOfTitleText.equals(".") ) {
				titleText += ".";
			}
			
			System.out.println("Adding title:" + titleText);
			returnText += titleText + " ";
		}
		*/
		
		String text = rootNode.getChildText("description");
		Element desc = rootNode.getChild("description");
		String descType = desc.getAttributeValue("type");
		
		// System.out.println("descType:" + descType);
		// System.out.println("text:" + text);
		if(text != null && descType.equals("morphology")) {  
			returnText += text;
			return returnText;
		}	
		return null;
		//throw new Exception("Could not find a description");
		
		
	}
	
	/**
	 * read a TaxonTextFile
	 * @return
	 */
	public TaxonTextFile readFile(){
		if(rootNode==null) return null;
		
		TaxonTextFile taxonFile = new TaxonTextFile();
		String taxon = this.getTaxon();
		taxonFile.setTaxon(taxon);
		String family = this.getFamily();
		taxonFile.setFamily(family);
		String genus = this.getGenus();
		taxonFile.setGenus(genus);
		String species = this.getSpecies();
		taxonFile.setSpecies(species);
		String strain_number = this.getStrain_number();
		taxonFile.setStrain_number(strain_number);
		String the16SrRNAAccessionNumber = this.get16SrRNAAccessionNumber();
		taxonFile.setThe16SrRNAAccessionNumber(the16SrRNAAccessionNumber);
		
		return taxonFile;
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

	
	// add on March 07, 2015 Saturday
	// 16S rRNA accession #
	// Family
	// Genus
	// Species
	// Strain
	
	public String getFamily(){
		Element taxon_identification = rootNode.getChild("taxon_identification");
		List<Element> taxon_nameListOfElement = taxon_identification.getChildren("taxon_name");
		String familyName = "";
		for(Element taxon_nameElement : taxon_nameListOfElement) {
			String rank = taxon_nameElement.getAttributeValue("rank");
			if( rank.equals("family")) {
				familyName = taxon_nameElement.getText();
			}
		}
//		if(familyName != null) {
//			System.out.println("familyName:" + familyName);
			return familyName;
//		}	
//		throw new Exception("Could not find a family name");
	}	
	
	
	
	public String getGenus(){
		Element taxon_identification = rootNode.getChild("taxon_identification");
		Element genusNameEl = taxon_identification.getChild("genus_name");
		String genusName = null;
		if(genusNameEl!=null){
			genusName = genusNameEl.getText();
		}
		return genusName;
	}		
	
	
	/*
	public String getGenus(){
		Element taxon_identification = rootNode.getChild("taxon_identification");
		List<Element> taxon_nameListOfElement = taxon_identification.getChildren("taxon_name");
		String genusName = "";
		for(Element taxon_nameElement : taxon_nameListOfElement) {
			String rank = taxon_nameElement.getAttributeValue("rank");
			if( rank.equals("genus")) {
				genusName = taxon_nameElement.getText();
			}
		}
//		if(genusName != null) {
//			System.out.println("genusName:" + genusName);
			return genusName;
//		}	
		//throw new Exception("Could not find a genus name");
	}	*/	

	public String getSpecies(){
		Element taxon_identification = rootNode.getChild("taxon_identification");
		Element speciesNameEl = taxon_identification.getChild("species_name");
		String speciesName = null;
		if(speciesNameEl!=null){
			speciesName = speciesNameEl.getText();
		}
		return speciesName;
	}	
	
	/*
	public String getSpecies(){
		Element taxon_identification = rootNode.getChild("taxon_identification");
		List<Element> taxon_nameListOfElement = taxon_identification.getChildren("taxon_name");
		String speciesName = "";
		for(Element taxon_nameElement : taxon_nameListOfElement) {
			String rank = taxon_nameElement.getAttributeValue("rank");
			if( rank.equals("species")) {
				speciesName = taxon_nameElement.getText();
			}
		}
//		if(speciesName != null) {
//			System.out.println("speciesName:" + speciesName);
			return speciesName;
//		}	
		//throw new Exception("Could not find a species name");
	}	*/

	public String getStrain_number() {
		Element taxon_identification = rootNode.getChild("taxon_identification");
		List<Element> strain_numberListOfElement = taxon_identification.getChildren("strain_number");
		String strain_number = "";
		for(Element strain_numberElement : strain_numberListOfElement) {
			strain_number = strain_numberElement.getText();
			
			// will we add "equivalent_strain_numbers"
			// String equivalent_strain_numbers = strain_numberElement.getAttributeValue("equivalent_strain_numbers");
			// strain_number += ";" + equivalent_strain_numbers;
			
		}
//		if(strain_number != null) {
//			System.out.println("strain_number:" + strain_number);
//			return strain_number;
//		} // else {
		 	return strain_number;
		// }
		//throw new Exception("Could not find a strain number");
	}
	
	
	public String get16SrRNAAccessionNumber(){
		Element taxon_identification = rootNode.getChild("taxon_identification");
		List<Element> strain_numberListOfElement = taxon_identification.getChildren("strain_number");
		String the16SrRNAAccessionNumber = "";
		for(Element strain_numberElement : strain_numberListOfElement) {
			the16SrRNAAccessionNumber = strain_numberElement.getAttributeValue("accession_number_16s_rrna");	
		}
//		if(the16SrRNAAccessionNumber != null) {
//			System.out.println("the16SrRNAAccessionNumber:" + the16SrRNAAccessionNumber);
//			return the16SrRNAAccessionNumber;
//		} // else {
		 return the16SrRNAAccessionNumber;
		// }
		//throw new Exception("16S rRNA Accession Number");
	}	
	
	
	public TaxonTextFile readTaxonFile(File inputFile) {
		XMLTextReader textReader = new XMLTextReader();
		textReader.setInputStream(inputFile);
		if(textReader.isNew()){
			textReader = new XMLNewSchemaTextReader();
			textReader.setInputStream(inputFile);
		}
		TaxonTextFile taxonFile = textReader.readFile();
		taxonFile.setTaxon(taxonFile.getGenus()+" "+taxonFile.getSpecies());
		
		
		String text = textReader.read();
		taxonFile.setInputFile(null);
		taxonFile.setText(text);
		taxonFile.setXmlFile(inputFile.getName());
		
		return taxonFile;
	}
	// New Schema 2:: 141111	

	/**
	 * detect whether it is new schema.
	 * @return
	 */
	public boolean isNew() {
		//System.out.println(rootNode);
		//System.out.println(rootNode.getNamespace().getPrefix());
		//if("bio:treatment".equals(rootNode.getName())) return true;
		if(rootNode==null||!"".equals(rootNode.getNamespace().getPrefix())) return true;
		return false;
	}
	
	
	public static void main(String[] args){
		String inputFile = "F:\\MicroPIE\\datasets\\carrine_bacteria\\input1\\inputForm-0.xml";
		XMLTextReader tReader = new XMLTextReader();
		tReader.setInputStream(inputFile);
		String text = tReader.read();
		System.out.println(text);
	}
	
}
