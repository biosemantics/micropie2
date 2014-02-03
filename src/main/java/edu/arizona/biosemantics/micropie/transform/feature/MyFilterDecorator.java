package edu.arizona.biosemantics.micropie.transform.feature;

import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class MyFilterDecorator implements IFilterDecorator {
	
	private int nGramMinSize;
	private int nGramMaxSize;
	private int minFrequency;

	public MyFilterDecorator(int nGramMinSize, int nGramMaxSize, 
			int minFrequency) {
		this.nGramMinSize = nGramMinSize;
		this.nGramMaxSize = nGramMaxSize;
		this.minFrequency = minFrequency;
	}
	
	@Override
	public void decorateFilter(MultiFilter multiFilter) {
		StringToWordVector stringToWordVector = new StringToWordVector();
		NGramTokenizer tokenizer = new NGramTokenizer();
		tokenizer.setNGramMaxSize(nGramMaxSize);
		tokenizer.setNGramMinSize(nGramMinSize);
		tokenizer.setDelimiters(" ");
		stringToWordVector.setTokenizer(tokenizer);
		stringToWordVector.setAttributeIndicesArray(new int[] { 1 });
		stringToWordVector.setLowerCaseTokens(true);
		
		stringToWordVector.setOutputWordCounts(true);
		stringToWordVector.setWordsToKeep(Integer.MAX_VALUE);
		stringToWordVector.setMinTermFreq(minFrequency);
		stringToWordVector.setTFTransform(true); // text frequency
	
		multiFilter.setFilters(new Filter[] { stringToWordVector });
	}
}
