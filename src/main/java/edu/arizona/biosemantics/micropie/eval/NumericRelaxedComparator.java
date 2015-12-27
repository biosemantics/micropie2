package edu.arizona.biosemantics.micropie.eval;

import java.util.Iterator;
import java.util.List;

import edu.arizona.biosemantics.micropie.model.CharacterValue;


/**
 * relaxed evaluation
 * 
 * 
 * @author maojin
 *
 */
public class NumericRelaxedComparator extends NumericComparator{
	
	
	@Override
	public double compare(List<CharacterValue> extValues,List<CharacterValue> gstValues) {
		if((extValues == null||extValues.size()==0)&&(gstValues==null||gstValues.size()==0)) return 1;
		if(extValues == null||gstValues==null||extValues.size()==0||gstValues.size()==0) return 0;
		//System.out.println(" gst:["+gstValues+"] tg:["+extValues+"]"); 
		double match = 0;
		for(int i=0;i<extValues.size();i++){
			CharacterValue extValue = extValues.get(i);
			String extValueStr = cleanValue(extValue.getValue()).trim();
			
			//extracted value string
			extValueStr = cleanValue(extValueStr).trim();
			//extracted value numeric
			Double anExtValue = null;
			if(!"".equalsIgnoreCase(extValueStr)){
				try{
					anExtValue = new Double(extValueStr);
				}catch(Exception e){
					anExtValue = null;
				}
			}
			
			Iterator<CharacterValue> gstValueIter = gstValues.iterator();
			while(gstValueIter.hasNext()){
				CharacterValue gstValue = gstValueIter.next();
				
				// Negation, must be the same
				boolean sameNeg = isSameNeg(extValue,gstValue);
				//System.out.println("sameNeg="+sameNeg);
				if(!sameNeg) return 0;
				
				//modifier match
				//double modMatch = modifierMatch(extValue, gstValue);
				//System.out.println("modMatch="+modMatch);
				double mainMatch = 0;
				//gst value string
				String gstValueStr = cleanValue(gstValue.getValue()).trim();
				
				//gst value numeric
				Double anGstValue = null;
				//System.out.println(extValueStr+"|"+gstValueStr+"|"+gstValueStr.equalsIgnoreCase(extValueStr));
				//if the string can be matched
				if(gstValueStr.equalsIgnoreCase(extValueStr)&&!extValueStr.equals("")){
					//System.out.println("mainMatch="+mainMatch+" "+modMatch);
					mainMatch=1;
					match+=mainMatch;
					break;
				}
				
				//TODO:for a range, it's more complex
				//compare the value as numeric
				if(!"".equalsIgnoreCase(extValueStr)){
					try{
						anGstValue = new Double(gstValueStr);
						if(anGstValue!=null&&anGstValue.equals(anExtValue)){
							mainMatch=1;
							
							match+=mainMatch;
							//System.out.println(" hit "+mainMatch);
							break;
						}
					}catch(Exception e){
						
					}
				}
				
				//System.out.println(extValueStr+" "+gstValueStr+" hit "+match);
			}
		}
		
		return match;
	}
}