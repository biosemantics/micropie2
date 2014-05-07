package usp.eval;

import java.util.*;
import java.io.*;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.model.SentenceDependency;
import edu.stanford.nlp.ling.StringLabelFactory;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeReader;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import usp.semantic.*;
import usp.syntax.*;
import usp.util.Pair;
import usp.util.Utils;


public class MicropieUSPExtractor {

	static Set<Character> vowels_=new HashSet<Character>();
	static {
		vowels_.add('a');
		vowels_.add('e');
		vowels_.add('i');
		vowels_.add('o');
		vowels_.add('u');
	}

	static Map<Question,Set<Answer>> qas_=new TreeMap<Question,Set<Answer>>();
	static String evalDir_="eval";
	static String rstDir_="results";
	static String dataDir_="genia";
	
	static Map<String, ArrayList<Question>> rel_qs_=new TreeMap<String, ArrayList<Question>>();  
	
	// identify verbs: dep->multiple argclust
	static Map<String,Integer> rel_clustIdx_=new HashMap<String,Integer>();
	
	static Set<String> qForms_=new HashSet<String>();
	static Set<String> qLemmas_=new HashSet<String>();
	static Map<String,Set<String>> form_lemma_=new HashMap<String,Set<String>>();

	// AB: B->ci,A
	static Map<Pair<String,String>,String> headDep_clustIdxs_=new HashMap<Pair<String,String>,String>();
	
	static Map<String,Set<String>> lemma_clustIdxs_=new HashMap<String,Set<String>>();
	static Map<Integer,Map<String,Integer>> clustIdx_depArgClustIdx_=new HashMap<Integer,Map<String,Integer>>();

	static Map<String,ArrayList<ArrayList<String>>> arg_cis_=new HashMap<String,ArrayList<ArrayList<String>>>();
		// multiple cis: each a possible parse
		// cis: multiple node, each w. multiple possible cis
	
	// part
	static Map<String, Pair<Integer,String>> ptId_clustIdxStr_=new HashMap<String,Pair<Integer,String>>();
	static Map<String, Map<Integer,Set<String>>> ptId_aciChdIds_=new HashMap<String, Map<Integer,Set<String>>>();
	static Map<String,String> ptId_parDep_=new HashMap<String,String>();	// ptid -> par dep
	static Map<Integer,Set<String>> clustIdx_ptIds_=new HashMap<Integer,Set<String>>();

	// sent
	static Map<String,String> id_sent_=new HashMap<String,String>();
	
	// dep: to generate final answer
	static Map<String,Article> id_article_=new HashMap<String, Article>();
	
	static Set<String> allowedDeps_=new HashSet<String>();
	static {
		allowedDeps_.add("nn");
		allowedDeps_.add("amod");
		allowedDeps_.add("prep_of");
		allowedDeps_.add("num");
		allowedDeps_.add("appos");
	}
	
	
	static Map<Integer,Map<String,String>> clustIdx_argTypeClustIdx_=new HashMap<Integer,Map<String,String>>();
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		
		MicropieUSPExtractor usp = new MicropieUSPExtractor();
		
		Set<String> output = new HashSet<String>();
		
		// Set<String> output = usp.getObjectValue("Produces arginine dihydrolase and lysine decarboxylase, but not ornithine decarboxylase.", "produces", "V", "dobj");
		
		// Set<String> output = usp.getObjectValue("Hydrogen sulfide is produced.", "produced", "V", "nsubjpass");
		// System.out.println(output.toString());
		
		
		// Resistant to vancomycin, ampicillin, ristocetin, novobiocin, rifampicin, streptomycin, and chloramphenicol.
		// Set<String> output = usp.getObjectValue("Resistant to vancomycin, ampicillin, ristocetin, novobiocin, rifampicin, streptomycin, and chloramphenicol.", "resistant", "J", "prep_to");
		// System.out.println(output.toString());
		
		
		// Resistant to chloramphenicol, ampicillin, penicillin, kanamycin, vancomycin and streptomycin, but sensitive to rifampicin.
		// Set<String> output = usp.getObjectValue("Resistant to chloramphenicol, ampicillin, penicillin, kanamycin, vancomycin and streptomycin, but sensitive to rifampicin.", "resistant", "J", "prep_to");
		// System.out.println(output.toString());		
		// Set<String> output2 = usp.getObjectValue("Resistant to chloramphenicol, ampicillin, penicillin, kanamycin, vancomycin and streptomycin, but sensitive to rifampicin.", "sensitive", "J", "prep_to");
		// System.out.println(output2.toString());
		
		// nsubjpass examples
		// Hydrogen sulfide is produced.
		// Relatively strong turbidity is produced containing serum.
		
		// Isolated from a commercial chalcocite heap leaching operation in Myanmar.
		// Both strains were isolated from solar saltern crystallizer ponds.
		// Set<String> output = usp.getObjectValue("Both strains were isolated from solar saltern crystallizer ponds.", "isolated", "V", "prep_from", "parse");
		// System.out.println(output.toString());

		// Glucose and arginine are utilized.
		// Set<String> output = usp.getObjectValue("Glucose and arginine are utilized.", "utilized", "V", "nsubjpass", "dep");
		//System.out.println(output.toString());
		// [arginine, Glucose]

				
		// Mycoplasma synoviae ferments glucose aerobically and anaerobically, does not utilize arginine or urea, does not ferment tetrazolium chloride or show phosphatase activity, and produces film and spots.
		// Set<String> output = usp.getObjectValue("Mycoplasma synoviae ferments glucose aerobically and anaerobically, does not utilize arginine or urea, does not ferment tetrazolium chloride or show phosphatase activity, and produces film and spots.", "utilize", "V", "dobj", "dep");
		// System.out.println(output.toString());
		
		// Utilizes L-arabinose, D-fructose, D-glucose, D-xylose and glutamic acid, but not D-galactose or serine as the sole energy source.
		// Set<String> output = usp.getObjectValue("Utilizes L-arabinose, D-fructose, D-glucose, D-xylose and glutamic acid, but not D-galactose or serine as the sole energy source.", "utilizes", "V", "dobj", "dep");
		// System.out.println(output.toString());
		

		
		// Set<String> output = usp.getObjectValue("Utilizes both H2/CO2 and formate for methane production.", "utilizes", "V", "dobj", "dep");
		// System.out.println(output.toString());

		// Utilizes glucose, fructose, glycerol, maltose, trehalose, starch, propionate, fumarate, acetate, threonine, asparagine and lysine as single carbon and energy sources for growth."
		// output = usp.getObjectValue("Utilizes glucose, fructose, glycerol, maltose, trehalose, starch, propionate, fumarate, acetate, threonine, asparagine and lysine as single carbon and energy sources for growth.", "utilizes", "V", "dobj", "dep");
		// System.out.println(output.toString());		

		// Utilizes acetate, lactate, malic acid, fumaric acid, sucrose, L-glutamic acid, glucose, fructose, succinate, lactose, DL-aspartic acid, pyruvate, glycine, galactose, sorbitol, glycerol, starch, L-histidine, trehalose, DL-norleucine, D-glucuronic acid, DL-phenylalanine, aesculin and salicin, but not L-arginine, L-alanine, sodium citrate, xylose, mannitol, L-threonine, dulcitol, dextrin, L-methionine, 3,3-dimethylglutaric acid or L-tyrosine.
		// output = usp.getObjectValue("Utilizes acetate, lactate, malic acid, fumaric acid, sucrose, L-glutamic acid, glucose, fructose, succinate, lactose, DL-aspartic acid, pyruvate, glycine, galactose, sorbitol, glycerol, starch, L-histidine, trehalose, DL-norleucine, D-glucuronic acid, DL-phenylalanine, aesculin and salicin, but not L-arginine, L-alanine, sodium citrate, xylose, mannitol, L-threonine, dulcitol, dextrin, L-methionine, 3,3-dimethylglutaric acid or L-tyrosine.", "utilizes", "V", "dobj", "dep");
		// System.out.println(output.toString());		
		
		
		// output = usp.getObjectValue("Utilizes H2/CO2, formate, 2-propanol/CO2 and 2-butanol/ CO2 for growth and/or methane production.", "utilizes", "V", "dobj", "dep");
		// System.out.println(output.toString());
		
		
		// Sensitive to (μg per disc) azithromycin (15), novobiocin (30), tetracycline (30), neomycin (30), cefoxitin (30), ceftriaxone (30), ceftizoxime (30), cefotaxime (30), lincomycin (2), cefazolin (30), rifampicin (5), spectinomycin (100), ofloxacin (5), amoxicillin (10), imipenem (10), cefixime (5), cefalexin (30), nitrofurantoin (300), vancomycin (30), gentamicin (10), streptomycin (10), furazolidone (300), cephradine (30), chloramphenicol (30), clindamycin (2), cefuroxime (30), cefoperazone (75), piperacillin (100), levofloxacin (5), teicoplanin (30) and norfloxacin (10).
		// kwd::sensitive::type::J

		// Resistant to kanamycin (30 μg), gentamicin (10 μg), neomycin (30 μg) and polymyxin B (300 μg), but sensitive to ampicillin (10 μg), penicillin (10 IU), streptomycin (10 μg) and tetracycline (30 μg).

		// Set<String> output = usp.getObjectValue("Resistant to kanamycin (30 μg), gentamicin (10 μg), neomycin (30 μg) and polymyxin B (300 μg), but sensitive to ampicillin (10 μg), penicillin (10 IU), streptomycin (10 μg) and tetracycline (30 μg).",
		// 		"resistant", "J", "prep_to", "dep");
		// System.out.println(output.toString());
		
		// Resistant to kanamycin, gentamicin, neomycin and polymyxin B, but sensitive to ampicillin, penicillin, streptomycin and tetracycline.
		// Set<String> output = usp.getObjectValue("Resistant to kanamycin, gentamicin, neomycin and polymyxin B, but sensitive to ampicillin, penicillin, streptomycin and tetracycline.",
		//		"resistant", "J", "prep_to", "dep");
		// System.out.println(output.toString());
		
		
		
