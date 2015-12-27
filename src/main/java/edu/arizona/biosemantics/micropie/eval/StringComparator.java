package edu.arizona.biosemantics.micropie.eval;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;


/**
 * compare the value by exact string matching
 * @author maojin
 *
 */
public class StringComparator implements IValueComparator{

	@Override
	public double compare(List<CharacterValue> extValues,List<CharacterValue> gstValues) {
		if((extValues == null||extValues.size()==0)&&(gstValues==null||gstValues.size()==0)) return 1;
		if(extValues == null||gstValues==null||extValues.size()==0||gstValues.size()==0) return 0;
		//System.out.println(" gst:["+gstValues+"] tg:["+extValues+"]"); 
		double match = 0;
		for(int i=0;i<extValues.size();i++){
			CharacterValue extValue = extValues.get(i);
			String extValueStr = cleanValue(extValue.getValue()).trim();
			
			/*if(extValueStr.indexOf("and ")>-1){
				String[] extDiValus = extValueStr.split("and");
				extValueStr = extDiValus[0].trim();
				
				//
				for(int j=1;j<extDiValus.length;j++){
					extValues.add(CharacterValueFactory.create(null, extDiValus[j].trim()));
				}
			}*/
			Iterator<CharacterValue> gstValueIter = gstValues.iterator();
			while(gstValueIter.hasNext()){
				CharacterValue gstValue = gstValueIter.next();
				//System.out.println(extValueStr+" "+gstValue.getValue()+" "+match);
				String gstValueStr = cleanValue(gstValue.getValue()).trim();
				extValueStr = extValueStr.trim();
				//exactly string match
				//System.out.println(extValueStr+" "+gstValueStr+" "+match);
				//if(extValueStr.equalsIgnoreCase(gstValueStr)&&!"".equals(extValueStr)){
				if(extValueStr.equalsIgnoreCase(gstValueStr)){
					//System.out.println(extValueStr+" "+gstValueStr+" hit "+match);
					match++;
					//gstValues.remove(gstValue);
					break;
				}
			}
		}
		return match;
	}
	
	
	public String cleanValue(String value){
		value = value.replace("-", " ");
		
		//unit
		value = value.replace("mol%", "")
				.replace("·", ".")
				.replace("°C", "")
				.replace("%", " ")
				.replace(" g", " ")
				.replace("sea salts", " ")
				.replace("(w/v)", " ")
				.replace(" NaCl", " ")
				.replace(" M", " ")
				.replace("??????C", "")
				.replace("??C", "")
				.replace("??m", "")
				.replace("-", " ").
				replace("[\\s]*:[\\s]*", ":");
		
		return value;
	}
	
	/**
	 * whether they are the same negation
	 * @param extValue
	 * @param gstValue
	 * @return
	 */
	public boolean isSameNeg(CharacterValue extValue, CharacterValue gstValue) {
		String extNeg = extValue.getNegation();
		String gstNeg = gstValue.getNegation();
		if((extNeg==null||"".equalsIgnoreCase(extNeg))&&(gstNeg!=null&&!"".equalsIgnoreCase(gstNeg))){
			return false;
		}else if((extNeg!=null&&!"".equalsIgnoreCase(extNeg))&&(gstNeg==null||"".equalsIgnoreCase(gstNeg))){
			return false;
		}else if((extNeg!=null&&!"".equalsIgnoreCase(extNeg))&&(gstNeg!=null&&!"".equalsIgnoreCase(gstNeg))){
			if(extNeg.trim().equalsIgnoreCase(gstNeg.trim())){
				return true;
			}else{
				return false;
			}
		}else{
			return true;
		}
	}
	
	
	/**
	 * whether they are the same modifer
	 * get half value
	 * @param extValue
	 * @param gstValue
	 * @return
	 */
	public double modifierMatch(CharacterValue extValue, CharacterValue gstValue) {
		
		String extNeg = extValue.getValueModifier();
		String gstNeg = gstValue.getValueModifier();
		
		
		String extUnit = extValue.getUnit();
		String gstUnit = gstValue.getUnit();
		
		if(extUnit!=null) extUnit = unitNormal(extUnit);
		if(gstUnit!=null) gstUnit = unitNormal(gstUnit);
		
		if(isTheSame(extNeg,gstNeg)&&isTheSame(extUnit,gstUnit)){
			return 1.0;
		}else{
			return 0.5;
		}
		
		/*
		if((extNeg==null||"".equals(extNeg))&&(gstNeg!=null&&!"".equals(gstNeg))){
			return 0.5;
		}else if((extNeg!=null&&!"".equals(extNeg))&&(gstNeg==null||"".equals(gstNeg))){
			return 0.5;
		}else if((extNeg!=null&&!"".equals(extNeg))&&(gstNeg!=null&&!"".equals(gstNeg))){
			if(extNeg.trim().equalsIgnoreCase(gstNeg.trim())){
				return 1.0;
			}else{
				return 0.5;
			}
		}else{
			return 1.0;
		}*/
	}

	
	public boolean isTheSame(String source, String target){
		if(source==null&&target==null){
			return true;
		}else if(source==null&&target!=null&&!"".equals(target.trim())){
			return false;
		}else if(source==null&&(target!=null&&"".equals(target.trim()))){
			return true;
		}else if(source!=null&&target==null&&!"".equals(source.trim())){
			return false;
		}else if(target==null&&source!=null&&"".equals(source.trim())){
			return true;
		}else if(source.trim().equalsIgnoreCase(target.trim())){
			return true;
		}else{
			return false;
		}
	}
	
	
	private String unitNormal(String value){
		value = value.replace("˚C", "˚C").replace("°C", "˚C").replace("°C", "˚C").replace("μm", "µm");
		return value;
	}
	
}
