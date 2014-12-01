package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import edu.arizona.biosemantics.common.log.LogLevel;
import au.com.bytecode.opencsv.CSVReader;

class XlstoCSV {

	private static void xlsToCsv(File inputFile, File outputFile) {
		// For storing data into CSV files
		StringBuffer data = new StringBuffer();
		try {
			FileOutputStream fos = new FileOutputStream(outputFile);

			// Get the workbook object for XLS file
			HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(
					inputFile));
			// Get first sheet from the workbook
			HSSFSheet sheet = workbook.getSheetAt(0);
			Cell cell;
			Row row;

			/*
			 * // Iterate through each rows from first sheet Iterator<Row>
			 * rowIterator = sheet.iterator(); while (rowIterator.hasNext()) {
			 * row = rowIterator.next(); // For each row, iterate through each
			 * columns Iterator<Cell> cellIterator = row.cellIterator(); while
			 * (cellIterator.hasNext()) { cell = cellIterator.next();
			 * 
			 * switch (cell.getCellType()) { case Cell.CELL_TYPE_BOOLEAN:
			 * data.append("\"" + cell.getBooleanCellValue() + "\","); break;
			 * 
			 * case Cell.CELL_TYPE_NUMERIC: data.append("\"" +
			 * cell.getNumericCellValue() + "\","); break;
			 * 
			 * case Cell.CELL_TYPE_STRING: data.append("\"" +
			 * cell.getStringCellValue() + "\",");
			 * System.out.println("cell.getStringCellValue()::" +
			 * cell.getStringCellValue());
			 * 
			 * break;
			 * 
			 * case Cell.CELL_TYPE_BLANK: data.append("\"" + "" + "\","); break;
			 * 
			 * default: data.append("\"" + cell + "\","); } } data.append('\n');
			 * }
			 */

			for (int i = 0; i <= sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				for (int j = 0; j < row.getLastCellNum(); j++) {
					cell = row.getCell(j);
					if (cell == null) {
						data.append("\"\",");
					} else {
						data.append("\"" + row.getCell(j) + "\",");
					}

				}
				data.append("\n");
			}

			fos.write(data.toString().getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	// This is not a finished function with bugs ...
	// columnCellCounter
	private static void columnCellCounter(File inputFile, File outputFile) {
		// For storing data into CSV files
		StringBuffer data = new StringBuffer();
		try {
			FileOutputStream fos = new FileOutputStream(outputFile);

			// Get the workbook object for XLS file
			HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(
					inputFile));
			// Get first sheet from the workbook
			HSSFSheet sheet = workbook.getSheetAt(0);
			Cell cell;
			Row row;

			// Iterate through each rows from first sheet
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				row = rowIterator.next();
				// For each row, iterate through each columns
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					cell = cellIterator.next();

					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_BOOLEAN:
						data.append("\"" + cell.getBooleanCellValue() + "\",");
						break;

					case Cell.CELL_TYPE_NUMERIC:
						data.append("\"" + cell.getNumericCellValue() + "\",");
						break;

					case Cell.CELL_TYPE_STRING:
						data.append("\"" + cell.getStringCellValue() + "\",");
						System.out.println("cell.getStringCellValue()::"
								+ cell.getStringCellValue());

						break;

					case Cell.CELL_TYPE_BLANK:
						data.append("\"\",");
						break;

					default:
						data.append("\"" + cell + "\",");
					}
				}
				data.append('\n');
			}

