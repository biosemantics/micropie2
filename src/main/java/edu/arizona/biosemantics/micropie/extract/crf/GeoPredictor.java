package edu.arizona.biosemantics.micropie.extract.crf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import com.google.inject.Inject;

import cc.mallet.fst.CRF;
import cc.mallet.fst.MaxLatticeDefault;
import cc.mallet.fst.NoopTransducerTrainer;
import cc.mallet.fst.TokenAccuracyEvaluator;
import cc.mallet.fst.Transducer;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SimpleTaggerSentence2TokenSequence;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.FeatureVectorSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Labeling;
import cc.mallet.types.Sequence;
import edu.arizona.biosemantics.micropie.Configuration;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * --model-file nouncrf  stest
 * @author maojin
 *
 */
public class GeoPredictor extends AbstractCharacterValueExtractor{
	//private ILabel label = Label.c31;//Geographic location
	
	private int nBest = 1;
	private boolean includeInput = false;
	private static int cacheSize = 100000;
	
	private FeatureRender featureRender;
	private CRF crf;
	private Pipe pipe;
	
	@Inject
	public GeoPredictor(ILabel label, String character,FeatureRender featureRender){
		super(label, character);
		this.featureRender = featureRender;
		readCRFModel();
	}
	
	
	public void readCRFModel(){
		ObjectInputStream s;
		try {
			s = new ObjectInputStream(new FileInputStream(Configuration.configurationFolder+"/"+Configuration.geoTaggerModel));
			
			crf = (CRF) s.readObject();
			if(crf!=null){
				pipe = crf.getInputPipe();
				//System.out.println("load crf model success! parameters:"+crf.getNumParameters());
			}
			//System.out.println(Configuration.geoTaggerModel);
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public List<CharacterValue> getCharacterValue(Sentence sentence) {
		List<CharacterValue> charValueList =  new ArrayList();
		String sentText = sentence.getText();
		//System.out.println(sentText);
		List<Token> sentTokenList = featureRender.render(sentText);
		InstanceList instance = transform(sentTokenList);
		String[] predictedLabels = predict(instance);
		List<BBEntity> entities = parseEntity(sentTokenList,predictedLabels);
		for(BBEntity entity:entities){
			charValueList.add(new CharacterValue(label, entity.getName()));
			//System.out.println(entity.getName());
		}
		return charValueList;
	}
	
	public List<BBEntity> parseEntity(List<Token> allTokenSeqs, String[] predLines){
		List<BBEntity> entities = new ArrayList();
		
		BBEntity entity = new BBEntity();
		entities.add(entity);
		for(int line = 0;line<allTokenSeqs.size(); line++ ){
			Token token = allTokenSeqs.get(line);
			
			if(predLines[line].indexOf("Habitat")>-1){//habitat
				if(entity.getName()==null){//a new one
					entity.addTokenOnly(token);
					entity.setName(token.getText());
					entity.setStart(token.getOffset());
					entity.setEnd(token.getOffend());
					entity.setType("Habitat");
				}else if(entity.getName()!=null&&entity.getType().equals("Habitat")){
					if(token.getOffset()-entity.getEnd()==0){
						entity.setName(entity.getName()+""+token.getText());
					}else{
						entity.setName(entity.getName()+" "+token.getText());
					}
					entity.addTokenOnly(token);
					entity.setEnd(token.getOffend());
				}else{//the former is bacteria, add to the list and create a new one
					entity = new BBEntity();
					entities.add(entity);
					entity.addTokenOnly(token);
					entity.setName(token.getText());
					entity.setStart(token.getOffset());
					entity.setEnd(token.getOffend());
					entity.setType("Habitat");
				}
			}else if(predLines[line].indexOf("Bacteria")>-1){//Bacteria
				if(entity.getName()==null){//a new one
					entity.setName(token.getText());
					entity.setStart(token.getOffset());
					entity.setEnd(token.getOffend());
					entity.addTokenOnly(token);
					entity.setType("Bacteria");
				}else if(entity.getName()!=null&&entity.getType().equals("Bacteria")){
					if(token.getOffset()-entity.getEnd()==0){
						entity.setName(entity.getName()+""+token.getText());
					}else{
						entity.setName(entity.getName()+" "+token.getText());
					}
					entity.addTokenOnly(token);
					entity.setEnd(token.getOffend());
				}else{//the former is bacteria, add to the list and create a new one
					entity = new BBEntity();
					entities.add(entity);
					entity.setName(token.getText());
					entity.setStart(token.getOffset());
					entity.setEnd(token.getOffend());
					entity.addTokenOnly(token);
					entity.setType("Bacteria");
				}
			}else if(predLines[line].indexOf("Geographical")>-1){//Geographical
				if(entity.getName()==null){//a new one
					entity.setName(token.getText());
					entity.setStart(token.getOffset());
					entity.setEnd(token.getOffend());
					entity.addTokenOnly(token);
					entity.setType("Geographical");
				}else if(entity.getName()!=null&&entity.getType().equals("Geographical")){
					if(token.getOffset()-entity.getEnd()==0){
						entity.setName(entity.getName()+""+token.getText());
					}else{
						entity.setName(entity.getName()+" "+token.getText());
					}
					entity.addTokenOnly(token);
					entity.setEnd(token.getOffend());
				}else{//the former is bacteria, add to the list and create a new one
					entity = new BBEntity();
					entities.add(entity);
					entity.setName(token.getText());
					entity.setStart(token.getOffset());
					entity.setEnd(token.getOffend());
					entity.setType("Geographical");
					entity.addTokenOnly(token);
				}
			}else if(predLines[line].trim().equals("O")||predLines[line].trim().equals("")){
				entity = new BBEntity();
				entities.add(entity);
			}
		}//
		for(int i=0;i<entities.size();){
			if(entities.get(i).getName()==null){
				entities.remove(i);
			}else{
				i++;
			}
		}
		return entities;
	}
	
	/**
	 * transform from Sentence by using Mallet pipe line.
	 * 
	 * @param sentence
	 * @return
	 */
	public InstanceList transform(List<Token> sentTokenList){
		String sentTokenString = transfromTokenFeatureString(sentTokenList);
//		Pipe p = new SimpleTaggerSentence2FeatureVectorSequence();
//		p.getTargetAlphabet().lookupIndex("O");
//		//System.out.println(sentTokenString);
		Reader trainingFile= new StringReader(sentTokenString);
		//p.setTargetProcessing(true);
		
		InstanceList trainingData = new InstanceList(pipe);
		trainingData.addThruPipe(new LineGroupIterator(trainingFile, Pattern.compile("^\\s*$"), true));
		
		//System.out.println("Import data:"+trainingData.size());
		return trainingData;
	}
	
	
	/**
	 * keep the same order with training file
	 * @param sentTokenList
	 * @return
	 */
	private String transfromTokenFeatureString(List<Token> sentTokenList) {
		StringBuffer sb = new StringBuffer();
		for(Token token:sentTokenList){
			sb.append(featureRender.generateLineForToken(token));
			sb.append("\n");
		}
		return sb.toString();
	}


	/**
	 * the last column is the annotation result
	 * @param modelPath
	 * @param testData
	 */
	public void predict(String modelPath, InstanceList testData, String testResult){
		ObjectInputStream s;
		try {
			s = new ObjectInputStream(
					new FileInputStream(modelPath));
			CRF crf = (CRF) s.readObject();
			s.close();
			
			//crf.transduce(testData);
			TransducerEvaluator eval = new TokenAccuracyEvaluator(new InstanceList[] {
					testData }, new String[] { "Testing" });
			//test(crf, eval, testData,testResult);
			
			//outputTestResults(testData,testResult);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Test a transducer on the given test data, evaluating accuracy with the
	 * given evaluator
	 *
	 * @param model
	 *            a <code>Transducer</code>
	 * @param eval
	 *            accuracy evaluator
	 * @param testing
	 *            test data
	 */
	public String[] predict(InstanceList testData) {
		//eval.evaluateInstanceList(tt, testing, "Testing");
		//false 1 100000
		//includeInputOption.value()+" "+nBestOption.value+" "+cacheSizeOption.value()
		String[] predictedResults;
		//FileWriter fw = new FileWriter(testResultFile);
		List<String> labelList = new ArrayList();
		for (int i = 0; i < testData.size(); i++) {
			Sequence input = (Sequence) testData.get(i).getData();
			//System.out.println("input size ="+input.size());
			Sequence[] outputs = apply(crf, input, nBest);
			//System.out.println(input.size()+" "+outputs.length+" "+outputs[outputs.length-1]);
			int k = outputs.length;
			boolean error = false;
			for (int a = 0; a < k; a++) {
				if (outputs[a].size() != input.size()) {
					error = true;
				}
			}
			if (!error) {
				for (int j = 0; j < input.size(); j++) {
					StringBuffer buf = new StringBuffer();
					for (int a = 0; a < k; a++)
						//buf.append(outputs[a].get(j).toString())
						//		.append(" ");
						labelList.add(outputs[a].get(j).toString());
//						if (includeInput) {
//							FeatureVector fv = (FeatureVector) input.get(j);
//							buf.append(fv.toString(true));
//						}
					//System.out.println(buf.toString());
					//fw.write(buf.toString()+"\n");
				}
				//System.out.println();
				//fw.write("\n");
			}
		}
		predictedResults= new String[labelList.size()];
		labelList.toArray(predictedResults);
		return predictedResults;
	}
	
	
	/**
	 * Apply a transducer to an input sequence to produce the k highest-scoring
	 * output sequences.
	 *
	 * @param model
	 *            the <code>Transducer</code>
	 * @param input
	 *            the input sequence
	 * @param k
	 *            the number of answers to return
	 * @return array of the k highest-scoring output sequences
	 */
	public static Sequence[] apply(Transducer model, Sequence input, int k) {
		System.out.println("input length="+input.size());
		Sequence[] answers;
		if (k == 1) {
			answers = new Sequence[1];
			answers[0] = model.transduce(input);
		} else {
			MaxLatticeDefault lattice = new MaxLatticeDefault(model, input,
					null, cacheSize);//cacheSizeOption.value()

			answers = lattice.bestOutputSequences(k).toArray(new Sequence[0]);
		}
		return answers;
	}
	
	
	/**
	 * 
	 * @param testData
	 * @param resultsFile
	 */
	public void outputTestResults(InstanceList testData, String resultsFile){
		try {
			FileWriter fw = new FileWriter(resultsFile);
			Iterator testIter = testData.iterator();
			while(testIter.hasNext()){
				Instance testInst = (Instance) testIter.next();
				Object testLabel = testInst.getTarget();
				FeatureVectorSequence fv = (FeatureVectorSequence) testInst.getData();
				//System.out.println(testLabel.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args){

		Properties stanfordCoreProperties = new Properties();
		stanfordCoreProperties.put("annotators", "tokenize, ssplit, parse,lemma");// ,parse, pos, lemma, ner, , dcoref
		StanfordCoreNLP sfCoreNLP = new StanfordCoreNLP(stanfordCoreProperties);
		
		FeatureRender tokenFeatureRender = new FeatureRender(sfCoreNLP);
		
		GeoPredictor crfPredictor = new GeoPredictor(Label.c31, "Geographical Location", tokenFeatureRender);
		crfPredictor.getCharacterValue(new Sentence("The type strain is MIM18T (= ACCC 05421T= KCTC 23224T), isolated from an alkaline and saline lake on the Mongolia Plateau"));
		crfPredictor.getCharacterValue(new Sentence("Isolated from the green alga Acrosiphonia sonderi, collected in Troitsa Bay, Gulf of Peter the Great, East Sea (Sea of Japan)."));
		crfPredictor.getCharacterValue(new Sentence("The type strain, DCY35 (=KCTC 13206 =JCM 15085), was isolated from soil of a ginseng field in South Korea"));
		crfPredictor.getCharacterValue(new Sentence("The type strain is RW262T  (= NCIMB 13979T= DSM 16823T), which was isolated from water of the River Taff, Cardiff, UK"));
		crfPredictor.getCharacterValue(new Sentence("The type strain, CCUG 53201 T (=DSM 22953 T), was isolated as a contaminant of a human blood sample in Gon Xuwen County"));
	}
}
