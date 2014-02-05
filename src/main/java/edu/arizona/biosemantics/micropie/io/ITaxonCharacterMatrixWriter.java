package edu.arizona.biosemantics.micropie.io;

import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;

public interface ITaxonCharacterMatrixWriter {

	void write(TaxonCharacterMatrix matrix) throws Exception;

}
