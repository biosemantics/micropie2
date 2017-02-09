package edu.arizona.biosemantics.micropie.io.xml;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.common.taxonomy.Rank;
import edu.arizona.biosemantics.micropie.model.XmlModelFile;

/**
 * generate ETC files from text
 * 
 * @author maojin
 *
 */
public class XmlFileConverter extends XmlModelFileCreator {

	public String[] fields =  new String[] {"author", "year", "title", "doi", "full citation",
			/**/"order", "suborder", "superfamily", "family", "subfamily", "tribe", "subtribe", "genus", "subgenus", 
			"section", "subsection", "series", "species", "subspecies", "variety", "forma", "unranked",
			"strain number", "equivalent strain numbers", "accession number 16s rrna",
			"morphology", "phenology",  "habitat", "distribution" };
	
	protected BracketChecker bracketChecker = new BracketChecker();
	//protected String[] nameTypes = { "order", "suborder", "superfamily", "family", "subfamily", "tribe", "subtribe", "genus", "subgenus", 
	//		"section", "subsection", "series", "species", "subspecies", "variety", "forma", "unranked" };
	protected ArrayList<String> nameTypes = new ArrayList<String>();
	protected Set<String> allLabels = new HashSet<String>();
	private XmlNamespaceManager xmlNamespaceManager = new XmlNamespaceManager();
	
	
	/**
	 * parse XML files from text
	 * @param text
	 * @param operator
	 * @return
	 */
	public List<XmlModelFile> createXmlModelFiles(String text, String operator) {
		List<XmlModelFile> result = new LinkedList<XmlModelFile>();
		text = normalizeText(text);
		List<String> treatmentTexts = getTreatmentTexts(text);
		
		for(String treatmentText : treatmentTexts) {
			XmlModelFile modelFile = createXmlModelFile(treatmentText, operator);
			result.add(modelFile);
		}
		
		return result;
	}


	/**
	 * generate a list of files
	 * @param text
	 * @param operator
	 * @return
	 */
	public XmlModelFile createXmlModelFile(String text, String operator) {
		XmlModelFile modelFile = new XmlModelFile();
		nameTypes = new ArrayList<String>();
		
		//prepare data map
		Map<String, String> data = new HashMap<String, String>();
		String[] lines = text.split("\n");
		
		Iterator<String> lineIterator = Arrays.asList(lines).iterator();
		while(lineIterator.hasNext()) {
			String line = lineIterator.next();
			//System.out.println("line: "+ line);
		
			int colonIndex = line.indexOf(":");
			if(colonIndex == -1) {
				modelFile.appendError("Line format invalid. Need a ':' to seperate field name from its text: " + line);
				continue;
			}

			String key = line.substring(0, colonIndex).toLowerCase().trim();
			String value = line.substring(colonIndex + 1, line.length()).trim();
			//System.out.println("key/value " + key + " = " + value);
			
			for(String descriptionType : descriptionTypes) {
				if(descriptionType.equals(key)) {
					if(value.startsWith("#")) {
						StringBuilder valueBuilder = new StringBuilder();
						valueBuilder.append(value.replaceAll("(^#|#$)", "") + "\n"); 
						while(!line.endsWith("#") && lineIterator.hasNext()) {
							line = lineIterator.next();
							if(line.endsWith("#"))
								valueBuilder.append(line.replaceFirst("#$", "") + "\n");
							else
								valueBuilder.append(line + "\n");
						}
						value = valueBuilder.toString();
					}
				}
			}
			if(data.containsKey(key)) {
				modelFile.appendError("No duplicate fields are allowed in one treatment: " + key);
			}
			System.out.println("Put key "+ key + " = " + value);
			data.put(key, value.isEmpty()? null : value);
		}
		
		//System.out.println("data" + data.toString());
		
		
		for(String key : data.keySet()) {
			//if(!this.allLabels.contains(key)) {
			//	System.out.println("Don't know what " +key + " is.");
			//} else {
				if(key.toLowerCase().endsWith(" name"))	 
					nameTypes.add(key);
			//}
		}
	
		//check data required to generate error if necessary
		if(!data.containsKey("author") || data.get("author") == null || data.get("author").trim().isEmpty())
			modelFile.appendError("You need to provide an author");
		if(!data.containsKey("year") || data.get("year") == null || data.get("year").trim().isEmpty())
			modelFile.appendError("You need to provide a year");
		if(!data.containsKey("title") || data.get("title") == null || data.get("title").trim().isEmpty())
			modelFile.appendError("You need to provide a title");
		
		boolean nameValid = false;
		if(nameTypes.size()>0){
			for(String nameType : nameTypes) {
				nameValid = data.containsKey(nameType) && data.get(nameType) != null && !data.get(nameType).trim().isEmpty();
				if(nameValid)
					break;
			}
		}
		nameValid = nameValid || (!nameValid && data.containsKey("strain number") && data.get("strain number") != null && !data.get("strain number").trim().isEmpty()); 
		if(!nameValid)
			modelFile.appendError("You need to provide at least either a taxon rank or a strain");
		
		boolean descriptionValid = false;
		for(String descriptionType : descriptionTypes) {
			descriptionValid = data.containsKey(descriptionType) && data.get(descriptionType) != null && !data.get(descriptionType).trim().isEmpty();
			if(descriptionValid)
				break;
		}
		if(!descriptionValid)
			modelFile.appendError("You need to provide at least one of the description types: morphology, phenology, habitat, or distribution");
		

		//build xml
		Document xml = createXML(data, operator, modelFile);
		modelFile.setFileName(createFileName(data, modelFile));
		modelFile.setXML(new XMLOutputter(Format.getPrettyFormat()).outputString(xml));
		return modelFile;
	}
	
