package edu.arizona.biosemantics.micropie.transform.feature;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class FilterDecorator implements IFilterDecorator {
	
	private int nGramMinSize;
	private int nGramMaxSize;
	private int minFrequency;

	@Inject
	public FilterDecorator(@Named("FilterDecorator_NGramMinSize")int nGramMinSize, 
			@Named("FilterDecorator_NGramMaxSize")int nGramMaxSize, 
			@Named("FilterDecorator_MinFrequency")int minFrequency) {
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
		stringToWordVector.setAttributeIndices("first-last");
		//stringToWordVector.setAttributeIndicesArray(new int[] { 1 });
		stringToWordVector.setLowerCaseTokens(true);
		
		stringToWordVector.setOutputWordCounts(true);
		stringToWordVector.setWordsToKeep(Integer.MAX_VALUE);
		stringToWordVector.setMinTermFreq(minFrequency);
		stringToWordVector.setTFTransform(true); // text frequency
	
		multiFilter.setFilters(new Filter[] { stringToWordVector });
	}
}
