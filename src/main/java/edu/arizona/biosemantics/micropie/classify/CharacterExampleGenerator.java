package edu.arizona.biosemantics.micropie.classify;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.arizona.biosemantics.micropie.io.FileReaderUtil;
import edu.arizona.biosemantics.micropie.model.RawSentence;


public class CharacterExampleGenerator {
	// sentence, labels
	public Map<String, Set<ILabel>> sentLabels = new HashMap();
	// label, sentences
	public Map<ILabel, Set<String>> labelSents = new HashMap();
	
	
	
	/**
	 * Two-column format:
	 * 1, label annotation
	 * 2, sentence
	 * 
	 * @param filePath
	 */
	public void loadTwoClns(String filePath){
		List<String> sentences = FileReaderUtil.readFileLines(filePath);
		for(String line:sentences){
			String[] sent = line.split("\t");
			ILabel label = LabelUtil.stringToLabel(sent[0].trim());
			String sentence = sent[1].trim();
			
			putToDataset(label,sentence);
		}
	}
	
	/**
	 * put into the dataset
	 * @param label
	 * @param sentence
	 */
	public void putToDataset(ILabel label, String sentence){
		Set<ILabel> labels = sentLabels.get(sentence);
		if(labels==null){
			labels = new HashSet();
			sentLabels.put(sentence, labels);
		}
		labels.add(label);
		
		Set<String> labelSentences = labelSents.get(label);
		if(labelSentences==null){
			labelSentences = new HashSet();
			labelSents.put(label, labelSentences);
		}
		labelSentences.add(sentence);
	}
	
	/**
	 * Three column format:
	 * first: corrected annotations
	 * second: org_predicted annotations
	 * third: sentence
	 */
	public void loadCorrectedClns(String filePath){
		List<String> sentences = FileReaderUtil.readFileLines(filePath);
		for(String line:sentences){
			//System.out.println(line);
			String[] sent = line.split("\t");
			String corLabels = sent[0].trim();
			String orgLabels = sent[1].trim();
			String labels = corLabels.equals("")?orgLabels:corLabels;
			String sentence = sent[2].trim();
			
			String[] labelStrs = labels.split(",");
			for(String labelStr:labelStrs){
				ILabel label = LabelUtil.stringToLabel(labelStr.trim());
				//System.out.println(labelStr+"="+label);
				putToDataset(label,sentence);
			}
		}
	}
	
	
	public Set<String> getPostiveSents(ILabel label){
		return this.labelSents.get(label);
	}
	
	/**
	 * only Yes and No two categories
	 * 
	 * @param label
	 * @return
	 * @throws Exception 
	 */
	public List<RawSentence> obtainTwoClassData(ILabel label) throws Exception {
		Set<String> positiveSet = this.getPostiveSents(label);
		if(positiveSet==null) throw new Exception();
		Iterator<String> allSentIter = sentLabels.keySet().iterator();
		List<RawSentence> rs = new ArrayList();
		while(allSentIter.hasNext()){
			String sent = allSentIter.next();
			if(positiveSet.contains(sent)) {
				rs.add(new RawSentence(sent,BinaryLabel.YES));
			}else{
				rs.add(new RawSentence(sent,BinaryLabel.NO));
			}
		}
		return rs;
	}
	
	
	public void outputTwoClns(String filePath){
		try {
			FileWriter fw = new FileWriter(filePath);
			Iterator<Entry<String, Set<ILabel>>> allSentIter = sentLabels.entrySet().iterator();
			while(allSentIter.hasNext()){
				Entry<String, Set<ILabel>> sent = allSentIter.next();
				Iterator<ILabel> sentLabels = sent.getValue().iterator();
				while(sentLabels.hasNext()){
					fw.write(LabelUtil.labelToString(sentLabels.next())+"\t"+sent.getKey()+"\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args){
		CharacterExampleGenerator chaSentsGenerator = new CharacterExampleGenerator();
		chaSentsGenerator.loadTwoClns("F:\\MicroPIE\\2017tasks\\training sentences\\150130-Training-Sentences-new-cleaned1201-2col-17039.txt");
		//System.out.println(chaSentsGenerator.sentLabels.size());
		chaSentsGenerator.loadCorrectedClns("F:\\MicroPIE\\2017tasks\\training sentences\\2017-0106_Firmicutes_predictions_CHECKED.txt");
		//System.out.println(chaSentsGenerator.sentLabels.size());
		chaSentsGenerator.loadCorrectedClns("F:\\MicroPIE\\2017tasks\\training sentences\\2017-0106_Halobacter_predictions_CHECKED_3c.txt");
		//System.out.println(chaSentsGenerator.sentLabels.size());
		chaSentsGenerator.loadCorrectedClns("F:\\MicroPIE\\2017tasks\\training sentences\\Firmicutes_Genomes_descriptions_122216_predictions_CHECKED_3c.txt");
		//System.out.println(chaSentsGenerator.sentLabels.size());
		chaSentsGenerator.loadCorrectedClns("F:\\MicroPIE\\2017tasks\\training sentences\\Firmicutes_NoGenomes_descriptions_122316_predictions_CHECKED_3c.txt");
		//System.out.println(chaSentsGenerator.sentLabels.size());
		chaSentsGenerator.loadCorrectedClns("F:\\MicroPIE\\2017tasks\\training sentences\\HalophilicArchaea_descriptions_122316_predictions_CHECKED_3c.txt");
		//System.out.println(chaSentsGenerator.labelSents.keySet().size());
		chaSentsGenerator.loadCorrectedClns("F:\\MicroPIE\\2017tasks\\training sentences\\Pathogen_Feb2017_predictions_checked_3c.txt");
		Iterator labels = chaSentsGenerator.labelSents.keySet().iterator();
		while(labels.hasNext()){
			ILabel label = (ILabel) labels.next();
			System.out.println(LabelUtil.labelToString(label)+" num = "+chaSentsGenerator.labelSents.get(label).size());
		}
		//chaSentsGenerator.outputTwoClns("F:\\MicroPIE\\2017tasks\\training sentences\\Final_training_sents_042417.txt");
	}

	
}
