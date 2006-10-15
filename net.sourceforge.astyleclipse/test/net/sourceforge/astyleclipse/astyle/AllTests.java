package net.sourceforge.astyleclipse.astyle;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for net.sourceforge.astyleclipse.astyle");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestASStreamIterator.class);
		suite.addTestSuite(TestASBeautifier.class);
		suite.addTestSuite(TestASFormatter.class);
		//$JUnit-END$
		return suite;
	}

}
