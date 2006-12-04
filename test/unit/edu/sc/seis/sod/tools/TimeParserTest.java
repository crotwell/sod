package edu.sc.seis.sod.tools;

import java.util.Map;
import com.martiansoftware.jsap.ParseException;
import junit.framework.TestCase;

public class TimeParserTest extends TestCase {

    public void testBasicTime() throws ParseException {
        TimeParser tp = new TimeParser();
        Map results = (Map)tp.parse("2006-11-19");
        assertEquals("2006", results.get("year"));
        assertEquals("11", results.get("month"));
        assertEquals("19", results.get("day"));
        assertFalse(results.containsKey("now"));
    }

    public void testBadTime() {
        TimeParser tp = new TimeParser();
        try {
            tp.parse("2006-111-19");
            fail("Shouldn't be able to parse a time like that");
        } catch(ParseException pe) {
            assertTrue(pe.getMessage().contains("2006-111-19"));
        }
    }

    public void testNow() throws ParseException {
        TimeParser tp = new TimeParser();
        Map result = (Map)tp.parse("now");
        assertTrue(result.containsKey("now"));
        assertFalse(result.containsKey("year"));
    }
}
