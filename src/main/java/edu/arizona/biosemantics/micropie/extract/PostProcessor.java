package edu.arizona.biosemantics.micropie.extract;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.classify.LabelGroups;
import edu.arizona.biosemantics.micropie.model.CharacterGroup;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.NumericCharacterValue;
import edu.arizona.biosemantics.micropie.nlptool.NegationIdentifier;


/**
 * post processor
 * It should be used after the extraction and before the parsing.
 * @author maojin
 *
 */
public class PostProcessor {
	private LabelGroups labelGroups = new LabelGroups();
	
	/**
	 * 
	 * post process the extracted values according to some rules:
	 * For 9.1,9.3 and 9.5 if the value is negative, chage it to another value
	 * 
	 * @param valueList
	 */
	public void postProcessor(List<CharacterValue> valueList, List<CharacterValue> noLabelValueList,Map<ILabel, List<CharacterValue>> charMap){
		
		/*
		List<CharacterValue> fermUsed = charMap.get(Label.c57);
		List<CharacterValue> fermNotUsed = charMap.get(Label.c57);
		
		
		Set fermSet = new HashSet();
		for(CharacterValue cv :fermUsed){
			fermSet.add(cv.getValue());
		}
		for(CharacterValue cv :fermNotUsed){
			fermSet.add(cv.getValue());
		}*/
		//post-process one by one
		for(int i=0;valueList!=null&&i<valueList.size();){
			CharacterValue aValue = valueList.get(i);
			ILabel valueLabel = aValue.getCharacter();
			if(valueLabel==null){//null label values
				i++;
				noLabelValueList.add(aValue);
				//System.out.println("this is null label:"+aValue);
				continue;
			}else{
				if(Label.c2.equals(valueLabel)){
					replaceGram(aValue);
				}else if(Label.c9.equals(valueLabel)){
					replaceRods(aValue);
				}else if(Label.c46.equals(valueLabel)){
					aValue.setNegation(null);
				}
				
				
				
				/**
				 * it should not be in the Fermentation substrate characater values
				if(Label.c53.equals(valueLabel)||Label.c54.equals(valueLabel)){
					System.out.println("check="+aValue+"===>"+fermSet.contains(aValue.getValue()));
					if(fermSet.contains(aValue.getValue())){
						valueList.remove(aValue);
						continue;
					}
				} */
				
				//Tests negative, organic compounds used or hydrolyzed, inorganic substances used,fermentation substrates used
				if(Label.c45.equals(valueLabel)||Label.c53.equals(valueLabel)||Label.c55.equals(valueLabel)||Label.c57.equals(valueLabel)){
					negationToAnother(aValue);
				}else if(Label.c54.equals(valueLabel)||Label.c56.equals(valueLabel)||Label.c58.equals(valueLabel)){
					if(aValue.getNegation()!=null){//should be 
						aValue.setNegation(null);
					}else{//if it's null
						//negationToPositive(aValue);
					}
				}else if(Label.c32.equals(valueLabel)||Label.c33.equals(valueLabel)){
					//Antibiotic sensitivity
					negationReverse(aValue);
				}
				
				//remove
				//if the value is empty, remove it.
				if(aValue.getValue()==null||"".equals(aValue.getValue())){//it must be a negation
					valueList.remove(aValue);
					//System.out.println("remove 1:"+aValue);
					i--;
				}else if(Label.c41.equals(valueLabel)){//it must be a negation
					if(aValue.getNegation()!=null&&!"".equals(aValue.getNegation())){
						valueList.remove(aValue);
						//System.out.println("remove 2:"+aValue);
						i--;
					}
				}else if(Label.c46.equals(valueLabel)){//it must be a negation
					if((aValue.getNegation()==null||"".equals(aValue.getNegation()))&&!NegationIdentifier.detectInlineNegation(aValue.getValue())){
						valueList.remove(aValue);
						//System.out.println("remove 2:"+aValue);
						i--;
					}
				}else if(Label.c32.equals(valueLabel)||Label.c33.equals(valueLabel)){//the length of value should be greater than 1
					if((aValue.getValue().trim().length()<=1)){
						//System.out.println("remove 3:"+aValue);
						valueList.remove(aValue);
						i--;
					}
				}
				
				
				i++;
			}
		}
		
		
		//System.out.println(" label:"+valueList);
		//PH, Temp, NaCl, Min Max, Opt
		//Min should be the minimum, Max should be the Maximum
		/*
		if(valueList.size()>0){
			ILabel valueLabel = valueList.get(0).getCharacter();
			if((labelGroups.maxValueGroup.contains(valueLabel)||labelGroups.minValueGroup.contains(valueLabel))&&valueList.size()>1){
				distinctRestrain(valueList, valueLabel);
			}
		}
		*/
		//System.out.println(" aa label:"+valueList);
	}

	
	private void replaceRods(CharacterValue aValue) {
		if(aValue.getValue()!=null){
			aValue.setValue(aValue.getValue().replace("rods", "").trim());
		}
	}


