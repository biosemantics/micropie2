package edu.arizona.biosemantics.micropie.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import de.mpii.clausie.ClausIE;
import de.mpii.clausie.Clause;
import de.mpii.clausie.Proposition;
import edu.arizona.biosemantics.micropie.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.ParseResult;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;

public class MyTextSentenceTransformer implements ITextSentenceTransformer {

	private ClausIE clausIE;
	private StanfordCoreNLP pipeline;
	private Map<Sentence, ParseResult> cachedParseResults = new HashMap<Sentence, ParseResult>();
	private PennTreebankLanguagePack pennTreebankLanguagePack;
	
	public MyTextSentenceTransformer() {
		Properties stanfordCoreProperties = new Properties();
		stanfordCoreProperties.put("annotators", "tokenize, ssplit");
		this.pipeline = new StanfordCoreNLP(stanfordCoreProperties);
		
		this.clausIE = new ClausIE();
		this.clausIE.initParser();
		this.clausIE.getOptions().print(System.out, "# ");
		
		this.pennTreebankLanguagePack = new PennTreebankLanguagePack();
	}
	
	@Override
	public List<Sentence> transform(String text) {
		List<Sentence> result = new LinkedList<Sentence>();
		List<String> sentences = getSentences(text);
		for(String sentence : sentences) {
			if(sentence.length() > 3) {
				result.addAll(this.compoundSplit(sentence));
			}
		}
		return result;
	}
	
	public ParseResult getCachedParseResult(Sentence sentence) {
		return cachedParseResults.get(sentence);
	}
	
	private List<String> getSentences(String text) {
		List<String> result = new LinkedList<String>();
		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		List<CoreMap> sentenceAnnotations = document.get(SentencesAnnotation.class);
		for (CoreMap sentenceAnnotation : sentenceAnnotations) {
			result.add(sentenceAnnotation.toString());
		}
		return result;
	}

	private List<Sentence> compoundSplit(String text) {
		List<Sentence> result = new LinkedList<Sentence>();
		
		clausIE.parse(text);
		Tree dependencyTree = clausIE.getDepTree();
		log(LogLevel.INFO, "Dependency parse : ");
		log(LogLevel.INFO, dependencyTree.pennString()
				.replaceAll("\n", "\n                   ").trim());
		
		
		List<String> sentenceList = new ArrayList<String>();
		handleCaseA(text, sentenceList);
		handleCaseB(text, sentenceList);	
		
		if (sentenceList.size() > 1) {
			for (String sentenceText : sentenceList) {
				Sentence sentence = new Sentence(sentenceText);
				clausIE.parse(sentenceText);
				dependencyTree = clausIE.getDepTree();
				cachedParseResults.put(sentence, getParseResult(dependencyTree));
				result.add(sentence);
			}
		} else {
			Sentence sentence = new Sentence(text);		
			cachedParseResults.put(sentence, getParseResult(dependencyTree));
			result.add(sentence);
		}
		return result;
	}
	
	private ParseResult getParseResult(Tree dependencyTree) {
		GrammaticalStructureFactory grammaticalStructureFactory = pennTreebankLanguagePack.grammaticalStructureFactory();
		GrammaticalStructure grammaticalStructure = grammaticalStructureFactory.newGrammaticalStructure(dependencyTree);
		Collection<TypedDependency> typedDependencies = grammaticalStructure.typedDependenciesCollapsed();
		ParseResult parseResult = new ParseResult(clausIE.getDepTree(), typedDependencies);
		return parseResult;
	}

	private void handleCaseB(String text, List<String> subSentenceList) {
		String depTreeString = clausIE.getDepTree().pennString();
		log(LogLevel.INFO, "depTreeString :: " + depTreeString);

		boolean containWhichOrThat = false;
		String whichOrTahtClauseKeywordOne = "";
		String whichOrTahtClauseKeywordTwo = "";

		if (depTreeString.contains("(WHNP (WDT which))")
				|| depTreeString.contains("(WHNP (WDT that))")) {
			log(LogLevel.INFO, "Yes, it contains \"that\" or \"which\"");
			containWhichOrThat = true;
			
			if ( text.contains("that") ) {
				String[] inputSentArray = text.split("that");
				String[] inputSentArrayOneArray = inputSentArray[1].split(" ");
				
				if (inputSentArrayOneArray.length > 3) {
					whichOrTahtClauseKeywordOne = inputSentArrayOneArray[1];
					whichOrTahtClauseKeywordTwo = inputSentArrayOneArray[2] + " " + inputSentArrayOneArray[3];
					log(LogLevel.INFO, whichOrTahtClauseKeywordOne + " and " + whichOrTahtClauseKeywordTwo); 
				} else {
					whichOrTahtClauseKeywordOne = inputSentArrayOneArray[1];
					whichOrTahtClauseKeywordTwo = inputSentArrayOneArray[2];
					log(LogLevel.INFO, whichOrTahtClauseKeywordOne + " and " + whichOrTahtClauseKeywordTwo); 
				}
			} else if ( text.contains("which") ) {
				String[] inputSentArray = text.split("which");
				String[] inputSentArrayOneArray = inputSentArray[1].split(" ");
				if (inputSentArrayOneArray.length > 3) {
					whichOrTahtClauseKeywordOne = inputSentArrayOneArray[1];
					whichOrTahtClauseKeywordTwo = inputSentArrayOneArray[2] + " " + inputSentArrayOneArray[3];
					log(LogLevel.INFO, whichOrTahtClauseKeywordOne + " and " + whichOrTahtClauseKeywordTwo); 
				} else {
					whichOrTahtClauseKeywordOne = inputSentArrayOneArray[1];
					whichOrTahtClauseKeywordTwo = inputSentArrayOneArray[2];
					log(LogLevel.INFO, whichOrTahtClauseKeywordOne + " and " + whichOrTahtClauseKeywordTwo); 
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
			log(LogLevel.INFO, "Input sentence 2   : " + text);
			log(LogLevel.INFO, "additionalSent :: " + additionalSent);
			
			subSentenceList.add(additionalSent);
		}
	}

	private void handleCaseA(String text, List<String> subSentenceList) {
		// This section is for detecting the sentence structure "A is B that/whcih is C ..."
		// And then transfer it into =>
		// A is C ...
		// =>
		// detect
		// (WHNP (WDT which))
		// (WHNP (WDT that))

		
		log(LogLevel.INFO, "Semantic graph   : ");
		log(LogLevel.INFO, clausIE.getSemanticGraph()
				.toFormattedString()
				.replaceAll("\n", "\n                   ").trim());

		// clause detection
		log(LogLevel.INFO, "ClausIE time     : ");
		long start = System.currentTimeMillis();

		clausIE.detectClauses();
		clausIE.generatePropositions();
		long end = System.currentTimeMillis();
		log(LogLevel.INFO, (end - start) / 1000. + "s");

		log(LogLevel.INFO, "Clauses          : ");
		String sep = "";
		for (Clause clause : clausIE.getClauses()) {
			log(LogLevel.INFO, sep
					+ clause.toString(clausIE.getOptions()));
			sep = "                   ";
		}

		// generate propositions
		log(LogLevel.INFO, "clausIE.getPropositions().size():"
				+ clausIE.getPropositions().size() + "\n");

		log(LogLevel.INFO, "Propositions     : ");

		sep = "";

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
			log(LogLevel.INFO, sep + propString);
			sep = "                   ";

			subSentenceList.add(propString);
		}
	}

}
