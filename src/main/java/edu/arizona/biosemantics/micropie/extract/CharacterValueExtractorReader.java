package edu.arizona.biosemantics.micropie.extract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

import com.google.inject.Inject;

import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.keyword.AntibioticPhraseExtractor;
import edu.arizona.biosemantics.micropie.extract.keyword.FermentationProductExtractor;
import edu.arizona.biosemantics.micropie.extract.keyword.HabitatIsolatedFromExtractor;
import edu.arizona.biosemantics.micropie.extract.keyword.InorganicSubstanceExtractor;
import edu.arizona.biosemantics.micropie.extract.keyword.KeywordBasedExtractor;
import edu.arizona.biosemantics.micropie.extract.keyword.OrganicCompoundExtractor;
import edu.arizona.biosemantics.micropie.extract.keyword.PhraseBasedExtractor;
import edu.arizona.biosemantics.micropie.extract.keyword.SalinityPreferenceExtractor;
import edu.arizona.biosemantics.micropie.io.ICharacterValueExtractorReader;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;


/**
 * create character value extractors according to the configuration files
 * 
 */
public class CharacterValueExtractorReader implements ICharacterValueExtractorReader {


	
	@Inject
	public CharacterValueExtractorReader() {
	}
	
	@Override
	/**
	 * create ValueExtractor from the file name, either USP or Keyword-based
	 * 
	 * Example : 
	 * 	file name: c2.Cell wall.key
	 * 	labelName: c2
	 * 	character: Cell wall
	 *  type:key
	 */
	public ICharacterValueExtractor read(File file){
		String name = file.getName();
		String[] fields = name.split("\\.");
		/*
		int firstDotIndex = name.indexOf(".");
		
		// Example: file name: c2.Cell wall.key
		
		int lastDotIndex = name.lastIndexOf(".");
		
		String labelName = name.substring(0, firstDotIndex);
		String characterName = name.substring(firstDotIndex + 1, lastDotIndex);
		String type = name.substring(lastDotIndex + 1, name.length());
		*/
		String labelName = fields[0];
		String characterName = fields[1];
		String type = fields[fields.length-1];
		String matchModel = "P";//phrase model
		if(fields.length==4){
			matchModel = fields[2];
		}
		ExtractorType extractorType = ExtractorType.valueOf(type);
		//System.out.println(type+"="+extractorType);
		switch(extractorType) {
		case key:
			return createKeywordBasedExtractor(file, labelName, characterName, matchModel);
		default:return null;
		}
	}

	
	/**
	 * Create keyword based extractor
	 * @param file: contains the initial keywords
	 * @param labelName
	 * @param character
	 * @return
	 * @throws IOException
	 */
	private ICharacterValueExtractor createKeywordBasedExtractor(File file, String labelName, String characterName, String matchMode){
		Set<String> keywords = new LinkedHashSet<String>();
		Map<String, List> subKeywords = new LinkedHashMap<String, List>();
		String firstLine = null;
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF8"));
			String strLine;
			int line =1;
			
			while ((strLine = br.readLine()) != null) {
				if(line++==1) firstLine = strLine;
				if(strLine.startsWith("#")) continue;
				//jin 09-24-2015
				if(strLine.indexOf("|")>-1){
					String[] fields = strLine.split("\\|");
					String keyword = fields[0].trim().replace("-", " ").replace("_", " ");
					keywords.add(keyword);
					subKeywords.put(keyword,new ArrayList());
					for(int i=1;i<fields.length;i++){
						subKeywords.get(keyword).add(fields[i].trim().replace("-", " ").replace("_", " "));
					}
				}else{
					keywords.add(strLine.trim().replace("-", " ").replace("_", " "));
				}
				
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		String initClass = null;
		if(firstLine!=null&&firstLine.startsWith("#")){
			initClass = firstLine.replace("#", "");
		}
		//return new KeywordBasedExtractor(Label.valueOf(labelName), characterName, keywords, subKeywords);
		//SentenceSpliter must be set latter
		
		Label label = Label.valueOf(labelName);
		//System.out.println("read character extractors "+label);
		if(initClass!=null){
			if("SalinityPreferenceExtractor".equals(initClass)){
				//fix it
				label = Label.c59;
				SalinityPreferenceExtractor saliPrefExtractor = new SalinityPreferenceExtractor(label, characterName,keywords,subKeywords);
				return saliPrefExtractor;
			}else if("HabitatIsolatedFromExtractor".equals(initClass)){
				return new HabitatIsolatedFromExtractor(label, characterName,keywords,subKeywords);
			}else if("FermentationProductExtractor".equals(initClass)){
				return new FermentationProductExtractor(label, characterName,keywords,subKeywords);
			}else if("AntibioticPhraseExtractor".equals(initClass)){
				return new AntibioticPhraseExtractor(label, characterName,keywords,subKeywords);
			}else if("OrganicCompoundExtractor".equals(initClass)){
				return new OrganicCompoundExtractor(label, characterName,keywords,subKeywords);
			}else if("InorganicSubstanceExtractor".equals(initClass)){
				return new InorganicSubstanceExtractor(label, characterName,keywords,subKeywords);
			}else if("KeywordBasedExtractor".equals(initClass)){
				return new KeywordBasedExtractor(label, characterName,keywords,subKeywords);
			}
		}

		//default
		return new PhraseBasedExtractor(Label.valueOf(labelName), characterName, keywords, subKeywords, matchMode);
	}
}