	private Document createXML(Map<String, String> data, String username, XmlModelFile modelFile) {
		
		Element treatment = new Element("treatment");
		/*root.addContent(new Element(article));
		Elements in which it is possible to set attributes:
		Attribute att1 = new Attribute("classe", "A1");
		article.setAttribute(att1);*/
		
		Document doc = new Document(treatment);
		Element meta = new Element("meta");
		treatment.addContent(meta);
		Element source = new Element("source");
		Element author = new Element("author");
		Element srcDate = new Element("date");
		Element title = new Element("title");
		source.addContent(author);
		source.addContent(srcDate);
		source.addContent(title);
		author.setText(data.get("author"));
		srcDate.setText(data.get("year"));
		title.setText(data.get("title"));
		meta.addContent(source);
		Element processedBy = new Element("processed_by");
		Element processor = new Element("processor");
		processedBy.addContent(processor);
		Element date = new Element("date");
		processor.addContent(date);
		String formattedDate = DateFormat.getDateInstance(DateFormat.FULL, Locale.US).format(new Date());
		date.setText(formattedDate);
		Element software = new Element("software");
		software.setAttribute("type", "Text Capture Input Generator");
		software.setAttribute("version", "1.0");
		processor.addContent(software);
		Element operator = new Element("operator");
		operator.setText(username);
		processor.addContent(operator);
		meta.addContent(processedBy);
		
		if(data.containsKey("doi") && data.get("doi") != null && !data.get("doi").trim().isEmpty()) {
			Element otherInfoOnMeta = new Element("other_info_on_meta");
			otherInfoOnMeta.setText(data.get("doi"));
			otherInfoOnMeta.setAttribute("type", "doi");
			meta.addContent(otherInfoOnMeta);
		}
		if(data.containsKey("full citation") && data.get("full citation") != null && !data.get("full citation").trim().isEmpty()) {
			Element otherInfoOnMeta = new Element("other_info_on_meta");
			otherInfoOnMeta.setText(data.get("full citation"));
			otherInfoOnMeta.setAttribute("type", "citation");
			meta.addContent(otherInfoOnMeta);
		}
		/*List<String> otherInfoOnMetas = new LinkedList<String>();
		  if(data.containsKey("doi") && data.get("doi") != null && !data.get("doi").trim().isEmpty()) {
			String otherInfoOnMeta = "doi: " + data.get("doi");
			otherInfoOnMetas.add(otherInfoOnMeta);
		}
		if(data.containsKey("full citation") && data.get("full citation") != null && !data.get("full citation").trim().isEmpty()) {
			String otherInfoOnMeta = "full citation: " + data.get("full citation");
			otherInfoOnMetas.add(otherInfoOnMeta);
		}
		
		if(!otherInfoOnMetas.isEmpty()) {
			for(String otherInfoOnMetaText : otherInfoOnMetas) {
				Element otherInfoOnMeta = new Element("other_info_on_meta");
				otherInfoOnMeta.setTextotherInfoOnMetaText));
				meta.addContent(otherInfoOnMeta);
			}
		}*/
		Element taxonIdentification = new Element("taxon_identification");
		treatment.addContent(taxonIdentification);
		Collections.sort(nameTypes, new Comparator<String>() {
			@Override
			public int compare(String a, String b) {
				a = a.replaceFirst(" name$", "");
				b = b.replaceFirst(" name$", "");
				try {
					Rank aRank = Rank.valueOf(a.toUpperCase());
					Rank bRank = Rank.valueOf(b.toUpperCase());
					return aRank.getId() - bRank.getId();
				} catch(Exception e) {
					return -Integer.MAX_VALUE;
				}
			}
		});
		System.out.println("nametype="+nameTypes.size());
		for(String nameType: nameTypes){
			System.out.println("nametype="+nameType);
			String rank = nameType.replace(" name", "");
			String nameString = data.get(nameType);
			nameString = nameString==null?"":nameString.trim();
			String name = nameString;
			String authority = null;
			String ndate = null;
			if(nameString.contains(" ")){
				name = nameString.substring(0, nameString.indexOf(" "));
				String authorityStr = nameString.substring(nameString.indexOf(" ")).trim();
				if(authorityStr.contains(",")){
					authority = authorityStr.substring(0, authorityStr.indexOf(",")).trim();
					ndate = authorityStr.substring(authorityStr.indexOf(",")+1).trim();
				}
			}
			Element element = new Element("taxon_name");
			taxonIdentification.addContent(element);
			element.setText(name);
			element.setAttribute("rank", rank);
			if(authority!=null) element.setAttribute("authority", authority);
			if(ndate!=null) element.setAttribute("date", ndate);
		}

		if(data.containsKey("strain number") && data.get("strain number") != null && !data.get("strain number").trim().isEmpty()||data.containsKey("accession number 16s rrna")) {
			Element element = new Element("strain_number");
			taxonIdentification.addContent(element);
			element.setText(data.get("strain number"));
			if(data.get("equivalent strain numbers") != null && !data.get("equivalent strain numbers").trim().isEmpty()) element.setAttribute("equivalent_strain_numbers", data.get("equivalent strain numbers"));
			if(data.get("accession number 16s rrna") != null && !data.get("accession number 16s rrna").trim().isEmpty()) element.setAttribute("accession_number_16s_rrna", data.get("accession number 16s rrna"));
		}

		taxonIdentification.setAttribute("status", "ACCEPTED");
		/*addAsElementIfExists(taxonIdentification, data, "class", "class_name", doc);
		addAsElementIfExists(taxonIdentification, data, "subclass", "subclass_name", doc);
		addAsElementIfExists(taxonIdentification, data, "order", "order_name", doc);
		addAsElementIfExists(taxonIdentification, data, "suborder", "suborder_name", doc);
		addAsElementIfExists(taxonIdentification, data, "superfamily", "superfamily_name", doc);
		addAsElementIfExists(taxonIdentification, data, "family", "family_name", doc);
		addAsElementIfExists(taxonIdentification, data, "subfamily", "subfamily_name", doc);
		addAsElementIfExists(taxonIdentification, data, "tribe", "tribe_name", doc);
		addAsElementIfExists(taxonIdentification, data, "subtribe", "subtribe_name", doc);
		addAsElementIfExists(taxonIdentification, data, "genus", "genus_name", doc);
		addAsElementIfExists(taxonIdentification, data, "subgenus", "subgenus_name", doc);
		addAsElementIfExists(taxonIdentification, data, "section", "section_name", doc);
		addAsElementIfExists(taxonIdentification, data, "subsection", "subsection_name", doc);
		addAsElementIfExists(taxonIdentification, data, "series", "series_name", doc);
		addAsElementIfExists(taxonIdentification, data, "subseries", "subseries_name", doc);
		addAsElementIfExists(taxonIdentification, data, "species", "species_name", doc);
		addAsElementIfExists(taxonIdentification, data, "subspecies", "subspecies_name", doc);
		addAsElementIfExists(taxonIdentification, data, "variety", "variety_name", doc);
		addAsElementIfExists(taxonIdentification, data, "subvarietas", "series_name", doc);
		addAsElementIfExists(taxonIdentification, data, "forma", "forma_name", doc);
		addAsElementIfExists(taxonIdentification, data, "subforma", "subforma_name", doc);
		addAsElementIfExists(taxonIdentification, data, "unranked", "unranked_epithet_name", doc);*/
		//addAsElementIfExists(taxonIdentification, data, "strain", "strain_name", doc);
		//addAsElementIfExists(taxonIdentification, data, "strain source", "strain_source", doc);

		for(String descriptionType : descriptionTypes) {
			if(data.containsKey(descriptionType)) {
				String descriptionText = data.get(descriptionType);
				if(descriptionText != null && !descriptionText.trim().isEmpty()) {
					String[] paragraphs = descriptionText.split("\n");
					for(String paragraph : paragraphs) {
						if(!paragraph.isEmpty()) {
							Element description = new Element("description");
							treatment.addContent(description);
							description.setAttribute("type", descriptionType);
							description.setText(paragraph);
							
							String bracketError = bracketChecker.checkBrackets(paragraph, descriptionType);
							if(!bracketError.isEmpty())
								modelFile.appendError(bracketError.substring(0, bracketError.length() - 2));
						}
					}
				}
			}
		}
		/* for debug purpose: to generate some random invalid xml files.
		if(Math.random()>0.5){
		Element element = new Element("bad_element");
		taxonIdentification.addContent(element);
		}*/
		xmlNamespaceManager.setXmlSchema(doc, FileTypeEnum.TAXON_DESCRIPTION);
		
		return doc;
	}

