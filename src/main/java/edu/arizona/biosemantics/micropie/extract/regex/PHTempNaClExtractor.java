package edu.arizona.biosemantics.micropie.extract.regex;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.inject.Inject;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.classify.LabelUtil;
import edu.arizona.biosemantics.micropie.model.CharacterGroup;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.NumericCharacterValue;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SubSentence;
import edu.arizona.biosemantics.micropie.model.ValueGroup;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;

/**
 * used to extract the PH, Temperature, NaCl mininum, optimum and maximum values
 * 
 * @author maojin
 *
 */
public class PHTempNaClExtractor extends FigureExtractor {

	public PHTempNaClExtractor(ILabel label, String characterName) {
		super(label, characterName);
	}
	
	@Inject
	public PHTempNaClExtractor(SentenceSpliter sentSplitter,PosTagger posTagger, Label label, String characterName){
		super(label,characterName);
		this.sentSplitter = sentSplitter;
		this.posTagger = posTagger;
	}
	
	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		String text = sentence.getText();
		text = text.replace("degree_celsius_1", "˚C").replace("degree_celsius_7", "˚C");
		//System.out.println(text);
		MultiClassifiedSentence sent = (MultiClassifiedSentence)sentence;
		sent.setSubSentence(null);//reseparate
		this.posSentence(sent);//get sub sentences and their tagged words list
		
		//1, detect sentences
		//2, detect StringSnippet
		//3,identify figures
		List<SubSentence> sents = sent.getSubSentence();
		List<List<TaggedWord>> taggedWordList = sent.getSubSentTaggedWords();
		//
		int sentSize = taggedWordList.size();
		List sentValueList = new LinkedList();
		boolean containWV = false;//whether the sentence contains the unit w/v; 
		for(int sid=0;sid<sentSize;sid++){
			List<TaggedWord> taggedWords = taggedWordList.get(sid);
			//System.out.println(taggedWords);
			if(isWVUnit(taggedWords)){
				containWV = true;
			}
			
			List<NumericCharacterValue> valueList = detectFigures(taggedWords);
			
			mergeFigureRange(valueList,taggedWords);
			//for(int i=0;i<valueList.size();i++){
			//	NumericCharacterValue curFd = valueList.get(i);
				//System.out.println("after merge:"+curFd.getValue()+" "+curFd.getUnit());
			//}
			
			//detect neutral pH
			recNeutralPH(valueList,text,taggedWords);
			
			int fsize = valueList.size();
			Map posCharaMap = new HashMap();//??
			for(int i=0;i<fsize;i++){
				NumericCharacterValue curFd = valueList.get(i);
				
				//4,determine the character of the figures.				
				CharacterGroup characterGroup = detectChracterGroup(curFd,taggedWords,text);
				curFd.setCharacterGroup(characterGroup);
				
				detectSubCharacter(curFd,taggedWords);
				
				posCharaMap.put(curFd.getTermBegIdx(), curFd);
				//determine the value group of the character value: MIN,MAX,OPT,USP
				ValueGroup valueGroup = detectValueGroup(curFd,taggedWords,text,posCharaMap);
				curFd.setValueGroup(valueGroup);
				
				detectModifier(curFd,taggedWords);// detect the modifier for the figure
				
				curFd.setNegation(detectNegation(curFd,taggedWords));
				//if(curFd.isNegation()) System.out.println("negation:"+curFd.isNegation()+" "+curFd.getValueModifier());
				//System.out.println(valueGroup(valueGroup));
				if(valueGroup==ValueGroup.USP){//
					if(curFd.getValue().indexOf("-")>-1){// range
						int rIndex =  curFd.getValue().lastIndexOf("-");
						String minValue = curFd.getValue().substring(0, rIndex);
						String maxValue = curFd.getValue().substring(rIndex+1, curFd.getValue().length());
						curFd.setValue(minValue);
						curFd.setCharacterGroup(characterGroup);
						curFd.setValueGroup(ValueGroup.MIN);
						
						NumericCharacterValue maxFd = new NumericCharacterValue(null,maxValue);
						maxFd.setCharacterGroup(characterGroup);
						maxFd.setValueGroup(ValueGroup.MAX);
						maxFd.setUnit(curFd.getUnit());
						maxFd.setSubCharacter(curFd.getSubCharacter());
						valueList.add(maxFd);
					}else{// really unspecified
						//System.out.println(character(characterGroup)+"_"+valueGroup(valueGroup)+" "+curFd.getValue()+" "+curFd.getUnit());
					}
					
				}else{//
					//System.out.println(character(characterGroup)+"_"+valueGroup(valueGroup)+" "+curFd.getValue()+" "+curFd.getUnit());
				}
			}
			
			//disambiguation according to the logic of value
			disambCharacters(valueList,taggedWords);
			
			fsize =  valueList.size();
			for(int i=0;i<fsize;i++){
				NumericCharacterValue curFd = valueList.get(i);
				//detemine the type
				LabelUtil.determineLabel(curFd);
				/*
				if(curFd.getCharacterGroup()!=null){
					System.out.println(curFd.getCharacter()+" "+curFd.getCharacterGroup()+"_"+curFd.getValueGroup()+" "+curFd.getValueModifier()+" "+curFd.getValue()+" "+curFd.getUnit());
				}else{
					System.err.println(curFd.getCharacter()+" "+curFd.getCharacterGroup()+"_"+curFd.getValueGroup()+" "+curFd.getValueModifier()+" "+curFd.getValue()+" "+curFd.getUnit());
				}
				*/
			}
			
			//combine all the subsentences
			sentValueList.addAll(valueList);
		}
		