		// Resistant to kanamycin, gentamicin, neomycin and polymyxin B.
		// Resistant to kanamycin , gentamicin , neomycin and polymyxin B.
		// Set<String> output = usp.getObjectValue("Resistant to kanamycin, gentamicin, neomycin and polymyxin B.",
		// 		"resistant", "J", "prep_to", "dep");
		// System.out.println(output.toString());
		
		
		// Utilizes Tween 40, d-galactose, gentiobiose, α-d-glucose, mono-succinate, citric acid, d-glucuronic acid, succinamic acid, succinic acid, alaninamide, glycyl l-aspartic acid, hydroxy-l-proline, l-ornithine, l-pyroglutamic acid, urocanic acid, thymidine, 2-aminoethanol and glycerol.
		// Set<String> output = usp.getObjectValue("Utilizes Tween 40, d-galactose, gentiobiose, α-d-glucose, mono-succinate, citric acid, d-glucuronic acid, succinamic acid, succinic acid, alaninamide, glycyl l-aspartic acid, hydroxy-l-proline, l-ornithine, l-pyroglutamic acid, urocanic acid, thymidine, 2-aminoethanol and glycerol.",
		//		"utilizes", "V", "dobj", "dep");
		// System.out.println(output.toString());
		
		
		// Indole is not produced.
		// Indole and catalase are not produced.
		// Hydrogen sulfide and indole are not produced.
		// Does not utilize inulin, lactose, maltose, D-mannitol, D-mannose, melibiose, rhamnose, D-ribose, sorbitol, sucrose, D-xylose or citrate.
		// Set<String> output = usp.getObjectValue("Indole and catalase are not produced.",
		//		"produced", "V", "nsubjpass", "dep");
		// System.out.println(output.toString());
		
		// Does not utilize inulin, lactose, maltose, D-mannitol, D-mannose, melibiose, rhamnose, D-ribose, sorbitol, sucrose, D-xylose or citrate.
		output = usp.getObjectValue("Does not utilize inulin, lactose, maltose, D-mannitol, D-mannose, melibiose, rhamnose, D-ribose, sorbitol, sucrose, D-xylose or citrate.",
				"utilize", "V", "dobj", "dep");
		System.out.println(output.toString());
		
		
		// Utilizes arabinose, xylose, cellobiose, fructose, galactose, glucose, mannose, maltose, melibiose, glycogen and soluble starch as growth substrates and produces acids.
		output = usp.getObjectValue("Utilizes arabinose, xylose, cellobiose, fructose, galactose, glucose, mannose, maltose, melibiose, glycogen and soluble starch as growth substrates and produces acids.",
				"utilizes", "V", "dobj", "dep");
		System.out.println(output.toString());
		
		
		// Cells are not flagellated.
		// Set<String> output = usp.getObjectValue("Cells are not flagellated.",
		// 			"Cells", "N", "amod", "dep");
		// System.out.println(output.toString());
		
		// Cells are Gram-negative rod-shaped non-sporulating and motile.
		// Set<String> output = usp.getObjectValue("Cells are Gram-negative rod-shaped non-sporulating and motile.",
		// 		"cells", "N", "amod", "dep");
		// System.out.println(output.toString());
		
		// Cells are not square and heavy.
		// Cells are square and round.
		// Set<String> output = usp.getObjectValue("Cells are not square and heavy.",
		// 		"cells", "N", "nsubj", "dep");
		// System.out.println(output.toString());
		
		// The cells are not motile.
		// Set<String> output = usp.getObjectValue("The cells are not motile.",
		//		"cells", "N", "nsubj", "dep");
		// System.out.println(output.toString());
		
		// It has no oxidase, arginine dihydrolase or urease activity.
		// Set<String> output = usp.getObjectValue("It has no oxidase, arginine dihydrolase or urease activity.",
		// 		"has", "V", "dobj", "dep");
		// System.out.println(output.toString());
		
		// Cells have no flagella.
		// Set<String> output = usp.getObjectValue("Cells have no flagella.",
		// 		"have", "V", "dobj", "dep");
		// System.out.println(output.toString());
		
		// Aggregates were never found.
		// Set<String> output = usp.getObjectValue("Aggregates were never found.",
		//			"found", "V", "nsubjpass", "dep");
		// System.out.println(output.toString());
		
		
		// No gas produced.
		// Set<String> output = usp.getObjectValue("No gas produced.",
		// 		"produced", "V", "nsubjpass", "dep");
		// System.out.println(output.toString());
		
		// output = usp.getObjectValue("No gas is produced.",
		// 		"produced", "V", "nsubjpass", "dep");
		// System.out.println(output.toString());
		
		// output = usp.getObjectValue("Oxidase and urease are not produced.",
		// 		"produced", "V", "nsubjpass", "dep");
		// System.out.println(output.toString());

		// Glycerol and starch utilized.
		// No glycerol and starch utilized.
		// Set<String> output = usp.getObjectValue("No glycerol and starch utilized.",
		//		"utilized", "V", "nsubj", "dep");
		// System.out.println(output.toString());
		
		// Glycerol and starch are utilized.
		// System.out.println("Glycerol and starch are utilized.");
		// Set<String> output = usp.getObjectValue("Glycerol and starch are utilized.",
		//		"utilized", "V", "nsubjpass", "dep");
		// System.out.println(output.toString());
		
		// No glycerol and starch are utilized.
		System.out.println("No glycerol and starch are utilized.");
		output = usp.getObjectValue("No glycerol and starch are utilized.",
				"utilized", "V", "nsubjpass", "dep");
		System.out.println(output.toString());
		
		// Metasub1, EEE, and FFF are not utilized.
		System.out.println("Metasub1, EEE, and FFF are not utilized.");
		output = usp.getObjectValue("Metasub1, EEE, and FFF are not utilized.",
				"utilized", "V", "nsubjpass", "dep");
		System.out.println(output.toString());
		
		// Metasub1, EEE, and FFF are utilized.
		System.out.println("Metasub1, EEE, and FFF are utilized.");
		output = usp.getObjectValue("Metasub1, EEE, and FFF are utilized.",
				"utilized", "V", "nsubjpass", "dep");
		System.out.println(output.toString());
		
		// Rule 3
		// Glycerol and starch utilized.
		// System.out.println("Glycerol and starch utilized.");
		// output = usp.getObjectValue("Glycerol and starch utilized.",
		//		"utilized", "V", "nsubj", "dep");
		// System.out.println(output.toString());
		
		// No glycerol and starch utilized.
		// System.out.println("No glycerol and starch utilized.");
		// output = usp.getObjectValue("No glycerol and starch utilized.",
		//		"utilized", "V", "nsubj", "dep");
		// System.out.println(output.toString());
		
		// Glycerol and starch not utilized.
		// System.out.println("Glycerol and starch not utilized.");
		// output = usp.getObjectValue("Glycerol and starch not utilized.",
		//		"utilized", "V", "nsubj", "dep");
		// System.out.println(output.toString());
		
		// Glycerol and starch utilized.
		// System.out.println("Glycerol and starch utilized.");
		// output = usp.getObjectValue("Glycerol and starch utilized.",
		// 		"utilized", "V", "nsubj", "dep");
		// System.out.println(output.toString());
		
		
		
		// Sensitive to tetracycline and vancomycin, but not to norfloxacin, ciprofloxacin, streptomycin, kanamycin or ampicillin.
		// System.out.println("Sensitive to tetracycline and vancomycin, but not to norfloxacin, ciprofloxacin, streptomycin, kanamycin or ampicillin.");
		// output = usp.getObjectValue("Sensitive to tetracycline and vancomycin, but not to norfloxacin, ciprofloxacin, streptomycin, kanamycin or ampicillin.",
		//		"sensitive", "J", "prep_to", "dep");
		// System.out.println(output.toString());
		
		// These avian ureaplasma strains have been reported to hydrolyze urea, but not to catabolize arginine or glucose.
		// System.out.println("These avian ureaplasma strains have been reported to hydrolyze urea, but not to catabolize arginine or glucose.");
		// output = usp.getObjectValue("These avian ureaplasma strains have been reported to hydrolyze urea, but not to catabolize arginine or glucose.",
		// 		"hydrolyze", "V", "dobj", "dep");
		// System.out.println(output.toString());
		
		
		// To hydrolyze urea but not to catabolize arginine or glucose.
		// System.out.println("To hydrolyze urea but not to catabolize arginine or glucose.");
		// output = usp.getObjectValue("To hydrolyze urea but not to catabolize arginine or glucose.",
		//		"hydrolyze", "V", "dobj", "dep");
		// System.out.println(output.toString());
		
		// It was reported to rub AAA and BBB but not to wash CCC and DDD.
		// System.out.println("It was reported to rub AAA and BBB but not to wash CCC and DDD.");
		// output = usp.getObjectValue("It was reported to rub AAA and BBB but not to wash CCC and DDD.",
		//		"rub", "V", "dobj", "dep");
		// System.out.println(output.toString());
		
		// Sensitive to tetracycline and vancomycin, but not to norfloxacin, ciprofloxacin, streptomycin, kanamycin or ampicillin.
		// Set<String> output = usp.getObjectValue("Sensitive to tetracycline and vancomycin, but not to norfloxacin, ciprofloxacin, streptomycin, kanamycin or ampicillin.",
		//		"sensitive", "J", "prep_to", "dep");
		// System.out.println(output.toString());		
		
		
		
		// Methane is produced from H2/CO2 and formate.
		// Acid is produced from xylose, arabinose, glucose, sucrose, maltose, lactose (weak) and starch.
		// Methane is produced from H2/CO2, formate, 2-propanol/CO2 and 2-butanol/CO2.
		// Set<String> output = usp.getObjectValue("Methane is produced from H2/CO2, formate, 2-propanol/CO2 and 2-butanol/CO2.", "produced", "V", "prep_from");
		// System.out.println(output.toString());
		
		
		// Cyanobacteria with tightly coiled trichomes are frequently found in thermal freshwater environments as well as in brackish, marine and hypersaline waters, Under favourable conditions they can form dense benthic populations and make major contributions to primary productivity, On the basis of the tightness of the helix, thin cross-walls (invisible by light microscopy) and several ultrastructural features, they are morphologically distinguished from a variety of other cyanobacteria with more loosely helical or sinuous trichomes, Marine strains were somewhat variable with respect to salinity optima and tolerances.
		// Set<String> output = usp.getObjectValue("Cyanobacteria with tightly coiled trichomes are frequently found in thermal freshwater environments as well as in brackish, marine and hypersaline waters, Under favourable conditions they can form dense benthic populations and make major contributions to primary productivity, On the basis of the tightness of the helix, thin cross-walls (invisible by light microscopy) and several ultrastructural features, they are morphologically distinguished from a variety of other cyanobacteria with more loosely helical or sinuous trichomes, Marine strains were somewhat variable with respect to salinity optima and tolerances.", "found", "V", "prep_in");
		// System.out.println(output.toString());
		
