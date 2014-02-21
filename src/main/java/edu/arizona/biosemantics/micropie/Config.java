package edu.arizona.biosemantics.micropie;

import java.io.File;
import java.io.FileInputStream;
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

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.regex.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellDiameterExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellLongExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellShapeExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellSizeExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellWideExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CharacterValueExtractorProvider;
import edu.arizona.biosemantics.micropie.extract.regex.GcExtractor;
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
import edu.arizona.biosemantics.micropie.extract.regex.ICharacterValueExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.ICharacterValueExtractorProvider;
import edu.arizona.biosemantics.micropie.io.CSVAbbreviationReader;
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.io.CharacterValueExtractorReader;
import edu.arizona.biosemantics.micropie.io.ICharacterValueExtractorReader;
import edu.arizona.biosemantics.micropie.io.ISentenceReader;
import edu.arizona.biosemantics.micropie.log.LogLevel;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.transform.ITextNormalizer;
import edu.arizona.biosemantics.micropie.transform.TextNormalizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;

public class Config extends AbstractModule {

	private String characterListString = "16S rRNA accession #|Family|Genus|Species|Strain|Genome size|%G+C|Other genetic characteristics|Cell shape|Pigments|Cell wall|Motility|Biofilm formation|Habitat isolated from|Oxygen Use|Salinity preference|pH minimum|pH optimum|pH maximum|Temperature minimum|Temperature optimum|Temperature maximum|NaCl minimum|NaCl optimum|NaCl maximum|Host|Symbiotic|Pathogenic|Disease Caused|Metabolism (energy & carbon source)|Carbohydrates (mono & disaccharides)|Polysaccharides|Amino Acids|Alcohols|Fatty Acids|Other Energy or Carbon Sources|Fermentation Products|Polyalkanoates (plastics)|Other Metabolic Product|Antibiotic Sensitivity|Antibiotic Resistant|Cell Diameter|Cell Long|Cell Wide|Cell Membrane & Cell Wall Components|External features|Filterability|Internal features|Lysis Susceptibility|Physiological requirements|Antibiotics|Secreted Products|Storage Products|Tests|Pathogen Target Organ|Complex Mixtures|Inorganic|Metals|Nitrogen Compounds|Organic|Organic Acids|Other";

	private String trainingFile = "training-base-140214.csv";
	private String testFolder = "new-microbe-xml";
	private String abbreviationFile = "abbrevlist.csv";
	private String predicitonsFile = "predictions.csv";
	private String matrixFile = "matrix.csv";
	
	private int nGramMinSize = 1;
	private int nGramMaxSize = 1;
	private int nGramMinFrequency = 1;
	
	private boolean parallelProcessing = false;
	private int maxThreads = 1;
	
	private String nGramTokenizerOptions = "-delimiters ' ' -max 1 -min 1";
	private String stringToWordVectorOptions = "-W " + Integer.MAX_VALUE + " -T -L -M 1 -tokenizer weka.core.tokenizer.NGramTokenizer " + nGramTokenizerOptions + "";
	private String multiFilterOptions = "-D -F weka.filters.unsupervised.attribute.StringToWordVector " + stringToWordVectorOptions + "";
	private String libSVMOptions = "-S 0 -D 3 -K 2 -G 0 -R 0 -N 0.5 -M 100 -C 2048 -P 1e-3";
	
	private Set<ICharacterValueExtractor> characterValueExtractors = 
			getCharacterValueExtractors("CharacterValueExtractors");
	