		if(containWV) updateNaClUnitWV(sentValueList);
		
		return sentValueList;
	}
	
	/**
	 * update the unit of NaCL characters
	 * @param sentValueList
	 */
	private void updateNaClUnitWV(List<NumericCharacterValue> sentValueList) {
		//System.out.println("update w/v");
		for(NumericCharacterValue ncv:sentValueList){
			//System.out.println(ncv.getCharacterGroup());
			if(CharacterGroup.NACL.equals(ncv.getCharacterGroup())){
				String unit = ncv.getUnit();
				if(unit==null) unit="w/v";
				else unit+="w/v";
				ncv.setUnit(unit);
			}
			
		}
		
	}
	
	/**
	 * whether this list is W/V
	 * [w/v/NN]
	 * @param taggedWords
	 * @return
	 */
	private boolean isWVUnit(List<TaggedWord> taggedWords) {
		if(taggedWords.toString().indexOf("w/v")>-1) return true;
		else return false;
	}

	/**
	 * replace neutral pH --> 7
	 * @param featureList
	 * @param taggedWords 
	 * @param snippet
	 */
	public void recNeutralPH(List<NumericCharacterValue> valueList, String content, List<TaggedWord> taggedWords) {
		 /*String neuPHRegEx="neutral[\\s]*pH"; //pH 7–7.5 
         Matcher rangeToMatcher = Pattern.compile(neuPHRegEx).matcher(content);  
         while(rangeToMatcher.find()){
        	 NumericCharacterValue neuFd = new NumericCharacterValue();
        	 neuFd.setCharacterGroup(Character.PH);
        	 //neuFd.setValueGroup(ValueGroup.USP);
        	 neuFd.setValue("7");
        	 neuFd.setUnit("");
        	 featureList.add(neuFd);
         }
         */
		
        int size = taggedWords.size(); 
 		for(int t=0;t<size-1;t++){
 			TaggedWord word = taggedWords.get(t);
 			String wordStr = word.word();
 			String nextwordStr = taggedWords.get(t+1).word();
 			if(wordStr.equalsIgnoreCase("neutral")&&nextwordStr.equalsIgnoreCase("pH")) {
 				 NumericCharacterValue neuFd = CharacterValueFactory.createNumericValue(null, "7", "");
 	        	 neuFd.setCharacterGroup(CharacterGroup.PH);
 	        	 neuFd.setTermBegIdx(t);
 	        	 neuFd.setTermEndIdx(t+1);
 	        	 valueList.add(neuFd);
 			}
 		}
	}
	
	/**
	 * 
	 * @param curFd
	 * @param taggedWords
	 * @return
	 */
	private CharacterGroup detectChracterGroup(CharacterValue curFd,List<TaggedWord> taggedWords, String paraContent) {
		//detect by unit
		if(curFd.getUnit()!=null&&(curFd.getUnit().equals("˚C")||curFd.getUnit().equals("˚"))){
			return CharacterGroup.TEMP;
		}else if(curFd.getUnit()!=null&&curFd.getUnit().indexOf("%")>-1){
			return CharacterGroup.NACL;
		}else if(curFd.getUnit()!=null&&curFd.getUnit().equals("M")){
			return CharacterGroup.NACL;
		}else  if(curFd.getUnit()!=null&&curFd.getUnit().equals("g")){
			return CharacterGroup.NACL;
		}else  if(curFd.getUnit()!=null&&curFd.getUnit().equals("mM")){
			return CharacterGroup.NACL;
		}else  if(curFd.getUnit()!=null&&curFd.getUnit().equals("‰")){
			return CharacterGroup.NACL;
		}
		
		//detect by context terms
		//PH
		int termIndex = curFd.getTermBegIdx();
		int termEndIndex = curFd.getTermEndIdx();
		int size = taggedWords.size();
		
		if(termIndex>1){
			TaggedWord formerWord = taggedWords.get(termIndex-1);
			if(formerWord.word().equalsIgnoreCase("ph")){return CharacterGroup.PH;}
			for(int t=termIndex-1;t>=0;t--){
				TaggedWord word = taggedWords.get(t);
				if(word.tag().equals("CD")) break;//scan until the former number.
				if(word.word().equalsIgnoreCase("ph")) {return CharacterGroup.PH;}
			}
		}
		
		//temperature
		/*
		for(int t=termIndex-1;t>=0;t--){
			TaggedWord word = taggedWords.get(t);
			if(word.tag().equals("CD")) break;//scan until the former number.
			if(word.word().startsWith("temperat")) { 
				//System.out.println("Character.TEMP identified "); 
				return Character.TEMP;
			}
		}
		*/
		//NACL		
		for(int t=termEndIndex+1;t<size;t++){
			TaggedWord word = taggedWords.get(t);
			String wordStr = word.word();
			//System.out.println("wordStr="+wordStr);
			if(word.tag().equals("CD")) break;//scan until the former number.
			//Na+,MgCl2,Cl-,Mg2+
			if(wordStr.equalsIgnoreCase("nacl")||wordStr.equalsIgnoreCase("na")||wordStr.equalsIgnoreCase("MgCl2")||wordStr.equalsIgnoreCase("cl")
					||wordStr.equalsIgnoreCase("mg2")||wordStr.equalsIgnoreCase("salinity")) {
				//curFd.setSubCharacter(wordStr);
				return CharacterGroup.NACL;
			}
		}
		for(int t=termIndex-1;t>=0;t--){
			TaggedWord word = taggedWords.get(t);
			String wordStr = word.word();
			//System.out.println("wordStr="+wordStr);
			if(word.tag().equals("CD")) break;//scan until the former number.
			//Na+,MgCl2,Cl-,Mg2+
			if(wordStr.equalsIgnoreCase("nacl")||wordStr.equalsIgnoreCase("na")||wordStr.equalsIgnoreCase("MgCl2")||wordStr.equalsIgnoreCase("cl")
					||wordStr.equalsIgnoreCase("mg2")||wordStr.equalsIgnoreCase("salinity")) {
				//System.out.println("Character.NACL identified ");
				return CharacterGroup.NACL;
			}
		}
		
		//System.out.println("character group not identified ");
		
		return detectChracterGroupBySent(paraContent);
	}
	
	
	/**
	 * determine the charachter group according to the whole sentence
	 * @param content
	 * @return
	 */
	public CharacterGroup detectChracterGroupBySent(String content){
		PTBTokenizer<Word> ptb = PTBTokenizer.newPTBTokenizer(new StringReader(content));
		List<Word> words = ptb.tokenize();
		int character[] = new int[3];
		//int charNum = 0;
		for(int t=0;t<words.size();t++){
			Word word = words.get(t);
			String wordStr = word.word().toLowerCase();
			if(wordStr.startsWith("temperat")) { 
				character[0]++;// =  Character.TEMP;
				//charNum++;
			}else if(wordStr.startsWith("ph")) { 
				character[1]++;// =   Character.PH;
				//charNum++;
			}else if(wordStr.equalsIgnoreCase("nacl")||wordStr.equalsIgnoreCase("na")||wordStr.equalsIgnoreCase("MgCl2")||wordStr.equalsIgnoreCase("cl")
					||wordStr.equalsIgnoreCase("mg2")||wordStr.equalsIgnoreCase("salinity")) { 
				character[2]++;//  Character.NACL;
				//charNum++;
			}
		}
		
		int largest = 0;
		CharacterGroup chara = null;
		/*if(character[0]>largest){
			largest = character[0];
			 chara = Character.TEMP;
		}*/
		if(character[1]>largest){
			largest = character[1];
			chara = CharacterGroup.PH;
		}
		if(character[2]>largest){
			largest = character[2];
			chara = CharacterGroup.NACL;
		}
		
		
		return chara;
	}
	
	/**
	 * detect the substances
	 * @param curFd
	 * @param taggedWords
	 */
	public void detectSubCharacter(NumericCharacterValue curFd, List<TaggedWord> taggedWords) {
		int termEndIndex = curFd.getTermEndIdx();
		int size = taggedWords.size();
		for(int t=termEndIndex+1;t<size&&t<termEndIndex+6;t++){
			TaggedWord word = taggedWords.get(t);
			String wordStr = word.word();
			if(word.tag().equals("CD")) break;//scan until the former number.
			//Na+,MgCl2,Cl-,Mg2+
			if(wordStr.equalsIgnoreCase("nacl")||wordStr.equalsIgnoreCase("na")||wordStr.startsWith("MgC")||wordStr.equalsIgnoreCase("cl")
					||wordStr.equalsIgnoreCase("mg2")||wordStr.equalsIgnoreCase("salinity")||wordStr.equalsIgnoreCase("sea")
					||wordStr.equalsIgnoreCase("artificial")||wordStr.equalsIgnoreCase("MgSO4")||wordStr.equalsIgnoreCase("Ca2")||wordStr.equalsIgnoreCase("magnesium")) {
				if(wordStr.equalsIgnoreCase("na")) wordStr = "Na+";
				if(wordStr.equalsIgnoreCase("sea")) wordStr = "sea salt";
				if(wordStr.equalsIgnoreCase("artificial")) wordStr = "artificial seawater";
				if(wordStr.equalsIgnoreCase("Mg2")) wordStr = "Mg2+";
				if(wordStr.equalsIgnoreCase("Ca2")) wordStr = "Ca2+";
				curFd.setSubCharacter(wordStr);
				break;
			}
		}
	}

	/**
	 * 
	 * @param curFd
	 * @param taggedWords
	 * @param content
	 * @param posCharaMap
	 * @return
	 */
	private ValueGroup detectValueGroup(NumericCharacterValue curFd,List<TaggedWord> taggedWords, String content, Map<Integer,NumericCharacterValue> posCharaMap) {
		int termIndex = curFd.getTermBegIdx();
		int termEndIndex = curFd.getTermEndIdx();
		int size = taggedWords.size();
		
		/*
		for(int t=termIndex+1;t<size;t++){
			TaggedWord word = taggedWords.get(t);
			String wordStr = word.word();
			if(wordStr.indexOf("optim")>-1){
				return ValueGroup.OPT;
			}else if(wordStr.indexOf("maxi")>-1){
				return ValueGroup.MAX;
			}else if(wordStr.indexOf("mini")>-1){
				return ValueGroup.MIN;
			}
		}*/
		
		for(int t=termIndex-1;t>=0;t--){
			TaggedWord word = taggedWords.get(t);
			String wordStr = word.word().toLowerCase();
			if(posCharaMap.get(t)!=null&&posCharaMap.get(t).getCharacterGroup()==curFd.getCharacterGroup()) break;
			
			
			if(t-1>0&&wordStr.equals("to")&&taggedWords.get(t-1).word().equals("up")){//up to 
				return ValueGroup.MAX;
			}else if(wordStr.indexOf("opt")>-1||wordStr.indexOf("best")>-1||wordStr.indexOf("fast")>-1||wordStr.indexOf("most")>-1||content.indexOf("maximum rate")>-1||content.indexOf("maximum growth")>-1){
				return ValueGroup.OPT;
			}else if(wordStr.indexOf("maxi")>-1){
				return ValueGroup.MAX;
			}else if(wordStr.indexOf("mini")>-1){
				return ValueGroup.MIN;
			}
		}
		
		//it may lead to some errors
		/*
		content = content.toLowerCase();
		if(content.indexOf("optim")>-1){
			return ValueGroup.OPT;
		}else if(content.indexOf("maxi")>-1){
			return ValueGroup.MAX;
		}else if(content.indexOf("mini")>-1){
			return ValueGroup.MIN;
		}
		*/
		return ValueGroup.USP;
	}
	
	/**
	 * multiple optimal values
	 * @param featureList
	 */
	public void disambCharacters(List<NumericCharacterValue> featureList,List taggedWords) {
		//List[] optListArr = new ArrayList[3];// PH TEMP NACL
		Map optCharacters = new HashMap();
		for(NumericCharacterValue fd:featureList){
			if(fd.getValueGroup() == ValueGroup.OPT){
				List optList = (List) optCharacters.get(fd.getCharacterGroup()+fd.getSubCharacter());
				if(optList==null){
					optList = new ArrayList();
					optList.add(fd);
					optCharacters.put(fd.getCharacterGroup()+fd.getSubCharacter(), optList);
				}else{
					optList.add(fd);
				}
			}
		}
		
		//
		Iterator<Entry> entIter = optCharacters.entrySet().iterator();
		while(entIter.hasNext()){
			Entry<Integer, List> keyEntry = entIter.next();
			List<NumericCharacterValue> optList = keyEntry.getValue();
			if(optList.size()>1) {//multiple optimal value
				//System.out.println(taggedWords);
				
				//if contains not 
				//if(optList.get(0).isNegation()&&!optList.get(0).isNegation()) continue;
				NumericCharacterValue firstOpt = optList.get(0);
				NumericCharacterValue secondOpt = optList.get(1);
				
				int firstOptIndex = optIndex(firstOpt,taggedWords);
				int secondOptIndex = optIndex(secondOpt,taggedWords);
				
//				System.out.println(firstOpt.toString()+" "+firstOptIndex);
//				System.out.println(secondOpt.toString()+" "+secondOptIndex);
//				System.out.println(isOptRange(firstOpt,secondOpt));
				//whether contains or between the value
				
				if(firstOptIndex<secondOptIndex&&isOptRange(firstOpt,secondOpt)){//the first one is optimal, others are assigned as 'USP' or max-min
					optList.remove(firstOpt);//
					reassign(featureList,optList);
				}else{
					optList.remove(secondOpt);//the second one is optimal, others are assigned as 'USP' or max-min
					reassign(featureList,optList);
				}
				
//				//optList
//				//boolean isDis = false;
//				//isDis = valueDis(featureList, optList);
//				int fsize =  featureList.size();
//				for(int i=0;i<fsize;i++){
//					NumericCharacterValue curFd = featureList.get(i);
//					System.out.println(curFd.toString());
//				}
			}
		}
	}
	
	/**
	 * if the NumericCharacterValue is not a range value, change it to  'USP'
	 * or to max-min
	 * @param featureList
	 * @param optList
	 */
	public void reassign(List<NumericCharacterValue> featureList, List<NumericCharacterValue> reassList) {
		for(int i=0;i<reassList.size();i++){
			NumericCharacterValue curFd = reassList.get(i);
			if(curFd.getValue().indexOf("-")<=-1){
				curFd.setValueGroup(ValueGroup.USP);
			}else{
				int rIndex =  curFd.getValue().lastIndexOf("-");
				String minValue = curFd.getValue().substring(0, rIndex);
				String maxValue = curFd.getValue().substring(rIndex+1, curFd.getValue().length());
				curFd.setValue(minValue);
				curFd.setValueGroup(ValueGroup.MIN);
				
				NumericCharacterValue maxFd = new NumericCharacterValue(null,maxValue,curFd.getUnit());
				maxFd.setCharacterGroup(curFd.getCharacterGroup());
				maxFd.setValueGroup(ValueGroup.MAX);
				maxFd.setValue(maxValue);
				maxFd.setNegation(curFd.getNegation());
				maxFd.setValueModifier(curFd.getValueModifier());
				maxFd.setSubCharacter(curFd.getSubCharacter());;
				featureList.add(maxFd);
			}
			
		}
	}
	
}