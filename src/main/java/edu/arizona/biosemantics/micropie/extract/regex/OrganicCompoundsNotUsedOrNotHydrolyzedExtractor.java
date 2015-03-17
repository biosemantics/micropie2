package edu.arizona.biosemantics.micropie.extract.regex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.name.Named;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.model.Sentence;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class OrganicCompoundsNotUsedOrNotHydrolyzedExtractor extends AbstractCharacterValueExtractor {

	public OrganicCompoundsNotUsedOrNotHydrolyzedExtractor(@Named("OrganicCompoundsNotUsedOrNotHydrolyzedExtractor_Label")ILabel label) {
		super(label, "organic compounds not used or not hydrolyzed");
	}
	
	public OrganicCompoundsNotUsedOrNotHydrolyzedExtractor(@Named("OrganicCompoundsNotUsedOrNotHydrolyzedExtractor_Label")ILabel label, 
			@Named("OrganicCompoundsNotUsedOrNotHydrolyzedExtractor_Character")String character) {
		super(label, character);
	}

	@Override
	public Set<String> getCharacterValue(String text) {
		// TODO Auto-generated constructor stub
		Set<String> output = new HashSet<String>(); // Output,
		// format::List<String>

		String keywords = "(R)-3-hydroxybutyric acid|1-butanol|1-propalol|1-propanol|2-aminobutyrate|2-aminoethanol|2-butanol|2-keto-gluconate|2-ketogluconate|2-methylbutryic acid|2-methylbutyrate|2-methylpropanol|2-methylpropionate|2-oxoglutarate|2-pentanol|2-propanol|2|2'deoxyadenosine|3-D-hdyroxybutyric acid|3-DL-hydroxybutyrate|3-hydroxy-benzoate|3-hydroxy-butyrate|3-hydroxy-butyric acid|3-hydroxybenzoate|3-hydroxybenzoic acid|3-hydroxybutyrate|3-keto-lactonate|3-ketobutanoic acid|3-ketobutyric acid|3-ketolactonate|3-ketolactose|3-methyl glucose|3-methyl-d-glucose|3-methylbutyrate|3-oxobutanoic acid|3|4-aminobenzoate|4-aminobutyrate|4-diemthylaminobenzaldehyde|4-hydroxy-benzoate|4-hydroxybenzate|4-hydroxybenzoate|4-hydroxybenzoic acid|5-keto-D-gluconate|5-ketogluconate|a-aminovalerate|a-cyclodextrin|a-hydroxybutyric acid|a-keratin|a-ketobutyric acid|a-ketoglutarate|a-ketoglutaric acid|a-ketovaleric acid|Acetamide|Acetate|Acetic|Acetic acid|acetoacetic acid|acetone|acid digest of casein|Aconitate|Aconitic acid|Adenine|Adipate|Adipic acid|Adonitol|Aesculin|Agar|Agarose|alaninamide|Alanine|Albumin|Alginates|alginic acid|Amino acid mixtures|Amino acids|amino butyric acid|Aminobutyric acid|Aminoethanol|Amniotic fluid|Amorphous cellulose|Amygdalin|Amylopectin|Amylose|AOA|AOB|AQDS|Arabinogalactan|Arabinose|Arabitol|Arbutin|archaea cell extract|Archaeal Cell Extract|Archaeal cell-extract|Arginine|Asparagine|Asparagines|Aspartate|aspartic acid|Azelate|b-cyclodextrin|b-hydroxy-DL-butyric acid|b-hydroxybutyric acid|b-keratin|b-methyl D-glucoside|bacterial cell extract|Bacto Peptone|Bacto-tryptone|Barley b-glucan|Beef Extract|Beef heart infusion|Beef infusion|Benzene|Benzoate|Benzoic acid|Beta-lactamase|betaine|Bile|Bile salts|Bio-trypcase|Bio-trypticase|Biosate peptone|Biotrypticase|Biotrypticase|Blood|Blood plasma|Bovine heart infusion|Brain Heart Infusion|bromosuccinic acid|Butan-2-ol|Butanol|Butyrate|Butyric acid|c-aminobutyric acid|c-hydroxybutyric acid|Calf thymus DNA|Caprate|Capric acid|Caprilate|Caproate|Capronate|Caprylate|Carbohydrates|Carboximethylcellulose|Carboxylmethylcellulose|Carboxymethyl cellulose|Carboxymethylcellulose|Carraghenans (Carrageenans)|Casamino acid|Casamino acids|Casein|Casein Hydrolysate|Casitone|Catechol|Cellobiose|Cellulose|Cerebrospinal fluid|Chitin|Chitosan|Chlorella cell extract|Chlorella extract|Cholesterol|Choline|Chondroitin 6-sulfate|Chondroitin sulfate|Chopped lean ground beef|Chopped meat|Chyme|Cinnamate|Cis aconitic acid|Cis-acetic acid|Cis-aconitate|Cis-aconitic acid|Cisaconitic acid|Citrate|Citric acid|Cm cellulose|Cmc-containing plates|Cmcellulose|Coagulated egg|Collagen|Corn oil|Coumarate|Courmarate|Creatine|Crude oil|Crystalline cellulose|Csy-3|Curdlan|Cyclodextrin|Cyclohexanol|Cyclopentanol|Cysteine|Cystine|Cytosine|D-alanine|D-arabinose|D-arabitol|D-celiobiose|D-cellobiose|D-fructose|D-galactonic acid|D-galactonic acid lactone|D-galactose|D-galacturonate|D-galacturonic acid|D-glucaric acid|D-gluconate|D-gluconic acid|D-glucoronate|D-glucosaminic acid|D-glucose|D-Glucuronate|D-glucuronic acid|D-l-glucose|D-lactate|D-lactic acid methyl ester|D-lactulose|D-laevulose|D-lyxose|D-malic acid|D-maltose|D-mannitol|D-mannose|D-melezitose|D-melibiose|D-mellibiose|D-psicose|D-raffinose|D-rhamnose|D-ribose|D-saccharate|D-saccharic acid|D-serine|D-sorbitol|D-tagatose|D-trehalose|D-turanose|D-xylose|Deoxyribonucleic acid|Dextran|Dextran sulfate|Dextran sulphate|Dextrane (aka dextran)|Dextrin|Dextrose|diethanolamine|Diethylamine|Diethylsulfide|Diethylsulphide|Digested serum|Dimenthylamine|Dimethyl disulfide|Dimethyl disulphide|Dimethyl sulfide|Dimethyl sulfone|Dimethyl sulphone|Dimethyl sulphide|Dimethylamine|Dimethylamines|Dimethylsulfide|Dimethylsulfoxide|Dimethylsulphide|Dimethylsulphoxide|diphenylamine|Dithiothreitol|DL-3-hydroxybutyrate|DL-carnitine|DL-lactate|DL-lactic acid|DL-lanthionine|DL-xylose|DMA|DMS|DMSO|Dulcitol|e-Leucine|Edamin|Egg extract|egg yolk|Egg yolk emulsion|Egg yolk oil|Erythritol|Esculin|Esculin ferric citrate|Ethane|Ethanol|Ethanolamine|Ethyl acetate|Extracts of bacteria or archaea cells|Extracts of bacterial and archaebacterial cells|Fecal extract|Fibrinolysin|filter paper|Fish peptone|Fonnate|Formaldehyde|Formate|Formiate|Formic|Formic acid|Fructose|Fucoidan|Fucose|Fumarate|Fumaric acid|g-aminobutyrate|g-aminobutyric acid|Galactosamine|Galactose|Galacturonate|Galacturonic acid|gamma-hydroxybutyric acid|gelatin|Gelatin (hydrolyzed form of collagen)|Gelatin peptone|Gelatine (aka gelatin)|Gelatine peptone|Gelatine peptone|gellan gum|Gelrite|Gelysate|Genitiobiose|Gentiobiose|Gluconate|Gluconic acid|Gluconolactone|Gluconuronate|Glucosamine|Glucose|Glucose-1-phosphate|Glucose-6-phosphate|Glucuriamide|Glucuronamide|Glucuronate|Glucuronic acid|Glutamate|Glutamic acid|Glutarate|Glutaric acid|Glutaric acids|Glutathione|Glycerate|Glycerin|Glycerol|glycerophosphocholine|Glycine|glycl-L-glutaminc acid|Glycogen|Glycolate|glycyl L-aspartic acid|glycyl L-glutamic acid|glycyl-L-aspartic acid|Glyoxylic acid|Guanine|guar gum|Gum arabic|Gum guar|Heart pancreatic digest|Heparin|Heptanoate|Heptose|hippurate|Histidine|Horse blood|Horse manure extract|Horse serum|hydroxy- L-proline|hydroxy-L-proline|Hydroxyethyl cellulose|Hydroxyphenylacetic ACID|I-carrageenan|I-erythritol|Indol|Indole|Inosine|Inositol|Inspissated serum|Inspissiated serum|Inulin|Iso-butanol|Iso-butyrate|Isobutanol|Isobutyrate|Isobutyric|Isobutyric acid|Isoleucine|Isopropalol|Isopropanol|Isovalerate|Isovaleric|Isoveralic acids|Itaconate|Itaconic acid|K-carrageenan|Kdo|Keratin|Ketobutyric acid|Konjac glucomannan|L-a-glycerol phosphate|L-alanine|L-alanyl glycine|l-alanyl-glycine|L-arabinose|L-arabitol|L-arginine|L-ascorbate|L-asparagine|L-aspartate|L-aspartic acid|L-cysteine|L-cystine|L-fucose|L-glucose|L-glutamate|L-glutamic acid|L-glutamine|L-glycine|L-histidine|L-isoleucine|L-lactate|L-leucine|L-lysine|L-malate|L-maltose|L-methionine|L-ornithine|L-phenylalanine|l-proanol|L-proline|L-pyroglutamic acid|L-rhamnose|L-sarcosine|L-serine|L-sorbitol|L-sorbose|L-threonine|L-tryptophan|L-tyrosine|L-valine|L-xylose|L(+)-lactate|Lactalbumin hydrolysate|Lactate|Lactic|Lactic acids|Lactose|Lactulose|Laked blood|Laminarin|Lean ground beef|Lecithin|Lecithovitellin solution|Leifson's O/F medium|Leucine|Leucrose|Levulose|Lichenan (aka Lichenin)|Lichenin|Lipids from egg yolk|Liver extract|Liver infusion|Locust Gum|Locust-bean gum|Lodgepole Pine Needle Extract|LPS Boivin|LPS fraction from E. coli cells|Lymph|Lysed red blood cells|Lysine|Lyxose|M-inositol|M-tartaric acid|Malate|Maleic acid|Malic acid|Malonate|Malonic acid|Malt|Malt Extract|Maltose|maltotriose|Mannitol|Mannose|Manure extract|Meat|Meat extract|Meat peptic digest|Meat peptone|Melezitose|Melibiose|Mercaptans|Mesaconate|Meso-inositol|mesoinositol|Methane thiol|Methanethiol|Methanol|Methionine|Methyl a-D-glucoside|Methyl a-d-mannoside|Methyl a-D-mannoside|Methyl b-D-glucoside|Methyl b-d-xyloside|Methyl pyruvate|Methyl sulfides|Methyl sulphides|Methyl-a-d-mannoside|Methyl-ad-glucopyranoside|Methyl-butyrate|Methylamine|Methylamines|Methylated amines|Methylmercury|Methylpyruvate|Methylsulfides|Methylsulphides|Microcrystalline cellulose|Milk|Mixture of amino acids|Mono-methyl succinate|Monomethyl succinate|Mucic acid|myo-inositol|Myoinositol|N-acetyl d-galactosamine|N-acetyl D-glucosamine|N-acetyl-galactosamine|N-acetyl-glucosamine|N-acetylgalactosamine|N-acetylglucosamine|n-butanol|N-butyrate|n-butyric acid|N-butyric acids|N-talosaminuronic acid|Neopeptone|nicotinic acid|ninhydrin|Nitrilotriacetic acid|Nonanoate|Olive oil|optochin|Ornithine|Ox bile|Ox bile salts|Ox gall|Ox-bile salts|Oxalate|Oxalic acid|Oxgall|Oxidized glutatione|p-aminobenzoate|p-aminobenzoic acid|p-hydroxy phenlyacetic acid|P-hydroxyphenyl acetic acid|p-hydroxyphenylacetic acid|p-hydroxyphenylacetic acid|P-hydroxyphenylacetic acid|Palatinose|Palmitate|Pancreatic digest of casein|Pancreatic digest of gelatin|Pancreatic digest of Gelatin peptone|Papaic digest of soybean|Papaic digest of soybean meal|para-aminobenzoic acid|Pectin|Peptic digest of animal tissue|Peptone|Peptone water|Phenol|Phenyl acetate|Phenyl ethylamine|Phenyl-acetate|Phenylacetate|Phenylacetic acid|Phenylalanine|Phenylethylamine|phosphatidylcholine|phosphocholine|Phytagel|Pimeliate|Pine needle extract|Plasma|poly-beta-hydroxybutyrate|Polyanethol sulfonate|Polypectate|Polypeptone|Potassium 2-ketogluconate|Potassium 5-ketogluconate|Potassium gluconate|PPLO serum fraction|Procine serum|Proline|Propan-2-ol|Propane|Propanoate|Propanoic acid|Propanol|Propionate|Propionic|Propionic acid|Propionic acids|Propylene|Proteose peptone|Protoheme|protoporphyrin|Psicose|Pullulan|Pure agar|Putrescine|Pyruvate|Pyruvic acid|Pyruvic acid methyl ester|Pyruvic acidmethyl ester|Quinic acid|Rabbit blood|Rabbit laked blood|Raffinose|rafinose|Raflinose|Ramnose|Rhamnose|Rhamnoside|Ribitol|Ribose|Rumen fluid|Ruminal fluid|Saccharate|Saccharic acid|Saccharose|Salicin|Salicylate|Saliva|Sarcosine|Seawater Agar|Sebacic acid|Serine|Serum|Sheep blood|Skim cow's milk|Skim milk|Skimmed milk|Sludge fluid|Sodium acetate|Sodium citrate|Sodium d-galacturonate|Sodium formate|Sodium fumarate|Sodium gluconate|Sodium lactate|Sodium lactic acid|Sodium malate|Sodium malonate|Sodium propionate|Sodium pyruvate|Sodium succinate|Sodium tartrate|Soil extract|Soluble starch|Sorbitol|Sorbose|Soy extract|Soy peptone|Soya bean extract|Soya broth|Soya extract|Soybean extract|Soytone|spermidine|spermine|sphingomyelin|Spissated serum|Stachyose|Starch|Stearate|Sterol|Sterols|Suberate|Suberic acid|Succinamic acid|Succinate|succinic|Succinic acid|Succinic acid mono-methyl ester|Succinic acid mono-methyl-ester|Succinic acid monomethyl ester|Succinic acid monomethyl- ester|Succinic acids|Succinoglucan|Sucrose|Sym-homospermidine|Synovial fluid|T-aconitate|Tagatose|Tartaric acid|Tartrate|Tetramethylammonium|Tetrathionate|Theobromine|Theophylline|Thioglycolate|Thioglycollate|Thiourea|Threonine|Thymidine|TMA|TMAO|TMP|Toluene|Trans-aconitate|Trehalose|triethylamine|Trimethoxybenzoate|Trimethylamine|Trimethylamine N-oxide|Trimethylamines|Trisodium citrate|Trypticase|Trypticase peptone|Tryptone|Tryptone soy agar|Tryptophan|Tryptose|Turanose|Tureen fluid|Tween 20|Tween 40|Tween 80|Tweens|Tweens 40 and 80|Tyrosine|UMP|Urate|Urea|Uric acid|Uridine|Urocanic acid|Valerate|Valeric acid|Valeric acids|Valine|Vanillate|VFA|Vitamin K1|Washed red blood cells|Washed red cells|Whole milk|Xanthan|Xanthan Gum|Xanthine|XeMM|Xylan|Xylitol|Xylose|Yeast enriched peptone|Yeast extract|Yeastolate|Yeastrel";
		String[] keywordsArray = keywords.split("\\|");
		// System.out.println("keywordsArray.length::" + keywordsArray.length);
		
		// java string array set string - Google Search
		// collections - Java - easily convert array to set - Stack Overflow
		// http://stackoverflow.com/questions/3064423/java-easily-convert-array-to-set
		
		// java string[] set string - Google Search
		// arrays - Java/ How to convert String[] to List or Set - Stack Overflow
		// http://stackoverflow.com/questions/11986593/java-how-to-convert-string-to-list-or-set
		
		
		// Set<String> keywordList = new HashSet<String>(Arrays.asList("a", "b"));
		Set<String> keywordList = new HashSet<String>(Arrays.asList(keywordsArray)); 
		
		// System.out.println("Sent :" + sent);
		// text = text.substring(0, text.length()-1);
		// text = " " + text + " ";
		
		// String[] sentenceArray = text.split("\\.|\\band\\b");
		String[] sentenceArray = text.split("\\.");
		System.out.println("sentenceArray.length :" + sentenceArray.length);
		
		for ( int i = 0; i < sentenceArray.length; i++ ) {
			
			String subText = sentenceArray[i] + ".";
			System.out.println("subText :" + subText);

			int caseNumber = 0;

			if ( subText.matches("(.+)(\\bno hydrolysis|are not hydrolyzed|is not hydrolyzed|is not used|are not used\\b)(.+)") ) {
				caseNumber = 1;
				
			}
			
			
			switch(caseNumber) {
			case 1:
				System.out.println("Case 1:");
				System.out.println("subText :" + subText);
				subText = subText.substring(0, subText.length()-1);
				subText = " " + subText + " ";		

				Set<String> returnCharacterStrings = new HashSet<String>();

				for (String keywordString : keywordList) {
					keywordString = keywordString.toLowerCase();
					keywordString = keywordString.replace("+", "\\+");
					
					String patternString = "\\s"+keywordString+"\\,|\\s"+keywordString+"\\s|^"+keywordString+"\\s|^"+keywordString+"\\,"; // regular expression pattern
					// String patternString = "(.*)(\\b"+keywordString+"\\b)(.*)"; // regular expression pattern

					Pattern pattern = Pattern.compile(patternString);
					Matcher matcher = pattern.matcher(subText.toLowerCase());			
					if (matcher.find() && keywordString.length() > 1) {
						String matchString = matcher.group().trim();
						if(matchString.substring(matchString.length()-1, matchString.length()).equals(",")) {
							matchString = matchString.substring(0, matchString.length()-1);
						}
						returnCharacterStrings.add(matchString);
						System.out.println(keywordString + "::" + matchString);
					}
				}
				output.addAll(returnCharacterStrings);
				
				break;
			case 2:

				break;
			default:
				// System.out.println("");
				// System.out.println("Go to Case 0::");
			}			
		}
		
		
		


		return output;
	}


	// Example: 
	public static void main(String[] args) throws IOException {
		
		
		
		System.out.println("Start::");
		
		
		OrganicCompoundsNotUsedOrNotHydrolyzedExtractor organicCompoundsNotUsedOrNotHydrolyzedExtractor = new OrganicCompoundsNotUsedOrNotHydrolyzedExtractor(Label.c52);
		
		// Test on February 09, 2015 Mon
		CSVSentenceReader sourceSentenceReader = new CSVSentenceReader();
		// Read sentence list
		// 
		String sourceFile = "micropieInput_zip/training_data/150130-Training-Sentences-new.csv";
		String svmLabelAndCategoryMappingFile = "micropieInput_zip/svmlabelandcategorymapping_data/SVMLabelAndCategoryMapping.txt";
		sourceSentenceReader.setInputStream(new FileInputStream(sourceFile));
		sourceSentenceReader.setInputStream2(new FileInputStream(svmLabelAndCategoryMappingFile));
		sourceSentenceReader.readSVMLabelAndCategoryMapping();
		List<Sentence> sourceSentenceList = sourceSentenceReader.readSentenceList();
		System.out.println("sourceSentenceList.size()::" + sourceSentenceList.size());

		
		String outputFile = "micropieOutput/organicCompoundsNotUsedOrNotHydrolyzedExtractor-150317.csv";
		OutputStream outputStream = new FileOutputStream(outputFile);
		CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF8")));
		List<String[]> lines = new LinkedList<String[]>();
		
		
		int sampleSentCounter = 0;
		int extractedValueCounter = 0;
		
		for (Sentence sourceSentence : sourceSentenceList) {
			String sourceSentText = sourceSentence.getText();
			sourceSentText = sourceSentText.toLowerCase();
			// "organic compounds used or hydrolyzed"
			
			
			
			if ( sourceSentText.matches("(.+)(\\bno hydrolysis|are not hydrolyzed|is not hydrolyzed|are not useed|is not used\\b)(.+)") ) {	
				System.out.println("\n");
				System.out.println("sourceSentText::" + sourceSentText);
				
				
				
				Set<String> organicCompoundsNotUsedOrNotHydrolyzedResult = organicCompoundsNotUsedOrNotHydrolyzedExtractor.getCharacterValue(sourceSentText);
				
				// System.out.println("growthNaclMaxExtractor.getRegexResultWithMappingCaseMap()::" + organicCompoundsNotUsedOrNotHydrolyzedExtractor.getRegexResultWithMappingCaseMap().toString());
				
				String regexResultWithMappingCaseMapString = "";
				
				// for (Map.Entry<String, String> entry : organicCompoundsNotUsedOrNotHydrolyzedExtractor.getRegexResultWithMappingCaseMap().entrySet()) {
				//	System.out.println("Key : " + entry.getKey() + " Value : "
				//	 	+ entry.getValue());
				// 
				//	regexResultWithMappingCaseMapString += entry.getKey() + ":" + entry.getValue() + ", ";
				//	
				// }
				
				System.out.println("organicCompoundsNotUsedOrNotHydrolyzedResult::" + organicCompoundsNotUsedOrNotHydrolyzedResult.toString());
				if ( organicCompoundsNotUsedOrNotHydrolyzedResult.size() > 0 ) {
					extractedValueCounter +=1;
				}
				sampleSentCounter +=1;
				
				System.out.println("regexResultWithMappingCaseMapString::" + regexResultWithMappingCaseMapString);

				
				// lines.add(new String[] { sourceSentText,
				//		regexResultWithMappingCaseMapString
				//		} );
				lines.add(new String[] { sourceSentText } );
				
			}
			
		
		
		} 

		System.out.println("\n");
		System.out.println("sampleSentCounter::" + sampleSentCounter);
		System.out.println("extractedValueCounter::" + extractedValueCounter);

		
		writer.writeAll(lines);
		writer.flush();
		writer.close();	
		

		
	}
	
	
	


}
