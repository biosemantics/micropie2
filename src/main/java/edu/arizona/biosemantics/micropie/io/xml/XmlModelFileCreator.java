package edu.arizona.biosemantics.micropie.io.xml;

import java.util.LinkedList;
import java.util.List;

/**
 * The shared part of XmlModelFileCreator, necessary to split text already on client side into treatments
 * in order to show progress.
 * @author rodenhausen
 *
 */
public class XmlModelFileCreator {

	protected String[] descriptionTypes = { "morphology", "habitat", "distribution", "phenology" };
	
	public String normalizeText(String text) {
		text = text.trim();
		text = text.replaceAll("\\r\\n", "\n");
		text = text.replaceAll("\\r", "\n"); 
		text = text.replaceAll("\\n{3,}", "\n\n");
		return text;
	}
	
	
	/**
	 * generate the treatment
	 * TODO:only one treatment?
	 * 
	 * @param text
	 * @return
	 */
	public List<String> getTreatmentTexts(String text) {
		List<String> result = new LinkedList<String>();
		
		boolean insideContinuousValue = false;
		
		StringBuilder treatment = new StringBuilder();
		for(String line : text.split("\n")) {
			line = line.trim();
			if(line.length()==0 && !insideContinuousValue) {
				result.add(treatment.toString());
				treatment = new StringBuilder();
			}else {
				treatment.append(line + "\n");
				int colonIndex = line.indexOf(":");
				if(colonIndex == -1 || insideContinuousValue) {
					if(line.endsWith("#"))
						insideContinuousValue = false;
					continue;
				} else {
					String key = line.substring(0, colonIndex).toLowerCase().trim();
					for(String descriptionType : descriptionTypes) {
						if(descriptionType.equals(key)) {
							String value = line.substring(colonIndex + 1, line.length()).trim();
							if(value.startsWith("#")) 
								insideContinuousValue = true;
							if(value.endsWith("#"))
								insideContinuousValue = false;
						}
					}
				}
			}			
		}
		String atreatment = treatment.toString().trim();
		result.add(atreatment); //replace all non-visible characters proceeding/trailing the treatment.
		return result;
	}
	
	
	/**
	 * 
	 * split string lines into multiple treatments
	 * 
	 * @param text
	 * @return
	 */
	public List<String> getTreatmentTexts(List<String> plainTextLines) {
		List<String> result = new LinkedList<String>();
		
		boolean insideContinuousValue = false;
		
		StringBuilder treatment = new StringBuilder();
		for(String line : plainTextLines) {
			line = line.trim();
			line = normalizeText(line);
			if(line.length()==0 && !insideContinuousValue) {
				if(treatment.toString().length()>0) result.add(treatment.toString());
				treatment = new StringBuilder();
			}else {
				treatment.append(line + "\n");
				int colonIndex = line.indexOf(":");
				if(colonIndex == -1 || insideContinuousValue) {
					if(line.endsWith("#"))
						insideContinuousValue = false;
					continue;
				} else {
					String key = line.substring(0, colonIndex).toLowerCase().trim();
					for(String descriptionType : descriptionTypes) {
						if(descriptionType.equals(key)) {
							String value = line.substring(colonIndex + 1, line.length()).trim();
							if(value.startsWith("#")) 
								insideContinuousValue = true;
							if(value.endsWith("#"))
								insideContinuousValue = false;
						}
					}
				}
			}			
		}
		String atreatment = treatment.toString().trim();
		result.add(atreatment); //replace all non-visible characters proceeding/trailing the treatment.
		return result;
	}

}