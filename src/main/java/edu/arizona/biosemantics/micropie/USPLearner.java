package edu.arizona.biosemantics.micropie;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import edu.arizona.biosemantics.micropie.extract.ExtractorType;
import edu.arizona.biosemantics.micropie.model.IndexMapping;
import edu.arizona.biosemantics.micropie.model.RawSentence;
import edu.arizona.biosemantics.micropie.nlptool.CollapseUSPSentByCategoryChar;
import edu.arizona.biosemantics.micropie.nlptool.SeperatorTokenizer;
import edu.arizona.biosemantics.micropie.nlptool.StanfordParserWrapper;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import usp.semantic.Parse;


/**
 * USP Generator---generate USP input files
 * @author maojin
 *
 */
public class USPLearner {
	
	private String uspBaseFolder = null;//the base folder path
	private String characterValueExtractorsFolder;
	private StanfordParserWrapper stanfordWrapper;
	private CollapseUSPSentByCategoryChar collapseUSPSentByCateogryChar = new CollapseUSPSentByCategoryChar();
	int counter = 1;//sentence counter
	private Hashtable<String, String> kwdListByCategory;
	
	public static void main(String[] args){
		Parse uspParse = new Parse();
		String uspModelDir = "F:\\MicroPIE\\micropieInput\\usp_base";
		try {
			uspParse.runParse(uspModelDir, "F:\\MicroPIE\\micropieInput\\output\\usp_results");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void initialize(){
		//if the folder "usp" exists, delete it
		//FileUtils.deleteDirectory(new File(uspBaseFolder));
		File baseFile = new File(uspBaseFolder);
		if(baseFile.exists()) System.out.println(uspBaseFolder+" is existed.");
				
		//create the folder structure for USP
		createBasicFolders();

		// STEP 1: Read Abbreviation (Keyword) List First and But HashTable<String, String>
		// Example {category1_character1,XXX|YYY|ZZZ}
		kwdListByCategory = readAbbrevList(characterValueExtractorsFolder);
	}
	
	/**
	 * Create input folders for USP from list sentences
	 */
	public void addSentenceList(List<RawSentence> listSentence)
			throws IOException, InterruptedException, ExecutionException {
		if(kwdListByCategory==null) this.initialize();
		
		//iteratively process all the train sentences
		for (RawSentence sentence : listSentence) {
			addASentence(sentence);
			counter++;
		}
	}
	
	/**
	 * add one folder to the dataset
	 * @param folder
	 */
	public void addFromFile(String fileName){
		if(kwdListByCategory==null) this.initialize();
		
		File datasetFile = new File(fileName);
		if(datasetFile.exists()){
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(datasetFile)));
				String line = null;
				while((line = br.readLine())!=null){
					addASentence(new RawSentence(line.trim()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	

	/**
	 * add a sentence to the dataset
	 * @param sentence
	 */
	public void addASentence(RawSentence sentence) {
		//StringBuilder depStringBuilder = new StringBuilder(); // Stanford Dependency
		//StringBuilder inputStringBuilder = new StringBuilder();
		StringBuilder morphStringBuilder = new StringBuilder();
		//StringBuilder parseStringBuilder = new StringBuilder(); // Parse Tree
		StringBuilder textStringBuilder = new StringBuilder();
		
		String sentText = sentence.getText(); // it is sentence based not text
		//ILabel sentLabel = sentence.getLabel(); // labels
		// based anymore ??
		
		//log(LogLevel.INFO, "build pos tagger and dependency as USP inputs using stanford corenlp pipeline...");
		
		//String depStringPlain = ""; // Dependency String
		StringTokenizer textToken = new StringTokenizer(sentText, " ");
		if (textToken.countTokens() < 40) {// if the sentence is larger than 40 tokens. will it encounter some errors?
			textStringBuilder.append(sentText);//sentText0==>sentText
			
			// STEP 3: Build sentence list with abbreviation terms and build the collapseUSPSent Index
			// build collapse USP sentence
			List indexMappingList = replaceAbbrev(sentText, kwdListByCategory, morphStringBuilder, textStringBuilder);
			
			StringBuffer collapsedSentString = new StringBuffer();
			StringBuffer collapsedSentIndexString = new StringBuffer();
			// STEP 4: Collapse the sentence and build index txt file
			buildIndexFile(indexMappingList,collapsedSentString,collapsedSentIndexString);
			
			// STEP 5: Build USP_COLLAPSED inputs
			// build collapsed USP inputs

			collapsedFilesBuilder(collapsedSentString.toString(),collapsedSentIndexString.toString(),counter);
			
			// write string into txt files
			try (PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(uspBaseFolder + "/morph_o/0/" + counter + ".morph",
							false)))) {
				out.println(morphStringBuilder);
			} catch (IOException e) {
				// exception handling left as an exercise for the reader
			}
			try (PrintWriter out = new PrintWriter(
					new BufferedWriter(new FileWriter(uspBaseFolder + "/text_o/0/"
							+ counter + ".txt", false)))) {
				out.println(textStringBuilder);
			} catch (IOException e) {
				// exception handling left as an exercise for the reader
			}
			
		}
		
		System.out.println("Finish creating USPInputs");
		// counter++;
		//log(LogLevel.INFO, "done building pos tagger and dependency as USP inputs using stanford corenlp pipeline...");
	}


	/**
	 * build collapsed sentence files
	 * @param collapsedSentString
	 */
	public void collapsedFilesBuilder(String collapsedSentString, String collapsedSentIndexString, int counter) {

		StringBuilder collapsedDepStringBuilder = new StringBuilder(); // Stanford Dependency
		StringBuilder collapsedInputStringBuilder = new StringBuilder();
		StringBuilder collapsedMorphStringBuilder = new StringBuilder();
		StringBuilder collapsedParseStringBuilder = new StringBuilder(); // Parse Tree
		StringBuilder collapsedTextStringBuilder = new StringBuilder();
		StringBuilder collapsedSentIndexStringBuilder = new StringBuilder();
		
		String collapsedDepStringPlain = "";
		
		// StringTokenizer collapsedSentStringToken = new StringTokenizer(collapsedSentString, " ");
		// if (collapsedSentStringToken.countTokens() < 100) {
		// }
		
		Annotation annotationCollapsedSent = new Annotation(collapsedSentString);
		this.stanfordWrapper.annotate(annotationCollapsedSent);
		List<CoreMap> sentenceAnnotationsCollapsedSent = annotationCollapsedSent.get(SentencesAnnotation.class);
		for (CoreMap sentenceAnnotationCollapsedSent : sentenceAnnotationsCollapsedSent) {
			// result.add(sentenceAnnotationCollapsedSent.toString());
			for (CoreLabel token : sentenceAnnotationCollapsedSent
					.get(TokensAnnotation.class)) {

				String pos = token.get(PartOfSpeechAnnotation.class);
				// System.out.println(token + "_" + pos);

				if (token.toString().equals("_")) {
					collapsedInputStringBuilder.append("dash_" + pos + "\n");
					collapsedMorphStringBuilder.append("dash\n");
				} else {
					collapsedInputStringBuilder.append(token + "_" + pos + "\n");
					collapsedMorphStringBuilder.append(token.toString()
							.toLowerCase() + "\n");
				}

			}
			
			Tree tree = sentenceAnnotationCollapsedSent.get(TreeAnnotation.class);

			//System.out.println("The first sentence parsed is:");
			// tree.pennPrint(outputStream);
			String treeString = tree.pennString();
			//System.out.println(treeString);
			collapsedParseStringBuilder.append(treeString);
			
			SemanticGraph dependencies = sentenceAnnotationCollapsedSent
					.get(CollapsedCCProcessedDependenciesAnnotation.class);

			String depStringXml = dependencies.toFormattedString();
					//。.toString("xml");
			
			if (depStringXml.length() == 0) {
				System.out.println("No Dependency!");	
			}
			
			
			SAXBuilder saxBuilder = new SAXBuilder();
			try {
				Document xmlDocument = saxBuilder
						.build(new StringReader(depStringXml));
				// String message =
				// xmlDocument.getRootElement().getText();
				// System.out.println(message);
				Element rootNode = xmlDocument.getRootElement();
				// System.out.println(rootNode.getName());
				// //<dependencies
				// style="typed"> => dependencies
				// System.out.println(rootNode.getAttributeValue("style"));
				// // style="typed" => typed

				List depList = rootNode.getChildren("dep");
				for (int i = 0; i <= depList.size() - 1; i++) {
					Element element = (Element) depList.get(i);
					// System.out.println("dep type : "+
					// element.getAttributeValue("type"));
					collapsedDepStringPlain += element.getAttributeValue("type")
							+ "(";

					// System.out.println("governor : "+
					// element.getChildText("governor"));
					collapsedDepStringPlain += element.getChildText("governor")
							+ "-";
					List<Element> childrenList = element
							.getChildren("governor");
					for (int j = 0; j <= childrenList.size() - 1; j++) {
						Element element2 = childrenList.get(j);
						// System.out.println("idx : "+
						// element2.getAttributeValue("idx"));
						collapsedDepStringPlain += element2
								.getAttributeValue("idx");
					}
					collapsedDepStringPlain += ", ";

					// System.out.println("dependent : "+
					// element.getChildText("dependent"));
					collapsedDepStringPlain += element.getChildText("dependent")
							+ "-";
					List<Element> childrenList2 = element
							.getChildren("dependent");
					for (int j = 0; j <= childrenList2.size() - 1; j++) {
						Element element2 = childrenList2.get(j);
						// System.out.println("idx : "+
						// element2.getAttributeValue("idx"));
						collapsedDepStringPlain += element2
								.getAttributeValue("idx");
					}
					collapsedDepStringPlain += ")\n";

				}
				collapsedDepStringBuilder.append(collapsedDepStringPlain);
				// System.out.println("dependencies::" + dependencies);
				// System.out.println("depStringPlain:: " + depStringPlain);

			} catch (JDOMException e) {
				// handle JDOMException
			} catch (IOException e) {
				// handle IOException
			}

			// collapsedTextStringBuilder.append(sentence.getText());

		}

		// System.out.println("collapsedDepStringPlain ::\n" + collapsedDepStringPlain);					
		
		collapsedTextStringBuilder.append(collapsedSentString);
		collapsedSentIndexStringBuilder.append(collapsedSentIndexString);				
		
		
		//if (collapsedDepStringBuilder.toString().length() == 0) continue;
		
		//write to the file
		
		

		try (PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(uspBaseFolder + "/index/0/" + counter + ".index", false)))) {
			out.println(collapsedSentIndexStringBuilder);
		} catch (IOException e) {
			// exception handling left as an exercise for the reader
		}
		
		try (PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(uspBaseFolder + "/dep/0/" + counter + ".dep", false)))) {
			out.println(collapsedDepStringBuilder);
		} catch (IOException e) {
			// exception handling left as an exercise for the reader
		}
		try (PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(uspBaseFolder + "/morph/0/" + counter + ".input",
						false)))) {
			out.println(collapsedInputStringBuilder);
		} catch (IOException e) {
			// exception handling left as an exercise for the reader
		}
		try (PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(uspBaseFolder + "/morph/0/" + counter + ".morph",
						false)))) {
			out.println(collapsedMorphStringBuilder);
		} catch (IOException e) {
			// exception handling left as an exercise for the reader
		}
		try (PrintWriter out = new PrintWriter(
				new BufferedWriter(new FileWriter(uspBaseFolder + "/text/0/"
						+ counter + ".txt", false)))) {
			out.println(collapsedTextStringBuilder);
		} catch (IOException e) {
			// exception handling left as an exercise for the reader
		}
		try (PrintWriter out = new PrintWriter(
				new BufferedWriter(new FileWriter(uspBaseFolder + "/parse/0/"
						+ counter + ".parse", false)))) {
			out.println(collapsedParseStringBuilder);
		} catch (IOException e) {
			// exception handling left as an exercise for the reader
		}
		
		
		
	}


	/**
	 * create basic folders for USP
	 * including: text, text_o, morph, morph_o, dep,parse,
	 * 
	 * TODO: let each folder hold 1000 files at most
	 */
	public void createBasicFolders() {
		new File(uspBaseFolder).mkdirs();
		new File(uspBaseFolder + "/text").mkdirs();
		new File(uspBaseFolder + "/text_o").mkdirs();
		new File(uspBaseFolder + "/morph").mkdirs();
		new File(uspBaseFolder + "/morph_o").mkdirs();
		new File(uspBaseFolder + "/dep").mkdirs();
		new File(uspBaseFolder + "/parse").mkdirs();
		new File(uspBaseFolder + "/parse").mkdirs();
		new File(uspBaseFolder + "/index").mkdirs();
	}


	/**
	 * read all the abbreviation list of categories from the given folder
	 * @param characterValueExtractorsFolder2
	 * @return
	 */
	public Hashtable<String, String> readAbbrevList(String characterValueExtractorsFolder) {
		// Construct abbreviation list
		Hashtable<String, String> kwdListByCategory = new Hashtable<String, String>();
		// Example {category1_character1,XXX|YYY|ZZZ}
		
		File inputDir = new File(characterValueExtractorsFolder);
		if(inputDir.exists() && !inputDir.isFile()) {
			for(File file : inputDir.listFiles()) {
				try {
					String name = file.getName();
					
					// ignore ._filename
					// ex: ._.DS_Store
					// ex: ._c2.Pigments.key
					// also ignore this file => ".DS_Store"
					
					if (name.substring(0,2).equals("._") || name.equals(".DS_Store")) {
						// Do nothing
					} else {
						// System.out.println("file name is ::" + name);
						
						int firstDotIndex = name.indexOf(".");
						int lastDotIndex = name.lastIndexOf(".");
						String labelName = name.substring(0, firstDotIndex);
						String character = name.substring(firstDotIndex + 1, lastDotIndex);
						
						// character = character.replaceAll("\\s", "-");
						// character = character.replaceAll("\\(", "-");
						// character = character.replaceAll("\\)", "-");
						character = character.replaceAll("\\s", "");
						character = character.replaceAll("\\(", "");
						character = character.replaceAll("\\)", "");
						character = character.replaceAll("\\&", "");
						
						// character = character.substring(0,10);
						String type = name.substring(lastDotIndex + 1, name.length());
						// System.out.println("type is ::" + type);
						
						ExtractorType extractorType = ExtractorType.valueOf(type);
						
						String keywords = "";
						switch(extractorType) {
						case key:
							BufferedReader br = new BufferedReader(new InputStreamReader(
									new FileInputStream(file), "UTF8"));
							
							String strLine;
							while ((strLine = br.readLine()) != null) {
								// System.out.println("strLine is ::" + strLine);
								// if (name.contains("c7.")) {
								//	System.out.println("c7 exists!");
								//	System.out.println("strLine is ::" + strLine);
								// }
								if (strLine.length() > 1) {
									// remove space
									// 
									String keyword = strLine.trim().toLowerCase();
									keywords += keyword + "|";
								}
							}
							br.close();
							
							if( keywords.length() > 1) {
								if( keywords.substring(keywords.length()-1, keywords.length()).equals("|")) {
									keywords = keywords.substring(0, keywords.length()-1);
								}
								kwdListByCategory.put(labelName+"-"+character , keywords);
							}
							break;

						case usp:
							// do nothing
							break;
						default:
							// throw new Exception("Could not identify extractor type from file");
						}//end of switch case
					}
					

				} catch(Exception e) {
					//log(LogLevel.ERROR, "Could not load extractor in file: " + file.getAbsolutePath() + "\nIt will be skipped", e);
				}
			}
		}
		return kwdListByCategory;
	}//end of readAbbrevList
	
	
	
	
	/**
	 * replace the abbrev with the category name and map the indexes
	 * @param sentence
	 * @param kwdListByCategory
	 */
	public List replaceAbbrev(String sentence, Hashtable kwdListByCategory,StringBuilder morphStringBuilder, StringBuilder textStringBuilder){
		SeperatorTokenizer spaceTokenizer = new SeperatorTokenizer("\\s+");
		
		//produce morph and text for the original sentence
		sentence = stanfordWrapper.tokenizer2Str(sentence);
		String[] tokens = spaceTokenizer.tokenize(sentence);
		for (int i = 0; i < tokens.length; i++) {
			morphStringBuilder.append(tokens[i].toString().toLowerCase()).append("\n");
		}
		
		
		//tag the category list for the string
		String tagSentText = collapseUSPSentByCateogryChar.tagWithCategoryList(sentence, kwdListByCategory);
		String[] tagTokens = spaceTokenizer.tokenize(tagSentText);
		
		//map the tagged string indexes to the original ones
		List<IndexMapping> collapseUSPSentIndexMappingList = new ArrayList<IndexMapping>();
		
		int oriIndex = 0;
		for (int index  = 0; index < tagTokens.length;) {
			String tagToken = tagTokens[index];
			
			List<String> words = new ArrayList<String>();
			words.add(tagToken);
			String category = collapseUSPSentByCateogryChar.getCategoryName(tagToken, kwdListByCategory);
			
			if ( !category.equals("") ) {
				words = collapseUSPSentByCateogryChar.fetchSubseqTokensOfCat(index, category, tagTokens);
				
				int count = 0;
				ArrayList<String> indices = new ArrayList<String>();
				
				for (String word : words) {
					
					// System.out.println("word::" + word);
					if (word.matches("\\W+")) {
						count++;
						// indices.add(String.valueOf(index+count));
					} else {
						word = collapseUSPSentByCateogryChar.removeNumAndCategory(word); //turn ‘DDD#EEE[Cat]’ to ‘DDD EEE’
						String[] parts = word.split(" ");
						
						String multiIndex = ""; 
						for ( String part: parts ){
							multiIndex += String.valueOf(oriIndex+count) + "-";
							// indices.add(String.valueOf(oriIndex+count));
							count++;
						}
						
						if (multiIndex.substring(multiIndex.length()-1, multiIndex.length()).equals("-")) {
						 	multiIndex = multiIndex.substring(0, multiIndex.length()-1);
						}
						indices.add(multiIndex);
					}
				}
				
				// transfer it into indiciesString
				String indicesString = "";
				int itemInIndicesCounter = 0;
				for (String itemInIndices : indices) {
					if (itemInIndicesCounter == (indices.size()-1)) {
						indicesString += itemInIndices;
					} else {
						indicesString += itemInIndices + ",";
					}
					itemInIndicesCounter++;
				}
				
				collapseUSPSentIndexMappingList.add(collapseUSPSentByCateogryChar.addMapping(category, indicesString)); //add a list of index	
				
				index = index + words.size();
				oriIndex = oriIndex + collapseUSPSentByCateogryChar.tokenNumber(words);
				
			} else {
				collapseUSPSentIndexMappingList.add(collapseUSPSentByCateogryChar.addMapping(tagToken, String.valueOf(oriIndex))); //add single index
				
				index +=1;
				oriIndex +=1;
			}
			
			
		}//end of the loop
		return collapseUSPSentIndexMappingList;
	}// end of the replaceAbbrev
	
	
	
	/**
	 * build the index folder for the usp input
	 * @param indexMappingList
	 */
	public String buildIndexFile(List<IndexMapping> indexMappingList,StringBuffer collapsedSentString,StringBuffer collapsedSentIndexString) {
		int lastIndexInPreviousItem = -1;
		int firstIndexInCurrentItem = -1;
		int itemInCollapseUSPSentIndexMappingListCounter = 0;
		for (IndexMapping itemInCollapseUSPSentIndexMappingList : indexMappingList) {
			// System.out.println("itemInCollapseUSPSentIndexMappingList.getToken()::" + itemInCollapseUSPSentIndexMappingList.getToken());
			// System.out.println("itemInCollapseUSPSentIndexMappingList.getIndices()::" + itemInCollapseUSPSentIndexMappingList.getIndices());
			String curToken = itemInCollapseUSPSentIndexMappingList.getToken();
			String curIndices = itemInCollapseUSPSentIndexMappingList.getIndices();
			
			String[] curIndicesArray = curIndices.split(",");
			String[] subFirstIndexArray = curIndicesArray[0].split("-");
			String[] subLastIndexArray = curIndicesArray[curIndicesArray.length-1].split("-");
			
			if (itemInCollapseUSPSentIndexMappingListCounter == 0) {
				firstIndexInCurrentItem = Integer.parseInt(subFirstIndexArray[0]);
			}
			
			if (itemInCollapseUSPSentIndexMappingListCounter > 0) {
				firstIndexInCurrentItem = Integer.parseInt(subFirstIndexArray[0]);
				}
			
			
			// System.out.println("lastIndexInPreviousItem::" + lastIndexInPreviousItem);					
			// System.out.println("firstIndexInCurrentItem::" + firstIndexInCurrentItem);
			
			
			
			if ((lastIndexInPreviousItem+1) < firstIndexInCurrentItem) {
				collapsedSentString.append(" , ").append(itemInCollapseUSPSentIndexMappingList.getToken());
				collapsedSentIndexString.append(",\t\n");
				collapsedSentIndexString.append(itemInCollapseUSPSentIndexMappingList.getToken())
					.append("\t")
					.append(itemInCollapseUSPSentIndexMappingList.getIndices())
					.append("\n");
			} else {
				collapsedSentString.append(" ").append(itemInCollapseUSPSentIndexMappingList.getToken());
				collapsedSentIndexString.append(itemInCollapseUSPSentIndexMappingList.getToken())
					.append("\t").append(itemInCollapseUSPSentIndexMappingList.getIndices())
					.append("\n");
			}
			
			lastIndexInPreviousItem = Integer.parseInt(subLastIndexArray[subLastIndexArray.length-1]);
				
			
			itemInCollapseUSPSentIndexMappingListCounter++;
			
		}
		
		// System.out.println("collapsedSent::" + collapsedSentString);
		// System.out.println("collapsedSentIndexString::" + collapsedSentIndexString);

		
		
		/*
		if ( isCollapsedSentence(sentStanfordTokenizedStringTokens) == true) {
			CollapsedSentenceAndIndex generatedCollapsedSentence = generateCollapsedSentenceAndIndex(sentStanfordTokenizedStringTokens);
			// System.out.println("generatedCollapsedSentence::" + generatedCollapsedSentence.getCollapsedSentence() + "\n");
			
			collapsedSentString = generatedCollapsedSentence.getCollapsedSentence();
			collapsedSentIndexString = generatedCollapsedSentence.getCollapsedSentenceIndex();	

			
			
		} else {
			// System.out.println("sentReplacedByCategoryKwd::" + sentReplacedByCategoryKwd + "\n");
			
			collapsedSentString = sentReplacedByCategoryKwd; // non-collapsed sentence

			
			// reduce this
			
			// CollapsedSentenceAndIndex generatedCollapsedSentence = generateCollapsedSentenceAndIndex(sentStanfordTokenizedStringTokens);
			// collapsedSentIndexString = generatedCollapsedSentence.getCollapsedSentenceIndex();	

			String generatedNonCollapsedSentenceIndexString = generateNonCollapsedSentenceIndexString(sentStanfordTokenizedStringTokens);
			
			collapsedSentIndexString = generatedNonCollapsedSentenceIndexString;
			
			
			// System.out.println("non-collapsed::collapsedSentIndexString::\n" + collapsedSentIndexString + "\n");
			

			
			
		}
		*/
		return collapsedSentString.toString();
	}//end of buildindexfile
	
	
}
