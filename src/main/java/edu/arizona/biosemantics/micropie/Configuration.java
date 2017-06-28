package edu.arizona.biosemantics.micropie;

import java.io.IOException;
import java.util.Properties;

import edu.arizona.biosemantics.common.log.Logger;


/**
 * read configuration file
 * @author maojin
 *
 */
public class Configuration {

	private final static Logger logger = Logger.getLogger(Configuration.class);
	
	public static String wordNetDirectory;
	public static String brownClusterFile;
	public static String geoTaggerModel;
	public static String wordEmbeddingClusterFile;
	
	static {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties properties = new Properties();
		try {
			
			properties.load(loader.getResourceAsStream("edu/arizona/biosemantics/micropie/config.properties"));
			wordNetDirectory = properties.getProperty("wordNetDirectory");
			brownClusterFile = properties.getProperty("brownClusterFile");
			wordEmbeddingClusterFile =  properties.getProperty("wordEmbeddingClusterFile");
			geoTaggerModel = properties.getProperty("geoTaggerModel");
		} catch (IOException e) {
			logger.error("Couldn't read configuration", e);
		}
	}
}
