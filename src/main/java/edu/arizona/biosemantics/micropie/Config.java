package edu.arizona.biosemantics.micropie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.classify.CategoryLabel;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.classify.LabelPhraseValueType;
import edu.arizona.biosemantics.micropie.extract.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.extract.CharacterValueExtractorProvider;
import edu.arizona.biosemantics.micropie.extract.CharacterValueExtractorReader;
import edu.arizona.biosemantics.micropie.extract.ICharacterBatchExtractor;
import edu.arizona.biosemantics.micropie.extract.ICharacterValueExtractor;
import edu.arizona.biosemantics.micropie.extract.ICharacterValueExtractorProvider;
import edu.arizona.biosemantics.micropie.extract.keyword.AntibioticPhraseExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.AntibioticSyntacticExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellDiameterExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellLengthExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellShapeExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellSizeExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellWidthExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.FermentationProductsExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.FermentationSubstratesNotUsed;
import edu.arizona.biosemantics.micropie.extract.regex.GcExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GcFigureExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthNaclMaxExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthNaclMinExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthNaclOptimumExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthPhExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthPhMaxExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthPhMinExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthPhOptimumExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthTempMaxExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthTempMinExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthTempOptimumExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.InorganicSubstancesNotUsedExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.OrganicCompoundsNotUsedOrNotHydrolyzedExtractor;
import edu.arizona.biosemantics.micropie.extract.usp.AntibioticSensitivityExtractor;
import edu.arizona.biosemantics.micropie.io.CSVAbbreviationReader;
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.io.CharacterReader;
import edu.arizona.biosemantics.micropie.io.ICharacterValueExtractorReader;
import edu.arizona.biosemantics.micropie.io.ISentenceReader;
import edu.arizona.biosemantics.micropie.io.KeywordReader;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.nlptool.ITextNormalizer;
import edu.arizona.biosemantics.micropie.nlptool.PosTagger;
import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;
import edu.arizona.biosemantics.micropie.nlptool.TextNormalizer;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;


/**
 * The read main Class
 *
 */
public class Config extends AbstractModule {


	// private String characterListString = "16S rRNA accession #|Family|Genus|Species|Strain|Genome size|%G+C|Other genetic characteristics|Cell shape|Pigments|Cell wall|Motility|Biofilm formation|Habitat isolated from|Oxygen Use|Salinity preference|pH minimum|pH optimum|pH maximum|Temperature minimum|Temperature optimum|Temperature maximum|NaCl minimum|NaCl optimum|NaCl maximum|Host|Symbiotic|Pathogenic|Disease Caused|Metabolism (energy & carbon source)|Carbohydrates (mono & disaccharides)|Polysaccharides|Amino Acids|Alcohols|Fatty Acids|Other Energy or Carbon Sources|Fermentation Products|Polyalkanoates (plastics)|Other Metabolic Product|Antibiotic Sensitivity|Antibiotic Resistant|Cell Diameter|Cell Long|Cell Wide|Cell Membrane & Cell Wall Components|External features|Filterability|Internal features|Lysis Susceptibility|Physiological requirements|Antibiotics|Secreted Products|Storage Products|Tests|Pathogen Target Organ|Complex Mixtures|Inorganic|Metals|Nitrogen Compounds|Organic|Organic Acids|Other";
	
