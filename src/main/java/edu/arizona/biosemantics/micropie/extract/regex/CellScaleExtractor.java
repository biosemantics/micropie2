package edu.arizona.biosemantics.micropie.extract.regex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.LabelUtil;
import edu.arizona.biosemantics.micropie.model.CharacterGroup;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.NumericCharacterValue;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.ValueGroup;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.stanford.nlp.ling.TaggedWord;


/**
 * extract the cell size, cell width, cell length
 * @author maojin
 *
 */
public class CellScaleExtractor extends FigureExtractor{

	public CellScaleExtractor(SentenceSpliter sentSplitter,
			PosTagger posTagger, ILabel label, String characterName) {
		super(sentSplitter, posTagger, label, characterName);
	}
	
	
	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		String text = sentence.getText();
		text = text.replace("µ.m", "µm");
		text = text.replace("_m", "µm");
		sentence.setText(text);
		MultiClassifiedSentence sent = (MultiClassifiedSentence)sentence; 
		this.posSentenceNoSub(sent);//no sub sentences
		List<TaggedWord> taggedWords = (List<TaggedWord>) sent.getSubSentTaggedWords().get(0);
		
		//System.out.println(text);//+":\n"+taggedWords
		//detect all the figures
		List valueList = detectFigures(taggedWords);
		//System.out.println("figures = "+valueList);
		//merge the figure ranges
		mergeFigureRange(valueList,taggedWords);
		//System.out.println("after merged figures = "+valueList);
		/**
		 * 0.5-3.0 x 0.4-0.5 µm===>0.5-3.0x0.4-0.5
		 * 0.4 by 2 to 20 µm===>0.4x2-20
		 */
		mergeMultiplication(valueList,taggedWords);
		//System.out.println("after mergeMultiplication figures = "+valueList);
		
