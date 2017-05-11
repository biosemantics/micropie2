package edu.arizona.biosemantics.micropie.run;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.micropie.Config;
import edu.arizona.biosemantics.micropie.SentenceBatchProcessor;

public class SentenceBatchProcessorTest {
	
	public static void main(String[] args){
		Config config = new Config();
		String prjInputFolder = "F:/MicroPIE/micropieInput";
		String prjOutputFolder ="F:/MicroPIE/micropieInput/output";
		config.setInputDirectory(prjInputFolder);
		config.setOutputDirectory(prjOutputFolder);
		
		Injector injector = Guice.createInjector(config);
		String svmLabelAndCategoryMappingFile = injector.getInstance(Key.get(String.class,  Names.named("svmLabelAndCategoryMappingFile")));
		String predictionsFile = "F:/MicroPIE/micropieInput/sentences/2.12 prediction.csv";
		String outputMatrixFile = "F:\\MicroPIE\\micropieInput\\sentences\\2.12 matrix.csv";
		
		SentenceBatchProcessor sentBatPIEProcessor = injector.getInstance(SentenceBatchProcessor.class);
		String lineFile = "F:/MicroPIE/micropieInput/sentences/2.12 biofilms.txt";
		sentBatPIEProcessor.processLineFile(lineFile, predictionsFile, outputMatrixFile);
	}
}
