package edu.arizona.biosemantics.micropie.extract;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.context.RelationParser;
import edu.arizona.biosemantics.micropie.extract.crf.FeatureRender;
import edu.arizona.biosemantics.micropie.extract.crf.GeoPredictor;
import edu.arizona.biosemantics.micropie.extract.keyword.AntibioticPhraseExtractor;
import edu.arizona.biosemantics.micropie.extract.keyword.HabitatIsolatedFromExtractor;
import edu.arizona.biosemantics.micropie.extract.keyword.PhraseBasedExtractor;
import edu.arizona.biosemantics.micropie.extract.keyword.SalinityPreferenceExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.AntibioticSyntacticExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellDiameterExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellLengthExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellScaleExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellWidthExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.FermentationSubstratesNotUsed;
import edu.arizona.biosemantics.micropie.extract.keyword.FermentationProductExtractor;
import edu.arizona.biosemantics.micropie.extract.keyword.OrganicCompoundExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GeographicLocationExtractor;
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
import edu.arizona.biosemantics.micropie.extract.regex.PigmentCompoundAbsorptionExtractor;
import edu.arizona.biosemantics.micropie.nlptool.PhraseParser;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.arizona.biosemantics.micropie.nlptool.StanfordParserWrapper;


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
			PosTagger posTagger,
			StanfordParserWrapper stanfordWrapper,
			FeatureRender featureRender,
			@Named("sensitiveTerms")Set<String> sensitiveTerms,
			@Named("sensitivePatterns")Set<String> sensitivePatterns,
			@Named("resistantTerms")Set<String> resistantTerms,
			@Named("resistantPatterns")Set<String> resistantPatterns) {
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
			if(!labelExtractorsMap.containsKey(label)){
				labelExtractorsMap.put(label, new HashSet<ICharacterValueExtractor>());
			}
				
		}
		
		
		extractors.add(new GeoPredictor(Label.c31, "Geographic location",featureRender));
		
		//System.out.println("initializing new characters "+extractors.size());
		//extractors.add(new OrganicCompoundsNotUsedOrNotHydrolyzedExtractor(Label.c52));
		//extractors.add(new InorganicSubstancesNotUsedExtractor(Label.c54));
		//extractors.add(new FermentationSubstratesNotUsed(Label.c56));
		/**/
		PhraseParser phraseParser = new PhraseParser();
		RelationParser phraseRelationParser = new RelationParser();
		
		extractors.add(new AntibioticSyntacticExtractor(Label.c32, "Antibiotic sensitivity",sensitivePatterns,sentSplitter,stanfordWrapper));
		//extractors.add(new AntibioticPhraseExtractor(Label.c32, "Antibiotic sensitivity", posTagger, phraseParser,phraseRelationParser, sentSplitter, sensitiveTerms));
		extractors.add(new AntibioticSyntacticExtractor(Label.c33, "Antibiotic resistant",resistantPatterns,sentSplitter,stanfordWrapper));
		//extractors.add(new AntibioticPhraseExtractor(Label.c33, "Antibiotic sensitivity", posTagger, phraseParser,phraseRelationParser, sentSplitter, resistantTerms));

		
		FermentationProductExtractor  fermentationExtractor =  null;
		Set<String> inorganicWords = null;
		//convert to a label-extractor map
		for(ICharacterValueExtractor extractor : extractors) {
			if(!labelExtractorsMap.containsKey(extractor.getLabel()))
				labelExtractorsMap.put(extractor.getLabel(), new HashSet<ICharacterValueExtractor>());
			if(extractor!=null){
				labelExtractorsMap.get(extractor.getLabel()).add(extractor);
			}
			/**/
			//phrase based extractor
			if(extractor instanceof SalinityPreferenceExtractor){
				((SalinityPreferenceExtractor) extractor).setPosTagger(posTagger);
				((SalinityPreferenceExtractor) extractor).setPhraseParser(phraseParser);
				//((PhraseBasedExtractor) extractor).setSentSplitter(sentSplitter);
			}else if(extractor instanceof edu.arizona.biosemantics.micropie.extract.keyword.HabitatIsolatedFromExtractor){
				((HabitatIsolatedFromExtractor) extractor).setPosTagger(posTagger);
				((HabitatIsolatedFromExtractor) extractor).setPhraseParser(phraseParser);
				((HabitatIsolatedFromExtractor) extractor).setSentSplitter(sentSplitter);
				((HabitatIsolatedFromExtractor) extractor).setRelationParser(phraseRelationParser);
				((HabitatIsolatedFromExtractor) extractor).setStanParser(stanfordWrapper);
			}else if(extractor instanceof edu.arizona.biosemantics.micropie.extract.keyword.FermentationProductExtractor){
				((FermentationProductExtractor) extractor).setPosTagger(posTagger);
				((FermentationProductExtractor) extractor).setPhraseParser(phraseParser);
				((FermentationProductExtractor) extractor).setSentSplitter(sentSplitter);
				((FermentationProductExtractor) extractor).setRelationParser(phraseRelationParser);
				((FermentationProductExtractor) extractor).setStanParser(stanfordWrapper);
				fermentationExtractor = (FermentationProductExtractor) extractor;
			}else if(extractor instanceof edu.arizona.biosemantics.micropie.extract.keyword.OrganicCompoundExtractor){
				((OrganicCompoundExtractor) extractor).setPosTagger(posTagger);
				((OrganicCompoundExtractor) extractor).setPhraseParser(phraseParser);
				((OrganicCompoundExtractor) extractor).setSentSplitter(sentSplitter);
				((OrganicCompoundExtractor) extractor).setStanParser(stanfordWrapper);
				((OrganicCompoundExtractor) extractor).setPhraseRelationParser(phraseRelationParser);
			}else if(extractor instanceof edu.arizona.biosemantics.micropie.extract.regex.GeographicLocationExtractor){
					((GeographicLocationExtractor) extractor).setStanParser(stanfordWrapper);
			}else if(extractor instanceof edu.arizona.biosemantics.micropie.extract.keyword.AntibioticPhraseExtractor){
				((AntibioticPhraseExtractor) extractor).setPosTagger(posTagger);
				((AntibioticPhraseExtractor) extractor).setPhraseParser(phraseParser);
				((AntibioticPhraseExtractor) extractor).setSentSplitter(sentSplitter);
				((AntibioticPhraseExtractor) extractor).setPhraseRelationParser(phraseRelationParser);
				if(Label.c32.equals(extractor.getLabel())){
					((AntibioticPhraseExtractor) extractor).setSensTypeKeywords(sensitiveTerms);
				}else{
					((AntibioticPhraseExtractor) extractor).setSensTypeKeywords(resistantTerms);
				}
			}else if(extractor instanceof PhraseBasedExtractor){//put in the last
				((PhraseBasedExtractor) extractor).setPosTagger(posTagger);
				((PhraseBasedExtractor) extractor).setPhraseParser(phraseParser);
			}
		}
		/**/
		//((PhraseBasedExtractor) extractor).setSentSplitter(sentSplitter);
		PhraseBasedExtractor inExtractor = (PhraseBasedExtractor)labelExtractorsMap.get(Label.c55).iterator().next();
		inorganicWords = inExtractor.getKeywords();
		for(ICharacterValueExtractor extractor : extractors) {
			 if(extractor instanceof edu.arizona.biosemantics.micropie.extract.keyword.FermentationProductExtractor){
				fermentationExtractor = (FermentationProductExtractor) extractor;
				fermentationExtractor.setInorganicWords(inorganicWords);
			 }
		}
		// Figure Extraction Method
		ICharacterValueExtractor gcFigureExtractor = new GcFigureExtractor(sentSplitter, posTagger, Label.c1, "%G+C");
		ICharacterValueExtractor ptnFigureExtractor = new PHTempNaClExtractor(sentSplitter, posTagger, null, "PHTempNacl");
		ICharacterValueExtractor cellScaleFigureExtractor = new CellScaleExtractor(sentSplitter, posTagger, null, "CellScale");
		PigmentCompoundAbsorptionExtractor pcAbsoExtractor = new PigmentCompoundAbsorptionExtractor(posTagger, Label.c12, "Pigment Compounds", null, null);
		pcAbsoExtractor.setPhraseParser(phraseParser);
		
		labelExtractorsMap.get(Label.c1).add(gcFigureExtractor);
		labelExtractorsMap.get(Label.c2).add(cellScaleFigureExtractor);//some values of cell shapes orrcur in this character sentences.
		labelExtractorsMap.get(Label.c3).add(cellScaleFigureExtractor);
		labelExtractorsMap.get(Label.c4).add(cellScaleFigureExtractor);
		labelExtractorsMap.get(Label.c5).add(cellScaleFigureExtractor);
		
		labelExtractorsMap.get(Label.c12).add(pcAbsoExtractor);
		
		//note the mapping
		labelExtractorsMap.get(Label.c18).add(ptnFigureExtractor);
		labelExtractorsMap.get(Label.c19).add(ptnFigureExtractor);
		labelExtractorsMap.get(Label.c20).add(ptnFigureExtractor);
		labelExtractorsMap.get(Label.c21).add(ptnFigureExtractor);
		labelExtractorsMap.get(Label.c22).add(ptnFigureExtractor);
		labelExtractorsMap.get(Label.c23).add(ptnFigureExtractor);
		labelExtractorsMap.get(Label.c24).add(ptnFigureExtractor);
		labelExtractorsMap.get(Label.c25).add(ptnFigureExtractor);
		labelExtractorsMap.get(Label.c26).add(ptnFigureExtractor);
		
		
		if(fermentationExtractor!=null){
			labelExtractorsMap.get(Label.c41).add(fermentationExtractor);
			labelExtractorsMap.get(Label.c42).add(fermentationExtractor);
			labelExtractorsMap.get(Label.c53).add(fermentationExtractor);
			labelExtractorsMap.get(Label.c54).add(fermentationExtractor);
		}
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


	@Override
	public Set<ICharacterValueExtractor> getAllContentExtractor() {
		Set<ICharacterValueExtractor> allExtractor = new HashSet();
		Iterator<Entry<ILabel, Set<ICharacterValueExtractor>>> entryIter = labelExtractorsMap.entrySet().iterator();
		while(entryIter.hasNext()){
			Entry<ILabel, Set<ICharacterValueExtractor>> entry = entryIter.next();
			allExtractor.addAll(entry.getValue());
		}
		return allExtractor;
	}


}