		for(int i=0;i<valueList.size();i++){
			NumericCharacterValue curFd = (NumericCharacterValue) valueList.get(i);
			
			//4,determine the character of the figures.				
			CharacterGroup characterGroup = detectChracterGroup(curFd,taggedWords,text);
			curFd.setCharacterGroup(characterGroup);
			
			//detectModifier(curFd,taggedWords);// detect the modifier for the figure
			
			//filter
			if(characterGroup == null&&(curFd.getUnit()==null||"".equals(curFd.getUnit().trim()))||!containNumber(curFd.getValue())){
				//System.err.println(curFd.toString());
				valueList.remove(curFd);
				i--;
			}else{
				if(characterGroup == null){
					curFd.setCharacterGroup(CharacterGroup.CSIZE);
				}
			}
			
			
			LabelUtil.determineLabel(curFd);
		}
		//System.out.println(valueList);
		separateCellSize(valueList);
		//remove some error files
		filter(valueList);
		return valueList;
	}
	
	
	/**
	 * separate cell size
	 * @param valueList
	 */
	public void separateCellSize(List<NumericCharacterValue> valueList) {
		try{
			for(int i=0;i<valueList.size();){
				NumericCharacterValue cv = valueList.get(i);
				if(CharacterGroup.CSIZE.equals(cv.getCharacterGroup())){
					String[] fields = cv.getValue().split("x|×");
					if(fields.length<2){i++;continue;}
					//System.out.println(cv);
					double firstMax = detectMaxValue(fields[0]);
					double secondMax = detectMaxValue(fields[1]);
					//System.out.println(fields[0]+" and "+fields[1]);
					if(firstMax>secondMax){//the first one should be the cell length
						//System.out.println("the first is cell length");
						NumericCharacterValue cellLength = new NumericCharacterValue(null);
						cellLength.setCharacterGroup(CharacterGroup.CLENGTH);
						cellLength.setValue(fields[0]);
						cellLength.setUnit(cv.getUnit());
						
						LabelUtil.determineLabel(cellLength);
						valueList.add(cellLength);
						
						NumericCharacterValue cellWidth = new NumericCharacterValue(null);
						cellWidth.setCharacterGroup(CharacterGroup.CWIDTH);
						cellWidth.setValue(fields[1]);
						cellWidth.setUnit(cv.getUnit());
						
						LabelUtil.determineLabel(cellWidth);
						valueList.add(cellWidth);
					}else{//the second one should be the cell length
						//System.out.println("the second is cell length");
						NumericCharacterValue cellLength = new NumericCharacterValue(null);
						cellLength.setCharacterGroup(CharacterGroup.CLENGTH);
						cellLength.setValue(fields[1]);
						cellLength.setUnit(cv.getUnit());
						
						LabelUtil.determineLabel(cellLength);
						valueList.add(cellLength);
						
						NumericCharacterValue cellWidth = new NumericCharacterValue(null);
						cellWidth.setCharacterGroup(CharacterGroup.CWIDTH);
						cellWidth.setValue(fields[0]);
						cellWidth.setUnit(cv.getUnit());
						
						LabelUtil.determineLabel(cellWidth);
						valueList.add(cellWidth);
					}
					
					valueList.remove(i);
				}else{
					i++;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	/**
	 * detect the max value, it should be the last figure
	 * 
	 * @param string
	 * @return
	 */
	public double detectMaxValue(String string) {
		int index = string.indexOf("-");
		double max = 0;
		if(index>-1){//it should be the second one
			String[] fields =  string.split("-");
			max = new Double(fields[1]);
		}else{
			max = new Double(string);
		}
		return max;
	}


	/**
	 * filter some wrong values
	 * @param valueList
	 */
	private void filter(List<NumericCharacterValue> valueList) {
		for(int i=0;i<valueList.size();){
			NumericCharacterValue cv = valueList.get(i);
			if(!cv.getValue().toLowerCase().equals(cv.getValue().toUpperCase())){
				valueList.remove(i);
			}else{
				i++;
			}
		}
	}


	/**
	 * determine what the character is
	 * @param curFd
	 * @param taggedWords
	 * @param text
	 * @return
	 */
	private CharacterGroup detectChracterGroup(NumericCharacterValue curFd,
			List<TaggedWord> taggedWords, String text) {
		
		if(curFd.getValue().indexOf("x")>-1) return CharacterGroup.CSIZE;
		
		int termBegIndex = curFd.getTermBegIdx();
		int termEndIndex = curFd.getTermEndIdx();
		int size = taggedWords.size();
		
		//go backwards to find clues
		for(int t=termEndIndex+1;t<size&&t<=termEndIndex+3;t++){//window is 3
			String word = taggedWords.get(t).word();
			//System.out.println(word);
			if(word.startsWith("diamet")) {return CharacterGroup.CDIAM;}
			else if(word.startsWith("length")||word.startsWith("long")||word.startsWith("short")) {return CharacterGroup.CLENGTH;}
			else if(word.startsWith("wid")) {return CharacterGroup.CWIDTH;}
			else if(word.startsWith("size")) {return CharacterGroup.CSIZE;}
			else if(word.startsWith("day")) {return CharacterGroup.TIME;}
			else if(word.startsWith("celsius_degree")) {return CharacterGroup.TEMP;}
			else if(word.startsWith("thick")) {return CharacterGroup.CWIDTH;}//treat cell thick as cell width
			else if(word.equals("and")||")".equals(word)) {break;}
		}
		
		//go forward
		for(int t=termBegIndex-1;t>=0&&t>=termEndIndex-5;t--){//window is 5
			String word = taggedWords.get(t).word();
			//System.out.println(word);
			if(word.startsWith("diamet")) {return CharacterGroup.CDIAM;}
			else if(word.startsWith("length")||word.startsWith("long")||word.startsWith("short")) {return CharacterGroup.CLENGTH;}
			else if(word.startsWith("wid")) {return CharacterGroup.CWIDTH;}
			else if(word.startsWith("size")) {return CharacterGroup.CSIZE;}
			else if(word.equals("and")) {break;}//||"(".equals(word)
		}
		return null;
	}


	/**
	 * Examples:
	 * 0.5-3.0 x 0.4-0.5 µm
	 * 0.4 by 2 to 20 µm
	 * @param valueList
	 * @param taggedWords
	 */
	private void mergeMultiplication(List featureList,
			List<TaggedWord> taggedWords) {
		for(int i=0;i<featureList.size()-1;){
			NumericCharacterValue curFd = (NumericCharacterValue) featureList.get(i);
			NumericCharacterValue nextFd = (NumericCharacterValue) featureList.get(i+1);
			int curEnd = curFd.getTermEndIdx();
			int nextBegin = nextFd.getTermBegIdx();
			//System.out.println("curEnd="+curEnd+" nextBegin="+nextBegin);
			//merge +/CC, //:, -1/CD
			if(nextBegin-curEnd<=4){
				if(nextFd.getValue().startsWith("x")||nextFd.getValue().startsWith("×")){
					String value = curFd.getValue()+nextFd.getValue();
					curFd.setValue(value);
					curFd.setTermEndIdx(nextFd.getTermEndIdx());
					if("".equals(curFd.getUnit())){
						curFd.setUnit(nextFd.getUnit());
					}
					featureList.remove(nextFd);
				}
				
				int j = curEnd+1;
				while(j<nextBegin){
					//System.out.println("by="+taggedWords.get(j).word());
					if(taggedWords.get(j).word().equals("x")||taggedWords.get(j).word().equals("by")||taggedWords.get(j).word().equals("×")){
						if(nextFd.getValue().startsWith("×")||nextFd.getValue().startsWith("x")){
							nextFd.setValue(nextFd.getValue().replace("×", "").replace("x", ""));
						}
						String value = curFd.getValue()+"x"+nextFd.getValue();
						curFd.setValue(value);
						curFd.setTermEndIdx(nextFd.getTermEndIdx());
						if("".equals(curFd.getUnit())){
							curFd.setUnit(nextFd.getUnit());
						}
						featureList.remove(nextFd);
					}
					j++;
				}
				
			}
			
				
			i++;
		}//deal all the values
	}


	/**
	 * detect single figure and figure ranges
	 * 
	 * @param taggedWords
	 * @return
	 */
	public List detectFigures(List<TaggedWord> taggedWords) {
		//System.out.println(taggedWords);
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
				termId = i;
				NumericCharacterValue fd = new NumericCharacterValue(this.getLabel());
				String unit = "";
				
				figure = word.word();
				//if(!containNumber(figure)){i++;continue;}
				//System.out.println("it is a figure:"+figure+" "+unit);
				while(true){
					if(i+1<taggedWords.size()&&(taggedWords.get(i+1).tag().equals("CD")&&(containNumber(taggedWords.get(i+1).word())||"<".equalsIgnoreCase(taggedWords.get(i+1).word()))||defIsNumber(taggedWords.get(i+1).word()))){
						figure+=taggedWords.get(i+1).word();
						i++;
						continue;
					}
					break;
				}
				if(i+1<taggedWords.size()){
					String followingWord = taggedWords.get(i+1).word();
					if(followingWord.equalsIgnoreCase("µm")||followingWord.equalsIgnoreCase("µmin")){
						unit = "µm";
					}else if(followingWord.equalsIgnoreCase("pm")){
						unit = "pm";
					}else if(followingWord.equals("mm")){//mm
						unit = "mm";
					}else if(followingWord.equalsIgnoreCase("microns")||followingWord.equalsIgnoreCase("mcirons")){//microns
						unit = "microns";
					}else if(followingWord.equalsIgnoreCase("micron")){//micron
						unit = "micron";
					}else if(followingWord.equalsIgnoreCase("nm")){//nm
						unit = "nm";
					}else if(followingWord.equalsIgnoreCase("µm")){//nm
						unit = "µm";
					}
				}
				
				
				fd.setValue(figure);
				fd.setTermBegIdx(termId);
				fd.setTermEndIdx(i);
				fd.setUnit(unit);
				
				//detectModifier(fd,taggedWords);// detect the modifier for the figure
				
				features.add(fd);
				
			}
			//System.out.println("it is a figure:"+figure);
			i++;
		}//all words traversed
		return features;
	}

}
