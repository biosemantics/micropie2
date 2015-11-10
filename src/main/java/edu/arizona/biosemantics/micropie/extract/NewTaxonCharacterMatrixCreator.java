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
	{	categoryThreeLabels.add(Label.c18);
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
	 * parse the results
	 * @param extResults
	 * @param taxonFile
	 * @param charValues
	 */
	private void parseResult(NewTaxonCharacterMatrix extResults, TaxonTextFile taxonFile, ILabel defaultLabel,
			List<CharacterValue> charValues) {
		if(charValues==null||charValues.size()==0) return;
		//before parse the values into the map, post process the values
		//System.out.println("before post process:"+charValues);
		postProcessor.postProcessor(charValues);
		//System.out.println("after post process:"+charValues);
		
		Map<ILabel, List> charMap = extResults.getAllTaxonCharacterValues(taxonFile);
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
		Map charaMap = new HashMap();
		Iterator<ILabel> charIter = characterLabels.iterator();
		while(charIter.hasNext()){
			ILabel ilabel = charIter.next();
			charaMap.put(ilabel, new LinkedList());
		}
		matrix.put(taxonFile, charaMap);
		
		// the sentences in the file
		for (MultiClassifiedSentence classifiedSentence : sentences) {// process one sentence
			//parse phrases
			String text = classifiedSentence.getText();
			text = text.replace("degree_celsius_1", "˚C").replace("degree_celsius_7", "˚C");
			classifiedSentence.setText(text);
			
			Set<ILabel> predictions = classifiedSentence.getPredictions();
			if (predictions.size() == 0) {// it can be any character
				Label[] labelList = Label.values();
				for (int i = 0; i < labelList.length; i++) {
					predictions.add(labelList[i]);
				}
			}

			// Reference:
			// get the character extractors for this sentence
			Set<ICharacterValueExtractor> extractors = new HashSet<ICharacterValueExtractor>();
			for (ILabel label : predictions) {// get all the extractors ready
				//if (label instanceof Label && characterLabels.contains(label)) {
				if (characterLabels.contains(label)) {
					extractors.addAll(contentExtractorProvider.getContentExtractor((Label) label));
				}
				if(categoryThreeLabels.contains(label)){
					extractors.addAll(contentExtractorProvider.getContentExtractor(Label.c59));
				}
			}

			//System.out.println(classifiedSentence.getText()+"\npredictions:"+predictions.size()+" "+predictions+" extractors: "+extractors);
			
			//List<CharacterValue> sentCharValues = new ArrayList();
			// call the extractors one by one according to the predicted characters
			for (ICharacterValueExtractor extractor : extractors) {
				//String character = extractor.getCharacterName();
				ILabel label = extractor.getLabel();
				List<CharacterValue> charValues = null;
				
				if(extractor instanceof CellScaleExtractor && !hasCellScale){
					charValues = extractor.getCharacterValue(classifiedSentence);
				}else if(extractor instanceof CellScaleExtractor && hasCellScale){
					continue;
				}else if(extractor instanceof PHTempNaClExtractor && !hasPTN){
					charValues = extractor.getCharacterValue(classifiedSentence);
				}else if(extractor instanceof PHTempNaClExtractor && hasPTN){
					charValues = extractor.getCharacterValue(classifiedSentence);
				}else{
					charValues = extractor.getCharacterValue(classifiedSentence);
				}
				
				parseResult(matrix, taxonFile, label, charValues);
				//sentCharValues.addAll(charValues);
			}
			
			
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
				
				for(int subsent=0;allPhraseList!=null&&subsent<allPhraseList.size();subsent++){
					PhraseRelationGraph prGraph = relationParser.parseCoordinativeRelationGraph(allPhraseList.get(subsent), taggedWordList.get(subsent));
					prGraphList.add(prGraph);
				}
			}
			
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
						/*else{
							System.out.println("values are found:"+p.getCharValue());
						}*/
					}
				}
			}
			
			
			parseResult(matrix, taxonFile, null, phraseCharValues);
			
			
			//Stragety 2: strict infer
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
								//System.out.println("values are found via Strict Strategy2:"+cv);
							}
						}
						/*else{
							System.out.println("values are found:"+p.getCharValue());
						}*/
					}
				}
			}
			
			parseResult(matrix, taxonFile, null, phraseCharValues);
			
			//infer the value according to the context
		}
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
