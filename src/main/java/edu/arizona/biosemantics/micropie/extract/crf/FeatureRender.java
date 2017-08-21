package edu.arizona.biosemantics.micropie.extract.crf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.Configuration;
import edu.arizona.biosemantics.micropie.io.FileReaderUtil;
import edu.arizona.biosemantics.micropie.nlptool.StanfordParserWrapper;
import edu.arizona.biosemantics.micropie.nlptool.StringUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.UniversalPOSMapper;
import edu.stanford.nlp.util.CoreMap;


/**
 * Designed Features:
 * 
 * The features are:
 *  0 the text of the token
	1 typographic type
	2 presence of capitalized characters
	3 presence of punctuation 
	4 presence of digit
	5 the lemma of the token
	6 POS tag
	7 Word sense
    8 Cluster identifier according to the Brown cluster
	9 word embedding
	10 Presence in the term list of geographical names
	11 label
 * @author maojin
 */
public class FeatureRender {
	
	private Map<String, String> brownClusterMap; 
	private Map<String, String> weClusterMap; 
	private Set<String> geoUniqSet;
	
	//private WekaClusterMatcher wordEmbeddingCluster;//word embedding clusters
	
	private WordSenseRender wordSenseRender;
	private StanfordCoreNLP sfCoreNLP;
	
	private int fileNum;
	private int sentenceNum;
	private int tokenNum;
	
	@Inject
	public FeatureRender(@Named("TokenizeSSplitPosParse")StanfordCoreNLP sfCoreNLP){
		wordSenseRender = new WordSenseRender();
		brownClusterMap = readBrownCluster(Configuration.configurationFolder+"/"+Configuration.brownClusterFile);
		weClusterMap = readWECluster(Configuration.configurationFolder+"/"+Configuration.wordEmbeddingClusterFile);
		geoUniqSet = readGeoTerms(Configuration.configurationFolder+"/"+Configuration.geoUniqTermFile);
		this.sfCoreNLP=sfCoreNLP;
	}
	
	public Map<String, String> readBrownCluster(String brownClusterFile){
		List<String> lineStr = FileReaderUtil.readFileLines(brownClusterFile);
		Map<String, String> brownCluster = new HashMap();
		for(String line : lineStr){
			String[] fields = line.split("\t");
			brownCluster.put(fields[1], fields[0]);
		}
		return brownCluster;
	}
	
	public Map<String, String> readWECluster(String wordEmbeddingClusterFile){
		List<String> lineStr = FileReaderUtil.readFileLines(wordEmbeddingClusterFile);
		Map<String, String> brownCluster = new HashMap();
		for(String line : lineStr){
			String[] fields = line.split("[\\s\t]+");
			brownCluster.put(fields[0], "WE"+fields[1]);
		}
		return brownCluster;
	}
	
	public Set<String> readGeoTerms(String geoUniqFile){
		List<String> lineStr = FileReaderUtil.readFileLines(geoUniqFile);
		Set<String> geoTerms = new HashSet();
		for(String line : lineStr){
			geoTerms.add(line.trim());
		}
		return geoTerms;
	}
	
	/**
	 * render the features for one sentence
	 * @param sentence the source sentence
	 * @param datasetName
	 */
	public List<Token> render(String sentence) {
		List<Token> sentTokens = tokenizeWithBasicFeatures(sentence);
		this.renderAdvFeatures(sentTokens);
		return sentTokens;
	}
	
	/**
	 * use standford parser to obtain simple features
	 * @param sentence
	 * @return
	 */
	private List<Token> tokenizeWithBasicFeatures(String sentence) {
		Annotation annotation = new Annotation(sentence);
		this.sfCoreNLP.annotate(annotation);
		
		List<Token> tokenList = new ArrayList();
		
      	List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
      	for(CoreMap sent: sentences) {
      		 for (CoreLabel ctoken: sent.get(TokensAnnotation.class)) {
               String word = ctoken.get(TextAnnotation.class);
               if("-RRB-".equals(word)){
            	   word = ")";
               }else if("-LRB-".equals(word)){
            	   word = "(";
               } //-LRB-
               String lemma = ctoken.get(LemmaAnnotation.class);
               Token token = new Token();
				token.setText(word);
				token.setAttribute(TokenAttribute.POS, ctoken.tag());
				token.setOffset(ctoken.beginPosition());
				token.setOffend(ctoken.endPosition());
				token.setLemma(lemma);
				
				tokenList.add(token);
      		 }
      	}
		return tokenList;
	}




	/**
	 * return the token index in the tokenList
	 * 
	 * @param tokenList
	 * @param sentId
	 * @param startId
	 * @return
	 */
	public int getToken(List<Token> tokenList, int startId, String tokenStr){
		for(;startId<tokenList.size();startId++){
			Token token = tokenList.get(startId);
			if(token.getText().equals(tokenStr)){
				return startId;
			}
		}
		return -1;
	}
	