	// Verison 2, March 08, 2015 Sunday
	// running parameter characters to be extracted
	//private String characterListString = "%G+C|Cell shape|Cell diameter|Cell length|Cell width|Cell relationships&aggregations|Gram stain type|Cell membrane & cell wall components|External features|Internal features|Motility|Pigment compounds|Biofilm formation|Filterability|Lysis susceptibility|Habitat isolated from|NaCl minimum|NaCl optimum|NaCl maximum|pH minimum|pH optimum|pH maximum|Temperature minimum|Temperature optimum|Temperature maximum|Pressure preference |Aerophilicity|Magnesium requirement for growth|Vitamins and Cofactors required for growth|Antibiotic sensitivity|Antibiotic resistant|Antibiotic production|Colony shape |Colony margin|Colony texture|Colony color |Film test result|Spot test result|Fermentation Products|Antibiotic production|Methanogenesis products|Other Metabolic Product|Tests positive|Tests negative|Symbiotic relationship|Host|Pathogenic|Disease caused|Pathogen target Organ|Haemolytic&haemadsorption properties|organic compounds used or hydrolyzed|organic compounds not used or not hydrolyzed|inorganic substances used|inorganic substances not used|fermentation substrates used|fermentation substrates not used|Other genetic characteristics|Other physiological characteristics";
	private String characterListString = "%G+C|Cell shape|Cell diameter|Cell length|Cell width|Cell relationships&aggregations|Gram stain type|Cell membrane & cell wall components|External features|Internal features|Motility|Pigment compounds|Biofilm formation|Filterability|Lysis susceptibility|Cell division pattern & reproduction|Salinity preference|Habitat isolated from|NaCl minimum|NaCl optimum|NaCl maximum|pH minimum|pH optimum|pH maximum|Temperature minimum|Temperature optimum|Temperature maximum|Pressure preference |Aerophilicity|Magnesium requirement for growth|Vitamins and Cofactors required for growth|Antibiotic sensitivity|Antibiotic resistant|Antibiotic production|Colony shape |Colony margin|Colony texture|Colony color |Film test result|Spot test result|Fermentation Products|Antibiotic production|Methanogenesis products|Other Metabolic Product|Tests positive|Tests negative|Symbiotic relationship|Host|Pathogenic|Disease caused|Pathogen target Organ|Haemolytic&haemadsorption properties|organic compounds used or hydrolyzed|organic compounds not used or not hydrolyzed|inorganic substances used|inorganic substances not used|fermentation substrates used|fermentation substrates not used";
	private String ouputCharacterListString = "%G+C|Cell shape|Cell length|Cell width|Cell relationships&aggregations|Gram stain type|External features|Internal features|Motility|Pigment compounds|Salinity preference|NaCl minimum|NaCl optimum|NaCl maximum|pH minimum|pH optimum|pH maximum|Temperature minimum|Temperature optimum|Temperature maximum|Aerophilicity|Magnesium requirement for growth|Vitamins and Cofactors required for growth|Antibiotic sensitivity|Antibiotic resistant|Colony shape |Colony margin|Colony texture|Colony color|Fermentation Products|Other Metabolic Product|Pathogenic|Disease caused|Pathogen target Organ|Haemolytic&haemadsorption properties|organic compounds used or hydrolyzed|organic compounds not used or not hydrolyzed|inorganic substances used|inorganic substances not used|fermentation substrates used|fermentation substrates not used";
	
	private String serializedClassifierModel = "nlpmodel/english.all.3class.distsim.crf.ser.gz";
	

	private String celsius_degreeReplaceSourcePattern = "(" +
			"\\s?˚C\\s?|" +
			"\\s?˚ C\\s?|" +
			"\\s?\"C\\s?|" +
			"\\s?\" C\\s?|" +
			"\\s?◦C\\s?|" +
			"\\s?◦ C\\s?|" +
			"\\s?°C\\s?|" +
			"\\s?° C\\s?|" +
			"\\s?\\”C\\s?|" +
			"\\s?\\” C\\s?|" +
			"\\s?u C\\s?" +
			")";
	
	

	/** INPUT DATA **/
	// private String trainingFile = "split-training-base-140310.csv";
	
	// private String trainingFile = "training_data/split-training-base-140603.csv";
	// split-training-base-150110.csv
	private String trainingFile = "training_data/split-training-base-150110.csv";
	private String trainedModelFile = "character_model";//predict the characters
	private String categoryModelFile = "category_model";//predict the categories
	
