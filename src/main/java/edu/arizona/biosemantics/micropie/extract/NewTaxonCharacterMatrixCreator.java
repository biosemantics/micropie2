package edu.arizona.biosemantics.micropie.extract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.classify.LabelPhraseValueType;
import edu.arizona.biosemantics.micropie.extract.context.ContextInferrenceExtractor;
import edu.arizona.biosemantics.micropie.extract.context.PhraseRelationGraph;
import edu.arizona.biosemantics.micropie.extract.context.RelationParser;
import edu.arizona.biosemantics.micropie.extract.keyword.GlobalKeywordExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellScaleExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.PHTempNaClExtractor;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.CharacterValueFactory;
import edu.arizona.biosemantics.micropie.model.NewTaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.Matrix;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.NumericCharacterValue;
import edu.arizona.biosemantics.micropie.model.Phrase;
import edu.arizona.biosemantics.micropie.model.PhraseRelation;
import edu.arizona.biosemantics.micropie.model.SubSentence;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.nlptool.PhraseParser;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.stanford.nlp.ling.TaggedWord;

/**
 * 
 * Jin's algorithm
 * 
 * @author maojin
 *
 */
public class NewTaxonCharacterMatrixCreator implements ITaxonCharacterMatrixCreator {

	private LinkedHashSet<ILabel> characterLabels;// the characters need to be parsed
	private LinkedHashSet<String> characterNames;
	private HashSet categoryThreeLabels=new HashSet();
	{	categoryThreeLabels.add(Label.c17);
		categoryThreeLabels.add(Label.c18);
		categoryThreeLabels.add(Label.c19);
		categoryThreeLabels.add(Label.c20);
		categoryThreeLabels.add(Label.c21);
		categoryThreeLabels.add(Label.c22);
		categoryThreeLabels.add(Label.c23);
		categoryThreeLabels.add(Label.c24);
		categoryThreeLabels.add(Label.c25);
		categoryThreeLabels.add(Label.c26);
		categoryThreeLabels.add(Label.c27);
		categoryThreeLabels.add(Label.c28);
		categoryThreeLabels.add(Label.c29);
		categoryThreeLabels.add(Label.c30);
	}
	
	
	private HashSet categoryTwoLabels=new HashSet();
	{	categoryTwoLabels.add(Label.c2);
		categoryTwoLabels.add(Label.c3);
		categoryTwoLabels.add(Label.c4);
		categoryTwoLabels.add(Label.c5);
		categoryTwoLabels.add(Label.c6);
		categoryTwoLabels.add(Label.c7);
		categoryTwoLabels.add(Label.c8);
		categoryTwoLabels.add(Label.c9);
		categoryTwoLabels.add(Label.c10);
		categoryTwoLabels.add(Label.c11);
		categoryTwoLabels.add(Label.c12);
		categoryTwoLabels.add(Label.c13);
		categoryTwoLabels.add(Label.c14);
		categoryTwoLabels.add(Label.c15);
		categoryTwoLabels.add(Label.c16);
	}
	
	private HashSet metobolismLabels = new HashSet();
	{
		metobolismLabels.add(Label.c53);
		metobolismLabels.add(Label.c54);
		metobolismLabels.add(Label.c55);
		metobolismLabels.add(Label.c56);
		metobolismLabels.add(Label.c57);
		metobolismLabels.add(Label.c58);
	}
	
	private ICharacterValueExtractorProvider contentExtractorProvider;// extractors

	private Map<TaxonTextFile, List<MultiClassifiedSentence>> taxonSentencesMap;

	private NewTaxonCharacterMatrix results;// the results of the extraction
	
	private LabelPhraseValueType labelValueType;
	
	//private Map<String, Set<ILabel>> GlobalTermCharacterMap;

	private GlobalKeywordExtractor globalKeywordExtractor;
	private ContextInferrenceExtractor contInfExtractor;
	private PhraseParser phraseParser = new PhraseParser();
	private RelationParser relationParser = new RelationParser();
	private PosTagger posTagger;
	private SentenceSpliter sentSplitter;
	private PostProcessor postProcessor = new PostProcessor(); 
	
	//whether make assumptions for USP values of pH, Temperature and NaCL values
	private boolean judgeUSPForPTN = false;
	
	@Inject
	public NewTaxonCharacterMatrixCreator(
			ICharacterValueExtractorProvider contentExtractorProvider,
			GlobalKeywordExtractor globalKeywordExtractor,
			ContextInferrenceExtractor contInfExtractor,
			PosTagger posTagger,
			SentenceSpliter sentSplitter,
			LabelPhraseValueType labelValueType) {
		this.contentExtractorProvider = contentExtractorProvider;
		this.globalKeywordExtractor = globalKeywordExtractor;
		this.contInfExtractor = contInfExtractor;
		this.labelValueType = labelValueType;
		this.posTagger = posTagger;
		this.sentSplitter = sentSplitter;
	}

