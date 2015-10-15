package edu.arizona.biosemantics.micropie.nlptool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import edu.arizona.biosemantics.micropie.extract.keyword.ClauseSeparator;
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.LabeledWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;
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
	
	private ClauseSeparator clauseSeparator = new ClauseSeparator();
	
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
		meaningfulTag.add("JJR");
		meaningfulTag.add("JJS");
		meaningfulTag.add("NN");
		meaningfulTag.add("RB");
		//meaningfulTag.add("VBG");
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
		advModifierTag.add("RB");
		advModifierTag.add("RBR");
		advModifierTag.add("RBS");
		advModifierTag.add("JJ");
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
	
	//negation word list
	private static Set negationSet = new HashSet();
	static {
		negationSet.add("not");
		negationSet.add("no");
		negationSet.add("never");
		negationSet.add("neither");
		negationSet.add("nor");
		negationSet.add("don't");
		negationSet.add("cann't");
		negationSet.add("cannot");
		negationSet.add("absent");
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
		        //System.out.println("labeledYield: " + labeledYield);
		        hitSet.add(leaves);
	        }
	    }
	    return result;
	}
	
	
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
		//System.out.println(phraseStr);
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
		//System.out.println(sentTaggedWords);
		
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
		List<Phrase> mfPhrases = new ArrayList();
		for(List meanExp: expressions){
			List<Phrase> expressionStr = filter(meanExp);
			if(expressionStr!=null&&expressionStr.size()>0){
				mfPhrases.addAll(expressionStr);
			}
		}
		
		//NOT TO DETECT NEGATION
		//for(Phrase p: mfPhrases){
		//	this.detectIntermNegation(p);
		//}
		
		//whether there are outer negations
		TaggedWord negationWord = detectOuttermNegation(sentTaggedWords);
		if(negationWord!=null){
			//determine the scope of its application
			//int negStart = negationWord.beginPosition();
			// negStart should be in the scope
			determineNegationScope(negationWord, sentTaggedWords, mfPhrases);
		}
		return mfPhrases;
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
		Phrase p = new Phrase();
		
		for(int i=0;i<meanExp.size();i++){
			
			TaggedWord tw = meanExp.get(i);
			String word = tw.word();
			if(i==0&&!("and".equals(word)||"but".equals(word))) {
				str.append(word);
				str.append(" ");
				p.setStart(tw.beginPosition());
				p.setEnd(tw.endPosition());
			}else if("and".equals(word)||"but".equals(word)) {
				
				p.setText(str.toString().trim());
				if(!"".equals(p.getText())) pharseList.add(p);
				p = new Phrase();
				str = new StringBuffer();
			}else{
				if(str.length()==0) p.setStart(tw.beginPosition());
				str.append(word);
				str.append(" ");
				p.setEnd(tw.endPosition());
			}
		}
		
		
		p.setText(str.toString().trim());
		if(!"".equals(p.getText())) pharseList.add(p);
		
		return pharseList;
	}


	/**
	 * detect in-term negation, e.g., non-motile, non-~~
	 * @param phrase
	 * @return
	 */
	public String detectIntermNegation(Phrase phrase){
		String patternString = "\\snon(?=[a-z\\-])|^non(?=[a-z\\-])"; // regular expression pattern
		Matcher matcher = Pattern.compile(patternString).matcher(phrase.getText());
		String negation = null;
		if (matcher.find()) {
			negation = matcher.group().trim();
			phrase.setNegation(negation);
		}
		return negation;
	}
	
	
	/**
	 * detect the standalone negation words that are not in the phrase.
	 * then to determine what phrases the negations can be applied to.
	 * 
	 */
	public TaggedWord detectOuttermNegation(List<TaggedWord> sentTaggedWords){
		for(TaggedWord tg : sentTaggedWords){
			if(negationSet.contains(tg.word())) return tg;
		}
		return null;
	}
	
	/**
	 * apply the negation to its clause only
	 * @param negStart
	 * @param sentTaggedWords
	 * @param mfPhrases
	 */
	private void determineNegationScope(TaggedWord negationWord,
			List<TaggedWord> sentTaggedWords, List<Phrase> mfPhrases) {
		int negStart = negationWord.beginPosition();
		int negEnd = negationWord.endPosition();
		//System.out.println("negStart="+negStart);
		//System.out.println("negEnd="+negEnd);
		List<List<TaggedWord>> clauseList = clauseSeparator.detect(sentTaggedWords);
		List<TaggedWord> scopeClause = null;
		for(List<TaggedWord> clause:clauseList){
			//System.out.println(clause.get(0).beginPosition()+" "+clause.get(clause.size()-1).endPosition());
			if(clause.get(0).beginPosition()<=negStart&&clause.get(clause.size()-1).endPosition()>=negEnd){
				scopeClause = clause;
				break;
			}
		}
		//only one clause 
		if(scopeClause==null)  scopeClause = clauseList.get(0);
		
		int startPos = scopeClause.get(0).beginPosition();
		int endPos = scopeClause.get(scopeClause.size()-1).endPosition();
		//System.out.println("negation is applied to "+startPos+" "+endPos);
		for(Phrase p : mfPhrases){
			if(p.getStart()>=startPos&&p.getEnd()<=endPos){
				p.setNegation(negationWord.word());
			}
		}
	}
	
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
		String sentence ="Does not hydrolyse DNA, cellulose, CM-cellulose, chitin or Tween 80";
		//String sentence ="In the API ZYM system, alkaline phosphatase, esterase (C4), esterase lipase (C8), leucine arylamidase, valine arylamidase, cystine arylamidase, acid phosphatase and naphthol-AS-BI-phosphohydrolase activities are present, but lipase (C14), trypsin, α-chymotrypsin, α-galactosidase, β-galactosidase, β-glucuronidase, α-glucosidase, β-glucosidase, N-acetyl-β-glucosaminidase, α-mannosidase and α-fucosidase activities are absent.";
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
		List<Phrase> phraseList = extractor.extract(taggedWords);
		for(Phrase p : phraseList){
			System.out.println(p.getNegation()+"|"+p.getText()+" ["+p.getStart()+"-"+p.getEnd()+"]");
		}
		
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
