package edu.sc.seis.sod.process.waveform;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author groves Created on Sep 8, 2004
 */
public class FullDataCoverageTest  {
    public void setUp(){
        fc = new FullDataCoverage();
    }

    @Test
    public void testOverage(){
        CoverageTestData ctd = CoverageTestData.makeOverage();
        assertTrue(fc.accept( null, null, ctd.request, null, ctd.seis, null).isSuccess());
    }

    @Test
    public void testEqualTimes(){
        CoverageTestData ctd = CoverageTestData.makeEqualTimes();
        assertTrue(fc.accept( null, null, ctd.request, null, ctd.seis, null).isSuccess());
    }

    @Test
    public void testTooEarlyEndTime(){
        CoverageTestData ctd = CoverageTestData.makeTooEarlyEndTime();
        assertFalse(fc.accept( null, null, ctd.request, null, ctd.seis, null).isSuccess());
    }

    @Test
    public void testTooLateBeginAndTooEarlyEnd(){
        CoverageTestData ctd = CoverageTestData.makeTooLateBeginAndTooEarlyEnd();
        assertFalse(fc.accept( null, null, ctd.request, null, ctd.seis, null).isSuccess());
    }

    @Test
    public void testNoData(){
        CoverageTestData ctd = CoverageTestData.makeNoData();
        assertFalse(fc.accept(null, null, ctd.request, null, ctd.seis, null).isSuccess());
    }

    @Test
    public void testCompleteMiss(){
        CoverageTestData ctd = CoverageTestData.makeCompleteMiss();
        assertFalse(fc.accept(null, null, ctd.request, null, ctd.seis, null).isSuccess());
    }
    
    private FullDataCoverage fc;
}