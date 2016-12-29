package edu.arizona.biosemantics.micropie;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.MultiSVMClassifier;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.arizona.biosemantics.micropie.nlptool.CompoundSentenceSplitRun;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;


/**
 * -i input: xml folder
 * -o results: output folder
 * -m model: folder
 * @author 
 *
 */
public class Main {
	
	protected Config config;
	
	
	/**
	 * -i input folder
	 * -o output folder for CSV file or markup XML files
	 * -m various models used by MicrioPIE
	 * -f output format i.e., 0 default CSV; mc, MatrixConverter format; xml, markup XML files
	 * -vi  value infer; true or false. Only apply to CSV output
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		Main main = new Main();
		//args = "-i in -o out -m models -f xml".split(" ");
		
		//args = "-i F:\\MicroPIE\\micropieweb\\danveno_at_qq_dot_com_2016_11_03_12_15_24_059\\input -o F:/MicroPIE/ext/craft -m F:/MicroPIE/MicroPIEWEB/models -f xml -vi true".split("\\s+");
		//System.out.println(args);
		main.parse(args);
		//main.run();
	}
	
	/**
	 * @param args to parse to set config appropriately
	 */
	public void parse(String[] args) throws Throwable {
		CommandLineParser parser = new BasicParser();
		Options options = new Options();
		options.addOption("i", "input", true, "input directory to use");
		options.addOption("o", "output", true, "output directory to use");
		options.addOption("m", "model", true, "model folders");
		options.addOption("f", "format", true, "outputformat");
		options.addOption("vi", "value infer", true, "infer value for unspecific values");
		options.addOption("h", "help", false, "shows the help");

		//System.out.println("parsing args");
		config = new Config();
		String xmlFolder = null;
		String outputFolder = null;
		
		int outputformat = 0;//default, plain CSV file
		//1, MatrixConverter
		//2, Markup XML files
		
		boolean isValueInference = false;//
		
		
		try {
		    CommandLine commandLine = parser.parse( options, args );
		    if(commandLine.hasOption("h")) {
		    	HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "what is this?", options );
				return;
		    }
		    if(!commandLine.hasOption("i")) {
		    	//log(LogLevel.ERROR, "You have to specify an input directory");
		    	throw new IllegalArgumentException();
		    } else {
		    	xmlFolder = commandLine.getOptionValue("i");
		    }
		    if(!commandLine.hasOption("m")) {
		    	log(LogLevel.ERROR, "You have to specify an model directory");
		    	throw new IllegalArgumentException();
		    } else {
		    	config.setInputDirectory(commandLine.getOptionValue("m"));
		    }
		    if(!commandLine.hasOption("o")) {
		    	//log(LogLevel.ERROR, "You have to specify an output directory");
		    	throw new IllegalArgumentException();
		    } else {
		    	outputFolder = commandLine.getOptionValue("o");
		    	config.setOutputDirectory(commandLine.getOptionValue("o"));
		    }
		    if(commandLine.hasOption("f")) {
		    	String outputformatStr = commandLine.getOptionValue("f");
		    	if("mc".equals(outputformatStr)){
		    		outputformat = 1;
		    	}else if("xml".equals(outputformatStr)){
		    		outputformat = 2;
		    	}else{
		    		throw new IllegalArgumentException(outputformatStr+" is invalid!");
		    	}
		    }
		    if(commandLine.hasOption("vi")) {
		    	String viStr = commandLine.getOptionValue("vi");
		    	if("true".equals(viStr)){
		    		isValueInference = true;
		    	}else if("false".equals(viStr)){
		    		isValueInference = false;
		    	}else{
		    		throw new IllegalArgumentException(viStr+" is invalid!");
		    	}
		    }
			Injector injector = Guice.createInjector(config);
		    MicroPIEProcessor microPIEProcessor = injector.getInstance(MicroPIEProcessor.class);
		    String predicitonsFile = null;
		    if(outputformat != 2)
		    	predicitonsFile = outputFolder + File.separator + "predictions.csv";
			String matrixFile = outputFolder + File.separator + "matrix.csv";
			
			
		    microPIEProcessor.processFolder(xmlFolder,predicitonsFile, matrixFile, outputformat, isValueInference);
			
		} catch(ParseException e) {
			e.printStackTrace();
			log(LogLevel.ERROR, "Problem parsing parameters", e);
		}
	}

	/*
	private void run() throws Exception {
		config = new Config();
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
		
		String testSentFile = "F:\\MicroPIE\\micropieInput\\training_data\\150130-Training-Sentences-new-cleaned-2col.csv";
		String savedModelFolder = "F:\\MicroPIE\\micropieInput\\models\\";
		
		//injector.getInstance(Key.get(new TypeLiteral<GenericDbClass<Integer>>(){});
		//List<ILabel> labels = injector.getInstance(Key.get(new TypeLiteral<List<ILabel>>() {},  Names.named("MultiSVMClassifier_Labels")));
		//run.train(testSentFile,savedModelFolder,labels);
		//run.testTruePositive(testSentFile,savedModelFolder,labels);
		//run.testTrueNegative(testSentFile,savedModelFolder,labels);
		
		
		
		
		
		//SentenceSpliter sspliter = injector.getInstance(SentenceSpliter.class);
		/*
		long b = System.currentTimeMillis();
		SentencePredictor sentPred1 = injector.getInstance(SentencePredictor.class);
		SentencePredictor sentPred2 = injector.getInstance(SentencePredictor.class);
		SentencePredictor sentPred3 = injector.getInstance(SentencePredictor.class);
		//Habitat is not known.
		//Colonies are 0.2 to 0.3 mm in diameter on blood-enriched Columbia agar and Brain Heart Infusion (BHI) agar.
		
		/*sentPred1.predict("Habitat is not known.");
		sentPred2.predict("Colonies are 0.2 to 0.3 mm in diameter on blood-enriched Columbia agar and Brain Heart Infusion (BHI) agar.");
		sentPred3.predict("Cells are rod-shaped with a mean diameter of 0.56 µm.");
		long e = System.currentTimeMillis();
		System.out.println(e-b);
		List<String> sents = sspliter.split("Colonies are 0.2 to 0.3 mm in diameter on blood-enriched Columbia agar and Brain Heart Infusion (BHI) agar. Cells are rod-shaped with a mean diameter of 0.56 µm; optimal growth is achieved anaerobically. Weak growth is observed in microaerophilic conditions. No growth is observed in aerobic conditions. Growth occurred between 30-37°C, with optimal growth observed at 37°C, in BHI medium + 5% NaCl. Cells stain Gram negative and are non-motile. Catalase, α-galactosidase, β-galactosidase, β-glucuronidase, arginine arlyamidase, glycine arylamidase, proline arylimidase, leucyl glycine arylamidase, and alanine arylamidase activities are present. Mannose fermentation and indole production are also present. Oxidase activity is absent. Cells are susceptible to penicillin G, amoxicillin + clavulanic acid, imipeneme and clindamycin but resistant to metronidazole. The G+C content of the genome is 58.40%.");
		SentencePredictor predictor = injector.getInstance(SentencePredictor.class);
		for(String sent : sents){
			predictor.predict(sent);
		}
		String sentence = "Cells are rod-shaped with a mean diameter of 0.56 µm; optimal growth is achieved anaerobically.";
		//String sentence = "Questions asking us to recommend or find a tool, library or favorite off-site resource are off-topic for Stack Overflow as they tend to attract opinionated answers and spam.";
		LexicalizedParser lexicalizedParser = injector.getInstance(LexicalizedParser.class);
		CompoundSentenceSplitRun splitRun = new CompoundSentenceSplitRun(
				sentence, lexicalizedParser, PTBTokenizer.factory(
						new CoreLabelTokenFactory(), ""));
		splitRun.call();
		
		String inputFolder = "F:\\MicroPIE\\micropieInput\\input";
		String svmLabelAndCategoryMappingFile = injector.getInstance(Key.get(String.class,  Names.named("svmLabelAndCategoryMappingFile")));
		String predictionsFile = "F:/MicroPIE/micropieInput/sentences/1.1prediction.csv";
		String outputMatrixFile = "F:\\MicroPIE\\micropieInput\\output\\matrix.csv";
		/*MicroPIEProcessor microPIEProcessor = injector.getInstance(MicroPIEProcessor.class);
		long b = System.currentTimeMillis();
		microPIEProcessor.processFolder(inputFolder, svmLabelAndCategoryMappingFile, predictionsFile, outputMatrixFile);
		long e2 = System.currentTimeMillis();
		System.out.println("get the splitter costs:"+(e2-b)+" ms");
		
		SentenceBatchProcessor sentBatPIEProcessor = injector.getInstance(SentenceBatchProcessor.class);
		String lineFile = "F:/MicroPIE/micropieInput/sentences/1.1 G+C.csv";
		sentBatPIEProcessor.processLineFile(lineFile, svmLabelAndCategoryMappingFile, predictionsFile, outputMatrixFile);
		}*/
}
