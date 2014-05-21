package semanticMarkup.ling.learn.knowledge;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.SentenceStructure;

/**
 * PatternBaseAnnotator Module: Annotate clauses which have some special
 * patterns. Those patterns are highly reliable indicators for annotating the
 * clauses.
 * 
 * @author Dongye
 * 
 */
public class PatternBasedAnnotator implements IModule {

	public PatternBasedAnnotator() {
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.markupByPattern");
		myLogger.trace("Enter markupByPattern");

		int size = dataholderHandler.getSentenceHolder().size();

		for (int i = 0; i < size; i++) {
			boolean flag = markupByPatternHelper(dataholderHandler
					.getSentenceHolder().get(i));
			if (flag) {
				myLogger.debug("Updated Sentence #" + i);
			}
		}
		myLogger.trace("Quite markupByPattern");
	}

	public boolean markupByPatternHelper(SentenceStructure sentence) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("markupByPattern");
		// case 1
		if (sentence.getOriginalSentence().matches("^x=.*")) {
			myLogger.trace("Case 1");
			sentence.setTag("chromosome");
			sentence.setModifier("");
			return true;
		}
		// case 2
		else if (sentence.getOriginalSentence().matches("^2n=.*")) {
			myLogger.trace("Case 2");
			sentence.setTag("chromosome");
			sentence.setModifier("");
			return true;
		}
		// case 3
		else if (sentence.getOriginalSentence().matches("^x .*")) {
			myLogger.trace("Case 3");
			sentence.setTag("chromosome");
			sentence.setModifier("");
			return true;
		}
		// case 4
		else if (sentence.getOriginalSentence().matches("^2n .*")) {
			myLogger.trace("Case 4");
			sentence.setTag("chromosome");
			sentence.setModifier("");
			return true;
		}
		// case 5
		else if (sentence.getOriginalSentence().matches("^2 n.*")) {
			myLogger.trace("Case 5");
			sentence.setTag("chromosome");
			sentence.setModifier("");
			return true;
		}
		// case 6
		else if (sentence.getOriginalSentence().matches("^fl.*")) {
			myLogger.trace("Case 6");
			sentence.setTag("flowerTime");
			sentence.setModifier("");
			return true;
		}
		// case 7
		else if (sentence.getOriginalSentence().matches("^fr.*")) {
			myLogger.trace("Case 7");
			sentence.setTag("fruitTime");
			sentence.setModifier("");
			return true;
		}
		return false;
	}

}
