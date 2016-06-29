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
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;


/**
 * This phrase parser is only useful for extracting values, not a complete phrase parser that can handle all types of phrases.
 * 
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
	private FigureClassifier figureClassifier = new FigureClassifier();
	
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
		//meaningfulTag.add("DT");//determiner 表示限定词, a word (such as “a,” “the,” “some,” “any,” “my,” or “your”) 
		meaningfulTag.add("JJ");
		meaningfulTag.add("JJR");
		meaningfulTag.add("JJS");
		//meaningfulTag.add("NN");
		meaningfulTag.add("RB");
		meaningfulTag.add("VBG");
		//meaningfulTag.add("VBN");
		//meaningfulTag.add("IN"); in of at with .....
		meaningfulTag.add("CD");
		//meaningfulTag.add(":");
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
	
	private static Set breakWord = new HashSet();
	static {
		//breakWord.add("and");
		breakWord.add("but");
		breakWord.add("or");
		breakWord.add("as");
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
		negationSet.add("devoid");
		//negationSet.add("negative");
	}
	
	
	/**
	 * allowed tags in the verb phrases
	 */
	private static Set verbSet = new HashSet();
	static{
		verbSet.add("VBZ");// verb, present tense,3rd person singular
		verbSet.add("VBP");//are
		verbSet.add("VBD");//verb, past tense
		verbSet.add("VBN");// verb, past participle
		verbSet.add("VBG");//verb, present participle or gerund
		verbSet.add("VB");
	}
	
	
	/**
	 * allowed tags in the verb phrases
	 */
	private static Set verbPhraseSet = new HashSet();
	static{
		verbPhraseSet.add("IN");
		verbPhraseSet.add("ADV");
		verbPhraseSet.add("RB");//adverb
		verbPhraseSet.add("RBR");
		verbPhraseSet.add("RBS");
		verbPhraseSet.add("RP");//particle
		verbPhraseSet.add("TO");
	}
	
	
	
	/**
	 * allowed tags in the verb phrases
	 */
	private static Set verbStopSet = new HashSet();
	static{
		verbStopSet.add("=");
		verbStopSet.add("is");
		verbStopSet.add("was");
		verbStopSet.add("are");
		verbStopSet.add("were");
	}
	
	/**
	 * stop words
	 */
	private static Set stopWordSet = new HashSet();
	static{
		stopWordSet.add("and");
		stopWordSet.add("or");
		stopWordSet.add("but");
		stopWordSet.add("in");
		stopWordSet.add("on");
		stopWordSet.add("above");
		stopWordSet.add("for");
		stopWordSet.add("of");
		stopWordSet.add("not");
		
	}
	
	/**
	 * stop words
	 */
	private static Set advAjdSet = new HashSet();
	static{
		advAjdSet.add("JJ");
		advAjdSet.add("JJR");
		advAjdSet.add("JJS");
		advAjdSet.add("RB");
		advAjdSet.add("RBR");
		advAjdSet.add("RBS");
	}
	
	
	
	
	/**
	 * working with the phrase structure parse tree, rather than the dependencies representation.
	 * @param parse
	 * @return
	 */
	public List<String> getNounPhrases(Tree phraseTree) {
	    List<String> result = new ArrayList<>();
	    // TregexPattern pattern = TregexPattern.compile("NP << NN|NNS|NNP");//,NNP,NNS
	   TregexPattern pattern = TregexPattern.compile("@NP");
	    TregexMatcher matcher = pattern.matcher(phraseTree);
	    Set hitSet = new HashSet();
	    while (matcher.find()) {
	        Tree match = matcher.getMatch();
	        List<Tree> leaves = match.getLeaves();
	       // System.out.println(leaves);
	        //// Some Guava magic.
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
	 * only reture noun phrases
	 * deepest search
	 * @param parse
	 * @return
	 */
	public List<Phrase> extractNounPharse(Tree phraseTree){
		List<Phrase> foundPhraseNodes = new ArrayList();
		depthTraval(phraseTree, foundPhraseNodes);
		return foundPhraseNodes;
	}
	
	
	
	/**
	 * was isolated from
	 * isolated in 1977 from
	 * 
	 * see: Etzioni, O., Fader, A., Christensen, J., Soderland, S., & Mausam, M. (2011, July). Open Information Extraction: The Second Generation. In IJCAI (Vol. 11, pp. 3-10).
	 * V | V P | VWP
	 * V = verb particle? adv?
	 * W = (noun | adj | adv | pron | det)
	 * P = (prep | particle | inf. marker)
	 * @param tree
	 * @param verbPhraseNodes
	 */
	public List<Phrase> extractVerbPharse(Tree phraseTree){
		List<LabeledWord> wordList = phraseTree.labeledYield();
		int size = wordList.size();
		
		List<Phrase> verbPhraseNodes = new ArrayList();
		for(int vid = 0; vid<size; vid++){
			LabeledWord word = wordList.get(vid);
			String text = word.word();
			String tag = word.tag().value();
			
			if(verbSet.contains(tag)&&!verbStopSet.contains(text)){//a verb candidate
				Phrase phrase = new Phrase();
				phrase.setStart(word.beginPosition());
				phrase.setStartIndex(vid);
				phrase.setEnd(word.endPosition());
				phrase.setEndIndex(vid);
				phrase.setText(text);
				phrase.setCore(text);
				backwardSearchVerbPhrase(wordList,phrase,vid);
				forwardSearchVerbPhrase(wordList,phrase,vid);
				
				phrase.setType("V");
				
				verbPhraseNodes.add(phrase);
			}
		}
		
		return verbPhraseNodes;
	}


	/**
	 * forward search the allowed verb tags
	 * @param wordList
	 * @param text
	 * @param vid
	 */
	public void forwardSearchVerbPhrase(List<LabeledWord> wordList, Phrase phrase, int vid) {
		for(int j=vid+1;j<wordList.size();j++){
			LabeledWord word = wordList.get(j);
			String text = word.word();
			String tag = word.tag().value();
			if(verbPhraseSet.contains(tag)){
				phrase.setText(phrase.getText()+" "+text);
				phrase.setEnd(word.endPosition());
				phrase.setEndIndex(j);
			}else{
				break;
			}
		}
	}


	/**
	 * backward search the allowed verb tags
	 * @param wordList
	 * @param text
	 * @param vid
	 */
	public void backwardSearchVerbPhrase(List<LabeledWord> wordList, Phrase phrase, int vid) {
		for(int j=vid-1;j>0;j--){
			LabeledWord word = wordList.get(j);
			String text = word.word();
			String tag = word.tag().value();
			if(verbPhraseSet.contains(tag)){
				phrase.setText(text+" "+phrase.getText());
				phrase.setStart(word.beginPosition());
				phrase.setStartIndex(j);
			}else{
				break;
			}
		}
	}


	/**
	 * Depth-First-Search
	 * 
	 * 
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
		    //System.out.println(tree.firstChild().depth()+" "+coreLabel.beginPosition()+" "+coreLabel.endPosition());
		    //Tree match = tree.firstChild();
		   // String text  =((CoreLabel)match.label()).getString(OriginalTextAnnotation.class);
		    //String curText  = coreLabel.getString(OriginalTextAnnotation.class);
		    String category = coreLabel.getString(CoreAnnotations.CategoryAnnotation.class);
		    //List<LabeledWord> labeledYield = match.labeledYield();
	        
		   // System.out.println(category+ " "+text);
	        if (nounNodeNames.contains(category)||adjNames.contains(category)) {
//	        	List<Word> wordList = tree.yieldWords();
//	        	int start = wordList.get(0).beginPosition();
//	        	int end = wordList.get(wordList.size()-1).endPosition();
	            Phrase phrase = flatten(tree);
//	            phrase.setStart(start);
//	            phrase.setEnd(end);
	            
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
		int start = tree.labeledYield().get(0).beginPosition();
		int end = tree.labeledYield().get(tree.labeledYield().size()-1).endPosition();; 
		for(LabeledWord word : tree.labeledYield()){
			phraseStr += word.word() +" ";
		}
		Phrase newPhrase = new Phrase();
		newPhrase.setText(phraseStr.trim());
		newPhrase.setStart(start);
		newPhrase.setEnd(end);
		return newPhrase;
	}



	/**
	 * extract by POS Tag Sequence
	 * @param sentTaggedWords
	 * @return
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
			}else if(curStr==null&&"CD".equals(tag)){//ends with a number
				if(i>0&&figureClassifier.isEntity(tagWord, sentTaggedWords.get(i-1))){
					curStr = new ArrayList();
					expressions.add(curStr); 
					curStr.add(0,tagWord);//add current
					curType = "N";
				}
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
	 * filter and generate the final strings
	 * 	1, the word, "and," should not be put in the first place.
	 * 	2, if the word, "and",  is in the middle, separate in to multiple . 
	 * @param meanExp
	 * @return
	private List<Phrase> filter(List<TaggedWord> meanExp) {
		StringBuffer str = new StringBuffer();
		List pharseList = new ArrayList();
		Phrase p = new Phrase();
		List<TaggedWord> phraseWords = new ArrayList();
		p.setWordTags(phraseWords);
		for(int i=0;i<meanExp.size();i++){
			TaggedWord tw = meanExp.get(i);
			String word = tw.word();
			if(i==0&&!("and".equals(word)||"but".equals(word)||"or".equals(word))) {
				str.append(word);
				str.append(" ");
				p.setStart(tw.beginPosition());
				p.setEnd(tw.endPosition());
				phraseWords.add(tw);
			}else if("and".equals(word)||"but".equals(word)||"or".equals(word)) {//not in the first
				p.setText(str.toString().trim());
				
				if(!"".equals(p.getText())) pharseList.add(p);
				p = new Phrase();
				str = new StringBuffer();
				phraseWords = new ArrayList();
				p.setWordTags(phraseWords);
			}else{
				if(str.length()==0) p.setStart(tw.beginPosition());
				str.append(word);
				str.append(" ");
				p.setEnd(tw.endPosition());
				phraseWords.add(tw);
			}
		}
		
		
		p.setText(str.toString().trim());
		if(!"".equals(p.getText())) pharseList.add(p);
		
		return pharseList;
	}*/
	
	/**
	 * extract by POS Tag Sequence
	 * @param sentTaggedWords
	 * @return
	 */
	public List<Phrase> extract(List<TaggedWord> sentTaggedWords){
		
		//reverse maximum match
		List<Phrase> expressions = new ArrayList();
		Phrase curPhrase = null;
		List curPhraseTagList = null;
		String curType = null;
		int length = sentTaggedWords.size();
		//System.out.println(sentTaggedWords);
		
		for(int i=length-1;i>=0;i--){
			
			TaggedWord tagWord = sentTaggedWords.get(i);
			String tag = tagWord.tag();
			String word = tagWord.word();
			if(breakTag.contains(tag)||breakWord.contains(word)){//separate
				//System.out.print(tagWord);
				curPhraseTagList = null;
			}else if(curPhraseTagList==null&&nounNodeNames.contains(tag)){//ends with a noun
				curPhrase = new Phrase();
				curPhrase.setStartIndex(i);
				curPhrase.setEndIndex(i);
				curPhraseTagList = new ArrayList();
				curPhrase.setWordTags(curPhraseTagList);
				expressions.add(curPhrase); 
				
				curPhraseTagList.add(0,tagWord);//add current
				curType = "N";
				curPhrase.setType(curType);
			}else if(curPhraseTagList==null&&adjNames.contains(tag)){//ends with an adjective
				curPhrase = new Phrase();
				curPhrase.setStartIndex(i);
				curPhrase.setEndIndex(i);
				curPhraseTagList = new ArrayList();
				curPhrase.setWordTags(curPhraseTagList);
				expressions.add(curPhrase); 
				
				curPhraseTagList.add(0,tagWord);//add current
				curType = "J";
				curPhrase.setType(curType);
			}else if(curPhraseTagList==null&&("CD".equals(tag)||"CC".equals(tag))){//ends with a number
				if(i>0&&figureClassifier.isEntity(tagWord, sentTaggedWords.get(i-1))){
					curPhrase = new Phrase();
					curPhrase.setStartIndex(i);
					curPhrase.setEndIndex(i);
					curPhraseTagList = new ArrayList();
					curPhrase.setWordTags(curPhraseTagList);
					expressions.add(curPhrase); 
					curPhraseTagList.add(0,tagWord);//add current
					curType = "N";
					curPhrase.setType(curType);
				}
			}else if(curPhraseTagList!=null&&"N".equals(curType)&&meaningfulTag.contains(tag)){//contain the meaning full word
				curPhraseTagList.add(0,tagWord);//add current
				curPhrase.setStartIndex(i);
			}else if(i>=1&&curPhraseTagList!=null&&"N".equals(curType)&&"VBN".equals(tag)&&nounNodeNames.contains(sentTaggedWords.get(i-1))){//if its VBN, the former must be a noun
				curPhraseTagList.add(0,tagWord);//add current
				curPhrase.setStartIndex(i);
			}else if(curPhraseTagList!=null&&"J".equals(curType)&&advModifierTag.contains(tag)){//contain the meaning full word
				curPhraseTagList.add(0,tagWord);//add current
				curPhrase.setStartIndex(i);
			}else {
				curPhraseTagList = null;
			}
		}
		
		//convert to string
		for(int i=0; i<expressions.size();i++){
			Phrase phrase = expressions.get(i);
			//filter the current phrase, and possibly add a new phrase
			filter(phrase,expressions);
		}
		
		
		//filter the expression
		// empity phrase, nul phrase, figure phrase
		for(int i=0; i<expressions.size();){
			Phrase phrase = expressions.get(i);
			//not empty
			if(phrase.getWordTags().size()==0||phrase.getText().trim().isEmpty()||phrase.getWordTags().get(0).equals("CD")||phrase.getWordTags().get(0).equals("CC")){
				expressions.remove(phrase);
			}else{
				i++;
			}
		}
				
				
		
		//detect the core of the phrase
		for(int i=0; i<expressions.size();i++){
			Phrase phrase = expressions.get(i);
			String core = detectCore(phrase);
			phrase.setCore(core);
		}
		
		//detect the modifier of the phrase
		for(int i=0; i<expressions.size();i++){
			Phrase phrase = expressions.get(i);
			String modifer = detectModifier(phrase);
			phrase.setModifer(modifer);
		}
				
		//NOT TO DETECT NEGATION
		//for(Phrase p: mfPhrases){
		//	this.detectIntermNegation(p);
		//}
		
		//whether there are outer negations
		TaggedWord negationWord = detectOutterNegation(sentTaggedWords);
		//System.out.println("negationWord="+negationWord);
		if(negationWord!=null){
			//determine the scope of its application
			//int negStart = negationWord.beginPosition();
			//negStart should be in the scope
			determineNegationScope(negationWord, sentTaggedWords, expressions);
		}
		return expressions;
	}
	
	

	

	/**
	 * filter and generate the final strings
	 * 	1, the word, "and," should not be put in the first place.
	 * 	2, if the word, "and",  is in the middle, separate in to multiple . 
	 * @param meanExp
	 * @return
	 */
	private void filter(Phrase phrase, List<Phrase> expressions) {
		StringBuffer str = new StringBuffer();
		List pharseList = new ArrayList();
		List<TaggedWord> meanExp = phrase.getWordTags();
		
		//new phrasewords
		List<TaggedWord> phraseWords = new ArrayList();
		phrase.setWordTags(phraseWords);
		int index = phrase.getStartIndex();
		for(int i=0;i<meanExp.size();i++){
			
			TaggedWord tw = meanExp.get(i);
			String word = tw.word();
			//System.out.println(i+" "+word);
			boolean isNew = false;
			if((i==0||isNew)&&stopWordSet.contains(word.toLowerCase())) {
				//System.out.println("stop="+word);
			}else if(i==0&&!stopWordSet.contains(word)) {
				if(i-1<0||tw.beginPosition()>meanExp.get(i-1).endPosition()) str.append(" ");
				str.append(tw.word());
				
				phrase.setStart(tw.beginPosition());
				phrase.setEnd(tw.endPosition());
				phraseWords.add(tw);
			}else if(i>0&&("and".equals(word)||"but".equals(word)||"or".equals(word)||"and/or".equals(word))) {//not in the first, create a new phrase, add it to the list
				//need to judge whether the ahead part has the same phrase type with the latter one
				String aheadTag = detectCurtag(meanExp.get(i-1).tag());//the tag of the word ahead of this
				String followTag = detectFollowtag(meanExp, i);
				//System.out.println(aheadTag+" "+followTag);
				if(aheadTag.equals(followTag)||followTag==null||i==meanExp.size()-2||(advAjdSet.contains(aheadTag)&&advAjdSet.contains(followTag))){//not merge, the left is only one
					phrase.setText(str.toString().trim().replace(" +","+"));
					phrase.setEndIndex(index-1);
					//if(!"".equals(phrase.getText())) pharseList.add(p);
					int insertIndex = expressions.indexOf(phrase);
					String type = phrase.getType();
					phrase = new Phrase();//a new one
					phrase.setStart(tw.beginPosition());
					phrase.setStartIndex(index+1);
					expressions.add(insertIndex, phrase);
					str = new StringBuffer();
					phraseWords = new ArrayList();
					phrase.setWordTags(phraseWords);
					phrase.setType(type);
					isNew = true;
				}else{// not the same type , merge
					if(i-1<0||tw.beginPosition()>meanExp.get(i-1).endPosition()) str.append(" ");
					str.append(tw.word());
					
					phrase.setEnd(tw.endPosition());
					phrase.setEndIndex(index);
					phraseWords.add(tw);
				}
			}else{
				//System.out.println(tw.beginPosition()+" "+meanExp.get(i-1).endPosition());
				if(str.length()==0) phrase.setStart(tw.beginPosition());
				//iso-15:0, iso-15:1ω10c and 10-methyl-16:0.
				if(i-1<0||tw.beginPosition()>meanExp.get(i-1).endPosition()) str.append(" ");
				str.append(tw.word());
				
				phrase.setEnd(tw.endPosition());
				phrase.setEndIndex(index);
				phraseWords.add(tw);
			}
			
			index++;
		}
		if(str.toString().trim().startsWith("not ")){
			phrase.setText(str.toString().trim().replace("not ", "").replace(" +","+"));
		}else{
			phrase.setText(str.toString().trim().replace(" +","+"));
		}
		
		//if(!"".equals(p.getText())) pharseList.add(p);
	}

	
	/**
	 * detect current tag type: N, J, and others
	 * @param tag
	 * @return
	 */
	private String detectCurtag(String tag) {
		if(tag.startsWith("J")){
			tag = "J";
		}else if(tag.startsWith("N")||tag.startsWith("FW")||tag.startsWith("CD")||tag.startsWith("CC")){
			return "N";
		}
		return tag;
	}


	/**
	 * detect the tag type following the term i
	 * @param meanExp
	 * @param i
	 * @return
	 */
	private String detectFollowtag(List<TaggedWord> meanExp, int i) {
		String tag = null;
		for(int j=i;j<meanExp.size();j++){
			TaggedWord tw = meanExp.get(j);
			if(tw.tag().startsWith("J")){
				tag = "J";
			}else if(tw.tag().startsWith("N")||tw.tag().startsWith("FW")){
				return "N";
			}else if(tw.tag().startsWith("RB")){
				return null;
			}
		}
		return tag;
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
	public TaggedWord detectOutterNegation(List<TaggedWord> sentTaggedWords){
		for(int i=0;i<sentTaggedWords.size();i++){
			TaggedWord tg = sentTaggedWords.get(i);
			if(negationSet.contains(tg.word().toLowerCase())){
				if("devoid".equalsIgnoreCase(tg.word())&&"of".equalsIgnoreCase(sentTaggedWords.get(i+1).word())){
					tg.setWord("devoid of");
					return tg;
				}else{
					return tg;
				}
			}
		}
		return null;
	}
	
	
	
	/**
	 * detect the standalone negation words that are not in the phrase.
	 * then to determine what phrases the negations can be applied to.
	 * 
	 */
	public TaggedWord applyNegative(List<TaggedWord> sentTaggedWords,List<Phrase> phraseList){
		TaggedWord negationWord = null;
		for(int i=0;i<sentTaggedWords.size();i++){
			TaggedWord tg = sentTaggedWords.get(i);
			if("negative".equalsIgnoreCase(tg.word().toLowerCase())){
				negationWord = tg;
				break;
			}
		}
		//System.out.println(negationWord);
		if(negationWord!=null){
			for(int i=phraseList.size()-1;i>=0;i--){//reverse
				Phrase p  =  phraseList.get(i);
				p.setNegation(negationWord.word());
			}
		}
		return negationWord;
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
		if(!"devoid of".equalsIgnoreCase(negationWord.word())){
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
		}else{//only the following
			for(int i=mfPhrases.size()-1;i>=0;i--){//reverse
				Phrase p  =  mfPhrases.get(i);
				if(p.getStart()>=negEnd){
					p.setNegation(negationWord.word());
					break;
				}
			}
		}
	}
	
	
	/**
	 * if it's NP, then the nouns are the core component
	 * if it's JP, then the adjectives are the core component
	 * @param phrase
	 * @return
	 */
	public String detectCore(Phrase phrase){
		StringBuffer core = new StringBuffer();
		List<TaggedWord> twList = phrase.getWordTags();
		if("N".equals(phrase.getType())){
			for(int i=twList.size()-1;i>=0;i--){
				TaggedWord tw = twList.get(i);
				String tag = tw.tag();
				if(nounNodeNames.contains(tag)||tw.tag().equals("CD")||tw.tag().equals("CC")){
					core.insert(0, " ").insert(0, tw.word());
				}else if(tw.tag().equals("IN")){
					core.delete(0, core.length());
				}
			}
		}else if("J".equals(phrase.getType())){
			for(int i=twList.size()-1;i>=0;i--){
				TaggedWord tw = twList.get(i);
				String tag = tw.tag();
				if(adjNames.contains(tag)){
					core.insert(0, " ").insert(0, tw.word());
				}else{
					break;
				}
			}
		}
		
		return core.toString().trim();
	}
	
	/**
	 * if it's NP, then the adjectives before the core are the modifiers
	 * if it's JP, then the adverbs before the core are the modifiers
	 * @param phrase
	 * @return
	 */
	public String detectModifier(Phrase phrase){
		StringBuffer modifier = new StringBuffer();
		List<TaggedWord> twList = phrase.getWordTags();
		if("N".equals(phrase.getType())){
			boolean begin = false;
			for(int i=twList.size()-1;i>=0;i--){
				TaggedWord tw = twList.get(i);
				String tag = tw.tag();
				if(nounNodeNames.contains(tag)||tw.tag().equals("CD")||tw.tag().equals("CC")){
					if(!begin&&phrase.getCore().indexOf(tw.word())>-1) begin= true;
					continue;
				}else if(adjNames.contains(tag)&&begin){
					modifier.insert(0, " ").insert(0, tw.word());
				}
			}
		}else if("J".equals(phrase.getType())){
			for(int i=twList.size()-1;i>=0;i--){
				TaggedWord tw = twList.get(i);
				String tag = tw.tag();
				if(adjNames.contains(tag)){
					continue;
				}else if(advModifierTag.contains(tag)){
					modifier.insert(0, " ").insert(0, tw.word());
				}
			}
		}
		
		return modifier.toString().trim();
	}
}
