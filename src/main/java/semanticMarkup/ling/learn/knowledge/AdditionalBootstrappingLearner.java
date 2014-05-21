package semanticMarkup.ling.learn.knowledge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import semanticMarkup.ling.learn.Configuration;
import semanticMarkup.ling.learn.auxiliary.SentenceLeadLengthComparator;
import semanticMarkup.ling.learn.dataholder.DataHolder;
import semanticMarkup.ling.learn.dataholder.SentenceStructure;
import semanticMarkup.ling.learn.utility.LearnerUtility;
import semanticMarkup.ling.learn.utility.StringUtility;

/**
 * Do bootstrapping learning using clues such as shared subject different
 * boundary and one lead word.
 * 
 * @author Dongye
 * 
 */
public class AdditionalBootstrappingLearner implements IModule {
	private LearnerUtility myLearnerUtility;
	private Configuration myConfiguration;

	public AdditionalBootstrappingLearner(LearnerUtility learnerUtility, Configuration configuration) {
		this.myLearnerUtility = learnerUtility;
		this.myConfiguration = configuration;
	}

	@Override
	public void run(DataHolder dataholderHandler) {
		this.additionalBootstrapping(dataholderHandler);
	}
	
	public void additionalBootstrapping(DataHolder dataholderHandler) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger.getLogger("learn.additionalBootStrapping");
		myLogger.trace("[additionalBootStrapping]Start");

		// dataholderHandler.printHolder(DataHolder.SENTENCE);

		int flag = 0;

		do {
			myLogger.trace(String.format("Enter one do-while loop iteration"));
			flag = 0;

			// warmup markup
			int cmReturn = wrapupMarkup(dataholderHandler);
			myLogger.trace(String
					.format("wrapupMarkup() returned %d", cmReturn));
			flag += cmReturn;

			// one lead word markup
			Set<String> tags = dataholderHandler.getCurrentTags();
			myLogger.trace(tags.toString());
			int omReturn = oneLeadWordMarkup(dataholderHandler, tags);
			myLogger.trace(String.format("oneLeadWordMarkup() returned %d",
					omReturn));
			flag += omReturn;

			// doit markup
			int dmReturn = this.myLearnerUtility.doItMarkup(dataholderHandler, this.myConfiguration.getMaxTagLength());
			myLogger.trace(String.format("doItMarkup() returned %d", dmReturn));
			flag += dmReturn;

			myLogger.trace(String.format("Quite this iteration with flag = %d",
					flag));
		} while (flag > 0);

