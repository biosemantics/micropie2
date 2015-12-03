package edu.arizona.biosemantics.micropie.extract.regex;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Inject;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.NumericCharacterValue;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SubSentence;
import edu.arizona.biosemantics.micropie.model.ValueGroup;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.stanford.nlp.ling.TaggedWord;

/**
 * extract the figures
 * @author maojin
 *
 */
public class FigureExtractor  extends AbstractCharacterValueExtractor{
	protected SentenceSpliter sentSplitter;
	protected PosTagger posTagger;
	
	@Inject
	public FigureExtractor(SentenceSpliter sentSplitter,PosTagger posTagger,ILabel label, String characterName){
		super(label,characterName);
		this.sentSplitter = sentSplitter;
		this.posTagger = posTagger;
	}

	public FigureExtractor(ILabel label, String characterName) {
		super(label,characterName);
	}

	
	/**
	 * 1, separate subsetences
	 * 2, postag each subsentence
	 * 
	 * @param sentence
	 */
	public void posSentence(MultiClassifiedSentence sentence){
		//1, detect sentences
		List<SubSentence> subSentences = sentSplitter.detectSnippet(sentence);
		sentence.setSubSentence(subSentences);
		
		//2, postag each subsentence
		List taggerwordsList = new LinkedList();
		sentence.setSubSentTaggedWords(taggerwordsList);
		for(SubSentence subsent:subSentences){
			String content = subsent.getContent();
			List<TaggedWord> taggedWords  = posTagger.tagString(content);
			taggerwordsList.add(taggedWords);
		}
	}
	
	/**
	 * 1, separate subsetences
	 * 2, postag each subsentence
	 * 
	 * @param sentence
	 */
	public void posSentenceNoSub(MultiClassifiedSentence sentence){
		//2, postag each subsentence
		List taggerwordsList = sentence.getSubSentTaggedWords();
		if(taggerwordsList==null){
			taggerwordsList = new LinkedList();
			sentence.setSubSentTaggedWords(taggerwordsList);
			List<TaggedWord> taggedWords  = posTagger.tagString(sentence.getText());
			taggerwordsList.add(taggedWords);
		}
	}
	
	
	
	/**
	 * detect single figure and figure ranges
	 * 
	 * @param taggedWords
	 * @return
	 */
	public List<NumericCharacterValue> detectFigures(List<TaggedWord> taggedWords) {
		
		List<NumericCharacterValue> features = new ArrayList();
		for(int i = 0;i<taggedWords.size();){
			int termId= 0;
			TaggedWord word = (TaggedWord) taggedWords.get(i);
			String figure = null;
			
			/**
			 * 1, if it's CD, it must be a figure
			 * 2, if it's JJ, it contains figure , it maybe a figure
			 * 3, if it's only figure words, it is a figure
			 */
			if(word.tag().equals("CD")||(word.tag().equals("JJ")&&containNumber(word.word()))||(defIsNumber(word.word()))){
				//if(word.tag().equals("CD")||defNumber(word.word())){
				termId = i;
				NumericCharacterValue fd = new NumericCharacterValue(this.getLabel());
				String unit = "";
				
				figure = word.word();
				if(!containNumber(figure)){i++;continue;}
				//System.out.println("it is a figure:"+figure+" "+unit);
				//if(i+1<taggedWords.size()&&(taggedWords.get(i+1).tag().equals("CD")&&(containNumber(taggedWords.get(i+1).word())||"<".equalsIgnoreCase(taggedWords.get(i+1).word()))||defIsNumber(taggedWords.get(i+1).word()))){
				while(i+1<taggedWords.size()&&(taggedWords.get(i+1).tag().equals("CD")&&(containNumber(taggedWords.get(i+1).word())||containNumSign(taggedWords.get(i+1).word()))||defIsNumber(taggedWords.get(i+1).word()))){
					figure+=taggedWords.get(i+1).word();
					i++;
				}
				//System.out.println("it is a figure:"+figure+" "+unit);
				if(i+1<taggedWords.size()){
					if((taggedWords.get(i+1).word().equals("°")&&taggedWords.get(i+2).word().equals("C"))
							||taggedWords.get(i+1).word().equals("degree_celsius_1")
							||taggedWords.get(i+1).word().equals("degree_celsius_7")){
						unit = "˚C";
					}else if(taggedWords.get(i+1).word().equalsIgnoreCase("˚C")){
						unit = "˚C";
					}else if(taggedWords.get(i+1).word().equalsIgnoreCase("˚")){
						unit = "˚C";
					}else if(taggedWords.get(i+1).word().equalsIgnoreCase("%")){
						unit = "%";
					}else if(taggedWords.get(i+1).word().equalsIgnoreCase("m")||taggedWords.get(i+1).word().equalsIgnoreCase("m.")){
						unit = "M";
					}else if(taggedWords.get(i+1).word().equalsIgnoreCase("g")){
						unit = "g";
					}else if(taggedWords.get(i+1).word().equalsIgnoreCase("mm")){
						unit = "mM";
					}else if(taggedWords.get(i+1).word().equalsIgnoreCase("‰")){
						unit = "‰";
					}else if(taggedWords.get(i+1).word().equalsIgnoreCase("mol")&&taggedWords.get(i+2).word().equalsIgnoreCase("%")){
						unit = "mol%";
					}
				}
				/*
				TaggedWord unitWord =taggedWords.get(i+1);
				if(unitWord.tag().startsWith("NN")){//&&(unitWord.word().equals("˚C"))
					unit = unitWord.word();
				}else if(unitWord.tag().startsWith("FW")){//foreign word 外来词
					
				}*/
				if(!defIsNumber(figure)){
					System.err.println("it is not a figure:"+figure+" "+unit);
				}else{
					fd.setValue(figure);
					fd.setTermBegIdx(termId);
					fd.setTermEndIdx(i);
					fd.setUnit(unit);
					features.add(fd);
				}
				
			}
			
			i++;
		}//all words traversed
		return features;
	}
	
	
	
