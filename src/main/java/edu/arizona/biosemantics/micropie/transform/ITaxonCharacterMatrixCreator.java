package edu.arizona.biosemantics.micropie.transform;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.micropie.model.ClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.TaxonCharacterMatrix;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;

public interface ITaxonCharacterMatrixCreator {

	TaxonCharacterMatrix create(Map<String, List<Sentence>> taxonSentencesMap,
			Map<Sentence, SentenceMetadata> sentenceMetadata,
			Map<Sentence, ClassifiedSentence> predictionResult);

}
