package edu.arizona.biosemantics.micropie.nlptool;

import java.io.FileInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.OriginalTextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.LabeledWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;


/**
 * Extract meaningful expressions：
 * 	1）noun, noun phrases, noun expressions
 * 	2) 
 * @author maojin
 *
 */
public class PhraseParser {
	/*
	private PosTagger posTagger;
	private StanfordParserWrapper parser;
	
	@Inject
	public MeaningfulExpressionExtractor(PosTagger posTagger,StanfordParserWrapper parser){
		this.parser = parser;
		this.posTagger = posTagger;
	}*/
	
	
	private static Set nounNodeNames;
	static {
	    nounNodeNames = new HashSet<String>();

	    nounNodeNames.add( "NP");
	    nounNodeNames.add( "NPS");
	    nounNodeNames.add( "FW");
	    nounNodeNames.add( "NN");
	    nounNodeNames.add( "NNS");
	    nounNodeNames.add( "NNP");
	    nounNodeNames.add( "NNPS");
	}
	
	private static Set adjNames;
	static {
		adjNames = new HashSet<String>();

		adjNames.add("JJ");
		adjNames.add("JJR");
		adjNames.add("JJS");
	}
	
	//meaningful tag in the sequence for noun
	private static Set meaningfulTag = new HashSet();
	static {
		meaningfulTag.add("DT");
		meaningfulTag.add("JJ");
		meaningfulTag.add("NN");
		meaningfulTag.add("RB");
		//meaningfulTag.add("VBN");
		meaningfulTag.add("IN");
		meaningfulTag.add("CD");
		meaningfulTag.add(":");
		meaningfulTag.add("FW");
		meaningfulTag.add("CC");
		//meaningfulTag.add("TO");
		meaningfulTag.addAll(nounNodeNames);
	}
	
	
	//meaningful tag in the sequence for adj
		private static Set advModifierTag = new HashSet();
		static {
			advModifierTag.add("DT");
		}
	