	public NewTaxonCharacterMatrix getResults() {
		return results;
	}

	public void setCharacterLabels(LinkedHashSet<ILabel> characterLabels) {
		this.characterLabels = characterLabels;
	}
	
	public void setCharacterNames(LinkedHashSet<String> characterNames) {
		this.characterNames = characterNames;
	}

	public void setTaxonSentencesMap(
			Map<TaxonTextFile, List<MultiClassifiedSentence>> taxonSentencesMap) {
		this.taxonSentencesMap = taxonSentencesMap;
	}

	
	/**
	 * whether make assumptions for USP values of pH, Temperature and NaCL values
	 * @param judgeUSPForPTN
	 */
	public void setJudgeUSPForPTN(boolean judgeUSPForPTN) {
		this.judgeUSPForPTN = judgeUSPForPTN;
	}

	
	@Override
	public Matrix create() {

		// <Taxon, <Character, List<Value>>>
		results = new NewTaxonCharacterMatrix();
		
		results.setTaxonFiles(taxonSentencesMap.keySet());
		results.setCharacterLabels(characterLabels);
		//results.setCharacterNames(characterNames);
		
		//process all the files
		for (TaxonTextFile taxonFile : taxonSentencesMap.keySet()) {
			LinkedList<MultiClassifiedSentence> sentences = (LinkedList<MultiClassifiedSentence>) taxonSentencesMap.get(taxonFile);
			this.create(results, taxonFile, sentences);
		}
		return results;
	}

	
	/**
	 * parse the results, put it into the matrix
	 * @param extResults
	 * @param taxonFile
	 * @param charValues
	 */
	private void parseResult(NewTaxonCharacterMatrix extResults, TaxonTextFile taxonFile, ILabel defaultLabel,
			List<CharacterValue> charValues, List<CharacterValue> noLabelValueList) {
		if(charValues==null||charValues.size()==0) return;
		//before parse the values into the map, post process the values
		//System.out.println("before post process:"+charValues);
		
		//System.out.println("after post process:"+charValues);
		
		Map<ILabel, List<CharacterValue>> charMap = extResults.getAllTaxonCharacterValues(taxonFile);
		//if(judgeUSPForPTN) postProcessor.dealUSP(noLabelValueList, charMap);
		postProcessor.postProcessor(charValues,noLabelValueList);
		//if(label!=null){//not a mixed value extractor
		//	charMap.get(label).addAll(charValues);
		//}else{
			for(CharacterValue value : charValues){
				
				ILabel clabel = value.getCharacter();//find the label
				List values = charMap.get(clabel);
				if(values==null){
					values = new ArrayList();
					charMap.put(clabel, values);
				}
				
				if(!values.contains(value)){
					if(clabel==null) clabel = defaultLabel;
					if(!values.contains(value)) charMap.get(clabel).add(value);
				}
				
				/*
				if(labelValueType.nuCharSet.contains(clabel)){//numeric value
					NumericCharacterValue nvalue = (NumericCharacterValue)value;
					if(clabel!=null){
						if(!values.contains(nvalue)) charMap.get(clabel).add(nvalue);
					}
				}else{//string value
					if(!values.contains(value)){
						if(clabel==null) clabel = defaultLabel;
						charMap.get(clabel).add(value);
					}
				}*/
			}
		//}
		
		
	}
	

