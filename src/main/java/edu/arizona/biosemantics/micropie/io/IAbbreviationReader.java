package edu.arizona.biosemantics.micropie.io;

import java.io.IOException;
import java.util.LinkedHashMap;

public interface IAbbreviationReader {

	public LinkedHashMap<String, String> read() throws IOException;

}