	/**
	 * change Gram-negative rods into rods
	 * @param aValue
	 */
	private void replaceGram(CharacterValue aValue) {
		if(aValue.getValue()!=null){
			aValue.setValue(aValue.getValue().replace("[gG]ram[\\-\\s]negative", "").trim());
			if(aValue.getValueModifier()!=null&&aValue.getValueModifier().indexOf("negative")>-1) aValue.setValueModifier(null);
		}
			
	}


	/**
	 * if the value is negation, change it to the corresponding negative character.
	 * @param aValue
	 */
	public void negationToAnother(CharacterValue aValue) {
		ILabel valueLabel = aValue.getCharacter();
		//valueLabel.getValue();
		String negation = aValue.getNegation();
		if(negation!=null&&!"".equals(negation)){//chage to the corresponding negative character
			Label newLabel = Label.getEnum((new Integer(valueLabel.getValue())+1)+"");
			aValue.setCharacter(newLabel);
			aValue.setNegation(null);
		}
	}
	
	
	/**
	 * if the value is negation, change it to the corresponding negative character.
	 * @param aValue
	 */
	public void negationToPositive(CharacterValue aValue) {
		ILabel valueLabel = aValue.getCharacter();
		//valueLabel.getValue();
		String negation = aValue.getNegation();
		if(negation!=null&&!"".equals(negation)){//chage to the corresponding negative character
			Label newLabel = Label.getEnum((new Integer(valueLabel.getValue())-1)+"");
			aValue.setCharacter(newLabel);
			aValue.setNegation(null);
		}
	}
	
	
	/**
	 * if the value is negation, change it to the corresponding negative character.
	 * @param aValue
	 */
	public void negationReverse(CharacterValue aValue) {
		ILabel valueLabel = aValue.getCharacter();
		valueLabel.getValue();
		String negation = aValue.getNegation();
		if(negation!=null&&!"".equals(negation)){//chage to the corresponding negative character
			Label newLabel = valueLabel.equals(Label.c32)?Label.c33:Label.c32;
			aValue.setCharacter(newLabel);
			aValue.setNegation(null);
		}
	}
	
	
	/**
	 * deal with the conflict numeric values
	 * @param charaMap
	 */
	public void dealConflictNum(Map<ILabel, List<CharacterValue>> charaMap) {
		for(ILabel minLabel : labelGroups.minValueGroup){
			List<CharacterValue> valueList  = charaMap.get(minLabel);
			distinctRestrain(valueList, minLabel);
		}
		for(ILabel maxLabel : labelGroups.maxValueGroup){
			List<CharacterValue> valueList  = charaMap.get(maxLabel);
			//System.out.println("dealConflictNum max="+valueList);
			distinctRestrain(valueList, maxLabel);
			//System.out.println("after dealConflictNum max="+valueList);
		}
	}

	
	
	
	/**
	 * For some characters there should be only one values
	 * @param characterValues
	 * @param label
	 * 
		if(valueList.size()>0){
			ILabel valueLabel = valueList.get(0).getCharacter();
			if((labelGroups.maxValueGroup.contains(valueLabel)||labelGroups.minValueGroup.contains(valueLabel))&&valueList.size()>1){
				distinctRestrain(valueList, valueLabel);
			}
		}
	 */
	public void distinctRestrain(List<CharacterValue> characterValues, ILabel label){
		try{
			if(characterValues!=null&&characterValues.size()>1){
				//determine min or max
				if(labelGroups.minValueGroup.contains(label)){//keep the smallest
					//the same subcharacter
					String[] subcharacter = new String[characterValues.size()];
					for(int i=0;i<characterValues.size();i++){
						subcharacter[i] = ((NumericCharacterValue)characterValues.get(i)).getSubCharacter()==null?null:((NumericCharacterValue)characterValues.get(i)).getSubCharacter().trim();
					}
					for(int i=0;i<characterValues.size();i++){
						for(int j=i+1;j<characterValues.size();j++){
							if(subcharacter[i]!=null&&subcharacter[j]!=null&&subcharacter[i].trim().equals(subcharacter[j].trim())){
								
							}else if(subcharacter[i]==null&&(subcharacter[j]==null||"".equals(subcharacter[j]))){
								
							}else if(subcharacter[j]==null&&(subcharacter[i]==null||"".equals(subcharacter[i]))){
								
							}else{
								return;
							}
					}
					}
					
					
					double[] minValues =  new double[characterValues.size()];
					for(int i=0;i<characterValues.size();i++){
						minValues[i] = detectMin(characterValues.get(i).getValue());
					}
					double min = 10000;
					NumericCharacterValue  minCV  = null;
					for(int i=0;i<characterValues.size();i++){
						if(minValues[i]<min){//update
							min = minValues[i];
							minCV = (NumericCharacterValue)characterValues.get(i);
						}
					}
					//keep the smallest
					characterValues.clear();
					characterValues.add(minCV);
					
				}else if(labelGroups.maxValueGroup.contains(label)){//keep the largest
					String[] subcharacter = new String[characterValues.size()];
					for(int i=0;i<characterValues.size();i++){
						subcharacter[i] = ((NumericCharacterValue)characterValues.get(i)).getSubCharacter()==null?null:((NumericCharacterValue)characterValues.get(i)).getSubCharacter().trim();
					}
					for(int i=0;i<characterValues.size();i++){
						for(int j=i+1;j<characterValues.size();j++){
							if(subcharacter[i]!=null&&subcharacter[j]!=null&&subcharacter[i].trim().equals(subcharacter[j].trim())){
								
							}else if(subcharacter[i]==null&&(subcharacter[j]==null||"".equals(subcharacter[j]))){
								
							}else if(subcharacter[j]==null&&(subcharacter[i]==null||"".equals(subcharacter[i]))){
								
							}else{
								return;
							}
					}
					}
					
					double[] maxValues =  new double[characterValues.size()];
					for(int i=0;i<characterValues.size();i++){
						maxValues[i] = detectMax(characterValues.get(i).getValue());
					}
					double max = -10000;
					NumericCharacterValue  maxCV  = null;
					for(int i=0;i<characterValues.size();i++){
						if(maxValues[i]>max){//update
							max = maxValues[i];
							maxCV = (NumericCharacterValue)characterValues.get(i);
						}
					}
					//keep the smallest
					characterValues.clear();
					characterValues.add(maxCV);
				}
			}
		}catch(Exception e){
			
		}
	}
	
	
	
