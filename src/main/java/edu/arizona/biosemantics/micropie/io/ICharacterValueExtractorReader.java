package edu.arizona.biosemantics.micropie.io;

import java.io.File;

import edu.arizona.biosemantics.micropie.extract.regex.ICharacterValueExtractor;

public interface ICharacterValueExtractorReader {

	ICharacterValueExtractor read(File file) throws Exception;
	
}
