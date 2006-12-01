package edu.sc.seis.sod.tools;

import java.util.Map;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.ParseException;
import junit.framework.TestCase;

public class DonutParserTest extends TestCase {

    public void testSuccessfulParse() throws ParseException {
        DonutParser dp = new DonutParser();
        Map results = (Map)dp.parse("23/43/0/45");
        assertEquals(results.get("lat"), "23");
        assertEquals(results.get("lon"), "43");
        assertEquals(results.get("min"), "0");
        assertEquals(results.get("max"), "45");
    }

    public void testBadBox() throws JSAPException {
        DonutParser sp = new DonutParser();
        try {
            sp.parse("24/43/2223");
            fail("A donut missing a slash should raise an exception!");
        } catch(ParseException pe) {
            assertTrue(pe.getMessage().contains("24/43/2223"));
        }
    }
}
