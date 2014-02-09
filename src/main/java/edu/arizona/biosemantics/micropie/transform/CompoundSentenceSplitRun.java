package edu.arizona.biosemantics.micropie.transform;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import de.mpii.clausie.ClausIE;
import de.mpii.clausie.Clause;
import de.mpii.clausie.Proposition;
import edu.arizona.biosemantics.micropie.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.LexicalizedParserQuery;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.Tree;

public class CompoundSentenceSplitRun implements Callable<List<String>> {

	private String sentence;
	//private CountDownLatch compoundSentenceSplitLatch;
	private ClausIE clausIE;

	public CompoundSentenceSplitRun(String sentence, LexicalizedParser lexicalizedParser, 
			TokenizerFactory<CoreLabel> tokenizerFactory) {
		this.sentence = sentence;
		//this.compoundSentenceSplitLatch = compoundSentenceSplitLatch;
		
		//according to stanford corenlp documentation 
		// - lexicalizedParser is thread safe
		// - PTBTokenizerFactory is not
		// - lexicalizedparserquery is meant to be instantiated per each thread (not thread safe), see
		//   http://nlp.stanford.edu/downloads/parser-faq.shtml#n
		LexicalizedParserQuery parserQuery = (LexicalizedParserQuery)lexicalizedParser.parserQuery();
		this.clausIE = new ClausIE(lexicalizedParser, tokenizerFactory, parserQuery);
		clausIE.getOptions().print(new OutputStream() {
		    private String buffer;
			@Override
			public void write(int b) throws IOException {
				byte[] bytes = new byte[1];
		        bytes[0] = (byte) (b & 0xff);
		        buffer = buffer + new String(bytes);

		        if (buffer.endsWith ("\n")) {
		        	buffer = buffer.substring (0, buffer.length () - 1);
		            flush();
		        }
			}
			@Override
		    public void flush () {
		    	log(LogLevel.INFO, buffer);
		    	buffer = "";
		    }
		}, "#ClausIE# ");
	}

	@Override
	public List<String> call() throws Exception {
		List<String> result = new LinkedList<String>();
		//try {
			log(LogLevel.INFO, "split compound sentences into subsentences using clausIE... sentence: " + sentence);
			System.out.println("compound split " + sentence);
			log(LogLevel.INFO, "clausIE parse...");
					
			clausIE.parse(sentence);
			log(LogLevel.INFO, "clausIE parse complete");
			Tree dependencyTree = clausIE.getDepTree();
			log(LogLevel.INFO, "Dependency parse : ");
			log(LogLevel.INFO, dependencyTree.pennString());
			log(LogLevel.INFO, "Semantic graph   : ");
			log(LogLevel.INFO, clausIE.getSemanticGraph().toFormattedString());
			//.replaceAll("\n", "\n                   ").trim());
			//.replaceAll("\n", "\n                   ").trim());
			
			List<String> sentenceList = new ArrayList<String>();
			handleCaseA(sentence, sentenceList, clausIE);
			handleCaseB(sentence, sentenceList, clausIE);	
			
			if (sentenceList.size() > 1) {
				log(LogLevel.INFO, "found subsentences: " + sentenceList.size());
				for (String sentenceText : sentenceList) {
					log(LogLevel.INFO, "clausIE parse...");
					//clausIE.parse(sentenceText);
					log(LogLevel.INFO, "clausIE parse complete");
					//dependencyTree = clausIE.getDepTree();
					//cachedParseResults.put(sentence, getParseResult(dependencyTree, clausIE));
					result.add(sentenceText);
				}
			} else {
				log(LogLevel.INFO, "did not find subsentences");
				//cachedParseResults.put(sentence, getParseResult(dependencyTree, clausIE));
				result.add(sentence);
			}
		/*} catch(Exception e) {
			log(LogLevel.ERROR, "Problem running compoundSentenceSplitRun", e);
		}*/
		//compoundSentenceSplitLatch.countDown();
		//System.out.println(compoundSentenceSplitLatch.getCount());
		System.out.println("done compound split");
		return result;
	}
	
