package edu.sc.seis.sod.tools;

import junit.framework.TestCase;
import com.martiansoftware.jsap.ParseException;

public class TimeParserTest extends TestCase {

    public void setUp() {
        tp = new TimeParser();
    }

    public void testBasicTime() throws ParseException {
        assertEquals("2006-11-19T00:00:00.0000Z", tp.parse("2006-11-19"));
    }

    public void testFullTime() throws ParseException {
        assertEquals("2002-07-02T12:30:15.0000Z", tp.parse("2002-07-02-12-30-15"));
    }

    public void testCompactTime() throws ParseException {
        assertEquals("2002-07-02T12:30:15.0000Z", tp.parse("20020702123015"));
    }

    public void testBadTime() {
        try {
            System.out.println(tp.parse("2006-111-19"));
            fail("Shouldn't be able to parse a time like that");
        } catch(ParseException pe) {
            assertTrue(pe.getMessage().contains("2006-111-19"));
        }
    }

    public void testOnlyYear() throws ParseException {
        assertEquals("2006-01-01T00:00:00.0000Z", tp.parse("2006"));
    }

    public void testNow() throws ParseException {
        assertEquals("<now/>", tp.parse("now"));
    }

    private TimeParser tp;
}
