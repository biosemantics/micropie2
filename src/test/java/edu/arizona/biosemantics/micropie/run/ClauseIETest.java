package edu.arizona.biosemantics.micropie.run;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.mpii.clausie.ClausIE;
import de.mpii.clausie.Clause;
import de.mpii.clausie.Proposition;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.LexicalizedParserQuery;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;


/**
 * 
 * @author maojin
 *
 */
public class ClauseIETest {
	
	public static void main(String[] args){
		
		
		LexicalizedParser lexicalizedParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");;
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(
				new CoreLabelTokenFactory(), "");
		
		
		LexicalizedParserQuery parserQuery = (LexicalizedParserQuery)lexicalizedParser.parserQuery();
		ClausIE clausIE = new ClausIE(lexicalizedParser, tokenizerFactory, parserQuery);
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
		    	buffer = "";
		    }
		}, "#ClausIE# ");
	
		
		
		String sentence = "growth is inhibited in the absence of NaCl and in the presence of >8 % (w/v) NaCl.";
		clausIE.parse(sentence);
		
		ClauseIETest cit = new ClauseIETest();
		List<String> sentenceList = new ArrayList<String>();
		try {
			cit.handleCaseA(sentence, sentenceList, clausIE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(sentenceList);
		
		
		if (sentenceList.size() > 1) {
			for (String sentenceText : sentenceList) {
				
				System.out.println("splittedSentenceText::" + sentenceText);
				
			}
		} else {
			//cachedParseResults.put(sentence, getParseResult(dependencyTree, clausIE));
		}
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
