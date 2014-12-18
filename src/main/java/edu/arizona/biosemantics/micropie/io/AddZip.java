package edu.arizona.biosemantics.micropie.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AddZip {
	List<String> fileList;
	
	private static final String OUTPUT_ZIP_FILE = "/home/sbs0457/git/micropie2/AddZipTest.zip";
	private static final String SOURCE_FOLDER = "/home/sbs0457/git/micropie2/AddZipTest";

	
	public AddZip() {
		fileList = new ArrayList<String>();
	}

	public static void main(String[] args) {
		AddZip addZip = new AddZip();
		addZip.generateFileList(new File(SOURCE_FOLDER));
		addZip.zipIt(SOURCE_FOLDER, OUTPUT_ZIP_FILE);
	}

	/**
	 * Zip it
	 * 
	 * @param zipFile
	 *            output ZIP file location
	 */
	public void zipIt(String zipfolder, String zipFile) {

		byte[] buffer = new byte[1024];

		try {

			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);

			System.out.println("Output to Zip : " + zipFile);
			System.out.println("zipfolder : " + zipfolder);
			
			for (String file : this.fileList) {

				System.out.println("File Added : " + file);
				ZipEntry ze = new ZipEntry(file);
				zos.putNextEntry(ze);

				FileInputStream in = new FileInputStream(zipfolder
						+ File.separator + file);

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}

				in.close();
			}

			zos.closeEntry();
			// remember close it
			zos.close();

			System.out.println("Done");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Traverse a directory and get all files, and add the file into fileList
	 * 
	 * @param node
	 *            file or directory
	 */
	public void generateFileList(File node) {

		
		System.out.println("generateFileList(File node)::node.getAbsolutePath()::" + node.getAbsolutePath());
		System.out.println("generateFileList(File node)::node.getPath()::" + node.getPath());

		
		// add file only
		if (node.isFile()) {
			// fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
			// fileList.add(generateZipEntry(node.getPath().getAbsolutePath()));
			fileList.add(generateZipEntry(node.getPath()));
		}

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				generateFileList(new File(node, filename));
			}
		}
		
	}

	/**
	 * Format the file path for zip
	 * 
	 * @param file
	 *            file path
	 * @return Formatted file path
	 */
	private String generateZipEntry(String file) {
		return file.substring(SOURCE_FOLDER.length() + 1, file.length());
	}
}
