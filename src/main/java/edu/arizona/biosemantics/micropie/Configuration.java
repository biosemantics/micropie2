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
	public static String characterListString;
	public static String ouputCharacterListString;
	public static String svmLabelAndCategoryMappingFile;
	public static String firstLevelCategoryMappingFile;
	public static String labelValutypeFile;
	public static String trainedModelFile;
	public static String categoryModelFile;
	public static String characterValueExtractorsFolder;
	public static String configurationFolder;
	
	public static String wordNetDirectory;
	public static String brownClusterFile;
	public static String geoTaggerModel;
	public static String wordEmbeddingClusterFile;

	public static String geoUniqTermFile;
	
	
	static {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties properties = new Properties();
		try {
			
			properties.load(loader.getResourceAsStream("edu/arizona/biosemantics/micropie/config.properties"));
			
			characterListString = properties.getProperty("characterListString");
			ouputCharacterListString = properties.getProperty("ouputCharacterListString");
			svmLabelAndCategoryMappingFile = properties.getProperty("svmLabelAndCategoryMappingFile");
			firstLevelCategoryMappingFile = properties.getProperty("firstLevelCategoryMappingFile");
			labelValutypeFile = properties.getProperty("labelValutypeFile");
			trainedModelFile = properties.getProperty("trainedModelFile");
			categoryModelFile = properties.getProperty("categoryModelFile");
			characterValueExtractorsFolder = properties.getProperty("characterValueExtractorsFolder");
			configurationFolder = properties.getProperty("configurationFolder");
			
			wordNetDirectory = properties.getProperty("wordNetDirectory");
			brownClusterFile = properties.getProperty("brownClusterFile");
			wordEmbeddingClusterFile =  properties.getProperty("wordEmbeddingClusterFile");
			geoUniqTermFile = properties.getProperty("geoUniqTermFile");
			geoTaggerModel = properties.getProperty("geoTaggerModel");
		} catch (IOException e) {
			logger.error("Couldn't read configuration", e);
		}
	}
}
