/**
 * RelaxTestSuite.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import junit.framework.TestSuite;

public class RelaxTestSuite extends TestSuite{
    public static TestSuite suite(){
        TestSuite suite = new TestSuite();
        suite.addTest(new TestSuite(CardinalityTest.class));
        suite.addTest(new TestSuite(DataTest.class));
        suite.addTest(new TestSuite(ExternalRefTest.class));
        suite.addTest(new TestSuite(IncludeTest.class));
        suite.addTest(new TestSuite(MostBasicTest.class));
        suite.addTest(new TestSuite(MultigenTest.class));
        suite.addTest(new TestSuite(SelfRefTest.class));
        return suite;
    }
}

