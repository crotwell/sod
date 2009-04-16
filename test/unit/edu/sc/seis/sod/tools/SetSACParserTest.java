package edu.sc.seis.sod.tools;

import java.util.Map;

import junit.framework.TestCase;

import com.martiansoftware.jsap.ParseException;

public class SetSACParserTest extends TestCase {

    public void testAHeader() throws ParseException {
        Map results = (Map)ssp.parse("tts-a");
        assertEquals("a", results.get("header"));
        assertEquals("tts", results.get("phase"));
    }

    public void test0Header() throws ParseException {
        Map results = (Map)ssp.parse("P-0");
        assertEquals("0", results.get("header"));
        assertEquals("P", results.get("phase"));
    }

    public void testSingleAutoHeader() throws ParseException {
        Map results = (Map)ssp.parse("P");
        assertEquals("0", results.get("header"));
        assertEquals("P", results.get("phase"));
    }

    public void testMultipleAutoHeader() throws ParseException {
        Map results = (Map)ssp.parse("P");
        assertEquals("0", results.get("header"));
        assertEquals("P", results.get("phase"));
        results = (Map)ssp.parse("tts");
        assertEquals("1", results.get("header"));
        assertEquals("tts", results.get("phase"));
    }

    public void testSpecifiedAndAutoHeader() throws ParseException {
        Map results = (Map)ssp.parse("P-0");
        assertEquals("0", results.get("header"));
        assertEquals("P", results.get("phase"));
        results = (Map)ssp.parse("tts");
        assertEquals("1", results.get("header"));
        assertEquals("tts", results.get("phase"));
        results = (Map)ssp.parse("ttp");
        assertEquals("2", results.get("header"));
        assertEquals("ttp", results.get("phase"));
        results = (Map)ssp.parse("q-a");
        assertEquals("a", results.get("header"));
        assertEquals("q", results.get("phase"));
    }

    public void testValidHeaderRequired() {
        try {
            ssp.parse("P-Q");
            fail("a valid header should be required");
        } catch(ParseException e) {
            assertTrue(e.getMessage().indexOf("P-Q") != -1);
        }
    }

    public void testPhaseRequired() {
        try {
            ssp.parse("-a");
            fail("a phase should be required");
        } catch(ParseException e) {
            assertTrue(e.getMessage().indexOf("-a") != -1);
        }
    }

    SetSACParser ssp = new SetSACParser();
}
