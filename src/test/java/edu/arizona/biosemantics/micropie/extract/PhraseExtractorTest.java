package edu.arizona.biosemantics.micropie.extract;

import java.util.List;

import edu.arizona.biosemantics.micropie.extract.context.PhraseRelationGraph;
import edu.arizona.biosemantics.micropie.extract.context.RelationParser;
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.nlptool.PhraseParser;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.StanfordParserWrapper;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

public class PhraseExtractorTest {
	public static void main(String[] args){
		PhraseParser extractor = new PhraseParser();
		/*
		Phrase p = new Phrase();
		p.setText("non-motile");
		extractor.detectIntermNegation(p);
		System.out.println(p.getText()+":"+p.getNegation());
		p.setText("nonmotile");
		extractor.detectIntermNegation(p);
		System.out.println(p.getText()+":"+p.getNegation());
		p.setText("a little nonmotile");
		extractor.detectIntermNegation(p);
		System.out.println(p.getText()+":"+p.getNegation());
		p.setText("nonmotile a little ");
		extractor.detectIntermNegation(p);
		System.out.println(p.getText()+":"+p.getNegation());
		*/
		
		
		
		
		//extractor.initParser();
		//Flexirubin-type pigments are not produced.
		//Gram-negative, short rod, non-motile and not forming spores
		//Does not hydrolyse DNA, cellulose, CM-cellulose, chitin or Tween 80.
		//H2S is produced, but indole is not. 
		//Nitrate is not reduced. 
		//Aesculin, casein, gelatin, starch and Tween 20 are hydrolysed, but agar, DNA and carboxymethylcellulose are not.
		//String sentence =" Colonies on solid medium containing 3.0% Noble agar are small and granular with dense centers but do not have a true fried-egg appearance.";
		//String sentence = "Colonies on solid media (ZoBell 2216e and TSA plus sea water) are yellowish, slightly convex (elevation), entire (margin) and round (configuration). ";
		//String sentence ="Does not hydrolyse DNA, cellulose, CM-cellulose, Grammoptera sp.,L. decemlineata, chitin or Tween 80";
		//String sentence ="In the API ZYM system, alkaline phosphatase, esterase (C4), esterase lipase (C8), leucine arylamidase, valine arylamidase, cystine arylamidase, acid phosphatase and naphthol-AS-BI-phosphohydrolase activities are present, but lipase (C14), trypsin, α-chymotrypsin, α-galactosidase, β-galactosidase, β-glucuronidase, α-glucosidase, β-glucosidase, N-acetyl-β-glucosaminidase, α-mannosidase and α-fucosidase activities are absent.";
		//String sentence ="Cells are Gram-negative, chemoheterotrophic, obligately aerobic, straight rods devoid of flagellar and gliding motility.";
		//String sentence = "Peaks in absorption spectra for the cellular pigments are observed at 397, 450 (major peak) and 470 nm.";
		//String sentence = "The only benefit of creating the SB outside is not losing the internal (potentially long) char[] of it";
		//String sentence ="I suggest creating a new StringBuffer (or even better, StringBuilder) for each iteration. ";
		//String sentence="Avoid declaring StringBuffer or StringBuilder objects within the loop else it will create new objects with each iteration.";
		//String sentence="Devoid of flagellar and gliding motilities.";
		//String sentence="Flexirubin-type pigments are absent";
		//String sentence="Enlarged cells and filamentous cells are seen occasionally in stationary phase in broth culture.";
		//String sentence="an Enlarged and filamentous cells is seen occasionally in stationary phase in broth culture.";
		//String sentence="Gliding motility is present.";
		//String sentence="Cells are rod-shaped with rounded ends, non-flagellated and non-gliding.";
		//The major cellular fatty acids are anteiso-C15:0, C18:1ω9c, iso-C15:0, and summed feature 11 (comprising iso-C17:0 3-OH and/or C18:2 dimethylacetal).
		//String sentence="Cells are Gram-negative, chemoheterotrophic, obligately aerobic, straight rods devoid of flagellar and gliding motility";
		String sentence="The type strain, DOKDO 007T  (= KCCM 42307T= JCM 13831T), was isolated from the rhizosphere of the marine alga Ecklonia kurome  collected on Dokdo Island, Korea.";
		//String sentence="Gram-negative, short rod, non-motile and not forming spores";
		
		//Tree phraseStructure = extractor.parsePhraseStructTree(sentence);
		//System.out.println(phraseStructure);
		
		//extractor.getNounPhrases(phraseStructure);
		
		PosTagger posTagger = new PosTagger("edu/stanford/nlp/models/pos-tagger/english-bidirectional/english-bidirectional-distsim.tagger");
		
		LexicalizedParser lexParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		StanfordParserWrapper parser = new StanfordParserWrapper(null, lexParser);
		
		
		
		CSVSentenceReader trainingSentenceReader = new CSVSentenceReader();
		String sentFile = "F:\\MicroPIE\\micropieInput\\sentences\\keywordbased.txt";
		trainingSentenceReader.setInputStream(sentFile);
		
		
		List<TaggedWord> taggedWords  = posTagger.tagString(sentence);
		System.out.println(sentence+"\nPOS:");
		//System.out.println(sentence);
		System.out.println(taggedWords);
		//List<Phrase> phraseList = extractor.extract(taggedWords);
		//List<Phrase> phraseList = extractor.extractNounPharse(lexParser.parse(sentence));
		List<Phrase> phraseList = extractor.extractVerbPharse(lexParser.parse(sentence));
		for(Phrase p : phraseList){
			System.out.println(p.getType()+":"+p.getNegation()+"|"+p.getText()+" ["+p.getStartIndex()+"-"+p.getEndIndex()+"]"+" ["+p.getStart()+"-"+p.getEnd()+"] the core is:"+p.getCore()+" the modifer is:"+p.getModifer());
		}
		
		//RelationParser relationParser = new RelationParser();
		//PhraseRelationGraph prGraph = relationParser.parseCoordinativeRelationGraph(phraseList,taggedWords);
		
		
		/*
		List<RawSentence> trainingSentences = trainingSentenceReader.readOneColumnSentenceList();
		
		for(RawSentence sent : trainingSentences){
			sentence = sent.getText();
	 
			System.out.println(sentence);
	 		//Tree tree = parser.parsePhraseTree(sentence);
			//extractor.extract(tree);
			
			List<TaggedWord> taggedWords  = posTagger.tagString(sentence);
			System.out.println("\nPOS:\n");
			//System.out.println(sentence);
			//System.out.println(taggedWords);
			extractor.extract(taggedWords);
		}*/
		
	}
}
