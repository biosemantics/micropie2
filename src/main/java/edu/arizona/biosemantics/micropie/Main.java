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
	 * -f output format i.e., csv default CSV; mc, MatrixConverter format; xml, markup XML files; csm, character and sentence matrix
	 * -vi  value infer; true or false. Only apply to CSV output
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		Main main = new Main();
		//args = "-i in -o out -m models -f xml".split(" ");
		
		//-f xml
		//args = "-i F:\\MicroPIE\\datasets\\craft -o F:\\MicroPIE\\ext\\2017new -m F:/MicroPIE/micropie0.2_model  -vi true -f mc".split("\\s+");
		
		//Part_One_111_final  GSM V1
		args = "-i F:\\MicroPIE\\datasets\\Part_One_111_final -o F:\\MicroPIE\\ext\\GSMv1 -vi false".split("\\s+");
		//args = "-i F:/MicroPIE/datasets/2017/GSM_v2_103_desccriptions_011217 -o F:/MicroPIE/ext/2017new -vi true".split("\\s+");// -f mc
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
		//options.addOption("m", "model", true, "model folders");
		options.addOption("f", "format", true, "outputformat");
		options.addOption("vi", "value infer", true, "infer value for unspecific values");
		options.addOption("pg", "propagate genus", true, "propagate values from the corresponding genus");
		options.addOption("h", "help", false, "shows the help");

		//System.out.println("parsing args");
		config = new Config();
		String xmlFolder = null;
		String outputFolder = null;
		
		int outputformat = 0;//default, plain CSV file
		//1, MatrixConverter
		//2, Markup XML files
		
		boolean isValueInference = false;//
		boolean propagateGenus = false;//
		
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
//		    if(!commandLine.hasOption("m")) {
//		    	log(LogLevel.ERROR, "You have to specify an model directory");
//		    	throw new IllegalArgumentException();
//		    } else {
//		    	config.setInputDirectory(commandLine.getOptionValue("m"));
//		    }
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
		    	}else if("csm".equals(outputformatStr)){
		    		outputformat = 3;
		    	}else if("csv".equals(outputformatStr)){
		    		outputformat = 0;
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
		    if(commandLine.hasOption("pg")) {
		    	String pgStr = commandLine.getOptionValue("pg");
		    	if("true".equals(pgStr)){
		    		propagateGenus = true;
		    	}else if("false".equals(pgStr)){
		    		propagateGenus = false;
		    	}else{
		    		throw new IllegalArgumentException(pgStr+" is invalid!");
		    	}
		    }
		    
		    
		    
		    config.setInputDirectory(Configuration.configurationFolder);
			Injector injector = Guice.createInjector(config);
		    MicroPIEProcessor microPIEProcessor = injector.getInstance(MicroPIEProcessor.class);
		    
		    String predicitonsFile = null;
		    if(outputformat != 2)
		    	predicitonsFile = outputFolder + File.separator + "predictions.csv";
			String matrixFile = outputFolder + File.separator + "matrix.csv";
			
			
		    microPIEProcessor.processFolder(xmlFolder,predicitonsFile, matrixFile, outputformat, isValueInference, propagateGenus);
			
		} catch(ParseException e) {
			e.printStackTrace();
			log(LogLevel.ERROR, "Problem parsing parameters", e);
		}
	}

}