	//these tags end the sequence
	private static Set breakTag = new HashSet();
	static {
		breakTag.add("VBZ");
		breakTag.add("VBP");
		breakTag.add("VBD");
		breakTag.add("VB");
		breakTag.add(",");
		breakTag.add("-RRB-");
		breakTag.add("-LRB-");
	}
	

	
	/**
	 * working with the phrase structure parse tree, rather than the dependencies representation.
	 * @param parse
	 * @return
	 */
	private List<String> getNounPhrases(Tree parse) {
	    List<String> result = new ArrayList<>();
	    TregexPattern pattern = TregexPattern.compile("NP << NN|NNS|NNP");//,NNP,NNS
	    //TregexPattern pattern = TregexPattern.compile("@NP");
	    TregexMatcher matcher = pattern.matcher(parse);
	    Set hitSet = new HashSet();
	    while (matcher.find()) {
	        Tree match = matcher.getMatch();
	        List<Tree> leaves = match.getLeaves();
	        //System.out.println(leaves);
	        // Some Guava magic.
	        if(!hitSet.contains(leaves)){
		        String nounPhrase = Joiner.on(' ').join(Lists.transform(leaves, Functions.toStringFunction()));
		        result.add(nounPhrase);
		        List<LabeledWord> labeledYield = match.labeledYield();
		        System.out.println("labeledYield: " + labeledYield);
		        hitSet.add(leaves);
	        }
	    }
	    return result;
	}
	
	

/*
	public  List<NounPhrase> extractPhrasesFromString(Tree tree, String originalString) {
	    List<NounPhrase> foundPhraseNodes = new ArrayList<NounPhrase>();

	    collect(tree, foundPhraseNodes);
	    logger.debug("parsing " + originalString + " yields " + foundPhraseNodes.size() + " noun node(s).");
	    if (foundPhraseNodes.size() == 0) {
	        foundPhraseNodes.add(new NounPhrase(tree, originalString));
	    }
	    return  foundPhraseNodes;
	}

	private void collect(Tree tree, List<NounPhrase> foundPhraseNodes) {
	    if (tree == null || tree.isLeaf()) {
	        return;
	    }


	    Label label = tree.label();
	    if (label instanceof CoreLabel) {
	        CoreLabel coreLabel = ((CoreLabel) label);

	        String text = ((CoreLabel) label).getString(CoreAnnotations.OriginalTextAnnotation.class);
	        logger.debug(" got text: " + text);
	        if (text.equals("THE")) {
	            logger.debug(" got THE text: " + text);
	        }

	        String category = coreLabel.getString(CoreAnnotations.CategoryAnnotation.class);
	        if (nounNodeNames.contains(category)) {
	            NounPhrase phrase = null;
	            String phraseString = flatten(tree);
	            if ((phrase = stringToNounPhrase.get(phraseString)) == null) {
	                phrase = new NounPhrase(tree, phraseString);
	                stringToNounPhrase.put(phraseString, phrase);
	            }

	            if (! foundPhraseNodes.contains(phrase)) {
	                logger.debug("adding found noun phrase to list: {}", phrase.debug());
	                foundPhraseNodes.add(phrase);
	            } else {
	                logger.debug("on list already, so skipping found noun phrase: {}", phrase.debug());
	            }
	        }
	    }


	    List<Tree> kids = tree.getChildrenAsList();
	    for (Tree kid : kids) {
	        collect(kid, foundPhraseNodes);
	    }
	}
	*/
	
	
	/**
	 * working with the phrase structure parse tree, rather than the dependencies representation.
	 * deepest search
	 * @param parse
	 * @return
	 */
	public List<Phrase> extract(Tree tree){
		List<Phrase> foundPhraseNodes = new ArrayList();
		depthTraval(tree, foundPhraseNodes);
		
		return foundPhraseNodes;
	}
	
	
	/**
	 * Depth-First-Search
	 * @param tree
	 * @param path
	 */
	public void depthTraval(Tree tree, List<Phrase> foundPhraseNodes){
		 if (tree == null || tree.isLeaf()) {
		        return;
		    }


		 Label label = tree.label();
		 if (label instanceof CoreLabel) {
		 //if (!tree.isLeaf()&&tree.children().length==1&&tree.firstChild().isLeaf()) {//it's the leaf
		 // if (tree.isPhrasal()&&tree.isUnaryRewrite()) {//it's the leaf
		    CoreLabel coreLabel = ((CoreLabel) label);
		    //Tree match = tree.firstChild();
		   // String text  =((CoreLabel)match.label()).getString(OriginalTextAnnotation.class);
		    //String curText  = coreLabel.getString(OriginalTextAnnotation.class);
		    String category = coreLabel.getString(CoreAnnotations.CategoryAnnotation.class);
		    //List<LabeledWord> labeledYield = match.labeledYield();
	        
		   // System.out.println(category+ " "+text);
	        if (nounNodeNames.contains(category)||adjNames.contains(category)) {
	            Phrase phrase = flatten(tree);
	            if (! foundPhraseNodes.contains(phrase)) {
	                foundPhraseNodes.add(phrase);
	            } else {
	                System.out.println("on list already, so skipping found noun phrase: {}");
	            }
	            return;
		    }
		    
		 }
		List<Tree> kids = tree.getChildrenAsList();
		
		for (Tree kid : kids) {
			depthTraval(kid, foundPhraseNodes);
		}
		 
	}
	
	
	
	/**
	 * 
	 * @param tree
	 * @return
	 */
	private Phrase flatten(Tree tree) {
		
		String phraseStr = "";
		for(LabeledWord word : tree.labeledYield()){
			phraseStr += word.word() +" ";
		}
		Phrase newPhrase = new Phrase();
		newPhrase.setText(phraseStr.trim());
		System.out.println(phraseStr);
		return newPhrase;
	}



