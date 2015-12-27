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
		if((extValues == null||extValues.size()==0)&&(gstValues==null||gstValues.size()==0)) return 1;
		if(extValues == null||gstValues==null||extValues.size()==0||gstValues.size()==0) return 0;
		//System.out.println(" gst:["+gstValues+"] tg:["+extValues+"]"); 
		double match = 0;
		for(int i=0;i<extValues.size();i++){
			CharacterValue extValue = extValues.get(i);
			String extValueStr = cleanValue(extValue.getValue()).trim();
			
			//if(extValue.getNegation()!=null)  extValueStr=extValue.getNegation()+" "+extValueStr;
			Iterator<CharacterValue> gstValueIter = gstValues.iterator();
			while(gstValueIter.hasNext()){
				CharacterValue gstValue = gstValueIter.next();
				
				
				// Negation, must be the same
				boolean sameNeg = isSameNeg(extValue,gstValue);
				//if(extValue.getValue().indexOf("equir")>-1) System.out.println("sameNeg="+sameNeg);
				if(!sameNeg) continue;//current is not matched, to the next
				
				// MainValue
				String gstValueStr = cleanValue(gstValue.getValue()).trim();
				extValueStr = extValueStr.trim();
				
				//exactly string match
				double mainMatch =  this.matchMeasure(gstValueStr, extValueStr);
				//if(extValue.getValue().indexOf("equir")>-1) System.out.println("mainMatch="+mainMatch);
				//modifier match
				double modMatch = modifierMatch(extValue, gstValue);
				
				double totalMatch = mainMatch*modMatch;
				match+= totalMatch;
				if(totalMatch>0) break;
				//}
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
		}else{//if no [, it's fullValue
			//keywords = new String[]{gstValueStr};
			fullValue = gstValueStr;
		}
		//System.out.println("fullValue="+fullValue);
		//for(String keyword:keywords) System.out.println("keywords="+keyword.toString());
		//match the full values
		fullValue = fullValue.trim();
		if(extValueStr.equalsIgnoreCase(fullValue)&&!"".equals(extValueStr)){
			return 1;
		}
		
		extValueStr = extValueStr.toLowerCase().trim();
		if(keywords==null||"".equals(keywords)){
			return 0;
		}else{
			int length = keywords.length;
			double hit = 0;
			for(String keyword : keywords){
				keyword= keyword.trim().toLowerCase();
				//System.out.println("keyword="+keyword+" against "+extValueStr);
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
		//String gstValueStr = "a,b[a plus b]";
		//String gstValueStr = "straight rods";
		//String gstValueStr = "straight rods devoid of flagella";
		//String gstValueStr = "straight rods[straight rods devoid of flagella]";
		String gstValueStr = "Halophilic [Halophilic, growing between 1.0 and 7.5% (w/v) NaCl with optimum growth at 1-3%.]";
		KeywordStringComparator kscomp = new KeywordStringComparator();
		System.out.println(kscomp.matchMeasure(gstValueStr, "Halophilic"));
		System.out.println(kscomp.matchMeasure(gstValueStr, "straight rods devoid"));
		System.out.println(kscomp.matchMeasure(gstValueStr, "straight rods "));
		System.out.println(kscomp.matchMeasure(gstValueStr, "rods"));
	}
}
