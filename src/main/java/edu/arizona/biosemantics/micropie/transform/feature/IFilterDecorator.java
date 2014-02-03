package edu.arizona.biosemantics.micropie.transform.feature;

import weka.filters.MultiFilter;

/**
 * FilterDecorator configures a weka MultiFilter
 * @author rodenhausen
 *
 */
public interface IFilterDecorator {

	/**
	 * @param filter to configure
	 */
	void decorateFilter(MultiFilter filter);

}
