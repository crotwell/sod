package edu.sc.seis.sod.process.waveform;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author groves
 * Created on Sep 8, 2004
 */
public class SomeDataCoverageTest  {
    
	@BeforeEach
    public void setUp() {
        sc = new SomeDataCoverage();
    }

    @Test
    public void testTooLateBeginAndTooEarlyEnd() {
        CoverageTestData ctd = CoverageTestData.makeTooLateBeginAndTooEarlyEnd();
        assertTrue(sc.accept(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    @Test
    public void testTooEarlyEnd() {
        CoverageTestData ctd = CoverageTestData.makeTooEarlyEndTime();
        assertTrue(sc.accept(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    @Test
    public void testOverage() {
        CoverageTestData ctd = CoverageTestData.makeOverage();
        assertTrue(sc.accept(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    @Test
    public void testNoData() {
        CoverageTestData ctd = CoverageTestData.makeNoData();
        assertFalse(sc.accept(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    @Test
    public void testCompleteMiss() {
        CoverageTestData ctd = CoverageTestData.makeCompleteMiss();
        assertFalse(sc.accept(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    private SomeDataCoverage sc;
}
