package edu.arizona.biosemantics.micropie.transform;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.arizona.biosemantics.micropie.nlptool.SentenceSpliter;

public class SentenceSplitterTest {

	@Test
	public void test() {
		SentenceSpliter sentSpliter = new SentenceSpliter();
		
		String test = "AAA is bbb C. BBB is ccc D. The strain number is 123. A. Andrew is a good man";
		sentSpliter.splitByCapitalPeriod(test);
		//fail("Not yet implemented");
	}

}