	private double detectMax(String value)  throws Exception {
		value = value.replace("<", "").replace(">", "").replace("-", " ").trim();
		String[] fields = value.split(" ");
		if(fields.length==1){
			return new Double(value);
		}else{
			double max = -10000;
			for(int i=0;i<fields.length;i++){
				if(new Double(fields[i])>max){//update
					max = new Double(fields[i]);
				}
			}
			return max;
		}
	}


	/**
	 * find the min value of this value
	 * @param value
	 * @return
	 * @throws Exception
	 */
	private double detectMin(String value)  throws Exception {
		value = value.replace("<", "").replace(">", "").replace("-", " ").trim();
		String[] fields = value.split(" ");
		if(fields.length==1){
			return new Double(value);
		}else{
			double min = 10000;
			for(int i=0;i<fields.length;i++){
				if(new Double(fields[i])<min){//update
					min = new Double(fields[i]);
				}
			}
			return min;
		}
	}

	/**
	 * deal unspecific values
	 * @param noLabelValueList
	 */
	public void dealUSP(List<CharacterValue> noLabelValueList, Map<ILabel, List<CharacterValue>> chaMap) {
		
		List<NumericCharacterValue> tempUspList = new ArrayList();
		List<NumericCharacterValue> phUspList = new ArrayList();
		List<NumericCharacterValue> naclUspList = new ArrayList();
		for(int i=0;i<noLabelValueList.size();i++){
			NumericCharacterValue ncv = (NumericCharacterValue) noLabelValueList.get(i);
			if(ncv!=null&&ncv.getCharacterGroup()!=null){
				switch(ncv.getCharacterGroup()){
					case TEMP: tempUspList.add(ncv);continue;
					case PH: phUspList.add(ncv);continue;
					case NACL: naclUspList.add(ncv);continue;
				}
			}
		}
		
		if(tempUspList.size()>1){
			//keep the smallest
			try {
				NumericCharacterValue minCV = findMinCharacter(tempUspList);
				if(minCV!=null){
					tempUspList.remove(minCV);
					minCV.setCharacter(Label.c24);//temp
					chaMap.get(Label.c24).add(minCV);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			//keep the largest
			try {
				NumericCharacterValue maxCV = findMaxCharacter(tempUspList);
				if(maxCV!=null){
					tempUspList.remove(maxCV);
					maxCV.setCharacter(Label.c26);//temp
					chaMap.get(Label.c26).add(maxCV);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		
		
		if(phUspList.size()>1){
			//keep the smallest
			try {
				NumericCharacterValue minCV = findMinCharacter(phUspList);
				if(minCV!=null){
					phUspList.remove(minCV);
					minCV.setCharacter(Label.c18);//ph min
					chaMap.get(Label.c18).add(minCV);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			//keep the largest
			try {
				NumericCharacterValue maxCV = findMaxCharacter(phUspList);
				if(maxCV!=null){
					phUspList.remove(maxCV);
					maxCV.setCharacter(Label.c20);//ph max
					chaMap.get(Label.c20).add(maxCV);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		if(naclUspList.size()>1){
			//keep the smallest
			try {
				NumericCharacterValue minCV = findMinCharacter(naclUspList);
				if(minCV!=null){
					naclUspList.remove(minCV);
					minCV.setCharacter(Label.c21);//ph min
					chaMap.get(Label.c21).add(minCV);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			//keep the largest
			try {
				NumericCharacterValue maxCV = findMaxCharacter(naclUspList);
				if(maxCV!=null){
					naclUspList.remove(maxCV);
					maxCV.setCharacter(Label.c23);//ph max
					chaMap.get(Label.c23).add(maxCV);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	
	/**
	 * find the smallest and add to the value List
	 * @param characterValues
	 * @param finaValues
	 * @throws Exception
	 */
	public NumericCharacterValue findMinCharacter(List<NumericCharacterValue> characterValues) throws Exception{
		double[] minValues =  new double[characterValues.size()];
		for(int i=0;i<characterValues.size();i++){
			minValues[i] = detectMin(characterValues.get(i).getValue());
		}
		double min = 10000;
		NumericCharacterValue  minCV  = null;
		for(int i=0;i<characterValues.size();i++){
			if(minValues[i]<min){//update
				min = minValues[i];
				minCV = (NumericCharacterValue)characterValues.get(i);
			}
		}
		return minCV;
	}
	
	
	/**
	 * find the smallest and add to the value List
	 * @param characterValues
	 * @param finaValues
	 * @throws Exception
	 */
	public NumericCharacterValue findMaxCharacter(List<NumericCharacterValue> characterValues) throws Exception{
		double[] maxValues =  new double[characterValues.size()];
		for(int i=0;i<characterValues.size();i++){
			maxValues[i] = detectMax(characterValues.get(i).getValue());
		}
		double max = -10000;
		NumericCharacterValue  maxCV  = null;
		for(int i=0;i<characterValues.size();i++){
			if(maxValues[i]>max){//update
				max = maxValues[i];
				maxCV = (NumericCharacterValue)characterValues.get(i);
			}
		}
		return maxCV;
	}
	
	

	public static void main(String[] args){
		NumericCharacterValue ncv1 = CharacterValueFactory.createNumericValue(Label.c23, "8", null);
		NumericCharacterValue ncv2 = CharacterValueFactory.createNumericValue(Label.c23, "9", null);
		
		List ncvList = new ArrayList();
		ncvList.add(ncv1);
		ncvList.add(ncv2);
		PostProcessor pp = new PostProcessor();
		pp.distinctRestrain(ncvList, Label.c23);
		System.out.println(ncvList);
	}

}