	@Override
	protected void configure() {
		bind(IRun.class).to(TrainTestRun.class).in(Singleton.class);
		
		bind(new TypeLiteral<LinkedHashSet<String>>() {}).annotatedWith(Names.named("Characters"))
			.toProvider(new Provider<LinkedHashSet<String>>() {
				@Override
				public LinkedHashSet<String> get() {
					return new LinkedHashSet<String>(Arrays.asList(characterListString.split("\\|")));
				}
		}).in(Singleton.class);
		
		bind(String.class).annotatedWith(Names.named("trainingFile")).toInstance(
				trainingFile);
		
		bind(String.class).annotatedWith(Names.named("testFolder")).toInstance(
				testFolder);
		
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
				
		bind(new TypeLiteral<Set<ICharacterValueExtractor>>() {}).toInstance(characterValueExtractors);
		
		bind(ISentenceReader.class).to(CSVSentenceReader.class).in(Singleton.class);
		
		bind(ITextNormalizer.class).to(TextNormalizer.class);
		
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
		
		bind(StanfordCoreNLP.class).toProvider(new Provider<StanfordCoreNLP>() {
			@Override
			public StanfordCoreNLP get() {
				Properties stanfordCoreProperties = new Properties();
				stanfordCoreProperties.put("annotators", "tokenize, ssplit, pos, parse");
				return new StanfordCoreNLP(stanfordCoreProperties);
			}
		}).in(Singleton.class);
		
		bind(LexicalizedParser.class).toProvider(new Provider<LexicalizedParser>() {
			@Override
			public LexicalizedParser get() {
				return LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
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
		
		bind(new TypeLiteral<Map<Sentence, MultiClassifiedSentence>>() {})
			.annotatedWith(Names.named("SentenceClassificationMap")).toProvider(new Provider<Map<Sentence, MultiClassifiedSentence>>() {
			@Override
			public Map<Sentence, MultiClassifiedSentence> get() {
				return new HashMap<Sentence, MultiClassifiedSentence>();
			}
		}).in(Singleton.class);
		
		bind(new TypeLiteral<Map<Sentence, SentenceMetadata>>() {})
			.annotatedWith(Names.named("SentenceMetadataMap")).toProvider(new Provider<Map<Sentence, SentenceMetadata>>() {
			@Override
			public Map<Sentence, SentenceMetadata> get() {
				return new HashMap<Sentence, SentenceMetadata>();
			}
		}).in(Singleton.class);
		
		bind(new TypeLiteral<Map<String, List<Sentence>>>() {})
			.annotatedWith(Names.named("TaxonSentencesMap")).toProvider(new Provider<Map<String, List<Sentence>>>() {
			@Override
			public Map<String, List<Sentence>> get() {
				return new HashMap<String, List<Sentence>>();
			}
		}).in(Singleton.class);
		
		bind(ICharacterValueExtractorProvider.class).to(CharacterValueExtractorProvider.class).in(Singleton.class);
		
		weka.core.logging.Logger.log(weka.core.logging.Logger.Level.INFO, "Weka Logging started"); 
	}

	private Set<ICharacterValueExtractor> getCharacterValueExtractors(String extratorsDirectory) {
		Set<ICharacterValueExtractor> extractors = new HashSet<ICharacterValueExtractor>();
		
		File inputDir = new File(extratorsDirectory);
		if(!inputDir.exists() || inputDir.isFile()) 
			return extractors;
		
		ICharacterValueExtractorReader extractorReader = new CharacterValueExtractorReader();
		for(File file : inputDir.listFiles()) {
			try {
				ICharacterValueExtractor extractor = extractorReader.read(file);
				extractors.add(extractor);
			} catch(Exception e) {
				log(LogLevel.ERROR, "Could not load extractor in file: " + file.getAbsolutePath() + "\nIt will be skipped", e);
			}
		}
		
		//Add additional more "customized" extractors than the universal keyword based one
		//e.g.
		// extractors.add(new CellSizeExtractor(Label.c1));
		//extractors.add(new CellSizeExtractor(Label.c2));
		
		extractors.add(new GcExtractor(Label.c1));
		extractors.add(new CellDiameterExtractor(Label.c2));
		extractors.add(new CellLongExtractor(Label.c2));
		extractors.add(new CellWideExtractor(Label.c2));
		
		
		extractors.add(new GrowthTempMaxExtractor(Label.c3));
		extractors.add(new GrowthTempMinExtractor(Label.c3));
		extractors.add(new GrowthPhMaxExtractor(Label.c3));
		extractors.add(new GrowthPhMinExtractor(Label.c3));
		extractors.add(new GrowthPhOptimumExtractor(Label.c3));
		extractors.add(new GrowthTempOptimumExtractor(Label.c3));
		extractors.add(new GrowthNaclOptimumExtractor(Label.c3));
		extractors.add(new GrowthNaclMaxExtractor(Label.c3));
		extractors.add(new GrowthNaclMinExtractor(Label.c3));
		
		return extractors;
	}

}
