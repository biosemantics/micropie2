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

import com.google.inject.Inject;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


/**
 * wrap simple standford parser functions
 * @author maojin
 *
 */
public class StanfordWrapper {
	
	private StanfordCoreNLP sfCoreNLP;
	
	@Inject
	public StanfordWrapper(StanfordCoreNLP sfCoreNLP){
		this.sfCoreNLP = sfCoreNLP;
	}
	
	/**
	 * tokenize the string and convert the results to a new string
	 * @param oriSent
	 * @return
	 */
	public String tokenizer2Str(String str) {
		String tokenStr = null;
		StringBuffer returnString = new StringBuffer();
				
		Annotation annotation = new Annotation(str);
		this.sfCoreNLP.annotate(annotation);
		List<CoreMap> sentenceAnnotations = annotation.get(SentencesAnnotation.class);
		
		if (sentenceAnnotations.size() > 0) {
			for (CoreMap sentenceAnnotation : sentenceAnnotations) {
				// result.add(sentenceAnnotation.toString());
				for (CoreLabel token : sentenceAnnotation.get(TokensAnnotation.class)) {
					returnString.append(" ").append(token);
				}
			}
			tokenStr = returnString.substring(1);	
		} else {
			tokenStr = str;
		}
		
		return tokenStr;
	}

	

	public void annotate(Annotation annotation) {
		this.sfCoreNLP.annotate(annotation);
	}
	
	/**
	 * split text to sentences using stanford corenlp pipeline...
	 * @param text
	 * @return
	 */
	public List<String> getSentences(String text) {
		List<String> result = new LinkedList<String>();
		Annotation annotation = new Annotation(text);
		this.sfCoreNLP.annotate(annotation);
		List<CoreMap> sentenceAnnotations = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentenceAnnotation : sentenceAnnotations) {
			result.add(sentenceAnnotation.toString());
		}
		return result;
	}
	
	
	
}
