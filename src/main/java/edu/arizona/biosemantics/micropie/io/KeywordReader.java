package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;


/**
 * read character term list
 * @author maojin
 *
 */
public class KeywordReader {
	
	/**
	 * read a term list from a file
	 * @param file
	 * @return
	 */
	public Map<String, List> readATermList(String file){
		Map<String, List> keywordMap = new LinkedHashMap<String, List>();
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				//jin 09-24-2015
				if(strLine.indexOf("|")>-1){
					String[] fields = strLine.split("\\|");
					String keyword = fields[0].trim();
					keywordMap.put(keyword,new ArrayList());
					for(int i=1;i<fields.length;i++){
						keywordMap.get(keyword).add(fields[i].toString());
					}
				}else{
					keywordMap.put(strLine.trim(),null);
				}
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return keywordMap;
	}
	
	
	/**
	 * read a term list from a file
	 * @param file
	 * @return
	 */
	public Set<String> readATermSet(String file){
		Set<String> keywordSet = new HashSet();
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				//jin 09-24-2015
				if(strLine.indexOf("|")>-1){
					String[] fields = strLine.split("\\|");
					for(int i=0;i<fields.length;i++){
						keywordSet.add(fields[i].trim());
					}
				}else{
					keywordSet.add(strLine.trim());
				}
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return keywordSet;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Map<String, Set<ILabel>> readTermCharacterMap(String termsFolder){
		Map<String, Set<ILabel>> termCharacterMap = new HashMap();
		File termFolderFile = new File(termsFolder);
		File[] files = termFolderFile.listFiles();
		for(File file : files){
			String name = file.getName();
			int firstDotIndex = name.indexOf(".");
			int lastDotIndex = name.lastIndexOf(".");
			
			String labelName = name.substring(0, firstDotIndex);
			Label label = null;
			try{
				label = Label.valueOf(labelName);
			}catch(Exception e){
				continue;
			}
			String characterName = name.substring(firstDotIndex + 1, lastDotIndex);
			String type = name.substring(lastDotIndex + 1, name.length());
			if("key".equals(type)){//a keyword list
				try{
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
					String strLine;
					while((strLine = br.readLine()) != null) {
						//jin 09-24-2015
						if(strLine.indexOf("|")>-1){
							String[] fields = strLine.split("\\|");
							for(int i=0;i<fields.length;i++){
								String keyword = fields[i].trim();
								if(!"".equalsIgnoreCase(keyword)) putIntoMap(label, keyword, termCharacterMap);
							}
						}else{
							String keyword = strLine.trim();
							if(!"".equalsIgnoreCase(keyword)) putIntoMap(label, strLine.trim(), termCharacterMap);
						}
					}
					br.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		return termCharacterMap;
	}


	/**
	 * put this 
	 * @param label
	 * @param keyword
	 * @param termCharacterMap
	 */
	private void putIntoMap(Label label, String keyword, Map<String, Set<ILabel>> termCharacterMap) {
		// TODO Auto-generated method stub
		Set<ILabel> labels = termCharacterMap.get(keyword);
		if(labels==null){
			labels = new HashSet();
			termCharacterMap.put(keyword, labels);
		}
		labels.add(label);
	}
	
	
	
	
	public int countOverLap(Set<String> termSet1, Set<String> termSet2){
		int overlap = 0;
		for(String term : termSet1){
			if(termSet2.contains(term)) overlap++;
		}
		return overlap;
	}
	
	public static void main(String[] args){
		KeywordReader keywordReader = new KeywordReader();
		String termsFolder = "F:/MicroPIE/micropieInput/termlist";
		
		/*compare two characters
		File termFolderFile = new File(termsFolder);
		File[] files = termFolderFile.listFiles();
		List<Set> allTermSet = new ArrayList();
		for(File file : files){
			String name = file.getName();
			//System.out.println(name);
			allTermSet.add(keywordReader.readATermSet(file.getAbsolutePath()));
		}
		
		for(int i=0;i<allTermSet.size();i++){
			String aname = files[i].getName();
			
			for(int j=i+1;j<allTermSet.size();j++){
				String bname = files[j].getName();
				
				int overlap = keywordReader.countOverLap(allTermSet.get(i), allTermSet.get(j));
				if(overlap>0) System.out.println(aname+"|"+bname+"|"+overlap);
			}
		}
		*/
		
		/* output termlist */
		String svmLabelAndCategoryMappingFile = "F:/MicroPIE/micropieInput//svmlabelandcategorymapping/categoryMapping_all.txt";
		CharacterReader characterReader = new CharacterReader();
		characterReader.setCategoryFile(svmLabelAndCategoryMappingFile);
		characterReader.read();
		
		Map<ILabel, String> labelCategoryCodeMap = characterReader.getLabelCategoryCodeMap();
		Map<String, ILabel> categoryCodeLabelMap =characterReader.getCategoryCodeLabelMap();
		Map<ILabel, String> labelCategoryNameMap = characterReader.getLabelCategoryNameMap();
		Map<String, ILabel> categoryNameLabelMap = characterReader.getCategoryNameLabelMap();
		
		
		Map<String, Set<ILabel>> termCharacterMap = keywordReader.readTermCharacterMap(termsFolder);
		for(Entry<String, Set<ILabel>> e: termCharacterMap.entrySet()){
			Set<ILabel> labels = e.getValue();
			StringBuffer sb = new StringBuffer();
			for(ILabel label : labels){
				sb.append(labelCategoryCodeMap.get(label)).append(" ").append(labelCategoryNameMap.get(label)).append(",");
			}
			System.out.println(e.getKey()+"|"+e.getValue().size()+"|"+sb.substring(0,sb.length()-1));
		}
		
	}
}
