package edu.sc.seis.sod.process.waveform;

import junit.framework.TestCase;

/**
 * @author groves Created on Sep 8, 2004
 */
public class FullDataCoverageTest extends TestCase {
    public void setUp(){
        fc = new FullDataCoverage();
    }
    
    public void testOverage(){
        CoverageTestData ctd = CoverageTestData.makeOverage();
        assertTrue(fc.process( null, null, ctd.request, null, ctd.seis, null).isSuccess());
    }
    
    public void testEqualTimes(){
        CoverageTestData ctd = CoverageTestData.makeEqualTimes();
        assertTrue(fc.process( null, null, ctd.request, null, ctd.seis, null).isSuccess());
    }
    
    public void testTooEarlyEndTime(){
        CoverageTestData ctd = CoverageTestData.makeTooEarlyEndTime();
        assertFalse(fc.process( null, null, ctd.request, null, ctd.seis, null).isSuccess());
    }
    
    public void testTooLateBeginAndTooEarlyEnd(){
        CoverageTestData ctd = CoverageTestData.makeTooLateBeginAndTooEarlyEnd();
        assertFalse(fc.process( null, null, ctd.request, null, ctd.seis, null).isSuccess());
    }
    
    public void testNoData(){
        CoverageTestData ctd = CoverageTestData.makeNoData();
        assertFalse(fc.process(null, null, ctd.request, null, ctd.seis, null).isSuccess());
    }
    
    public void testCompleteMiss(){
        CoverageTestData ctd = CoverageTestData.makeCompleteMiss();
        assertFalse(fc.process(null, null, ctd.request, null, ctd.seis, null).isSuccess());
    }
    
    private FullDataCoverage fc;
}