	/**
	 * extract by POS Tag Sequence
	 * @param sentTaggedWords
	 * @return
	 */
	public List<Phrase> extract(List<TaggedWord> sentTaggedWords){
		
		//reverse maximuml match
		List<List> expressions = new ArrayList();
		List curStr = null;
		String curType = null;
		int length = sentTaggedWords.size();
		
		
		for(int i=length-1;i>=0;i--){
			TaggedWord tagWord = sentTaggedWords.get(i);
			String tag = tagWord.tag();
			String word = tagWord.word();
			if(curStr==null&&nounNodeNames.contains(tag)){//ends with a noun
				curStr = new ArrayList();
				expressions.add(curStr); 
				curStr.add(0,tagWord);//add current
				curType = "N";
			}else if(curStr==null&&adjNames.contains(tag)){//ends with an adjective
				curStr = new ArrayList();
				expressions.add(curStr); 
				curStr.add(0,tagWord);//add current
				curType = "J";
			}else if(curStr!=null&&"N".equals(curType)&&meaningfulTag.contains(tag)){//contain the meaning full word
				curStr.add(0,tagWord);//add current
			}else if(i>=1&&curStr!=null&&"N".equals(curType)&&"VBN".equals(tag)&&nounNodeNames.contains(sentTaggedWords.get(i-1))){//if its VBN, the former must be a noun
				curStr.add(0,tagWord);//add current
			}else if(curStr!=null&&"J".equals(curType)&&advModifierTag.contains(tag)){//contain the meaning full word
				curStr.add(0,tagWord);//add current
			}else if(curStr!=null&&breakTag.contains(tag)){
				curStr = null;
			}else{
				curStr = null;
			}
		}
		
		//convert to string
		List<Phrase> mfString = new ArrayList();
		for(List meanExp: expressions){
			List<Phrase> expressionStr = filter(meanExp);
			if(expressionStr!=null&&expressionStr.size()>0){
				mfString.addAll(expressionStr);
			}
		}
		
		for(Phrase phrase: mfString){
			System.out.println("\t"+phrase.getText());
		}
		
		return mfString;
	}
	
	
	/**
	 * filter and generate the final strings
	 * 	1, the word, "and," should not be put in the first place.
	 * 	2, if the word, "and",  is in the middle, separate in to multiple . 
	 * @param meanExp
	 * @return
	 */
	private List<Phrase> filter(List<TaggedWord> meanExp) {
		StringBuffer str = new StringBuffer();
		List pharseList = new ArrayList();
		for(int i=0;i<meanExp.size();i++){
			TaggedWord tw = meanExp.get(i);
			String word = tw.word();
			if(i==0&&!"and".equals(word)) {
				str.append(word);
				str.append(" ");
			}else if("and".equals(word)||"but".equals(word)) {
				Phrase p = new Phrase();
				p.setText(str.toString().trim());
				pharseList.add(p);
				str = new StringBuffer();
			}else{
				str.append(word);
				str.append(" ");
			}
		}
		
		Phrase p = new Phrase();
		p.setText(str.toString().trim());
		pharseList.add(p);
		//pharseList.add(str.toString().trim());
		
//		if(str.length()>0){
//			return str.toString().trim();
//		}else{
//			return null;
//		}
//		
		return pharseList;
	}



	public static void main(String[] args){
		PhraseParser extractor = new PhraseParser();
		//extractor.initParser();
		//String sentence =" Colonies on solid medium containing 3.0% Noble agar are small and granular with dense centers but do not have a true fried-egg appearance.";
		String sentence = "Colonies on solid media (ZoBell 2216e and TSA plus sea water) are yellowish, slightly convex (elevation), entire (margin) and round (configuration). ";
		//Tree phraseStructure = extractor.parsePhraseStructTree(sentence);
		//System.out.println(phraseStructure);
		
		//extractor.getNounPhrases(phraseStructure);
		
		PosTagger posTagger = new PosTagger("edu/stanford/nlp/models/pos-tagger/english-bidirectional/english-bidirectional-distsim.tagger");
		
		LexicalizedParser lexParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		StanfordParserWrapper parser = new StanfordParserWrapper(null, lexParser);
		
		
		
		CSVSentenceReader trainingSentenceReader = new CSVSentenceReader();
		String sentFile = "F:\\MicroPIE\\micropieInput\\sentences\\keywordbased.txt";
		trainingSentenceReader.setInputStream(sentFile);
		
		List<RawSentence> trainingSentences = trainingSentenceReader.readOneColumnSentenceList();
		
		for(RawSentence sent : trainingSentences){
			sentence = sent.getText();
	 
			System.out.println(sentence);
	 		Tree tree = parser.parsePhraseTree(sentence);
			extractor.extract(tree);
			
			List<TaggedWord> taggedWords  = posTagger.tagString(sentence);
			System.out.println("\nPOS:\n");
			//System.out.println(sentence);
			//System.out.println(taggedWords);
			extractor.extract(taggedWords);
		}
		
	}
}
