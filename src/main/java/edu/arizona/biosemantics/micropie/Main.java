package edu.arizona.biosemantics.micropie;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.arizona.biosemantics.micropie.log.LogLevel;

public class Main {
	
	private static String testFolderNameFromCommandLine = "";
	
	public static void main(String[] args) throws Exception {
		Main main = new Main();
		
		// for (String s: args) {
		//	System.out.println("111:" + s);
		// }
		if (args[0].equals("-s")) {
			if (!args[1].equals("")) {
				testFolderNameFromCommandLine = args[1];
			}
		}
		
		main.run(testFolderNameFromCommandLine);
	}
	

	
	private void run(String testString) throws Exception {
		Injector injector = Guice.createInjector(new Config());
		IRun run = injector.getInstance(IRun.class);	
		
		log(LogLevel.INFO, "running " + run.getClass() + "...");
		run.run(testFolderNameFromCommandLine);
		
		
		
		
	}	


}
