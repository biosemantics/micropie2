package edu.arizona.biosemantics.micropie.eval;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.NumericCharacterValue;


/**
 * compare the numeric values
 * @author maojin
 *
 */
public class NumericComparator implements IValueComparator{

	@Override
	public double compare(List<CharacterValue> extValues,List<CharacterValue> gstValues) {
		if((extValues == null||extValues.size()==0)&&(gstValues==null||gstValues.size()==0)) return 1;
		if(extValues == null||gstValues==null||extValues.size()==0||gstValues.size()==0) return 0;
		//System.out.println(" gst:["+gstValues+"] tg:["+extValues+"]"); 
		int match = 0;
		for(int i=0;i<extValues.size();i++){
			CharacterValue extValue = extValues.get(i);
			String extValueStr = cleanValue(extValue.getValue()).trim();
			if(extValueStr.indexOf("and ")>-1){
				String[] extDiValus = extValueStr.split("and");
				extValueStr = extDiValus[0].trim();
				
				//
				for(int j=1;j<extDiValus.length;j++){
					extValues.add(CharacterValueFactory.create(null, extDiValus[j].trim()));
				}
			}
			
			
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
				
				//gst value string
				String gstValueStr = cleanValue(gstValue.getValue()).trim();
				
				//gst value numeric
				Double anGstValue = null;
				
				//if the string can be matched
				if(gstValueStr.equalsIgnoreCase(extValueStr)&&!extValueStr.equals("")){
					match++;
					break;
				}
				
				//TODO:for a range, it's more complex
				//compare the value
				if(!"".equalsIgnoreCase(extValueStr)){
					try{
						anGstValue = new Double(gstValueStr);
						if(anGstValue!=null&&anGstValue.equals(anExtValue)){
							match++;
							//System.out.println(extValueStr+" "+gstValueStr+" hit "+match);
							//gstValues.remove(gstValue);
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
	
	
	public String cleanValue(String value){
		//value = value.replace("-", " ");
		
		//unit
		value = value.replace("mol%", "");
		value = value.replace("·", ".");
		value = value.replace("°C", "");
		value = value.replace("%", "");
		value = value.replace(" g", "");
		value = value.replace("sea salts", "");
		value = value.replace("(w/v)", "");
		value = value.replace(" NaCl", "");
		value = value.replace(" M", "");
		value = value.replace("??????C", "");
		value = value.replace("??C", "");
		value = value.replace("??m", "");
		value = value.replace("μm", "");
		
		value = value.replace("|", "");
		
		return value;
	}
	
	
	public static void main(String[] args ){
		List<CharacterValue> extValues = new ArrayList();
		extValues.add(new NumericCharacterValue(null,"1.00%"));
		List<CharacterValue> gstValues = new ArrayList();
		gstValues.add(new NumericCharacterValue(null,"1"));
	
		NumericComparator nc = new NumericComparator();
		System.out.println(nc.compare(extValues, gstValues));
	}
}
