package edu.arizona.biosemantics.micropie.io;

import java.util.List;

import edu.arizona.biosemantics.micropie.model.ClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;

public interface IMatrixWriter {

	void write(TaxonCharacterMatrix matrix) throws Exception;

}
