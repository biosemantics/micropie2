package edu.arizona.biosemantics.micropie.nlptool;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Random;

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
	
	public static void main(String[] args){
		String str = " Mitsuokella jalaludinii , GsQsLansYsWsHosNsAbdullah2002_GENUS_Mitsuokellasunspecified_SPECIES_jalaludiniisunspecified.xml ,cell shape, small#stout rods , small#stout rods , stout|rods#irregular|groups#short|chains#pairs ,2.0,4.0,0.0,0.0, Mitsuokella jalaludinii (jal.al.u.di«ni.i.N.L. gen. n. jalaludinii of Jalaludin, in honour of  S .Jalaludin, an animal nutritionist and Vice-Chancellor of Universiti Putra Malaysia, who has contributed significantly to rumen microbiology).Gram-negative, non-spore-forming, non-motile, <span style='background:#8DA2E0'>small</span>  <span style='background:#8DA2E0'>stout rods</span> (1.2-2.4 µm in length and 0.6-0.8 µm in width).Cells occur singly, in <span style='background:#E0CB8D'>pairs</span>  in short <span style='background:#E0CB8D'>chains</span> or in irregular <span style='background:#E0CB8D'>groups</span> Colonies on PYG agar after 2 d incubation are greyish-white, roughly circular and convex with smooth surfaces and regular edges.Strictly anaerobic.The final pH in PYG broth is 3.8-4.0.There is no copious gas formation from glucose.The major end-products from glucose fermentation are lactic acid, succinic acid and some acetic acid.Growth is stimulated by fermentable carbohydrates but not by bile.The optimum growth temperature is 42 °C.Growth occurs at 45 and 47 °C.Gelatin liquefaction, indole production, catalase production, urease production, hydrogen sulfide production, Tween 80 hydrolysis, arginine acid decarboxylase and growth in 4.5% sodium chloride are negative.The nitrate-reduction test, the methyl red test and the Voges-Proskauer reaction are positive.Glycerol, D-arabinose, ribose, L-xylose, galactose, glucose, fructose, D-mannose, inositol, sorbitol, arbutin, aesculin, salicin, cellobiose, maltose, lactose, melibiose, saccharose, trehalose, D-raffinose, amidon, D-turanose, D-arabitol and 5-keto-gluconate are fermented.Erythritol, D-arabinose, L-xylose, adonitol, methyl β-xyloside, L-sorbose, rhamnose, dulcitol, mannitol, methyl α-D-mannoside, methyl α-D-glucoside, N-acetylglucosamine, amygdalin, inulin, melezitose, glycogen, xylitol, β-gentiobiose, D-lyxose, D-tagatose, D-fucose, L-fucose, L-arabitol, gluconate and 2-ketogluconate are not fermented.Resistant to kanamycin, neomycin, penicillin, bacitracin and erythromycin.Intolerant to 0.005% crystal violet and 0.001% brilliant green.Full 16S rRNA gene sequence similarity and DNA-DNA relatedness to Mitsuokella multacida are 98.7 and 63.8%, respectively.The G­C content of the DNA is 56.8 mol%.Isolated from the rumens of cattle.The type strain is M 9 (DSM 13811, ATCC BAA-307). ";
		StringUtil.filterOffUtf8Mb4(str);
		
		System.out.println(str);
		str = str.replaceAll( "([\\ud800-\\udbff\\udc00-\\udfff])", "");
		System.out.println(str);
	}

	
}