	/**
	 * for a file, get the token list and render the features 
	 * @param datasetName
	 * @param collxFile
	 * @return
	 */
	private List<Token> renderAdvFeatures(List<Token> sentTokens){

		int tokenId = 1;
		for (Token token : sentTokens) {
			tokenNum++;
			//re-order
			token.setTokenId(tokenId++);
			
			//features from shallow parse results
			token.setAttribute(TokenAttribute.Length, token.getText().length());
			token.setAttribute(TokenAttribute.Typographic, StringUtil.getTypographic(token.getText()));//4 typographic types
			token.setAttribute(TokenAttribute.pPunct, StringUtil.hasPunctuation(token.getText()));//5 isPunctuation  Punc_
			token.setAttribute(TokenAttribute.pDigit, StringUtil.hasDigit(token.getText()));//6 presentsDigit   Digit_
			token.setAttribute(TokenAttribute.pCap, StringUtil.hasCapital(token.getText()));//6 presentsDigit   Digit_
			
			String bclusterID = brownClusterMap.get(token.getText());
			bclusterID = bclusterID == null ? "O" : bclusterID;
			token.setAttribute(TokenAttribute.BrownCluster, bclusterID);
			
			String weclusterID = weClusterMap.get(token.getText());
			weclusterID = weclusterID == null ? "O" : weclusterID;
			token.setAttribute(TokenAttribute.wordEmbedCluster, weclusterID);
			
			//get word sense
			String posTag = (String)token.getAttribute(TokenAttribute.POS);
			token.setAttribute(TokenAttribute.wordSense, wordSenseRender.getSense(token.getLemma(), posTag));
			
			token.setAttribute(TokenAttribute.isGeo, this.geoUniqSet.contains(token.getText()));
			
			//token.setAttribute(TokenAttribute.wordEmbedCluster, wordEmbeddingCluster.findCluster(token.getText()));
		}
		return sentTokens;
	}
	
	
	public StringBuffer generateLineForToken(Token token){
		StringBuffer sb = new StringBuffer();
		sb.append(token.getText()+" ");//0 token
		//fw.write(""+token.getOffset()+" ");//1: character offset
		sb.append(token.getAttribute(TokenAttribute.Typographic)+" ");//2 typographic types
		sb.append((boolean)token.getAttribute(TokenAttribute.pCap)?"pCap":"O");//3 presence of capitalized characters
		sb.append(" ");
		sb.append((boolean)token.getAttribute(TokenAttribute.pPunct)?"pPunct":"O");//4 presence of punctuation 
		sb.append(" ");
		sb.append((boolean)token.getAttribute(TokenAttribute.pDigit)?"pDigit":"O");//5 presentsDigit   Digit_
		sb.append(" ");
		sb.append(token.getLemma()+" ");//7 the lemma of the token

		sb.append(token.getAttribute(TokenAttribute.POS)+" ");//8   POS;
		
		String wordSense = (String) token.getAttribute(TokenAttribute.wordSense);
		sb.append(wordSense==null?"O":"ws_"+wordSense);//21 word sense
		sb.append(" ");
		
		sb.append(token.getAttribute(TokenAttribute.BrownCluster)+" ");//20 BrownCluster
		
		String wordembCluster = (String) token.getAttribute(TokenAttribute.wordEmbedCluster);
		sb.append(wordembCluster==null?"O":wordembCluster);//21 word sense
		sb.append(" ");
		
		sb.append((boolean)token.getAttribute(TokenAttribute.isGeo)?"pGeo":"O");//5 presentsInGeoTermList
		sb.append(" ");
		return sb;
	}
	
	public void renderLabel(List<Token> tokens, List<BBEntity> entityList) {
		for(Token token:tokens){
			String nerType = detectNERType(entityList, token);
			token.setAttribute(TokenAttribute.NER, nerType);
		}
	}
	
	
	/**
	 * 
	 * @param annEntityList
	 * @return
	 */
	public String detectNERType(List<BBEntity> annEntityList, Token token) {
		//List<BBEntity> bbList = new ArrayList<BBEntity>();
		for(BBEntity orgEntity:annEntityList){
			int start = orgEntity.getStart();
			int end = orgEntity.getEnd();
			int start2 = orgEntity.getStart2();
			int end2 = orgEntity.getEnd2();
			String entityTxt = orgEntity.getName();
			String nerType = orgEntity.getType();
			if("Geographical_location".equals(nerType)) nerType="Geographical";
			String tokenStr = token.getText();
//			if("-LRB-".equals(tokenStr)) tokenStr="(";
//			if("-RRB-".equals(tokenStr)) tokenStr=")";
			if(((token.getOffset()>=start&&token.getOffend()<=end)||
					(token.getOffset()>=start2&&token.getOffend()<=end2)
					)&&entityTxt.indexOf(tokenStr)>-1){
				return nerType;
			}
		}
		return "O";
	}
	

	
	/**
	 * Different typographic types
	 * AA --- all capitalized
	 * Aa --- First capitalized
	 * aa --- all lowercases
	 * 
	 * 
	 * digits?
	 * O --- not characters
	 * @return
	 */
	public String getSimpleTypographic(String str){
		boolean containsLetter = false;
		//for(int i = 0;i < str.length();i++){
			if(!Character.isLetter(str.charAt(0))){
				containsLetter = true;
				//break;
			}
		//}//
		if(containsLetter) return "O";//others, not characters
		
		if(str.toUpperCase().equals(str)) return "AA";
		if(str.toLowerCase().equals(str)) return "aa";
		return "Aa";
	}
	
		
	/**
	 * 
	 * @param token
	 * @param speciesList
	 * @return
	 */
	public boolean isAppear(Token token, List<Token> matchList) {
		String tokenStr = token.getText();
		for(Token term :matchList ){
			String termStr = term.getText();
			String[] termItems = termStr.split("[\\s]+");
			for(String termItem: termItems){
				if(termItem.equals(tokenStr)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static void main(String[] args){
		Properties stanfordCoreProperties = new Properties();
		stanfordCoreProperties.put("annotators", "tokenize, ssplit, parse,lemma");// ,parse, pos, lemma, ner, , dcoref
		StanfordCoreNLP sfCoreNLP = new StanfordCoreNLP(stanfordCoreProperties);
		
		FeatureRender tokenFeatureRender = new FeatureRender(sfCoreNLP);
		List<Token> tokenList = tokenFeatureRender.render("The type strain, JS16-4T  (= KACC 12954T= JCM 15441T), was isolated from soil from Jeju Island, Republic of Korea.");
		for(Token t:tokenList){
			System.out.println(t.toString());
		}
	}
}