	/**
	 * detect the modifier for the figure
	 * the modifiers are: about, around, near, above, below, 
	 * @param curFd
	 * @param taggedWords
	 */
	public void detectModifier(NumericCharacterValue curFd, List<TaggedWord> taggedWords) {
		int termIndex = curFd.getTermBegIdx();
		for(int t=termIndex-1;t>=termIndex-3&&t>0;t--){
			TaggedWord word = taggedWords.get(t);
			String wordStr = word.word();
			//System.out.println(wordStr);
			if(word.tag().equals("CD")) break;//scan until the former number.
			if(wordStr.equalsIgnoreCase("about")||
					wordStr.equalsIgnoreCase("around")||
					wordStr.equalsIgnoreCase("near")||
					wordStr.equalsIgnoreCase("above")||
					wordStr.equalsIgnoreCase("below")||
					wordStr.equalsIgnoreCase(">")||
					wordStr.equalsIgnoreCase("<")
					//approximately 
					) {
				curFd.setValueModifier(wordStr);
				break;
			}else if(wordStr.equalsIgnoreCase("to")&&taggedWords.get(t-1).word().equalsIgnoreCase("up")){
				curFd.setValueModifier("up to");
				break;
			}
		}
	}



	/**
	 * the first one is or not between the second opt
	 * @param firstOpt
	 * @param secondOpt
	 * @return
	 */
	public boolean isOptRange(NumericCharacterValue firstOpt, NumericCharacterValue secondOpt) {
		
		double minValue = Double.MIN_VALUE; 
		double maxValue = Double.MAX_VALUE;
		
		int rIndex =  secondOpt.getValue().lastIndexOf("-");
		int frIndex =  firstOpt.getValue().lastIndexOf("-");
		String firstValueStr = firstOpt.getValue();
		String secondValueStr = secondOpt.getValue();
		if(rIndex>-1){//second range
			minValue = new Double(secondValueStr.substring(0, rIndex));
			maxValue = new Double(secondValueStr.substring(rIndex+1, secondValueStr.length()));
			if(frIndex>-1){//first range 
				double fminValue = new Double(firstValueStr.substring(0, frIndex));
				double fmaxValue = new Double(firstValueStr.substring(frIndex+1, firstValueStr.length()));
				if(minValue<fminValue&&maxValue>fmaxValue){
					return true;
				}else{
					return false;
				}
			}else{//first single
				//System.out.println(minValue+"-"+maxValue);
				double firstValue = new Double(firstValueStr);
				if(firstValue<=maxValue&&firstValue>=minValue){
					return true;
				}else{
					return false;
				}
			}
		}else{//second single
			double secondValue = new Double(secondValueStr);
			if(frIndex>-1){//first range
				double fminValue = new Double(firstValueStr.substring(0, frIndex));
				double fmaxValue = new Double(firstValueStr.substring(frIndex+1, firstValueStr.length()));
				
				if(fmaxValue<=secondValue){//is range
					return true;
				} else{
					return false;
				}
			}else{//fist single
				//System.out.println(minValue+"-"+maxValue);
				double firstValue = new Double(firstValueStr);
				if(firstValue<=secondValue){
					return true;
				}else{
					return false;
				}
			}
		}
	}