	private void addAsElementIfExists(Element parentElement,
			Map<String, String> data, String dataName, String xmlName, Document document) {
		if(data.containsKey(dataName) && data.get(dataName) != null && !data.get(dataName).trim().isEmpty()) {
			Element element = new Element(xmlName);
			parentElement.addContent(element);
			element.setText(data.get(dataName));
		}
	}

	public String createFileName(Map<String, String> data, XmlModelFile modelFile) {
		List<TaxonIdentificationEntry> taxonIdentificationEntries = new LinkedList<TaxonIdentificationEntry>();
		for(String nameType : nameTypes) {
			if(data.containsKey(nameType) && data.get(nameType) != null && !data.get(nameType).trim().isEmpty()) {
				taxonIdentificationEntries.add(new TaxonIdentificationEntry(Rank.valueOf(nameType.replaceFirst(" name$", "").toUpperCase()), 
						data.get(nameType)));
			}
		}
		Collections.sort(taxonIdentificationEntries);
		
		String filename = data.get("author") + data.get("year") + "_";
		for (TaxonIdentificationEntry taxonIdentificationEntry : taxonIdentificationEntries) {
			if (filename.matches(".*(_|^)" + taxonIdentificationEntry.getRank()	+ "(_|$).*"))
				modelFile.appendError("Redundant rank '" + taxonIdentificationEntry.getRank() + "'");
			filename += taxonIdentificationEntry.getRank() + "_" + taxonIdentificationEntry.getValue() + "_";
		}
		if(data.containsKey("strain number") && data.get("strain number") != null && !data.get("strain number").trim().isEmpty())
			filename += "strain_" + data.get("strain number") + "_";
		
		filename = filename.replaceAll("_+", "_").replaceFirst("_$", ".xml");
		return filename;
	}
	
}
