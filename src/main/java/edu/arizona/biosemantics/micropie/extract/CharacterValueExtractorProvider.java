package edu.arizona.biosemantics.micropie.extract;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.keyword.PhraseBasedExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellDiameterExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellLengthExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellScaleExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellWidthExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.FermentationSubstratesNotUsed;
import edu.arizona.biosemantics.micropie.extract.regex.GcExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GcFigureExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthNaclMaxExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthNaclMinExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthNaclOptimumExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthPhMaxExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthPhMinExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthPhOptimumExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthTempMaxExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthTempMinExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthTempOptimumExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.InorganicSubstancesNotUsedExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.OrganicCompoundsNotUsedOrNotHydrolyzedExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.PHTempNaClExtractor;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;


/**
 * provide extractors pertaining to the category label
 * 
 */
public class CharacterValueExtractorProvider implements ICharacterValueExtractorProvider {

	/**
	 * the <Label, Extractor> map
	 */
	private Map<ILabel, Set<ICharacterValueExtractor>> labelExtractorsMap = new 
			HashMap<ILabel, Set<ICharacterValueExtractor>>();

	@Inject
	/**
	 * Set<ICharacterValueExtractor> extractors is constructed at a former step
	 * @param extractors
	 */
	public CharacterValueExtractorProvider(Set<ICharacterValueExtractor> extractors,
			SentenceSpliter sentSplitter,
			PosTagger posTagger) {
		/*
		//Elvis's Method
		//Add additional more "customized" extractors than the universal keyword based one
		//extractors.add(new GcExtractor(sentSplitter, posTagger, Label.c1, "%G+C"));
		extractors.add(new GcExtractor(Label.c1, "%G+C"));
		extractors.add(new CellDiameterExtractor(Label.c3));
		extractors.add(new CellLengthExtractor(Label.c4));
		extractors.add(new CellWidthExtractor(Label.c5));

		extractors.add(new GrowthNaclMinExtractor(Label.c17));
		extractors.add(new GrowthNaclOptimumExtractor(Label.c18));
		extractors.add(new GrowthNaclMaxExtractor(Label.c19));
		
		extractors.add(new GrowthPhMinExtractor(Label.c20));
		extractors.add(new GrowthPhOptimumExtractor(Label.c21));
		extractors.add(new GrowthPhMaxExtractor(Label.c22));		
		
		
		extractors.add(new GrowthTempMinExtractor(Label.c23));
		extractors.add(new GrowthTempOptimumExtractor(Label.c24));
		extractors.add(new GrowthTempMaxExtractor(Label.c25));
		
		extractors.add(new OrganicCompoundsNotUsedOrNotHydrolyzedExtractor(Label.c52));
		extractors.add(new InorganicSubstancesNotUsedExtractor(Label.c54));
		extractors.add(new FermentationSubstratesNotUsed(Label.c56));
		
		for(ICharacterValueExtractor extractor : extractors) {
			if(!labelExtractorsMap.containsKey(extractor.getLabel()))
				labelExtractorsMap.put(extractor.getLabel(), new HashSet<ICharacterValueExtractor>());
			labelExtractorsMap.get(extractor.getLabel()).add(extractor);
			//System.out.println(extractor.getLabel()+" "+extractor.getClass().getName());
		}
		*/
		
		//My method
		for(Label label: Label.values()){
			if(!labelExtractorsMap.containsKey(label))
				labelExtractorsMap.put(label, new HashSet<ICharacterValueExtractor>());
		}
		
		//System.out.println(extractors+" "+extractors.size());
		extractors.add(new OrganicCompoundsNotUsedOrNotHydrolyzedExtractor(Label.c52));
		extractors.add(new InorganicSubstancesNotUsedExtractor(Label.c54));
		extractors.add(new FermentationSubstratesNotUsed(Label.c56));
		
		
		for(ICharacterValueExtractor extractor : extractors) {
			if(!labelExtractorsMap.containsKey(extractor.getLabel()))
				labelExtractorsMap.put(extractor.getLabel(), new HashSet<ICharacterValueExtractor>());
			labelExtractorsMap.get(extractor.getLabel()).add(extractor);
			
			//phrase based extractor
			if(extractor instanceof PhraseBasedExtractor){
				((PhraseBasedExtractor) extractor).setPosTagger(posTagger);
				((PhraseBasedExtractor) extractor).setSentSplitter(sentSplitter);
			}
		}
		
		// Figure Extraction Method
		ICharacterValueExtractor gcFigureExtractor = new GcFigureExtractor(sentSplitter, posTagger, Label.c1, "%G+C");
		ICharacterValueExtractor ptnFigureExtractor = new PHTempNaClExtractor(sentSplitter, posTagger, null, null);
		ICharacterValueExtractor cellScaleFigureExtractor = new CellScaleExtractor(sentSplitter, posTagger, null, null);
		
		labelExtractorsMap.get(Label.c1).add(gcFigureExtractor);
		labelExtractorsMap.get(Label.c3).add(cellScaleFigureExtractor);
		labelExtractorsMap.get(Label.c4).add(cellScaleFigureExtractor);
		labelExtractorsMap.get(Label.c5).add(cellScaleFigureExtractor);
		
		labelExtractorsMap.get(Label.c17).add(ptnFigureExtractor);
		labelExtractorsMap.get(Label.c18).add(ptnFigureExtractor);
		labelExtractorsMap.get(Label.c19).add(ptnFigureExtractor);
		labelExtractorsMap.get(Label.c20).add(ptnFigureExtractor);
		labelExtractorsMap.get(Label.c21).add(ptnFigureExtractor);
		labelExtractorsMap.get(Label.c22).add(ptnFigureExtractor);
		labelExtractorsMap.get(Label.c23).add(ptnFigureExtractor);
		labelExtractorsMap.get(Label.c24).add(ptnFigureExtractor);
		labelExtractorsMap.get(Label.c25).add(ptnFigureExtractor);
		
		/*
		for(Label label: Label.values()){
			System.out.println(label+","+ labelExtractorsMap.get(label).size());
		}
		*/
	}
	
	@Override
	public Set<ICharacterValueExtractor> getContentExtractor(Label label) {
		if(labelExtractorsMap.containsKey(label))
			return labelExtractorsMap.get(label);
		return new HashSet<ICharacterValueExtractor>();
	}

	@Override
	public boolean hasExtractor(Label label) {
		return !this.getContentExtractor(label).isEmpty();
	}

}