		myLogger.trace("[additionalBootStrapping]End");
	}
	
	/**
	 * In the sentence collections, search for such sentence, whose lead is
	 * among the tags passed in, and add the lead into word POS collections as a
	 * noun
	 * 
	 * @param tags
	 *            a set of all tags in the tagged sentences in the sentence
	 *            collection
	 * @return the numbet of updates made
	 */
	public int oneLeadWordMarkup(DataHolder dataholderHandler, Set<String> tags) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger
				.getLogger("learn.additionalBootStrapping.oneLeadWordMarkup");
		// String tags = StringUtility.joinList("|", tags);
		int sign = 0;
		myLogger.trace(String.format("Enter (%s)", tags));

		Iterator<SentenceStructure> iter = dataholderHandler
				.getSentenceHolder().iterator();

		while (iter.hasNext()) {
			SentenceStructure sentence = iter.next();
			int ID = sentence.getID();
			String tag = sentence.getTag();
			String lead = sentence.getLead();

			if ((tag == null)
					&& (!(StringUtility.createMatcher(lead, ".* .*").find()))) {
				if (tags.contains(lead)) {
					this.myLearnerUtility.tagSentence(dataholderHandler, this.myConfiguration.getMaxTagLength(), ID, lead);
					myLogger.trace(String.format(
							"updateDataHolder(%s, n, -, wordpos, 1)", lead));
					sign += dataholderHandler.updateDataHolder(lead, "n", "-",
							"wordpos", 1);
				}
			}
		}

		myLogger.trace("Return: " + sign);
		return 0;
	}
	
	/**
	 * for the remaining of sentences that do not have a tag yet, look for lead
	 * word co-ocurrance, use the most freq. co-occured phrases as tags e.g.
	 * plication induplicate (n times) and plication reduplicate (m times) =>
	 * plication is the tag and a noun e.g. stigmatic scar basal (n times) and
	 * stigmatic scar apical (m times) => stigmatic scar is the tag and scar is
	 * a noun. what about externally like A; externally like B, functionally
	 * staminate florets, functionally staminate xyz?
	 * 
	 * @return
	 */
	public int wrapupMarkup(DataHolder dataholderHandler) {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger myLogger = Logger
				.getLogger("learn.additionalBootStrapping.wrapupMarkup");
		myLogger.trace("Enter");

		int sign = 0;
		Set<Integer> checkedIDs = new HashSet<Integer>();
		List<SentenceStructure> sentenceList = new LinkedList<SentenceStructure>();

		for (int id1 = 0; id1 < dataholderHandler.getSentenceHolder().size(); id1++) {
			SentenceStructure sentence = dataholderHandler.getSentenceHolder()
					.get(id1);
			String tag = sentence.getTag();
			String lead = sentence.getLead();

			if ((tag == null)
					&& (StringUtility.createMatcher(lead, ".* .*").find())) {
				sentenceList.add(sentence);
			}
		}

		SentenceLeadLengthComparator myComparator = new SentenceLeadLengthComparator(
				false);
		Collections.sort(sentenceList, myComparator);

		Iterator<SentenceStructure> iter1 = sentenceList.iterator();
		while (iter1.hasNext()) {
			SentenceStructure sentence = iter1.next();
			int ID1 = sentence.getID();
			String lead = sentence.getLead();
			// if this sentence has been checked, pass
			if (checkedIDs.contains(ID1)) {
				continue;
			}

			List<String> words = new ArrayList<String>();
			words.addAll(Arrays.asList(lead.split("\\s+")));

			List<String> sharedHead = new ArrayList<String>();
			sharedHead.addAll(words.subList(0, words.size() - 1));
			String match = StringUtility.joinList(" ", sharedHead);

			Set<SentenceStructure> sentenceSet = new HashSet<SentenceStructure>();
			for (int index = 0; index < dataholderHandler.getSentenceHolder()
					.size(); index++) {
				SentenceStructure thisSentence = dataholderHandler
						.getSentenceHolder().get(index);
				String thisLead = thisSentence.getLead();
				String tag = thisSentence.getTag();
				String pTemp = "^" + match + " [\\S]+$";
				myLogger.trace(thisLead);
				myLogger.trace(pTemp);

				// if ((tag==null) && StringUtility.isMatchedNullSafe(pTemp,
				// thisLead)) {
				if ((tag == null)
						&& StringUtility.isMatchedNullSafe(thisLead, pTemp)) {
					if (!StringUtils.equals(thisLead, lead)) {
						sentenceSet.add(thisSentence);
					}
				}
			}

			if (sentenceSet.size() > 1) {
				String ptn = this.myLearnerUtility.getPOSptn(dataholderHandler, sharedHead);
				String wnPOS = this.myLearnerUtility.getWordFormUtility()
						.checkWN(sharedHead.get(sharedHead.size() - 1), "pos");

				myLogger.trace("ptn: " + ptn);
				myLogger.trace("wnPOS: " + wnPOS);

				if ((StringUtility.createMatcher(ptn, "[nsp]$").find())
						|| ((StringUtility.createMatcher(ptn, "\\?$").find()) && (StringUtility
								.createMatcher(wnPOS, "n").find()))) {

					Iterator<SentenceStructure> iter2 = sentenceSet.iterator();
					while (iter2.hasNext()) {
						SentenceStructure thisSentence = iter2.next();
						int ID = thisSentence.getID();
						String thisLead = thisSentence.getLead();

						List<String> words2 = new ArrayList<String>();
						words2.addAll(Arrays.asList(thisLead.split("\\s+")));

						// case 1
						boolean case1 = false;
						boolean case2 = false;
						case1 = words2.size() > sharedHead.size();
						if (case1) {
							List<String> checkWord = new ArrayList<String>();
							checkWord.add(words2.get(sharedHead.size()));
							case2 = StringUtility.createMatcher(
									this.myLearnerUtility.getPOSptn(dataholderHandler, checkWord), "[psn]").find();
						}

						if (case1 && case2) {
							myLogger.trace("Case 1");
							String nb = words2.size() >= sharedHead.size() + 2 ? words2
									.get(sharedHead.size() + 1) : "";
							words2 = StringUtility.stringArraySplice(words2, 0,
									sharedHead.size() + 1);
							String nmatch = StringUtility.joinList(" ", words2);

							this.myLearnerUtility.tagSentence(dataholderHandler, this.myConfiguration.getMaxTagLength(),ID, nmatch);
							myLogger.trace(String.format("tag (%d, %s)", ID,
									nmatch));
							this.myLearnerUtility.tagSentence(dataholderHandler, this.myConfiguration.getMaxTagLength(),ID1, match);
							myLogger.trace(String.format("tag (%d, %s)", ID1,
									match));

							String updatedWord = words2.get(words2.size() - 1);
							int update1 = dataholderHandler.updateDataHolder(
									updatedWord, "n", "-", "wordpos", 1);
							sign += update1;
							myLogger.trace(String.format("update (%s)",
									updatedWord));

							if (!StringUtils.equals(nb, "")) {
								int update2 = dataholderHandler
										.updateDataHolder(nb, "b", "",
												"wordpos", 1);
								sign += update2;
								myLogger.trace(String.format("update (%s)", nb));
							}

							updatedWord = words.get(words.size() - 1);
							int update3 = dataholderHandler.updateDataHolder(
									words.get(words.size() - 1), "b", "",
									"wordpos", 1);
							sign += update3;
							myLogger.trace(String.format("update (%s)",
									updatedWord));
						}
						// case 2
						else {
							myLogger.trace("Case 2");
							String b = words2.size() >= sharedHead.size() + 1 ? words2
									.get(sharedHead.size()) : "";

							this.myLearnerUtility.tagSentence(dataholderHandler, this.myConfiguration.getMaxTagLength(),ID, match);
							this.myLearnerUtility.tagSentence(dataholderHandler, this.myConfiguration.getMaxTagLength(),ID1, match);

							// if (sharedHead.get(sharedHead.size() -
							// 1).equals("tissue")) {
							// System.out.println();
							// }

							int update1 = dataholderHandler.updateDataHolder(
									sharedHead.get(sharedHead.size() - 1), "n",
									"-", "wordpos", 1);
							sign += update1;
							if (!StringUtils.equals(b, "")) {
								int update2 = dataholderHandler
										.updateDataHolder(b, "b", "",
												"wordpos", 1);
								sign += update2;
							}
							int update3 = dataholderHandler.updateDataHolder(
									words.get(words.size() - 1), "b", "",
									"wordpos", 1);
							sign += update3;

						}
						checkedIDs.add(ID);
					}
				} else {
					Iterator<SentenceStructure> iter2 = sentenceSet.iterator();
					while (iter2.hasNext()) {
						SentenceStructure thisSentence = iter2.next();
						int ID = thisSentence.getID();
						checkedIDs.add(ID);
					}
				}
			} else {
				checkedIDs.add(ID1);
			}
		}

		myLogger.trace("Return " + sign);
		return sign;
	}

}
