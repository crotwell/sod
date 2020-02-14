package edu.sc.seis.sod.process.waveform;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import junit.framework.TestCase;

/**
 * @author groves Created on Sep 8, 2004
 */
public class NoDataGapsTest {

	@BeforeEach
    public void setUp() {
        ng = new NoDataGaps();
    }

    @Test
    public void testOverage() {
        CoverageTestData ctd = CoverageTestData.makeOverage();
        assertTrue(ng.accept(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    @Test
    public void testNoData() {
        CoverageTestData ctd = CoverageTestData.makeNoData();
        assertTrue(ng.accept(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    @Test
    public void testCompleteMiss() {
        CoverageTestData ctd = CoverageTestData.makeCompleteMiss();
        assertTrue(ng.accept(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    @Test
    public void testContigousData() {
        CoverageTestData ctd = CoverageTestData.makeContigousData();
        assertTrue(ng.accept(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    @Test
    public void testSlightlySeperatedData() {
        CoverageTestData ctd = CoverageTestData.makeSlightlySeperatedData();
        assertFalse(ng.accept(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    @Test
    public void testOverlappingData(){
        CoverageTestData ctd = CoverageTestData.makeOverlappingData();
        assertTrue(ng.accept(null, null, ctd.request, null, ctd.seis, null)
                .isSuccess());
    }

    private NoDataGaps ng;
}