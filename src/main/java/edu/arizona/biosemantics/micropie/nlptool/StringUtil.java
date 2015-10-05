package edu.arizona.biosemantics.micropie.nlptool;

public class StringUtil {
	/**
	 * the capital character and the sign _ with their two followings
	 * @param fileName
	 * @return
	 */
	public static String standFileName(String fileName){
		int length = fileName.length();
		StringBuffer finalName = new StringBuffer();
		for(int i=0;i<length;i++){
			char c = fileName.charAt(i);
			if(c>='A'&&c<='Z'||c=='_'){
				finalName.append(c);
				if(i<length-1) finalName.append(fileName.charAt(++i));
				if(i<length-1) finalName.append(fileName.charAt(++i));
			}
		}
		finalName.append(".xml");
		return finalName.toString();
	}
}
