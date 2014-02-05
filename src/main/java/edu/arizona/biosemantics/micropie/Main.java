package edu.arizona.biosemantics.micropie;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.arizona.biosemantics.micropie.log.LogLevel;

public class Main {

	public static void main(String[] args) throws Exception {
		Main main = new Main();
		main.run();
	}
	
	private void run() throws Exception {
		Injector injector = Guice.createInjector(new Config());
		IRun run = injector.getInstance(IRun.class);
		
		log(LogLevel.INFO, "running " + run.getClass() + "...");
		run.run();
	}	

}
