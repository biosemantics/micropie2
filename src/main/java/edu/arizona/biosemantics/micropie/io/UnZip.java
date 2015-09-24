package edu.arizona.biosemantics.micropie.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import edu.arizona.biosemantics.micropie.transform.StringUtil;

public class UnZip {
	List<String> fileList;
	private static final String INPUT_ZIP_FILE = "F:\\MicroPIE\\datasets\\Microbial_Phenomics_Project_Experiment1_papers-2015-09-14.zip";
	private static final String OUTPUT_FOLDER = "F:\\MicroPIE\\datasets\\exp1";

	public static void main(String[] args) {
		UnZip unZip = new UnZip();
		unZip.unZipIt(INPUT_ZIP_FILE, OUTPUT_FOLDER);
	}

	/**
	 * Unzip it
	 * 
	 * @param zipFile
	 *            input zip file
	 * @param output
	 *            zip file output folder
	 */
	public void unZipIt(String zipFileStr, String outputFolder) {

		byte[] buffer = new byte[1024];

		try {

			// create output directory is not exists
			// File folder = new File(OUTPUT_FOLDER);
			File folder = new File(outputFolder);
			
			
			if (!folder.exists()) {
				folder.mkdir();
			}
			
			 Charset charset = Charset.forName("CP866");//CP866
			//ZipFile zipFile = new ZipFile(zipFileStr, CP866);

			// get the zip file content
			ZipInputStream zis = new ZipInputStream(
					new FileInputStream(zipFileStr),charset);
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				fileName = fileName.replace("Microbial Phenomics Project Experiment1 papers", "");
				File newFile = new File(outputFolder + File.separator
						+ StringUtil.standFileName(fileName));

				System.out.println("file unzip : "+ newFile.getName());

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

			System.out.println("UnZip Done");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}

