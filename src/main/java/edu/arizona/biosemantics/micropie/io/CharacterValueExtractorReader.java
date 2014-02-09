package edu.arizona.biosemantics.micropie.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.regex.ICharacterValueExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.KeywordBasedExtractor;

public class CharacterValueExtractorReader implements
		ICharacterValueExtractorReader {

	@Override
	public ICharacterValueExtractor read(File file) throws Exception {
		String name = file.getName();
		int dotIndex = name.indexOf(".");
		String labelName = name.substring(0, dotIndex);
		String character = name.substring(dotIndex + 1, name.length());
		
		Set<String> keywords = new HashSet<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "UTF8"));
		String strLine;
		while ((strLine = br.readLine()) != null) {
			keywords.add(strLine);
		}
		br.close();
		ICharacterValueExtractor extractor = new KeywordBasedExtractor(
				Label.valueOf(labelName), character, keywords);
		return extractor;
	}
}
