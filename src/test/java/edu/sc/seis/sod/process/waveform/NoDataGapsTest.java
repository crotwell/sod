package edu.sc.seis.sod.process.waveform;

import junit.framework.TestCase;

/**
 * @author groves Created on Sep 8, 2004
 */
public class NoDataGapsTest extends TestCase {

    public void setUp() {
        ng = new NoDataGaps();
    }

    public void testOverage() {
        CoverageTestData ctd = CoverageTestData.makeOverage();
        assertTrue(ng.process(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    public void testNoData() {
        CoverageTestData ctd = CoverageTestData.makeNoData();
        assertTrue(ng.process(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    public void testCompleteMiss() {
        CoverageTestData ctd = CoverageTestData.makeCompleteMiss();
        assertTrue(ng.process(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    public void testContigousData() {
        CoverageTestData ctd = CoverageTestData.makeContigousData();
        assertTrue(ng.process(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    public void testSlightlySeperatedData() {
        CoverageTestData ctd = CoverageTestData.makeSlightlySeperatedData();
        assertFalse(ng.process(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }
    
    public void testOverlappingData(){
        CoverageTestData ctd = CoverageTestData.makeOverlappingData();
        assertTrue(ng.process(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    private NoDataGaps ng;
}