	//System Parameters
	//private String svmLabelAndCategoryMappingFile = "svmlabelandcategorymapping/categoryMapping_poster.txt";
	private String svmLabelAndCategoryMappingFile = "svmlabelandcategorymapping/categoryMapping_micropie1.5.txt";
	private String firstLevelCategoryMappingFile = "svmlabelandcategorymapping/categoryMapping_category.txt";
	private String labelValutypeFile ="svmlabelandcategorymapping/character_valuetype.txt";
	//private String svmLabelAndCategoryMappingFile = "svmlabelandcategorymapping_data/SVMLabelAndCategoryMapping.txt";
	
	private Set antiResistantTerms = null;
	private Set antiResistantPatterns = null;
	private Set antiSensPatterns = null;
	private Set antiSensTerms = null;
	
	private String testFolder = "input";
	private String characterValueExtractorsFolder = "CharacterValueExtractors0.2.0";
	private String abbreviationFile = "abbrevlist/abbrevlist.csv";
	private String resFolder = "res";
	private String kbFolder = "kb";
	private String dataHolderFolder = "dataholder";

	// private String uspBaseString = "usp_small_test";
	// private String uspBaseString = "usp_base_new";
	private String uspBaseString = "job1_usp";
	private String uspResultsDirectory = "job1_usp_results";
	private String uspFolder = "job1_usp/dep/0";
	private String uspString= "job1_usp";
	
	
	// => don't useFolder anymore !!
	
	private String uspBaseZipFileName = "usp_base.zip";
	
	private int nGramMinSize = 1;
	private int nGramMaxSize = 1;
	private int nGramMinFrequency = 1;
	private String nGramTokenizerOptions = "-delimiters ' ' -max 1 -min 1";
	private String stringToWordVectorOptions = "-W " + Integer.MAX_VALUE + " -T -L -M 1 -tokenizer weka.core.tokenizer.NGramTokenizer " + nGramTokenizerOptions + "";
	private String multiFilterOptions = "-D -F weka.filters.unsupervised.attribute.StringToWordVector " + stringToWordVectorOptions + "";
	private String libSVMOptions = "-h 0 -S 0 -D 3 -K 2 -G 0 -R 0 -N 0.5 -M 100 -C 2048 -P 1e-3";
	
