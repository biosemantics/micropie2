package semanticMarkup.ling.learn.knowledge;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.SentenceStructure;

/**
 * Annotate sentences based on ignore patterns
 * @author Dongye
 *
 */
public class IgnorePatternAnnotator implements IModule {

	public IgnorePatternAnnotator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		this.markupIgnore(dataholderHandler);
	}
	
	public void markupIgnore(DataHolder dataholderHandler) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.markupIgnore");
		myLogger.trace("Enter markupIgnore");

		for (int i = 0; i < dataholderHandler.getSentenceHolder().size(); i++) {
			boolean flag = markupIgnoreHelper(dataholderHandler
					.getSentenceHolder().get(i));
			if (flag) {
				myLogger.debug("Updated Sentence #" + i);
			}
		}

		myLogger.trace("Quite markupIgnore");
	}
	
	public boolean markupIgnoreHelper(SentenceStructure sentence) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("markupIgnore");

		String thisOriginalSentence = sentence.getOriginalSentence();
		String pattern = "(^|^ )" + Constant.IGNORE_PATTERN + ".*$";
		if (thisOriginalSentence.matches(pattern)) {
			sentence.setTag("ignore");
			sentence.setModifier("");
			myLogger.trace("Set Tag to \"ignore\", Modifier to \"\"");

			return true;
		}

		return false;
	}

}
