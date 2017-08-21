package edu.arizona.biosemantics.micropie.nlptool;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		int lastIndex  = fileName.lastIndexOf("_");
		if(lastIndex>-1){
			finalName.append(fileName.substring(lastIndex, fileName.lastIndexOf(".")));
		}
		finalName.append(new Random().nextInt(1000));
		finalName.append(".xml");
		return finalName.toString();
	}
	
	/**
	 * 
	 * str = str.replaceAll( "([\\ud800-\\udbff\\udc00-\\udfff])", "");
	 * 
	 * remove none utf8 char
	 * @param text
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String filterOffUtf8Mb4(String text){
		byte[] bytes;
		try {
			bytes = text.getBytes("UTF-8");
			ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
			System.out.println(bytes.length);
			int i = 0;
			while (i < bytes.length) {
				short b = bytes[i];
				if (b > 0) {
					buffer.put(bytes[i++]);
					continue;
				}
				b += 256;
				if ((b ^ 0xC0) >> 4 == 0) {
					buffer.put(bytes, i, 2);
					i += 2;
				}
				else if ((b ^ 0xE0) >> 4 == 0) {
					buffer.put(bytes, i, 3);
					i += 3;
				}
				else if ((b ^ 0xF0) >> 4 == 0) {
					i += 4;
				}
			}
			buffer.flip();
			//System.out.println(bytes.length);
			return new String(buffer.array(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return text;
	}

	/**
	 * check whether a string is contained in the term list
	 * @param terms
	 * @param string
	 * @return
	 */
	public static boolean contains(String[] terms, String string) {
		for(String term:terms){
			if(term.equalsIgnoreCase(string)) return true;
		}
		return false;
	}
	
	/**
	 * check whether its a punctuation
	 * @param str
	 * @return
	 */
	public static boolean isPunctuation(String str){
		char c = str.charAt(0);
        if(str.length()==1&&(c == ','
            || c == '.'
            || c == '!'
            || c == '?'
            || c == ':'
            || c == ';'))	return true;
        return false;
	}
	
	public static boolean hasPunctuation(String str){
		Pattern p = Pattern.compile("\\p{Punct}");
		Matcher m = p.matcher(str);
		if (m.find())
			return true;
		return false;
	}
	
	public static String getFileName(String filePath){
		int lastSplashIndex = filePath.lastIndexOf("/");
		int firstDotIndex = filePath.indexOf(".");
		return filePath.substring(lastSplashIndex+1, firstDotIndex);
	}
	
	public static String replaceStanfordWilds(String str){
		str=str.replace("-LRB-","(");
		str=str.replace("-RRB-",")");
		str=str.replace("-LSB-","[");
		str=str.replace("-RSB-","]");
		str=str.replace("''","\"");
		str=str.replace("``","\"");
		return str;
	}
	
	public static boolean hasDigit(String content) {
		boolean flag = false;
		Pattern p = Pattern.compile(".*\\d+.*");
		Matcher m = p.matcher(content);
		if (m.matches())
		flag = true;
		return flag;
	}
	
	
	/**
	 * presents any digit
	 * @param str
	 * @return
	 */
	public int presentsDigit(String str){
		for(int i = 0;i < str.length();i++){
			if(Character.isDigit(str.charAt(i))){
				return 1;
			}
		}//
		return 0;
	}
	
	public static boolean hasCapital(String str) {
		for(int i = 0;i < str.length();i++){
			if(Character.isUpperCase(str.charAt(i))){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * separate white space, dot, hyphen
	 * @param str
	 * @return
	 */
	public static List<String> ruleTokenize(String str){
		List<String> tokens = new ArrayList();
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<str.length();i++){
			//if(str.charAt(i)=='-'||str.charAt(i)==' '||str.charAt(i)=='.'||str.charAt(i)=="'".charAt(0)||Character.isLetterOrDigit(ch)){
			if(!(str.charAt(i)==','||Character.isLetterOrDigit(str.charAt(i)))){
				String ntoken = sb.toString().trim();
				if(!ntoken.equals("")) tokens.add(ntoken);
				if(!Character.isWhitespace(str.charAt(i))) tokens.add(str.charAt(i)+"");
				 sb = new StringBuffer();
			}else{
				sb.append(str.charAt(i));
			}
		}
		String ntoken = sb.toString().trim();
		if(!ntoken.trim().equals("")) tokens.add(ntoken);
		return tokens;
	}
	
	/**
	 * Different typographic types
	 * replace all capitalized characters with A
	 * replace all lowercases with a
	 * shorten the string
	 * 
	 * 
	 * digits?
	 * O --- not characters
	 * @return
	 */
	public static String getTypographic(String str){
		char[] chars = str.toCharArray();
		for(int i = 0;i < chars.length;i++){
			if(Character.isUpperCase(chars[i])){
				chars[i]= 'A';
			}else if(Character.isLowerCase(chars[i])){
				chars[i]= 'a';
			}else if(Character.isDigit(chars[i])){
				chars[i]= '1';
			}
		}//
		char lastChar = chars[0];
		int length = 1;
		StringBuffer sb = new StringBuffer();
		sb.append(chars[0]);
		for(int i = 1;i < chars.length;){
			if(chars[i]==lastChar){
				if(length>2) i++;
				else{
					sb.append(chars[i]);
					length++;
					i++;
				}
			}else{
				sb.append(chars[i]);
				lastChar = chars[i];
				i++;
				length=1;
			}
			
		}
		return sb.toString();
	}
	
	
	public static String matchShortTerm(String longTerm, String shortTerm){
	
		String patternString = "\\s"+shortTerm+"\\s|^"+shortTerm+"\\s|\\s"+shortTerm+"$|^"+shortTerm+"$"; // regular expression pattern
		try{
			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(longTerm);			
			if (matcher.find()) {
				return shortTerm;
			}
		}catch(Exception e){
			return null;
		}
		return null;
	}
	
	public static void main(String[] args){
		String str = " Mitsuokella jalaludinii , GsQsLansYsWsHosNsAbdullah2002_GENUS_Mitsuokellasunspecified_SPECIES_jalaludiniisunspecified.xml ,cell shape, small#stout rods , small#stout rods , stout|rods#irregular|groups#short|chains#pairs ,2.0,4.0,0.0,0.0, Mitsuokella jalaludinii (jal.al.u.di«ni.i.N.L. gen. n. jalaludinii of Jalaludin, in honour of  S .Jalaludin, an animal nutritionist and Vice-Chancellor of Universiti Putra Malaysia, who has contributed significantly to rumen microbiology).Gram-negative, non-spore-forming, non-motile, <span style='background:#8DA2E0'>small</span>  <span style='background:#8DA2E0'>stout rods</span> (1.2-2.4 µm in length and 0.6-0.8 µm in width).Cells occur singly, in <span style='background:#E0CB8D'>pairs</span>  in short <span style='background:#E0CB8D'>chains</span> or in irregular <span style='background:#E0CB8D'>groups</span> Colonies on PYG agar after 2 d incubation are greyish-white, roughly circular and convex with smooth surfaces and regular edges.Strictly anaerobic.The final pH in PYG broth is 3.8-4.0.There is no copious gas formation from glucose.The major end-products from glucose fermentation are lactic acid, succinic acid and some acetic acid.Growth is stimulated by fermentable carbohydrates but not by bile.The optimum growth temperature is 42 °C.Growth occurs at 45 and 47 °C.Gelatin liquefaction, indole production, catalase production, urease production, hydrogen sulfide production, Tween 80 hydrolysis, arginine acid decarboxylase and growth in 4.5% sodium chloride are negative.The nitrate-reduction test, the methyl red test and the Voges-Proskauer reaction are positive.Glycerol, D-arabinose, ribose, L-xylose, galactose, glucose, fructose, D-mannose, inositol, sorbitol, arbutin, aesculin, salicin, cellobiose, maltose, lactose, melibiose, saccharose, trehalose, D-raffinose, amidon, D-turanose, D-arabitol and 5-keto-gluconate are fermented.Erythritol, D-arabinose, L-xylose, adonitol, methyl β-xyloside, L-sorbose, rhamnose, dulcitol, mannitol, methyl α-D-mannoside, methyl α-D-glucoside, N-acetylglucosamine, amygdalin, inulin, melezitose, glycogen, xylitol, β-gentiobiose, D-lyxose, D-tagatose, D-fucose, L-fucose, L-arabitol, gluconate and 2-ketogluconate are not fermented.Resistant to kanamycin, neomycin, penicillin, bacitracin and erythromycin.Intolerant to 0.005% crystal violet and 0.001% brilliant green.Full 16S rRNA gene sequence similarity and DNA-DNA relatedness to Mitsuokella multacida are 98.7 and 63.8%, respectively.The G­C content of the DNA is 56.8 mol%.Isolated from the rumens of cattle.The type strain is M 9 (DSM 13811, ATCC BAA-307). ";
		StringUtil.filterOffUtf8Mb4(str);
		
		System.out.println(str);
		str = str.replaceAll( "([\\ud800-\\udbff\\udc00-\\udfff])", "");
		System.out.println(str);
	}

	
}
