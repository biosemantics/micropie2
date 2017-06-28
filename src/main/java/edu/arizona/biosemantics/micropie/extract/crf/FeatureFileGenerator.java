package edu.arizona.biosemantics.micropie.extract.crf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * Deal with generate input files for multiple tools
 * 
 * @author maojin
 *
 */
public class FeatureFileGenerator {
	private FeatureRender featureRender;
	private A1FormatFileUtil a1FileUtil = new A1FormatFileUtil();
	
	public FeatureFileGenerator(){
		Properties stanfordCoreProperties = new Properties();
		stanfordCoreProperties.put("annotators", "tokenize, ssplit, parse,lemma");// ,parse, pos, lemma, ner, , dcoref
		StanfordCoreNLP sfCoreNLP = new StanfordCoreNLP(stanfordCoreProperties);
		
		featureRender = new FeatureRender(sfCoreNLP);
	}
	
	
	public void generateForAllFiles(String folder, String featureFile, boolean isTrain){
		try {
			FileWriter fw = new FileWriter(new File(featureFile),false);
			
			File[] files = new File(folder).listFiles();
			//read all txt files
			for(File file : files){
				String fileName = file.getName();
				if(fileName.endsWith("txt")){//text file
					String sent = FileUtils.readFileToString(file,"UTF-8");
					
					List<Token> tokens = featureRender.render(sent);
					
					//read ann file
					String annFile = folder+"/"+fileName.replace(".txt", ".ann");
					List<BBEntity> entityList = a1FileUtil.readFromFile(annFile);
					
					if(entityList!=null&&entityList.size()>0){
						//render label
						featureRender.renderLabel(tokens, entityList);
						
						//output to sequence
						outputCRFFile(tokens, isTrain, fw);
						fw.write("\n");
					}
				}
			}
			
			fw.flush();
			fw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * generate the input file for Mallet sequence labelling, CRF/HMM
	 * The features are from TokenFeatureRender
	 * 
	 * @param tokenList
	 * @param crfFormatFile
	 * @param isTrain
	 */
	public void outputCRFFile(List<Token> tokenList, boolean isTrain,FileWriter fw){
		try {
			
			//fw.write(fileName+"\n");
			//Format: 
			/***
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
				10 label
			 */
			int currentSent = -1;
			String lastLabel = "";
			for(Token token : tokenList){
				int sentId = token.getSentenceId();
				if(currentSent!=-1&&sentId!=currentSent){
					fw.write("\n");
				}
				currentSent = sentId;
				
				//start
				fw.write(featureRender.generateLineForToken(token).toString());
				
				if(isTrain){//22
					String currentLabel = token.getAttribute(TokenAttribute.NER).toString();
					if(currentLabel.equals("Habitat")&&"Habitat".equals(lastLabel)){
						fw.write("I-Habitat");//I-Habitat
					}else if(currentLabel.equals("Habitat")&&!"Habitat".equals(lastLabel)){
						fw.write("B-Habitat");//currentLabel
					}else if(currentLabel.equals("Bacteria")&&"Bacteria".equals(lastLabel)){
						fw.write("I-Bacteria");//I-Bacteria
					}else if(currentLabel.equals("Bacteria")&&!"Bacteria".equals(lastLabel)){
						fw.write("B-Bacteria");//B-Bacteria
					}else if(currentLabel.equals("Geographical")&&"Geographical".equals(lastLabel)){
						fw.write("I-Geographical");//I-Geographical
					}else if(currentLabel.equals("Geographical")&&!"Geographical".equals(lastLabel)){
						fw.write("B-Geographical");//B-Geographical
					}else{
						fw.write("O");//"O"
					}
					lastLabel = currentLabel;
				}
				
				fw.write("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		FeatureFileGenerator featureGenerator = new FeatureFileGenerator();
		String folder = "F:/MicroPIE/CRF/annotation";
		String featureFile ="F:/MicroPIE/CRF/geo_crf_model_062017.train";
		featureGenerator.generateForAllFiles(folder, featureFile, true);
	}
}
