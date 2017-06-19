package edu.arizona.biosemantics.micropie.eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class TranFile {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inputFile = "F:\\dataset\\Genomic\\genomics2_150611205606.sql";
		String outFile ="F:\\dataset\\Genomic\\genomics2_1506112056062.sql";
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
			FileWriter fw = new FileWriter(new File(outFile));
			String line = null;
			boolean needReplace = false;
			while((line=br.readLine())!=null){
					if(line.indexOf("mesh_rank_te_cos_100")>-1){
						fw.write(line);
						fw.write("\n");
					}
					
			}
			br.close();
			fw.flush();
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
