package edu.arizona.biosemantics.micropie.eval;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.io.CategoryReader;
import edu.arizona.biosemantics.micropie.io.MatrixReader;
import edu.arizona.biosemantics.micropie.model.CharacterValue;
import edu.arizona.biosemantics.micropie.model.NewTaxonCharacterMatrix;




/**
 * 
 *  1: each taxon
 * 	2: each character
 * 	3: all the extraction
 * 
 * output the details into CSV files
 * 
 * @author maojin
 *
 */
public class ExtractionEvaluation {
	private String[] gstBasicFields;//basic fields used to describe the taxon/taxon file
	private String[] tgBasicFields;
	private String gstKeyField;//the field of the unique taxon name
	private String tgKeyField;
	
	private String categoryMappingFile; 
	
	private ILabel[] comparedCharacterLabels;
	private String[] comparedCharacterNames;
	
	private IValueComparator valueComparator;
	
	
	
	
	/**
	 * the gold standard matrix and the target matrix have the same structure
	 * @param basicFields
	 * @param keyField
	 * @param categoryMappingFile
	 */
	public ExtractionEvaluation(String basicFields,String keyField,String categoryMappingFile){
		String[] fields = basicFields.split(",");
		this.gstBasicFields = fields;
		this.tgBasicFields = fields;
		this.gstKeyField = keyField;
		this.tgKeyField = keyField;
		
		this.categoryMappingFile = categoryMappingFile;
	}
	
	/**
	 * the gold standard matrix and the target matrix have the same structures
	 * @param gstBasicFields
	 * @param tgBasicFields
	 * @param gstKeyField
	 * @param tgKeyField
	 * @param categoryMappingFile
	 */
	public ExtractionEvaluation(String gstBasicFields,String tgBasicFields,String gstKeyField,String tgKeyField,String categoryMappingFile){
		
		String[] gstFields = gstBasicFields.split("|");
		String[] tsFields = tgBasicFields.split("|");
		
		this.gstBasicFields = gstFields;
		this.tgBasicFields = tsFields;
		this.gstKeyField = gstKeyField;
		this.tgKeyField = tgKeyField;
		
		this.categoryMappingFile = categoryMappingFile;
	}
	
	public void setValueComparator(IValueComparator valueComparator) {
		this.valueComparator = valueComparator;
	}

	public static void main(String[] args){
		String[] basicFields = null;
		String keyField = null;
		
		String comparedCharacterName ="%G+C|Cell shape|Cell membrane & cell wall components|Motility|Pigment compounds|Biofilm formation|Habitat isolated from|NaCl minimum|NaCl optimum|NaCl maximum|pH minimum|pH optimum|pH maximum|Temperature minimum|Temperature optimum|Temperature maximum|Oxygen use|Salinity preference|Host|Symbiotic relationship|Pathogenic|Disease caused|Metabolism (energy & carbon source)|Metabolism (energy & carbon source) NOT used|Fermentation products|Polyalkanoates|Antibiotic sensitivity|Antibiotic resistant";
		
		String labelmappingFile = "F:/MicroPIE/micropieInput/svmlabelandcategorymapping_data/categoryMapping_paper.txt";
		
	}
	
	
	/**
	 * make evaluation
	 * @param basicFields
	 * @param keyField
	 * @param labelmappingFile
	 * @param goldStandardsMatrixFile
	 * @param targetMatrixFile
	 */
	public void evaluate(String goldStandardsMatrixFile, String targetMatrixFile){
		
		CategoryReader cateReader = new CategoryReader();
		cateReader.setCategoryFile(this.categoryMappingFile);
		cateReader.read();
		Map<String, ILabel> characterNameLabelMapping = cateReader.getCategoryNameLabelMap();
		
		
		//parse gold standard matrix and taget matrix
		MatrixReader gsdMatrixReader = new MatrixReader(gstBasicFields, gstKeyField, characterNameLabelMapping);
		gsdMatrixReader.readMatrixFromFile(goldStandardsMatrixFile);
		NewTaxonCharacterMatrix gsdMatrix = gsdMatrixReader.parseMatrix("#");
		
		MatrixReader tgMatrixReader = new MatrixReader(tgBasicFields, tgKeyField, characterNameLabelMapping);
		tgMatrixReader.readMatrixFromFile(targetMatrixFile);
		NewTaxonCharacterMatrix tgMatrix = tgMatrixReader.parseMatrix("#");
		
		compareResults(gsdMatrix, tgMatrix);
	}
	
	
	/**
	 * compare the two character matrix
	 * @param gsdMatrix
	 * @param tgMatrix
	 */
	public void compareResults(NewTaxonCharacterMatrix gsdMatrix, NewTaxonCharacterMatrix tgMatrix){
		
		//for evaluation
		double gstTotalValueNum = 0;
		double tgTotalValueNum = 0;
		double matchedTotalCredit = 0;
		
		int charLength = comparedCharacterLabels.length;
		
		//compare all the taxa
		Set<String> taxa = gsdMatrix.keySet();
		Iterator<String> taxaIter = taxa.iterator();
		while(taxaIter.hasNext()){
			String taxonName = taxaIter.next();
			Map<ILabel, List> gstAllCharacterValues = gsdMatrix.getAllTaxonCharacterValues(taxonName);
			Map<ILabel, List> tgAllCharacterValues = tgMatrix.getAllTaxonCharacterValues(taxonName);
			
			//compare all the characters
			for(int ch = 0; ch<charLength;ch++){
				ILabel charLabel = comparedCharacterLabels[ch];
				
				List<CharacterValue> gstCharValue = gstAllCharacterValues.get(charLabel);
				List<CharacterValue> tgCharValue = tgAllCharacterValues.get(charLabel);
				
				double matched = valueComparator.compare(gstCharValue, tgCharValue);
				
				matchedTotalCredit += matched;
			}
		}
		
		
		//measure report
		
		
		
	}
	
	
	/**
	 * calculate the measurements for a taxon
	 * @param taxon
	 * @return
	 */
	public List evaluateTaxon(String taxon){
		double totalExtValueSize = 0;
		double positiveSize = 0;
		double totalGstValueSize = 0;
		
		Map<ILabel,Set<CharacterValue>> extTaxonCharMap = (Map<ILabel, Set<CharacterValue>>) extResultMap.get(taxon);
		Map<ILabel,Set<CharacterValue>> gstTaxonCharMap = (Map<ILabel, Set<CharacterValue>>) goldstdMap.get(taxon);
		
		
		//Set<ILabel> labels = gstTaxonCharMap.keySet();
		
		//Iterator<ILabel> labelIter = labels.iterator();
		//while(labelIter.hasNext()){
			//ILabel label = labelIter.next();
		Label[] labels = Label.values();
		for(ILabel label : labels){
			//character value set for this character in the extraction results
			Set<CharacterValue> extCharValueMap =  extTaxonCharMap.get(label);
			//character value set for this character in the gold standards
			Set<CharacterValue> gstCharValueMap = gstTaxonCharMap.get(label);
			

			if(extCharValueMap!=null){
				//extracted number
				int extSize = extCharValueMap.size();
				totalExtValueSize+=extSize;
			}
			if(gstCharValueMap!=null){
				//gold standard number
				int gstSize = gstCharValueMap.size();
				totalGstValueSize+=gstSize;
			}
			
			if(extCharValueMap!=null&&gstCharValueMap!=null){
				//positive number
				int positiveNum = valueComparator.compare(extCharValueMap, gstCharValueMap);
				positiveSize+=positiveNum;
			}
			
		}//all labels are over
		
		//calculate the values
		return calMeasure(totalExtValueSize, positiveSize,totalGstValueSize);
	}
	
