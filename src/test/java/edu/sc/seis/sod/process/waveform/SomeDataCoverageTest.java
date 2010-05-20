package edu.sc.seis.sod.process.waveform;

import junit.framework.TestCase;


/**
 * @author groves
 * Created on Sep 8, 2004
 */
public class SomeDataCoverageTest extends TestCase {
    
    public void setUp() {
        sc = new SomeDataCoverage();
    }

    public void testTooLateBeginAndTooEarlyEnd() {
        CoverageTestData ctd = CoverageTestData.makeTooLateBeginAndTooEarlyEnd();
        assertTrue(sc.process(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    public void testTooEarlyEnd() {
        CoverageTestData ctd = CoverageTestData.makeTooEarlyEndTime();
        assertTrue(sc.process(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    public void testOverage() {
        CoverageTestData ctd = CoverageTestData.makeOverage();
        assertTrue(sc.process(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    public void testNoData() {
        CoverageTestData ctd = CoverageTestData.makeNoData();
        assertFalse(sc.process(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    public void testCompleteMiss() {
        CoverageTestData ctd = CoverageTestData.makeCompleteMiss();
        assertFalse(sc.process(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    private SomeDataCoverage sc;
}
