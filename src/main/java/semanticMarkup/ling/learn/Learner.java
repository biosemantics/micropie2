package semanticMarkup.ling.learn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import semanticMarkup.core.Treatment;
import semanticMarkup.know.IGlossary;
import semanticMarkup.knowledge.KnowledgeBase;
import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.WordPOSKey;
import semanticMarkup.ling.learn.dataholder.WordPOSValue;
import semanticMarkup.ling.learn.knowledge.AdditionalBootstrappingLearner;
import semanticMarkup.ling.learn.knowledge.AdjectiveSubjectBootstrappingLearner;
import semanticMarkup.ling.learn.knowledge.AdjectiveVerifier;
import semanticMarkup.ling.learn.knowledge.AndOrTagSetter;
import semanticMarkup.ling.learn.knowledge.AnnotationNormalizer;
import semanticMarkup.ling.learn.knowledge.CommaAsAndAnnotator;
import semanticMarkup.ling.learn.knowledge.CommonSubstructureAnnotator;
import semanticMarkup.ling.learn.knowledge.CoreBootstrappingLearner;
import semanticMarkup.ling.learn.knowledge.DittoAnnotator;
import semanticMarkup.ling.learn.knowledge.FiniteSetsLoader;
import semanticMarkup.ling.learn.knowledge.HeuristicNounLearnerUseMorphology;
import semanticMarkup.ling.learn.knowledge.IgnorePatternAnnotator;
import semanticMarkup.ling.learn.knowledge.IgnoredFinalizer;
import semanticMarkup.ling.learn.knowledge.Initializer;
import semanticMarkup.ling.learn.knowledge.ModifierTagSeparator;
import semanticMarkup.ling.learn.knowledge.NMBResolver;
import semanticMarkup.ling.learn.knowledge.NullSentenceTagger;
import semanticMarkup.ling.learn.knowledge.POSBasedAnnotator;
import semanticMarkup.ling.learn.knowledge.PatternBasedAnnotator;
import semanticMarkup.ling.learn.knowledge.PhraseClauseAnnotator;
import semanticMarkup.ling.learn.knowledge.PronounCharactersAnnotator;
import semanticMarkup.ling.learn.knowledge.HeuristicNounLearnerUseSuffix;
import semanticMarkup.ling.learn.knowledge.UnknownWordBootstrappingLearner;
import semanticMarkup.ling.learn.utility.LearnerUtility;
import semanticMarkup.ling.learn.utility.StringUtility;
import semanticMarkup.ling.transform.ITokenizer;

public class Learner {
	private Configuration myConfiguration;
	private ITokenizer myTokenizer;

	// Data holder
	private DataHolder myDataHolder;

	// Learner utility
	private LearnerUtility myLearnerUtility;

	// Class variables
	Map<String, Boolean> checkedModifiers;

	// Modules
	KnowledgeBase knowledgeBase;
	
	Initializer initializer;
	
	HeuristicNounLearnerUseMorphology heuristicNounLearnerUseMorphology;

	FiniteSetsLoader finiteSetsLoader;
	
	HeuristicNounLearnerUseSuffix heuristicNounLearnerUseSuffix;
	
	PatternBasedAnnotator patternBasedAnnotator; 
	
	IgnorePatternAnnotator ignorePatternAnnotator;
	
	CoreBootstrappingLearner coreBootstrappingLearner;
	
	AdditionalBootstrappingLearner additionalBootstrappingLearner;
	
	UnknownWordBootstrappingLearner unknownWordBootstrappingLearner;
	
	AdjectiveVerifier adjectiveVerifier;
	
	ModifierTagSeparator modifierTagSeparator;
	
	NMBResolver nMBResolver;
	
	AndOrTagSetter andOrTagSetter;
	
	AdjectiveSubjectBootstrappingLearner adjectiveSubjectBootstrappingLearner;

	POSBasedAnnotator posBasedAnnotator;
	
	PhraseClauseAnnotator phraseClauseAnnotator;
	