	private void handleCaseB(String text, List<String> subSentenceList, ClausIE clausIE)  throws Exception {
		log(LogLevel.INFO, "handle case B...");
		String depTreeString = clausIE.getDepTree().pennString();

		boolean containWhichOrThat = false;
		String whichOrTahtClauseKeywordOne = "";
		String whichOrTahtClauseKeywordTwo = "";

		if (depTreeString.contains("(WHNP (WDT which))")
				|| depTreeString.contains("(WHNP (WDT that))")) {
			//log(LogLevel.INFO, "Yes, it contains \"that\" or \"which\"");
			containWhichOrThat = true;
			
			if ( text.contains("that") ) {
				String[] inputSentArray = text.split("that");
				String[] inputSentArrayOneArray = inputSentArray[1].split(" ");
				
				if (inputSentArrayOneArray.length > 3) {
					whichOrTahtClauseKeywordOne = inputSentArrayOneArray[1];
					whichOrTahtClauseKeywordTwo = inputSentArrayOneArray[2] + " " + inputSentArrayOneArray[3];
					//log(LogLevel.INFO, whichOrTahtClauseKeywordOne + " and " + whichOrTahtClauseKeywordTwo); 
				} else {
					whichOrTahtClauseKeywordOne = inputSentArrayOneArray[1];
					whichOrTahtClauseKeywordTwo = inputSentArrayOneArray[2];
					//log(LogLevel.INFO, whichOrTahtClauseKeywordOne + " and " + whichOrTahtClauseKeywordTwo); 
				}
			} else if ( text.contains("which") ) {
				String[] inputSentArray = text.split("which");
				String[] inputSentArrayOneArray = inputSentArray[1].split(" ");
				if (inputSentArrayOneArray.length > 3) {
					whichOrTahtClauseKeywordOne = inputSentArrayOneArray[1];
					whichOrTahtClauseKeywordTwo = inputSentArrayOneArray[2] + " " + inputSentArrayOneArray[3];
					//log(LogLevel.INFO, whichOrTahtClauseKeywordOne + " and " + whichOrTahtClauseKeywordTwo); 
				} else {
					whichOrTahtClauseKeywordOne = inputSentArrayOneArray[1];
					whichOrTahtClauseKeywordTwo = inputSentArrayOneArray[2];
					//log(LogLevel.INFO, whichOrTahtClauseKeywordOne + " and " + whichOrTahtClauseKeywordTwo); 
				}
			}
		}
		
		// This section is for detecting the sentence structure "A is B that/whcih is C ..."
		// And then transfer it into =>
		// A is C ...
		
		String additionalSent = "";
		if (containWhichOrThat == true) {
			String clausIEGetPropositionsZero = clausIE.getPropositions().get(0).toString();
			clausIEGetPropositionsZero= clausIEGetPropositionsZero.substring(1,
					clausIEGetPropositionsZero.length() - 1);
			
			// String clausIEGetPropositionsOne = clausIE.getPropositions().get(1).toString();
			// clausIEGetPropositionsOne = clausIEGetPropositionsOne.substring(1,
			//		clausIEGetPropositionsOne.length() - 1);
			
			String clausIEGetPropositionsOne = "";
			for (Proposition prop : clausIE.getPropositions()) {
				if ( prop.toString().contains(whichOrTahtClauseKeywordOne) && prop.toString().contains(whichOrTahtClauseKeywordTwo) ) {
					//log(LogLevel.INFO, (prop.toString());
					//log(LogLevel.INFO, (whichOrTahtClauseKeywordOne + " and " + whichOrTahtClauseKeywordTwo);
					clausIEGetPropositionsOne = prop.toString().substring(1,prop.toString().length() - 1);
				}
			}

			
			
			String[] clausIEGetPropositionsZeroArray = clausIEGetPropositionsZero.split(",");
			String[] clausIEGetPropositionsOneArray = clausIEGetPropositionsOne.split(",");
			
			// propString = propString.replaceAll("\"", "");
			additionalSent += clausIEGetPropositionsZeroArray[0].replaceAll("\"", "");
			
			for(int i = 1; i < clausIEGetPropositionsOneArray.length; i++) {
				additionalSent += clausIEGetPropositionsOneArray[i].replaceAll("\"", "");
			}	
			additionalSent += ".";
			//log(LogLevel.INFO, "Input sentence 2   : " + text);
			//log(LogLevel.INFO, "additionalSent :: " + additionalSent);
			
			log(LogLevel.INFO, "additional sentence found and added: " + additionalSent);
			subSentenceList.add(additionalSent);
		}
		log(LogLevel.INFO, "Done handling case B");
	}

	private void handleCaseA(String text, List<String> subSentenceList, ClausIE clausIE)  throws Exception {
		log(LogLevel.INFO, "handle case A...");
		// This section is for detecting the sentence structure "A is B that/whcih is C ..."
		// And then transfer it into =>
		// A is C ...
		// =>
		// detect
		// (WHNP (WDT which))
		// (WHNP (WDT that))

		log(LogLevel.INFO, "detect clauses ...");
		clausIE.detectClauses();
		log(LogLevel.INFO, "done detecting clauses: ");
		for (Clause clause : clausIE.getClauses()) {
			log(LogLevel.INFO, clause.toString(clausIE.getOptions()));
		}
		
		log(LogLevel.INFO, "generate propositions ...");
		clausIE.generatePropositions();
		log(LogLevel.INFO, "done generating propositions: "+ clausIE.getPropositions().size());

		for (Proposition prop : clausIE.getPropositions()) {
			String propString = prop.toString();
			propString = propString.replaceAll("\"", "");
			propString = propString.replaceAll(",", "");
			propString = propString.substring(1,
					propString.length() - 1); // Remove "(" and ")" in (Sentence Contect)
			// propString = propString.toLowerCase();
			propString = propString.substring(0, 1).toUpperCase()
					+ propString.substring(1);// Capitalize the first
												// letter

			propString += ".";

			subSentenceList.add(propString);
			log(LogLevel.INFO, "Additional sentence (propositions-string) added: " + propString);
			
		}
		
		log(LogLevel.INFO, "Done handling case A");
	}

}
