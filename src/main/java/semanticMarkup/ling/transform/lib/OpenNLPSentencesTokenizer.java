package semanticMarkup.ling.transform.lib;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.InvalidFormatException;
import semanticMarkup.ling.Token;
import semanticMarkup.ling.transform.ITokenizer;


public class OpenNLPSentencesTokenizer implements ITokenizer{
	private SentenceDetectorME mySentenceDetector;
	
	public  OpenNLPSentencesTokenizer (String openNLPSentenceDetectorDir) {
		InputStream sentModelIn;

		try {
			sentModelIn = new FileInputStream(openNLPSentenceDetectorDir);
			SentenceModel model = new SentenceModel(sentModelIn);
			this.mySentenceDetector = new SentenceDetectorME(model);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public List<Token> tokenize(String text) {
		String[] sentenceArray = this.mySentenceDetector.sentDetect(text);
		List<Token> sentences = new LinkedList<Token>();
		for (String sentence: sentenceArray) {
			Token sentenceElement = new Token(sentence);
			sentences.add(sentenceElement);
		}
		
		return sentences;
	}

}
