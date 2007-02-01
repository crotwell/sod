package edu.sc.seis.sod.tools;

import java.util.Map;
import com.martiansoftware.jsap.ParseException;
import junit.framework.TestCase;

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

    public void testDashRequired() {
        try {
            ssp.parse("0P");
            fail("a dash should be required");
        } catch(ParseException e) {
            assertTrue(e.getMessage().contains("0P"));
        }
    }

    public void testHeaderRequired() {
        try {
            ssp.parse("P-");
            fail("a header should be required");
        } catch(ParseException e) {
            assertTrue(e.getMessage().contains("P-"));
        }
    }

    public void testValidHeaderRequired() {
        try {
            ssp.parse("P-Q");
            fail("a valid header should be required");
        } catch(ParseException e) {
            assertTrue(e.getMessage().contains("P-Q"));
        }
    }

    public void testPhaseRequired() {
        try {
            ssp.parse("-a");
            fail("a phase should be required");
        } catch(ParseException e) {
            assertTrue(e.getMessage().contains("-a"));
        }
    }

    SetSACParser ssp = new SetSACParser();
}
