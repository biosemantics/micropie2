package edu.arizona.biosemantics.micropie.transform;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.InvalidFormatException;
import edu.arizona.biosemantics.common.log.LogLevel;

public class OpenNLPWrapper {
	/**
	 * split sentences with given OpenNLP model
	 * @param sentences
	 * @param sentenceModel
	 * @return
	 * @throws FileNotFoundException
	 */
	public List<String> splitSentencesByModel(List<String> sentences, String sentenceModel) throws FileNotFoundException {
	//public List<String> getSentencesOpennlp(List<String> sentences, String opennlpSentDetectorSource) throws FileNotFoundException {
		log(LogLevel.INFO, "ssplit3:: split text to sentences using Opennlp sentence detector...");

		List<String> result = new LinkedList<String>();		

		InputStream modelIn = new FileInputStream(sentenceModel);
		try {
			
			SentenceModel model = new SentenceModel(modelIn);
			SentenceDetectorME sdetector = new SentenceDetectorME(model);
			for (String sentence : sentences) {
				String subSentences[] = sdetector.sentDetect(sentence);
				for ( int i = 0; i < subSentences.length; i++ ) {
					String subSent = subSentences[i];
					result.add(subSent);
				}
			}
			modelIn.close();		  
		}
		catch (IOException e) {
		  e.printStackTrace();
		}
		finally {
		  if (modelIn != null) {
		    try {
		      modelIn.close();
		    }
		    catch (IOException e) {
		    }
		  }
		}
		log(LogLevel.INFO, "done ssplit3:: splitting text to sentences using Opennlp sentence detector. Created " + result.size() + " sentences");
		return result;
	}	
	
	private void openNlpSentSplitter(String source) throws InvalidFormatException, IOException {
		// This is just for testing
		String paragraph = "Hi. How are you? This is Mike. This is Elvis A. A cat in the hat. The type strain is KOPRI 21160T (= KCTC 23670T= JCM 18092T), isolated from a soil sample collected near the King Sejong Station on King George Island, Antarctica. The DNA G+ C content of the type strain is 30.0 mol%.";

		InputStream modelIn = new FileInputStream(source);

		try {
		    // SentenceModel model = new SentenceModel(modelIn);
			// InputStream is = new FileInputStream(myConfiguration.getOpenNLPTokenizerDir());
			
			SentenceModel model = new SentenceModel(modelIn);
			SentenceDetectorME sdetector = new SentenceDetectorME(model);
			String sentences[] = sdetector.sentDetect(paragraph);
			for ( int i = 0; i < sentences.length; i++ ) {
				System.out.println(sentences[i]);
			}
			modelIn.close();		  
		}
		catch (IOException e) {
		  e.printStackTrace();
		}
		finally {
		  if (modelIn != null) {
		    try {
		      modelIn.close();
		    }
		    catch (IOException e) {
		    }
		  }
		}
	}
}
