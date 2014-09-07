package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.jdom2.Element;

public class XlsReader implements IXlsReader {

	public XlsReader() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		XlsReader xlsReader = new XlsReader();
		// xlsReader.read("AdditionalSentences.xls");
		// xlsReader.read("xls2csv-1.xls");
		xlsReader.read("matrix-140616-semigold-standard.xls");
	}

	
	@Override
	public void read(String targetFileName) throws Exception {
		
		try {
			
			StringBuilder csvStringBuilder = new StringBuilder();
			
			FileInputStream file = new FileInputStream(new File(targetFileName));
			//Get the workbook instance for XLS file
			HSSFWorkbook workbook = new HSSFWorkbook(file);
			//Get first sheet from the workbook
			
			//Get the number of sheets in the xlsx file
			int numberOfSheets = workbook.getNumberOfSheets();
			
			//loop through each of the sheets
			for(int i=0; i < numberOfSheets; i++){
				HSSFSheet sheet = workbook.getSheetAt(i);

				//Iterate through each rows from first sheet
				Iterator<Row> rowIterator = sheet.iterator();
				
				while(rowIterator.hasNext()) {
					
					Row row = rowIterator.next();
					
					//For each row, iterate through each columns
					
					Iterator<Cell> cellIterator = row.cellIterator();
					
					while(cellIterator.hasNext()) {

						Cell cell = cellIterator.next();
						
						switch(cell.getCellType()) {
						
							case Cell.CELL_TYPE_BOOLEAN:
								System.out.print(cell.getBooleanCellValue() + "\t\t");
								// csvStringBuilder.append(cell.getBooleanCellValue() + "\t");
								csvStringBuilder.append("\"" + cell.getBooleanCellValue() + "\",");
								break;
							case Cell.CELL_TYPE_NUMERIC:
								System.out.print(cell.getNumericCellValue() + "\t\t");
								// csvStringBuilder.append(cell.getNumericCellValue() + "\t");
								
								int intValue = (int) cell.getNumericCellValue();
								
								
								csvStringBuilder.append("\"" + intValue + "\",");
								
								break;
								
							case Cell.CELL_TYPE_STRING:
								System.out.print(cell.getStringCellValue() + "\t\t");
								// csvStringBuilder.append(cell.getStringCellValue() + "\t");
								
								String cellValueString = cell.getStringCellValue();
								cellValueString = cellValueString.replace("mol%", "");
								cellValueString = cellValueString.replace("mol %", "");
								cellValueString = cellValueString.replace("°C", "");
								cellValueString = cellValueString.replace("%", "");
								cellValueString = cellValueString.replace("(w/v)", "");
								
								csvStringBuilder.append("\"" + cell.getStringCellValue() + "\",");
								break;
						}
						
					}
					csvStringBuilder.append("\n");
					System.out.println("");
				}
				
				file.close();
				
				
				String targetFileNameArray[] = targetFileName.split("\\.");
				String outputTargetFileName = "";
				if ( targetFileNameArray.length > 1 ) {
					outputTargetFileName = targetFileNameArray[0];
				}
				
				if ( outputTargetFileName.equals("") ) {
					outputTargetFileName = "tempOutput.csv";
				}
				
				
				try (PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter(outputTargetFileName+ ".csv",
								false)))) {
					out.println(csvStringBuilder);
				} catch (IOException e) {
					// exception handling left as an exercise for the reader
				}
				
				
			}

			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
				
	}	
	
	
}
