package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;

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
			
			// label,category,category name
			for (String[] lineOfSVMLabelAndCategoryMapping : linesOfSVMLabelAndCategoryMapping) {
				if (lineOfSVMLabelAndCategoryMapping.length > 2) {
					
					ILabel label = Label.getEnum(lineOfSVMLabelAndCategoryMapping[0].trim());
					String categoryCode = lineOfSVMLabelAndCategoryMapping[1].trim();
					String categoryName = lineOfSVMLabelAndCategoryMapping[2].trim().toLowerCase();
					System.out.println(categoryCode+" "+categoryName+" "+label);
					labelCategoryCodeMap.put(label,categoryCode);
					categoryCodeLabelMap.put(categoryCode, label);
					labelCategoryNameMap.put(label, categoryName);
					categoryNameLabelMap.put(categoryName, label);
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
