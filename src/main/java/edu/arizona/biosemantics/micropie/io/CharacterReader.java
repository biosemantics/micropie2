package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import edu.arizona.biosemantics.micropie.classify.CategoryLabel;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.classify.LabelPhraseValueType;
import edu.arizona.biosemantics.micropie.extract.PhraseValueType;

/**
 * Read the categories and parse the mappings ILabel<-->CategoryCode
 * ILabel<-->CategoryName
 * 
 * Mainly Used in SVM and also standardize all the category information with
 * ILabels
 * 
 * @author maojin
 *
 */
public class CharacterReader {

	/**
	 * format: ILabel, categoryCode, Name
	 * 
	 * 
	 */
	private String categoryFile;

	/**
	 * <label, categoryCode>
	 */
	private Map<ILabel, String> labelCategoryCodeMap;

	/**
	 * <categoryCode, label>
	 */
	private Map<String, ILabel> categoryCodeLabelMap;

	/**
	 * <label, categoryName>
	 */
	private Map<ILabel, String> labelCategoryNameMap;

	/**
	 * <categoryName, label>
	 */
	private Map<String, ILabel> categoryNameLabelMap;
	
	/**
	 * <character, category>
	 */
	private Map<ILabel, ILabel> categoryUpperMap;
	

	public CharacterReader() {

	}

	public String getCategoryFile() {
		return categoryFile;
	}

	public void setCategoryFile(String categoryFile) {
		this.categoryFile = categoryFile;
	}

	public Map<ILabel, String> getLabelCategoryCodeMap() {
		return labelCategoryCodeMap;
	}

	public void setLabelCategoryCodeMap(Map<ILabel, String> labelCategoryCodeMap) {
		this.labelCategoryCodeMap = labelCategoryCodeMap;
	}

	public Map<String, ILabel> getCategoryCodeLabelMap() {
		return categoryCodeLabelMap;
	}

	public void setCategoryCodeLabelMap(Map<String, ILabel> categoryCodeLabelMap) {
		this.categoryCodeLabelMap = categoryCodeLabelMap;
	}

	public Map<ILabel, String> getLabelCategoryNameMap() {
		return labelCategoryNameMap;
	}

	public void setLabelCategoryNameMap(Map<ILabel, String> labelCategoryNameMap) {
		this.labelCategoryNameMap = labelCategoryNameMap;
	}

	public Map<String, ILabel> getCategoryNameLabelMap() {
		return categoryNameLabelMap;
	}

	public void setCategoryNameLabelMap(Map<String, ILabel> categoryNameLabelMap) {
		this.categoryNameLabelMap = categoryNameLabelMap;
	}

	public Map<ILabel, ILabel> getCategoryUpperMap() {
		return categoryUpperMap;
	}

	public void setCategoryUpperMap(Map<ILabel, ILabel> categoryUpperMap) {
		this.categoryUpperMap = categoryUpperMap;
	}

	/**
	 * parse the file
	 * @return
	 */
	public void read() {
		CSVReader readerOfSVMLabelAndCategoryMapping;
		try {
			readerOfSVMLabelAndCategoryMapping = new CSVReader(
					new BufferedReader(new InputStreamReader(new FileInputStream(this.categoryFile),
							"UTF8")));
			List<String[]> linesOfSVMLabelAndCategoryMapping = readerOfSVMLabelAndCategoryMapping
					.readAll();

			labelCategoryCodeMap = new HashMap<ILabel, String>();
			categoryCodeLabelMap = new HashMap<String, ILabel>();
			labelCategoryNameMap = new HashMap<ILabel, String>();
			categoryNameLabelMap = new HashMap<String, ILabel>();
			categoryUpperMap = new HashMap<ILabel, ILabel>();
			
			// label,category,category name
			for (String[] lineOfSVMLabelAndCategoryMapping : linesOfSVMLabelAndCategoryMapping) {
				if (lineOfSVMLabelAndCategoryMapping.length > 2) {
					
					ILabel label = Label.getEnum(lineOfSVMLabelAndCategoryMapping[0].trim());
					String categoryCode = lineOfSVMLabelAndCategoryMapping[1].trim();
					String categoryName = lineOfSVMLabelAndCategoryMapping[2].trim().toLowerCase();
					
					String upperCategoryCode = categoryCode.substring(0, categoryCode.indexOf("."));
					ILabel upperLabel = CategoryLabel.getEnum(upperCategoryCode);
					//System.out.println(categoryCode+" "+categoryName+" "+label+" "+upperCategoryCode+" "+upperLabel);
					labelCategoryCodeMap.put(label,categoryCode);
					categoryCodeLabelMap.put(categoryCode, label);
					labelCategoryNameMap.put(label, categoryName);
					categoryNameLabelMap.put(categoryName, label);
					categoryUpperMap.put(label, upperLabel);
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * read the configuration of labelvaluetype
	 * @param labelValueTypeFile
	 * @return
	 */
	public LabelPhraseValueType readLabelValueType(String labelValueTypeFile){
		//System.out.println("read label value type");
		LabelPhraseValueType lpt = new LabelPhraseValueType();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(labelValueTypeFile),
							"UTF8"));
			String line = null;
			while((line=br.readLine())!=null){
				if(!line.startsWith("#")&&!"".equals(line.trim())){
					String[] field = line.split("\\\"\\,");
					Label label = Label.getEnum(field[0].replace("\"", ""));
					String[] allowValueTypes = field[1].replace("\"", "").split(",");
					for(String avt : allowValueTypes){
						//PhraseValueType pvt = PhraseValueType.getEmum(avt);
						if("NU".equals(avt)){
							lpt.nuCharSet.add(label);
						}else if("NP".equals(avt)){
							lpt.npCharSet.add(label);
						}else if("JP".equals(avt)){
							lpt.jpCharSet.add(label);
						}else{
							lpt.spCharSet.add(label);
						}
					}
				}
			}
			br.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return lpt;
	}

}
