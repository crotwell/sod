/**
 * RelaxTestSuite.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import org.junit.jupiter.api.Test;

import junit.framework.TestSuite;

public class RelaxTestSuite {
	
	@Test
    public static TestSuite suite(){
        TestSuite suite = new TestSuite();
        suite.addTest(new TestSuite(CardinalityTest.class));
        suite.addTest(new TestSuite(CommentTest.class));
        suite.addTest(new TestSuite(ExternalRefTest.class));
        suite.addTest(new TestSuite(IncludeTest.class));
        suite.addTest(new TestSuite(MostBasicTest.class));
        suite.addTest(new TestSuite(MultigenTest.class));
        suite.addTest(new TestSuite(ParentTest.class));
        suite.addTest(new TestSuite(SelfRefTest.class));
        return suite;
    }
}