	/**
	 * calculate the measurements for a character
	 * @param label
	 */
	public List evaluateCharacter(ILabel label){
		Set<String> taxonSet = extResultMap.keySet();
		//int taxonSize = taxonSet.size();
		
		double totalExtValueSize = 0;
		double positiveSize = 0;
		double totalGstValueSize = 0;
		Iterator<String> taxonIter = taxonSet.iterator();
		while(taxonIter.hasNext()){
			String taxon = taxonIter.next();
			
			//character map for this taxon
			Map<ILabel,Set<CharacterValue>> extTaxonCharMap = (Map<ILabel, Set<CharacterValue>>) extResultMap.get(taxon);
			Map<ILabel,Set<CharacterValue>> gstTaxonCharMap = (Map<ILabel, Set<CharacterValue>>) goldstdMap.get(taxon);
			
			//character value set for this character in the extraction results
			Set<CharacterValue> extCharValueMap =  extTaxonCharMap.get(label);
			//character value set for this character in the gold standards
			Set<CharacterValue> gstCharValueMap = gstTaxonCharMap.get(label);
			
			if(extCharValueMap!=null){
				//extracted number
				int extSize = extCharValueMap.size();
				totalExtValueSize+=extSize;
			}
			if(gstCharValueMap!=null){
				//gold standard number
				int gstSize = gstCharValueMap.size();
				totalGstValueSize+=gstSize;
			}
			
			if(extCharValueMap!=null&&gstCharValueMap!=null){
				//positive number
				int positiveNum = valueComparator.compare(extCharValueMap, gstCharValueMap);
				positiveSize+=positiveNum;
			}
			
		}//taxa over
		
		//calculate the values
		return calMeasure(totalExtValueSize, positiveSize,totalGstValueSize);
	}
	
	
	/**
	 * 
	 * @param totalExtValueSize
	 * @param positiveSize
	 * @param totalGstValueSize
	 * @return
	 */
	private List calMeasure(double totalExtValueSize, double positiveSize,
			double totalGstValueSize) {
		double p = positiveSize/totalExtValueSize;
		double r =positiveSize/totalGstValueSize;
		double f1 = 2*p*r/(p+r);
		
		List meaList = new ArrayList();
		meaList.add(new Measurement("P",p));
		meaList.add(new Measurement("R",r));
		meaList.add(new Measurement("F1",f1));
		return meaList;
	}
}