	/**
	 * parse the results, put it into the matrix
	 * @param extResults
	 * @param taxonFile
	 * @param charValues
	 */
	private void parseResultForSentence(List<CharacterValue> newCharValues,TaxonTextFile taxonFile, ILabel defaultLabel,
			List<CharacterValue> charValues, List<CharacterValue> noLabelValueList) {
		if(charValues==null||charValues.size()==0) return;
		
		postProcessor.postProcessor(charValues,noLabelValueList);
		for(CharacterValue value : charValues){
			
			ILabel clabel = value.getCharacter();//find the label
			if(clabel==null) clabel = defaultLabel;
			value.setCharacter(clabel);
			if(!newCharValues.contains(value)){
				newCharValues.add(value);
			}
		}//values
		
	}


	
	/**
	 * extract a taxonfile and extract the result into a given matrix
	 * @param matrix
	 * @param textFile
	 * @param sentences
	 */
	public void create(NewTaxonCharacterMatrix matrix, TaxonTextFile taxonFile,
			List<MultiClassifiedSentence> sentences) {
		String taxon = taxonFile.getTaxon();
		boolean hasGC = false;
		boolean hasCellScale = false;
		boolean hasPTN = false;//has parsed the ph temp nacl
		
		//initialize the character values
		Map<ILabel, List<CharacterValue>> charaMap = new HashMap();
		Iterator<ILabel> charIter = characterLabels.iterator();
		while(charIter.hasNext()){
			ILabel ilabel = charIter.next();
			charaMap.put(ilabel, new LinkedList());
		}
		matrix.put(taxonFile, charaMap);
		
		//no labeled values for this taxon file
		List<CharacterValue> noLabelValueList = new ArrayList();
		
		// the sentences in the file
		for (MultiClassifiedSentence classifiedSentence : sentences) {// process one sentence
			//parse phrases
			String text = classifiedSentence.getText();
			text = text.replace("degree_celsius_1", "˚C").replace("degree_celsius_7", "˚C");
			classifiedSentence.setText(text);
			
			

			// Reference:
			// get the character extractors for this sentence
			Set<ICharacterValueExtractor> extractors = new HashSet<ICharacterValueExtractor>();
			Set<ILabel> predictions = classifiedSentence.getPredictions();
			if (predictions.size() == 0) {// it can be any character,||(predictions.size() == 1&&predictions.contains(Label.c0))
				extractors = contentExtractorProvider.getAllContentExtractor();
			}
			
			for (ILabel label : predictions) {// get all the extractors ready
				//if (label instanceof Label && characterLabels.contains(label)) {
				if (characterLabels.contains(label)) {
					Set<ICharacterValueExtractor> labelExtractors = contentExtractorProvider.getContentExtractor((Label) label);
					if(labelExtractors!=null){
						extractors.addAll(labelExtractors);
					}
				}
				
				//salinity preference
				if(categoryThreeLabels.contains(label)){
					Set<ICharacterValueExtractor> labelExtractors = contentExtractorProvider.getContentExtractor(Label.c59);
					if(labelExtractors!=null){
						extractors.addAll(labelExtractors);
					}
				}
				
				//cell shape
				if(categoryTwoLabels.contains(label)){
					Set<ICharacterValueExtractor> labelExtractors = contentExtractorProvider.getContentExtractor(Label.c2);
					if(labelExtractors!=null){
						extractors.addAll(labelExtractors);
					}
				}
			}

			List<CharacterValue> sentCharValues = new ArrayList();
			// call the extractors one by one according to the predicted characters
			//TODO:FermentationProductExtractor
			boolean containMetabolism = false;
			for (ICharacterValueExtractor extractor : extractors) {
				String character = extractor.getCharacterName();
				//System.out.println("current character:"+character+" sentence:"+text);
				ILabel label = extractor.getLabel();
				List<CharacterValue> charValues = null;
				if(!containMetabolism&&metobolismLabels.contains(label)){
					containMetabolism = true;
				}
				if(extractor instanceof CellScaleExtractor && !hasCellScale){
					charValues = extractor.getCharacterValue(classifiedSentence);
				}else if(extractor instanceof CellScaleExtractor && hasCellScale){
					continue;
				}else if(extractor instanceof PHTempNaClExtractor && !hasPTN){
					charValues = extractor.getCharacterValue(classifiedSentence);
					
					//TODO:check whether multiple labels are in this list
					if(judgeUSPForPTN) postProcessor.seperateLabelAndUnlabelList(charValues,noLabelValueList);
				}else if(extractor instanceof PHTempNaClExtractor && hasPTN){
					charValues = extractor.getCharacterValue(classifiedSentence);
					//TODO:check whether multiple labels are in this list
					if(judgeUSPForPTN) postProcessor.seperateLabelAndUnlabelList(charValues,noLabelValueList);
				}else{
					charValues = extractor.getCharacterValue(classifiedSentence);
				}
				
				parseResult(matrix, taxonFile, label, charValues,noLabelValueList);
				//sentCharValues.addAll(charValues);
				//add characters into sentence list
				parseResultForSentence(sentCharValues,taxonFile, label, charValues, noLabelValueList);
			}
			
			//if(containMetabolism) continue;
			/*
			posSentence(classifiedSentence);//get sub sentences and their tagged words list
			List<List<TaggedWord>> taggedWordList = classifiedSentence.getSubSentTaggedWords();
			
			if(taggedWordList.size()>=1){//process each subsentence
				List<List<Phrase>> allPhraseList = classifiedSentence.getPhraseList();
				if(allPhraseList==null){
					allPhraseList = new ArrayList();
					classifiedSentence.setPhraseList(allPhraseList);
					for(int subsent=0;subsent<taggedWordList.size();subsent++){
						List<Phrase> phraseList = phraseParser.extract(taggedWordList.get(subsent));
						allPhraseList.add(phraseList);
					}
				}
				
				
				List<PhraseRelationGraph> prGraphList = new ArrayList();
				classifiedSentence.setPhraseRelationGraphs(prGraphList);
				
				for(int subsent=0;allPhraseList!=null&&subsent<taggedWordList.size()&&subsent<allPhraseList.size();subsent++){
					PhraseRelationGraph prGraph = relationParser.parseCoordinativeRelationGraph(allPhraseList.get(subsent), taggedWordList.get(subsent));
					prGraphList.add(prGraph);
				}
			}
			
			
			//System.out.println("predict character according to the context");
			//to check whether there are some phrases that do not identified any values
			//Stragety 1: unique match
			List phraseCharValues = new ArrayList();
			List<List<Phrase>> phrases = classifiedSentence.getPhraseList();
			if(phrases!=null&&phrases.size()>0){//need to parse before this step
				for(List<Phrase> plist:phrases){
					for(Phrase p:plist){
						if(p.getCharValue()==null){
							//System.out.println("after first round extraction, no value identified:"+p.getText());
							//if a term is unique in the term list
							CharacterValue cv = this.globalKeywordExtractor.uniqMatch(p);
							
							if(cv!=null&&!phraseCharValues.contains(cv)){
								phraseCharValues.add(cv);
								//System.out.println("values are found via global matching:"+cv);
							}
						}
						else{
							System.out.println("values are found:"+p.getCharValue());
					}
				}
			}
			
			
			parseResult(matrix, taxonFile, null, phraseCharValues,noLabelValueList);
			
			*/
			//Stragety 2: strict infer
			/*
			phraseCharValues = new ArrayList();
			phrases = classifiedSentence.getPhraseList();
			if(phrases!=null&&phrases.size()>0){//need to parse before this step
				for(int sent =0; sent<phrases.size();sent++){//for each subsentence
					List<Phrase> plist = phrases.get(sent);
					PhraseRelationGraph prGraph = classifiedSentence.getPhraseRelationGraphs().get(sent);
					for(int i = 0;i<plist.size();i++){
						Phrase p = plist.get(i); 
						if(p.getCharValue()==null){
							//if the ahead term and the follow term have the same character value
							//CharacterValue cv = contInfExtractor.strictStrategy1(p, prGraph);
							
							//according to the surround weight
							CharacterValue cv = contInfExtractor.strictStrategy2(p, prGraph);
							
							if(cv!=null&&!phraseCharValues.contains(cv)){
								phraseCharValues.add(cv);
								System.out.println("values are found via Strict Strategy2:"+cv);
							}
						}
					}
				}
			}
			
			parseResult(matrix, taxonFile, null, phraseCharValues,noLabelValueList);
			//infer the value according to the context
			 */
			taxonFile.getSentCharacterValues().put(classifiedSentence, sentCharValues);
		}//sentence end
		
		
		//out of single sentence
		/**
		This component is used to predict values for PH, NACL, TEMP			
		**/
		if(judgeUSPForPTN)  postProcessor.dealUSP(noLabelValueList,charaMap);
		
		
		//postProcessor.dealConflictNum(charaMap);
		
		postProcessor.mergeCellDiameterIntoWidth(charaMap);
	}
	
	
	/**
	 * 1, do not separate subsetences
	 * 2, postag each subsentence
	 * 
	 * @param sentence
	 */
	public void posSentence(MultiClassifiedSentence sentence){
		List taggerwordsList = sentence.getSubSentTaggedWords();
		if(taggerwordsList==null){
			taggerwordsList = new LinkedList();
			sentence.setSubSentTaggedWords(taggerwordsList);
			String content = sentence.getText();
			List<TaggedWord> taggedWords  = posTagger.tagString(content);
			taggerwordsList.add(taggedWords);
		}
	}

	/**
	 * 1, separate subsetences
	 * 2, postag each subsentence
	 * 
	 * @param sentence
	 
	public void posSentence(MultiClassifiedSentence sentence){
		//1, detect sentences
		List<SubSentence> subSentences = sentence.getSubSentence();
		if(subSentences == null){
			subSentences = sentSplitter.detectSnippet(sentence);
			sentence.setSubSentence(subSentences);
		}
		
		//2, postag each subsentence
		List taggerwordsList = sentence.getSubSentTaggedWords();
		if(taggerwordsList==null){
			taggerwordsList = new LinkedList();
			sentence.setSubSentTaggedWords(taggerwordsList);
			for(SubSentence subsent:subSentences){
				String content = subsent.getContent();
				List<TaggedWord> taggedWords  = posTagger.tagString(content);
				taggerwordsList.add(taggedWords);
			}
		}
	}*/
}