	/** OUTPUT DATA **/
	private String predicitonsFile = "predictions.csv";
	private String matrixFile = "matrix.csv";
	
	
	/** prarallel PROCESSING param **/
	private boolean parallelProcessing = true;
	private int maxThreads = 3;
	
	
	
	
			
			
	@Override
	protected void configure() {
		
		bind(new TypeLiteral<LinkedHashSet<String>>() {}).annotatedWith(Names.named("Characters"))
			.toProvider(new Provider<LinkedHashSet<String>>() {
				@Override
				public LinkedHashSet<String> get() {
					return new LinkedHashSet<String>(Arrays.asList(characterListString.split("\\|")));
				}
		}).in(Singleton.class);
		
		bind(new TypeLiteral<LinkedHashSet<String>>() {}).annotatedWith(Names.named("OutputCharacters"))
		.toProvider(new Provider<LinkedHashSet<String>>() {
			@Override
			public LinkedHashSet<String> get() {
				return new LinkedHashSet<String>(Arrays.asList(ouputCharacterListString.split("\\|")));
			}
		}).in(Singleton.class);
		
		

		bind(String.class).annotatedWith(Names.named("resFolder")).toInstance(resFolder);
		bind(String.class).annotatedWith(Names.named("kbFolder")).toInstance(kbFolder);
		bind(String.class).annotatedWith(Names.named("dataHolderFolder")).toInstance(dataHolderFolder);
		
		bind(String.class).annotatedWith(Names.named("celsius_degreeReplaceSourcePattern")).toInstance(
				celsius_degreeReplaceSourcePattern);
		
		bind(String.class).annotatedWith(Names.named("uspBaseString")).toInstance(
				uspBaseString);
		
		bind(String.class).annotatedWith(Names.named("uspBaseZipFileName")).toInstance(
				uspBaseZipFileName);

		
		
		bind(String.class).annotatedWith(Names.named("uspString")).toInstance(
				uspString);
		
		bind(String.class).annotatedWith(Names.named("uspResultsDirectory")).toInstance(uspResultsDirectory);
		
		
		/*********************************    sentence classifiers                ******************************/
		
		bind(String.class).annotatedWith(Names.named("trainingFile")).toInstance(
				trainingFile);
		bind(String.class).annotatedWith(Names.named("trainedModelFile")).toInstance(
				trainedModelFile);
		bind(String.class).annotatedWith(Names.named("categoryModelFile")).toInstance(
				categoryModelFile);
		
		
		
		/*********************************    configure the categoryMapping          ******************************/
		bind(String.class).annotatedWith(Names.named("svmLabelAndCategoryMappingFile")).toInstance(
				svmLabelAndCategoryMappingFile);
		
		CharacterReader characterReader = new CharacterReader();
		characterReader.setCategoryFile(svmLabelAndCategoryMappingFile);
		characterReader.read();
		
		bind(new TypeLiteral<Map<ILabel, String>>(){}).annotatedWith(Names.named("labelCategoryCodeMap"))
			.toInstance(characterReader.getLabelCategoryCodeMap());
		bind(new TypeLiteral<Map<String, ILabel>>(){}).annotatedWith(Names.named("categoryCodeLabelMap"))
			.toInstance(characterReader.getCategoryCodeLabelMap());
		bind(new TypeLiteral<Map<ILabel, String>>(){}).annotatedWith(Names.named("labelCategoryNameMap"))
			.toInstance(characterReader.getLabelCategoryNameMap());
		bind(new TypeLiteral<Map<String, ILabel>>(){}).annotatedWith(Names.named("categoryNameLabelMap"))
			.toInstance(characterReader.getCategoryNameLabelMap());
		bind(new TypeLiteral<Map<String, ILabel>>(){}).annotatedWith(Names.named("categoryNameLabelMap"))
		.toInstance(characterReader.getCategoryNameLabelMap());
		
		//character labels
		bind(new TypeLiteral<List<ILabel>>() {}).annotatedWith(Names.named("MultiSVMClassifier_Labels"))
		.toProvider(new Provider<List<ILabel>>() {
			@Override
			public List<ILabel> get() {
				Label[] labels = Label.values();
				List<ILabel> result = new ArrayList<ILabel>(labels.length);
				for(Label label : labels)
					result.add(label);
				return result;
			}
		});
		
		//category labels
		bind(new TypeLiteral<List<ILabel>>() {}).annotatedWith(Names.named("Category_Labels"))
		.toProvider(new Provider<List<ILabel>>() {
			@Override
			public List<ILabel> get() {
				ILabel[] labels = CategoryLabel.values();
				List<ILabel> result = new ArrayList<ILabel>(labels.length);
				for(ILabel label : labels)
					result.add(label);
				return result;
			}
		});
		
		
		
		
		/*********************************    configure the extractors          ******************************/
		
		
		bind(String.class).annotatedWith(Names.named("testFolder")).toInstance(
				testFolder);
		
		bind(String.class).annotatedWith(Names.named("uspFolder")).toInstance(
				uspFolder);
		
		bind(String.class).annotatedWith(Names.named("characterValueExtractorsFolder")).toInstance(
				characterValueExtractorsFolder);
		
		bind(String.class).annotatedWith(Names.named("abbreviationFile")).toInstance(
				abbreviationFile);
		
		bind(String.class).annotatedWith(Names.named("predictionsFile")).toInstance(
				predicitonsFile);
		
		bind(String.class).annotatedWith(Names.named("matrixFile")).toInstance(
				matrixFile);
		
		bind(Integer.class).annotatedWith(Names.named("FilterDecorator_NGramMinSize"))
				.toInstance(nGramMinSize);
		
		bind(Integer.class).annotatedWith(Names.named("FilterDecorator_NGramMaxSize"))
				.toInstance(nGramMaxSize);
		
		bind(Integer.class).annotatedWith(Names.named("FilterDecorator_MinFrequency"))
				.toInstance(nGramMinFrequency);
		
		bind(Boolean.class).annotatedWith(Names.named("parallelProcessing")).toInstance(
				parallelProcessing);
		
		bind(Integer.class).annotatedWith(Names.named("maxThreads")).toInstance(
				maxThreads);
		
		bind(String.class).annotatedWith(Names.named("MultiFilterOptions")).toInstance(multiFilterOptions);
		
		bind(String.class).annotatedWith(Names.named("LibSVMOptions")).toInstance(libSVMOptions);
				
		bind(new TypeLiteral<Set<String>>(){}).annotatedWith(Names.named("sensitiveTerms")).toInstance(getAntibioticSensitivityTerms());
		bind(new TypeLiteral<Set<String>>(){}).annotatedWith(Names.named("sensitivePatterns")).toInstance(getAntibioticSensitivityPatterns());
		bind(new TypeLiteral<Set<String>>(){}).annotatedWith(Names.named("resistantTerms")).toInstance(getAntibioticResistantTerms());
		bind(new TypeLiteral<Set<String>>(){}).annotatedWith(Names.named("resistantPatterns")).toInstance(getAntibioticResistantPatterns());
		
		/*********************************    NLP Tools                ******************************/
		bind(ISentenceReader.class).to(CSVSentenceReader.class).in(Singleton.class);
		
		bind(ITextNormalizer.class).to(TextNormalizer.class);
		
		bind(StanfordCoreNLP.class).annotatedWith(Names.named("TokenizeSSplit")).toProvider(new Provider<StanfordCoreNLP>() {
			@Override
			public StanfordCoreNLP get() {
				Properties stanfordCoreProperties = new Properties();
				stanfordCoreProperties.put("annotators", "tokenize, ssplit");
				return new StanfordCoreNLP(stanfordCoreProperties);
			}
		}).in(Singleton.class);
		
		bind(StanfordCoreNLP.class).annotatedWith(Names.named("TokenizeSSplitPosParse")).toProvider(new Provider<StanfordCoreNLP>() {
			@Override
			public StanfordCoreNLP get() {
				Properties stanfordCoreProperties = new Properties();
				stanfordCoreProperties.put("annotators", "tokenize, ssplit, pos, parse");
				return new StanfordCoreNLP(stanfordCoreProperties);
			}
		});
		
		
		//Standford Parser
		bind(String.class).annotatedWith(Names.named("pos_model_file")).toInstance("edu/stanford/nlp/models/pos-tagger/english-bidirectional/english-bidirectional-distsim.tagger");//english-bidirectional-distsim
		bind(PosTagger.class).in(Singleton.class);
		bind(LexicalizedParser.class).toProvider(new Provider<LexicalizedParser>() {
			@Override
			public LexicalizedParser get() {
				return LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
			}
		}).in(Singleton.class);
		
		
		bind(AbstractSequenceClassifier.class).annotatedWith(Names.named("nerClassifier")).toProvider(new Provider<AbstractSequenceClassifier>() {
			@Override
			public AbstractSequenceClassifier get() {
				return CRFClassifier.getClassifierNoExceptions(serializedClassifierModel);
			}
		}).in(Singleton.class);
		
		
		bind(new TypeLiteral<TokenizerFactory<CoreLabel>>() {}).toProvider(new Provider<TokenizerFactory<CoreLabel>>() {
			@Override
			public TokenizerFactory<CoreLabel> get() {
				return PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
			}
		}).in(Singleton.class);
		
		bind(new TypeLiteral<LinkedHashMap<String, String>>() {}).annotatedWith(Names.named("Abbreviations"))
			.toProvider(new Provider<LinkedHashMap<String, String>>() {
			@Override
			public LinkedHashMap<String, String> get() {
				CSVAbbreviationReader abbreviationReader = new CSVAbbreviationReader();
				try {
					abbreviationReader.setInputStream(new FileInputStream(abbreviationFile));
					return abbreviationReader.read();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return new LinkedHashMap<String, String>();
			}
		}).in(Singleton.class);
		
		
		
		
		bind(new TypeLiteral<Map<RawSentence, MultiClassifiedSentence>>() {})
			.annotatedWith(Names.named("SentenceClassificationMap")).toProvider(new Provider<Map<RawSentence, MultiClassifiedSentence>>() {
			@Override
			public Map<RawSentence, MultiClassifiedSentence> get() {
				return new HashMap<RawSentence, MultiClassifiedSentence>();
			}
		}).in(Singleton.class);
		
		bind(new TypeLiteral<Map<RawSentence, SentenceMetadata>>() {})
			.annotatedWith(Names.named("SentenceMetadataMap")).toProvider(new Provider<Map<RawSentence, SentenceMetadata>>() {
			@Override
			public Map<RawSentence, SentenceMetadata> get() {
				return new HashMap<RawSentence, SentenceMetadata>();
			}
		}).in(Singleton.class);
		
		bind(new TypeLiteral<Map<TaxonTextFile, List<MultiClassifiedSentence>>>() {})
			.annotatedWith(Names.named("TaxonSentencesMap")).toProvider(new Provider<Map<TaxonTextFile, List<MultiClassifiedSentence>>>() {
			@Override
			public Map<TaxonTextFile, List<MultiClassifiedSentence>> get() {
				return new HashMap<TaxonTextFile, List<MultiClassifiedSentence>>();
			}
		}).in(Singleton.class);
		
		
		//////////////////////////////////////////////////////////////////////////
		/////////////////////////    Sentence Tools   ////////////////////////////
		//////////////////////////////////////////////////////////////////////////
		bind(SentenceSpliter.class).in(Singleton.class);
		bind(SentencePredictor.class).in(Singleton.class);
		
		
		
		
		
		
		/***************  Intitializing resources that will be used by the extractors          *********/
		//configure the extractors
		bind(LabelPhraseValueType.class).toInstance(getLabelPhraseValueType(labelValutypeFile));
		
		bind(new TypeLiteral<Set<ICharacterValueExtractor>>() {}).toInstance(getCharacterValueExtractors(characterValueExtractorsFolder, 
		 		uspResultsDirectory, uspString));
		
		bind(ICharacterValueExtractorProvider.class).to(CharacterValueExtractorProvider.class).in(Singleton.class);
		
		//read keywords from the keyword configuration files
		bind(new TypeLiteral<Map<String, Set<ILabel>>>() {}).annotatedWith(Names.named("GlobalTermCharacterMap"))
		.toProvider(new Provider<Map<String, Set<ILabel>>>() {
		@Override
		public Map<String, Set<ILabel>> get() {
			KeywordReader keywordReader = new KeywordReader();
			try {
				return keywordReader.readTermCharacterMap(characterValueExtractorsFolder);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new HashMap<String, Set<ILabel>>();
		}
		}).in(Singleton.class);
		
		
		weka.core.logging.Logger.log(weka.core.logging.Logger.Level.INFO, "Weka Logging started"); 
		
		//bind(IRun.class).to(TrainTestRun.class).in(Singleton.class);
		bind(TrainSentenceClassifier.class).in(Singleton.class);
		
		//bind(MicroPIEProcessor.class);
		//bind(MicroPIEProcessorOld.class);
		bind(SentenceBatchProcessor.class).in(Singleton.class);
	}

	/**
	 * provide  labelValutypeFile
	 * @param labelValutypeFile2
	 * @return
	 */
	private LabelPhraseValueType getLabelPhraseValueType(String labelValutypeFile) {
		return  new CharacterReader().readLabelValueType(labelValutypeFile);
	}

	
	
	/**
	 * the keywords used to indicate the Antibiotic Sensitivity
	 * @return
	 */
	private Set<String> getAntibioticSensitivityTerms(){
		antiSensTerms = new HashSet();
		antiSensTerms.add("susceptible");
		antiSensTerms.add("sensitive");
		antiSensTerms.add("sensitivity");
		antiSensTerms.add("inhibited");
		antiSensTerms.add("susceptibility");
		
		return antiSensTerms;
	}
	
	/**
	 * the keywords used to indicate the Antibiotic Sensitivity
	 * @return
	 */
	private Set<String> getAntibioticSensitivityPatterns(){
		antiSensPatterns = new HashSet();
		antiSensPatterns.add("[Ss]usceptible");
		antiSensPatterns.add("[Ss]ensitive");
		antiSensPatterns.add("[Ii]nhibited");
		antiSensPatterns.add("[Ii]nhibiting");
		antiSensPatterns.add("[Ss]ensitivity");
		
		return antiSensPatterns;
	}
	
	
	/**
	 * the keywords used to indicate the Antibiotic Resistant
	 * @return
	 */
	private Set<String> getAntibioticResistantTerms(){
		antiResistantTerms = new HashSet();
		antiResistantTerms.add("resistant");
		antiResistantTerms.add("resistance");
		antiResistantTerms.add("insusceptible");
		antiResistantTerms.add("insensitive");
		
		return antiResistantTerms;
	}
	
	/**
	 * the keywords used to indicate the Antibiotic Resistant
	 * @return
	 */
	private Set<String> getAntibioticResistantPatterns(){
		antiResistantPatterns = new HashSet();
		antiResistantPatterns.add("[Rr]esistant");
		antiResistantPatterns.add("[Rr]esistance");
		antiResistantPatterns.add("[Ii]nsusceptible");
		antiResistantPatterns.add("[Ii]nsensitive");
		
		return antiResistantPatterns;
	}

	
	
	/**
	 * create extractors from the configuration folder: characterValueExtractorsFolder; 
	 * e.g., micropieInput\CharacterValueExtractors
	 * 
	 * @param extratorsDirectory
	 * @param uspResultsDirectory
	 * @param uspString
	 * @return
	 */
	private Set<ICharacterValueExtractor> getCharacterValueExtractors(String extratorsDirectory, String uspResultsDirectory, 
			String uspString) {
		Set<ICharacterValueExtractor> extractors = new HashSet<ICharacterValueExtractor>();
		
		File inputDir = new File(extratorsDirectory);
		if(!inputDir.exists() || inputDir.isFile()) 
			return extractors;
		
		
		//read extractors from configure files
		ICharacterValueExtractorReader extractorReader = new CharacterValueExtractorReader(
				uspBaseString, uspResultsDirectory, uspString);
		for(File file : inputDir.listFiles()) {
			try {
				ICharacterValueExtractor extractor = extractorReader.read(file);
				//System.out.println(file+" ==>"+extractor.getClass().getName());
				if(extractor!=null) extractors.add(extractor);
			} catch(Exception e) {
				//System.out.println("Could not load extractor in file: " + file.getAbsolutePath() + "\nIt will be skipped");
				log(LogLevel.ERROR, "Could not load extractor in file: " + file.getAbsolutePath() + "\nIt will be skipped");
			}
		}
		
		
		
		
		return extractors;
	}
	
	
	/**
	 * extractors must have a label
	 * @param extractors
	 */
	public void craftExtractors(Set<ICharacterValueExtractor> extractors, SentenceSpliter sentSplitter,
			PosTagger posTagger){
		//System.out.println(extractors+" "+extractors.size());
		//extractors.add(new OrganicCompoundsNotUsedOrNotHydrolyzedExtractor(Label.c52));
		//extractors.add(new InorganicSubstancesNotUsedExtractor(Label.c54));
		//extractors.add(new FermentationSubstratesNotUsed(Label.c56));
		extractors.add(new GcFigureExtractor(sentSplitter, posTagger, Label.c1, "%G+C"));
		
		//AntibioticSyntacticExtractor extractor1 = new AntibioticSyntacticExtractor(Label.c32, "Antibiotic sensitivity",patterns,sentSplitter,stanfordWrapper);
		//new AntibioticPhraseExtractor(Label.c32, "Antibiotic sensitivity", postagger, phraseParser,phraseRelationParser, sentSplitter, keywords);
	}

	
	/**
	 * set input directory and project directories
	 * This method should be called first before all the processes
	 * @param inputDirectory
	 */
	public void setInputDirectory(String inputDirectory) {
		testFolder = inputDirectory + File.separator + testFolder;
		
		// trainingFile = inputDirectory + File.separator + "training_data" + File.separator + "split-training-base-140603.csv";
		// 150123-Training-Sentences.csv
		// trainingFile = inputDirectory + File.separator + "training_data" + File.separator + "150123-Training-Sentences.csv";
		// 150130-Training-Sentences-new.csv
		trainingFile = inputDirectory + File.separator + trainingFile;
		trainedModelFile = inputDirectory + File.separator + trainedModelFile;
		categoryModelFile = inputDirectory + File.separator + categoryModelFile;
		
		svmLabelAndCategoryMappingFile = inputDirectory + File.separator + svmLabelAndCategoryMappingFile;
		firstLevelCategoryMappingFile = inputDirectory + File.separator + firstLevelCategoryMappingFile;
		labelValutypeFile  = inputDirectory + File.separator + labelValutypeFile;
		
		characterValueExtractorsFolder = inputDirectory + File.separator + characterValueExtractorsFolder;
		abbreviationFile = inputDirectory + File.separator + abbreviationFile;
		resFolder = inputDirectory + File.separator + resFolder;
		kbFolder = inputDirectory + File.separator + kbFolder;
		dataHolderFolder = inputDirectory + File.separator + dataHolderFolder;
		
		uspBaseString = inputDirectory + File.separator + uspBaseString;
		uspBaseZipFileName = inputDirectory + File.separator + uspBaseZipFileName;
		uspFolder = inputDirectory + File.separator + uspFolder;
		
		serializedClassifierModel = inputDirectory + File.separator+serializedClassifierModel;

		//System.out.println("resFolder = "+resFolder);
	}
	
	public void setOutputDirectory(String outputDirectory) {
		predicitonsFile = outputDirectory + File.separator + "predictions.csv";
		matrixFile = outputDirectory + File.separator + "matrix.csv";
		//uspBaseString= outputDirectory + File.separator + uspBaseString;
		//new File(uspString).mkdirs();
		uspResultsDirectory = outputDirectory + File.separator + uspResultsDirectory;
		//new File(uspResultsDirectory).mkdirs();
	}

	/*
	@Provides @Named("TokenizeSSplit") @Singleton
	StanfordCoreNLP provideStanfordCoreNLP(){
		Properties stanfordCoreProperties = new Properties();
		stanfordCoreProperties.put("annotators", "tokenize, ssplit");
		return new StanfordCoreNLP(stanfordCoreProperties);
	}
	
	bind(StanfordCoreNLP.class).annotatedWith(Names.named("TokenizeSSplit")).toProvider(new Provider<StanfordCoreNLP>() {
		@Override
		public StanfordCoreNLP get() {
			Properties stanfordCoreProperties = new Properties();
			stanfordCoreProperties.put("annotators", "tokenize, ssplit");
			return new StanfordCoreNLP(stanfordCoreProperties);
		}
	}).in(Singleton.class);
	*/
}