	DittoAnnotator dittoAnnotator;
	
	PronounCharactersAnnotator pronounCharactersAnnotator;
	
	IgnoredFinalizer ignoredFinalizer; 
	
	CommonSubstructureAnnotator commonSubstructureAnnotator;
	
	CommaAsAndAnnotator commaAsAndAnnotator;
	
	NullSentenceTagger nullSentenceTagger;
	
	AnnotationNormalizer annotationNormalizer; 

	public Learner(Configuration configuration, ITokenizer tokenizer,
			LearnerUtility learnerUtility) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("Learner");

		this.myConfiguration = configuration;
		this.myTokenizer = tokenizer;

		// Utilities
		this.myLearnerUtility = learnerUtility;

		// Data holder
		this.myDataHolder = new DataHolder(myConfiguration,
				myLearnerUtility.getConstant(), myLearnerUtility.getWordFormUtility());

		// Class variables
		this.checkedModifiers = new HashMap<String, Boolean>();

		myLogger.info("Created Learner");
		myLogger.info("\tLearning Mode: " + myConfiguration.getLearningMode());
		myLogger.info("\tMax Tag Lengthr: " + myConfiguration.getMaxTagLength());
		myLogger.info("\n");

		this.knowledgeBase = new KnowledgeBase();
		
		this.initializer = new Initializer(this.myLearnerUtility,
				this.myConfiguration.getNumLeadWords());
		this.heuristicNounLearnerUseMorphology = new HeuristicNounLearnerUseMorphology(this.myLearnerUtility);
		
		this.finiteSetsLoader = new FiniteSetsLoader(this.myLearnerUtility);
		
		this.heuristicNounLearnerUseSuffix = new HeuristicNounLearnerUseSuffix(this.myLearnerUtility);
		
		this.patternBasedAnnotator = new PatternBasedAnnotator();
		
		this.ignorePatternAnnotator = new IgnorePatternAnnotator();
		
		this.coreBootstrappingLearner = new CoreBootstrappingLearner(this.myLearnerUtility, this.myConfiguration);
		
		this.additionalBootstrappingLearner = new AdditionalBootstrappingLearner(this.myLearnerUtility, this.myConfiguration);
		
		this.unknownWordBootstrappingLearner = new UnknownWordBootstrappingLearner(
				this.myLearnerUtility);
		
		this.adjectiveVerifier = new AdjectiveVerifier(this.myLearnerUtility);
		
		this.modifierTagSeparator = new ModifierTagSeparator(this.myLearnerUtility);
		
		this.nMBResolver = new NMBResolver();
		
		this.andOrTagSetter = new AndOrTagSetter(this.myLearnerUtility);
		
		this.adjectiveSubjectBootstrappingLearner = new AdjectiveSubjectBootstrappingLearner(this.myLearnerUtility, this.myConfiguration.getLearningMode(), this.myConfiguration.getMaxTagLength());
		
		this.posBasedAnnotator = new POSBasedAnnotator(this.myLearnerUtility);
		
		this.phraseClauseAnnotator = new PhraseClauseAnnotator(this.myLearnerUtility);
		
		this.dittoAnnotator = new DittoAnnotator(this.myLearnerUtility);
		
		this.pronounCharactersAnnotator = new PronounCharactersAnnotator(this.myLearnerUtility);
		
		this.ignoredFinalizer = new IgnoredFinalizer();
		
		this.nullSentenceTagger = new NullSentenceTagger(this.myLearnerUtility, this.myConfiguration.getDefaultGeneralTag());
		
		this.commonSubstructureAnnotator = new CommonSubstructureAnnotator();
		
		this.commaAsAndAnnotator = new CommaAsAndAnnotator(this.myLearnerUtility);
		
