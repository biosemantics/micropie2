package edu.arizona.biosemantics.micropie.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.ValueFormatterUtil;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;




/**
 * output Markup XML files with character values
 * 
 * @author maojin
 *
 */
public class MarkupXMLWriter {
	private Set<TaxonTextFile> taxonFiles;
	private ValueFormatterUtil formatter = new ValueFormatterUtil();
	private String inputFolder;
	private String outputFolder;
	private Map<ILabel, String> labelCategoryNameMap;
	
	public void setTaxonFiles(Set<TaxonTextFile> taxonFiles) {
		this.taxonFiles = taxonFiles;
	}

	public void setInputFolder(String inputFolder) {
		this.inputFolder = inputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}
	
	public void setLabelCategoryNameMap(Map<ILabel, String> labelCategoryNameMap) {
		this.labelCategoryNameMap = labelCategoryNameMap;
	}

	/**
	 * generate output XML files that should be marked up with character values
	 */
	public void generateOutputXML(){
		Iterator<TaxonTextFile> taxonFileIter = taxonFiles.iterator();
		while(taxonFileIter.hasNext()){
			TaxonTextFile taxonFile = taxonFileIter.next();
			
			File inputFile = taxonFile.getInputFile();
			
			String outputXMLFile = this.outputFolder+"/"+inputFile.getName();
			
			outputMarkupXML(taxonFile, inputFile, outputXMLFile);
		}
	}

	
	/**
	 * output one markup XML File
	 * @param taxonFile
	 * @param inputFile
	 * @param outputXMLFile
	 */
	public void outputMarkupXML(TaxonTextFile taxonFile, File inputFile, String outputXMLFile) {
		int organId = 0;
		// TODO Auto-generated method stub
		SAXBuilder builder = new SAXBuilder();
		try {
			Document xmlDocument = (Document) builder.build(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
			Element rootNode = xmlDocument.getRootElement();
			Namespace xsins = rootNode.getNamespace("xsi");
			rootNode.setAttribute("schemaLocation","http://www.github.com/biosemantics https://raw.githubusercontent.com/biosemantics/schemas/master/semanticMarkupOutput.xsd", xsins);
			
			List<Sentence> sentences = taxonFile.getSentences();
			Map<Sentence, List> sentValues = taxonFile.getSentCharacterValues();
			
			
			//get the document    bio:treatment-->description
			Element descriptionEl = xmlDocument.getRootElement().getChild("description");
			descriptionEl.removeContent();//remove orginial text
			
			for(int sentId =0; sentId < sentences.size(); sentId++){
				Sentence sent = sentences.get(sentId);
				Element sentEL = new Element("statement");
				descriptionEl.addContent(sentEL);
				sentEL.setAttribute("id", "d0_s"+sentId);
				
				//text
				Element textEl = new Element("text");
				sentEL.addContent(textEl);
				textEl.setText(sent.getText());
				
				//biological_entity
				Element biological_entityEl = new Element("biological_entity");
				sentEL.addContent(biological_entityEl);
				//<biological_entity id="o1" name="flower" name_original="flowers" type="structure">
				biological_entityEl.setAttribute("id", "o" + organId++);
				biological_entityEl.setAttribute("name", "whole_organism");
				biological_entityEl.setAttribute("type", "structure");
				
				// <character is_modifier="true" name="architecture" value="regular" />
				List<CharacterValue> characterList = sentValues.get(sent);
				if(characterList!=null&&characterList.size()>0){
					for(CharacterValue charValue:characterList){
						Element characterEl = new Element("character");
						
						ILabel label = charValue.getCharacter();
						String charName = labelCategoryNameMap.get(label);
						if(charName == null) {
							//This happens with the three etc sample files
							//character: USP
							//label: USP
							System.out.println("character: " + charValue.getCharacter());
							System.out.println("label: " + label);
						} else {
							characterEl.setAttribute("name",charName);
							String value = formatter.format(charValue);
							characterEl.setAttribute("value",value);
							biological_entityEl.addContent(characterEl);
						}
					}
				}
			}
			
			// new XMLOutputter().output(doc, System.out);
			XMLOutputter xmlOutput = new XMLOutputter();

			// display nice nice
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(xmlDocument, new FileWriter(outputXMLFile));
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
//	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, JDOMException, IOException{
//		// TODO Auto-generated method stub
//		String inputFile = "C:\\Users\\maojin\\Desktop\\Fernald_Rosaceae_1950.xml";
//				SAXBuilder builder = new SAXBuilder();
//					Document xmlDocument = (Document) builder.build(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
//					Element rootNode = xmlDocument.getRootElement();
//					//List<Namespace> ns = rootNode.getNamespacesIntroduced();
//					List<Namespace> ns = rootNode.getAdditionalNamespaces();
//					//List<Namespace> ns = rootNode.getNamespacesInScope();
//					Namespace xsins = rootNode.getNamespace("xsi");
////					for(Namespace xsins:ns){
////						if(xsins.getPrefix().equals("xsi")){
//							//Namespace axsins = xsins.
//							System.out.println(xsins.getURI()+":"+xsins.getPrefix());
//							rootNode.setAttribute("schemaLocation", "http://www.energystar.gov/manageBldgs/req http://estar8.energystar.gov/ESES/ABS20/Schemas/ManageBuildingsRequest.xsd", xsins);
////						}
////						System.out.println(xsins.getURI()+":"+xsins.getPrefix());
//						
////					}
//					Namespace xsi = Namespace.getNamespace("xsi", 
//							"http://www.w3.org/2001/XMLSchema-instance");
//					rootNode.addNamespaceDeclaration(xsi);
//					
//					// new XMLOutputter().output(doc, System.out);
//					XMLOutputter xmlOutput = new XMLOutputter();
//
//					// display nice nice
//					xmlOutput.setFormat(Format.getPrettyFormat());
//					xmlOutput.output(xmlDocument, new FileWriter("C:\\Users\\maojin\\Desktop\\Fernald_Rosaceae_1950_2.xml"));
//	}
//	
}
