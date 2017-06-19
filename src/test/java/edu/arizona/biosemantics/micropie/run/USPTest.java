package edu.arizona.biosemantics.micropie.run;

import java.util.Set;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.micropie.Config;
import edu.arizona.biosemantics.micropie.MicroPIEProcessor;
import edu.arizona.biosemantics.micropie.MicroPIEProcessorOld;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.usp.USPBasedExtractor;
import edu.arizona.biosemantics.micropie.extract.usp.USPRequest;
import edu.arizona.biosemantics.micropie.model.Sentence;

public class USPTest {

	public static void main(String[] args) {
		/*
		// TODO Auto-generated method stub
		Config config = new Config();
		String prjInputFolder = "F:/MicroPIE/micropieInput";
		String prjOutputFolder ="F:/MicroPIE/micropieInput/output";
		config.setInputDirectory(prjInputFolder);
		config.setOutputDirectory(prjOutputFolder);
		
		Injector injector = Guice.createInjector(config);
		
		//String inputFolder = "F:\\MicroPIE\\micropieInput\\input";
		String inputFolder = "F:\\MicroPIE\\datasets\\goldtest";
		//String inputFolder = "F:\\MicroPIE\\datasets\\Part One 112";
		//String inputFolder ="F:\\MicroPIE\\ext\\sample1";
		//String svmLabelAndCategoryMappingFile = injector.getInstance(Key.get(String.class,  Names.named("svmLabelAndCategoryMappingFile")));
		String predictionsFile = "F:\\MicroPIE\\ext\\goldtest\\goldtest_22_prediction.csv";
		String outputMatrixFile = "F:\\MicroPIE\\ext\\goldtest\\goldtest_22_1124.csv";
		
		//MicroPIEProcessorOld microPIEProcessor = injector.getInstance(MicroPIEProcessorOld.class);
		
		USPBasedExtractor uspProcessor = new USPBasedExtractor(Label.c32, 
				"Antibiotic sensitivity",
				@Named("USPBasedExtractor_")Set<USPRequest> uspRequests,
				injector.getInstance(Names.named("uspResultsDirectory")).toString(), 
				@Named("uspString") String uspString) {
		//microPIEProcessor.processFolder(inputFolder, svmLabelAndCategoryMappingFile, predictionsFile, outputMatrixFile);
		Sentence sentence = new Sentence();
		sentence.setText("Susceptible to ampicillin, chloramphenicol, gentamicin, kanamycin, nalidixic acid, neomycin, penicillin, streptomycin and tetracycline.");
		uspProcessor.getCharacterValue(sentence);
		*/
	}

}
