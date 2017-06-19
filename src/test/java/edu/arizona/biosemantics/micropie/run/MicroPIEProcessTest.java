package edu.arizona.biosemantics.micropie.run;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.micropie.Config;
import edu.arizona.biosemantics.micropie.MicroPIEProcessor;
import edu.arizona.biosemantics.micropie.MicroPIEProcessorOld;

public class MicroPIEProcessTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Config config = new Config();
		String prjInputFolder = "F:/MicroPIE/micropieInput";
		String prjOutputFolder ="F:/MicroPIE/micropieInput/output";
		config.setInputDirectory(prjInputFolder);
		config.setOutputDirectory(prjOutputFolder);
		
		Injector injector = Guice.createInjector(config);
		
		String inputFolder = "F:\\MicroPIE\\datasets\\Part_One_111_final";
//		String inputFolder = "F:\\MicroPIE\\datasets\\craft";
//		String predictionsFile = "F:\\MicroPIE\\ext\\goldtest\\craft_prediction.csv";
//		String outputMatrixFile = "F:\\MicroPIE\\ext\\goldtest\\craft_1228.csv";
		
		
		
		//String inputFolder ="F:\\MicroPIE\\ext\\sample1";
		//String svmLabelAndCategoryMappingFile = injector.getInstance(Key.get(String.class,  Names.named("svmLabelAndCategoryMappingFile")));
		//String inputFolder = "F:\\MicroPIE\\datasets\\goldtest_ma";//_ma
		String predictionsFile = "F:\\MicroPIE\\ext\\final111\\PartOne111_prediction_NOSVM.csv";//PartOne111_prediction
		String outputMatrixFile = "F:\\MicroPIE\\ext\\final111\\PartOne111_onlyUSP_matrix.csv";
		//simplest: no numeric features, no keyword prediction
		//nouspnoconf: no USP figures, no conflict figures + rule1 keyword prediction
		//husp:handle unspecific figures + rule1
		//hconf:handle conflict figures + rule1
		//pred2:no numeric features, rule2 keyword prediction
		
		//MicroPIEProcessorOld microPIEProcessor = injector.getInstance(MicroPIEProcessorOld.class);
		
		MicroPIEProcessor microPIEProcessor = injector.getInstance(MicroPIEProcessor.class);
		//microPIEProcessor.processFolder(inputFolder, svmLabelAndCategoryMappingFile, predictionsFile, outputMatrixFile);
		microPIEProcessor.processFolder(inputFolder,predictionsFile, outputMatrixFile);
	}

}
