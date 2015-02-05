package edu.arizona.biosemantics.micropie.log;

import edu.arizona.biosemantics.common.log.AbstractLogInjection;
import edu.arizona.biosemantics.common.log.ILoggable;

public aspect LogInjection extends AbstractLogInjection {
	
	declare parents : edu.arizona.biosemantics.micropie..* implements ILoggable;
	declare parents : weka.core.logging.LoggerBridge implements ILoggable;

}