package edu.arizona.biosemantics.micropie.extract;

import java.util.List;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.model.CharacterValue;


/**
 * post processor
 * It should be used after the extraction and before the parsing.
 * @author maojin
 *
 */
public class PostProcessor {
	
	
	/**
	 * 
	 * post process the extracted values according to some rules:
	 * For 9.1,9.3 and 9.5 if the value is negative, chage it to another value
	 * 
	 * @param valueList
	 */
	public void postProcessor(List<CharacterValue> valueList){
		
		//post-process one by one
		for(int i=0;i<valueList.size();){
			CharacterValue aValue = valueList.get(i);
			ILabel valueLabel = aValue.getCharacter();
			if(valueLabel==null){
				i++;
				continue;
			}else{
				if(Label.c53.equals(valueLabel)||Label.c55.equals(valueLabel)||Label.c57.equals(valueLabel)){
					negationToAnother(aValue);
				}else if(Label.c32.equals(valueLabel)||Label.c33.equals(valueLabel)){
					negationReverse(aValue);
				}
				
				i++;
			}
		}
	}

	
	/**
	 * if the value is negation, change it to the corresponding negative character.
	 * @param aValue
	 */
	public void negationToAnother(CharacterValue aValue) {
		ILabel valueLabel = aValue.getCharacter();
		valueLabel.getValue();
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
	
	
	public static void main(String[] args){
		ILabel valueLabel = Label.c52;
		System.out.println(valueLabel.getValue());
		Label newLabel = Label.getEnum((new Integer(valueLabel.getValue())+1)+"");
		System.out.println(newLabel.getValue());
	}
	
	
	
	

}