		this.annotationNormalizer 
			= new AnnotationNormalizer(this.getConfiguration().getLearningMode(), 
					this.checkedModifiers, this.getLearnerUtility());
	}

	public DataHolder learn(List<Treatment> treatments, IGlossary glossary,
			String markupMode) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("Learn");
		myLogger.trace("Enter Learn");
		myLogger.trace(String.format("Learning Mode: %s",
				this.myConfiguration.getLearningMode()));

		this.knowledgeBase.importKnowledgeBase(this.myDataHolder, "kb", this.myLearnerUtility.getConstant());
		
		this.initializer.loadTreatments(treatments);
		this.initializer.run(myDataHolder);

		this.heuristicNounLearnerUseMorphology.run(this.myDataHolder);

		this.finiteSetsLoader.run(this.myDataHolder);

		this.heuristicNounLearnerUseSuffix.run(myDataHolder);
	
		// Set the certaintyU and certaintyL value of every entry in WordPOS collection to be 0
		this.resetCounts(myDataHolder);
		
		this.patternBasedAnnotator.run(myDataHolder);

		this.ignorePatternAnnotator.run(myDataHolder);

		
		this.coreBootstrappingLearner.setStatus("start");
		this.coreBootstrappingLearner.run(myDataHolder);
		
		this.coreBootstrappingLearner.setStatus("normal");
		this.coreBootstrappingLearner.run(myDataHolder);
		
		this.additionalBootstrappingLearner.run(myDataHolder);

		myLogger.info("Unknownword bootstrappings:");
		this.unknownWordBootstrappingLearner.run(myDataHolder);

		myLogger.info("Adjectives Verification:");
		this.adjectiveVerifier.run(myDataHolder);

		// For those sentences whose tag has a space between words, separate modifier and update the tag
		this.modifierTagSeparator.run(myDataHolder);
		
		// deal with words that plays N, and B roles
		this.nMBResolver.run(myDataHolder);

		// set and/or tags
		this.andOrTagSetter.run(myDataHolder);

		this.adjectiveSubjectBootstrappingLearner.run(myDataHolder);

		// set tags of sentences with "andor" tag to null
		this.resetAndOrTags(myDataHolder);

		this.getLearnerUtility().tagAllSentences(myDataHolder, "singletag",
				"sentence");

		this.posBasedAnnotator.run(myDataHolder);

		this.phraseClauseAnnotator.run(myDataHolder);

		this.dittoAnnotator.run(myDataHolder);

		this.pronounCharactersAnnotator.run(myDataHolder);
		
		this.ignoredFinalizer.run(myDataHolder);
		
		this.posBasedAnnotator.run(myDataHolder);

		// tag remaining sentences with null tags 
		this.nullSentenceTagger.run(myDataHolder);

		if (StringUtils.equals(this.myConfiguration.getLearningMode(), "adj")) {
			// Modify the sentences which are tagged with commons substructure
			this.commonSubstructureAnnotator.run(myDataHolder);
		}
		
		this.commaAsAndAnnotator.run(myDataHolder);
		
		this.annotationNormalizer.run(myDataHolder);
		
		this.prepareTablesForParser(myDataHolder);

		myDataHolder.writeToFile("dataholder", "");

		myLogger.info("Learning done!");

		return myDataHolder;
	}

	// Learner Methods
	public void addGlossary(IGlossary glossary) {
		if (glossary != null) {
			String category = "struture";
			Set<String> pWords = glossary.getWords(category);
			Set<String> categories = new HashSet<String>();
			categories.add(category);
			Set<String> bWords = glossary.getWordsNotInCategories(categories);
			this.getDataHolder().addWords2WordPOSHolder(pWords, "p");
			this.getDataHolder().addWords2WordPOSHolder(bWords, "b");
		}
	}

	public DataHolder getDataHolder() {
		return this.myDataHolder;
	}
	
	/**
	 * Set the certaintyU and certaintyL value of every entry in WordPOS
	 * collection to be 0
	 * 
	 * @param dh
	 *            DataHolder handler to update the dataholder and return the
	 *            updated dataholder
	 * @return Number of records that have been changed
	 */
	public int resetCounts(DataHolder dh) {
		int count = 0;
		Iterator<Entry<WordPOSKey, WordPOSValue>> iter = dh
				.getWordPOSHolderIterator();
		while (iter.hasNext()) {
			Entry<WordPOSKey, WordPOSValue> wordPOSObject = iter.next();
			wordPOSObject.getValue().setCertiantyU(0);
			wordPOSObject.getValue().setCertiantyL(0);
			count++;
		}

		return count;
	}

	public void resetAndOrTags(DataHolder dataholderHandler) {
		dataholderHandler.updateSentenceTag("^andor$", null);
	}
	
	/**
	 * Set saved_flag to red for the following terms in preparation to run the Parser
	 * 1. words that are not in allwords table 
	 * 2. special words added
	 */
	public void prepareTablesForParser(DataHolder dataholderHandler) {
		Set<String> toRemove = new HashSet<String>();
		toRemove.addAll(this.myLearnerUtility.getConstant().pronounWords);
		toRemove.addAll(this.myLearnerUtility.getConstant().characterWords);
		toRemove.addAll(this.myLearnerUtility.getConstant().numberWords);
		toRemove.addAll(this.myLearnerUtility.getConstant().clusterStringWords);
		toRemove.addAll(this.myLearnerUtility.getConstant().pronounWords);
		toRemove.addAll(this.myLearnerUtility.getConstant().stopWords);	
		
		Set<String> unknownWords =dataholderHandler.getUnknownWordHolder().keySet(); 
		
		// set saved_flag to red in WordPOS collection
		Iterator<Entry<WordPOSKey, WordPOSValue>> iter = dataholderHandler.getWordPOSHolderIterator();
		while (iter.hasNext()) {
			Entry<WordPOSKey, WordPOSValue> entry = iter.next();
			WordPOSKey key = entry.getKey();
			WordPOSValue value = entry.getValue();
			String word = key.getWord();
//			boolean c1 = toRemove.contains(word);
//			boolean c2 = StringUtility.isMatchedNullSafe(word, "[a-z]");
//			boolean c3 = unknownWords.contains(word);
			
			if (toRemove.contains(word)
					|| !StringUtility.isMatchedNullSafe(word, "[a-z]")
					|| !unknownWords.contains(word)) {
				value.setSavedFlag("red");
			}
		}
		
		// handle -ly words
		// If a word in WordPOS collection, has ending of -ly, and after
		// removing the -ly ending, it appears in the UnknownWords collections,
		// then set the savedFlag to "red"
		Iterator<Entry<WordPOSKey, WordPOSValue>> iter2 = dataholderHandler.getWordPOSHolderIterator();
		while (iter2.hasNext()) {
			Entry<WordPOSKey, WordPOSValue> entry = iter2.next();
			WordPOSKey key = entry.getKey();
			WordPOSValue value = entry.getValue();
			String lyWord = key.getWord();
			if (StringUtility.isMatchedNullSafe(lyWord, "ly$")) {
				String nWord = lyWord.replaceAll("ly$", "");
				if (unknownWords.contains(nWord)) {
					value.setSavedFlag("red");
				}
			}
		}
	}

	public LearnerUtility getLearnerUtility() {
		return this.myLearnerUtility;
	}

	public ITokenizer getTokenizer() {
		return this.myTokenizer;
	}

	public Configuration getConfiguration() {
		return this.myConfiguration;
	}

	/**
	 * check if the lead has the head in the beginning of it
	 * 
	 * @param head
	 * @param lead
	 * @return true if it has, false if it does not have
	 */
	public boolean hasHead(List<String> head, List<String> lead) {

		// null case
		if (head == null || lead == null) {
			return false;
		}

		int headSize = head.size();
		int leadSize = lead.size();
		if (headSize > leadSize) {
			return false;
		}

		for (int i = 0; i < headSize; i++) {
			if (!StringUtils.equals(head.get(i), lead.get(i))) {
				return false;
			}
		}

		return true;
	}	
}