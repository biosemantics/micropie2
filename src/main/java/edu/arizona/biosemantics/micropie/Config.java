package edu.arizona.biosemantics.micropie;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.io.CSVAbbreviationReader;
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.io.ISentenceReader;
import edu.arizona.biosemantics.micropie.model.ClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.transform.ITextNormalizer;
import edu.arizona.biosemantics.micropie.transform.TextNormalizer;
import edu.arizona.biosemantics.micropie.transform.feature.FilterDecorator;
import edu.arizona.biosemantics.micropie.transform.feature.IFilterDecorator;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;

public class Config extends AbstractModule {

	private String characterListString = "16S rRNA accession #|Family|Genus|Species|Strain|Genome size|%G+C|Other genetic characteristics|Cell shape|Pigments|Cell wall|Motility|Biofilm formation|Habitat isolated from|Oxygen use|Salinity preference|pH minimum|pH optimum|pH maximum|Temperature minimum|Temperature optimum|Temperature maximum|NaCl minimum|NaCl optimum|NaCl maximum|Host|Symbiotic|Pathogenic|Disease caused|Metabolism (energy & carbon source)|Mono & di-saccharides|Polysaccharides|Amino acids|Alcohols|Fatty acids|Other energy or carbon sources|Fermentation products|Polyalkanoates (plastics)|Other metabolic product|Antibiotic sensitivity|Antibiotic resistant";
	
	private String trainingFile = "training-base-140205-test1.csv";
	private String testFolder = "new-microbe-xml-2";
	private String abbreviationFile = "abbrevlist.csv";
	private String predicitonsFile = "predictions.csv";
	private String matrixFile = "matrix.csv";
	
	private int nGramMinSize = 1;
	private int nGramMaxSize = 1;
	private int nGramMinFrequency = 1;
	
	private boolean parallelProcessing = false;
	private int maxThreads = 1;
	
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
		
		bind(IFilterDecorator.class).annotatedWith(Names.named("MultiSVMClassifier_FilterDecorator"))
				.to(FilterDecorator.class);
		
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
				stanfordCoreProperties.put("annotators", "tokenize, ssplit");
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
		
		bind(new TypeLiteral<Map<Sentence, ClassifiedSentence>>() {})
			.annotatedWith(Names.named("SentenceClassificationMap")).toProvider(new Provider<Map<Sentence, ClassifiedSentence>>() {
			@Override
			public Map<Sentence, ClassifiedSentence> get() {
				return new HashMap<Sentence, ClassifiedSentence>();
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
	}
}
