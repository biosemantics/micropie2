package edu.arizona.biosemantics.micropie.run;

import java.util.List;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.micropie.Config;
import edu.arizona.biosemantics.micropie.TrainSentenceClassifier;
import edu.arizona.biosemantics.micropie.classify.ILabel;

public class TrainClassifierTest {

	public static void main(String[] args) {
		Config config = new Config();
		String prjInputFolder = "F:/MicroPIE/micropieInput";
		String prjOutputFolder ="F:/MicroPIE/micropieInput/output";
		config.setInputDirectory(prjInputFolder);
		config.setOutputDirectory(prjOutputFolder);
		
		Injector injector = Guice.createInjector(config);
		//IRun run = injector.getInstance(IRun.class);	
		//IRun run = injector.getInstance(IRun.class);
		
		//Train the sentence splitter
		//TrainSentenceClassifier run = (TrainSentenceClassifier)injector.getInstance(TrainSentenceClassifier.class,  Names.named("TrainSentenceClassifier")));
		
		//log(LogLevel.INFO, "running " + run.getClass() + "...");
		//run.run();
		TrainSentenceClassifier run = (TrainSentenceClassifier)injector.getInstance(TrainSentenceClassifier.class);
		
		//String testSentFile = "F:\\MicroPIE\\micropieInput\\training_data\\150130-Training-Sentences-new-cleaned-2col-17039.csv";
		String trainFiles = "F:\\MicroPIE\\micropieInput\\training_data\\150130-Training-Sentences-new-cleaned-2coladdnew-17147.csv";
		String savedModelFolder = "F:\\MicroPIE\\micropieInput\\character_model_2017_v2\\";
		String categoryMappingFile ="F:\\MicroPIE\\micropieInput\\svmlabelandcategorymapping/categoryMapping_micropie1.5.txt";
		
		//injector.getInstance(Key.get(new TypeLiteral<GenericDbClass<Integer>>(){});
		List<ILabel> labels = injector.getInstance(Key.get(new TypeLiteral<List<ILabel>>() {},  Names.named("MultiSVMClassifier_Labels")));
		run.trainNew(trainFiles,savedModelFolder,labels, categoryMappingFile);//the first parameter is not used.
		//run.testTruePositive(testSentFile,savedModelFolder,labels);
		//run.testTrueNegative(testSentFile,savedModelFolder,labels);
	}

}
