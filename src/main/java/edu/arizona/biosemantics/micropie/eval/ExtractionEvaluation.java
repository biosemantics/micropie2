package edu.arizona.biosemantics.micropie.eval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.arizona.biosemantics.micropie.model.ExtractedOutputSimilarityResults;
import edu.arizona.biosemantics.micropie.model.Vector;
import au.com.bytecode.opencsv.CSVReader;

public class ExtractionEvaluation {

	public ExtractionEvaluation() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub

		// String targetCellValue0 = "A,C,D,E";
		// String goldCellValue0 = "A,B,C";
		// System.out.println(similarityComparison(targetCellValue0, goldCellValue0));
		
		// String targetCellValue0 = "rods,,rods,spherical";
		// String goldCellValue0 = "rods,,rods,spherical";
		// System.out.println(similarityComparison(targetCellValue0, goldCellValue0));
		// {
		//  "precisionValue" : 1.0,
		//  "recallValue" : 1.0,
		//  "fvalue" : 1.0
		// }
		
		// String targetCellValue0 = "Tween";
		// String goldCellValue0 = "Tween 40";
		// System.out.println("cosineSimilarityCalculation::" + cosineSimilarityCalculation(targetCellValue0, goldCellValue0));
		
		
		
		
		ExtractedOutputSimilarityResults extractedOutputSimilarityResults = new ExtractedOutputSimilarityResults();
		
		
		InputStream inputStreamGold;
		InputStream inputStream;
		try {
			inputStreamGold = new FileInputStream("matrix-gold.csv");
			inputStream = new FileInputStream("matrix-old.csv");

			CSVReader readerGold = new CSVReader(new BufferedReader(
					new InputStreamReader(inputStreamGold, "UTF8")));
			CSVReader reader = new CSVReader(new BufferedReader(
					new InputStreamReader(inputStream, "UTF8")));

			List<String[]> linesGold = readerGold.readAll();
			List<String[]> lines = reader.readAll();

			// System.out.println(linesGold.get(0)[0]);//taxon
			// System.out.println(linesGold.get(0)[1]);//16S rRNA accession #

			
			
			
			
			StringBuilder outputStringBuilder = new StringBuilder("");
			
			StringBuilder totalStatOutputStringBuilder = new StringBuilder("");
			
			
			
			outputStringBuilder.append("\"Taxon Name\","
					+ "\"Character Name\","
					+ "\"Gold Standard\","
					+ "\"Extracted Output\","
					+ "\"Similarity(F-Value(%))\","
					+ "\"Precision(%)\","
					+ "\"Recall(%)\","
					+ "\"Extracted Date\""
					+ "\n"
					);
			
			
			totalStatOutputStringBuilder.append("\"Precision(%)\","
					+ "\"Recall(%)\","
					+ "\"Similarity(F-Value(%))\","
					+ "\"Extracted Date\""
					+ "\n"
					);
			
			
			
			float totalPrecsionValue = 0;
			float totalRecallValue = 0;
			float totalFValue = 0;
			int totalCounter = 0;
			
			Date date = new Date();
	        // System.out.println(date.toString());
			
			//for (int i = 1; i < linesGold.get(0).length; i++) {
			for (int i = 1; i < lines.get(0).length; i++) { // Go through to each column
				String targetCharName = lines.get(0)[i];
				
				// System.out.println(targetCharName); // Target column name
				// System.out.println(lines.size()); // Target Number of rows
				
				for (int j = 1; j < lines.size(); j++) { // Go through to each row
					// Start from first row
					String targetTaxonName = lines.get(j)[0]; // Target taxon name
					String targetCellValue = lines.get(j)[i];
					
					
					// the position of gold standard is not the same as target so you need to 
					// go through and find it out
					// 
					for (int k = 1; k < linesGold.get(0).length; k++) {
						String goldCharName = linesGold.get(0)[k];
						
						for (int l = 1; l < linesGold.size(); l++) {
							String goldTaxonName = linesGold.get(l)[0];
							String goldCellValue = linesGold.get(l)[k];

							if (targetCharName.equals(goldCharName) &&
									targetTaxonName.equals(goldTaxonName)) {
								// System.out.println("Yes, we have!!");
								
								extractedOutputSimilarityResults = similarityComparison(targetCellValue, goldCellValue);
								float precisionValue = extractedOutputSimilarityResults.getPrecisionValue();
								float recallValue = extractedOutputSimilarityResults.getRecallValue();
								float similarityFValue = extractedOutputSimilarityResults.getFValue();

								if ( precisionValue > 1)
									System.out.println("precisionValue:" + precisionValue + " > 1");
								
								if ( recallValue > 1)
									System.out.println("recallValue:" + recallValue + " > 1");
								// System.out.println(similarityFValue);

								
								
								
								
								outputStringBuilder.append("\"" + targetTaxonName + "\",");
								outputStringBuilder.append("\"" + targetCharName + "\",");

								outputStringBuilder.append("\"" + goldCellValue + "\",");
								outputStringBuilder.append("\"" + targetCellValue + "\",");
								
								outputStringBuilder.append("\"" + precisionValue + "\",");
								outputStringBuilder.append("\"" + recallValue + "\",");
								outputStringBuilder.append("\"" + similarityFValue + "\",");
								
								outputStringBuilder.append("\"" + date.toString() + "\"");
								outputStringBuilder.append("\n");
								
								
								totalPrecsionValue += precisionValue;
								totalRecallValue += recallValue;
								totalFValue += similarityFValue;
								totalCounter+=1;
								
							}
						}
					}
				}
			}
			
			
			// System.out.println("totalCounter::" + totalCounter);
			// System.out.println("totalPrecsionValue::" + totalPrecsionValue);
			// System.out.println("totalRecallValue::" + totalRecallValue);
			
			float averagePrecisionValue = totalPrecsionValue / (float) totalCounter;
			float averageRecallValue = totalRecallValue / (float) totalCounter;
			float averageFValue = totalFValue / (float) totalCounter;
			

			totalStatOutputStringBuilder.append("\"" +averagePrecisionValue + "\",");
			totalStatOutputStringBuilder.append("\"" + averageRecallValue + "\",");
			totalStatOutputStringBuilder.append("\"" + averageFValue + "\",");
			totalStatOutputStringBuilder.append("\"" + date.toString() + "\"");
			totalStatOutputStringBuilder.append("\n");
			
			

			try (PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter("GoldStandardEval.csv", true)))) {
				out.println(outputStringBuilder);
			} catch (IOException e) {
				// exception handling left as an exercise for the reader
			}
			
			try (PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter("GoldStandardEvalTotalStat.csv", true)))) {
				out.println(totalStatOutputStringBuilder);
			} catch (IOException e) {
				// exception handling left as an exercise for the reader
			}			
			
			
			
			System.out.println("Done on creating Gold Standard Evalution!");
			readerGold.close();
			reader.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		

	}
	
	private static ExtractedOutputSimilarityResults similarityComparison(String targetCellValue, String goldCellValue) {
		
		ExtractedOutputSimilarityResults extractedOutputSimilarityResults = new ExtractedOutputSimilarityResults();
		
		String[] targetCellValueArray = targetCellValue.split(",");
		String[] goldCellValueArray = goldCellValue.split(",");
		
		// remove empty item in array
		// ArrayList<String> list = new ArrayList<String>();
		Set<String> setList = new HashSet<String>();
		for (String s : targetCellValueArray)
		    if (!s.equals("")) {
		    	// list.add(s);
		    	setList.add(s);
		    }
		// targetCellValueArray = list.toArray(new String[list.size()]);
		targetCellValueArray = setList.toArray(new String[setList.size()]);
		
		// ArrayList<String> list2 = new ArrayList<String>();
		Set<String> setList2 = new HashSet<String>();
		for (String s : goldCellValueArray)
		    if (!s.equals(""))
		    	setList2.add(s);
		goldCellValueArray = setList2.toArray(new String[setList2.size()]);		
		
		
		
		
		
		float similarityValue = 0;
		float precisionFloat = 0;
		float recallFloat = 0;
		
		if (goldCellValueArray.length == 0 && targetCellValueArray.length > 0) {
			similarityValue = 0;
			precisionFloat = 0;
			recallFloat = 0;
		} else if (goldCellValueArray.length == 0 && targetCellValueArray.length == 0) {
			similarityValue = 1;
			precisionFloat = 1;
			recallFloat = 1;			
		} else if (goldCellValueArray.length > 0) {
			float correctNumber = 0;
			for (int i = 0; i < goldCellValueArray.length; i++) {
				
				for (int j = 0; j < targetCellValueArray.length; j++) {
					if (targetCellValueArray[j].length() > 0 &&
							goldCellValueArray[i].length() > 0)
						// System.out.println("goldCellValueArray[i]::" + goldCellValueArray[i]);
						// System.out.println("targetCellValueArray[j]::" + targetCellValueArray[j]);
						// System.out.println("cosineSimilarityCalculation::" + cosineSimilarityCalculation(targetCellValueArray[j], goldCellValueArray[i]));
						correctNumber += cosineSimilarityCalculation(targetCellValueArray[j], goldCellValueArray[i]);
						
					
					// if (targetCellValueArray[j].length() > 0 &&
					// 		goldCellValueArray[i].length() > 0 &&
					// 		goldCellValueArray[i].equals(targetCellValueArray[j])) 
					//	correctNumber +=1;					
					
				}
				
			}
			
			String precisionNumber = String.valueOf(correctNumber) + "/" + String.valueOf(targetCellValueArray.length);
			String recallNumber = String.valueOf(correctNumber) + "/" + String.valueOf(goldCellValueArray.length);
			
			// System.out.println("precisionNumber:" + precisionNumber);
			// System.out.println("recallNumber:" + recallNumber);
			
			precisionFloat = Float.parseFloat(String.valueOf(correctNumber))/Float.parseFloat(String.valueOf(targetCellValueArray.length));
			recallFloat = Float.parseFloat(String.valueOf(correctNumber))/Float.parseFloat(String.valueOf(goldCellValueArray.length));
			
			if (precisionFloat > 1)
				precisionFloat = 1;
			if (recallFloat > 1)
				recallFloat = 1;
			
			float fValue = 2 * precisionFloat * recallFloat / (precisionFloat + recallFloat);
			
			// System.out.println("precisionFloat:" + precisionFloat);
			// System.out.println("recallFloat:" + recallFloat);
			// System.out.println("fValue:" + fValue);


			similarityValue = fValue;
		}
		
		extractedOutputSimilarityResults.setPrecisionValue(precisionFloat);
		extractedOutputSimilarityResults.setRecallValue(recallFloat);
		extractedOutputSimilarityResults.setFValue(similarityValue);
		// String similarityString = "";
		// return similarityString;
		// return similarityValue;
		return extractedOutputSimilarityResults;
	}
	
	
	
	private static float cosineSimilarityCalculation(String targetCellValue, String goldCellValue) {
		
		Set<String> stringVector = new HashSet<String>();
		
		String[] targetCellValueArray = targetCellValue.split("\\s+");
		for (String itemInTargetCellValueArray : targetCellValueArray)
			stringVector.add(itemInTargetCellValueArray);
		
		// System.out.println("targetCellValueArray.length::" + targetCellValueArray.length);

		String[] goldCellValueArray = goldCellValue.split("\\s+");
		for (String itemInGoldCellValueArray : goldCellValueArray)
			stringVector.add(itemInGoldCellValueArray);		
		
		// System.out.println("goldCellValueArray.length::" + goldCellValueArray.length);
		
		// System.out.println("stringVector.toString()::" + stringVector.toString());

		List<Double> targetVector = new ArrayList<Double>();
		List<Double> goldVector = new ArrayList<Double>();
		
		Iterator stringVectorIter = stringVector.iterator();
		while (stringVectorIter.hasNext()) {
			//System.out.println(stringVectorIter.next());
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
			
		// System.out.println("targetVector:" + targetVector.toString());
		// System.out.println("goldVector:" + goldVector.toString());
		
		double []xdata = new double[targetVector.size()];
		for (int i = 0; i < xdata.length; i++) {
			xdata[i] = targetVector.get(i);
		}

		double []ydata = new double[goldVector.size()];
		for (int i = 0; i < ydata.length; i++) {
			ydata[i] = goldVector.get(i);
		}
		
		
		
		// double[] xdata = { 1.0, 2.0, 3.0, 4.0 };
        // double[] ydata = { 5.0, 2.0, 4.0, 1.0 };

        Vector x = new Vector(xdata);
        Vector y = new Vector(ydata);
        
        
        
        // System.out.println("x        =  " + x);
        // System.out.println("y        =  " + y);
        // System.out.println("x + y    =  " + x.plus(y));
        // System.out.println("10x      =  " + x.times(10.0));
        // System.out.println("|x|      =  " + x.magnitude());
        // System.out.println("<x, y>   =  " + x.dot(y));
        // System.out.println("|x - y|  =  " + x.minus(y).magnitude());
        
        // System.out.println("|x|      =  " + x.magnitude());
        // System.out.println("|y|      =  " + y.magnitude());
		
		
		
		//float returnFloat = 0;
        float returnFloat = (float) (x.dot(y) / (float) (x.magnitude() * y.magnitude()));
        
        return returnFloat;
	}
	
	
	

}

