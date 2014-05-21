package semanticMarkup.ling.learn.auxiliary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import semanticMarkup.core.Treatment;

public class FileLoader {
	
	private String dir;
	private int count=0;
	
	private List<String>  fileNameList = new LinkedList<String>();
	private List<Integer> typeList = new ArrayList<Integer>();
	private List<String>  textList = new LinkedList<String>();
	private List<Treatment> treatmentList = new LinkedList<Treatment>();
	
	public FileLoader(String d) {
		this.dir = d;
	}
	
	public boolean load() {
		//System.out.println("Reading sentences:\n");		
		File myDir = new File(this.dir);
		File[] contents = myDir.listFiles();
		
		
		this.count=contents.length;
		for (int i = 0; i < count; i++) {
			File f = contents[i];
			//System.out.println(f.getName() + ": " + f.length());
			String name = f.getName();
			int type = this.getType(f.getName());
			//System.out.println(type);
			String s2 = this.dir+"/"+f.getName();
			//System.out.println(s2); 
			
			File file = new File(s2);
			BufferedReader reader = null;
			String text = null;
			try {
				reader = new BufferedReader(new FileReader(file));				
				text = reader.readLine();
				//System.out.println(text);
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			
			this.fileNameList.add(name);
			this.typeList.add(type);
			this.textList.add(text);
			
			Treatment tm = new Treatment(name,text);
			this.treatmentList.add(tm);
		}
		
		//for (int i=0;i<fileNameList.size();i++) {
		//	System.out.println(fileNameList.get(i)+sentList.get(i)+'\n');
		//}
		
		return true;
	}
	
	//determine if a file contains a character statement or a character state(description) statement, by the file name
	//0-character, 1-description
	private int getType(String fileName) {
		//System.out.println(fileName);
		fileName = fileName.replaceAll(".*\\.xml_","");//remove pdf.xml
		fileName = fileName.replaceAll("[^_]","");//remove all non_ charaters
		//System.out.println(fileName);
		if (fileName.length()==1)
			return 1;
		else
			return 0;
	}
	
	public List<String> getFileNameList() {
		return this.fileNameList;
	}
	
	public List<Integer> getTypeList() {
		return this.typeList;
	}
	
	public List<String> getTextList() {
		return this.textList;
	}
	
	public List<Treatment> getTreatmentList(){
		return this.treatmentList;
	}
	
	public int getCount() {
		return this.count;
	}
	
	public List<String> getUnknownWordList()
	{
		List<String> unknownList = new LinkedList<String>();
		
		for (int i=0;i<count;i++) {
			//System.out.println(i);
			//System.out.println(sentList.get(i));
			//System.out.println(fileNameList.get(i));
			if (textList.get(i)!= null) {
				//System.out.println(sentList.get(i).length());
				//System.out.println("\n");
				String[] tokenList = ((textList.get(i)).toLowerCase()).split("\\s");
				for (int x=0; x<tokenList.length; x++) {
					//System.out.println(i);
					//System.out.println(tokenList.length);
					System.out.println(tokenList[x]);
					unknownList.add(tokenList[x]);
				}
			}
		}
		
		return unknownList;
	}
	
}
