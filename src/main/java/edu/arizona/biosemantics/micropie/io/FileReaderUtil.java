package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;




public class FileReaderUtil {

	/**
	 * read the file as the UTF-8 charset.
	 * @param fileName
	 * @return
	 */
	public String readFile(String fileName){
		InputStream inputStream;
		StringBuffer sb = new StringBuffer();
		try {			
			inputStream = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
			String line = null;
			while((line=br.readLine())!=null){
				sb.append(line);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		return sb.toString();
	}
}
