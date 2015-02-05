package edu.arizona.biosemantics.micropie;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.arizona.biosemantics.common.log.LogLevel;

public class Main {
	
	protected Config config;
	
	public static void main(String[] args) throws Throwable {
		Main main = new Main();
		main.parse(args);
		main.run();
	}
	
	/**
	 * @param args to parse to set config appropriately
	 */
	public void parse(String[] args) throws Throwable {
		CommandLineParser parser = new BasicParser();
		Options options = new Options();
		options.addOption("i", "input", true, "input directory to use");
		options.addOption("o", "output", true, "output directory to use");
		options.addOption("h", "help", false, "shows the help");

		config = new Config();
			
		try {
		    CommandLine commandLine = parser.parse( options, args );
		    if(commandLine.hasOption("h")) {
		    	HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "what is this?", options );
				return;
		    }
		    if(!commandLine.hasOption("i")) {
		    	log(LogLevel.ERROR, "You have to specify an input directory");
		    	throw new IllegalArgumentException();
		    } else {
		    	config.setInputDirectory(commandLine.getOptionValue("i"));
		    }
		    if(!commandLine.hasOption("o")) {
		    	log(LogLevel.ERROR, "You have to specify an output directory");
		    	throw new IllegalArgumentException();
		    } else {
		    	config.setOutputDirectory(commandLine.getOptionValue("o"));
		    }
		} catch(ParseException e) {
			log(LogLevel.ERROR, "Problem parsing parameters", e);
		}
	}

	
	private void run() throws Exception {
		Injector injector = Guice.createInjector(config);
		IRun run = injector.getInstance(IRun.class);	
		
		log(LogLevel.INFO, "running " + run.getClass() + "...");
		run.run();		
	}	

}
