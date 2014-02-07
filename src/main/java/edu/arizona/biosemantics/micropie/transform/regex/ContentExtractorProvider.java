package edu.arizona.biosemantics.micropie.transform.regex;

import java.util.HashSet;
import java.util.Set;

import com.google.inject.Inject;

import edu.arizona.biosemantics.micropie.classify.Label;

public class ContentExtractorProvider implements IContentExtractorProvider {

	private GcExtractor gcExtractor;
	private GrowthPhExtractor growthPhExtractor;
	private CellSizeExtractor cellSizeExtractor;
	private CellShapeExtractor cellShapeExtractor;

	@Inject
	public ContentExtractorProvider(GcExtractor gcExtractor, GrowthPhExtractor growthPhExtractor, CellSizeExtractor cellSizeExtractor, CellShapeExtractor cellShapeExtractor) {
		this.gcExtractor = gcExtractor;
		this.growthPhExtractor = growthPhExtractor;
		this.cellSizeExtractor = cellSizeExtractor;
		this.cellShapeExtractor = cellShapeExtractor;
	}
	
	@Override
	public Set<IContentExtractor> getContentExtractor(Label label) {
		Set<IContentExtractor> extractors = new HashSet<IContentExtractor>();
		switch(label) {
		case c1:
			extractors.add(gcExtractor);
			return extractors;
		case c2:
			extractors.add(growthPhExtractor);
			extractors.add(cellShapeExtractor);
			return extractors;
		case c3:
			extractors.add(cellSizeExtractor);
			return extractors;
		default:
			return extractors;
		}
	}

	@Override
	public boolean hasExtractor(Label label) {
		return !this.getContentExtractor(label).isEmpty();
	}

}
