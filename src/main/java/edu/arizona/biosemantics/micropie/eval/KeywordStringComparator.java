package edu.arizona.biosemantics.micropie.eval;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;


/**
 * To compare the values by keyword
 * key1,key2[full string]
 * @author maojin
 *
 */
public class KeywordStringComparator extends StringComparator{
	@Override
	public double compare(List<CharacterValue> extValues,List<CharacterValue> gstValues) {
		if(extValues == null||gstValues==null||extValues.size()==0||gstValues.size()==0) return 0;
		//System.out.println(" gst:["+gstValues+"] tg:["+extValues+"]"); 
		int match = 0;
		for(int i=0;i<extValues.size();i++){
			CharacterValue extValue = extValues.get(i);
			String extValueStr = cleanValue(extValue.getValue().trim());
			if(extValueStr.indexOf("and ")>-1){
				String[] extDiValus = extValueStr.split("and");
				extValueStr = extDiValus[0].trim();
				
				//
				for(int j=1;j<extDiValus.length;j++){
					extValues.add(CharacterValueFactory.create(null, extDiValus[j].trim()));
				}
			}
			Iterator<CharacterValue> gstValueIter = gstValues.iterator();
			while(gstValueIter.hasNext()){
				CharacterValue gstValue = gstValueIter.next();
				String gstValueStr = cleanValue(gstValue.getValue().trim());
				
				//exactly string match
				if(extValueStr.equalsIgnoreCase(gstValueStr)&&!"".equals(extValueStr)){
					//System.out.println(extValueStr+" "+gstValueStr+" "+match);
					match++;
					//gstValues.remove(gstValue);
					break;
				}
			}
		}
		return match;
	}
	
	
	/**
	 * measure by keywords
	 * @param gstValueStr
	 * @param extValueStr
	 * @return
	 */
	public double matchMeasure(String gstValueStr, String extValueStr){
		String fullValue = gstValueStr;
		String[] keywords = null;
		if(gstValueStr!=null&&gstValueStr.indexOf("[")>-1&&gstValueStr.indexOf("]")>-1){
			fullValue = gstValueStr.substring(gstValueStr.indexOf("[")+1,gstValueStr.indexOf("]"));
			keywords = gstValueStr.substring(0,gstValueStr.indexOf("[")).split(",");
		}
		if(extValueStr.equalsIgnoreCase(fullValue)&&!"".equals(extValueStr)){
			return 1;
		}
		if(keywords==null||"".equals(keywords)){
			return 0;
		}else{
			int length = keywords.length;
			double hit = 0;
			for(String keyword : keywords){
				keyword= keyword.trim().toLowerCase();
				String patternString = "\\s"+keyword+"$|\\s"+keyword+"\\s|^"+keyword+"\\s|^"+keyword+"$"; // regular expression pattern
				Pattern pattern = Pattern.compile(patternString);
				Matcher matcher = pattern.matcher(extValueStr);			
				if (matcher.find()) {
					hit++;
					//System.out.println(hit+" "+length);
					continue;
				}
				
			}
			return hit/length;
		}
	}
	
	
	public static void main(String[] args){
		String gstValueStr = "a,b[a plus b]";
		KeywordStringComparator kscomp = new KeywordStringComparator();
		System.out.println(kscomp.matchMeasure(gstValueStr, "b 1a is not ab a"));
		System.out.println(kscomp.matchMeasure(gstValueStr, "b 1a is not ab b"));
		System.out.println(kscomp.matchMeasure(gstValueStr, "b"));
		System.out.println(kscomp.matchMeasure(gstValueStr, "afasd b"));
	}
}
