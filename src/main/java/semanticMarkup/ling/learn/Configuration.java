package semanticMarkup.ling.learn;

public class Configuration {
	private String wordNetDictDir = null;
	private String openNLPModelDir = null;
	private String openNLPSentenceDetectorDir = null;
	private String openNLPTokenizerDir = null;
	
	private String learningMode = null;
	private int maxTagLength;
	private int numLeadWords; 
	private String defaultGeneralTag;
	
	private static final String defaultWordNetDictDir = "res/WordNet/WordNet-3.0/dict";
	private static final String defaultOpenNLPModelDir = "res";
	private static final String defaultLearningMode = "plain";
	private static final int defaultTagLength = 150;
	private static final int defaultNumLeadWords = 3;

	public Configuration() {
		//this.learningMode = learningMode;
		this.learningMode = System.getProperty("charaparser.learningmode");
		if (this.learningMode == null){
			this.learningMode = Configuration.defaultLearningMode;
		}
		
		this.defaultGeneralTag = System.getProperty("charaparser.defaultgeneraltag");
		if (this.defaultGeneralTag == null) {
			this.defaultGeneralTag = "general";
		}
		
		this.wordNetDictDir = System.getProperty("charaparser.wordnet.dict.dir");
		if (this.wordNetDictDir == null) {
			this.wordNetDictDir = Configuration.defaultWordNetDictDir;
		}
		
		String tempTagLength = System.getProperty("charaparser.taglength");
		if (tempTagLength != null) {
			this.maxTagLength = Integer.parseInt(tempTagLength);
		}
		else {
			this.maxTagLength = Configuration.defaultTagLength;
		}
		
		String numLeadWords = System.getProperty("charaparser.numleadwords");
		if (numLeadWords != null) {
			this.numLeadWords = Integer.parseInt(numLeadWords);
		}
		else {
			this.numLeadWords = Configuration.defaultNumLeadWords;
		}
		
		
		String tempOpenNLPModelDir = System.getProperty("charaparser.opennlp.model.dir");
		if ( tempOpenNLPModelDir==null){
			this.openNLPSentenceDetectorDir=System.getProperty("charaparser.opennlp.model.sent.dir");
			if (this.openNLPSentenceDetectorDir==null){
				this.openNLPSentenceDetectorDir = Configuration.defaultOpenNLPModelDir+"//en-sent.bin";
			}
			
			this.openNLPTokenizerDir=System.getProperty("charaparser.opennlp.model.token.dir");
			if (this.openNLPTokenizerDir==null){
				this.openNLPTokenizerDir=Configuration.defaultOpenNLPModelDir+"//en-token.bin";
			}			
		}
		else{
			this.setOpenNLPModelDir(tempOpenNLPModelDir);
		}
		

	}
	
	public String getLearningMode() {
		// TODO Auto-generated method stub
		return this.learningMode;
	}
	
	public void setLearningMode(String learningMode) {
		// TODO Auto-generated method stub
		this.learningMode = learningMode;
	}
	
	public String getWordNetDictDir() {
		return this.wordNetDictDir;
	}
	
	public void setWordNetDictDir(String wordNetDictDir){
		this.wordNetDictDir=wordNetDictDir;
	}
	
	public String getOpenNLPSentenceDetectorDir() {
		return this.openNLPSentenceDetectorDir;
	}
	
	public int getMaxTagLength(){
		return this.maxTagLength;
	}
	
	public int getNumLeadWords(){
		return this.numLeadWords;
	}
	
	public void setOpenNLPSentenceDetectorDir(String openNLPSentenceDetectorDir){
		this.openNLPSentenceDetectorDir=openNLPSentenceDetectorDir;
	}
	
	public String getOpenNLPTokenizerDir() {
		return this.openNLPTokenizerDir;
	}
	
	public void setOpenNLPTokenizerDir(String openNLPTokenizerDir){
		this.openNLPTokenizerDir=openNLPTokenizerDir;
	}
	
	public String getOpenNLPModelDir() {
		return this.openNLPModelDir;
	}
	
	public void setOpenNLPModelDir(String openNLPModelDir){
		this.openNLPModelDir=openNLPModelDir;
		this.openNLPSentenceDetectorDir = this.openNLPModelDir+"//en-sent.bin";
		this.openNLPTokenizerDir=this.openNLPModelDir+"//en-token.bin";
	}
	
	public void setMaxTagLength(int maxTL){
		this.maxTagLength = maxTL;
	}

	public String getDefaultGeneralTag() {
		return this.defaultGeneralTag;
	}

}