			fos.write(data.toString().getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// studentMatrixOperation
	private static void studentMatrixOperation(String inputFileName,
			String referenceFileName, String outputFileName) {

		InputStream inputStreamInputFile;
		InputStream inputStreamReferenceFile;

		try {

			inputStreamInputFile = new FileInputStream(inputFileName);
			inputStreamReferenceFile = new FileInputStream(referenceFileName);

			CSVReader readerInputFile = new CSVReader(new BufferedReader(
					new InputStreamReader(inputStreamInputFile, "UTF8")));
			CSVReader readerReferenceFile = new CSVReader(new BufferedReader(
					new InputStreamReader(inputStreamReferenceFile, "UTF8")));

			List<String[]> linesInputFile = readerInputFile.readAll();
			List<String[]> linesReferenceFile = readerReferenceFile.readAll();

			StringBuilder outputStringBuilder = new StringBuilder("");

			for (int i = 1; i < linesInputFile.size(); i++) {
				String[] lineArray = linesInputFile.get(i);
				String xmlFileName = linesInputFile.get(i)[0];

				// System.out.println("xmlFileName::" + xmlFileName);
				// System.out.println("xmlFileName.length()::" +
				// xmlFileName.length());

				if (xmlFileName.length() > 0) {
					int counter = 0;
					for (int j = 1; j < linesReferenceFile.size(); j++) {
						String xmlFileNameInReferenceFile = linesReferenceFile
								.get(j)[1]; // file
						String taxonNameInReferenceFile = linesReferenceFile
								.get(j)[0]; // taxon_name

						if (xmlFileName.equals(xmlFileNameInReferenceFile)) {
							System.out.println("xmlFileName::" + xmlFileName);
							counter += 1;
							outputStringBuilder.append("\""
									+ taxonNameInReferenceFile + "\",");

							for (int k = 0; k < lineArray.length; k++) {
								outputStringBuilder.append("\"" + lineArray[k]
										+ "\",");

							}
							outputStringBuilder.append("\n");

						}

					}
					// System.out.println("counter::" + counter);
					if (counter > 1) {
						System.out.println("counter::" + counter);
					}

				}

			}

			try (PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(outputFileName, false)))) {
				out.println(outputStringBuilder);
			} catch (IOException e) {
				// exception handling left as an exercise for the reader
			}

			// System.out.println("Done on creating Gold Standard Evalution!");
			readerInputFile.close();
			readerReferenceFile.close();

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

	// columnCellCounter2
	private static void columnCellCounter2(String inputFileName, String outputFileName) {

		InputStream inputStreamInputFile;
		InputStream inputStreamReferenceFile;

		try {

			inputStreamInputFile = new FileInputStream(inputFileName);

			CSVReader readerInputFile = new CSVReader(new BufferedReader(
					new InputStreamReader(inputStreamInputFile, "UTF8")));

			List<String[]> linesInputFile = readerInputFile.readAll();

			StringBuilder outputStringBuilder = new StringBuilder("");

			for (int i = 0; i < linesInputFile.get(0).length; i++) { // Go
																		// through
																		// to
																		// each
																		// column
				String columnName = linesInputFile.get(0)[i];
				int counter = 0;
				for (int j = 1; j < linesInputFile.size(); j++) {
					if (linesInputFile.get(j)[i].length() > 0) {
						counter += 1;
					}
				}
				System.out.println(columnName + "::" + counter);
			}

			/*
			 * try (PrintWriter out = new PrintWriter(new BufferedWriter( new
			 * FileWriter(outputFileName, false)))) {
			 * out.println(outputStringBuilder); } catch (IOException e) { //
			 * exception handling left as an exercise for the reader }
			 */

			// System.out.println("Done on creating Gold Standard Evalution!");
			readerInputFile.close();

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
	
	
	
	private static void csvToXls(String inputFileName, String outputFileName) {
		
		
		InputStream inputStreamInputFile;
	

		try {
			inputStreamInputFile = new FileInputStream(inputFileName);

			CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStreamInputFile, "UTF8")));
		    List<String[]> lines = reader.readAll();
			System.out.println("lines.size():" + lines.size());

			System.out.println("lines.get(7297):" + Arrays.toString(lines.get(7298)));
			
		    /*
			FileOutputStream fileOut = new FileOutputStream(outputFileName);
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet worksheet = workbook.createSheet("sheet1");

			System.out.println("lines.size():" + lines.size());
			for (int i = 0; i < lines.size(); i++) {
				HSSFRow rowContent = worksheet.createRow(i); // create row content
				String[] line = lines.get(i);
				for (int j = 0; j < line.length; j++) {
					System.out.println("line.length::" + line.length);
					if ( line.length > 9 ) {
						System.out.println("lines.get(i):" + Arrays.toString(lines.get(i)));
						System.out.println("line[3]:" + line[3]);
					}
					
					HSSFCell cellContent = rowContent.createCell(j);
					cellContent.setCellValue(line[j]);
				}
			}
			
			reader.close();
			workbook.write(fileOut);
			
			fileOut.flush();
			fileOut.close();
			*/
		    
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	    
		
	}
	
	

	public static void main(String[] args) {
		// File inputFile = new File(
		// "matrix-140626-GoldStandardforStudents-000.xls");
		// File outputFile = new File(
		// "matrix-140626-GoldStandardforStudents-000.csv");
		// xls(inputFile, outputFile);

		// File inputFile = new File(
		// "140819-StudentExperimentGoldStandardMatrix-000.xls");
		// File outputFile = new File(
		// "140819-StudentExperimentGoldStandardMatrix-000.csv");
		// xls(inputFile, outputFile);

		// matrix-140611-experiment.senttoElvis-Carrine-000.xls
		// File inputFile = new File(
		// "matrix-140611-experiment.senttoElvis-Carrine-000.xls");
		// File outputFile = new File(
		// "matrix-140611-experiment.senttoElvis-Carrine-000.csv");
		// xlsToCsv(inputFile, outputFile);

		// File inputFile = new File(
		// "matrix-140611-experiment.senttoElvis-Carrine-000.xls");
		// File outputFile = new File(
		// "matrix-140611-experiment.senttoElvis-Carrine-000-columnCellCounter.csv");
		// columnCellCounter(inputFile, outputFile);

		// File inputFile = new File(
		// "140819-StudentExperimentGoldStandardMatrix-111.xls");
		// File outputFile = new File(
		// "140819-StudentExperimentGoldStandardMatrix-111.csv");
		// xlsToCsv(inputFile, outputFile);

		// Microbial Phenomics Project Data Form experiment output 040214.xls
		// File inputFile = new File(
		// "Microbial Phenomics Project Data Form experiment output 040214.xls");
		// File outputFile = new File(
		// "Microbial Phenomics Project Data Form experiment output 040214.csv");
		// xlsToCsv(inputFile, outputFile);

		// String aaa =
		// "Microbial Phenomics Project Data Form experiment output 040214-xml-name-added.csv";
		// String bbb = "140819-StudentExperimentGoldStandardMatrix-111.csv";
		// String ccc =
		// "Microbial Phenomics Project Data Form experiment output 040214-xml-name-added-1.csv";

		// studentMatrixOperation(aaa, bbb, ccc);

		// String inputFile =
		// "Microbial Phenomics Project Data Form experiment output 040214-xml-name-added-1.csv";
		// String outputFile =
		// "Microbial Phenomics Project Data Form experiment output 040214-xml-name-added-ColumnCellCounter.csv";
		// columnCellCounter2(inputFile, outputFile);

		// File inputFile = new
		// File("140819-StudentExperimentGoldStandardMatrix.xls");
		// File outputFile = new
		// File("140819-StudentExperimentGoldStandardMatrix-Column-Cell-Counter.csv");
		// columnCellCounter2(inputFile, outputFile);

		// 140819-StudentExperimentGoldStandardMatrix.xls
		// File inputFile = new
		// File("140819-StudentExperimentGoldStandardMatrix.xls");
		// File outputFile = new
		// File("140819-StudentExperimentGoldStandardMatrix.csv");
		// xlsToCsv(inputFile, outputFile);

		// String inputFile2 = "140819-StudentExperimentGoldStandardMatrix.csv";
		// String outputFile2 ="140819-StudentExperimentGoldStandardMatrix-ColumnCellCounter.csv";
		// columnCellCounter2(inputFile2, outputFile2);

		// it is not good columnCellCounter2(file1, file2);

		// Microbial Phenomics Project Data Form experiment output 040214.xls
		// File inputFile = new File("Microbial Phenomics Project Data Form experiment output 040214.xls");
		// File outputFile = new File("Microbial Phenomics Project Data Form experiment output 040214.csv");
		// xlsToCsv(inputFile, outputFile);

		
		// String inputFileName = "GoldStandardCharStatEval.csv";
		// String outputFileName = "GoldStandardCharStatEval.xls";
		
		// GoldStandardEvalTotalStat.csv
		// String inputFileName = "GoldStandardEvalTotalStat.csv";
		// String outputFileName = "GoldStandardEvalTotalStat.xls";
		
		String inputFileName = "GoldStandardEval.csv";
		String outputFileName = "GoldStandardEval.xls";
		csvToXls(inputFileName, outputFileName);
		
	}
}
