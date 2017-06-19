package edu.arizona.biosemantics.micropie.eval;

import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.io.CharacterReader;
import edu.arizona.biosemantics.micropie.io.MatrixReader;

public class MatrixReaderTest {

	public static void main(String[] args){
		String[] basicFields = {"corrected by", "taxon", "file", "16S rRNA accession #", "Family", "Genus", "Species", "Strain", "Genome size"};
		String matrixFile = "F:/MicroPIE/evaluation/gold_matrix_examples_4_jin.csv";
		
		String labelmappingFile = "F:/MicroPIE/micropieInput/svmlabelandcategorymapping_data/SVMLabelAndCategoryMapping.txt";
		
		CharacterReader cateReader = new CharacterReader();
		cateReader.setCategoryFile(labelmappingFile);
		cateReader.read();
		
		MatrixReader matrixReader = new MatrixReader(basicFields, "taxon", cateReader.getCategoryNameLabelMap());
		matrixReader.readMatrixFromFile(matrixFile);
		for(String str: matrixReader.allFields){
			System.out.print(str+" | ");
		}
		System.out.println();
		for(String str: matrixReader.basicFields){
			System.out.print(str+" | ");
		}
		System.out.println();
		for(String str: matrixReader.characterFields){
			System.out.print(str+" | ");
		}
		
		matrixReader.parseMatrix("#");
	}
}