	/**
	 * if the FeatureDesc is not a range value, change it to  'USP'
	 * or to max-min
	 * @param featureList
	 * @param optList
	 */
	public void reassignMinMax(List<NumericCharacterValue> featureList, List<NumericCharacterValue> reassList) {
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
				
				NumericCharacterValue maxFd = new NumericCharacterValue(curFd.getCharacter());
				maxFd.setCharacterGroup(curFd.getCharacterGroup());
				maxFd.setValueGroup(ValueGroup.MAX);
				maxFd.setValue(maxValue);
				maxFd.setUnit(curFd.getUnit());
				maxFd.setNegation(curFd.getNegation());
				maxFd.setValueModifier(curFd.getValueModifier());
				maxFd.setSubCharacter(curFd.getSubCharacter());
				
				featureList.add(maxFd);
			}
			
		}
	}


	/**
	 * get tne term index of the optimum value
	 * @param fd
	 * @param taggedWords
	 * @return
	 */
	public int optIndex(NumericCharacterValue fd, List<TaggedWord> taggedWords) {
		int termIndex = fd.getTermBegIdx();
		int index = Integer.MAX_VALUE;
		for(int t=termIndex-1;t>=0;t--){
			TaggedWord word = taggedWords.get(t);
			String wordStr = word.word().toLowerCase();
			//if(word.tag().equals("CD")) break;//scan until the former number.
			if(wordStr.indexOf("opt")>-1||wordStr.indexOf("best")>-1||wordStr.indexOf("fast")>-1||wordStr.indexOf("most")>-1){
				index = termIndex - t;
			}
		}
		return index;
	}


	/**
	 * not/RB 
	 * @param curFd
	 * @param taggedWords
	 */
	public String detectNegation(NumericCharacterValue curFd, List<TaggedWord> taggedWords) {
		//[neither/DT, growth/NN, nor/CC, methane/NN, production/NN, is/VBZ, observed/VBN, at/IN, 48/CD, ˚C/NN, ./.]
		//no/DT
		int termIndex = curFd.getTermBegIdx();
		for(int t=termIndex-1;t>=0;t--){
			TaggedWord word = taggedWords.get(t);
			String wordStr = word.word();
			//System.out.println(word.word()+" "+word.tag());
			if(word.tag().equals("CD")) break;//scan until the former number.
			if((wordStr.equalsIgnoreCase("no")||(wordStr.equalsIgnoreCase("neither"))&&word.tag().equalsIgnoreCase("dt"))) {
				if("".equals(curFd.getValueModifier())){
					curFd.setValueModifier(wordStr); 
				}else{
					curFd.setValueModifier(wordStr+" "+curFd.getValueModifier());
				}
				return wordStr;
			}
			if(wordStr.equalsIgnoreCase("not")&&word.tag().equalsIgnoreCase("rb")) {
				if("".equals(curFd.getValueModifier())){
					curFd.setValueModifier(wordStr); 
				}else{
					curFd.setValueModifier(wordStr+" "+curFd.getValueModifier());
				}
				return wordStr;
			}
		}
		
		return null;
	}


	/**
	 * determine the value group:
	 * 	MIN = 1
	 *  MAX = 2
	 *  OPT = 3
	 *  USP = 4
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
			
			
			if(wordStr.equals("to")&&taggedWords.get(t-1).word().equals("up")){//up to 
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
	 * merge the range value
	 * @param featureList
	 */
	public void mergeFigureRange(List featureList,List<TaggedWord> taggedWords) {

		int tagLength = taggedWords.size();
		for(int i=0;i<featureList.size()-1;){
			NumericCharacterValue curFd = (NumericCharacterValue) featureList.get(i);
			NumericCharacterValue nextFd = (NumericCharacterValue) featureList.get(i+1);
			int curEnd = curFd.getTermEndIdx();
			int nextBegin = nextFd.getTermBegIdx();
			int nextEnd =  nextFd.getTermEndIdx();
			
			//merge +/CC, //:, -1/CD
			if(nextBegin-curEnd<=3){
				int j = curEnd+1;
				//System.out.println(taggedWords.get(j).word()+" "+taggedWords.get(j+1).tag());
				if(taggedWords.get(j).word().equals("+")&&taggedWords.get(j+1).tag().equals(":")){
					String value = curFd.getValue()+" +/"+nextFd.getValue();
					curFd.setValue(value);
					curFd.setTermEndIdx(nextFd.getTermEndIdx());
					if("".equals(curFd.getUnit())){
						curFd.setUnit(nextFd.getUnit());
					}
					featureList.remove(nextFd);
				}
			}
			
			///////// merge range
			if(curFd.getValue().indexOf("-")>-1){
				i++;
				continue;//not a range value;
			}
			
			if(nextFd.getValue().indexOf("-")>-1){
				i++;i++;
				continue;//not a range value;
			}
			
			
			//System.out.println(curEnd+" "+nextBegin);
			boolean shouldMerge = false;
			if(nextBegin-curEnd<=3){//the distance should not be greater than 2
				//如果中间包含to,and之类的就合并
				for(int j = curEnd+1;j<nextBegin;j++){
					if(taggedWords.get(j).word().equals("to")||taggedWords.get(j).word().equals("-")||taggedWords.get(j).tag().equals(":")){
						shouldMerge = true;
					}
					//System.out.println(curFd.getValue()+" "+curFd.getUnit()+"-"+nextFd.getValue()+" "+nextFd.getUnit());
					try{
						new Double(curFd.getValue()); 
						new Double(nextFd.getValue());
					}catch(Exception e){
						//System.out.println(curFd+" is not a figure");
						continue;
					}
					if(taggedWords.get(j).word().equals("and")&&( new Double(curFd.getValue())<new Double(nextFd.getValue()))){// the former and the latter should be the same type
						//System.out.println(curFd.getValue()+" "+curFd.getUnit()+"-"+nextFd.getValue()+" "+nextFd.getUnit());
						shouldMerge = true;
					}
				}
				
				
				//(40.2 and 40.3%, respectively)
				for(int k=nextEnd+1;k<=nextEnd+4&&k<tagLength;k++){
					if(taggedWords.get(k).word().equals("respectively")){
						shouldMerge = false;
						if("".equals(curFd.getUnit())){
							curFd.setUnit(nextFd.getUnit());
						}
					}
				}
				
			}
			
			if(shouldMerge){//merge them into one
				String value = curFd.getValue()+"-"+(nextFd.getValueModifier()==null?"":nextFd.getValueModifier())+nextFd.getValue();
				curFd.setValue(value);
				curFd.setTermEndIdx(nextFd.getTermEndIdx());
				if("".equals(curFd.getUnit())){
					curFd.setUnit(nextFd.getUnit());
				}
				featureList.remove(nextFd);
				featureList.set(i, curFd);
			}
			
			//System.out.println("merged:"+curFd);
			i++;
			/*
			for(int j=0;j<featureList.size()-1;){
				System.out.println("merged in:"+featureList.get(j));
				j++;
			}*/
		}//deal all the values
		
		/*for(int i=0;i<featureList.size()-1;){
			System.out.println("merged in:"+featureList.get(i));
			i++;
		}*/
	}

	/**
	 * definitely is figure
	 * @param word
	 * @return
	 */
	public boolean defIsNumber(String word) {
		if(word.length()==1){
			return word.matches("[0-9]+");
		}else{
			Matcher m = Pattern.compile("[+-.0-9±]+").matcher(word);
			if (m.matches()){
				return true;
			}
		}
		return false;
	}

	/**
	 * only contain a number
	 * @param word
	 * @return
	 */
	public boolean containNumber(String word) {
		//System.out.println(word.matches("[+-]?[1-9]+[0-9]*(\\.[0-9]+)?[-]?[1-9]+[0-9]*(\\.[0-9]+)?"));
		if(word.length()==1){
			return word.matches("[0-9]+");
		}else{
			Matcher m = Pattern.compile(".*\\d+.*").matcher(word);// Pattern.compile(".*\\d+.*").matcher(word);
			if (m.matches()){
				return true;
			}
			//return word.matches("[+-.0-9]+"); 
		}
		
		return false;
	}

	
	/**
	 * only contain a number
	 * @param word
	 * @return
	 */
	public boolean containNumSign(String word) {
		return word.matches("[+-±<>]+");
	}
	
	
	@Override
	public List<CharacterValue> getCharacterValue(
			Sentence text) {
		// TODO Auto-generated method stub
		return null;
	}

}