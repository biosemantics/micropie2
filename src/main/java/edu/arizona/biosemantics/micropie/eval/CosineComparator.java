package edu.arizona.biosemantics.micropie.eval;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.Vector;


/**
 * compare the value by exact string matching
 * @author maojin
 *
 */
public class CosineComparator implements IValueComparator{

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
				match+=cosine(extValueStr,gstValueStr);
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
	
	private static float cosine(String targetCellValue,
			String goldCellValue) {

		targetCellValue = targetCellValue.trim();
		goldCellValue = goldCellValue.trim();
		
		Set<String> stringVector = new HashSet<String>();

		String[] targetCellValueArray = targetCellValue.split("\\s+");
		for (String itemInTargetCellValueArray : targetCellValueArray)
			stringVector.add(itemInTargetCellValueArray);

		// System.out.println("targetCellValueArray.length::" +
		// targetCellValueArray.length);

		String[] goldCellValueArray = goldCellValue.split("\\s+");
		for (String itemInGoldCellValueArray : goldCellValueArray)
			stringVector.add(itemInGoldCellValueArray);

		// System.out.println("goldCellValueArray.length::" +
		// goldCellValueArray.length);

		// System.out.println("stringVector.toString()::" +
		// stringVector.toString());

		List<Double> targetVector = new ArrayList<Double>();
		List<Double> goldVector = new ArrayList<Double>();

		Iterator stringVectorIter = stringVector.iterator();
		while (stringVectorIter.hasNext()) {
			// System.out.println(stringVectorIter.next());
			String vectorElement = stringVectorIter.next().toString();

			boolean isInStringVector = false;

			for (String itemInTargetCellValueArray : targetCellValueArray) {
				if (vectorElement.equals(itemInTargetCellValueArray))
					isInStringVector = true;
			}

			if (isInStringVector == true)
				targetVector.add(1.0);
			else
				targetVector.add(0.0);

			isInStringVector = false;

			for (String itemInGoldCellValueArray : goldCellValueArray) {
				if (vectorElement.equals(itemInGoldCellValueArray))
					isInStringVector = true;
			}

			if (isInStringVector == true)
				goldVector.add(1.0);
			else
				goldVector.add(0.0);

		}

		double[] xdata = new double[targetVector.size()];
		for (int i = 0; i < xdata.length; i++) {
			xdata[i] = targetVector.get(i);
		}

		double[] ydata = new double[goldVector.size()];
		for (int i = 0; i < ydata.length; i++) {
			ydata[i] = goldVector.get(i);
		}
		Vector x = new Vector(xdata);
		Vector y = new Vector(ydata);

		float returnFloat = (float) (x.dot(y) / (float) (x.magnitude() * y
				.magnitude()));

		return returnFloat;
	}
	
}