		// XXX be required for YYY
		// Supplements of elemental sulfur are not required for the chemoheterotrophic growth.
		// Set<String> output = usp.getObjectValue("Supplements of elemental sulfur are not required for the chemoheterotrophic growth.", "required", "V", "prep_for");
		// System.out.println(output.toString());
		
		// XXX be required as YYY
		// Acetate is required as a carbon source for growth.
		// Set<String> output = usp.getObjectValue("Acetate is required as a carbon source for growth.", "required", "V", "prep_as");
		// System.out.println(output.toString());
		
		
		
		
		
		
		
		
		
		
		
	}

	
	public MicropieUSPExtractor() {
		
	}
	
	
	public Set<String> getObjectValue(String text, String keyword, String keywordType, String keywordObject, String extractionType) throws Exception {
		
		Set<String> output = new HashSet<String>(); // Output, format::List<String>
		String USPId = "";
				
		rstDir_ = "usp_results";
		dataDir_ = "usp";		

		Map<String, Map<String,Integer>> keywordList_ = new TreeMap<String,Map<String,Integer>>();
		keywordList_.put(keyword, new HashMap<String, Integer>());
		
		String dir = rstDir_;
		String fid = dataDir_;
		if (fid.indexOf(Utils.FILE_SEP)>=0) fid=fid.substring(fid.lastIndexOf(Utils.FILE_SEP));

		
		String fileName=dir+Utils.FILE_SEP+fid+".mln";
		readClust2(fileName, keywordList_);
		
		fileName=dir+Utils.FILE_SEP+fid+".parse";
		readPart(fileName);
		
		// readSents();	// also read dep
		
		for (Map.Entry<String, Map<String,Integer>> entry : keywordList_.entrySet()) {

			String key = entry.getKey(); // keyword
			// System.out.println("Keyword::" + key);

			
			Map<String,Integer> clustIdx_pos_ = entry.getValue();
			// System.out.println("clustIdx_pos::" + clustIdx_pos_.toString());
			
			for (Map.Entry<String,Integer> clustIdx_pos_entry_ : clustIdx_pos_.entrySet()) {

				String pos = clustIdx_pos_entry_.getKey(); // type, ex: V, N, and J
				int ci= clustIdx_pos_entry_.getValue(); // ci: clustIdx, cluster index


				// System.out.println("cluster index::\n" + ci);
				// System.out.println("clustIdx_depArgClustIdx_.get(ci)::\n" + clustIdx_depArgClustIdx_.get(ci));
				// System.out.println("clustIdx_argTypeClustIdx_.get(ci)::\n" + clustIdx_argTypeClustIdx_.get(ci));
				// System.out.println("clustIdx_depArgClustIdx_.get(ci).get(\"nsubj\")::" + clustIdx_depArgClustIdx_.get(ci).get("nsubj"));				
				
				
				
				// Build different rules for different types such as V, N, and J
				Set<String> pids=clustIdx_ptIds_.get(ci);
				// System.out.println("pids :: " + pids);
				// System.out.println("pids.size() :: " + pids.size());

				
				// Rule 1:: V => dobj
				
				// V obj1, obj2, ...
				// Does not V obj1, obj2, ...
				


				if ( pos.equals("V") && pos.equals(keywordType) && keywordObject.equals("dobj") ) {
					if ( clustIdx_depArgClustIdx_.get(ci).get("dobj") == null ) continue; // doesn't go through the following
					int aci = clustIdx_depArgClustIdx_.get(ci).get("dobj"); 
					// System.out.println("aci is ::" + aci );

					for (String pid:pids) {
						
						if (ptId_aciChdIds_.get(pid)!=null) { 
							// System.out.println("pid is ::" + pid);
							// System.out.println("ptId_aciChdIds_.get(pid).toString() ::" + ptId_aciChdIds_.get(pid).toString());
							
							
							if (ptId_aciChdIds_.get(pid).get(aci)!=null) {
								for (String cid:ptId_aciChdIds_.get(pid).get(aci)) {
									// System.out.println("cid is ::" + cid);
									String sentId = cid.split(":")[0];
									// System.out.println("sentId is ::" + sentId);

									String txtFileName = dataDir_+ "/text/0/" + sentId + ".txt";
									String sentText = readDepFromTxtFile(txtFileName); // collapsed sentence
									
									String oriTxtFileName = dataDir_+ "/text_o/0/" + sentId + ".txt";
									String oriSentText = readDepFromTxtFile(oriTxtFileName);
									
									
									// System.out.println("text::" + text);
									// System.out.println("oriSentText::" + oriSentText);
									// System.out.println("sentText(collapsed sentence)::" + sentText);
									
									if (text.equals(oriSentText)) {
									// if (text.equals(sentText)) {
										// Go to .dep to grab the result back
										// to see how much we can get								
										
										String depFileName = dataDir_+ "/dep/0/" + sentId + ".dep";
										List<SentenceDependency> depList = readDepFromDepFile(depFileName);
										
										// System.out.println("depFileName::" + depFileName);
										System.out.println("\nRule 1::text::" + text);
										
										USPId = sentId;

										System.out.println("keyword::" + keyword);
										System.out.println("depFileName::" + depFileName);
										// System.out.println("text::" + text);
										// System.out.println("oriSentText::" + oriSentText);
										System.out.println("sentText(collapsed sentence)::" + sentText);										
										
										
										String dobjStringList = "";
										Map<String, String> outputMap = new HashMap<String, String>();										
										
										
										for (SentenceDependency rowInDepList : depList) {
											String relString = rowInDepList.getRelString();
											String govString = rowInDepList.getGovString();
											String govIdx = rowInDepList.getGovIdx();
											String depString = rowInDepList.getDepString();
											String depIdx = rowInDepList.getDepIdx();											
											
											// Additional rule for detecting subject term
											// if (! (relString.equals("nsubj") &&
											//		govString.equals(keyword) &&
											//		depIdx.toLowerCase().equals("1")) ) {
											//	System.out.println("Have a subject term!");
											//	continue;
											// }
											// go to rule 1-1 ??
											

											
											if (relString.equals("dobj") && govString.toLowerCase().equals(keyword)) {
												String dobjString = depString;
												String dobjIdx = depIdx;
												
												System.out.println("dobjString::" + dobjString); 
												dobjStringList += dobjString + "\t";
												
												if (dobjString.matches("(^c\\d\\-)(.*)")) {
													// System.out.println("This dobj is category term!!");
													
													// output.add(dobjString + "::" + dobjIdx);
													outputMap.put(dobjIdx, dobjString + "::Empty");
													
												} else {
													String tmpOutput = "";
													
													for (SentenceDependency rowInDepList2 : depList) {
														String relString2 = rowInDepList2.getRelString();
														String govString2 = rowInDepList2.getGovString();
														String govIdx2 = rowInDepList2.getGovIdx();
														String depString2 = rowInDepList2.getDepString();
														String depIdx2 = rowInDepList2.getDepIdx();
														
														if (dobjString.equals(govString2) && dobjIdx.equals(govIdx2) ) {												
															// System.out.println("dobjString::" + dobjString );
															// System.out.println("dep::" + depString );
															// System.out.println("rel::" + relString );
															
															if (relString2.equals("nn") || relString2.equals("amod")) {
																// amod(acid-10, mono-succinate-7)
																// amod(acid-10, citric-9)
																// System.out.println("dobjString::" + dobjString );
																// System.out.println("dep::" + depString );
																// System.out.println("rel::" + relString );
																
																tmpOutput += depString + " ";
															}
														}
														
													}
													tmpOutput += dobjString;
													// output.add(tmpOutput + "::" + dobjIdx);
													outputMap.put(dobjIdx, tmpOutput + "::Empty");

												}
											}
										}
										
										
										boolean isNegative = false;
										String negString = "";

										
										for (SentenceDependency rowInDepList : depList) {
											String relString = rowInDepList.getRelString();
											String govString = rowInDepList.getGovString();
											String govIdx = rowInDepList.getGovIdx();
											String depString = rowInDepList.getDepString();
											String depIdx = rowInDepList.getDepIdx();										
										
											if (relString.equals("neg") &&
													govString.toLowerCase().equals(keyword) ) {
												negString = depString;
												// String negIdx = depIdx;
												isNegative = true;
											}
										}										
										
										
										if (isNegative == true) {
											
											String rootNsubjpassString = ""; 
											for (SentenceDependency rowInDepList : depList) {
												String relString = rowInDepList.getRelString();
												String govString = rowInDepList.getGovString();
												String govIdx = rowInDepList.getGovIdx();
												String depString = rowInDepList.getDepString();
												String depIdx = rowInDepList.getDepIdx();
												
												Iterator outputMapIterator = outputMap.entrySet().iterator();
												while (outputMapIterator.hasNext()) {
													Map.Entry mapEntry = (Map.Entry) outputMapIterator.next();
													String[] outputMapArray =  mapEntry.getValue().toString().split("::");
													if ( depString.equals(outputMapArray[0]) &&
															govString.equals(keyword) &&
															relString.equals("dobj") ) { // nsubjpass(produced-8, Gas-1)
														
														// 
														// output.remove(outputIterator.next());
														// output.add(outputArray[0] + "::elvis_Not::" + outputArray[2]);
														// outputMap.put(key, value);
														outputMap.put(mapEntry.getKey().toString(), outputMapArray[0] + "::" + negString);
													}
													// outputMapIterator.remove(); // avoids a ConcurrentModificationException
													// http://stackoverflow.com/questions/1066589/java-iterate-through-hashmap
												}
											}

										}
										
										Iterator outputMapIterator = outputMap.entrySet().iterator();
										while (outputMapIterator.hasNext()) {
											Map.Entry mapEntry = (Map.Entry) outputMapIterator.next();
											// System.out.println("The key is: " + mapEntry.getKey()
											// 	+ ",value is :" + mapEntry.getValue());
											output.add(mapEntry.getValue() + "::" + mapEntry.getKey());
										}

										// System.out.println("output:\n" + output);										
										
									}
								}
							}
						}
					}				
				}

				
				// Rule 2:: V => nsubjpass
				if ( pos.equals("V") && pos.equals(keywordType) && keywordObject.equals("nsubjpass") ) {
					if ( clustIdx_depArgClustIdx_.get(ci).get("nsubjpass") == null ) continue; // doesn't go through the following
					int aci = clustIdx_depArgClustIdx_.get(ci).get("nsubjpass"); 
					// System.out.println("aci is ::" + aci );

					for (String pid:pids) {
						
						if (ptId_aciChdIds_.get(pid)!=null) { 
							// System.out.println("pid is ::" + pid);
							// System.out.println("ptId_aciChdIds_.get(pid).toString() ::" + ptId_aciChdIds_.get(pid).toString());
							
							
							if (ptId_aciChdIds_.get(pid).get(aci)!=null) {
								for (String cid:ptId_aciChdIds_.get(pid).get(aci)) {
									// System.out.println("cid is ::" + cid);
									String sentId = cid.split(":")[0];
									// System.out.println("sentId is ::" + sentId);

									String txtFileName = dataDir_+ "/text/0/" + sentId + ".txt";
									String sentText = readDepFromTxtFile(txtFileName);
									
									String oriTxtFileName = dataDir_+ "/text_o/0/" + sentId + ".txt";
									String oriSentText = readDepFromTxtFile(oriTxtFileName);
									
									// System.out.println("rule 2:: ")
									// System.out.println("text::" + text);
									// System.out.println("oriSentText::" + oriSentText);
									// System.out.println("sentText(collapsed sentence)::" + sentText);
									
									if (text.equals(oriSentText)) {
									// if (text.equals(sentText)) {
										// Go to .dep to grab the result back
										// to see how much we can get								
										
										String depFileName = dataDir_+ "/dep/0/" + sentId + ".dep";
										List<SentenceDependency> depList = readDepFromDepFile(depFileName);
										System.out.println("depFileName::" + depFileName);
										
										
										System.out.println("Rule 2::");
										String nsubjpassStringList = "";
										
										Map<String, String> outputMap = new HashMap<String, String>();
										
										for (SentenceDependency rowInDepList : depList) {
											String relString = rowInDepList.getRelString();
											String govString = rowInDepList.getGovString();
											String govIdx = rowInDepList.getGovIdx();
											String depString = rowInDepList.getDepString();
											String depIdx = rowInDepList.getDepIdx();											
											
											// System.out.println("relString::" + relString);
											// System.out.println("govString::" + govString);
											///System.out.println("govIdx::" + govIdx);
											// System.out.println("depString::" + depString);
											// System.out.println("depIdx::" + depIdx);
											
											// Additional rule for detecting subject term
											//if (! (rowInDepList.get(0).toString().equals("nsubj") &&
											//		rowInDepList.get(4).toString().toLowerCase().equals("1")) ) //{
												//System.out.println("Does not have a subject term!");
											//	continue;
											//}
											

											
											if (relString.equals("nsubjpass") && govString.toLowerCase().equals(keyword)){
												String nsubjpassString = depString;
												String nsubjpassIdx = depIdx;
												
												USPId = sentId;

												System.out.println("nsubjpassString::" + nsubjpassString);
												nsubjpassStringList +=  nsubjpassString + "\t";

												
												if (nsubjpassString.matches("(^c\\d\\-)(.*)")) {
													// System.out.println("This nsubjpass is category term!!");
													// output.add(nsubjpassString + "::Empty::" + nsubjpassIdx);
													outputMap.put(nsubjpassIdx, nsubjpassString + "::Empty");
													
												} else {
													String tmpOutput = "";
													
													for (SentenceDependency rowInDepList2 : depList) {
														String relString2 = rowInDepList2.getRelString();
														String govString2 = rowInDepList2.getGovString();
														String govIdx2 = rowInDepList2.getGovIdx();
														String depString2 = rowInDepList2.getDepString();
														String depIdx2 = rowInDepList2.getDepIdx();
														
														if (nsubjpassString.equals(govString2) && nsubjpassIdx.equals(govIdx2) ) {												
															// System.out.println("dep::" + depString );
															// System.out.println("rel::" + relString );
															if (relString2.equals("nn") || relString2.equals("amod")) {
																// System.out.println("dep::" + depString );
																// System.out.println("rel::" + relString );
																tmpOutput += depString2 + " ";
															}
														}
														
													}
													tmpOutput += nsubjpassString;
													// output.add(tmpOutput + "::Empty::" + nsubjpassIdx);
													outputMap.put(nsubjpassIdx, nsubjpassString + "::Empty");

												}
											}
										}
										
										boolean isNegative = false;
										String negString = "";
										
										for (SentenceDependency rowInDepList : depList) {
											String relString = rowInDepList.getRelString();
											String govString = rowInDepList.getGovString();
											String govIdx = rowInDepList.getGovIdx();
											String depString = rowInDepList.getDepString();
											String depIdx = rowInDepList.getDepIdx();
																						
											// if (rowInDepList.get(0).toString().equals("neg") &&
											//		rowInDepList.get(1).toString().toLowerCase().equals(keyword) ) {
											//	String negString = rowInDepList.get(3).toString();
											//	String negIdx = rowInDepList.get(4).toString();
											//	output.add(negString + "::" + negIdx);
											// }
											
											String[] nsubjStringListArray = nsubjpassStringList.split("\t");
											// System.out.println(Arrays.toString(nsubjStringListArray));
											
											for (int i = 0; i < nsubjStringListArray.length; i++) {
												if (nsubjStringListArray[i].toString().length() > 1) {
													if (relString.equals("neg") &&
															govString.toLowerCase().equals(nsubjStringListArray[i].toString().toLowerCase())) {
														isNegative = true;
														negString = depString;
														// System.out.println("isNegative = true");
													}
													if (relString.equals("neg") &&
															govString.toLowerCase().equals(keyword)) {
														isNegative = true;
														negString = depString;
														// System.out.println("isNegative = true");
													}
													if (relString.equals("det") &&
															govString.toLowerCase().equals(nsubjStringListArray[i].toString().toLowerCase())) {
														isNegative = true;
														negString = depString;
														// System.out.println("isNegative = true");
													}										
												}
											}
										}
										
										if (isNegative == true) {
											
											String rootNsubjpassString = ""; 
											for (SentenceDependency rowInDepList : depList) {
												String relString = rowInDepList.getRelString();
												String govString = rowInDepList.getGovString();
												String govIdx = rowInDepList.getGovIdx();
												String depString = rowInDepList.getDepString();
												String depIdx = rowInDepList.getDepIdx();
												
												Iterator outputMapIterator = outputMap.entrySet().iterator();
												while (outputMapIterator.hasNext()) {
													Map.Entry mapEntry = (Map.Entry) outputMapIterator.next();
													String[] outputMapArray =  mapEntry.getValue().toString().split("::");
													if ( depString.equals(outputMapArray[0]) &&
															govString.equals(keyword) &&
															relString.equals("nsubjpass") ) { // nsubjpass(produced-8, Gas-1)
														
														rootNsubjpassString = outputMapArray[0];
														// 
														// output.remove(outputIterator.next());
														// output.add(outputArray[0] + "::elvis_Not::" + outputArray[2]);
														// outputMap.put(key, value);
														outputMap.put(mapEntry.getKey().toString(), outputMapArray[0] + "::" + negString);
													}
													// outputMapIterator.remove(); // avoids a ConcurrentModificationException
													// http://stackoverflow.com/questions/1066589/java-iterate-through-hashmap
												}
											}
											for (SentenceDependency rowInDepList : depList) {
												String relString = rowInDepList.getRelString();
												String govString = rowInDepList.getGovString();
												String govIdx = rowInDepList.getGovIdx();
												String depString = rowInDepList.getDepString();
												String depIdx = rowInDepList.getDepIdx();
												
												Iterator outputMapIterator = outputMap.entrySet().iterator();
												while (outputMapIterator.hasNext()) {
													Map.Entry mapEntry = (Map.Entry)outputMapIterator.next();
													String[] outputMapArray =  mapEntry.getValue().toString().split("::");
													if ( depString.equals(rootNsubjpassString) &&
															govString.equals(outputMapArray[0]) &&
															relString.equals("conj_and") ) { // conj_and(Gas-1, BBB-5)
														// 
														// output.remove(outputIterator.next());
														// output.add(outputArray[0] + "::elvis_Not::" + outputArray[2]);
														outputMap.put(mapEntry.getKey().toString(), outputMapArray[0] + "::" + negString);
													}
													// outputMapIterator.remove(); // avoids a ConcurrentModificationException
													// http://stackoverflow.com/questions/1066589/java-iterate-through-hashmap
												}
											}
										}
										
										Iterator outputMapIterator = outputMap.entrySet().iterator();
										while (outputMapIterator.hasNext()) {
											Map.Entry mapEntry = (Map.Entry) outputMapIterator.next();
											// System.out.println("The key is: " + mapEntry.getKey()
											// 	+ ",value is :" + mapEntry.getValue());
											output.add(mapEntry.getValue() + "::" + mapEntry.getKey());
										}
										
										
										// System.out.println("output:\n" + output);										
									}
								}
							}
						}
					}				
				}

				

				
				
								
				
				

				
				
				
			} // end of rules
			// System.out.println("\n");
			
			
			// Final step::
			// Read index
			
			// Set<String> outputItemList = mapToOriMorph(USPId, output);
			// output = outputItemList;
			
			output = mapToOriMorph(USPId, output);
			// Map back to orignal sentence
			
			// Add Negation desc!?!?
			//  
			
			
			
		}
		
		// 
		// http://stackoverflow.com/questions/1318980/how-to-iterate-over-a-treemap		
		// 
		// http://examples.javacodegeeks.com/core-java/util/treemap/treemap-iterator-example/

		// The above part is my own testing		
		
		return output;
		
	}
	
	static Set<String> mapToOriMorph(String USPId, Set<String> output) throws NumberFormatException, IOException {
		Set<String> extractedItemList = new HashSet<String>();

		if ( output.size() > 0 ) {
			
			System.out.println("output::" + output);
			
			String collapsedIndexFileName = dataDir_+ "/index/0/" + USPId + ".index";
			String originalMorphFileName = dataDir_+ "/morph_o/0/" + USPId + ".morph";
			
			List<String> collapsedIndexList = new ArrayList<String>();
			List<String> originaMorphList = new ArrayList<String>();
			
			File collapsedIndexFile = new File(collapsedIndexFileName);
			BufferedReader collapsedIndexFileReader = new BufferedReader(new FileReader(collapsedIndexFile));
			String s;
			
			String collapsedIndexString = "";
			
			while ((s = collapsedIndexFileReader.readLine())!=null) {
				collapsedIndexString += s + "\n";
				collapsedIndexList.add(s);
			}				
			collapsedIndexFileReader.close();

			File originalMorphFile = new File(originalMorphFileName);
			BufferedReader originalMorphFileReader = new BufferedReader(new FileReader(originalMorphFile));
			
			while ((s = originalMorphFileReader.readLine())!=null) {
				originaMorphList.add(s);
			}				
			originalMorphFileReader.close();
			
			// System.out.println("collapsedIndexString::" + collapsedIndexString);

			
			Iterator<String> iterator = output.iterator();
			// example:output::[c9-Alcohols::not::16, c9-Carbohydratesmono::not::12, c9-Polysaccharides::not::4, c9-OrganicAcids::not::22, c9-Alcohols::not::10, c9-Carbohydratesmono::not::6, c9-Carbohydratesmono::not::18]

			while (iterator.hasNext()) {
				String outputString = iterator.next().toString();
				String[] outputStringArray = outputString.split("::"); // example:c9-Alcohols::not::16
				
				String termName = "";
				String termNeg = "";
				String termIndex = "";
				if ( outputStringArray.length > 1 ) {
					termName = outputStringArray[0];
					termNeg = outputStringArray[1];
					termIndex = outputStringArray[2];
					
					// System.out.println("termNeg::0::" + termNeg);
					
					
					//if (termName.matches("^c\\d\\-\\w+")) {
					if (termName.matches("(^c\\d\\-)(.*)")) {	
						int indexInCollapsedIndexListCounter = 1;
						for (String itemInCollapsedIndexList : collapsedIndexList) {
							// System.out.println("termIndex::" + termIndex);
							// System.out.println("itemInCollapsedIndexList::" + indexInCollapsedIndexListCounter + "::" + itemInCollapsedIndexList);
							String[] itemInCollapsedIndexListArray = itemInCollapsedIndexList.split("\t");
							
							if (itemInCollapsedIndexListArray.length > 1) {
								String itemName = itemInCollapsedIndexListArray[0];
								String itemIndexList = itemInCollapsedIndexListArray[1];
								
								if (termName.equals(itemName) && Integer.parseInt(termIndex) == indexInCollapsedIndexListCounter) {
									// System.out.println("itemIndexList::" + itemIndexList);
									
									String[] itemIndexListArray = itemIndexList.split(",");
									
									for ( int i = 0; i < itemIndexListArray.length; i++) {
										// System.out.println("itemIndexListArray[i]::" + itemIndexListArray[i]);
										//example: 1,3,5
										//example2: 1,3-4,5
										String actualIndexInOriginalMorph = itemIndexListArray[i];
										String[] actualIndexInOriginalMorphArray = actualIndexInOriginalMorph.split("-");
										
										// System.out.println("actualIndexInOriginalMorphArray.length::" + actualIndexInOriginalMorphArray.length);
										
										String extractedItems = "";
										for (int j = 0;  j < actualIndexInOriginalMorphArray.length; j++) {
											// System.out.println("actualIndexInOriginalMorphArray[j]::" + actualIndexInOriginalMorphArray[j]);

											// Read the original sentence (txt file)
											// Read originaMorphList
											int actualIndexInOriginalMorphInt= Integer.parseInt(actualIndexInOriginalMorphArray[j]);
											
											int indexInOriginaMorphListCounter = 0;
											for ( String itemInOriginaMorphList : originaMorphList) {
												

													
												if (actualIndexInOriginalMorphInt == indexInOriginaMorphListCounter) {												
													// if (indexInOriginaMorphListCounter > 0) {
													//	String previousString = originaMorphList.get(indexInOriginaMorphListCounter-1).toString();
													//	System.out.println("previousString::" + previousString);
													//
													//	if ( !previousString.matches("\\W+")) {
													//		//extractedItems += originaMorphList.get(indexInOriginaMorphListCounter-1).toString() + " ";
													//		System.out.println("Should::" + previousString + " " + itemInOriginaMorphList);
													//	}
													// }
													// System.out.println("itemInOriginaMorphList::" + itemInOriginaMorphList);
													
													
													if ( !termNeg.equals("Empty")) {
														extractedItems += termNeg + " ";
													}
													
													extractedItems += itemInOriginaMorphList + " ";
													
												}
												

												
												indexInOriginaMorphListCounter++;
											}										
										}
										extractedItemList.add(extractedItems);
										

										
										
										

										
									}
				
								}					
							}

							
							indexInCollapsedIndexListCounter++;
						}						
						
					} else {
						String extractedItems = "";
						if ( !termNeg.equals("Empty")) {
							extractedItems += termNeg + " ";
						}
						extractedItems += termName;
						extractedItemList.add(extractedItems);
					}
					
			

				}
			}
			
			if (extractedItemList.size() == 0) {
				extractedItemList.add("");
			}			
			
			
			
		} else {
			extractedItemList.add("");
		}
		return extractedItemList;
		
	}

	

	static String readDepFromTxtFile(String txtFileName) throws NumberFormatException, IOException {
		String returnString = "";
		
		File txtFile = new File(txtFileName);
		BufferedReader in = new BufferedReader(new FileReader(txtFile));
		String s;
				
		while ((s = in.readLine())!=null) {
			returnString += s;			
		}
		in.close();		
		
		return returnString;
	}	
	
	static String readParseFromParseFile(String parseFileName) throws NumberFormatException, IOException {
		String returnString = "";
		
		File parseFile = new File(parseFileName);
		BufferedReader in = new BufferedReader(new FileReader(parseFile));
		String s;
				
		while ((s = in.readLine())!=null) {
			returnString += s;			
		}
		in.close();		
		
		return returnString;
	}	
	
	static List<SentenceDependency> readDepFromDepFile(String depFileName) throws NumberFormatException, IOException {
		
		File depFile = new File(depFileName);
		BufferedReader in = new BufferedReader(new FileReader(depFile));
		String s;
		int counter = 0;
		
		List<SentenceDependency> depList = new ArrayList<SentenceDependency>();
		
		while ((s = in.readLine())!=null) {
			
			if (s.length() > 0) {
				// System.out.println(counter+":s:" + s);
				// nn(monkeys-2, Squirrel-1) 
				String rel=s.substring(0,s.indexOf("("));
				int i1=s.indexOf("(")+1, i3=s.lastIndexOf(")"), i2=s.indexOf(", ");
				while (i1==i2 || (!Character.isDigit(s.charAt(i2-1)) && s.charAt(i2-1)!='\'')) {
					i2=s.indexOf(",",i2+1);
				}; 
				
				
//				Utils.println("s="+s+" i1="+i1+" i2="+i2+" i3="+i3);
				
				// List<String> depRowList = new ArrayList<String>();
				SentenceDependency sentenceDependency = new SentenceDependency();
				
				String gov=s.substring(i1,i2).trim();			
				String dep=s.substring(i2+1,i3).trim();
				
//				Utils.println("gov="+gov+" dep="+dep);
				
				// all-info
				if (gov.charAt(gov.length()-1)=='\'') gov=gov.substring(0, gov.length()-1);
				if (dep.charAt(dep.length()-1)=='\'') dep=dep.substring(0, dep.length()-1);			
				int govId=Integer.parseInt(gov.substring(gov.lastIndexOf("-")+1));
				int depId=Integer.parseInt(dep.substring(dep.lastIndexOf("-")+1));
				
				gov = gov.substring(0, gov.lastIndexOf("-")); 
				dep = dep.substring(0, dep.lastIndexOf("-"));
				
				// depRowList.add(rel);
				// depRowList.add(gov);
				// depRowList.add(String.valueOf(govId));
				// depRowList.add(dep);
				// depRowList.add(String.valueOf(depId));
				
				sentenceDependency.setRelString(rel);
				sentenceDependency.setGovString(gov);
				sentenceDependency.setGovIdx(String.valueOf(govId));
				sentenceDependency.setDepString(dep);
				sentenceDependency.setDepIdx(String.valueOf(depId));
				
				// System.out.println("rel::"+ rel);
				// System.out.println("gov::"+ gov);
				// System.out.println("dep::"+ dep);
				// System.out.println("govId::"+ govId);
				// System.out.println("depId::"+ depId);
				
				depList.add(sentenceDependency);
			}
			
			counter+=1;
		}
		in.close();		
		
		return depList;
	}
	
	static String printMatchingResult(String argForm, String denpendencyType, String termType, Map<String,String> outputMap) {
		
		String returnString = "";
		
		if (argForm.contains(denpendencyType)) {
			// System.out.println(outputMap.toString());			
			// System.out.println(denpendencyType);

			// Iterate it
			for (Map.Entry<String, String> entry : outputMap.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (value.contains(termType)){
					// System.out.println(key);
					if (!(key.contains("?") || key.contains("(") || key.contains(")") || key.contains(":") || key.contains(".")))
						returnString += key + " ";
				}
			}
		}
		if (returnString.length() > 2 && returnString.substring(returnString.length()-2, returnString.length()-1).equals(" ")) {
			returnString = returnString.substring(0, returnString.length()-1);
		}
		return returnString;
		
	}

	
	static Map<String,String> readClustrCoreFormToOutputMap(String clusterCoreForms) {
		Map<String,String> outputMap = new HashMap<String,String>();
					
		int i=clusterCoreForms.indexOf('\t');
		String cs=clusterCoreForms.substring(i+1);//[(N:diffusion):1]
		i=cs.indexOf('(');
		while (i>=0) {
			int j=cs.indexOf(':',i);
			String pos=cs.substring(i+1,j);
			int k=cs.indexOf("):",j);
			i=cs.indexOf('(',k);
			String rt=cs.substring(j+1,k);
			// System.out.println("1. pos :: " + pos);
			// System.out.println("2. rt :: " + rt);
			outputMap.put(rt, pos);
			
		}
		return outputMap;
	}
	
	
	
	// find clustIdx for rel in questions
	static void readClust2(String fileName, Map<String, Map<String,Integer>> keywordList_) throws Exception {
		BufferedReader in;
		String s;
		String[] ts;
		
		int currCi=-1;
		Map<String,Integer> dep_aci=null;
		
		Map<String,String> argTypeClust = null;
		
		
		in=new BufferedReader(new FileReader(fileName));
		while ((s=in.readLine())!=null) {			
			if (!Character.isDigit(s.charAt(0))) {
				// aci/argnum 
				int i=s.indexOf('\t');
				int j=s.indexOf('\t',i+1);
				int aci=Integer.parseInt(s.substring(i+1,j));				
				
				// ati
				s=in.readLine().trim();
				String argFromStr = s;
				if (argFromStr.length() == 0)
					argFromStr = "Empty";
				// System.out.println("arg form ::" + argFromStr);
				
				ts=s.split("\\t");
				
				for (int k=0; k<ts.length; k++) {
					String x=ts[k];
					i=x.indexOf(':');
					j=x.indexOf(':',i+1);
					String dep=x.substring(i+2,j-1);	// "<...>"
					dep_aci.put(dep, aci);
				}
				
				// chdcl
				s=in.readLine().trim();
				String argFormCluster = s;
				if (argFormCluster.length() == 0)
					argFormCluster = "Empty";
				// System.out.println("arg form cluster ::" + argFormCluster);
				
				argTypeClust.put(argFromStr, argFormCluster);
				//argTypeClust.put(String.valueOf(aci),String.valueOf(aci));
				
				continue;
			}
			
			// if (s.contains("92") && s.contains("V")) {
			//	System.out.println(s);
			// }
			
//			Utils.println(s);
			int i=s.indexOf('\t');
			int ci=Integer.parseInt(s.substring(0,i));
			
			// System.out.println("Cluster Idx ::" + ci + "\n\n");
			
			
			
			
			String cs=s.substring(i+1);//[(N:diffusion):1]
			// String cs = "";
			// if (s.contains("92") && s.contains("V")) {
			//	cs=s.substring(i+1);//[(N:diffusion):1]
			// }
			
			// if (s.contains("92") && s.contains("V")) {
			//	System.out.println("cs::" + cs);
			// }
			// cs: [(V:used):144,	(V:utilized):144]
			// cs: [(V:forming):12]

			
			i=cs.indexOf('(');
			while (i>=0) {
				int j=cs.indexOf(':',i);
				String pos=cs.substring(i+1,j);
				int k=cs.indexOf("):",j);
				i=cs.indexOf('(',k);
				
				String rt=cs.substring(j+1,k);
				
				// String frequency = cs.substring(k+2, cs.length()-1);
				
				// System.out.println("0. cs :: " + cs);
				// System.out.println("1. ci :: " + ci);
				// System.out.println("2. pos :: " + pos);
				// System.out.println("3. rt :: " + rt);
				// System.out.println("4. frequency :: " + frequency);
				
				// process multiple piece				
				procRelType2(ci,pos,rt, keywordList_);			
			}
			currCi=ci;
			dep_aci = new HashMap<String,Integer>();
			argTypeClust = new HashMap<String,String>();
			clustIdx_depArgClustIdx_.put(ci, dep_aci);
			clustIdx_argTypeClustIdx_.put(ci, argTypeClust);
		}
		in.close();		
	}

	static void procRelType2(int clustIdx, String pos, String relType, Map<String, Map<String,Integer>> keywordList_) {
		// 19508	[(N:b (nn (N:nf-kappa))):1466,	(N:b (dep (N:nf-kappa))):2]

		if (pos.equals("V")) {
			String vCoreFormList = clustIdx + ":" + pos + "::" + relType;
			// System.out.println("vCoreFormList::" + vCoreFormList);
		}
		
		// rel only				
		//if (keywordList_.containsKey(relType) && ( pos.equals("V") || pos.equals("N") || pos.equals("J") ) ) {					
		
		
		if (keywordList_.containsKey(relType)) {	
			Map<String,Integer> clustIdx_pos = keywordList_.get(relType);
			clustIdx_pos.put(pos, clustIdx);
			// System.out.println("clustIdx_pos::" + relType + "::" + clustIdx_pos.toString());
			
			// keywordList_.get(relType).put(pos, clustIdx);
			keywordList_.put(relType, clustIdx_pos);
			
			// System.out.println("keywordList_.get(relType)::" + keywordList_.get(relType));
			// System.out.println("qLemmas_.contains(relType) ::" + relType.toString());
			// System.out.println("clustIdx ::" + clustIdx);
			// System.out.println("pos ::" + pos);
		}
		
		
	}	
	
	
	
	static void readQuestions() throws Exception {
		BufferedReader in=new BufferedReader(new FileReader(evalDir_+Utils.FILE_SEP+"question.sbj.txt"));
		String s;
		while ((s=in.readLine())!=null) {
			s=s.trim();
			if (s.length()==0) 	continue;
			int k=s.lastIndexOf(" ");
			String v=s.substring(k+1,s.length()-1);
			String q=s.substring("What does ".length(),k).trim().toLowerCase();
			ArrayList<Question> qs=rel_qs_.get(v);
			if (qs==null) {
				qs=new ArrayList<Question>();
				rel_qs_.put(v, qs);
			}
			Question qu=new Question(v,q,"nsubj");
			qs.add(qu);
			String[] ts=q.split(" ");
			for (int i=0; i<ts.length; i++) { 
				qForms_.add(ts[i]);
			}
			qForms_.add(v);
		}
		in.close();
		in=new BufferedReader(new FileReader(evalDir_+Utils.FILE_SEP+"question.obj.txt"));
		
		while ((s=in.readLine())!=null) {
			s=s.trim();
			if (s.length()==0) 	continue;
			int i=s.indexOf(" ");
			s=s.substring(i+1).trim();
			String q=s.substring(s.indexOf(' ')+1,s.length()-1).trim().toLowerCase();	// remove .
			String v=removeThirdPerson(s.substring(0,s.indexOf(' ')));
			//String v=s.substring(0,s.indexOf(' '));
						
			ArrayList<Question> qs=rel_qs_.get(v);
			if (qs==null) {
				qs=new ArrayList<Question>();
				rel_qs_.put(v, qs);
			}
			Question qu=new Question(v,q,"dobj");
			qs.add(qu);
			String[] ts=q.split(" ");
			for (i=0; i<ts.length; i++) {
				qForms_.add(ts[i]);
			}
			qForms_.add(v);
		}
		in.close();	
		System.out.println("qForms is ::" + qForms_.toString());
		System.out.println("rel_qs_ is ::" + rel_qs_.toString());
	}

	static void readMorph() throws Exception {		
		File rdf=new File(dataDir_+Utils.FILE_SEP+"morph");
		String[] dirs=rdf.list();
		Arrays.sort(dirs);
		
		for (int i=0; i<dirs.length; i++) {	// try 2k files first
			File d=new File(rdf.getPath()+Utils.FILE_SEP+dirs[i]);
			if (!d.isDirectory()) continue;
			String[] fns=d.list();
			Arrays.sort(fns);
			for (int j=0; j<fns.length; j++) {
				if (fns[j].indexOf(".morph")<0) continue;				
				String fid=fns[j].substring(0,fns[j].indexOf('.'));
				String mfn=d.getPath()+Utils.FILE_SEP+fns[j];
				String ifn=d.getPath()+Utils.FILE_SEP+fid+".input";
				readMorph(fns[j].substring(0,fns[j].indexOf('.')), mfn, ifn);
			}
		}
	}
	static void readMorph(String aid,String mfileName,String ifileName) throws Exception {
		BufferedReader in=new BufferedReader(new FileReader(mfileName));
		BufferedReader in2=new BufferedReader(new FileReader(ifileName));
		String s,s2;
		while ((s=in.readLine())!=null) {
			s2=in2.readLine();
			s=s.trim(); s2=s2.trim();
			if (s.length()==0) continue;
			if (s.indexOf("_nn")>=0) s=s.substring(0,s.indexOf("_nn"));
			
			s=s.toLowerCase(); s2=s2.toLowerCase();
			s2=s2.substring(0,s2.indexOf('_'));
			if (!qForms_.contains(s2)) continue;
			Set<String> ls=form_lemma_.get(s2);
			if (ls==null) {
				ls=new HashSet<String>();
				form_lemma_.put(s2, ls);
			}
			ls.add(s);
			qLemmas_.add(s);
		}
		in.close();
		in2.close();
		// System.out.println("qLemmas_ is ::" + qLemmas_.toString());
		
	}
	static void readSents() throws Exception {		
		
		StanfordParseReader reader=new StanfordParseReader();
		
		File rdf=new File(dataDir_+Utils.FILE_SEP+"text");
		String[] dirs=rdf.list();
		Arrays.sort(dirs);
		
		for (int i=0; i<dirs.length; i++) {	// try 2k files first
			File d=new File(rdf.getPath()+Utils.FILE_SEP+dirs[i]);
			if (!d.isDirectory()) continue;
			String[] fns=d.list();
			Arrays.sort(fns);
			for (int j=0; j<fns.length; j++) {
				if (fns[j].indexOf(".txt")<0) continue;
				String aid=fns[j].substring(0,fns[j].indexOf('.'));
				String fn=d.getPath()+Utils.FILE_SEP+aid;
				// System.out.println("aid::" + aid);
				//System.out.println("fn::" + fn);
				readSents(aid, fn+".txt");
				id_article_.put(aid, reader.readParse(fn,false));	// don't ignore deps
			}
		}
	}
	static void readSents(String aid,String fileName) throws Exception {
		BufferedReader in=new BufferedReader(new FileReader(fileName));
		String s;
		int idx=0;
		while ((s=in.readLine())!=null) {
			s=s.trim();
			if (s.length()==0) continue;
			String id=aid+":"+(idx++);
			id_sent_.put(id, s);
		}
		in.close();
	}	

	// read parts
	static void readPart(String fileName) throws Exception {
		BufferedReader in;
		String s;
		String[] ts;
		
		
		// System.out.println(fileName);
		in=new BufferedReader(new FileReader(fileName));
		while ((s=in.readLine())!=null) {
			// id/str
			int i=s.indexOf('\t');
			String id=s.substring(0,i);
			String str=s.substring(i+1);
			
			// clustIdx/clust
			s=in.readLine().trim();
			i=s.indexOf('\t');
			int ci=Integer.parseInt(s.substring(0,i));
			
			// System.out.println("ci::" + ci);
			
			Set<String> pids=clustIdx_ptIds_.get(ci);
			if (pids==null) {
				pids=new HashSet<String>();
				
				
				
				clustIdx_ptIds_.put(ci, pids);
			}
			pids.add(id);
			
			// System.out.println("pids::" + pids);
			
			ptId_clustIdxStr_.put(id, new Pair<Integer,String>(ci,str));

			// par/arg
			s=in.readLine().trim();
			String s2=in.readLine().trim();
			if (s.length()>0) {
				// parid/clust
				ts=s.split("\\t");
				String pid=ts[0];				
								
				ts=s2.split("\\t");
				int aci=Integer.parseInt(ts[0]);
				String dep=ts[2].substring(1,ts[2].length()-1);	// "<>"
				ptId_parDep_.put(id, dep);
				
				// System.out.println("dep::" + dep);
				
				
				Map<Integer,Set<String>> aci_cids=ptId_aciChdIds_.get(pid);
				if (aci_cids==null) {
					aci_cids=new HashMap<Integer,Set<String>>();
					ptId_aciChdIds_.put(pid, aci_cids);
				}
				// System.out.println("aci_cids::" + aci_cids);
				Set<String> cids=aci_cids.get(aci);
				if (cids==null) {
					cids=new HashSet<String>();
					aci_cids.put(aci, cids);
				}
				cids.add(id);
			}
		}
		in.close();
	}
	
	// TO-DO: only handle 2 layer for now; ignore rt
	static void procRelType(int clustIdx, String pos, String relType) {
		// 19508	[(N:b (nn (N:nf-kappa))):1466,	(N:b (dep (N:nf-kappa))):2]
	
		// rel only				
		if (rel_qs_.containsKey(relType) && pos.equals("V")) {					
			if (rel_clustIdx_.containsKey(relType)) {
				Utils.println("ERR: multiple clusts "+relType);
			}
			rel_clustIdx_.put(relType, clustIdx);
		}
		
		// arbitrary str
		if (qLemmas_.contains(relType)) {
			System.out.println("qLemmas_.contains(relType) ::" + relType.toString());
			System.out.println("clustIdx ::" + clustIdx);
			System.out.println("pos ::" + pos);

			Set<String> cis=lemma_clustIdxs_.get(relType);
			if (cis==null) {
				cis=new HashSet<String>();
				lemma_clustIdxs_.put(relType, cis);
			}
			cis.add(""+clustIdx);
		}
		else {
			// probably multiple?
			int k=relType.indexOf(' ');
			if (k<0) return;			
			String head=relType.substring(0,k);
			k=relType.indexOf('(',k);
			int j=relType.indexOf(':',k);
			k=relType.indexOf(')',j);
			if (j<0 || k<0) {Utils.println("ERR: reltype="+relType); return;}
			String dep=relType.substring(j+1,k);
			
			if (!qLemmas_.contains(head) || !qLemmas_.contains(dep)) return;
			
			Pair<String,String> hd=new Pair<String,String>(head,dep);
			headDep_clustIdxs_.put(hd, ""+clustIdx);
		}
	}
	
	// find clustIdx for rel in questions
	static void readClust(String fileName) throws Exception {
		BufferedReader in;
		String s;
		String[] ts;
		
		int currCi=-1;
		Map<String,Integer> dep_aci=null;
		in=new BufferedReader(new FileReader(fileName));
		while ((s=in.readLine())!=null) {			
			if (!Character.isDigit(s.charAt(0))) {
				// aci/argnum 
				int i=s.indexOf('\t');
				int j=s.indexOf('\t',i+1);
				int aci=Integer.parseInt(s.substring(i+1,j));				
				
				// ati
				s=in.readLine().trim();
				ts=s.split("\\t");
				for (int k=0; k<ts.length; k++) {
					String x=ts[k];
					i=x.indexOf(':');
					j=x.indexOf(':',i+1);
					String dep=x.substring(i+2,j-1);	// "<...>"
					dep_aci.put(dep, aci);
				}
				
				// chdcl
				s=in.readLine().trim();
				continue;
			}
//			Utils.println(s);
			int i=s.indexOf('\t');
			int ci=Integer.parseInt(s.substring(0,i));
			String cs=s.substring(i+1);			
			i=cs.indexOf('(');
			while (i>=0) {
				int j=cs.indexOf(':',i);
				String pos=cs.substring(i+1,j);
				int k=cs.indexOf("):",j);
				i=cs.indexOf('(',k);
				String rt=cs.substring(j+1,k);

				// process multiple piece				
				procRelType(ci,pos,rt);			
			}
			currCi=ci;
			dep_aci=new HashMap<String,Integer>();
			clustIdx_depArgClustIdx_.put(ci, dep_aci);
		}
		in.close();		
	}
	static void printAns() {
		for (Question q:qas_.keySet()) {			
			Set<Answer> as=qas_.get(q);
			for (Answer ans:as) {
				String sent=id_sent_.get(ans.sid_);
				Utils.println("<question str=\""+q+"\">");
				Utils.println("<label></label>");
				Utils.println("<answer>"+ans.rst_+"</answer>");
				Utils.println("<sentence id=\""+ans.sid_+"\">"+sent+"</sentence>");
				Utils.println("</question>\n");
			}
		}
	}
	
	// based on clust
	static String getTreeStr(String ptId) {
		Map<String, String> id_str=new TreeMap<String,String>();
		
		// System.out.println("ptId ++ " + ptId);
		// System.out.println("ptId_aciChdIds_ ++ " + ptId_aciChdIds_.toString());

		
		if (ptId_aciChdIds_.get(ptId)!=null) {
			for (Integer aci: ptId_aciChdIds_.get(ptId).keySet()) {
				Set<String> cids=ptId_aciChdIds_.get(ptId).get(aci);
				for (String cid: cids) {
					String s="";
					String dep=ptId_parDep_.get(cid);
					
					// skip 
					if (!allowedDeps_.contains(dep)) continue;
					
					s+=getTreeStr(cid);
					id_str.put(cid,s);
					System.out.println("getTreeStr(cid) ++ " + s);
				}
			}
		}
		id_str.put(ptId,""+ptId_clustIdxStr_.get(ptId).getFirst());
		
		String x="";
		for (String id:id_str.keySet()) {
			if (x.length()>0) x+=" ";
			x+=id_str.get(id);
		}

		return x;
	}
	
	// based on word
	static String getTreeStrOld(String ptId) {
		Map<String, String> id_str=new TreeMap<String,String>();
		if (ptId_aciChdIds_.get(ptId)!=null) {
			for (Integer aci: ptId_aciChdIds_.get(ptId).keySet()) {
				Set<String> cids=ptId_aciChdIds_.get(ptId).get(aci);
				for (String cid: cids) {
					String s="";
					String dep=ptId_parDep_.get(cid);
					
					// skip 
					if (!allowedDeps_.contains(dep)) continue;
					
					if (dep.indexOf("prep_")==0) {
						s=dep.substring(5)+" ";
					}
					
					s+=getTreeStrOld(cid);
					id_str.put(cid,s);
				}
			}
		}
		id_str.put(ptId,ptId_clustIdxStr_.get(ptId).getSecond());
		
		String x="";
		for (String id:id_str.keySet()) {
			if (x.length()>0) x+=" ";
			x+=id_str.get(id);
		}
		return x;
	}


	static boolean contains(String cs,String c) {
		String[] x=cs.split(" ");
		for (int i=0; i<x.length; i++) if (c.equals(x[i])) return true;
		return false;
	}
	static boolean contains(ArrayList<String> cis, int ci) {
		String x=""+ci;
		for (int i=0; i<cis.size(); i++) {
			if (contains(cis.get(i),x)) return true;
		}
		return false;
	}

	static Set<String> getTreeCis(String ptId) {
		Set<String> cis=new TreeSet<String>();
		cis.add(ptId_clustIdxStr_.get(ptId).getFirst()+"");
		if (ptId_aciChdIds_.get(ptId)!=null) {
			for (Integer aci: ptId_aciChdIds_.get(ptId).keySet()) {
				Set<String> cids=ptId_aciChdIds_.get(ptId).get(aci);
				for (String cid: cids) {
					String s="";
					String dep=ptId_parDep_.get(cid);
					
					// skip 
					if (!allowedDeps_.contains(dep)) continue;
										
					Set<String> x=getTreeCis(cid);
					cis.addAll(x);
				}
			}
		}
		return cis;
	}
	

	static boolean isMatchFromHead(String chdPtId, ArrayList<String> cis) {
		int hci=ptId_clustIdxStr_.get(chdPtId).getFirst();
		if (!contains(cis,hci)) return false;
	
		Set<String> tcis=getTreeCis(chdPtId);
		for (int i=0; i<cis.size(); i++) {			
			String x=cis.get(i);
			String[] ts=x.split(" ");
			boolean ok=false;
			for (int k=0; k<ts.length; k++) if (tcis.contains(ts[k])) {ok=true;break;}
			if (!ok) return false;
		}
		return true;
	}
	
	static boolean isMatch(String chdPtId, String arg) {	
		ArrayList<ArrayList<String>> allcis=arg_cis_.get(arg);
		for (ArrayList<String> cis:allcis) {
			if (isMatchFromHead(chdPtId,cis)) return true;
		}
		
		if (ptId_aciChdIds_.get(chdPtId)==null) return false;
		for (Integer aci:ptId_aciChdIds_.get(chdPtId).keySet()) {
			Set<String> cids=ptId_aciChdIds_.get(chdPtId).get(aci);
			for (String cid:cids) {
				String dep=ptId_parDep_.get(cid);
				if ((dep.indexOf("conj_")==0 && !dep.equals("conj_negcc")) || dep.equals("appos")) {
					for (ArrayList<String> cis:allcis) {
						if (isMatchFromHead(cid,cis)) return true;
					}
				}
			}
		}
		return false;
	}

	// aci: given; aci2: ans
	static void match(Question q, String pid, int aci, int aci2) {
		if (ptId_aciChdIds_.get(pid)==null) return;
		if (ptId_aciChdIds_.get(pid).get(aci)==null || ptId_aciChdIds_.get(pid).get(aci2)==null) return;

		// negation
		for (Integer x:ptId_aciChdIds_.get(pid).keySet()) {
			if (x==aci || x==aci2) continue;
			for (String cid:ptId_aciChdIds_.get(pid).get(x)) {
				String dep=ptId_parDep_.get(cid);
				if (dep.equals("neg")) {
					return;
				}
			}
		}
		
		// match aci w. arg
		boolean isMatch=false;
		for (String cid:ptId_aciChdIds_.get(pid).get(aci)) {
			if (isMatch(cid,q.arg_)) {
				System.out.println("pid is ::" + pid);
				System.out.println("cid is ::" + cid);
				System.out.println("q.arg_ is ::" + q.arg_);
				System.out.println("cid :: " + cid);
				
				isMatch=true;
				break;
			}
		}
		
		// retrieve aci2
		if (!isMatch) return;
		for (String cid:ptId_aciChdIds_.get(pid).get(aci2)) {
			System.out.println("cid :: " + cid);
			System.out.println("q :: " + q.toString());

			findAns(q,cid);	// recursively construct ans; take care of and/appos
		}
	}
	
	static String getSentId(String ptId) {
		int i=ptId.lastIndexOf(':');
		return ptId.substring(0,i);
	}	
	static String getArticleId(String ptId) {
		int i=ptId.indexOf(':');
		return ptId.substring(0,i);
	}
	static int getSentIdx(String ptId) {
		int i1=ptId.indexOf(':');
		int i2=ptId.lastIndexOf(':');
		return Integer.parseInt(ptId.substring(i1+1,i2));
	}
	static int getTknIdx(String ptId) {
		int i=ptId.lastIndexOf(':');
		return Integer.parseInt(ptId.substring(i+1));
	}
	
	static void findAns(Question q,String pid) {
		String sid=getSentId(pid);
		
		String aid=getArticleId(pid);
		int sIdx=getSentIdx(pid);
		Article art=id_article_.get(aid);
		Sentence sent=art.sentences_.get(sIdx);
		
		Map<String,String> pid_minPid=new HashMap<String,String>();
		ArrayList<TreeSet<String>> ans=findAns(pid, pid_minPid);
		for (int i=0; i<ans.size(); i++) {
			TreeSet<String> a=ans.get(i);
			TreeSet<Integer> na=new TreeSet<Integer>();	// tknIdx; may add some prep/det
			
			TreeMap<Integer,String> idx_prep=new TreeMap<Integer,String>();
			
			for (String id:a) {
				// look up dependency
				int tknIdx=getTknIdx(id);
				na.add(tknIdx);
				
				// det
				int detIdx=-1;
				Set<Pair<String,Integer>> chd=sent.tkn_children_.get(tknIdx);
				if (chd!=null)
				for (Pair<String,Integer> depChd: chd) {
					if (depChd.getFirst().equals("det")) {
						detIdx=depChd.getSecond();
						na.add(detIdx);
						break;
					}
				}

				// prep?
				Pair<String,Integer> par=sent.tkn_par_.get(tknIdx);
				if (par!=null && par.getFirst().indexOf("prep_")==0) {
					int parIdx=par.getSecond();
					String parId=Utils.genTreeNodeId(aid, sIdx, parIdx);
					if (a.contains(parId)) {
						// should include the preposition in the answer string
						String prep=par.getFirst().substring("prep_".length());
						String mpid=pid_minPid.get(id);
						int midx=getTknIdx(mpid);
						if (detIdx>=0 && detIdx<midx) midx=detIdx;
						idx_prep.put(midx, prep);
					}
				}
			}
			String s="";
			for (Integer idx:na) {
				// prep
				if (!idx_prep.isEmpty()) {
					int pidx=idx_prep.firstKey();
					if (idx>=pidx) {
						String prep=idx_prep.get(pidx);
						if (s.length()>0) s+=" ";
						s+=prep;
						idx_prep.remove(pidx);
					}
				}
				
				// word
				String word=sent.tokens_.get(idx).getForm();
				
				String xid=Utils.genTreeNodeId(aid, sIdx, idx);
				if (ptId_clustIdxStr_.get(xid)!=null) {
				String xs=ptId_clustIdxStr_.get(xid).getSecond();
				if (xs.indexOf(' ')>0) word=xs;
				}
				
				if (s.length()>0) s+=" ";
				s+=word;				
			}
			System.out.println("Final answer ::" + s);
			Answer x=new Answer(sid,s);
			Set<Answer> y=qas_.get(q);
			if (y==null) {
				y=new TreeSet<Answer>();
				qas_.put(q, y);
			}
			y.add(x);
		}
	}
	
	// minPid for inserting prep, if any
	static ArrayList<TreeSet<String>> findAns(String pid, Map<String, String> pid_minPid) {
		ArrayList<TreeSet<String>> ans=new ArrayList<TreeSet<String>>();
		ArrayList<TreeSet<String>> curr=new ArrayList<TreeSet<String>>();
		TreeSet<String> z=new TreeSet<String>();
		z.add(pid);
		curr.add(z);
		pid_minPid.put(pid, pid);
		
		if (ptId_aciChdIds_.get(pid)!=null) { 
		for (Integer x:ptId_aciChdIds_.get(pid).keySet()) {
			for (String cid:ptId_aciChdIds_.get(pid).get(x)) {
				
				String dep=ptId_parDep_.get(cid);
			
				if (dep.indexOf("conj_")==0 && !dep.equals("conj_negcc") || dep.equals("appos")) {
					ArrayList<TreeSet<String>> y=findAns(cid, pid_minPid);
					ans.addAll(y);
					if (pid_minPid.get(cid).compareTo(pid_minPid.get(pid))<0)
						pid_minPid.put(pid, pid_minPid.get(cid));
				}
				else if (allowedDeps_.contains(dep)) {
					ArrayList<TreeSet<String>> curr1=new ArrayList<TreeSet<String>>();
					ArrayList<TreeSet<String>> y=findAns(cid, pid_minPid);
					if (pid_minPid.get(cid).compareTo(pid_minPid.get(pid))<0)
						pid_minPid.put(pid, pid_minPid.get(cid));
					for (int i=0; i<curr.size(); i++) {
						TreeSet<String> a=curr.get(i);
						for (int k=0; k<y.size(); k++) {
							TreeSet<String> b=y.get(k);
							TreeSet<String> c=new TreeSet<String>();
							c.addAll(a); c.addAll(b);
							curr1.add(c);
						}
					}
					curr=curr1;
				}
			}
		}
		}
		ans.addAll(curr);
		return ans;
	}
	
	static void match() {
		System.out.println("rel_qs_ ::" + rel_qs_);
		// System.out.println("clustIdx_ptIds_ ::" + clustIdx_ptIds_.toString());
		for (String rel:rel_qs_.keySet()) {
			System.out.println("rel ::" + rel);
			
//			Utils.println("Processing "+rel);
			ArrayList<Question> qs=rel_qs_.get(rel);
			int ci=rel_clustIdx_.get(rel);
			System.out.println("ci ::" + ci);
			Set<String> pids=clustIdx_ptIds_.get(ci);
			// System.out.println("pids ::" + pids);

			for (Question q:qs) {
//				Utils.println("\tProcess " +q);
				System.out.println("q ::" + q.toString());
				System.out.println("q.dep_ ::" + q.dep_);
				
				

				// find aci // argument cluster index
				String dep=q.dep_;
				String dep2=(q.dep_.equals("nsubj"))?"dobj":"nsubj";
				
				
				System.out.println("dep" + dep);
				System.out.println("dep2" + dep2);
				
				int aci=clustIdx_depArgClustIdx_.get(ci).get(dep), aci2=clustIdx_depArgClustIdx_.get(ci).get(dep2);
				// finally figure it out!!
				// 
				
				
				for (String pid:pids) match(q,pid,aci,aci2);
			}
		}
	}
	

	static void preprocArgs() {
		System.out.println("lemma_clustIdxs_ :: " + lemma_clustIdxs_.toString());
		for (String r: rel_qs_.keySet()) {
			ArrayList<Question> qs=rel_qs_.get(r);
			Set<Question> ignoredQs=new HashSet<Question>();	// ignore due to missing form
			for (Question q: qs) {
				if (arg_cis_.containsKey(q.arg_)) continue;
				
				ArrayList<ArrayList<String>> cis=new ArrayList<ArrayList<String>>();
				
				ArrayList<String> x=new ArrayList<String>();
				String[] ts=q.arg_.split(" ");
				
				// find lemmas
				boolean isIgnored=false;
				for (int i=0; i<ts.length; i++) {
					String f=ts[i];
					
					// TO-DO: match rel
					if (f.equals("the") || f.equals("of") || f.equals("in")) continue;
					Set<String> z=new TreeSet<String>();
					Set<String> ls=form_lemma_.get(f);
					
					if (ls==null) {
						//Utils.println("ERR: "+f);
						isIgnored=true;
						break;
					}					
					for (String l:ls) {
						if (lemma_clustIdxs_.get(l)==null) ;//Utils.println("ERR: missing lemma: "+l);
						else z.addAll(lemma_clustIdxs_.get(l));												
					}
					
					String s="";
					for (String ci:z) {
						if (s.length()>0) s+=" ";
						s+=ci;
					}
					x.add(s);
				}
				if (isIgnored) {ignoredQs.add(q); continue;}
				
				// generate all possible matches
				cis.add(x);
				System.out.println("All possible cis ::" + cis);
				
				// sort all possible match: TO-DO only check last two for now
				if (ts.length>=2) {
					Set<String> z=new TreeSet<String>();
					
					// check if last two are together
					Set<String> hs=form_lemma_.get(ts[ts.length-1]);
					Set<String> ds=form_lemma_.get(ts[ts.length-2]);
					for (String h:hs) {
						for (String d:ds) {
							Pair<String,String> hd=new Pair<String,String>(h,d);
							String ci=headDep_clustIdxs_.get(hd);
							if (ci!=null) {
								z.add(ci);								
							}
						}
					}
					
					if (z.size()>0) {
						ArrayList<String> y=new ArrayList<String>();
						
						// TO-DO: may have delete the/of/ before last?
						for (int i=0; i<x.size()-2; i++) {
							y.add(x.get(i));
						}
						String s="";
						for (String ci:z) {
							if (s.length()>0) s+=" ";
							s+=ci;
						}
						y.add(s);
						cis.add(y);
						System.out.println("z ::" + z.toString());
					}	
								
				}
				
				arg_cis_.put(q.arg_, cis);
			}
			qs.removeAll(ignoredQs);

			ignoredQs=new HashSet<Question>();	// ignore due to missing form
			for (Question q:qs) {
				// find aci
				if (rel_clustIdx_.get(q.rel_)==null) 
					Utils.println("ERR: "+q.rel_);
				int ci=rel_clustIdx_.get(q.rel_);
				String dep=q.dep_;
				String dep2=(q.dep_.equals("nsubj"))?"dobj":"nsubj";
				if (clustIdx_depArgClustIdx_.get(ci).get(dep)==null || clustIdx_depArgClustIdx_.get(ci).get(dep2)==null) {
					ignoredQs.add(q);
					//Utils.println("ERR: missing arg: "+q);
				}
			}
			qs.removeAll(ignoredQs);
			
		}
		

	}
	static String removeThirdPerson(String v) {
		if (v.length()<=3) Utils.println("*** "+v);
		int l=v.length();
		if (v.charAt(l-2)!='e') {			
			return v.substring(0,l-1);
		}
		else if (v.charAt(l-3)=='i') return v.substring(0,l-3)+"y";
		else if (v.charAt(l-3)=='s' && v.charAt(l-4)=='s') return v.substring(0,l-2);
		else if (v.charAt(l-3)=='h' && v.charAt(l-4)=='s') return v.substring(0,l-2);
		else return v.substring(0,l-1);
	}
}

